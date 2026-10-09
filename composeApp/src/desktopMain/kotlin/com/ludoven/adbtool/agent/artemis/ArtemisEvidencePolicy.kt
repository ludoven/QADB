package com.ludoven.adbtool.agent.artemis

import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Clock
import java.time.Duration

/** Non-secret, immutable model identifiers for one run; credentials stay in SecretStore. */
data class ArtemisModelBundle(
    val planner: String,
    val operator: String,
    val explorer: String? = null,
    val checker: String? = null,
    val summary: String? = null,
    val fallback: String? = null
) {
    val fingerprint: String = MessageDigest.getInstance("SHA-256")
        .digest(listOf(planner, operator, explorer, checker, summary, fallback).joinToString("\u0000").toByteArray())
        .joinToString("") { "%02x".format(it) }
}

enum class ArtemisEvidenceSource { NONE, SYSTEM_PROBE, ARTEMIS_CHECKER, VISUAL_REVIEW, HUMAN_CONFIRMED }
enum class ArtemisVerificationVerdict { UNVERIFIED, VERIFIED, FAILED, UNKNOWN }

data class ArtemisEvidencePolicy(
    val rawTraceRetentionHours: Int = 24,
    val screenRecordingEnabled: Boolean = false
) {
    init {
        require(rawTraceRetentionHours in 1..720)
    }
}

/** Deletes only expired regular files under the caller-owned Artemis trace root. */
class ArtemisTraceRetention(
    private val policy: ArtemisEvidencePolicy,
    private val clock: Clock = Clock.systemUTC()
) {
    fun purgeExpired(traceRoot: Path): List<Path> {
        require(Files.isDirectory(traceRoot)) { "Artemis trace root must be an existing directory" }
        val cutoff = clock.instant().minus(Duration.ofHours(policy.rawTraceRetentionHours.toLong()))
        Files.walk(traceRoot).use { paths ->
            return paths
                .filter { path -> Files.isRegularFile(path) }
                .filter { path -> Files.getLastModifiedTime(path).toInstant().isBefore(cutoff) }
                .sorted { left, right -> right.nameCount.compareTo(left.nameCount) }
                .map { path -> path.also(Files::delete) }
                .toList()
        }
    }
}

fun ArtemisEvidenceSource.toAgentEvidenceSource(): com.ludoven.adbtool.agent.AgentEvidenceSource = when (this) {
    ArtemisEvidenceSource.NONE -> com.ludoven.adbtool.agent.AgentEvidenceSource.NONE
    ArtemisEvidenceSource.SYSTEM_PROBE -> com.ludoven.adbtool.agent.AgentEvidenceSource.SYSTEM_PROBE
    ArtemisEvidenceSource.ARTEMIS_CHECKER -> com.ludoven.adbtool.agent.AgentEvidenceSource.ARTEMIS_CHECKER
    ArtemisEvidenceSource.VISUAL_REVIEW -> com.ludoven.adbtool.agent.AgentEvidenceSource.VISUAL_REVIEW
    ArtemisEvidenceSource.HUMAN_CONFIRMED -> com.ludoven.adbtool.agent.AgentEvidenceSource.HUMAN_CONFIRMED
}

fun ArtemisVerificationVerdict.toAgentVerificationVerdict(): com.ludoven.adbtool.agent.AgentVerificationVerdict = when (this) {
    ArtemisVerificationVerdict.UNVERIFIED -> com.ludoven.adbtool.agent.AgentVerificationVerdict.UNVERIFIED
    ArtemisVerificationVerdict.VERIFIED -> com.ludoven.adbtool.agent.AgentVerificationVerdict.VERIFIED
    ArtemisVerificationVerdict.FAILED -> com.ludoven.adbtool.agent.AgentVerificationVerdict.FAILED
    ArtemisVerificationVerdict.UNKNOWN -> com.ludoven.adbtool.agent.AgentVerificationVerdict.UNKNOWN
}
