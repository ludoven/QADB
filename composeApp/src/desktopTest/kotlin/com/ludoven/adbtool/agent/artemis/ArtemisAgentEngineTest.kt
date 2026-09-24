package com.ludoven.adbtool.agent.artemis

import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentAction
import com.ludoven.adbtool.agent.AgentExecutionStrategy
import com.ludoven.adbtool.agent.AgentStepStatus
import com.ludoven.adbtool.agent.AgentTaskUiState
import java.util.ArrayDeque
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class ArtemisAgentEngineTest {
    @Test
    fun `completed external task maps to completed QADB state`(): Unit = runBlocking {
        val service = FakeTransport(
            responses = listOf(
                json("""{"status":"queued","tasks":[{"session_id":"$RUN","status":"queued"}],"enqueued_count":1}"""),
                json("""{"session_id":"$RUN","status":"completed","summary":"读取到 73%"}""")
            )
        )
        val states = mutableListOf<AgentTaskUiState>()
        val result = ArtemisAgentEngine(
            ArtemisHttpClient(BASE_URL, service),
            ArtemisProfile.FLASH,
            FakeBridge(),
            registry = InMemoryRegistry(),
            pollIntervalMs = 100
        )
            .run("读取电量", "emulator-5554", AgentTaskUiState(), RUN, onState = states::add)

        assertFalse(result.isRunning)
        assertEquals(AgentRunPhase.COMPLETED, result.phase)
        assertEquals(AgentExecutionStrategy.SEMANTIC_V2, states.first().executionStrategy)
        assertTrue(result.messages.last().text.contains("读取到 73%"))
        assertTrue(result.messages.last().text.contains("尚未独立核实"))
        assertEquals(listOf("POST", "GET"), service.requests.map { it.method })
    }

    @Test
    fun `completed stream event remains progress until owned session is terminal`(): Unit = runBlocking {
        val service = IntermediateCompletedEventTransport()
        val states = mutableListOf<AgentTaskUiState>()

        val result = ArtemisAgentEngine(
            ArtemisHttpClient(BASE_URL, service),
            ArtemisProfile.FLASH,
            FakeBridge(),
            registry = InMemoryRegistry(),
            pollIntervalMs = 100
        ).run("打开浏览器", "emulator-5554", AgentTaskUiState(), RUN, onState = states::add)

        val intermediate = states.first { "subtask complete" in it.executionDetails }
        assertEquals(AgentRunPhase.EXECUTING, intermediate.phase)
        assertEquals(AgentRunPhase.COMPLETED, result.phase)
    }

    @Test
    fun `bridge approval is published before waiting and decided once`(): Unit = runBlocking {
        val bridge = RecordingApprovalBridge()
        val states = mutableListOf<AgentTaskUiState>()

        ArtemisAgentEngine(
            ArtemisHttpClient(
                BASE_URL,
                FakeTransport(
                    listOf(
                        json("""{"status":"queued","tasks":[{"session_id":"$RUN","status":"queued"}]}"""),
                        json("""{"session_id":"$RUN","status":"completed","summary":"done"}""")
                    )
                )
            ),
            ArtemisProfile.FLASH,
            bridge,
            registry = InMemoryRegistry(),
            pollIntervalMs = 100
        ).run(
            "打开浏览器",
            "emulator-5554",
            AgentTaskUiState(),
            RUN,
            onState = states::add,
            confirmSensitiveAction = { step ->
                assertEquals(step, states.last().pendingConfirmation)
                assertEquals(AgentRunPhase.AWAITING_CONFIRMATION, states.last().phase)
                assertTrue(step.action is AgentAction.ExternalApproval)
                true
            }
        )

        assertEquals(listOf("approval-1" to true), bridge.decisions)
        assertTrue(states.any { state ->
            state.steps.any { it.id == "artemis-approval-1" && it.status == AgentStepStatus.RUNNING }
        })
    }

    @Test
    fun `approval without bound target is denied before UI confirmation`(): Unit = runBlocking {
        val bridge = RecordingApprovalBridge(actionTarget = null)
        var prompted = false
        ArtemisAgentEngine(
            ArtemisHttpClient(
                BASE_URL,
                FakeTransport(listOf(
                    json("""{"status":"queued","tasks":[{"session_id":"$RUN","status":"queued"}]}"""),
                    json("""{"session_id":"$RUN","status":"completed","summary":"done"}""")
                ))
            ), ArtemisProfile.FLASH, bridge, registry = InMemoryRegistry(), pollIntervalMs = 100
        ).run("stop app", "emulator-5554", AgentTaskUiState(), RUN,
            confirmSensitiveAction = { prompted = true; true })

        assertFalse(prompted)
        assertEquals(listOf("approval-1" to false), bridge.decisions)
    }

    @Test
    fun `approval metadata rejects wrong device stale version and expiry`() {
        val valid = ArtemisApprovalMetadata(
            "approval-1", "manage_app_uninstall", "com.example.app", "a".repeat(64),
            "emulator-5554", 0, 15_000, true
        )
        assertTrue(valid.isReviewable("approval-1", "emulator-5554"))
        assertTrue(valid.copy(actionKind = "press_key_enter", actionTarget = "Enter / submit key")
            .isReviewable("approval-1", "emulator-5554"))
        assertFalse(valid.copy(deviceId = "other").isReviewable("approval-1", "emulator-5554"))
        assertFalse(valid.copy(taskVersion = 1).isReviewable("approval-1", "emulator-5554"))
        assertFalse(valid.copy(expiresInMs = 0).isReviewable("approval-1", "emulator-5554"))
        assertFalse(valid.copy(actionKind = "unknown").isReviewable("approval-1", "emulator-5554"))
    }

    @Test
    fun `cancellation stops only the matching external session`(): Unit = runBlocking {
        val service = BlockingSessionTransport()
        val engine = ArtemisAgentEngine(
            ArtemisHttpClient(BASE_URL, service),
            ArtemisProfile.PRO,
            FakeBridge(),
            registry = InMemoryRegistry(),
            pollIntervalMs = 100
        )
        val running = async {
            engine.run("读取电量", "emulator-5554", AgentTaskUiState(), RUN)
        }
        withTimeout(2_000) { service.sessionReadStarted.await() }

        assertFalse(engine.cancel(RUN))
        running.cancel()
        withTimeout(2_000) { runCatching { running.await() } }
        assertEquals("/api/stop", service.requests.last().path)
        assertTrue(service.requests.last().body.orEmpty().contains("\"all\":false"))
        assertTrue(service.requests.last().body.orEmpty().contains(RUN))
    }

    @Test
    fun `device lease is exclusive per device and releases only its holder`() {
        val leases = DeviceLeaseManager()
        assertTrue(leases.tryAcquire("serial", "run-a"))
        assertFalse(leases.tryAcquire("serial", "run-b"))
        leases.release("serial", "run-b")
        assertEquals("run-a", leases.holder("serial"))
        leases.release("serial", "run-a")
        assertTrue(leases.tryAcquire("serial", "run-b"))
    }

    @Test
    fun `event mapper rejects foreign session and maps session progress`() {
        val event = ArtemisSseEvent("progress", """{"session_id":"$RUN","status":"running","message":"observing"}""", RUN)

        assertEquals(ArtemisTaskStatus.RUNNING, ArtemisEventMapper.map(event, RUN)?.status)
        assertEquals("observing", ArtemisEventMapper.map(event, RUN)?.detail)
        assertEquals(null, ArtemisEventMapper.map(event, "other"))
    }

    @Test
    fun `restart reconciliation preserves a missing session as submission unknown without resubmitting`(): Unit = runBlocking {
        val registry = InMemoryRegistry(
            ExternalTaskRecord(RUN, RUN, "emulator-5554", "flash", "submitting", 1)
        )
        val service = FakeTransport(listOf(ArtemisHttpResponse(404, "{}")))

        val result = ExternalTaskReconciler(registry, ArtemisHttpClient(BASE_URL, service)).reconcile().single()

        assertEquals("submission_unknown", result.status)
        assertEquals(listOf("GET"), service.requests.map { it.method })
    }

    @Test
    fun `unconfirmed stop remains blocked until exact worker quiescence is known`(): Unit = runBlocking {
        val record = ExternalTaskRecord(RUN, RUN, "emulator-5554", "flash", "stop_unconfirmed", 1)
        fun transport() = FakeTransport(listOf(json("""{"session_id":"$RUN","status":"completed"}""")))

        val unresolved = ExternalTaskReconciler(
            InMemoryRegistry(record), ArtemisHttpClient(BASE_URL, transport()), FakeBridge()
        ).reconcile().single()
        assertEquals("stop_unconfirmed", unresolved.status)

        val resolved = ExternalTaskReconciler(
            InMemoryRegistry(record), ArtemisHttpClient(BASE_URL, transport()), FakeBridge(quiescent = true)
        ).reconcile().single()
        assertEquals("stopped", resolved.status)
    }

    private class FakeTransport(responses: List<ArtemisHttpResponse>) : ArtemisHttpTransport {
        private val queued = ArrayDeque(responses)
        val requests = mutableListOf<ArtemisHttpRequest>()

        override suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse {
            requests += request
            return queued.removeFirst()
        }
    }

    private class BlockingSessionTransport : ArtemisHttpTransport {
        val requests = mutableListOf<ArtemisHttpRequest>()
        val sessionReadStarted = kotlinx.coroutines.CompletableDeferred<Unit>()

        override suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse {
            requests += request
            return when (request.path) {
                "/api/run" -> json("""{"status":"queued","tasks":[{"session_id":"$RUN","status":"queued"}]}""")
                "/api/sessions/$RUN" -> {
                    sessionReadStarted.complete(Unit)
                    delay(Long.MAX_VALUE)
                    error("unreachable")
                }
                "/api/stop" -> json("""{"status":"stopped","session_id":"$RUN"}""")
                else -> error("Unexpected request ${request.path}")
            }
        }
    }

    private class IntermediateCompletedEventTransport : ArtemisHttpTransport {
        private var sessionReads = 0

        override suspend fun execute(request: ArtemisHttpRequest): ArtemisHttpResponse = when (request.path) {
            "/api/run" -> json("""{"status":"queued","tasks":[{"session_id":"$RUN","status":"queued"}]}""")
            "/api/sessions/$RUN" -> {
                sessionReads += 1
                if (sessionReads == 1) {
                    delay(100)
                    json("""{"session_id":"$RUN","status":"running"}""")
                } else {
                    json("""{"session_id":"$RUN","status":"completed","summary":"done"}""")
                }
            }
            else -> error("Unexpected request ${request.path}")
        }

        override suspend fun stream(request: ArtemisHttpRequest, onChunk: (String) -> Unit) {
            onChunk(
                "event: progress\n" +
                    "data: {\"session_id\":\"$RUN\",\"status\":\"completed\",\"message\":\"subtask complete\"}\n\n"
            )
            delay(Long.MAX_VALUE)
        }
    }

    private class InMemoryRegistry(vararg initialRecords: ExternalTaskRecord) : ExternalTaskRegistry {
        private val records = initialRecords.toMutableList()
        override fun upsert(record: ExternalTaskRecord) {
            records.removeAll { it.runId == record.runId }
            records += record
        }
        override fun find(runId: String): ExternalTaskRecord? = records.lastOrNull { it.runId == runId }
        override fun recent(): List<ExternalTaskRecord> = records.toList()
    }

    private class FakeBridge(private val quiescent: Boolean = false) : ArtemisBridge {
        override suspend fun handshake() = Unit
        override suspend fun register(runId: String, deviceId: String) = Unit
        override suspend fun heartbeat(runId: String) = Unit
        override suspend fun stop(runId: String): Boolean = false
        override suspend fun quiescent(runId: String): Boolean = quiescent
        override suspend fun pending(runId: String): List<String> = emptyList()
        override suspend fun decide(runId: String, approvalId: String, allowed: Boolean) = Unit
    }

    private class RecordingApprovalBridge(private val actionTarget: String? = "com.example.app") : ArtemisBridge {
        val decisions = mutableListOf<Pair<String, Boolean>>()

        override suspend fun handshake() = Unit
        override suspend fun register(runId: String, deviceId: String) = Unit
        override suspend fun heartbeat(runId: String) = Unit
        override suspend fun stop(runId: String): Boolean = false
        override suspend fun pending(runId: String): List<String> =
            if (decisions.isEmpty()) listOf("approval-1") else emptyList()

        override suspend fun approval(runId: String, approvalId: String): ArtemisApprovalMetadata =
            ArtemisApprovalMetadata(
                approvalId, "manage_app_uninstall", actionTarget, "a".repeat(64),
                "emulator-5554", 0, 15_000, decisions.isEmpty()
            )

        override suspend fun decide(runId: String, approvalId: String, allowed: Boolean) {
            decisions += approvalId to allowed
        }
    }

    private companion object {
        const val BASE_URL = "http://127.0.0.1:18761"
        const val RUN = "11111111-1111-1111-1111-111111111111"
        fun json(body: String) = ArtemisHttpResponse(200, body)
    }
}
