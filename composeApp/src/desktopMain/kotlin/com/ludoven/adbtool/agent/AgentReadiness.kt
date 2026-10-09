package com.ludoven.adbtool.agent

internal enum class AgentReadiness {
    CHECKING,
    DEVICE_REQUIRED,
    MODEL_REQUIRED,
    MODEL_TEST_REQUIRED,
    ARTEMIS_REQUIRED,
    BRIDGE_REQUIRED,
    ENGINE_UNAVAILABLE,
    READY
}

internal data class AgentComponentReadiness(
    val modelReady: Boolean,
    val deviceReady: Boolean,
    val artemisReady: Boolean = true,
    val bridgeReady: Boolean = true
) {
    val allReady: Boolean
        get() = modelReady && deviceReady && artemisReady && bridgeReady
}

internal fun resolveAgentReadiness(
    deviceConnected: Boolean,
    configurationChecked: Boolean,
    modelConfigured: Boolean,
    executionReady: Boolean,
    externalEngineSelected: Boolean,
    artemisDaemonReady: Boolean = true,
    bridgeReady: Boolean = true
): AgentReadiness = when {
    !deviceConnected -> AgentReadiness.DEVICE_REQUIRED
    !configurationChecked -> AgentReadiness.CHECKING
    !modelConfigured -> AgentReadiness.MODEL_REQUIRED
    externalEngineSelected && !artemisDaemonReady -> AgentReadiness.ARTEMIS_REQUIRED
    externalEngineSelected && !bridgeReady -> AgentReadiness.BRIDGE_REQUIRED
    !executionReady && externalEngineSelected -> AgentReadiness.ENGINE_UNAVAILABLE
    !executionReady -> AgentReadiness.MODEL_TEST_REQUIRED
    else -> AgentReadiness.READY
}
