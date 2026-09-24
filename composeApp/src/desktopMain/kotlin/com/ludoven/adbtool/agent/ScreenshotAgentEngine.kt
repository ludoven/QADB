package com.ludoven.adbtool.agent

import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

const val SCREENSHOT_AGENT_SOFT_ACTION_LIMIT = 20
const val SCREENSHOT_AGENT_HARD_ACTION_LIMIT = 40
const val SCREENSHOT_AGENT_PROGRESS_CHECK_INTERVAL = 5
const val SCREENSHOT_AGENT_MODEL_TIMEOUT_MS = 90_000L
const val SCREENSHOT_AGENT_DEVICE_TIMEOUT_MS = 30_000L

data class ScreenshotObservationFrame(
    val revision: Long,
    val observationId: String,
    val screenshot: ByteArray,
    val screenshotMimeType: String,
    val deviceWidth: Int,
    val deviceHeight: Int,
    val foregroundApp: String?,
    val uiHint: String?
)

data class ScreenshotAgentHistoryEntry(
    val actionNumber: Int,
    val action: String,
    val success: Boolean,
    val beforeFingerprint: String,
    val afterFingerprint: String,
    val result: String
) {
    val progressed: Boolean get() = beforeFingerprint != afterFingerprint
}

data class ScreenshotAgentRequest(
    val task: String,
    val frame: ScreenshotObservationFrame,
    val recentHistory: List<ScreenshotAgentHistoryEntry>,
    val protocolCorrection: String? = null
)

sealed interface ScreenshotAgentDecision {
    val revision: Long

    data class Execute(
        val action: AgentAction,
        override val revision: Long
    ) : ScreenshotAgentDecision

    data class Finish(
        val summary: String,
        override val revision: Long
    ) : ScreenshotAgentDecision

    data class AskUser(
        val question: String,
        override val revision: Long
    ) : ScreenshotAgentDecision

    data class Blocked(
        val reason: String,
        override val revision: Long
    ) : ScreenshotAgentDecision
}

data class ScreenshotAgentDecisionResult(
    val decision: ScreenshotAgentDecision,
    val usage: AgentUsage = AgentUsage(),
    val providerSnapshot: AgentModelProviderSnapshot? = null,
    val billing: AgentModelBilling? = null
)

enum class ProgressVerdict { CONTINUE, FINISH, BLOCKED }

data class ProgressAssessment(
    val verdict: ProgressVerdict,
    val evidence: String,
    val nextMilestone: String? = null
)

data class ScreenshotProgressResult(
    val assessment: ProgressAssessment,
    val usage: AgentUsage = AgentUsage(),
    val providerSnapshot: AgentModelProviderSnapshot? = null,
    val billing: AgentModelBilling? = null
)

interface ScreenshotAgentModelClient : ResolvedAgentModelClient {
    suspend fun decideScreenshotAction(
        provider: ResolvedAgentProvider,
        request: ScreenshotAgentRequest
    ): ScreenshotAgentDecisionResult

    suspend fun assessScreenshotProgress(
        provider: ResolvedAgentProvider,
        request: ScreenshotAgentRequest
    ): ScreenshotProgressResult
}

interface ScreenshotAgentGateway {
    suspend fun decide(request: ScreenshotAgentRequest): ScreenshotAgentDecisionResult
    suspend fun assessProgress(request: ScreenshotAgentRequest): ScreenshotProgressResult
}

