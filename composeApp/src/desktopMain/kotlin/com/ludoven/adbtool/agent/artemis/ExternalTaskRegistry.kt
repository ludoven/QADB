package com.ludoven.adbtool.agent.artemis

import java.util.prefs.Preferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Persisted, non-secret ownership record for QADB-created external sessions. */
data class ExternalTaskRecord(
    val runId: String,
    val sessionId: String,
    val deviceId: String,
    val profile: String,
    val status: String,
    val updatedAtMs: Long
)

interface ExternalTaskRegistry {
    fun upsert(record: ExternalTaskRecord)
    fun find(runId: String): ExternalTaskRecord?
    fun recent(): List<ExternalTaskRecord>
}

/**
 * Restarts never submit a task again. They reconcile only QADB-owned records
 * against the exact upstream session and preserve uncertain submissions.
 */
class ExternalTaskReconciler(
    private val registry: ExternalTaskRegistry,
    private val client: ArtemisHttpClient,
    private val bridge: ArtemisBridge? = null
) {
    suspend fun reconcile(): List<ExternalTaskRecord> = registry.recent().map { record ->
        if (record.status in TERMINAL_STATUSES) return@map record
        val next = when (val lookup = client.getSession(record.sessionId)) {
            ArtemisSessionLookup.NotPersistedYet -> record.copy(
                status = if (record.status == "stop_unconfirmed") "stop_unconfirmed" else "submission_unknown"
            )
            is ArtemisSessionLookup.Found -> record.copy(
                status = if (record.status == "stop_unconfirmed") {
                    val terminal = lookup.handle.status in setOf(
                        ArtemisTaskStatus.COMPLETED, ArtemisTaskStatus.FAILED, ArtemisTaskStatus.REJECTED
                    )
                    if (terminal && runCatching { bridge?.quiescent(record.runId) }.getOrNull() == true) {
                        "stopped"
                    } else {
                        "stop_unconfirmed"
                    }
                } else when (lookup.handle.status) {
                    ArtemisTaskStatus.COMPLETED -> "engine_reported_success"
                    ArtemisTaskStatus.FAILED -> "failed"
                    ArtemisTaskStatus.REJECTED -> "rejected"
                    ArtemisTaskStatus.QUEUED -> "queued"
                    ArtemisTaskStatus.PAUSED -> "paused"
                    ArtemisTaskStatus.RUNNING -> "running"
                    ArtemisTaskStatus.UNKNOWN -> "outcome_unknown"
                }
            )
        }
        registry.upsert(next)
        next
    }

    private companion object {
        val TERMINAL_STATUSES = setOf("completed", "engine_reported_success", "failed", "rejected", "stopped", "orphaned_released")
    }
}

class PreferencesExternalTaskRegistry(
    private val preferences: Preferences = Preferences.userNodeForPackage(PreferencesExternalTaskRegistry::class.java),
    private val clock: () -> Long = System::currentTimeMillis
) : ExternalTaskRegistry {
    private val json = Json { ignoreUnknownKeys = true }

    override fun upsert(record: ExternalTaskRecord) {
        val current = read().filterNot { it.runId == record.runId }
        val encoded = buildJsonArray {
            (current + record.copy(updatedAtMs = clock())).takeLast(MAX_RECORDS).forEach { item ->
                add(buildJsonObject {
                    put("run_id", item.runId)
                    put("session_id", item.sessionId)
                    put("device_id", item.deviceId)
                    put("profile", item.profile)
                    put("status", item.status)
                    put("updated_at_ms", item.updatedAtMs)
                })
            }
        }.toString()
        preferences.put(KEY_RECORDS, encoded)
        preferences.flush()
    }

    override fun find(runId: String): ExternalTaskRecord? = read().lastOrNull { it.runId == runId }

    override fun recent(): List<ExternalTaskRecord> = read()

    private fun read(): List<ExternalTaskRecord> = runCatching {
        json.parseToJsonElement(preferences.get(KEY_RECORDS, "[]")).jsonArray.mapNotNull { value ->
            val item = value.jsonObject
            val runId = item["run_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val sessionId = item["session_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val deviceId = item["device_id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            ExternalTaskRecord(
                runId = runId,
                sessionId = sessionId,
                deviceId = deviceId,
                profile = item["profile"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                status = item["status"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                updatedAtMs = item["updated_at_ms"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
            )
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val KEY_RECORDS = "agent.artemis.external_tasks"
        const val MAX_RECORDS = 100
    }
}

object ExternalTaskRuntime {
    val registry: ExternalTaskRegistry by lazy { PreferencesExternalTaskRegistry() }
}
