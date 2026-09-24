package com.ludoven.adbtool.agent

import java.io.File
import java.sql.DriverManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/** A read-only replay of public run facts. Opening a record never resumes device actions. */
data class AgentSessionHistoryRecord(
    val runId: String,
    val parentRunId: String? = null,
    val title: String,
    val deviceId: String,
    val startedAtMs: Long,
    val finishedAtMs: Long?,
    val phase: AgentRunPhase,
    val stopOutcome: AgentStopOutcome,
    val needsUser: Boolean,
    val activities: List<AgentPublicActivityItem>
)

interface AgentSessionHistoryStore {
    fun start(runId: String, task: String, deviceId: String, startedAtMs: Long, parentRunId: String? = null)
    fun finish(runId: String, state: AgentTaskUiState)
    fun confirmStop(runId: String)
    /** Deletes only a settled leaf record; audit tables and device evidence are separate. */
    fun deleteFinished(runId: String): Boolean
    fun recent(): List<AgentSessionHistoryRecord>
    fun find(runId: String): AgentSessionHistoryRecord?
}

object NoopAgentSessionHistoryStore : AgentSessionHistoryStore {
    override fun start(runId: String, task: String, deviceId: String, startedAtMs: Long, parentRunId: String?) = Unit
    override fun finish(runId: String, state: AgentTaskUiState) = Unit
    override fun confirmStop(runId: String) = Unit
    override fun deleteFinished(runId: String): Boolean = false
    override fun recent(): List<AgentSessionHistoryRecord> = emptyList()
    override fun find(runId: String): AgentSessionHistoryRecord? = null
}

