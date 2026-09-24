package com.ludoven.adbtool.agent.artemis

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Converts only a session-scoped, non-sensitive event into display metadata. */
data class ArtemisMappedEvent(val status: ArtemisTaskStatus?, val detail: String?)

object ArtemisEventMapper {
    fun map(event: ArtemisSseEvent, expectedSessionId: String): ArtemisMappedEvent? {
        if (event.sessionId != expectedSessionId) return null
        val payload = runCatching { Json.parseToJsonElement(event.data).jsonObject }.getOrNull() ?: return null
        return ArtemisMappedEvent(
            status = payload["status"]?.jsonPrimitive?.contentOrNull?.let(ArtemisTaskStatus::fromWire),
            detail = payload["message"]?.jsonPrimitive?.contentOrNull
                ?: payload["stage"]?.jsonPrimitive?.contentOrNull
        )
    }
}
