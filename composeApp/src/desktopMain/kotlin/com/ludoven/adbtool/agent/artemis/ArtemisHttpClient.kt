package com.ludoven.adbtool.agent.artemis

import java.net.InetAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class ArtemisHttpRequest(
    val method: String,
    val path: String,
    val body: String? = null,
    val accept: String = "application/json"
)

data class ArtemisHttpResponse(val statusCode: Int, val body: String)

/** Kept small so protocol fixtures can test HTTP and SSE without a device or Python runtime. */
interface ArtemisHttpTransport {
    suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse

    suspend fun stream(request: ArtemisHttpRequest, onChunk: (String) -> Unit) {
        throw UnsupportedOperationException("This Artemis transport does not provide SSE")
    }
}

private class JdkArtemisHttpTransport(
    private val baseUri: URI,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .build()
) : ArtemisHttpTransport {
    override suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse = withContext(Dispatchers.IO) {
        val httpRequest = request.toJdkRequest(baseUri)
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        ArtemisHttpResponse(response.statusCode(), response.body())
    }

    override suspend fun stream(request: ArtemisHttpRequest, onChunk: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val response = httpClient.send(request.toJdkRequest(baseUri), HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() !in 200..299) {
                response.body().close()
                throw ArtemisHttpException(response.statusCode(), "Artemis SSE request failed with HTTP ${response.statusCode()}")
            }
            response.body().bufferedReader(StandardCharsets.UTF_8).use { reader ->
                reader.forEachLine { line -> onChunk("$line\n") }
            }
        }
    }

    private fun ArtemisHttpRequest.toJdkRequest(baseUri: URI): HttpRequest {
        val target = baseUri.resolve(path.removePrefix("/"))
        val builder = HttpRequest.newBuilder(target)
            .timeout(Duration.ofSeconds(30))
            .header("Accept", accept)
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody())
        } else {
            builder.header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
        }
        return builder.build()
    }
}

