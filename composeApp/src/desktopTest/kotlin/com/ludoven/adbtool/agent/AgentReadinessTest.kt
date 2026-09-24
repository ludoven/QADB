package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals

class AgentReadinessTest {
    @Test
    fun `device guidance has priority over model configuration`() {
        assertEquals(
            AgentReadiness.DEVICE_REQUIRED,
            resolveAgentReadiness(
                deviceConnected = false,
                configurationChecked = false,
                modelConfigured = false,
                executionReady = false,
                externalEngineSelected = false
            )
        )
    }

    @Test
    fun `connected device waits for configuration check`() {
        assertEquals(
            AgentReadiness.CHECKING,
            resolveAgentReadiness(
                deviceConnected = true,
                configurationChecked = false,
                modelConfigured = false,
                executionReady = false,
                externalEngineSelected = false
            )
        )
    }

    @Test
    fun `model is required after configuration check`() {
        assertEquals(
            AgentReadiness.MODEL_REQUIRED,
            resolveAgentReadiness(
                deviceConnected = true,
                configurationChecked = true,
                modelConfigured = false,
                executionReady = false,
                externalEngineSelected = false
            )
        )
    }

    @Test
    fun `configured model requests capability test instead of configuration`() {
        assertEquals(
            AgentReadiness.MODEL_TEST_REQUIRED,
            resolveAgentReadiness(
                deviceConnected = true,
                configurationChecked = true,
                modelConfigured = true,
                executionReady = false,
                externalEngineSelected = false
            )
        )
    }

    @Test
    fun `configured external engine failure is not reported as missing model`() {
        assertEquals(
            AgentReadiness.ENGINE_UNAVAILABLE,
            resolveAgentReadiness(
                deviceConnected = true,
                configurationChecked = true,
                modelConfigured = true,
                executionReady = false,
                externalEngineSelected = true
            )
        )
    }

    @Test
    fun `ready requires both device and tested model`() {
        assertEquals(
            AgentReadiness.READY,
            resolveAgentReadiness(
                deviceConnected = true,
                configurationChecked = true,
                modelConfigured = true,
                executionReady = true,
                externalEngineSelected = false
            )
        )
    }
}
