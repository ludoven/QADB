package com.ludoven.adbtool.agent.artemis

import com.ludoven.adbtool.agent.AgentBudgetStatus
import com.ludoven.adbtool.agent.AgentPublicActivityReducer
import com.ludoven.adbtool.agent.AgentPublicEvent
import com.ludoven.adbtool.agent.AgentPublicEventPayload
import com.ludoven.adbtool.agent.AgentPublicMetrics
import com.ludoven.adbtool.agent.AgentStepStatus
import com.ludoven.adbtool.agent.AgentTaskUiState
import com.ludoven.adbtool.agent.AgentUsage

/**
 * Converts Artemis usage snapshots into QADB AgentUsage, PublicMetrics and BudgetStatus.
 */
object ArtemisUsageAdapter {
    fun toAgentUsage(snapshot: ArtemisUsageSnapshot): AgentUsage = AgentUsage(
        promptTokens = snapshot.promptTokens?.toInt() ?: 0,
        completionTokens = snapshot.completionTokens?.toInt() ?: 0,
        cachedTokens = 0,
        totalTokens = snapshot.totalTokens?.toInt() ?: (
            (snapshot.promptTokens?.toInt() ?: 0) + (snapshot.completionTokens?.toInt() ?: 0)
        )
    )

    fun toPublicMetrics(snapshot: ArtemisUsageSnapshot, deviceActions: Int = 0): AgentPublicMetrics = AgentPublicMetrics(
        promptTokens = snapshot.promptTokens?.toInt() ?: 0,
        completionTokens = snapshot.completionTokens?.toInt() ?: 0,
        totalTokens = snapshot.totalTokens?.toInt() ?: 0,
        modelCalls = snapshot.llmCalls ?: 0,
        deviceActions = deviceActions,
        deviceActionLimit = 0
    )

    fun updateStateWithUsage(
        state: AgentTaskUiState,
        snapshot: ArtemisUsageSnapshot,
        runId: String,
        sequence: Long = System.nanoTime(),
        occurredAtMs: Long = System.currentTimeMillis()
    ): AgentTaskUiState {
        val usage = toAgentUsage(snapshot)
        val deviceActions = state.steps.count { it.status == AgentStepStatus.COMPLETED }
        val metrics = toPublicMetrics(snapshot, deviceActions = deviceActions)
        val budgetStatus = state.budgetStatus.copy(
            usage = usage,
            modelCalls = snapshot.llmCalls ?: state.budgetStatus.modelCalls,
            deviceActions = deviceActions
        )
        val publicEvent = AgentPublicEvent(
            runId = runId,
            sequence = sequence,
            occurredAtMs = occurredAtMs,
            payload = AgentPublicEventPayload.MetricsUpdated(metrics)
        )
        val updatedPublicActivity = AgentPublicActivityReducer.reduce(state.publicActivity, publicEvent)
        return state.copy(
            usage = usage,
            budgetStatus = budgetStatus,
            publicActivity = updatedPublicActivity
        )
    }
}