class RoutedScreenshotAgentGateway(
    private val providers: AgentProviderRepository = AgentProviderRuntime.repository,
    private val client: ScreenshotAgentModelClient = OpenAiCompatibleClient()
) : ScreenshotAgentGateway {
    override suspend fun decide(request: ScreenshotAgentRequest): ScreenshotAgentDecisionResult {
        val provider = resolveVisualBrain()
        return client.decideScreenshotAction(provider, request)
    }

    override suspend fun assessProgress(request: ScreenshotAgentRequest): ScreenshotProgressResult {
        val provider = resolveVisualBrain()
        return client.assessScreenshotProgress(provider, request)
    }

    private suspend fun resolveVisualBrain(): ResolvedAgentProvider {
        providers.ensureMigration()
        val resolved = requireNotNull(providers.resolve(AgentModelRole.BRAIN)) {
            "No enabled BRAIN provider is configured"
        }
        if (resolved.profile.authType != AgentProviderAuthType.NONE) {
            require(!resolved.authSecret.isNullOrBlank()) { "API key is missing for ${resolved.profile.name}" }
        }
        val attestation = requireNotNull(providers.capabilityAttestation(resolved.profile)) {
            "Test the configured visual Agent provider in Settings before starting a task"
        }
        require(attestation.tier >= AgentCapabilityTier.L3_VISUAL_AGENT) {
            "The configured BRAIN provider must pass the L3 visual Agent capability test"
        }
        return resolved.copy(
            profile = resolved.profile.copy(
                limits = resolved.limits.copy(timeoutMs = SCREENSHOT_AGENT_MODEL_TIMEOUT_MS, maxRetries = 0)
            )
        )
    }
}