/** HTTP client for the pinned Artemis E0/E1 protocol; it deliberately has no QADB Bridge routes. */
class ArtemisHttpClient(
    baseUrl: String,
    private val transport: ArtemisHttpTransport? = null
) {
    private val baseUri = validateLocalBaseUrl(baseUrl)
    private val actualTransport: ArtemisHttpTransport = transport ?: JdkArtemisHttpTransport(baseUri)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun submit(request: ArtemisRunRequest): ArtemisSubmitResult {
        val payload = buildJsonObject {
            put("goal", request.goal)
            put("profile", request.profile.wireValue)
            put("device_serial", request.deviceSerial)
            put("session_id", request.sessionId)
            request.conversationId?.takeIf(String::isNotBlank)?.let { put("conversation_id", it) }
            request.verificationLevel?.let { put("verification_level", it) }
            request.explorerMode?.let { put("explorer_mode", it) }
        }
        val response = actualTransport.execute(
            ArtemisHttpRequest("POST", "/api/run", json.encodeToString(JsonObject.serializer(), payload))
        )
        return parseSubmission(response, request.sessionId)
    }

    suspend fun getSession(sessionId: String): ArtemisSessionLookup {
        val response = actualTransport.execute(ArtemisHttpRequest("GET", "/api/sessions/$sessionId"))
        if (response.statusCode == 404) return ArtemisSessionLookup.NotPersistedYet
        val root = requireSuccessObject(response, "session lookup")
        val task = root["session"]?.jsonObject ?: root
        val resolvedId = task.string("session_id") ?: sessionId
        require(resolvedId == sessionId) { "Artemis session lookup returned a different session id" }
        return ArtemisSessionLookup.Found(
            handle = task.toHandle(resolvedId),
            summary = task.string("summary") ?: task.string("result") ?: task.string("error"),
            rawStatus = task.string("status")
        )
    }

    suspend fun getStatus(): ArtemisStatusSnapshot {
        val root = requireSuccessObject(
            actualTransport.execute(ArtemisHttpRequest("GET", "/api/status")),
            "status lookup"
        )
        val active = buildSet {
            root["session_id"]?.jsonPrimitive?.contentOrNull?.let(::add)
            root["active_tasks"]?.jsonArray?.forEach { item ->
                item.jsonObject.string("session_id")?.let(::add)
            }
            root["queue"]?.jsonArray?.forEach { item ->
                item.jsonObject.string("session_id")?.let(::add)
            }
        }
        return ArtemisStatusSnapshot(
            status = ArtemisTaskStatus.fromWire(root.string("status")),
            sessionId = root.string("session_id"),
            activeTaskIds = active
        )
    }

    /** Admission readiness is local-daemon state, not proof of task safety or completion. */
    suspend fun getReadiness(): ArtemisReadinessSnapshot {
        val root = requireSuccessObject(
            actualTransport.execute(ArtemisHttpRequest("GET", "/api/system/readiness")),
            "readiness lookup"
        )
        val explicitBlockers = root["blockers"]?.jsonArray.orEmpty().map { it.jsonObject }
        val probeBlockers = root["probes"]?.jsonArray.orEmpty()
            .map { it.jsonObject }
            .filter { probe ->
                probe["is_blocker"]?.jsonPrimitive?.booleanOrNull == true &&
                    probe.string("status") !in setOf("pass", "ok", "ready")
            }
        val blockerItems = explicitBlockers.ifEmpty { probeBlockers }
        fun blockerSummary(item: JsonObject): String? =
            item.string("message") ?: item.string("summary")
        val blockers = blockerItems.mapNotNull(::blockerSummary)
        val serviceBlockers = blockerItems.mapNotNull { item ->
            blockerSummary(item)?.takeUnless { item.string("id") == "android_adb" }
        }
        return ArtemisReadinessSnapshot(
            ready = root["overall_ready"]?.jsonPrimitive?.booleanOrNull
                ?: root["ready"]?.jsonPrimitive?.booleanOrNull
                ?: false,
            blockers = blockers,
            serviceBlockers = serviceBlockers
        )
    }

    suspend fun getUsage(sessionId: String): ArtemisUsageSnapshot {
        val root = requireSuccessObject(
            actualTransport.execute(ArtemisHttpRequest("GET", "/api/sessions/$sessionId/usage")),
            "usage lookup"
        )
        fun number(vararg keys: String): Long? = keys.asSequence()
            .mapNotNull { root[it]?.jsonPrimitive?.contentOrNull?.toLongOrNull() }
            .firstOrNull()
        return ArtemisUsageSnapshot(
            llmCalls = number("llm_calls", "model_calls")?.toInt(),
            promptTokens = number("prompt_tokens"),
            completionTokens = number("completion_tokens"),
            totalTokens = number("total_tokens")
        )
    }

    /** Never sends `all=true`; a QADB task may only target its own upstream session. */
    suspend fun stop(sessionId: String): ArtemisStopStatus {
        require(sessionId.isNotBlank()) { "Artemis session id is required to stop a task" }
        val payload = buildJsonObject {
            put("session_id", sessionId)
            put("all", false)
        }
        val root = requireSuccessObject(
            actualTransport.execute(
                ArtemisHttpRequest("POST", "/api/stop", json.encodeToString(JsonObject.serializer(), payload))
            ),
            "stop request"
        )
        return when (root.string("status")) {
            "stopped" -> ArtemisStopStatus.STOPPED
            "no_running_task" -> ArtemisStopStatus.NO_RUNNING_TASK
            else -> throw ArtemisProtocolException("Artemis stop response has an unknown status")
        }
    }

    /**
     * Reads a session-specific stream and drops global or foreign events.  The
     * upstream stream intentionally emits some global lifecycle events, so URL
     * scoping alone is not sufficient for QADB's task ownership boundary.
     */
    suspend fun streamEvents(sessionId: String, onEvent: (ArtemisSseEvent) -> Unit) {
        val decoder = ArtemisSseDecoder()
        actualTransport.stream(ArtemisHttpRequest("GET", "/api/stream/$sessionId", accept = "text/event-stream")) { chunk ->
            decoder.push(chunk)
                .filter { event -> event.sessionId == sessionId }
                .forEach(onEvent)
        }
    }

    private fun parseSubmission(response: ArtemisHttpResponse, expectedSessionId: String): ArtemisSubmitResult {
        val root = requireSuccessObject(response, "task submission")
        val status = root.string("status")?.lowercase()
        if (status in setOf("rejected", "failed", "error")) {
            return ArtemisSubmitResult.Rejected(
                root.string("error")
                    ?: root.string("message")
                    ?: root.string("detail")
                    ?: "Artemis $status the task without a reason"
            )
        }
        val tasks = root["tasks"] as? JsonArray
            ?: throw ArtemisProtocolException("Artemis submission response does not contain tasks")
        val matchingTask = tasks.map { it.jsonObject }
            .singleOrNull { it.string("session_id") == expectedSessionId }
            ?: throw ArtemisProtocolException("Artemis submission response has no task for the requested session id")
        val handle = matchingTask.toHandle(expectedSessionId)
        if (handle.status == ArtemisTaskStatus.REJECTED) {
            return ArtemisSubmitResult.Rejected(matchingTask.string("error") ?: "Artemis rejected the task")
        }
        if (handle.status == ArtemisTaskStatus.UNKNOWN) {
            throw ArtemisProtocolException("Artemis submission task has no recognized status")
        }
        return ArtemisSubmitResult.Accepted(handle, root["enqueued_count"]?.jsonPrimitive?.contentOrNull?.toIntOrNull())
    }

    private fun requireSuccessObject(response: ArtemisHttpResponse, operation: String): JsonObject {
        if (response.statusCode !in 200..299) {
            throw ArtemisHttpException(response.statusCode, "Artemis $operation failed with HTTP ${response.statusCode}")
        }
        return runCatching { json.parseToJsonElement(response.body).jsonObject }
            .getOrElse { throw ArtemisProtocolException("Artemis $operation returned invalid JSON") }
    }

    private fun JsonObject.toHandle(fallbackSessionId: String): ArtemisTaskHandle = ArtemisTaskHandle(
        sessionId = string("session_id") ?: fallbackSessionId,
        status = ArtemisTaskStatus.fromWire(string("status")),
        goal = string("goal"),
        deviceSerial = string("device_serial")
    )

    private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
}