/** Version 2 adds a nullable parent run; version 1 rows remain readable. */
class SqliteAgentSessionHistoryStore(
    databaseFile: File = AgentDataPaths.memoryDatabase()
) : AgentSessionHistoryStore, AutoCloseable {
    private val connection = run {
        Class.forName("org.sqlite.JDBC")
        databaseFile.parentFile?.mkdirs()
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
    }

    init {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA busy_timeout = 2000")
            statement.execute(
                """CREATE TABLE IF NOT EXISTS agent_session_history (
                    run_id TEXT PRIMARY KEY,
                    format_version INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    device_id TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    finished_at INTEGER,
                    phase TEXT NOT NULL,
                    stop_outcome TEXT NOT NULL,
                    needs_user INTEGER NOT NULL,
                    public_activities TEXT NOT NULL
                )"""
            )
            val hasParent = statement.executeQuery("PRAGMA table_info(agent_session_history)").use { columns ->
                generateSequence { if (columns.next()) columns.getString("name") else null }
                    .any { it == "parent_run_id" }
            }
            if (!hasParent) statement.execute("ALTER TABLE agent_session_history ADD COLUMN parent_run_id TEXT")
            statement.execute(
                "CREATE INDEX IF NOT EXISTS agent_session_history_started_idx " +
                    "ON agent_session_history(started_at DESC)"
            )
        }
    }

    @Synchronized
    override fun start(runId: String, task: String, deviceId: String, startedAtMs: Long, parentRunId: String?) {
        val title = task.lineSequence().firstOrNull().orEmpty().trim().take(100)
            .ifBlank { "Agent task" }
        connection.prepareStatement(
            """INSERT OR IGNORE INTO agent_session_history
                (run_id, format_version, title, device_id, started_at, phase, stop_outcome,
                 needs_user, public_activities, parent_run_id) VALUES (?, 2, ?, ?, ?, ?, ?, 0, '[]', ?)"""
        ).use { statement ->
            statement.setString(1, runId)
            statement.setString(2, title)
            statement.setString(3, deviceId)
            statement.setLong(4, startedAtMs)
            statement.setString(5, AgentRunPhase.OBSERVING.name)
            statement.setString(6, AgentStopOutcome.NONE.name)
            statement.setString(7, parentRunId)
            statement.executeUpdate()
        }
    }

    @Synchronized
    override fun finish(runId: String, state: AgentTaskUiState) {
        val activities = state.publicActivity.runs[runId]?.activities.orEmpty().takeLast(30)
        val encoded = buildJsonArray {
            activities.forEach { activity ->
                add(buildJsonObject {
                    put("sequence", activity.sequence)
                    put("at", activity.occurredAtMs)
                    put("stage", activity.stage.name)
                    activity.tool?.let { put("tool", it.kind.name) }
                    activity.result?.let { put("result", it.name) }
                })
            }
        }.toString()
        connection.prepareStatement(
            """UPDATE agent_session_history SET finished_at = ?, phase = ?, stop_outcome = ?,
                needs_user = ?, public_activities = ? WHERE run_id = ?"""
        ).use { statement ->
            statement.setLong(1, System.currentTimeMillis())
            statement.setString(2, state.phase.name)
            statement.setString(3, state.stopOutcome.name)
            statement.setInt(4, if (state.needsUser) 1 else 0)
            statement.setString(5, encoded)
            statement.setString(6, runId)
            statement.executeUpdate()
        }
    }

    @Synchronized
    override fun confirmStop(runId: String) {
        connection.prepareStatement(
            "UPDATE agent_session_history SET stop_outcome = ? WHERE run_id = ? AND stop_outcome = ?"
        ).use { statement ->
            statement.setString(1, AgentStopOutcome.CONFIRMED.name)
            statement.setString(2, runId)
            statement.setString(3, AgentStopOutcome.UNCONFIRMED.name)
            statement.executeUpdate()
        }
    }

    @Synchronized
    override fun deleteFinished(runId: String): Boolean = connection.prepareStatement(
        """DELETE FROM agent_session_history
            WHERE run_id = ? AND finished_at IS NOT NULL
            AND phase IN ('COMPLETED', 'FAILED', 'CANCELLED')
            AND stop_outcome IN ('NONE', 'CONFIRMED')
            AND NOT EXISTS (
                SELECT 1 FROM agent_session_history AS child WHERE child.parent_run_id = ?
            )"""
    ).use { statement ->
        statement.setString(1, runId)
        statement.setString(2, runId)
        statement.executeUpdate() == 1
    }

    @Synchronized
    override fun recent(): List<AgentSessionHistoryRecord> = connection.prepareStatement(
        """SELECT run_id, format_version, title, device_id, started_at, finished_at, phase,
            stop_outcome, needs_user, public_activities, parent_run_id FROM agent_session_history
            ORDER BY started_at DESC LIMIT 80"""
    ).use { statement ->
        statement.executeQuery().use { rows ->
            buildList {
                while (rows.next()) {
                    decodeRecord(rows)?.let(::add)
                }
            }
        }
    }

    @Synchronized
    override fun find(runId: String): AgentSessionHistoryRecord? = connection.prepareStatement(
        """SELECT run_id, format_version, title, device_id, started_at, finished_at, phase,
            stop_outcome, needs_user, public_activities, parent_run_id FROM agent_session_history
            WHERE run_id = ?"""
    ).use { statement ->
        statement.setString(1, runId)
        statement.executeQuery().use { rows -> if (rows.next()) decodeRecord(rows) else null }
    }

    private fun decodeRecord(rows: java.sql.ResultSet): AgentSessionHistoryRecord? {
        val version = rows.getInt("format_version")
        if (version !in 1..2) return null
        return AgentSessionHistoryRecord(
            runId = rows.getString("run_id"),
            parentRunId = if (version >= 2) rows.getString("parent_run_id") else null,
            title = rows.getString("title"),
            deviceId = rows.getString("device_id"),
            startedAtMs = rows.getLong("started_at"),
            finishedAtMs = rows.getLong("finished_at").takeUnless { rows.wasNull() },
            phase = enumValueOrDefault(rows.getString("phase"), AgentRunPhase.IDLE),
            stopOutcome = enumValueOrDefault(rows.getString("stop_outcome"), AgentStopOutcome.NONE),
            needsUser = rows.getInt("needs_user") != 0,
            activities = decodeActivities(rows.getString("public_activities"))
        )
    }

    override fun close() = connection.close()

    private fun decodeActivities(value: String): List<AgentPublicActivityItem> = runCatching {
        Json.parseToJsonElement(value).jsonArray.mapNotNull { element ->
            val item = element.jsonObject
            val stage = enumValueOrNull<AgentPublicStage>(item["stage"]?.jsonPrimitive?.contentOrNull)
                ?: return@mapNotNull null
            AgentPublicActivityItem(
                sequence = item["sequence"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null,
                occurredAtMs = item["at"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null,
                stage = stage,
                tool = enumValueOrNull<AgentPublicToolKind>(item["tool"]?.jsonPrimitive?.contentOrNull)
                    ?.let { AgentPublicToolSummary(it) },
                result = enumValueOrNull<AgentPublicToolResult>(item["result"]?.jsonPrimitive?.contentOrNull)
            )
        }
    }.getOrDefault(emptyList())

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
        enumValueOrNull<T>(value) ?: default

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String?): T? =
        enumValues<T>().firstOrNull { it.name == value }
}

object AgentSessionHistoryRuntime {
    val store: AgentSessionHistoryStore by lazy { SqliteAgentSessionHistoryStore() }
}
