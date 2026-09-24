package com.ludoven.adbtool.agent.artemis

import java.util.ArrayDeque
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ArtemisHttpClientTest {
    @Test
    fun `submission accepts only its matching task and preserves Chinese goal`(): Unit = runBlocking {
        val service = MockArtemisService(
            responses = listOf(
                response(
                    """{"status":"queued","tasks":[{"session_id":"$SESSION","status":"queued","goal":"读取当前电量","device_serial":"emulator-5554","future_field":true}],"enqueued_count":1}"""
                )
            )
        )
        val result = client(service).submit(request())

        val accepted = assertIs<ArtemisSubmitResult.Accepted>(result)
        assertEquals(ArtemisTaskStatus.QUEUED, accepted.handle.status)
        assertEquals(1, accepted.enqueuedCount)
        val body = Json.parseToJsonElement(service.requests.single().body!!).jsonObject
        assertEquals("读取当前电量", body["goal"]?.jsonPrimitive?.content)
        assertEquals("flash", body["profile"]?.jsonPrimitive?.content)
        assertEquals(SESSION, body["session_id"]?.jsonPrimitive?.content)
        assertEquals("emulator-5554", body["device_serial"]?.jsonPrimitive?.content)
        assertTrue("goals" !in body)
    }

    @Test
    fun `HTTP success with rejected business status is not accepted`(): Unit = runBlocking {
        val result = client(
            MockArtemisService(listOf(response("""{"status":"rejected","error":"device unavailable","tasks":[]}""")))
        ).submit(request())

        assertEquals(ArtemisSubmitResult.Rejected("device unavailable"), result)
    }

    @Test
    fun `HTTP success with failed business status keeps the provider reason`(): Unit = runBlocking {
        val result = client(
            MockArtemisService(
                listOf(response("""{"status":"failed","message":"model returned invalid action"}"""))
            )
        ).submit(request())

        assertEquals(ArtemisSubmitResult.Rejected("model returned invalid action"), result)
    }

    @Test
    fun `submission with empty tasks fails protocol instead of inventing a run`(): Unit = runBlocking {
        val service = MockArtemisService(listOf(response("""{"status":"queued","tasks":[]}""")))

        assertFailsWith<ArtemisProtocolException> { client(service).submit(request()) }
    }

    @Test
    fun `queue period 404 is explicitly not persisted yet`(): Unit = runBlocking {
        val lookup = client(MockArtemisService(listOf(ArtemisHttpResponse(404, "{}")))).getSession(SESSION)

        assertEquals(ArtemisSessionLookup.NotPersistedYet, lookup)
    }

    @Test
    fun `stop is always targeted and never escalates to all tasks`(): Unit = runBlocking {
        val service = MockArtemisService(listOf(response("""{"status":"stopped","session_id":"$SESSION"}""")))
        val result = client(service).stop(SESSION)

        assertEquals(ArtemisStopStatus.STOPPED, result)
        val request = service.requests.single()
        assertEquals("POST", request.method)
        assertEquals("/api/stop", request.path)
        val body = Json.parseToJsonElement(request.body!!).jsonObject
        assertEquals(SESSION, body["session_id"]?.jsonPrimitive?.content)
        assertEquals("false", body["all"]?.jsonPrimitive?.content)
    }

    @Test
    fun `status tracks all returned ids without assuming latest belongs to QADB`(): Unit = runBlocking {
        val status = client(
            MockArtemisService(
                listOf(
                    response(
                        """{"status":"running","session_id":"other-session","active_tasks":[{"session_id":"$SESSION"}],"queue":[{"session_id":"queued-session"}]}"""
                    )
                )
            )
        ).getStatus()

        assertEquals(ArtemisTaskStatus.RUNNING, status.status)
        assertEquals(setOf("other-session", SESSION, "queued-session"), status.activeTaskIds)
    }

    @Test
    fun `readiness uses the daemon gate and retains blocker summaries`(): Unit = runBlocking {
        val readiness = client(
            MockArtemisService(
                listOf(response("""{"overall_ready":false,"blockers":[{"summary":"Device Locked"}]}"""))
            )
        ).getReadiness()

        assertEquals(false, readiness.ready)
        assertEquals(listOf("Device Locked"), readiness.blockers)
        assertEquals(listOf("Device Locked"), readiness.serviceBlockers)
    }

    @Test
    fun `global device blocker is deferred to the exact QADB device binding`(): Unit = runBlocking {
        val readiness = client(
            MockArtemisService(
                listOf(response("""{"overall_ready":false,"blockers":[{"id":"android_adb","summary":"Device Locked"}]}"""))
            )
        ).getReadiness()

        assertEquals(false, readiness.ready)
        assertEquals(listOf("Device Locked"), readiness.blockers)
        assertEquals(emptyList(), readiness.serviceBlockers)
    }

    @Test
    fun `readiness derives missing blocker list from failed blocker probes`(): Unit = runBlocking {
        val readiness = client(
            MockArtemisService(
                listOf(response("""{"overall_ready":false,"blockers":[],"probes":[{"id":"system_config","status":"fail","is_blocker":true,"summary":"Config Invalid"}]}"""))
            )
        ).getReadiness()

        assertEquals(listOf("Config Invalid"), readiness.blockers)
        assertEquals(listOf("Config Invalid"), readiness.serviceBlockers)
    }

    @Test
    fun `usage preserves omitted values instead of converting them to zero`(): Unit = runBlocking {
        val usage = client(
            MockArtemisService(listOf(response("""{"llm_calls":4,"prompt_tokens":12,"total_tokens":20}""")))
        ).getUsage(SESSION)

        assertEquals(4, usage.llmCalls)
        assertEquals(12, usage.promptTokens)
        assertEquals(null, usage.completionTokens)
        assertEquals(20, usage.totalTokens)
    }

    @Test
    fun `fragmented SSE drops global and foreign session events`(): Unit = runBlocking {
        val service = MockArtemisService(
            responses = emptyList(),
            streamChunks = listOf(
                "event: session_started\ndata: {\"session_id\":\"other\"}\n\n",
                "event: startup_progress\nda",
                "ta: {\"session_id\":\"$SESSION\",\"message\":\"正在连接\"}\n\n",
                "event: session_ended\ndata: {\"session_id\":\"other\"}\n\n"
            )
        )
        val events = mutableListOf<ArtemisSseEvent>()

        client(service).streamEvents(SESSION, events::add)

        assertEquals(1, events.size)
        assertEquals("startup_progress", events.single().type)
        assertEquals(SESSION, events.single().sessionId)
        assertTrue(events.single().data.contains("正在连接"))
    }

    @Test
    fun `experimental client rejects a non loopback base URL`(): Unit {
        assertFailsWith<IllegalArgumentException> {
            ArtemisHttpClient("https://daemon.example.test", MockArtemisService(emptyList()))
        }
        assertFailsWith<IllegalArgumentException> {
            ArtemisHttpClient("http://127.0.0.1:18761/api", MockArtemisService(emptyList()))
        }
    }

    private fun client(service: MockArtemisService) = ArtemisHttpClient("http://127.0.0.1:18761", service)

    private fun request() = ArtemisRunRequest(
        goal = "读取当前电量",
        profile = ArtemisProfile.FLASH,
        deviceSerial = "emulator-5554",
        sessionId = SESSION
    )

    private fun response(body: String) = ArtemisHttpResponse(200, body)

    private class MockArtemisService(
        responses: List<ArtemisHttpResponse>,
        private val streamChunks: List<String> = emptyList()
    ) : ArtemisHttpTransport {
        private val queuedResponses = ArrayDeque(responses)
        val requests = mutableListOf<ArtemisHttpRequest>()

        override suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse {
            requests += request
            return if (queuedResponses.isEmpty()) {
                error("No mocked response for ${request.method} ${request.path}")
            } else {
                queuedResponses.removeFirst()
            }
        }

        override suspend fun stream(request: ArtemisHttpRequest, onChunk: (String) -> Unit) {
            requests += request
            streamChunks.forEach(onChunk)
        }
    }

    private companion object {
        const val SESSION = "11111111-1111-1111-1111-111111111111"
    }
}