class ScreenshotAgentEngine(
    private val model: ScreenshotAgentGateway = RoutedScreenshotAgentGateway(),
    private val deviceGateway: AgentDeviceGateway = RealAgentDeviceGateway(),
    private val riskEvaluator: AgentRiskEvaluator = AgentRiskEvaluator(),
    private val softActionLimit: Int = SCREENSHOT_AGENT_SOFT_ACTION_LIMIT,
    private val hardActionLimit: Int = SCREENSHOT_AGENT_HARD_ACTION_LIMIT,
    private val progressCheckInterval: Int = SCREENSHOT_AGENT_PROGRESS_CHECK_INTERVAL,
    private val taskLogStoreProvider: () -> AgentTaskLogStore = { AgentTaskLogRuntime.store }
) : AgentTaskRunner {
    init {
        require(softActionLimit in 1 until hardActionLimit)
        require(hardActionLimit <= 100)
        require(progressCheckInterval in 1..softActionLimit)
    }

    override suspend fun run(
        task: String,
        deviceId: String,
        initialState: AgentTaskUiState,
        runId: String?,
        acceptedAtMs: Long?,
        firstFeedbackMs: Long?,
        onState: (AgentTaskUiState) -> Unit,
        confirmSensitiveAction: suspend (AgentStep) -> Boolean
    ): AgentTaskUiState {
        val activeRunId = runId?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
        val approvalPolicy = riskEvaluator.policySnapshot()
        var modelCalls = 0
        var usage = AgentUsage()
        var actionCount = 0
        val history = mutableListOf<ScreenshotAgentHistoryEntry>()
        val uncertainIrreversibleActions = mutableSetOf<String>()
        val taskLogStore = runCatching(taskLogStoreProvider).getOrElse { NoopAgentTaskLogStore }
        var taskLogStarted = false
        var state = initialState.copy(
            isRunning = true,
            needsUser = false,
            boundDeviceId = deviceId,
            pendingConfirmation = null,
            errorMessage = null,
            failure = null,
            phase = AgentRunPhase.OBSERVING,
            observationMode = AgentObservationMode.VISION,
            executionStrategy = AgentExecutionStrategy.INTERACTIVE,
            executionDetails = listOf("Screenshot-first single-agent loop started")
        )

        fun publish() {
            onState(state)
            runCatching {
                if (!taskLogStarted) {
                    taskLogStore.start(activeRunId, task, state)
                    taskLogStarted = true
                }
                taskLogStore.record(activeRunId, state)
            }
        }
        fun updateBudget() {
            state = state.copy(
                usage = usage,
                budgetStatus = AgentBudgetStatus(
                    usage = usage,
                    modelCalls = modelCalls,
                    visionCalls = modelCalls,
                    // The screenshot engine caps device actions instead of model calls;
                    // zero limits keep the public metrics display in "usage" mode.
                    modelCallLimit = 0,
                    visionCallLimit = 0,
                    replanLimit = 0,
                    deviceActions = actionCount,
                    deviceActionLimit = hardActionLimit
                )
            )
        }
        fun fail(message: String): AgentTaskUiState {
            state = state.copy(
                isRunning = false,
                needsUser = false,
                pendingConfirmation = null,
                phase = AgentRunPhase.FAILED,
                errorMessage = message,
                failure = agentFailureFrom(message)
            )
            publish()
            return state
        }
        fun needsUser(message: String): AgentTaskUiState {
            state = state.copy(
                isRunning = false,
                needsUser = true,
                pendingConfirmation = null,
                phase = AgentRunPhase.COMPLETED,
                errorMessage = null,
                failure = null,
                messages = state.messages + AgentMessage(
                    id = UUID.randomUUID().toString(),
                    role = AgentMessageRole.ASSISTANT,
                    text = message,
                    runId = activeRunId
                ),
                executionDetails = (state.executionDetails + "needs_user: $message").takeLast(40)
            )
            publish()
            return state
        }
        fun complete(message: String): AgentTaskUiState {
            state = state.copy(
                isRunning = false,
                needsUser = false,
                pendingConfirmation = null,
                phase = AgentRunPhase.COMPLETED,
                messages = state.messages + AgentMessage(
                    id = UUID.randomUUID().toString(),
                    role = AgentMessageRole.ASSISTANT,
                    text = message,
                    runId = activeRunId
                )
            )
            publish()
            return state
        }

        try {
            publish()
            if (deviceId.isBlank() || !withTimeout(SCREENSHOT_AGENT_DEVICE_TIMEOUT_MS) {
                    deviceGateway.isConnected(deviceId)
                }
            ) {
                return fail("The selected device is no longer connected")
            }

            var observation = observeFresh(deviceId)
            val initialForegroundPkg = observation.currentActivity?.substringBefore('/')?.takeIf { '/' in observation.currentActivity && it.isNotBlank() }
            val initialAuthorized = if (state.authorizedPackages.isNotEmpty()) {
                state.authorizedPackages
            } else if (initialForegroundPkg != null && initialForegroundPkg !in SYSTEM_EXEMPT_PACKAGES && !initialForegroundPkg.contains("launcher", ignoreCase = true)) {
                setOf(initialForegroundPkg)
            } else {
                emptySet()
            }
            state = state.copy(authorizedPackages = initialAuthorized)
            while (actionCount < hardActionLimit) {
                val frame = observation.toScreenshotFrame()
                state = state.copy(
                    phase = AgentRunPhase.THINKING,
                    latestScreenshot = frame.screenshot,
                    observationMode = AgentObservationMode.VISION,
                    deviceState = PageSignatureEngine.state(observation),
                    deviceChannel = observation.source,
                    observationTimings = observation.timings,
                    executionDetails = (state.executionDetails +
                        "Observed revision ${frame.revision}; action $actionCount/$hardActionLimit").takeLast(40)
                )
                publish()

                if (actionCount >= softActionLimit &&
                    (actionCount - softActionLimit) % progressCheckInterval == 0
                ) {
                    val window = history.takeLast(progressCheckInterval)
                    if (!hasLocalProgress(window)) {
                        return needsUser(
                            "任务在第 $actionCount 步后没有检测到持续进展，已停止以避免重复操作。"
                        )
                    }
                    val progressResult = assessProgressWithSingleProtocolRetry(
                        ScreenshotAgentRequest(task, frame, window)
                    )
                    val progress = progressResult.first
                    modelCalls += progressResult.second
                    usage += progress.usage
                    state = state.copy(lastRequestUsage = progress.usage)
                    updateBudget()
                    when (progress.assessment.verdict) {
                        ProgressVerdict.FINISH -> return complete(progress.assessment.evidence)
                        ProgressVerdict.BLOCKED -> return needsUser(progress.assessment.evidence)
                        ProgressVerdict.CONTINUE -> {
                            val milestone = progress.assessment.nextMilestone.orEmpty()
                            if (progress.assessment.evidence.isBlank() || milestone.isBlank()) {
                                return needsUser("进度检查没有给出可验证证据或下一目标，已停止。")
                            }
                            state = state.copy(
                                executionDetails = (state.executionDetails +
                                    "Progress check passed: ${progress.assessment.evidence.take(160)}; next=${milestone.take(120)}")
                                    .takeLast(40)
                            )
                            publish()
                        }
                    }
                }

                val request = ScreenshotAgentRequest(task, frame, history.takeLast(8))
                val result = decideWithSingleProtocolRetry(request)
                modelCalls += result.second
                usage += result.first.usage
                state = state.copy(lastRequestUsage = result.first.usage)
                updateBudget()
                val decision = result.first.decision
                if (decision.revision != frame.revision) {
                    return fail("The model returned an action for a stale screenshot revision")
                }
                when (decision) {
                    is ScreenshotAgentDecision.Finish -> return complete(decision.summary)
                    is ScreenshotAgentDecision.AskUser -> return needsUser(decision.question)
                    is ScreenshotAgentDecision.Blocked -> return fail("Task blocked: ${decision.reason}")
                    is ScreenshotAgentDecision.Execute -> Unit
                }

                val normalizedAction = (decision as ScreenshotAgentDecision.Execute).action
                var action = normalizedAction.fromPermille(frame)
                validateScreenshotAgentAction(action, observation).getOrElse {
                    return fail("Model protocol error: ${it.message}")
                }
                val irreversibleKey = action.irreversibleKey()
                if (irreversibleKey != null && irreversibleKey in uncertainIrreversibleActions) {
                    return needsUser("上一次不可逆操作的结果不明确，未自动重复执行。")
                }

                val capabilityReason = withTimeout(SCREENSHOT_AGENT_DEVICE_TIMEOUT_MS) {
                    deviceGateway.confirmationRequirement(deviceId, action, observation)
                }
                val localSafetyReason = capabilityReason ?: when (action.meta.operationKind) {
                    AgentOperationKind.SEND -> "Confirm sending the requested content"
                    else -> null
                }
                val risk = riskEvaluator.evaluate(
                    action = action,
                    observation = observation,
                    localCapabilityReason = localSafetyReason,
                    policy = approvalPolicy,
                    authorizedPackages = state.authorizedPackages
                )
                if (risk.level == AgentRiskLevel.BLOCKED) return fail("Task blocked: ${risk.reason}")
                var step = AgentStep(
                    id = UUID.randomUUID().toString(),
                    action = action,
                    status = if (risk.level == AgentRiskLevel.CONFIRMATION_REQUIRED) {
                        AgentStepStatus.AWAITING_CONFIRMATION
                    } else {
                        AgentStepStatus.RUNNING
                    },
                    riskLevel = risk.level,
                    confirmationReason = risk.reason,
                    containsSensitiveData = action is AgentAction.InputText
                )
                state = state.copy(
                    steps = state.steps + step,
                    pendingConfirmation = step.takeIf { risk.level == AgentRiskLevel.CONFIRMATION_REQUIRED },
                    phase = if (risk.level == AgentRiskLevel.CONFIRMATION_REQUIRED) {
                        AgentRunPhase.AWAITING_CONFIRMATION
                    } else {
                        AgentRunPhase.EXECUTING
                    }
                )
                publish()
                if (risk.level == AgentRiskLevel.CONFIRMATION_REQUIRED) {
                    if (!confirmSensitiveAction(step)) return needsUser("用户取消了需要确认的操作。")
                    val targetPkg = when (action) {
                        is AgentAction.LaunchPackage -> action.packageName
                        is AgentAction.ForceStopPackage -> action.packageName
                        is AgentAction.ClearAppData -> action.packageName
                        is AgentAction.UninstallPackage -> action.packageName
                        else -> observation.currentActivity?.substringBefore('/')?.takeIf { '/' in observation.currentActivity && it.isNotBlank() }
                    }
                    if (targetPkg != null && targetPkg !in SYSTEM_EXEMPT_PACKAGES && !targetPkg.contains("launcher", ignoreCase = true)) {
                        state = state.copy(authorizedPackages = state.authorizedPackages + targetPkg)
                    }
                    val refreshed = runCatching { observeFresh(deviceId) }.getOrElse {
                        return needsUser("确认后无法重新观察设备，未执行该操作。")
                    }
                    if (refreshed.visualFingerprint() != observation.visualFingerprint() ||
                        refreshed.uiHierarchy != observation.uiHierarchy ||
                        refreshed.screenWidth != observation.screenWidth ||
                        refreshed.screenHeight != observation.screenHeight
                    ) {
                        return needsUser("确认期间设备画面已变化，原授权失效；请重新提交目标。")
                    }
                    action = when (action) {
                        is AgentAction.Tap -> action.copy(observationId = refreshed.observationId)
                        is AgentAction.InputText -> action.copy(observationId = refreshed.observationId)
                        else -> action
                    }
                    validateScreenshotAgentAction(action, refreshed).getOrElse {
                        return needsUser("确认后的操作目标已失效，未执行该操作。")
                    }
                    observation = refreshed
                    step = step.copy(action = action, status = AgentStepStatus.RUNNING)
                    state = state.replaceScreenshotStep(step).copy(
                        pendingConfirmation = null,
                        phase = AgentRunPhase.EXECUTING
                    )
                    publish()
                }

                val beforeFingerprint = observation.visualFingerprint()
                step = step.copy(executedActionCount = 1)
                state = state.replaceScreenshotStep(step)
                val toolResult = withTimeout(SCREENSHOT_AGENT_DEVICE_TIMEOUT_MS) {
                    deviceGateway.execute(deviceId, action)
                }
                if (toolResult.success) {
                    val launchedPkg = when (action) {
                        is AgentAction.LaunchPackage -> action.packageName
                        is AgentAction.OpenApp -> toolResult.resolvedPackages.firstOrNull()
                        else -> null
                    }
                    if (launchedPkg != null && launchedPkg !in SYSTEM_EXEMPT_PACKAGES && !launchedPkg.contains("launcher", ignoreCase = true)) {
                        state = state.copy(authorizedPackages = state.authorizedPackages + launchedPkg)
                    }
                }
                actionCount += 1
                state = state.copy(phase = AgentRunPhase.OBSERVING)
                publish()
                val postObservation = runCatching { observeFresh(deviceId) }.getOrElse { error ->
                    if (irreversibleKey != null) uncertainIrreversibleActions += irreversibleKey
                    return needsUser(
                        "操作已执行，但无法获取新的设备截图确认结果：${error.message ?: "unknown observation error"}"
                    )
                }
                val afterFingerprint = postObservation.visualFingerprint()
                val progressed = beforeFingerprint != afterFingerprint
                val finalStatus = when {
                    !toolResult.success -> AgentStepStatus.FAILED
                    progressed || action is AgentAction.Wait -> AgentStepStatus.COMPLETED
                    else -> AgentStepStatus.UNVERIFIED
                }
                if (irreversibleKey != null && (!toolResult.success || !progressed)) {
                    uncertainIrreversibleActions += irreversibleKey
                }
                step = step.copy(
                    status = finalStatus,
                    result = toolResult.output.take(500)
                )
                state = state.replaceScreenshotStep(step).copy(
                    latestScreenshot = postObservation.screenshotPng,
                    deviceState = PageSignatureEngine.state(postObservation),
                    pageDiff = PageSignatureEngine.diff(
                        PageSignatureEngine.state(observation),
                        PageSignatureEngine.state(postObservation)
                    ),
                    phase = AgentRunPhase.THINKING,
                    executionDetails = (state.executionDetails +
                        "${action.toolName}: ${finalStatus.name.lowercase()}; changed=$progressed").takeLast(40)
                )
                history += ScreenshotAgentHistoryEntry(
                    actionNumber = actionCount,
                    action = action.safeHistoryLabel(),
                    success = toolResult.success,
                    beforeFingerprint = beforeFingerprint,
                    afterFingerprint = afterFingerprint,
                    result = toolResult.output.take(160)
                )
                observation = postObservation
                publish()

                if (history.hasRepeatedNoProgressLoop()) {
                    return needsUser("检测到相同页面上的重复操作，已停止以避免循环。")
                }
            }
            return needsUser(
                "已达到 $hardActionLimit 步硬限制并强制停止。当前画面已保留，可由用户确认后继续新任务。"
            )
        } catch (cancelled: CancellationException) {
            state = state.copy(
                isRunning = false,
                needsUser = false,
                pendingConfirmation = null,
                phase = AgentRunPhase.CANCELLED
            )
            publish()
            throw cancelled
        } catch (error: Exception) {
            return fail(error.message ?: "Agent execution failed")
        }
    }

    private suspend fun decideWithSingleProtocolRetry(
        request: ScreenshotAgentRequest
    ): Pair<ScreenshotAgentDecisionResult, Int> {
        return try {
            withTimeout(SCREENSHOT_AGENT_MODEL_TIMEOUT_MS) { model.decide(request) } to 1
        } catch (issue: ModelProtocolIssueException) {
            val corrected = request.copy(
                protocolCorrection = "Return exactly one allowed tool with every required field. ${issue.issue.message}"
            )
            withTimeout(SCREENSHOT_AGENT_MODEL_TIMEOUT_MS) { model.decide(corrected) } to 2
        }
    }

    private suspend fun assessProgressWithSingleProtocolRetry(
        request: ScreenshotAgentRequest
    ): Pair<ScreenshotProgressResult, Int> {
        return try {
            withTimeout(SCREENSHOT_AGENT_MODEL_TIMEOUT_MS) { model.assessProgress(request) } to 1
        } catch (issue: ModelProtocolIssueException) {
            val corrected = request.copy(
                protocolCorrection = "Return assess_progress exactly once with a valid verdict and evidence. ${issue.issue.message}"
            )
            withTimeout(SCREENSHOT_AGENT_MODEL_TIMEOUT_MS) { model.assessProgress(corrected) } to 2
        }
    }

    private suspend fun observeFresh(deviceId: String): AgentObservation =
        withTimeout(SCREENSHOT_AGENT_DEVICE_TIMEOUT_MS) { deviceGateway.observe(deviceId) }
            .also { require(it.screenshotPng?.isNotEmpty() == true) { "A fresh screenshot is required for every Agent decision" } }
}

