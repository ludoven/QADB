package com.ludoven.adbtool.agent.artemis

import java.util.UUID

/** The only upstream execution profiles QADB's experimental client can submit. */
enum class ArtemisProfile(val wireValue: String) {
    FLASH("flash"),
    PRO("pro")
}

/**
 * Immutable input for one upstream run.  The caller owns [sessionId] so that a
 * retry can reuse it; this is the upstream idempotency boundary.
 */
data class ArtemisRunRequest(
    val goal: String,
    val profile: ArtemisProfile,
    val deviceSerial: String,
    val sessionId: String,
    val conversationId: String? = null,
    val verificationLevel: String? = null,
    val explorerMode: String? = null
) {
    init {
        require(goal.isNotBlank()) { "Artemis goal is required" }
        require(deviceSerial.isNotBlank()) { "Artemis device serial is required" }
        require(runCatching { UUID.fromString(sessionId) }.isSuccess) { "Artemis session id must be a UUID" }
        require(conversationId.isNullOrBlank() || runCatching { UUID.fromString(conversationId) }.isSuccess) {
            "Artemis conversation id must be a UUID when supplied"
        }
        require(profile == ArtemisProfile.PRO || (verificationLevel == null && explorerMode == null)) {
            "Artemis Pro tuning is not supported by the Flash profile"
        }
    }
}

enum class ArtemisTaskStatus {
    QUEUED,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    REJECTED,
    UNKNOWN;

    val isTerminal: Boolean
        get() = this == COMPLETED || this == FAILED || this == REJECTED

    companion object {
        fun fromWire(value: String?): ArtemisTaskStatus = when (value?.lowercase()) {
            "queued", "pending" -> QUEUED
            "running", "active" -> RUNNING
            "paused" -> PAUSED
            "completed", "complete", "success", "succeeded" -> COMPLETED
            "failed", "error", "cancelled", "canceled", "stopped" -> FAILED
            "rejected" -> REJECTED
            else -> UNKNOWN
        }
    }
}

data class ArtemisTaskHandle(
    val sessionId: String,
    val status: ArtemisTaskStatus,
    val goal: String? = null,
    val deviceSerial: String? = null
)

sealed interface ArtemisSubmitResult {
    data class Accepted(
        val handle: ArtemisTaskHandle,
        val enqueuedCount: Int?
    ) : ArtemisSubmitResult

    data class Rejected(val reason: String) : ArtemisSubmitResult
}

sealed interface ArtemisSessionLookup {
    /** A 404 immediately after admission is not evidence that the task failed. */
    data object NotPersistedYet : ArtemisSessionLookup

    data class Found(
        val handle: ArtemisTaskHandle,
        val summary: String? = null,
        val rawStatus: String? = null
    ) : ArtemisSessionLookup
}

enum class ArtemisStopStatus {
    STOPPED,
    NO_RUNNING_TASK
}

data class ArtemisStatusSnapshot(
    val status: ArtemisTaskStatus,
    val sessionId: String?,
    val activeTaskIds: Set<String>
)

data class ArtemisReadinessSnapshot(
    val ready: Boolean,
    val blockers: List<String> = emptyList(),
    /** Blockers unrelated to Artemis' global device probe. QADB binds the exact device at run admission. */
    val serviceBlockers: List<String> = blockers
)

/** Null means upstream omitted the field; it is never coerced to zero. */
data class ArtemisUsageSnapshot(
    val llmCalls: Int?,
    val promptTokens: Long?,
    val completionTokens: Long?,
    val totalTokens: Long?
)

data class ArtemisSseEvent(
    val type: String,
    val data: String,
    val sessionId: String?
)

class ArtemisProtocolException(message: String) : Exception(message)

class ArtemisHttpException(
    val statusCode: Int,
    message: String
) : Exception(message)
