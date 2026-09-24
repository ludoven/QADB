package com.ludoven.adbtool.agent

internal enum class AgentReadiness {
    CHECKING,
    DEVICE_REQUIRED,
    MODEL_REQUIRED,
    MODEL_TEST_REQUIRED,
    ENGINE_UNAVAILABLE,
    READY
}

internal fun resolveAgentReadiness(
    deviceConnected: Boolean,
    configurationChecked: Boolean,
    modelConfigured: Boolean,
    executionReady: Boolean,
    externalEngineSelected: Boolean
): AgentReadiness = when {
    !deviceConnected -> AgentReadiness.DEVICE_REQUIRED
    !configurationChecked -> AgentReadiness.CHECKING
    !modelConfigured -> AgentReadiness.MODEL_REQUIRED
    !executionReady && externalEngineSelected -> AgentReadiness.ENGINE_UNAVAILABLE
    !executionReady -> AgentReadiness.MODEL_TEST_REQUIRED
    else -> AgentReadiness.READY
}