/** Incremental SSE decoder that keeps incomplete lines and frames until they are complete. */
class ArtemisSseDecoder {
    private var pendingLine = ""
    private var eventType = "message"
    private val dataLines = mutableListOf<String>()

    fun push(chunk: String): List<ArtemisSseEvent> {
        val events = mutableListOf<ArtemisSseEvent>()
        val text = pendingLine + chunk
        val lines = text.split('\n')
        pendingLine = if (text.endsWith('\n')) "" else lines.last()
        lines.dropLast(if (text.endsWith('\n')) 0 else 1).forEach { rawLine ->
            val line = rawLine.removeSuffix("\r")
            if (line.isEmpty()) {
                finishEvent()?.let(events::add)
            } else if (!line.startsWith(":")) {
                val field = line.substringBefore(':')
                val value = line.substringAfter(':', "").removePrefix(" ")
                when (field) {
                    "event" -> eventType = value.ifBlank { "message" }
                    "data" -> dataLines += value
                }
            }
        }
        return events
    }

    private fun finishEvent(): ArtemisSseEvent? {
        if (dataLines.isEmpty()) {
            eventType = "message"
            return null
        }
        val data = dataLines.joinToString("\n")
        val sessionId = runCatching {
            Json.parseToJsonElement(data).jsonObject["session_id"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        val event = ArtemisSseEvent(eventType, data, sessionId)
        eventType = "message"
        dataLines.clear()
        return event
    }
}

private fun validateLocalBaseUrl(value: String): URI {
    val uri = runCatching { URI.create(value.trim()) }
        .getOrElse { throw IllegalArgumentException("Artemis base URL is invalid") }
    require(uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) { "Artemis base URL must use HTTP or HTTPS" }
    require(!uri.host.isNullOrBlank()) { "Artemis base URL must include a host" }
    require(uri.userInfo == null && uri.query == null && uri.fragment == null) { "Artemis base URL must not contain credentials, query, or fragment" }
    require(uri.rawPath.isNullOrBlank() || uri.rawPath == "/") { "Artemis base URL must not contain a path" }
    require(isLoopbackHost(uri.host)) { "Experimental Artemis must use a loopback service" }
    return URI("${uri.scheme}://${uri.rawAuthority}${uri.rawPath.orEmpty().trimEnd('/')}/")
}

private fun isLoopbackHost(host: String): Boolean {
    val normalized = host.removePrefix("[").removeSuffix("]").removeSuffix(".").lowercase()
    return normalized == "localhost" || normalized.endsWith(".localhost") ||
        runCatching { InetAddress.getByName(normalized).isLoopbackAddress }.getOrDefault(false)
}
