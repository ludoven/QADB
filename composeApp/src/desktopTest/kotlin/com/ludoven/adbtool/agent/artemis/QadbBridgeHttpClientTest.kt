package com.ludoven.adbtool.agent.artemis

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class QadbBridgeHttpClientTest {
    @Test
    fun `approval metadata and stop quiescence are read from exact Bridge routes`() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/qadb/v1/runs/run/approvals/approval") { exchange ->
                val response = """{"approvalId":"approval","actionKind":"manage_app_uninstall",
                    "actionTarget":"com.example.app","actionDigest":"${"a".repeat(64)}",
                    "deviceId":"emulator-5554","taskVersion":0,"expiresInMs":12000,"pending":true}"""
                    .encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            createContext("/qadb/v1/runs/run/stop") { exchange ->
                exchange.requestBody.close()
                val response = """{"stopping":true,"quiescent":false}""".encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            createContext("/qadb/v1/runs/run/control") { exchange ->
                val response = """{"runId":"run","stopping":true,"workerFinished":true,
                    "quiescent":true,"dispatchedApprovals":[],"pendingApprovals":[]}""".encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        try {
            val client = QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token")
            val approval = client.approval("run", "approval")
            assertEquals("com.example.app", approval?.actionTarget)
            assertEquals("emulator-5554", approval?.deviceId)
            assertTrue(client.stop("run"))
            assertTrue(client.quiescent("run"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `quiescence rejects a bare boolean without worker proof`() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/qadb/v1/runs/run/control") { exchange ->
                val response = """{"runId":"run","quiescent":true}""".encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        try {
            assertFalse(QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token")
                .quiescent("run"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `handshake accepts only the expected Bridge protocol`() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/qadb/v1/handshake") { exchange ->
                val response = """{"protocolVersion":"qadb-bridge-v1","capabilities":
                    {"quiescenceAck":true,"actionGate":true,"approvals":true}}""".encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        try {
            QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token").handshake()
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `register sends the Bridge run registration contract`() = runBlocking {
        val runId = UUID.randomUUID().toString()
        var method: String? = null
        var path: String? = null
        var contentType: String? = null
        var token: String? = null
        var body: String? = null
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/qadb/v1/runs/$runId") { exchange ->
                method = exchange.requestMethod
                path = exchange.requestURI.path
                contentType = exchange.requestHeaders.getFirst("Content-Type")
                token = exchange.requestHeaders.getFirst("X-QADB-Token")
                body = exchange.requestBody.bufferedReader().use { it.readText() }
                val response = "{\"runId\":\"$runId\"}".encodeToByteArray()
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        try {
            QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token")
                .register(runId, "emulator-5554")

            assertEquals("POST", method)
            assertEquals("/qadb/v1/runs/$runId", path)
            assertTrue(contentType.orEmpty().startsWith("application/json"))
            assertEquals("test-bridge-token", token)
            val payload = Json.parseToJsonElement(requireNotNull(body)).jsonObject
            assertEquals("emulator-5554", payload["device_id"]?.jsonPrimitive?.content)
            assertEquals("0", payload["task_version"]?.jsonPrimitive?.content)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `validation error exposes only its field location`() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                val response = """{"detail":[{"loc":["body","device_id"],"input":"must-not-leak"}]}"""
                    .encodeToByteArray()
                exchange.sendResponseHeaders(422, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        try {
            val error = assertFailsWith<QadbBridgeException> {
                QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token")
                    .register("runs", "emulator-5554")
            }
            assertEquals("QADB Bridge rejected the request format at body.device_id", error.message)
            assertTrue(!error.message.orEmpty().contains("must-not-leak"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `register retries transient Bridge failures with the same run id`() = runBlocking {
        val runId = UUID.randomUUID().toString()
        var attempts = 0
        val paths = mutableListOf<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/qadb/v1/runs/$runId") { exchange ->
                attempts += 1
                paths += exchange.requestURI.path
                exchange.requestBody.close()
                val status = if (attempts < 3) 503 else 200
                val response = if (status == 200) "{\"runId\":\"$runId\"}" else "{}"
                val bytes = response.encodeToByteArray()
                exchange.sendResponseHeaders(status, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
        try {
            QadbBridgeHttpClient("http://127.0.0.1:${server.address.port}", "test-bridge-token")
                .register(runId, "emulator-5554")

            assertEquals(3, attempts)
            assertEquals(List(3) { "/qadb/v1/runs/$runId" }, paths)
        } finally {
            server.stop(0)
        }
    }
}
