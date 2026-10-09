package com.ludoven.adbtool.agent.artemis

import com.ludoven.adbtool.agent.*
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArtemisObservabilityAdapterTest {

    @Test
    fun `trace adapter maps stage transitions to public stage changed events`() {
        val adapter = ArtemisTraceAdapter(
            runId = "test-run",
            timeProvider = { 1000L },
            sequenceProvider = { 1L }
        )

        val plannerEvent = ArtemisSseEvent(
            type = "stage",
            data = """{"session_id":"test-run","stage":"planner","message":"Planning next steps"}""",
            sessionId = "test-run"
        )
        val plannerResult = adapter.adapt(plannerEvent)
        assertEquals(1, plannerResult.publicEvents.size)
        val stagePayload = plannerResult.publicEvents.first().payload as AgentPublicEventPayload.StageChanged
        assertEquals(AgentPublicStage.PLANNING, stagePayload.stage)
        assertEquals("Planning next steps", plannerResult.summaryDetail)

        val checkerEvent = ArtemisSseEvent(
            type = "stage",
            data = """{"session_id":"test-run","stage":"checker","message":"Verifying outcome"}""",
            sessionId = "test-run"
        )
        val checkerResult = adapter.adapt(checkerEvent)
        assertEquals(1, checkerResult.publicEvents.size)
        val checkerPayload = checkerResult.publicEvents.first().payload as AgentPublicEventPayload.StageChanged
        assertEquals(AgentPublicStage.VERIFYING, checkerPayload.stage)
    }

    @Test
    fun `trace adapter maps tool start and finish and extracts screenshot frame`() {
        val adapter = ArtemisTraceAdapter(
            runId = "test-run",
            timeProvider = { 1000L },
            sequenceProvider = { 1L }
        )

        val dummyBytes = byteArrayOf(1, 2, 3, 4, 5)
        val base64Png = Base64.getEncoder().encodeToString(dummyBytes)

        val startEvent = ArtemisSseEvent(
            type = "action_start",
            data = """{"session_id":"test-run","action":"click","target":"电池选项","screenshot":"$base64Png"}""",
            sessionId = "test-run"
        )
        val startResult = adapter.adapt(startEvent)
        assertNotNull(startResult.step)
        assertEquals(AgentStepStatus.RUNNING, startResult.step?.status)
        assertTrue(startResult.publicEvents.any { it.payload is AgentPublicEventPayload.ToolStarted })
        assertNotNull(startResult.screenshotPng)
        assertEquals(dummyBytes.toList(), startResult.screenshotPng?.toList())

        val finishEvent = ArtemisSseEvent(
            type = "action_finished",
            data = """{"session_id":"test-run","action":"click","status":"success","message":"点击成功"}""",
            sessionId = "test-run"
        )
        val finishResult = adapter.adapt(finishEvent)
        assertNotNull(finishResult.step)
        assertEquals(AgentStepStatus.COMPLETED, finishResult.step?.status)
        assertEquals("点击成功", finishResult.step?.result)
        assertTrue(finishResult.publicEvents.any { it.payload is AgentPublicEventPayload.ToolFinished })
    }

    @Test
    fun `usage adapter converts usage snapshot to agent usage and updates public metrics`() {
        val snapshot = ArtemisUsageSnapshot(
            llmCalls = 5,
            promptTokens = 2400L,
            completionTokens = 350L,
            totalTokens = 2750L
        )

        val usage = ArtemisUsageAdapter.toAgentUsage(snapshot)
        assertEquals(2400, usage.promptTokens)
        assertEquals(350, usage.completionTokens)
        assertEquals(2750, usage.totalTokens)

        val metrics = ArtemisUsageAdapter.toPublicMetrics(snapshot, deviceActions = 3)
        assertEquals(5, metrics.modelCalls)
        assertEquals(3, metrics.deviceActions)
        assertEquals(2750, metrics.totalTokens)

        val initialState = AgentTaskUiState()
        val updatedState = ArtemisUsageAdapter.updateStateWithUsage(
            state = initialState,
            snapshot = snapshot,
            runId = "run-usage-test"
        )

        assertEquals(2750, updatedState.usage.totalTokens)
        assertEquals(5, updatedState.budgetStatus.modelCalls)
    }
}
