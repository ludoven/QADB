package com.ludoven.adbtool.agent.artemis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArtemisProviderProjectionTest {

    @Test
    fun `inferProviderType recognizes gemini, claude and openrouter endpoints`() {
        assertEquals(
            ProviderType.GEMINI,
            ArtemisProviderProjection.inferProviderType("https://generativelanguage.googleapis.com/v1beta", "gemini-2.5-flash")
        )
        assertEquals(
            ProviderType.ANTHROPIC,
            ArtemisProviderProjection.inferProviderType("https://api.anthropic.com/v1", "claude-3-7-sonnet")
        )
        assertEquals(
            ProviderType.OPEN_ROUTER,
            ArtemisProviderProjection.inferProviderType("https://openrouter.ai/api/v1", "anthropic/claude-3.5-sonnet")
        )
        assertEquals(
            ProviderType.OPENAI_COMPATIBLE,
            ArtemisProviderProjection.inferProviderType("https://api.openai.com/v1", "gpt-4o")
        )
    }

    @Test
    fun `project generates appropriate environment variables for gemini`() {
        val profile = UnifiedModelProfile(
            provider = ProviderType.GEMINI,
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            apiKey = "test-gemini-key",
            model = "gemini-2.5-flash"
        )
        val env = ArtemisProviderProjection.project(profile)
        assertEquals("test-gemini-key", env["GEMINI_API_KEY"])
        assertEquals("test-gemini-key", env["GOOGLE_API_KEY"])
        assertEquals("gemini-2.5-flash", env["ARTEMIS_MODEL"])
    }

    @Test
    fun `project generates appropriate environment variables for openrouter`() {
        val profile = UnifiedModelProfile(
            provider = ProviderType.OPEN_ROUTER,
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "test-openrouter-key",
            model = "meta-llama/llama-3.1-70b-instruct"
        )
        val env = ArtemisProviderProjection.project(profile)
        assertEquals("test-openrouter-key", env["OPEN_ROUTER_API_KEY"])
        assertEquals("test-openrouter-key", env["OPENAI_API_KEY"])
        assertEquals("https://openrouter.ai/api/v1", env["OPENAI_BASE_URL"])
        assertEquals("meta-llama/llama-3.1-70b-instruct", env["ARTEMIS_MODEL"])
    }

    @Test
    fun `blank api key produces empty environment map`() {
        val profile = UnifiedModelProfile(
            provider = ProviderType.OPENAI_COMPATIBLE,
            baseUrl = "https://api.openai.com/v1",
            apiKey = "",
            model = "gpt-4o"
        )
        val env = ArtemisProviderProjection.project(profile)
        assertTrue(env.isEmpty())
    }
}
