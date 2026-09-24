package com.ludoven.adbtool.viewmodel

import com.ludoven.adbtool.agent.AgentCapabilities
import com.ludoven.adbtool.agent.AgentCapabilityAttestationStore
import com.ludoven.adbtool.agent.AgentCapabilityTier
import com.ludoven.adbtool.agent.AgentEngineKind
import com.ludoven.adbtool.agent.AgentMessage
import com.ludoven.adbtool.agent.AgentMessageRole
import com.ludoven.adbtool.agent.AgentProviderAuthType
import com.ludoven.adbtool.agent.AgentProviderProfile
import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentStopOutcome
import com.ludoven.adbtool.agent.AgentStep
import com.ludoven.adbtool.agent.AgentTaskRunner
import com.ludoven.adbtool.agent.CancellableAgentTaskRunner
import com.ludoven.adbtool.agent.AgentTaskRunnerReadiness
import com.ludoven.adbtool.agent.AgentRunnerReadiness
import com.ludoven.adbtool.agent.AgentTaskUiState
import com.ludoven.adbtool.agent.AiConfigRepository
import com.ludoven.adbtool.agent.AgentProviderRepository
import com.ludoven.adbtool.agent.ArtemisEngineConfiguration
import com.ludoven.adbtool.agent.ArtemisEnginePreferences
import com.ludoven.adbtool.agent.SecretStore
import com.ludoven.adbtool.agent.NoopAgentSessionHistoryStore
import com.ludoven.adbtool.agent.AgentSessionHistoryStore
import com.ludoven.adbtool.agent.SqliteAgentSessionHistoryStore
import com.ludoven.adbtool.agent.artemis.DeviceLeaseManager
import com.ludoven.adbtool.util.l10n
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.runBlocking

/**
 * End-to-end regression for the AI Agent conversation chain:
 *
 * startTask → optimistic user message → engine first-frame snapshot publish →
 * the published snapshot must still carry the user message, and the final
 * viewModel state must keep it too.
 *
 * Guards against the historical defect where executeTask received the
 * pre-send initial state, so the engine's first snapshot erased the
 * optimistic user message from the conversation.
 */
class AiAgentViewModelStartTaskTest {

    private val preferenceNodes = mutableListOf<Preferences>()

    @AfterTest
    fun cleanupPreferences() {
        preferenceNodes.forEach { node -> runCatching { node.removeNode() } }
        preferenceNodes.clear()
    }

    @Test
    fun `engine first frame snapshot preserves the accepted user message`() {
        val runner = FirstFrameRecordingRunner()
        val viewModel = newViewModel(runner)
        val task = "打开设置并关闭蓝牙"

        viewModel.startTask(task, "emulator-5554")

        val accepted = runner.awaitInitialState()
        val userMessage = accepted.messages.lastOrNull()
        assertNotNull(userMessage, "Engine initial state must contain the accepted user message")
        assertEquals(AgentMessageRole.USER, userMessage.role)
        assertEquals(task, userMessage.text)
        assertNotNull(userMessage.runId, "Accepted user message must be tagged with the run id")

        assertTrue(
            runner.awaitFirstFramePublished(),
            "First frame snapshot was never handed to onState"
        )
        runner.finishRun()

        val finalState = viewModel.awaitUntil(
            "run completes",
            diagnostics = { "; runnerFailure=${runner.runFailure}" }
        ) {
            it.phase == AgentRunPhase.COMPLETED && !it.isRunning
        }
        assertTrue(
            finalState.messages.any { it.role == AgentMessageRole.USER && it.text == task },
            "Final viewModel state must retain the user message after engine snapshots publish"
        )
        assertTrue(finalState.messages.any { it.role == AgentMessageRole.ASSISTANT })
    }