private fun AgentObservation.toScreenshotFrame(): ScreenshotObservationFrame = ScreenshotObservationFrame(
    revision = revision,
    observationId = observationId,
    screenshot = requireNotNull(screenshotPng),
    screenshotMimeType = screenshotMimeType,
    deviceWidth = screenWidth,
    deviceHeight = screenHeight,
    foregroundApp = currentActivity.takeIf(String::isNotBlank),
    uiHint = buildUiHint()
)

private fun AgentObservation.buildUiHint(): String? {
    if (uiNodes.isEmpty()) return null
    return uiNodes.asSequence()
        .filter { it.enabled && !it.password }
        .filter { it.clickable || it.editable || it.text.isNotBlank() || it.contentDescription.isNotBlank() }
        .take(80)
        .joinToString("\n") { node ->
            val label = node.text.ifBlank { node.contentDescription }.replace(Regex("[\\r\\n\\t]+"), " ").take(100)
            "id=${node.elementId} role=${node.role.ifBlank { node.className.substringAfterLast('.') }} " +
                "label=${label.ifBlank { "<none>" }} clickable=${node.clickable} editable=${node.editable} " +
                "bounds=${node.bounds.toPermille(screenWidth, screenHeight)}"
        }
        .takeIf(String::isNotBlank)
}

