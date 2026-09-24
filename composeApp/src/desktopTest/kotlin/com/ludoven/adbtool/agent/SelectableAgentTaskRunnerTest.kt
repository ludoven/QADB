package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.util.prefs.Preferences
import kotlinx.coroutines.runBlocking

class SelectableAgentTaskRunnerTest {
    @Test
    fun askModeRoutesToTextRunnerWithoutInvokingDeviceRunner() = runBlocking {
        var advisoryCalls = 0
        val deviceRunner = object : AgentTaskRunner {
            override suspend fun run(
                task: String,
                deviceId: String,
                initialState: AgentTaskUiState,
                runId: String?,
                acceptedAtMs: Long?,
                firstFeedbackMs: Long?,
                onState: (AgentTaskUiState) -> Unit,
                confirmSensitiveAction: suspend (AgentStep) -> Boolean
            ): AgentTaskUiState = error("Device runner must not start")
        }
        val textRunner = object : AgentTaskRunner {
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
                advisoryCalls++
                return initialState.copy(phase = AgentRunPhase.COMPLETED)
            }
        }
        val prefs = Preferences.userRoot().node("qadb-agent-mode-test-${System.nanoTime()}")
        try {
            val runner = SelectableAgentTaskRunner(
                screenshotRunner = deviceRunner,
                artemisSettings = ArtemisEnginePreferences(prefs),
                advisoryRunner = textRunner
            )
            val result = runner.run("question", "", AgentTaskUiState(taskMode = AgentTaskMode.ASK))
            assertEquals(AgentRunPhase.COMPLETED, result.phase)
            assertEquals(1, advisoryCalls)
        } finally {
            prefs.removeNode()
        }
    }

    @Test
    fun readinessRetriesColdStartTransportFailures() = runBlocking {
        var attempts = 0

        val result = awaitAgentRunnerReadiness(maxAttempts = 3, retryDelayMs = 0) {
            attempts += 1
            if (attempts < 3) error("service is starting")
            AgentRunnerReadiness(ready = true)
        }

        assertTrue(result.ready)
        assertEquals(3, attempts)
    }

    @Test
    fun readinessDoesNotRetryAuthoritativeBlockers() = runBlocking {
        var attempts = 0

        val result = awaitAgentRunnerReadiness(maxAttempts = 3, retryDelayMs = 0) {
            attempts += 1
            AgentRunnerReadiness(ready = false, message = "Provider unavailable")
        }

        assertFalse(result.ready)
        assertEquals("Provider unavailable", result.message)
        assertEquals(1, attempts)
    }

    @Test
    fun readinessRetriesEmptyColdStartState() = runBlocking {
        var attempts = 0

        val result = awaitAgentRunnerReadiness(maxAttempts = 2, retryDelayMs = 0) {
            attempts += 1
            AgentRunnerReadiness(ready = attempts == 2)
        }

        assertTrue(result.ready)
        assertEquals(2, attempts)
    }

    @Test
    fun readinessDefaultsToScreenshotEngine() {
        val readiness = AgentRunnerReadiness(ready = true)

        assertEquals(AgentEngineKind.SCREENSHOT, readiness.selectedEngine)
    }

    @Test
    fun readinessCanReportArtemisAsTheAdmittedEngine() {
        val readiness = AgentRunnerReadiness(
            ready = true,
            selectedEngine = AgentEngineKind.ARTEMIS
        )

        assertEquals(AgentEngineKind.ARTEMIS, readiness.selectedEngine)
    }
}
