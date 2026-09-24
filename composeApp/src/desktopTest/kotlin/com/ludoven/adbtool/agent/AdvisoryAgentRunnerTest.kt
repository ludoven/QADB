package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class AdvisoryAgentRunnerTest {
    @Test
    fun askUsesOnlyTextAndNeverRequiresDeviceConfirmation() = runBlocking {
        var request: AgentModelRequestContext? = null
        val model = object : AgentModelGateway {
            override suspend fun plan(context: AgentModelRequestContext): AgentPlanDecision? = null
            override suspend fun decide(context: AgentModelRequestContext, preferVision: Boolean): AgentModelDecision =
                error("Action planning must not be called")
            override suspend fun recover(context: AgentModelRequestContext, failure: String): AgentPlanDecision? = null
            override suspend fun summarize(context: AgentModelRequestContext): AgentCompactionResult? = null
            override suspend fun streamUserAnswer(
                context: AgentModelRequestContext,
                preferVision: Boolean,
                onText: (String) -> Unit
            ): AgentUserAnswerStreamResult {
                assertFalse(preferVision)
                request = context
                onText("A text answer")
                return AgentUserAnswerStreamResult(
                    AgentModelDecision(AgentAction.Finish("A text answer"), usedVision = false),
                    AgentStreamingMode.DISABLED,
                    usedStreaming = false
                )
            }
        }
        val result = AdvisoryAgentRunner(model).run(
            task = "What is Android?",
            deviceId = "should-not-be-used",
            initialState = AgentTaskUiState(taskMode = AgentTaskMode.ASK),
            confirmSensitiveAction = { error("No approval is needed") }
        )

        assertEquals(AgentRunPhase.COMPLETED, result.phase)
        assertNull(result.boundDeviceId)
        assertNull(request?.observation?.screenshotPng)
        assertEquals("", request?.observation?.uiHierarchy)
        assertTrue(result.messages.last().text.contains("A text answer"))
    }

    @Test
    fun authorizedPlanReadsCurrentDeviceTextOnceWithoutDispatchingActions() = runBlocking {
        var observations = 0
        var request: AgentModelRequestContext? = null
        val model = object : AgentModelGateway {
            override suspend fun plan(context: AgentModelRequestContext): AgentPlanDecision? = null
            override suspend fun decide(context: AgentModelRequestContext, preferVision: Boolean): AgentModelDecision =
                error("Device actions must not be planned")
            override suspend fun recover(context: AgentModelRequestContext, failure: String): AgentPlanDecision? = null
            override suspend fun summarize(context: AgentModelRequestContext): AgentCompactionResult? = null
            override suspend fun streamUserAnswer(
                context: AgentModelRequestContext,
                preferVision: Boolean,
                onText: (String) -> Unit
            ): AgentUserAnswerStreamResult {
                assertFalse(preferVision)
                request = context
                onText("Review the current screen")
                return AgentUserAnswerStreamResult(
                    AgentModelDecision(AgentAction.Finish("Review the current screen"), usedVision = false),
                    AgentStreamingMode.DISABLED,
                    usedStreaming = false
                )
            }
        }
        val runner = AdvisoryAgentRunner(model) { deviceId ->
            assertEquals("serial", deviceId)
            observations++
            AgentObservation(null, "<screen>Settings</screen>", "Settings", 0, 0)
        }
        val result = runner.run(
            task = "How should I proceed?",
            deviceId = "serial",
            initialState = AgentTaskUiState(taskMode = AgentTaskMode.PLAN, deviceEvidenceAuthorized = true),
            confirmSensitiveAction = { error("No action approval is needed") }
        )
        assertEquals(1, observations)
        assertEquals("serial", result.boundDeviceId)
        assertEquals("<screen>Settings</screen>", request?.observation?.uiHierarchy)
        assertNull(request?.observation?.screenshotPng)
        assertTrue(result.messages.last().text.contains("界面信息"))
    }
}
