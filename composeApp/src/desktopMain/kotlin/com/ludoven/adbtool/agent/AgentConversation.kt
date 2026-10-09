package com.ludoven.adbtool.agent

import java.util.UUID

/**
 * A persistent conversation containing sequential execution runs.
 */
data class AgentConversation(
    val id: String = UUID.randomUUID().toString(),
    val rootGoal: String,
    val runIds: List<String> = emptyList(),
    val createdAtMs: Long = System.currentTimeMillis()
)

/**
 * Lightweight, text-only context projection across turns.
 * Strictly excludes past screenshots to prevent visual context explosion and stale observations.
 */
data class AgentConversationContext(
    val conversationId: String,
    val rootGoal: String,
    val userCorrections: List<String> = emptyList(),
    val confirmedFacts: List<String> = emptyList(),
    val currentSubGoal: String? = null,
    val prohibitedActions: List<String> = emptyList(),
    val pendingRisks: List<String> = emptyList()
) {
    fun toAugmentedGoal(currentInstruction: String): String = buildString {
        appendLine("会话目标: $rootGoal")
        if (userCorrections.isNotEmpty()) {
            appendLine("历史补充修正: ${userCorrections.joinToString("；")}")
        }
        if (confirmedFacts.isNotEmpty()) {
            appendLine("已确认设备事实: ${confirmedFacts.joinToString("；")}")
        }
        if (prohibitedActions.isNotEmpty()) {
            appendLine("禁止事项: ${prohibitedActions.joinToString("；")}")
        }
        appendLine("本轮指令: $currentInstruction")
    }.trim()
}
