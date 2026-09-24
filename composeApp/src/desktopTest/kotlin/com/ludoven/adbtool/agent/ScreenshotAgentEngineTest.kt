package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenshotAgentEngineTest {
    @Test
    fun mapsPermilleCoordinatesAndCapturesFreshScreenshotAfterAction() = runBlocking {
        val device = FakeScreenshotDevice(width = 1_080, height = 1_920)
        val model = FakeScreenshotModel { request, call ->
            if (call == 1) {
                ScreenshotAgentDecision.Execute(
                    AgentAction.Tap(500, 250, request.frame.observationId),
                    request.frame.revision
                )
            } else {
                ScreenshotAgentDecision.Finish("done", request.frame.revision)
            }
        }
        val engine = ScreenshotAgentEngine(
            model,
            device,
            hardActionLimit = 2,
            softActionLimit = 1,
            progressCheckInterval = 1,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        )

        val result = engine.runTask()

        val tap = device.executed.single() as AgentAction.Tap
        assertEquals(540, tap.x)
        assertEquals(480, tap.y)
        assertEquals(2, device.observeCount)
        assertFalse(result.isRunning)
    }

    @Test
    fun softLimitStopsWhenRecentScreensShowNoProgress() = runBlocking {
        val device = FakeScreenshotDevice(changeScreenshot = false)
        val model = FakeScreenshotModel { request, _ ->
            ScreenshotAgentDecision.Execute(AgentAction.Wait(100), request.frame.revision)
        }
        val engine = ScreenshotAgentEngine(
            model = model,
            deviceGateway = device,
            softActionLimit = 2,
            hardActionLimit = 4,
            progressCheckInterval = 1,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        )

        val result = engine.runTask()

        assertEquals(2, result.steps.size)
        assertTrue(result.messages.last().text.contains("没有检测到持续进展"))
        assertEquals(0, model.progressCalls)
    }

    @Test
    fun progressChecksAllowWorkUntilHardLimitThenStop() = runBlocking {
        val device = FakeScreenshotDevice(changeScreenshot = true)
        val model = FakeScreenshotModel { request, _ ->
            ScreenshotAgentDecision.Execute(AgentAction.Wait(100), request.frame.revision)
        }
        val engine = ScreenshotAgentEngine(
            model = model,
            deviceGateway = device,
            softActionLimit = 2,
            hardActionLimit = 4,
            progressCheckInterval = 1,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        )

        val result = engine.runTask()

        assertEquals(4, result.steps.size)
        assertEquals(2, model.progressCalls)
        assertTrue(result.needsUser)
        assertTrue(result.messages.last().text.contains("4 步硬限制"))
    }

    @Test
    fun staleRevisionIsRejectedBeforeDeviceExecution() = runBlocking {
        val device = FakeScreenshotDevice()
        val model = FakeScreenshotModel { request, _ ->
            ScreenshotAgentDecision.Execute(AgentAction.Wait(100), request.frame.revision - 1)
        }

        val result = ScreenshotAgentEngine(
            model,
            device,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        ).runTask()

        assertTrue(result.errorMessage.orEmpty().contains("stale screenshot revision"))
        assertTrue(device.executed.isEmpty())
    }

    @Test
    fun protocolFailureIsRetriedOnlyOnce() = runBlocking {
        val device = FakeScreenshotDevice()
        val model = FakeScreenshotModel { request, call ->
            if (call == 1) {
                throw ModelProtocolIssueException(
                    ModelProtocolIssue(
                        code = ModelProtocolIssueCode.MISSING_ARGUMENT,
                        operation = AgentModelOperation.ACTION,
                        role = AgentModelRole.BRAIN,
                        message = "Missing argument: revision"
                    )
                )
            }
            ScreenshotAgentDecision.Finish("done", request.frame.revision)
        }

        val result = ScreenshotAgentEngine(
            model,
            device,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        ).runTask()

        assertEquals(2, model.decisionCalls)
        assertEquals(AgentRunPhase.COMPLETED, result.phase)
    }

    @Test
    fun textInputCanUseFocusedFieldWhenUiHierarchyIsUnavailable() {
        val observation = AgentObservation(
            screenshotPng = byteArrayOf(1),
            uiHierarchy = "",
            currentActivity = "com.example/.MainActivity",
            screenWidth = 1_000,
            screenHeight = 2_000,
            observationId = "latest",
            uiNodes = emptyList()
        )

        val result = validateScreenshotAgentAction(
            AgentAction.InputText(text = "hello", observationId = "latest"),
            observation
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun uncertainSendIsConfirmedOnceAndNeverReplayed() = runBlocking {
        val device = FakeScreenshotDevice(changeScreenshot = false)
        val model = FakeScreenshotModel { request, _ ->
            ScreenshotAgentDecision.Execute(
                AgentAction.Tap(
                    x = 500,
                    y = 900,
                    observationId = request.frame.observationId,
                    meta = AgentActionMeta(target = "send", operationKind = AgentOperationKind.SEND)
                ),
                request.frame.revision
            )
        }
        var confirmationCount = 0

        val result = ScreenshotAgentEngine(
            model,
            device,
            taskLogStoreProvider = { NoopAgentTaskLogStore }
        ).run(
            task = "发送消息",
            deviceId = "device",
            onState = {},
            confirmSensitiveAction = {
                confirmationCount += 1
                true
            }
        )

        assertEquals(1, confirmationCount)
        assertEquals(1, device.executed.size)
        assertTrue(result.needsUser)
        assertTrue(result.messages.last().text.contains("未自动重复执行"))
    }

    @Test
    fun approvalIsInvalidatedWhenTheScreenChangesBeforeDispatch() = runBlocking {
        val device = FakeScreenshotDevice(changeScreenshot = true)
        val model = FakeScreenshotModel { request, _ ->
            ScreenshotAgentDecision.Execute(
                AgentAction.Tap(
                    500, 900, request.frame.observationId,
                    meta = AgentActionMeta(target = "send", operationKind = AgentOperationKind.SEND)
                ),
                request.frame.revision
            )
        }

        val result = ScreenshotAgentEngine(
            model, device, taskLogStoreProvider = { NoopAgentTaskLogStore }
        ).run("发送消息", "device", onState = {}, confirmSensitiveAction = { true })

        assertTrue(result.needsUser)
        assertTrue(result.messages.last().text.contains("原授权失效"))
        assertTrue(device.executed.isEmpty())
    }

    @Test
    fun openAiProtocolParsesOneNormalizedTapBoundToExactRevision() {
        val frame = ScreenshotObservationFrame(
            revision = 42,
            observationId = "observation",
            screenshot = byteArrayOf(1),
            screenshotMimeType = "image/png",
            deviceWidth = 1_080,
            deviceHeight = 1_920,
            foregroundApp = null,
            uiHint = null
        )
        val response = screenshotToolResponse(
            "tap",
            """{"x":500,"y":250,"revision":"42","operation_kind":"NAVIGATION"}"""
        )

        val decision = OpenAiCompatibleClient().parseScreenshotAgentDecision(response, frame)

        val execute = decision as ScreenshotAgentDecision.Execute
        val tap = execute.action as AgentAction.Tap
        assertEquals(42, execute.revision)
        assertEquals(500, tap.x)
        assertEquals(250, tap.y)
        assertEquals("observation", tap.observationId)
    }

    @Test
    fun openAiProtocolAcceptsLegacyFunctionCallWithObjectArguments() {
        val frame = screenshotFrame()
        val response = buildJsonObject {
            put("choices", buildJsonArray {
                add(buildJsonObject {
                    put("message", buildJsonObject {
                        put("function_call", buildJsonObject {
                            put("name", "finish")
                            put("arguments", buildJsonObject {
                                put("summary", "done")
                                put("revision", "42")
                            })
                        })
                    })
                })
            })
        }

        val decision = OpenAiCompatibleClient().parseScreenshotAgentDecision(response, frame)

        assertEquals("done", (decision as ScreenshotAgentDecision.Finish).summary)
    }

    @Test
    fun openAiProtocolAcceptsExactJsonToolCallContent() {
        val frame = screenshotFrame()
        val response = buildJsonObject {
            put("choices", buildJsonArray {
                add(buildJsonObject {
                    put("message", buildJsonObject {
                        put(
                            "content",
                            """{"name":"finish","arguments":{"summary":"done","revision":"42"}}"""
                        )
                    })
                })
            })
        }

        val decision = OpenAiCompatibleClient().parseScreenshotAgentDecision(response, frame)

        assertEquals("done", (decision as ScreenshotAgentDecision.Finish).summary)
    }

    @Test
    fun progressProtocolRequiresConcreteNextMilestoneForContinue() {
        val response = screenshotToolResponse(
            "assess_progress",
            """{"verdict":"CONTINUE","evidence":"screen changed"}"""
        )

        val result = runCatching { OpenAiCompatibleClient().parseProgressAssessment(response) }

        assertTrue(result.isFailure)
    }
}

private fun screenshotFrame() = ScreenshotObservationFrame(
    revision = 42,
    observationId = "observation",
    screenshot = byteArrayOf(1),
    screenshotMimeType = "image/png",
    deviceWidth = 1_080,
    deviceHeight = 1_920,
    foregroundApp = null,
    uiHint = null
)

private fun screenshotToolResponse(name: String, arguments: String) = buildJsonObject {
    put("choices", buildJsonArray {
        add(buildJsonObject {
            put("message", buildJsonObject {
                put("tool_calls", buildJsonArray {
                    add(buildJsonObject {
                        put("function", buildJsonObject {
                            put("name", name)
                            put("arguments", JsonPrimitive(arguments))
                        })
                    })
                })
            })
        })
    })
}

private suspend fun ScreenshotAgentEngine.runTask(): AgentTaskUiState = run(
    task = "完成测试任务",
    deviceId = "device",
    onState = {},
    confirmSensitiveAction = { true }
)

private class FakeScreenshotModel(
    private val decision: suspend (ScreenshotAgentRequest, Int) -> ScreenshotAgentDecision
) : ScreenshotAgentGateway {
    var decisionCalls = 0
    var progressCalls = 0

    override suspend fun decide(request: ScreenshotAgentRequest): ScreenshotAgentDecisionResult {
        decisionCalls += 1
        return ScreenshotAgentDecisionResult(decision(request, decisionCalls))
    }

    override suspend fun assessProgress(request: ScreenshotAgentRequest): ScreenshotProgressResult {
        progressCalls += 1
        return ScreenshotProgressResult(
            ProgressAssessment(
                verdict = ProgressVerdict.CONTINUE,
                evidence = "The latest screen is different and the previous action succeeded",
                nextMilestone = "Continue to the next visible screen"
            )
        )
    }
}

private class FakeScreenshotDevice(
    private val width: Int = 1_000,
    private val height: Int = 2_000,
    private val changeScreenshot: Boolean = true
) : AgentDeviceGateway {
    var observeCount = 0
    val executed = mutableListOf<AgentAction>()

    override suspend fun isConnected(deviceId: String): Boolean = true

    override suspend fun observe(deviceId: String): AgentObservation {
        observeCount += 1
        val marker = if (changeScreenshot) observeCount else 1
        return AgentObservation(
            screenshotPng = byteArrayOf(1, marker.toByte(), 3),
            uiHierarchy = "",
            currentActivity = "com.example/.MainActivity",
            screenWidth = width,
            screenHeight = height,
            observationId = "observation-$observeCount",
            revision = observeCount.toLong()
        )
    }

    override suspend fun execute(deviceId: String, action: AgentAction): AgentToolResult {
        executed += action
        return AgentToolResult(true, "ok")
    }
}