    @Test
    fun `continuation requires a finished same-device segment and starts with fresh context`() {
        val database = Files.createTempFile("agent-continuation", ".db").toFile()
        try {
            SqliteAgentSessionHistoryStore(database).use { history ->
                history.start("parent", "Send a message", "emulator-5554", 1)
                history.finish("parent", AgentTaskUiState(phase = AgentRunPhase.COMPLETED))
                history.start("unconfirmed", "Previous Artemis action", "emulator-5554", 2)
                history.finish("unconfirmed", AgentTaskUiState(
                    phase = AgentRunPhase.CANCELLED,
                    stopOutcome = AgentStopOutcome.UNCONFIRMED
                ))
                val runner = FirstFrameRecordingRunner()
                val viewModel = newViewModel(runner, sessionHistoryStore = history)
                assertEquals(false, viewModel.startTask("Next action", "emulator-5554", parentRunId = "parent"))
                assertEquals(false, viewModel.startTask("Next action", "emulator-5556", parentRunId = "parent",
                    priorOutcomeChecked = true))
                assertEquals(false, viewModel.startTask("Next action", "emulator-5554", parentRunId = "unconfirmed",
                    priorOutcomeChecked = true))
                assertEquals(false, viewModel.state.value.isRunning)
                assertTrue(viewModel.startTask("Check result and continue", "emulator-5554", parentRunId = "parent",
                    priorOutcomeChecked = true))
                val accepted = runner.awaitInitialState()
                assertEquals(listOf("Check result and continue"), accepted.messages.map { it.text })
                assertEquals("parent", history.find(accepted.publicActivity.activeRunId.orEmpty())?.parentRunId)
                runner.finishRun()
                viewModel.awaitUntil("continuation finishes") { !it.isRunning && it.phase == AgentRunPhase.COMPLETED }
            }
        } finally {
            database.delete()
        }
    }

    @Test
    fun `cancelTask resets isRunning and allows a new run to start`() {
        val runner = GatedBlockingRunner()
        val viewModel = newViewModel(runner)
        val firstTask = "截图并分析当前页面"

        viewModel.startTask(firstTask, "emulator-5554")
        viewModel.awaitUntil("run starts") { it.isRunning }

        viewModel.cancelTask()

        val cancelledState = viewModel.awaitUntil("run cancelled") {
            !it.isRunning && it.phase == AgentRunPhase.CANCELLED
        }
        assertTrue(
            cancelledState.messages.any { it.role == AgentMessageRole.USER && it.text == firstTask },
            "Cancelled run must retain the user message"
        )

        val secondTask = "返回桌面"
        viewModel.startTask(secondTask, "emulator-5554")
        val restarted = viewModel.awaitUntil("second run starts") { it.isRunning }
        assertTrue(
            restarted.messages.any { it.role == AgentMessageRole.USER && it.text == secondTask },
            "Second run state must contain the new user message"
        )
        assertTrue(restarted.messages.none { it.text == firstTask }, "A new run must not inherit prior context")

        runner.release()
        val completed = viewModel.awaitUntil(
            "second run completes",
            diagnostics = { "; runnerFailure=${runner.runFailure}" }
        ) {
            it.phase == AgentRunPhase.COMPLETED && !it.isRunning
        }
        assertTrue(
            completed.messages.any { it.role == AgentMessageRole.USER && it.text == secondTask },
            "Completed second run must retain its user message"
        )
    }

    @Test
    fun `new task while running requests cancellation without erasing the active run`() {
        val runner = GatedBlockingRunner()
        val viewModel = newViewModel(runner)
        val task = "读取当前页面"

        viewModel.startTask(task, "emulator-5554")
        viewModel.awaitUntil("run starts") { it.isRunning }
        viewModel.newTask()

        val cancelled = viewModel.awaitUntil("run cancelled") { !it.isRunning && it.phase == AgentRunPhase.CANCELLED }
        assertTrue(cancelled.messages.any { it.role == AgentMessageRole.USER && it.text == task })
    }

    @Test
    fun `unconfirmed remote stop blocks the same device after new task`() {
        val viewModel = newViewModel(UnconfirmedStopRunner())
        viewModel.startTask("查看设备", "emulator-5554")
        viewModel.awaitUntil("run starts") { it.isRunning }

        viewModel.cancelTask()
        viewModel.awaitUntil("remote stop is unconfirmed") {
            !it.isRunning && it.stopOutcome == AgentStopOutcome.UNCONFIRMED
        }
        viewModel.newTask()
        assertEquals(AgentStopOutcome.NONE, viewModel.state.value.stopOutcome)
        viewModel.startTask("第二个任务", "emulator-5554")
        assertTrue(viewModel.state.value.errorMessage.orEmpty().contains(l10n("未决", "unresolved")))
        viewModel.startTask("另一台设备的任务", "emulator-5556")
        viewModel.awaitUntil("other device run starts") {
            it.isRunning && it.boundDeviceId == "emulator-5556"
        }
    }

    @Test
    fun `runner exception closes the visible run as failed`() {
        val viewModel = newViewModel(ThrowingRunner())

        viewModel.startTask("执行会失败的任务", "emulator-5554")

        val failed = viewModel.awaitUntil("runner exception is visible") {
            !it.isRunning && it.phase == AgentRunPhase.FAILED
        }
        assertEquals(l10n("智能体任务失败，请重试。", "The agent task failed. Please retry."), failed.errorMessage)
        assertNotNull(failed.failure)
    }