private fun UiBounds.toPermille(width: Int, height: Int): String =
    "[${left.scaleToPermille(width)},${top.scaleToPermille(height)}," +
        "${right.scaleToPermille(width)},${bottom.scaleToPermille(height)}]"

private fun Int.scaleToPermille(total: Int): Int =
    if (total <= 0) 0 else ((toLong() * 1_000L) / total).toInt().coerceIn(0, 1_000)

private fun Int.scaleFromPermille(total: Int): Int =
    ((coerceIn(0, 1_000).toLong() * total.coerceAtLeast(1)) / 1_000L)
        .toInt().coerceIn(0, (total - 1).coerceAtLeast(0))

private fun AgentAction.fromPermille(frame: ScreenshotObservationFrame): AgentAction = when (this) {
    is AgentAction.Tap -> copy(
        x = x.scaleFromPermille(frame.deviceWidth),
        y = y.scaleFromPermille(frame.deviceHeight),
        observationId = frame.observationId
    )
    is AgentAction.Swipe -> copy(
        startX = startX.scaleFromPermille(frame.deviceWidth),
        startY = startY.scaleFromPermille(frame.deviceHeight),
        endX = endX.scaleFromPermille(frame.deviceWidth),
        endY = endY.scaleFromPermille(frame.deviceHeight)
    )
    is AgentAction.InputText -> copy(observationId = frame.observationId)
    else -> this
}

