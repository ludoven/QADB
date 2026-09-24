package com.ludoven.adbtool.viewmodel

import com.ludoven.adbtool.agent.AgentPublicActivityReducer
import com.ludoven.adbtool.agent.AgentPublicEventPayload
import com.ludoven.adbtool.agent.AgentPublicRunStatus
import com.ludoven.adbtool.agent.AgentAction
import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentStep
import com.ludoven.adbtool.agent.AgentStepStatus
import com.ludoven.adbtool.agent.AgentTaskUiState
import com.ludoven.adbtool.agent.AgentTaskIntentRouter
import com.ludoven.adbtool.agent.AgentModelRole
import com.ludoven.adbtool.agent.AgentCapabilityAttestation
import com.ludoven.adbtool.agent.AgentCapabilityTier
import com.ludoven.adbtool.agent.AgentProviderAuthType
import com.ludoven.adbtool.agent.AgentProviderProfile
import com.ludoven.adbtool.agent.ResolvedAgentProvider
import com.ludoven.adbtool.agent.executionGate
import com.ludoven.adbtool.agent.artemis.ExternalTaskRecord
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AiAgentViewModelTest {
    @Test
    fun `unresolved external record blocks only its bound device after restart`() {
        val record = ExternalTaskRecord("run", "session", "device-a", "flash", "stop_unconfirmed", 1)
        assertTrue(record.blocksNewDeviceTask("device-a"))
        assertFalse(record.blocksNewDeviceTask("device-b"))
        assertFalse(record.copy(status = "stopped").blocksNewDeviceTask("device-a"))
        assertFalse(record.copy(status = "orphaned_released").blocksNewDeviceTask("device-a"))
    }

    @Test
    fun `preflight rejects explicit device operation before model classification`() {
        val gate = AgentTaskIntentRouter().executionGate("打开微信")

        assertFalse(gate.canStart(hasDevice = false, hasProvider = true))
        assertTrue(gate.canStart(hasDevice = true, hasProvider = true))
        assertFalse(gate.canStart(hasDevice = false, hasProvider = false))
    }

    @Test
    fun `responder readiness follows resolved provider authentication`() {
        val missingProvider: ResolvedAgentProvider? = null
        assertFalse(missingProvider.isReadyForAgentResponse())
        assertTrue(resolved(AgentProviderAuthType.NONE, null).isReadyForAgentResponse())
        assertFalse(resolved(AgentProviderAuthType.BEARER, null).isReadyForAgentResponse())
        assertFalse(resolved(AgentProviderAuthType.API_KEY_HEADER, " ").isReadyForAgentResponse())
        assertTrue(resolved(AgentProviderAuthType.BEARER, "provider-secret").isReadyForAgentResponse())
    }

    @Test
    fun `visual agent readiness requires an L3 capability attestation`() {
        val provider = resolved(AgentProviderAuthType.BEARER, "provider-secret")
        val l1 = AgentCapabilityAttestation("fingerprint", AgentCapabilityTier.L1_STRUCTURED_READ, 1, 2)
        val l2 = l1.copy(tier = AgentCapabilityTier.L2_SEMANTIC_AGENT)

        assertFalse(provider.isReadyForVisualAgent(null))
        assertFalse(provider.isReadyForVisualAgent(l1))
        assertFalse(provider.isReadyForVisualAgent(l2))
        assertTrue(provider.isReadyForVisualAgent(l2.copy(tier = AgentCapabilityTier.L3_VISUAL_AGENT)))
    }

    @Test
    fun `cancelling cannot be overwritten by an in-flight orchestrator snapshot`() {
        val synchronizer = AgentPublicStateSynchronizer()
        val baseline = AgentTaskUiState()
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { 1L }
        var state = baseline.copy(
            isRunning = true,
            publicActivity = AgentPublicActivityReducer.reduce(
                baseline.publicActivity,
                adapter.startEvent()
            )
        )
        val snapshotEntered = CountDownLatch(1)
        val releaseSnapshot = CountDownLatch(1)
        val cancelAttempted = CountDownLatch(1)

        val snapshotThread = thread(start = true, name = "agent-snapshot-test") {
            synchronizer.serialized {
                val current = state
                snapshotEntered.countDown()
                releaseSnapshot.await(1, TimeUnit.SECONDS)
                var activity = current.publicActivity
                adapter.eventsFor(
                    baseline.copy(isRunning = true, phase = AgentRunPhase.THINKING)
                ).forEach { event ->
                    activity = AgentPublicActivityReducer.reduce(activity, event)
                }
                state = current.copy(publicActivity = activity)
            }
        }
        assertTrue(snapshotEntered.await(1, TimeUnit.SECONDS))

        val cancelThread = thread(start = true, name = "agent-cancel-test") {
            cancelAttempted.countDown()
            synchronizer.serialized {
                val current = state
                val cancelling = adapter.next(AgentPublicEventPayload.Cancelling)
                state = current.copy(
                    publicActivity = AgentPublicActivityReducer.reduce(current.publicActivity, cancelling)
                )
            }
        }
        assertTrue(cancelAttempted.await(1, TimeUnit.SECONDS))
        releaseSnapshot.countDown()
        snapshotThread.join(1_000)
        cancelThread.join(1_000)

        assertFalse(snapshotThread.isAlive)
        assertFalse(cancelThread.isAlive)
        assertEquals(AgentPublicRunStatus.CANCELLING, state.publicActivity.activeRun?.status)
    }

    @Test
    fun `old run finally cannot clear a newer run after cancellation`() {
        val registry = AgentTaskRunHandleRegistry()
        val baseline = AgentTaskUiState()
        val runA = registry.begin("run-a", AgentOrchestratorPublicEventAdapter("run-a", baseline), 4L, "device")
        val jobA = Job()
        val confirmationA = CompletableDeferred<Boolean>()
        assertTrue(registry.attachJob(runA, jobA))
        assertTrue(registry.registerConfirmation(runA, "step-a", confirmationA))

        val cancelledA = registry.detachActiveForCancellation()
        assertSame(jobA, cancelledA.job)
        assertSame(confirmationA, cancelledA.confirmation)

        val runB = registry.begin("run-b", AgentOrchestratorPublicEventAdapter("run-b", baseline), 6L, "device")
        val jobB = Job()
        val confirmationB = CompletableDeferred<Boolean>()
        assertTrue(registry.attachJob(runB, jobB))
        assertTrue(registry.registerConfirmation(runB, "step-b", confirmationB))

        assertFalse(registry.respondToActiveConfirmation("run-a", "step-a", true))
        assertFalse(registry.respondToActiveConfirmation("run-b", "step-a", true))
        assertFalse(confirmationB.isCompleted)
        assertTrue(registry.respondToActiveConfirmation("run-b", "step-b", false))
        assertTrue(confirmationB.isCompleted)

        assertFalse(registry.finish(runA))
        val cancelledB = registry.detachActiveForCancellation()
        assertSame(jobB, cancelledB.job)
        assertSame(confirmationB, cancelledB.confirmation)
    }

    @Test
    fun `cancelled event synchronously clears confirmation and is idempotent`() {
        val baseline = AgentTaskUiState()
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { 1L }
        var state = baseline.copy(
            isRunning = true,
            pendingConfirmation = confirmationStep(),
            phase = AgentRunPhase.AWAITING_CONFIRMATION,
            publicActivity = AgentPublicActivityReducer.reduce(
                baseline.publicActivity,
                adapter.startEvent()
            )
        )
        state = state.copy(
            publicActivity = AgentPublicActivityReducer.reduce(
                state.publicActivity,
                adapter.next(AgentPublicEventPayload.Cancelling)
            )
        )

        val cancelled = state.applyCancelledEvent(adapter.next(AgentPublicEventPayload.Cancelled))
        val duplicate = cancelled.applyCancelledEvent(adapter.next(AgentPublicEventPayload.Cancelled))

        assertFalse(cancelled.isRunning)
        assertNull(cancelled.pendingConfirmation)
        assertEquals(AgentRunPhase.CANCELLED, cancelled.phase)
        assertEquals(AgentPublicRunStatus.CANCELLED, cancelled.publicActivity.activeRun?.status)
        assertEquals(cancelled, duplicate)
    }

    @Test
    fun `late nonterminal snapshot cannot restore confirmation after cancelling`() {
        val baseline = AgentTaskUiState()
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { 1L }
        var activity = AgentPublicActivityReducer.reduce(
            baseline.publicActivity,
            adapter.startEvent()
        )
        activity = AgentPublicActivityReducer.reduce(
            activity,
            adapter.next(AgentPublicEventPayload.Cancelling)
        )
        val current = baseline.copy(
            isRunning = true,
            pendingConfirmation = null,
            phase = AgentRunPhase.AWAITING_CONFIRMATION,
            publicActivity = activity
        )
        val staleSnapshot = baseline.copy(
            isRunning = true,
            pendingConfirmation = confirmationStep(),
            phase = AgentRunPhase.THINKING
        )

        val merged = mergeAgentOrchestratorSnapshot(current, staleSnapshot, activity)

        assertNull(merged.pendingConfirmation)
        assertEquals(AgentRunPhase.AWAITING_CONFIRMATION, merged.phase)
        assertEquals(AgentPublicRunStatus.CANCELLING, merged.publicActivity.activeRun?.status)
    }

    @Test
    fun `cancelling a lazy job before start still invokes cancellation completion`() {
        var invocationCount = 0
        val job = CoroutineScope(Dispatchers.Unconfined).launch(start = CoroutineStart.LAZY) {
            error("Lazy task must not enter its body after cancellation")
        }
        job.invokeOnAgentCancellation { invocationCount += 1 }

        job.cancel()

        assertFalse(job.start())
        assertEquals(1, invocationCount)
    }

    @Test
    fun `run handle retains first feedback measured before lazy execution`() {
        val registry = AgentTaskRunHandleRegistry()
        val baseline = AgentTaskUiState()
        var nowMs = 104L
        val acceptedAtMs = 100L
        val handle = registry.begin(
            "run",
            AgentOrchestratorPublicEventAdapter("run", baseline),
            (nowMs - acceptedAtMs).coerceAtLeast(0),
            "device"
        )

        nowMs = 1_000L

        assertEquals(4L, handle.firstFeedbackMs)
        assertTrue(nowMs - acceptedAtMs > 100L)
    }

    private fun resolved(
        authType: AgentProviderAuthType,
        authSecret: String?
    ): ResolvedAgentProvider = ResolvedAgentProvider(
        role = AgentModelRole.RESPONDER,
        profile = AgentProviderProfile(
            name = "Test",
            authType = authType,
            authHeaderName = authType.defaultHeaderName,
            baseUrl = "https://example.test/v1",
            defaultModel = "test-model"
        ),
        authSecret = authSecret
    )

    private fun confirmationStep(): AgentStep = AgentStep(
        id = "confirmation",
        action = AgentAction.ClearAppData("com.example"),
        status = AgentStepStatus.AWAITING_CONFIRMATION
    )
}