    @Test
    fun `enabled Artemis blocks screenshot fallback when external engine is unavailable`() {
        val artemisPreferences = ArtemisEnginePreferences(newPreferencesNode()).also {
            it.setConfiguration(ArtemisEngineConfiguration(enabled = true))
        }
        val viewModel = newViewModel(
            runner = UnavailableArtemisReadinessRunner(),
            artemisPreferences = artemisPreferences,
            requireReady = false
        )

        assertTrue(viewModel.modelConfigured.value)
        assertEquals(false, viewModel.configurationReady.value)
        assertTrue(viewModel.externalEngineSelected.value)
    }

    @Test
    fun `stale Artemis readiness cannot replace a newer screenshot selection`() {
        val artemisPreferences = ArtemisEnginePreferences(newPreferencesNode())
        val runner = DelayedArtemisReadinessRunner()
        val viewModel = newViewModel(runner, artemisPreferences)

        artemisPreferences.setConfiguration(ArtemisEngineConfiguration(enabled = true))
        assertTrue(runner.probeStarted.await(10, TimeUnit.SECONDS), "Artemis probe did not start")
        artemisPreferences.setConfiguration(ArtemisEngineConfiguration(enabled = false))
        viewModel.refreshConfigurationStatus()
        val deadline = System.currentTimeMillis() + 10_000
        while (!viewModel.configurationChecked.value || viewModel.externalEngineSelected.value) {
            check(System.currentTimeMillis() < deadline) { "New screenshot readiness did not settle" }
            Thread.sleep(20)
        }
        runner.releaseProbe()
        Thread.sleep(200)

        assertTrue(viewModel.configurationReady.value)
        assertEquals(false, viewModel.externalEngineSelected.value)
    }

    /** Records the initial state handed to the engine, then mirrors it back as the first frame. */
    private class FirstFrameRecordingRunner : AgentTaskRunner {
        private val initialStateLatch = CountDownLatch(1)

        @Volatile
        private var initialStateValue: AgentTaskUiState? = null
        private val firstFramePublished = CountDownLatch(1)
        private val finishRequested = CountDownLatch(1)

        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState {
            initialStateValue = initialState
            initialStateLatch.countDown()
            try {
                // First engine frame: an observation snapshot derived from the accepted state.
                onState(initialState.copy(phase = AgentRunPhase.THINKING))
                firstFramePublished.countDown()
                assertTrue(
                    finishRequested.await(10, TimeUnit.SECONDS),
                    "Test never released the runner"
                )
                return completedState(initialState, task).also { onState(it) }
            } catch (t: Throwable) {
                runFailure = t
                throw t
            }
        }

        @Volatile
        var runFailure: Throwable? = null
            private set

        fun awaitInitialState(): AgentTaskUiState {
            assertTrue(
                initialStateLatch.await(10, TimeUnit.SECONDS),
                "Engine run() was never invoked"
            )
            return requireNotNull(initialStateValue)
        }

        fun awaitFirstFramePublished(): Boolean =
            firstFramePublished.await(10, TimeUnit.SECONDS)

        fun finishRun() {
            finishRequested.countDown()
        }
    }

    /** Blocks inside the run until [release], so cancellation can be exercised. */
    private class GatedBlockingRunner : AgentTaskRunner {
        private val gate = CompletableDeferred<Unit>()

        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState {
            try {
                onState(initialState.copy(phase = AgentRunPhase.THINKING))
                gate.await()
                return completedState(initialState, task).also { onState(it) }
            } catch (t: Throwable) {
                if (t !is kotlinx.coroutines.CancellationException) runFailure = t
                throw t
            }
        }

        @Volatile
        var runFailure: Throwable? = null
            private set

        fun release() {
            gate.complete(Unit)
        }
    }

    private class UnconfirmedStopRunner : CancellableAgentTaskRunner {
        private val gate = CompletableDeferred<Unit>()

        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState {
            onState(initialState.copy(phase = AgentRunPhase.THINKING))
            gate.await()
            return completedState(initialState, task)
        }

        override suspend fun cancel(runId: String): Boolean = false
    }

    private class ThrowingRunner : AgentTaskRunner {
        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState = throw IllegalStateException("bridge rejected a malformed request")
    }

