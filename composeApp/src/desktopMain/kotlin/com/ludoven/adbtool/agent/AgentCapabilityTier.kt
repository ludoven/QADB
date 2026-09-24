package com.ludoven.adbtool.agent

import java.security.MessageDigest
import java.util.prefs.Preferences

enum class AgentCapabilityTier {
    L0_TEXT,
    L1_STRUCTURED_READ,
    L2_SEMANTIC_AGENT,
    L3_VISUAL_AGENT
}

fun AgentCapabilities.capabilityTier(): AgentCapabilityTier = when {
    text && toolCalling && vision -> AgentCapabilityTier.L3_VISUAL_AGENT
    text && toolCalling -> AgentCapabilityTier.L2_SEMANTIC_AGENT
    text && structuredOutput -> AgentCapabilityTier.L1_STRUCTURED_READ
    else -> AgentCapabilityTier.L0_TEXT
}

data class AgentCapabilityAttestation(
    val providerFingerprint: String,
    val tier: AgentCapabilityTier,
    val verifiedAtMs: Long,
    val expiresAtMs: Long
) {
    fun isValidFor(provider: AgentProviderProfile, nowMs: Long = System.currentTimeMillis()): Boolean =
        providerFingerprint == provider.capabilityFingerprint() && nowMs in verifiedAtMs until expiresAtMs
}

/** Local-only compatibility evidence. It stores no endpoint credentials or user/device content. */
class AgentCapabilityAttestationStore(
    private val preferences: Preferences = Preferences.userNodeForPackage(AgentCapabilityAttestationStore::class.java),
    private val clockMs: () -> Long = System::currentTimeMillis
) {
    fun save(profile: AgentProviderProfile, tier: AgentCapabilityTier): AgentCapabilityAttestation {
        val verifiedAt = clockMs()
        val attestation = AgentCapabilityAttestation(
            providerFingerprint = profile.capabilityFingerprint(),
            tier = tier,
            verifiedAtMs = verifiedAt,
            // Provider edits and credential changes already invalidate this record.
            // Requiring a time-based re-test made a persisted model look unconfigured
            // even though none of its compatibility inputs had changed.
            expiresAtMs = Long.MAX_VALUE
        )
        preferences.put(attestationKey(profile.id), listOf(
            attestation.providerFingerprint,
            attestation.tier.name,
            attestation.verifiedAtMs,
            attestation.expiresAtMs
        ).joinToString("|"))
        preferences.flush()
        return attestation
    }

    fun load(profile: AgentProviderProfile): AgentCapabilityAttestation? {
        val parts = preferences.get(attestationKey(profile.id), "").split('|')
        if (parts.size != 4) return null
        val attestation = AgentCapabilityAttestation(
            providerFingerprint = parts[0],
            tier = runCatching { AgentCapabilityTier.valueOf(parts[1]) }.getOrNull() ?: return null,
            verifiedAtMs = parts[2].toLongOrNull() ?: return null,
            expiresAtMs = parts[3].toLongOrNull() ?: return null
        )
        return attestation.takeIf { it.isValidFor(profile, clockMs()) }
    }

    fun invalidate(providerId: String) {
        preferences.remove(attestationKey(providerId))
        preferences.flush()
    }

    private fun attestationKey(providerId: String): String =
        "agent.cap.${providerId.safePreferenceHash().take(PREFERENCE_HASH_CHARS)}"
}

fun AgentProviderProfile.capabilityFingerprint(): String = listOf(
    id,
    baseUrl.trimEnd('/'),
    defaultModel,
    protocol.name,
    capabilities.toString()
).joinToString("|").safePreferenceHash()

private fun String.safePreferenceHash(): String = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray(Charsets.UTF_8))
    .joinToString("") { "%02x".format(it) }

private const val PREFERENCE_HASH_CHARS = 48
