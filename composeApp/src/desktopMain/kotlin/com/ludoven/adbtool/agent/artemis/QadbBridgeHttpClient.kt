package com.ludoven.adbtool.agent.artemis

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

data class ArtemisApprovalMetadata(
    val approvalId: String,
    val actionKind: String,
    val actionTarget: String?,
    val actionDigest: String,
    val deviceId: String,
    val taskVersion: Int,
    val expiresInMs: Long,
    val pending: Boolean
)

interface ArtemisBridge {
    suspend fun handshake()
    suspend fun register(runId: String, deviceId: String)
    suspend fun heartbeat(runId: String)
    /** True only when the Bridge confirms all device actuators are quiescent. */
    suspend fun stop(runId: String): Boolean
    suspend fun quiescent(runId: String): Boolean = false
    suspend fun pending(runId: String): List<String>
    suspend fun approval(runId: String, approvalId: String): ArtemisApprovalMetadata? = null
    suspend fun decide(runId: String, approvalId: String, allowed: Boolean)
}

class QadbBridgeException(
    message: String,
    val statusCode: Int? = null,
    val retryable: Boolean = false,
    cause: Throwable? = null
) : Exception(message, cause)

class QadbBridgeHttpClient(private val baseUrl: String, private val token: String) : ArtemisBridge {
    // Uvicorn does not accept the JDK client's clear-text HTTP/2 upgrade body
    // reliably: the route can receive an empty body and return 422. The local
    // Artemis control plane is HTTP/1.1, so pin the transport explicitly.
    private val client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val base = URI(baseUrl.trimEnd('/') + "/")

    override suspend fun handshake() {
        val handshake = request("GET", "/qadb/v1/handshake").jsonObject
        val protocolVersion = handshake["protocolVersion"]?.jsonPrimitive?.contentOrNull
        require(protocolVersion == BRIDGE_PROTOCOL_VERSION) { "QADB Bridge protocol is incompatible" }
        val capabilities = handshake["capabilities"]?.jsonObject
        require(capabilities?.get("quiescenceAck")?.jsonPrimitive?.booleanOrNull == true &&
            capabilities["actionGate"]?.jsonPrimitive?.booleanOrNull == true &&
            capabilities["approvals"]?.jsonPrimitive?.booleanOrNull == true
        ) { "QADB Bridge required safety capabilities are missing" }
    }

    override suspend fun register(runId: String, deviceId: String) {
        val payload = buildJsonObject {
            put("device_id", deviceId)
            put("task_version", 0)
        }
        var lastFailure: QadbBridgeException? = null
        repeat(REGISTER_ATTEMPTS) { attempt ->
            try {
                request("POST", "/qadb/v1/runs/$runId", payload)
                return
            } catch (failure: QadbBridgeException) {
                lastFailure = failure
                if (!failure.retryable || attempt == REGISTER_ATTEMPTS - 1) throw failure
                delay(REGISTER_RETRY_DELAY_MS * (attempt + 1))
            }
        }
        throw checkNotNull(lastFailure)
    }
    override suspend fun heartbeat(runId: String) { request("POST", "/qadb/v1/runs/$runId/heartbeat") }
    override suspend fun stop(runId: String): Boolean {
        request("POST", "/qadb/v1/runs/$runId/stop")
        return quiescent(runId)
    }
    override suspend fun quiescent(runId: String): Boolean {
        val control = request("GET", "/qadb/v1/runs/$runId/control").jsonObject
        return control["runId"]?.jsonPrimitive?.contentOrNull == runId &&
            control["stopping"]?.jsonPrimitive?.booleanOrNull == true &&
            control["workerFinished"]?.jsonPrimitive?.booleanOrNull == true &&
            control["dispatchedApprovals"]?.jsonArray?.isEmpty() == true &&
            control["quiescent"]?.jsonPrimitive?.booleanOrNull == true
    }
    override suspend fun pending(runId: String): List<String> = request("GET", "/qadb/v1/runs/$runId/control")
        .jsonObject["pendingApprovals"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    override suspend fun approval(runId: String, approvalId: String): ArtemisApprovalMetadata? {
        val item = request("GET", "/qadb/v1/runs/$runId/approvals/$approvalId").jsonObject
        return ArtemisApprovalMetadata(
            approvalId = item["approvalId"]?.jsonPrimitive?.contentOrNull ?: return null,
            actionKind = item["actionKind"]?.jsonPrimitive?.contentOrNull ?: return null,
            actionTarget = item["actionTarget"]?.jsonPrimitive?.contentOrNull,
            actionDigest = item["actionDigest"]?.jsonPrimitive?.contentOrNull ?: return null,
            deviceId = item["deviceId"]?.jsonPrimitive?.contentOrNull ?: return null,
            taskVersion = item["taskVersion"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: return null,
            expiresInMs = item["expiresInMs"]?.jsonPrimitive?.longOrNull ?: return null,
            pending = item["pending"]?.jsonPrimitive?.booleanOrNull ?: return null
        )
    }
    override suspend fun decide(runId: String, approvalId: String, allowed: Boolean) { request(
        "POST", "/qadb/v1/runs/$runId/approvals/$approvalId", buildJsonObject { put("allowed", allowed) }
    ) }

    private suspend fun request(method: String, path: String, body: kotlinx.serialization.json.JsonObject? = null) = withContext(Dispatchers.IO) {
        val builder = HttpRequest.newBuilder(base.resolve(path.removePrefix("/"))).timeout(Duration.ofSeconds(15))
            .header("X-QADB-Token", token).header("Accept", "application/json")
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody()) else builder.header("Content-Type", "application/json")
            .method(method, HttpRequest.BodyPublishers.ofString(json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), body)))
        val response = try {
            client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        } catch (failure: IOException) {
            throw QadbBridgeException("QADB Bridge is unavailable", retryable = true, cause = failure)
        } catch (failure: InterruptedException) {
            Thread.currentThread().interrupt()
            throw QadbBridgeException("QADB Bridge request was interrupted", retryable = true, cause = failure)
        }
        if (response.statusCode() !in 200..299) {
            val statusCode = response.statusCode()
            throw QadbBridgeException(
                message = when (statusCode) {
                    401, 403 -> "QADB Bridge authorization failed"
                    422 -> "QADB Bridge rejected the request format${validationLocation(response.body())}"
                    else -> "QADB Bridge request failed with HTTP $statusCode"
                },
                statusCode = statusCode,
                retryable = statusCode == 429 || statusCode >= 500
            )
        }
        try {
            json.parseToJsonElement(response.body())
        } catch (failure: Exception) {
            throw QadbBridgeException("QADB Bridge returned invalid JSON", retryable = true, cause = failure)
        }
    }

    /**
     * FastAPI's validation locations identify only the request shape.  Keep
     * that useful clue while deliberately discarding rejected values and every
     * other response field, which could be untrusted diagnostic text.
     */
    private fun validationLocation(responseBody: String): String {
        val location = runCatching {
            json.parseToJsonElement(responseBody).jsonObject["detail"]?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("loc")?.jsonArray
                ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                ?.joinToString(".")
        }.getOrNull().orEmpty()
        return if (location.matches(Regex("[A-Za-z0-9_.-]{1,120}"))) " at $location" else ""
    }

    private companion object {
        const val BRIDGE_PROTOCOL_VERSION = "qadb-bridge-v1"
        const val REGISTER_ATTEMPTS = 3
        const val REGISTER_RETRY_DELAY_MS = 150L
    }
}
