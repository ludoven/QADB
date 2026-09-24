package com.ludoven.adbtool.agent

import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AgentCapabilityAttestationStoreTest {
    @Test
    fun verifiedCapabilityPersistsWhileProviderFingerprintIsUnchanged() {
        val preferences = Preferences.userRoot().node("qadb-tests.capability-persistence.${UUID.randomUUID()}")
        var nowMs = 1_000L
        val profile = AgentProviderProfile(
            id = "provider",
            name = "Provider",
            baseUrl = "https://example.test/v1",
            defaultModel = "model",
            capabilities = AgentCapabilities(vision = true)
        )
        try {
            AgentCapabilityAttestationStore(preferences) { nowMs }
                .save(profile, AgentCapabilityTier.L3_VISUAL_AGENT)

            nowMs += 365L * 24L * 60L * 60L * 1_000L
            val reloaded = AgentCapabilityAttestationStore(preferences) { nowMs }.load(profile)

            assertNotNull(reloaded)
            assertEquals(AgentCapabilityTier.L3_VISUAL_AGENT, reloaded.tier)
        } finally {
            preferences.removeNode()
        }
    }
}
