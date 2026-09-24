package com.ludoven.adbtool.agent.artemis

/**
 * The pinned upstream daemon has no mandatory capabilities endpoint.  This
 * client intentionally relies only on the endpoints in [ArtemisUpstreamLock].
 */
object ArtemisUpstreamLock {
    const val REPOSITORY = "https://github.com/google/artemis"
    const val COMMIT = "371aa6df56880643da57b30da936e9812fb0ec66"
    const val PROTOCOL_VERSION = "artemis-http-v0.1"

    val requiredEndpoints = setOf(
        "POST /api/run",
        "GET /api/sessions/{session_id}",
        "GET /api/status",
        "POST /api/stop",
        "GET /api/devices",
        "GET /api/system/readiness"
    )
}