    private class UnavailableArtemisReadinessRunner : AgentTaskRunner, AgentTaskRunnerReadiness {
        override suspend fun readiness(): AgentRunnerReadiness = AgentRunnerReadiness(
            ready = false,
            message = "Artemis unavailable",
            selectedEngine = AgentEngineKind.ARTEMIS
        )

        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState = error("Screenshot fallback must not run")
    }

    private class DelayedArtemisReadinessRunner : AgentTaskRunner, AgentTaskRunnerReadiness {
        val probeStarted = CountDownLatch(1)
        private val probeResult = CompletableDeferred<Unit>()

        override suspend fun readiness(): AgentRunnerReadiness {
            probeStarted.countDown()
            probeResult.await()
            return AgentRunnerReadiness(ready = true, selectedEngine = AgentEngineKind.ARTEMIS)
        }

        fun releaseProbe() { probeResult.complete(Unit) }

        override suspend fun run(
            task: String,
            deviceId: String,
            initialState: AgentTaskUiState,
            runId: String?,
            acceptedAtMs: Long?,
            firstFeedbackMs: Long?,
            onState: (AgentTaskUiState) -> Unit,
            confirmSensitiveAction: suspend (AgentStep) -> Boolean
        ): AgentTaskUiState = error("No task should be submitted")
    }

    private fun AiAgentViewModel.awaitUntil(
        description: String,
        timeoutMs: Long = 10_000,
        diagnostics: () -> String = { "" },
        condition: (AgentTaskUiState) -> Boolean
    ): AgentTaskUiState {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val snapshot = state.value
            if (condition(snapshot)) return snapshot
            Thread.sleep(20)
        }
        error("Timed out waiting for: $description; last state=${state.value}${diagnostics()}")
    }

    private fun newViewModel(
        runner: AgentTaskRunner,
        artemisPreferences: ArtemisEnginePreferences = ArtemisEnginePreferences(newPreferencesNode()),
        requireReady: Boolean = true,
        sessionHistoryStore: AgentSessionHistoryStore = NoopAgentSessionHistoryStore
    ): AiAgentViewModel = runBlocking {
        val configRepository = AiConfigRepository(
            preferences = newPreferencesNode(),
            secretStore = InMemorySecretStore()
        )
        val providerRepository = AgentProviderRepository(
            preferences = newPreferencesNode(),
            secrets = InMemorySecretStore(),
            legacy = configRepository,
            capabilityAttestations = AgentCapabilityAttestationStore(newPreferencesNode())
        )
        val profile = AgentProviderProfile(
            name = "Test L3 Provider",
            authType = AgentProviderAuthType.NONE,
            baseUrl = "https://example.test/v1",
            defaultModel = "test-model",
            capabilities = AgentCapabilities(vision = true)
        )
        providerRepository.upsert(profile, apiKey = null)
        providerRepository.attestCapabilities(profile, AgentCapabilityTier.L3_VISUAL_AGENT)

        val viewModel = AiAgentViewModel(
            configRepository = configRepository,
            agentTaskRunner = runner,
            providerRepository = providerRepository,
            deviceLeaseManager = DeviceLeaseManager(),
            artemisPreferences = artemisPreferences,
            sessionHistoryStore = sessionHistoryStore
        )
        val readyDeadline = System.currentTimeMillis() + 10_000
        while (if (requireReady) !viewModel.configurationReady.value else !viewModel.configurationChecked.value) {
            check(System.currentTimeMillis() < readyDeadline) {
                "Timed out waiting for configurationReady; " +
                    "state=${viewModel.state.value} provider=${providerRepository.profiles.value}"
            }
            Thread.sleep(20)
        }
        viewModel
    }

    private fun newPreferencesNode(): Preferences =
        Preferences.userRoot()
            .node("adbtool-test-${UUID.randomUUID()}")
            .also { preferenceNodes.add(it) }
}

private fun completedState(initialState: AgentTaskUiState, task: String): AgentTaskUiState =
    initialState.copy(
        phase = AgentRunPhase.COMPLETED,
        isRunning = false,
        messages = initialState.messages + AgentMessage(
            id = "assistant-${UUID.randomUUID()}",
            role = AgentMessageRole.ASSISTANT,
            text = "已完成：$task"
        )
    )

private class InMemorySecretStore : SecretStore {
        private val secrets = ConcurrentHashMap<String, String>()

        override fun write(account: String, secret: String) {
            secrets[account] = secret
        }

        override fun read(account: String): String? = secrets[account]

        override fun delete(account: String) {
            secrets.remove(account)
        }
    }