internal fun validateScreenshotAgentAction(
    action: AgentAction,
    observation: AgentObservation
): Result<Unit> = runCatching {
    require(action is AgentAction.OpenApp || action is AgentAction.Tap || action is AgentAction.InputText ||
        action is AgentAction.Swipe || action is AgentAction.KeyEvent || action is AgentAction.Wait) {
        "Unsupported screenshot Agent action: ${action.toolName}"
    }
    if (action is AgentAction.InputText && action.elementId == null) {
        require(action.observationId == observation.observationId) { "Input requires the latest observation" }
        require(action.text.isNotBlank() && action.text.length <= 2_000) { "Input text is invalid" }
        require(observation.uiNodes.none { it.password }) {
            "Untargeted text input is blocked while a password field is visible"
        }
    } else {
        validateAgentAction(action, observation).getOrThrow()
    }
}

private fun AgentObservation.visualFingerprint(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(currentActivity.toByteArray())
    screenshotPng?.let(digest::update)
    return digest.digest().take(10).joinToString("") { "%02x".format(it) }
}

private fun AgentTaskUiState.replaceScreenshotStep(updated: AgentStep): AgentTaskUiState =
    copy(steps = steps.map { if (it.id == updated.id) updated else it })

private fun hasLocalProgress(history: List<ScreenshotAgentHistoryEntry>): Boolean =
    history.isNotEmpty() && history.any(ScreenshotAgentHistoryEntry::success) &&
        history.any(ScreenshotAgentHistoryEntry::progressed) &&
        !history.hasRepeatedNoProgressLoop()

private fun List<ScreenshotAgentHistoryEntry>.hasRepeatedNoProgressLoop(): Boolean {
    val tail = takeLast(3)
    return tail.size == 3 && tail.all { !it.progressed } && tail.map { it.action }.distinct().size == 1
}

private fun AgentAction.safeHistoryLabel(): String = when (this) {
    is AgentAction.OpenApp -> "open_app:${query.take(40)}"
    is AgentAction.Tap -> "tap:${x / 20}:${y / 20}:${meta.operationKind}"
    is AgentAction.InputText -> "type_text:${elementId ?: "focused"}"
    is AgentAction.Swipe -> "swipe:${startX / 50}:${startY / 50}:${endX / 50}:${endY / 50}"
    is AgentAction.KeyEvent -> "key:${key.name}"
    is AgentAction.Wait -> "wait:${durationMs}"
    else -> toolName
}

private fun AgentAction.irreversibleKey(): String? = when {
    meta.operationKind in setOf(
        AgentOperationKind.SEND,
        AgentOperationKind.DELETE,
        AgentOperationKind.PURCHASE,
        AgentOperationKind.ACCOUNT
    ) -> safeHistoryLabel()
    else -> null
}
