package com.ludoven.adbtool.agent

import com.ludoven.adbtool.agent.artemis.QadbBridgeException
import com.ludoven.adbtool.viewmodel.AgentOrchestratorPublicEventAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentPublicActivityTest {
    @Test
    fun `reducer rejects stale sequence old run and terminal events`() {
        var state = AgentPublicActivityState()
        state = reduce(state, "run-1", 1, AgentPublicEventPayload.RunStarted)
        state = reduce(
            state,
            "run-1",
            2,
            AgentPublicEventPayload.StageChanged(AgentPublicStage.READING)
        )
        val stale = reduce(
            state,
            "run-1",
            2,
            AgentPublicEventPayload.StageChanged(AgentPublicStage.EXECUTING)
        )
        assertEquals(state, stale)

        state = reduce(state, "run-2", 1, AgentPublicEventPayload.RunStarted)
        val oldRunLate = reduce(
            state,
            "run-1",
            3,
            AgentPublicEventPayload.StageChanged(AgentPublicStage.EXECUTING)
        )
        assertEquals(state, oldRunLate)

        state = reduce(state, "run-2", 2, AgentPublicEventPayload.Completed)
        val terminalLate = reduce(
            state,
            "run-2",
            3,
            AgentPublicEventPayload.ResponseDelta("late")
        )
        assertEquals(state, terminalLate)
        assertEquals(AgentPublicRunStatus.COMPLETED, state.activeRun?.status)
    }

    @Test
    fun `cancelling run rejects late tools and response deltas`() {
        var state = reduce(AgentPublicActivityState(), "run", 1, AgentPublicEventPayload.RunStarted)
        state = reduce(state, "run", 2, AgentPublicEventPayload.Cancelling)

        val lateTool = reduce(
            state,
            "run",
            3,
            AgentPublicEventPayload.ToolStarted(AgentPublicToolSummary(AgentPublicToolKind.TAP))
        )
        val lateDelta = reduce(state, "run", 4, AgentPublicEventPayload.ResponseDelta("late"))

        assertEquals(state, lateTool)
        assertEquals(state, lateDelta)
        assertEquals(AgentPublicRunStatus.CANCELLING, state.activeRun?.status)
        state = reduce(state, "run", 5, AgentPublicEventPayload.Cancelled)
        assertEquals(AgentPublicRunStatus.CANCELLED, state.activeRun?.status)
    }

    @Test
    fun `cancelling run accepts every terminal outcome`() {
        val outcomes = listOf(
            AgentPublicEventPayload.Completed to AgentPublicRunStatus.COMPLETED,
            AgentPublicEventPayload.Cancelled to AgentPublicRunStatus.CANCELLED,
            AgentPublicEventPayload.Failed(agentFailureFrom("verification failed")) to
                AgentPublicRunStatus.FAILED
        )

        outcomes.forEachIndexed { index, (payload, expectedStatus) ->
            val runId = "run-$index"
            var state = reduce(
                AgentPublicActivityState(),
                runId,
                1,
                AgentPublicEventPayload.RunStarted
            )
            state = reduce(state, runId, 2, AgentPublicEventPayload.Cancelling)
            state = reduce(state, runId, 3, payload)

            assertEquals(expectedStatus, state.activeRun?.status)
            assertEquals(3L, state.activeRun?.finishedAtMs)
        }
    }

    @Test
    fun `activity history is capped at thirty while response deltas are preserved`() {
        var state = reduce(
            AgentPublicActivityState(),
            "run",
            1,
            AgentPublicEventPayload.RunStarted
        )
        repeat(40) { index ->
            state = reduce(
                state,
                "run",
                index + 2L,
                AgentPublicEventPayload.StageChanged(
                    if (index % 2 == 0) AgentPublicStage.READING else AgentPublicStage.PLANNING
                )
            )
        }
        assertEquals(30, state.activeRun?.activities?.size)

        repeat(500) { index ->
            state = reduce(
                state,
                "run",
                index + 42L,
                AgentPublicEventPayload.ResponseDelta("x")
            )
        }
        assertEquals(500, state.activeRun?.responseText?.length)
        assertEquals(30, state.activeRun?.activities?.size)
    }

    @Test
    fun `failure and confirmation request prefer expanded presentation`() {
        var state = reduce(
            AgentPublicActivityState(),
            "run-confirm",
            1,
            AgentPublicEventPayload.RunStarted
        )
        state = reduce(
            state,
            "run-confirm",
            2,
            AgentPublicEventPayload.ConfirmationRequested(
                AgentPublicToolSummary(AgentPublicToolKind.CLEAR_APP_DATA)
            )
        )
        assertTrue(state.activeRun?.preferredExpanded == true)
        assertEquals(AgentPublicRunStatus.WAITING_CONFIRMATION, state.activeRun?.status)

        state = reduce(state, "run-failed", 1, AgentPublicEventPayload.RunStarted)
        state = reduce(
            state,
            "run-failed",
            2,
            AgentPublicEventPayload.Failed(agentFailureFrom("HTTP 429 rate limited"))
        )
        assertTrue(state.activeRun?.preferredExpanded == true)
        assertEquals(AgentFailureCategory.RATE_LIMIT, state.activeRun?.failure?.category)
    }

    @Test
    fun `public tool summary never exposes action arguments or typed text`() {
        val tap = AgentAction.Tap(123, 456, "observation-secret").toPublicToolSummary()
        val tapElement = AgentAction.TapElement("observation-secret", "element-secret").toPublicToolSummary()
        val input = AgentAction.InputText("private message", "observation-secret", "field-secret")
            .toPublicToolSummary()
        val launch = AgentAction.LaunchPackage("com.private.application").toPublicToolSummary()

        assertEquals(AgentPublicToolKind.TAP, tap.kind)
        assertEquals(tap, tapElement)
        assertEquals(15, input.inputCharacterCount)
        assertEquals(AgentPublicToolKind.OPEN_APP, launch.kind)
        listOf(tap, tapElement, input, launch).forEach { summary ->
            val publicText = summary.toString()
            assertFalse(publicText.contains("123"))
            assertFalse(publicText.contains("element-secret"))
            assertFalse(publicText.contains("private message"))
            assertFalse(publicText.contains("com.private"))
        }
    }

    @Test
    fun `diagnostic sanitization removes credentials urls and caps output`() {
        val failure = agentFailureFrom(
            "Authorization: Bearer top-secret api_key=key-secret " +
                "https://example.com/private?token=also-secret " + "x".repeat(500)
        )

        assertEquals(AgentFailureCategory.AUTHENTICATION, failure.category)
        assertEquals(AgentFailureAction.OPEN_MODEL_SETTINGS, failure.suggestedAction)
        assertTrue(failure.technicalDetail.orEmpty().length <= 300)
        assertFalse(failure.technicalDetail.orEmpty().contains("top-secret"))
        assertFalse(failure.technicalDetail.orEmpty().contains("key-secret"))
        assertFalse(failure.technicalDetail.orEmpty().contains("example.com"))
        assertNull(AgentRunPresentation("run", 1, 1).failure)
    }

    @Test
    fun `localized authentication failure opens model settings`() {
        val failure = agentFailureFrom("模型服务认证失败，请检查模型设置。")

        assertEquals(AgentFailureCategory.AUTHENTICATION, failure.category)
        assertEquals(AgentFailureAction.OPEN_MODEL_SETTINGS, failure.suggestedAction)
        assertFalse(failure.retryable)
    }

    @Test
    fun `snapshot adapter prefers typed failure over localized message guessing`() {
        val baseline = AgentTaskUiState()
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { 1L }
        adapter.startEvent()

        val events = adapter.eventsFor(
            baseline.copy(
                phase = AgentRunPhase.FAILED,
                errorMessage = "任务未能完成",
                failure = agentFailure(
                    AgentFailureCategory.PROTOCOL,
                    code = AgentFailureCode.PROTOCOL_INVALID
                )
            )
        )
        val failed = events.mapNotNull { it.payload as? AgentPublicEventPayload.Failed }.single()

        assertEquals(AgentFailureCategory.PROTOCOL, failed.failure.category)
        assertEquals(AgentFailureCode.PROTOCOL_INVALID, failed.failure.code)
        assertEquals(AgentFailureAction.RETRY, failed.failure.suggestedAction)
    }

    @Test
    fun `snapshot adapter emits semantic events without action arguments`() {
        var clock = 100L
        val baseline = AgentTaskUiState()
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { ++clock }
        assertTrue(adapter.startEvent().payload is AgentPublicEventPayload.RunStarted)

        val runningStep = AgentStep(
            id = "step",
            action = AgentAction.InputText("private body", "observation", "element"),
            status = AgentStepStatus.RUNNING,
            containsSensitiveData = true
        )
        val runningEvents = adapter.eventsFor(
            baseline.copy(
                isRunning = true,
                phase = AgentRunPhase.EXECUTING,
                steps = listOf(runningStep),
                budgetStatus = AgentBudgetStatus(modelCalls = 1)
            )
        )
        val toolStarted = runningEvents.map { it.payload }
            .filterIsInstance<AgentPublicEventPayload.ToolStarted>()
            .single()
        assertEquals(AgentPublicToolKind.INPUT_TEXT, toolStarted.tool.kind)
        assertEquals(12, toolStarted.tool.inputCharacterCount)
        assertFalse(runningEvents.toString().contains("private body"))
        assertFalse(runningEvents.toString().contains("observation"))
        assertFalse(runningEvents.toString().contains("element"))

        val completedEvents = adapter.eventsFor(
            baseline.copy(
                phase = AgentRunPhase.COMPLETED,
                steps = listOf(runningStep.copy(status = AgentStepStatus.COMPLETED)),
                messages = listOf(AgentMessage("answer", AgentMessageRole.ASSISTANT, "done"))
            )
        )
        assertTrue(completedEvents.any { it.payload is AgentPublicEventPayload.ResponseDelta })
        assertTrue(completedEvents.any { it.payload is AgentPublicEventPayload.ResponseCompleted })
        assertTrue(completedEvents.any { it.payload is AgentPublicEventPayload.Completed })
    }

    @Test
    fun `snapshot adapter emits assistant growth as suffix and completes only at terminal state`() {
        val oldMessage = AgentMessage(
            id = "old-answer",
            role = AgentMessageRole.ASSISTANT,
            text = "old"
        )
        val baseline = AgentTaskUiState(messages = listOf(oldMessage))
        val adapter = AgentOrchestratorPublicEventAdapter("run", baseline) { 1L }
        val streamingMessage = AgentMessage(
            id = "stream-answer",
            role = AgentMessageRole.ASSISTANT,
            text = "你"
        )

        val first = adapter.eventsFor(
            baseline.copy(
                isRunning = true,
                phase = AgentRunPhase.THINKING,
                messages = listOf(oldMessage, streamingMessage)
            )
        )
        assertEquals(
            listOf("你"),
            first.mapNotNull { (it.payload as? AgentPublicEventPayload.ResponseDelta)?.delta }
        )
        assertFalse(first.any { it.payload is AgentPublicEventPayload.ResponseCompleted })

        val grownMessage = streamingMessage.copy(text = "你好")
        val growth = adapter.eventsFor(
            baseline.copy(
                isRunning = true,
                phase = AgentRunPhase.THINKING,
                messages = listOf(oldMessage, grownMessage)
            )
        )
        assertEquals(
            listOf("好"),
            growth.mapNotNull { (it.payload as? AgentPublicEventPayload.ResponseDelta)?.delta }
        )

        val unchanged = adapter.eventsFor(
            baseline.copy(
                isRunning = true,
                phase = AgentRunPhase.THINKING,
                messages = listOf(oldMessage, grownMessage)
            )
        )
        assertFalse(unchanged.any { it.payload is AgentPublicEventPayload.ResponseDelta })

        adapter.eventsFor(
            baseline.copy(isRunning = true, phase = AgentRunPhase.THINKING)
        )
        val restored = adapter.eventsFor(
            baseline.copy(
                isRunning = true,
                phase = AgentRunPhase.THINKING,
                messages = listOf(oldMessage, grownMessage)
            )
        )
        assertFalse(restored.any { it.payload is AgentPublicEventPayload.ResponseDelta })

        val terminal = adapter.eventsFor(
            baseline.copy(
                isRunning = false,
                phase = AgentRunPhase.COMPLETED,
                messages = listOf(oldMessage, grownMessage)
            )
        )
        assertTrue(terminal.any { it.payload is AgentPublicEventPayload.ResponseCompleted })
        assertTrue(terminal.any { it.payload is AgentPublicEventPayload.Completed })

        val repeatedTerminal = adapter.eventsFor(
            baseline.copy(
                isRunning = false,
                phase = AgentRunPhase.COMPLETED,
                messages = listOf(oldMessage, grownMessage)
            )
        )
        assertFalse(repeatedTerminal.any { it.payload is AgentPublicEventPayload.ResponseDelta })
        assertFalse(repeatedTerminal.any { it.payload is AgentPublicEventPayload.ResponseCompleted })
    }

    @Test
    fun `device operation deadline is not reported as a network failure`() {
        val failure = agentFailureFrom(AgentOperationTimeoutException())

        assertEquals(AgentFailureCategory.VERIFICATION, failure.category)
        assertEquals(AgentFailureCode.OPERATION_TIMED_OUT, failure.code)
        assertEquals(AgentFailureSubsystem.VERIFICATION, failure.subsystem)
        assertEquals(AgentFailureStage.VERIFYING, failure.stage)
        assertTrue(failure.retryable)
        assertEquals(AgentFailureAction.RETRY, failure.suggestedAction)
        assertEquals("operation_timed_out", failure.technicalDetail)
    }

    @Test
    fun `device and observation failures are not mislabeled as provider network failures`() {
        val observation = agentFailureFrom("Device observation timed out while reading UI hierarchy")
        val execution = agentFailureFrom("ADB device action failed")
        val provider = agentFailureFrom(ModelHttpException(503, "Provider connection failed"))

        assertEquals(AgentFailureCategory.OBSERVATION, observation.category)
        assertEquals(AgentFailureCode.OBSERVATION_FAILED, observation.code)
        assertEquals(AgentFailureCategory.DEVICE_EXECUTION, execution.category)
        assertEquals(AgentFailureCode.DEVICE_EXECUTION_FAILED, execution.code)
        assertEquals(AgentFailureCategory.NETWORK, provider.category)
        assertEquals(AgentFailureCode.NETWORK_UNAVAILABLE, provider.code)
    }

    @Test
    fun `model call timeout is attributed to provider planning`() {
        val failure = agentFailureFrom(AgentModelCallTimeoutException())

        assertEquals(AgentFailureCategory.NETWORK, failure.category)
        assertEquals(AgentFailureCode.MODEL_CALL_TIMED_OUT, failure.code)
        assertEquals(AgentFailureSubsystem.PROVIDER, failure.subsystem)
        assertEquals(AgentFailureStage.PLANNING, failure.stage)
        assertTrue(failure.retryable)
    }

    @Test
    fun `transient Bridge failure is attributed to Artemis configuration instead of model protocol`() {
        val failure = agentFailureFrom(
            QadbBridgeException("QADB Bridge request failed with HTTP 503", statusCode = 503, retryable = true)
        )

        assertEquals(AgentFailureCategory.NETWORK, failure.category)
        assertEquals(AgentFailureCode.ENGINE_UNAVAILABLE, failure.code)
        assertEquals(AgentFailureSubsystem.ORCHESTRATOR, failure.subsystem)
        assertEquals(AgentFailureStage.CONFIGURATION, failure.stage)
        assertEquals(AgentFailureAction.RETRY, failure.suggestedAction)
    }

    private fun reduce(
        state: AgentPublicActivityState,
        runId: String,
        sequence: Long,
        payload: AgentPublicEventPayload
    ): AgentPublicActivityState = AgentPublicActivityReducer.reduce(
        state,
        AgentPublicEvent(
            runId = runId,
            sequence = sequence,
            occurredAtMs = sequence,
            payload = payload
        )
    )
}
