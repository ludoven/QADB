package com.ludoven.adbtool.agent

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class AgentTaskOutcomeAndVerificationTest {

    @Test
    fun `public activity reducer marks ENGINE_FINISHED status on unverified completion`() {
        var state = AgentPublicActivityState()
        val runId = "run-engine-finish"
        val event1 = AgentPublicEvent(
            runId = runId,
            sequence = 1L,
            occurredAtMs = 1000L,
            payload = AgentPublicEventPayload.RunStarted
        )
        state = AgentPublicActivityReducer.reduce(state, event1)
        assertEquals(AgentPublicRunStatus.RUNNING, state.activeRun?.status)

        val finishEvent = AgentPublicEvent(
            runId = runId,
            sequence = 2L,
            occurredAtMs = 2000L,
            payload = AgentPublicEventPayload.Completed(
                outcome = AgentTaskOutcome.ENGINE_FINISHED,
                verification = AgentVerificationState(
                    verdict = AgentVerificationVerdict.UNVERIFIED,
                    level = AgentVerificationLevel.ENGINE_REPORTED
                )
            )
        )
        state = AgentPublicActivityReducer.reduce(state, finishEvent)
        assertEquals(AgentPublicRunStatus.ENGINE_FINISHED, state.activeRun?.status)
        assertEquals(AgentTaskOutcome.ENGINE_FINISHED, state.activeRun?.outcome)
        assertEquals(AgentVerificationLevel.ENGINE_REPORTED, state.activeRun?.verification?.level)
    }

    @Test
    fun `sqlite history store persists outcome and verification level`() {
        val tempDb = File.createTempFile("history_test", ".db")
        try {
            val store = SqliteAgentSessionHistoryStore(tempDb)
            val runId = "run-history-test"
            store.start(runId, "测试电量", "dev-1", 1000L)

            val terminalState = AgentTaskUiState(
                phase = AgentRunPhase.COMPLETED,
                outcome = AgentTaskOutcome.VERIFIED_SUCCESS,
                verification = AgentVerificationState(
                    verdict = AgentVerificationVerdict.VERIFIED,
                    level = AgentVerificationLevel.DETERMINISTIC
                )
            )
            store.finish(runId, terminalState)

            val record = store.find(runId)
            assertEquals(runId, record?.runId)
            assertEquals(AgentRunPhase.COMPLETED, record?.phase)
            assertEquals(AgentTaskOutcome.VERIFIED_SUCCESS, record?.outcome)
            assertEquals(AgentVerificationLevel.DETERMINISTIC, record?.verificationLevel)
        } finally {
            tempDb.delete()
        }
    }
}
