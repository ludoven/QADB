package com.ludoven.adbtool.agent

import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentSessionHistoryTest {
    @Test
    fun `restarts show public history without resuming or storing raw task text`() {
        val database = Files.createTempFile("agent-session-history", ".db").toFile()
        try {
            SqliteAgentSessionHistoryStore(database).use { store ->
                store.start("run-1", "打开设置\n输入秘密正文", "emulator-5554", 1234)
                val started = AgentPublicActivityReducer.reduce(
                    AgentPublicActivityState(),
                    AgentPublicEvent("run-1", 1, 1234, AgentPublicEventPayload.RunStarted)
                )
                val activity = AgentPublicActivityReducer.reduce(
                    started,
                    AgentPublicEvent("run-1", 2, 1300,
                        AgentPublicEventPayload.ToolFinished(
                            AgentPublicToolSummary(AgentPublicToolKind.OPEN_APP),
                            AgentPublicToolResult.SUCCEEDED
                        ))
                )
                store.finish("run-1", AgentTaskUiState(
                    phase = AgentRunPhase.COMPLETED,
                    publicActivity = activity,
                    latestScreenshot = byteArrayOf(1, 2, 3)
                ))
            }
            SqliteAgentSessionHistoryStore(database).use { reopened ->
                val record = reopened.recent().single()
                assertEquals("打开设置", record.title)
                assertEquals("emulator-5554", record.deviceId)
                assertEquals(AgentRunPhase.COMPLETED, record.phase)
                assertEquals(AgentPublicToolKind.OPEN_APP, record.activities.last().tool?.kind)
                assertTrue(record.finishedAtMs != null)
            }
            val databaseBytes = database.readBytes().toString(Charsets.ISO_8859_1)
            assertFalse(databaseBytes.contains("输入秘密正文"))
        } finally {
            database.delete()
        }
    }

    @Test
    fun `unfinished history remains read only after restart`() {
        val database = Files.createTempFile("agent-session-unfinished", ".db").toFile()
        try {
            SqliteAgentSessionHistoryStore(database).use {
                it.start("run-2", "查看设备", "serial", 5678)
            }
            SqliteAgentSessionHistoryStore(database).use {
                val record = it.recent().single()
                assertNull(record.finishedAtMs)
                assertEquals(AgentRunPhase.OBSERVING, record.phase)
            }
        } finally {
            database.delete()
        }
    }

    @Test
    fun `confirmed worker stop updates only the bound history outcome`() {
        val database = Files.createTempFile("agent-session-stop", ".db").toFile()
        try {
            SqliteAgentSessionHistoryStore(database).use { store ->
                store.start("run-a", "读取屏幕", "serial", 1)
                store.start("run-b", "读取状态", "serial", 2)
                store.finish("run-a", AgentTaskUiState(
                    phase = AgentRunPhase.CANCELLED,
                    stopOutcome = AgentStopOutcome.UNCONFIRMED
                ))
                store.finish("run-b", AgentTaskUiState(
                    phase = AgentRunPhase.CANCELLED,
                    stopOutcome = AgentStopOutcome.UNCONFIRMED
                ))
                store.confirmStop("run-a")
            }
            SqliteAgentSessionHistoryStore(database).use { reopened ->
                val records = reopened.recent().associateBy { it.runId }
                assertEquals(AgentStopOutcome.CONFIRMED, records.getValue("run-a").stopOutcome)
                assertEquals(AgentStopOutcome.UNCONFIRMED, records.getValue("run-b").stopOutcome)
            }
        } finally {
            database.delete()
        }
    }

    @Test
    fun `version one history remains readable and new segments retain only a parent reference`() {
        val database = Files.createTempFile("agent-session-migration", ".db").toFile()
        try {
            DriverManager.getConnection("jdbc:sqlite:${database.absolutePath}").use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("""CREATE TABLE agent_session_history (
                        run_id TEXT PRIMARY KEY, format_version INTEGER NOT NULL, title TEXT NOT NULL,
                        device_id TEXT NOT NULL, started_at INTEGER NOT NULL, finished_at INTEGER,
                        phase TEXT NOT NULL, stop_outcome TEXT NOT NULL, needs_user INTEGER NOT NULL,
                        public_activities TEXT NOT NULL)""")
                    statement.execute("""INSERT INTO agent_session_history VALUES
                        ('old-run', 1, 'Earlier task', 'serial', 100, 200, 'COMPLETED', 'NONE', 0, '[]')""")
                }
            }
            SqliteAgentSessionHistoryStore(database).use { store ->
                assertNull(store.find("old-run")?.parentRunId)
                store.start("new-run", "Check prior result, then navigate", "serial", 300, "old-run")
                assertEquals("old-run", store.find("new-run")?.parentRunId)
                assertEquals(2, store.recent().size)
            }
            SqliteAgentSessionHistoryStore(database).use { reopened ->
                assertEquals("old-run", reopened.find("new-run")?.parentRunId)
            }
        } finally {
            database.delete()
        }
    }

    @Test
    fun `history deletion keeps unsettled and linked records`() {
        val database = Files.createTempFile("agent-session-delete", ".db").toFile()
        try {
            SqliteAgentSessionHistoryStore(database).use { store ->
                store.start("active", "Active", "serial", 1)
                store.start("unconfirmed", "Unconfirmed", "serial", 2)
                store.finish("unconfirmed", AgentTaskUiState(
                    phase = AgentRunPhase.CANCELLED,
                    stopOutcome = AgentStopOutcome.UNCONFIRMED
                ))
                store.start("parent", "Parent", "serial", 3)
                store.finish("parent", AgentTaskUiState(phase = AgentRunPhase.COMPLETED))
                store.start("child", "Child", "serial", 4, "parent")
                store.finish("child", AgentTaskUiState(phase = AgentRunPhase.COMPLETED))

                assertFalse(store.deleteFinished("active"))
                assertFalse(store.deleteFinished("unconfirmed"))
                assertFalse(store.deleteFinished("parent"))
                assertTrue(store.deleteFinished("child"))
                assertNull(store.find("child"))
                assertTrue(store.deleteFinished("parent"))
                assertNull(store.find("parent"))
                assertEquals(setOf("active", "unconfirmed"), store.recent().map { it.runId }.toSet())
            }
        } finally {
            database.delete()
        }
    }
}
