package com.ludoven.adbtool.agent

import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentConversationContextTest {

    @Test
    fun `context projection formats augmented goal without image payload`() {
        val convId = UUID.randomUUID().toString()
        val projection = AgentConversationContext(
            conversationId = convId,
            rootGoal = "打开微信查找联系人张三",
            userCorrections = listOf("选列表中第二个"),
            confirmedFacts = listOf("当前处于联系人搜索结果页"),
            prohibitedActions = listOf("不可点击删除")
        )

        val augmented = projection.toAugmentedGoal("点击发送消息")
        assertTrue(augmented.contains("打开微信查找联系人张三"))
        assertTrue(augmented.contains("选列表中第二个"))
        assertTrue(augmented.contains("当前处于联系人搜索结果页"))
        assertTrue(augmented.contains("不可点击删除"))
        assertTrue(augmented.contains("点击发送消息"))
    }

    @Test
    fun `history store persists and retrieves conversationId across multiple runs`() {
        val tempDb = File.createTempFile("conv_history_test", ".db")
        try {
            val store = SqliteAgentSessionHistoryStore(tempDb)
            val conversationId = UUID.randomUUID().toString()
            val run1 = UUID.randomUUID().toString()
            val run2 = UUID.randomUUID().toString()

            // Run 1: Root run
            store.start(
                runId = run1,
                task = "打开微信找张三",
                deviceId = "dev-pixel",
                startedAtMs = 1000L,
                parentRunId = null,
                conversationId = conversationId
            )
            store.finish(
                run1,
                AgentTaskUiState(
                    phase = AgentRunPhase.COMPLETED,
                    outcome = AgentTaskOutcome.ENGINE_FINISHED,
                    conversationId = conversationId
                )
            )

            // Run 2: Continued segment
            store.start(
                runId = run2,
                task = "选第二个",
                deviceId = "dev-pixel",
                startedAtMs = 2000L,
                parentRunId = run1,
                conversationId = conversationId
            )
            store.finish(
                run2,
                AgentTaskUiState(
                    phase = AgentRunPhase.COMPLETED,
                    outcome = AgentTaskOutcome.VERIFIED_SUCCESS,
                    conversationId = conversationId
                )
            )

            val record1 = store.find(run1)
            val record2 = store.find(run2)

            assertNotEquals(record1?.runId, record2?.runId)
            assertEquals(conversationId, record1?.conversationId)
            assertEquals(conversationId, record2?.conversationId)
            assertNull(record1?.parentRunId)
            assertEquals(run1, record2?.parentRunId)
        } finally {
            tempDb.delete()
        }
    }
}
