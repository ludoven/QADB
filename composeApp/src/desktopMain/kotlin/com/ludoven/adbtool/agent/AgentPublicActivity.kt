package com.ludoven.adbtool.agent

import com.ludoven.adbtool.agent.artemis.QadbBridgeException

/**
 * Public, presentation-safe Agent activity. This intentionally carries no model prompts,
 * reasoning text, UI hierarchy, element identifiers, coordinates, packages, or typed text.
 */
data class AgentPublicEvent(
    val runId: String,
    val sequence: Long,
    val occurredAtMs: Long = System.currentTimeMillis(),
    val payload: AgentPublicEventPayload
)

sealed interface AgentPublicEventPayload {
    data object RunStarted : AgentPublicEventPayload
    data class StageChanged(val stage: AgentPublicStage) : AgentPublicEventPayload
    data class ToolStarted(val tool: AgentPublicToolSummary) : AgentPublicEventPayload
    data class ToolFinished(
        val tool: AgentPublicToolSummary,
        val result: AgentPublicToolResult
    ) : AgentPublicEventPayload

    data class ConfirmationRequested(val tool: AgentPublicToolSummary) : AgentPublicEventPayload
    data class ResponseDelta(val delta: String) : AgentPublicEventPayload
    data object ResponseCompleted : AgentPublicEventPayload
    data class Retrying(val attempt: Int? = null) : AgentPublicEventPayload
    data class MetricsUpdated(val metrics: AgentPublicMetrics) : AgentPublicEventPayload
    data class Failed(val failure: AgentFailure) : AgentPublicEventPayload
    data object Cancelling : AgentPublicEventPayload
    data object Cancelled : AgentPublicEventPayload
    data object Completed : AgentPublicEventPayload
}

enum class AgentPublicStage {
    UNDERSTANDING,
    READING,
    PLANNING,
    EXECUTING,
    VERIFYING,
    RECOVERING,
    WAITING_CONFIRMATION,
    RESPONDING,
    CANCELLING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class AgentPublicRunStatus {
    RUNNING,
    WAITING_CONFIRMATION,
    CANCELLING,
    COMPLETED,
    FAILED,
    CANCELLED;

    val isTerminal: Boolean
        get() = this == COMPLETED || this == FAILED || this == CANCELLED
}

enum class AgentPublicToolKind {
    SEMANTIC_GOAL,
    OBSERVE_DEVICE,
    FIND_APP,
    OPEN_APP,
    TAP,
    SWIPE,
    INPUT_TEXT,
    KEY_EVENT,
    WAIT,
    FINISH,
    FORCE_STOP_APP,
    CLEAR_APP_DATA,
    UNINSTALL_APP,
    REBOOT_DEVICE
}

data class AgentPublicToolSummary(
    val kind: AgentPublicToolKind,
    /** The only public input detail: character count, never the text itself. */
    val inputCharacterCount: Int? = null
)

enum class AgentPublicToolResult {
    SUCCEEDED,
    RECOVERED,
    UNVERIFIED,
    FAILED,
    DENIED
}

enum class AgentFailureCategory {
    PROTOCOL,
    NETWORK,
    RATE_LIMIT,
    AUTHENTICATION,
    DEVICE_DISCONNECTED,
    DEVICE_EXECUTION,
    OBSERVATION,
    VERIFICATION,
    SAFETY_BLOCKED,
    BUDGET_EXHAUSTED,
    UNKNOWN
}

enum class AgentFailureSubsystem {
    PROVIDER,
    DEVICE_TRANSPORT,
    OBSERVATION,
    DEVICE_ACTION,
    VERIFICATION,
    SAFETY_POLICY,
    ORCHESTRATOR,
    UNKNOWN
}

enum class AgentFailureStage {
    CONFIGURATION,
    UNDERSTANDING,
    OBSERVING,
    PLANNING,
    EXECUTING,
    VERIFYING,
    RECOVERING,
    UNKNOWN
}

enum class AgentFailureCode {
    ENGINE_UNAVAILABLE,
    PROTOCOL_INVALID,
    NETWORK_UNAVAILABLE,
    RATE_LIMITED,
    AUTHENTICATION_FAILED,
    DEVICE_DISCONNECTED,
    DEVICE_EXECUTION_FAILED,
    OBSERVATION_FAILED,
    VERIFICATION_FAILED,
    OUTCOME_UNCERTAIN,
    OPERATION_TIMED_OUT,
    MODEL_CALL_TIMED_OUT,
    SAFETY_BLOCKED,
    USER_DENIED,
    BUDGET_EXHAUSTED,
    UNKNOWN
}

enum class AgentFailureAction {
    RETRY,
    OPEN_MODEL_SETTINGS,
    OPEN_DEVICES,
    NONE
}

data class AgentFailure(
    val category: AgentFailureCategory,
    val retryable: Boolean,
    val suggestedAction: AgentFailureAction,
    val code: AgentFailureCode = AgentFailureCode.UNKNOWN,
    val subsystem: AgentFailureSubsystem = category.defaultFailureSubsystem(),
    val stage: AgentFailureStage = category.defaultFailureStage(),
    /** Sanitized and capped diagnostic text. It is never shown in the collapsed activity card. */
    val technicalDetail: String? = null
)

data class AgentPublicMetrics(
    val engineVersion: AgentEngineVersion = AgentEngineVersion.SCREENSHOT,
    val modelCalls: Int = 0,
    val modelCallLimit: Int = 0,
    val visionCalls: Int = 0,
    val visionCallLimit: Int = 0,
    val replans: Int = 0,
    val replanLimit: Int = 0,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val cachedTokens: Int = 0,
    val totalTokens: Int = 0,
    val estimatedCost: Double = 0.0,
    val deviceActions: Int = 0,
    val deviceActionLimit: Int = 0
)

data class AgentPublicActivityItem(
    val sequence: Long,
    val occurredAtMs: Long,
    val stage: AgentPublicStage,
    val tool: AgentPublicToolSummary? = null,
    val result: AgentPublicToolResult? = null
)

data class AgentRunPresentation(
    val runId: String,
    val lastSequence: Long,
    val startedAtMs: Long,
    val finishedAtMs: Long? = null,
    val stage: AgentPublicStage = AgentPublicStage.UNDERSTANDING,
    val status: AgentPublicRunStatus = AgentPublicRunStatus.RUNNING,
    val activities: List<AgentPublicActivityItem> = emptyList(),
    val latestTool: AgentPublicToolSummary? = null,
    val responseText: String = "",
    val responseCompleted: Boolean = false,
    val metrics: AgentPublicMetrics = AgentPublicMetrics(),
    val failure: AgentFailure? = null,
    val preferredExpanded: Boolean = false
)

data class AgentPublicActivityState(
    val activeRunId: String? = null,
    val runs: Map<String, AgentRunPresentation> = emptyMap()
) {
    val activeRun: AgentRunPresentation?
        get() = activeRunId?.let(runs::get)
}

object AgentPublicActivityReducer {
    private const val MAX_ACTIVITIES = 30

    fun reduce(
        state: AgentPublicActivityState,
        event: AgentPublicEvent
    ): AgentPublicActivityState {
        val payload = event.payload
        if (payload is AgentPublicEventPayload.RunStarted) {
            if (state.runs.containsKey(event.runId)) return state
            val run = AgentRunPresentation(
                runId = event.runId,
                lastSequence = event.sequence,
                startedAtMs = event.occurredAtMs,
                activities = listOf(
                    AgentPublicActivityItem(
                        sequence = event.sequence,
                        occurredAtMs = event.occurredAtMs,
                        stage = AgentPublicStage.UNDERSTANDING
                    )
                )
            )
            return state.copy(
                activeRunId = event.runId,
                runs = state.runs + (event.runId to run)
            )
        }

        if (state.activeRunId != event.runId) return state
        val current = state.runs[event.runId] ?: return state
        if (current.status.isTerminal || event.sequence <= current.lastSequence) return state
        if (
            current.status == AgentPublicRunStatus.CANCELLING &&
            payload !is AgentPublicEventPayload.Cancelled &&
            payload !is AgentPublicEventPayload.Failed &&
            payload !is AgentPublicEventPayload.Completed
        ) {
            return state
        }

        val updated = when (payload) {
            is AgentPublicEventPayload.StageChanged -> current.withStageEvent(event, payload.stage)
            is AgentPublicEventPayload.ToolStarted -> current.withToolEvent(
                event = event,
                tool = payload.tool,
                result = null,
                stage = AgentPublicStage.EXECUTING
            )
            is AgentPublicEventPayload.ToolFinished -> current.withToolEvent(
                event = event,
                tool = payload.tool,
                result = payload.result,
                stage = if (payload.result == AgentPublicToolResult.FAILED) {
                    AgentPublicStage.RECOVERING
                } else {
                    AgentPublicStage.VERIFYING
                }
            )
            is AgentPublicEventPayload.ConfirmationRequested -> current.withToolEvent(
                event = event,
                tool = payload.tool,
                result = null,
                stage = AgentPublicStage.WAITING_CONFIRMATION
            ).copy(
                status = AgentPublicRunStatus.WAITING_CONFIRMATION,
                preferredExpanded = true
            )
            is AgentPublicEventPayload.ResponseDelta -> current.copy(
                lastSequence = event.sequence,
                stage = AgentPublicStage.RESPONDING,
                status = AgentPublicRunStatus.RUNNING,
                responseText = current.responseText + payload.delta
            )
            AgentPublicEventPayload.ResponseCompleted -> current.copy(
                lastSequence = event.sequence,
                stage = AgentPublicStage.RESPONDING,
                responseCompleted = true
            )
            is AgentPublicEventPayload.Retrying -> current.withStageEvent(
                event,
                AgentPublicStage.RECOVERING
            )
            is AgentPublicEventPayload.MetricsUpdated -> current.copy(
                lastSequence = event.sequence,
                metrics = payload.metrics
            )
            is AgentPublicEventPayload.Failed -> current.withStageEvent(
                event,
                AgentPublicStage.FAILED
            ).copy(
                finishedAtMs = event.occurredAtMs,
                status = AgentPublicRunStatus.FAILED,
                failure = payload.failure,
                preferredExpanded = true
            )
            AgentPublicEventPayload.Cancelling -> current.withStageEvent(
                event,
                AgentPublicStage.CANCELLING
            ).copy(
                status = AgentPublicRunStatus.CANCELLING
            )
            AgentPublicEventPayload.Cancelled -> current.withStageEvent(
                event,
                AgentPublicStage.CANCELLED
            ).copy(
                finishedAtMs = event.occurredAtMs,
                status = AgentPublicRunStatus.CANCELLED,
                preferredExpanded = false
            )
            AgentPublicEventPayload.Completed -> current.withStageEvent(
                event,
                AgentPublicStage.COMPLETED
            ).copy(
                finishedAtMs = event.occurredAtMs,
                status = AgentPublicRunStatus.COMPLETED,
                preferredExpanded = false
            )
            AgentPublicEventPayload.RunStarted -> current
        }

        return state.copy(runs = state.runs + (event.runId to updated))
    }

    private fun AgentRunPresentation.withStageEvent(
        event: AgentPublicEvent,
        nextStage: AgentPublicStage
    ): AgentRunPresentation = copy(
        lastSequence = event.sequence,
        stage = nextStage,
        status = when (nextStage) {
            AgentPublicStage.WAITING_CONFIRMATION -> AgentPublicRunStatus.WAITING_CONFIRMATION
            AgentPublicStage.CANCELLING -> AgentPublicRunStatus.CANCELLING
            else -> if (status.isTerminal) status else AgentPublicRunStatus.RUNNING
        },
        activities = appendActivity(
            AgentPublicActivityItem(event.sequence, event.occurredAtMs, nextStage)
        )
    )

    private fun AgentRunPresentation.withToolEvent(
        event: AgentPublicEvent,
        tool: AgentPublicToolSummary,
        result: AgentPublicToolResult?,
        stage: AgentPublicStage
    ): AgentRunPresentation = copy(
        lastSequence = event.sequence,
        stage = stage,
        status = AgentPublicRunStatus.RUNNING,
        latestTool = tool,
        activities = appendActivity(
            AgentPublicActivityItem(
                sequence = event.sequence,
                occurredAtMs = event.occurredAtMs,
                stage = stage,
                tool = tool,
                result = result
            )
        )
    )

    private fun AgentRunPresentation.appendActivity(item: AgentPublicActivityItem): List<AgentPublicActivityItem> =
        (activities + item).takeLast(MAX_ACTIVITIES)
}

class AgentPublicEventSequencer(
    val runId: String,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private var sequence: Long = 0

    fun next(payload: AgentPublicEventPayload): AgentPublicEvent = AgentPublicEvent(
        runId = runId,
        sequence = ++sequence,
        occurredAtMs = clock(),
        payload = payload
    )
}

fun AgentAction.toPublicToolSummary(): AgentPublicToolSummary = when (this) {
    AgentAction.Observe -> AgentPublicToolSummary(AgentPublicToolKind.OBSERVE_DEVICE)
    is AgentAction.FindApp -> AgentPublicToolSummary(AgentPublicToolKind.FIND_APP)
    is AgentAction.OpenApp -> AgentPublicToolSummary(AgentPublicToolKind.OPEN_APP)
    is AgentAction.LaunchPackage -> AgentPublicToolSummary(AgentPublicToolKind.OPEN_APP)
    is AgentAction.Tap, is AgentAction.TapElement -> AgentPublicToolSummary(AgentPublicToolKind.TAP)
    is AgentAction.Swipe -> AgentPublicToolSummary(AgentPublicToolKind.SWIPE)
    is AgentAction.InputText -> AgentPublicToolSummary(
        AgentPublicToolKind.INPUT_TEXT,
        inputCharacterCount = text.length
    )
    is AgentAction.KeyEvent -> AgentPublicToolSummary(AgentPublicToolKind.KEY_EVENT)
    is AgentAction.Wait -> AgentPublicToolSummary(AgentPublicToolKind.WAIT)
    is AgentAction.Finish -> AgentPublicToolSummary(AgentPublicToolKind.FINISH)
    is AgentAction.ForceStopPackage -> AgentPublicToolSummary(AgentPublicToolKind.FORCE_STOP_APP)
    is AgentAction.ClearAppData -> AgentPublicToolSummary(AgentPublicToolKind.CLEAR_APP_DATA)
    is AgentAction.UninstallPackage -> AgentPublicToolSummary(AgentPublicToolKind.UNINSTALL_APP)
    is AgentAction.ExternalApproval -> AgentPublicToolSummary(AgentPublicToolKind.SEMANTIC_GOAL)
    AgentAction.RebootDevice -> AgentPublicToolSummary(AgentPublicToolKind.REBOOT_DEVICE)
}

fun AgentStepStatus.toPublicToolResult(): AgentPublicToolResult? = when (this) {
    AgentStepStatus.COMPLETED -> AgentPublicToolResult.SUCCEEDED
    AgentStepStatus.RECOVERED -> AgentPublicToolResult.RECOVERED
    AgentStepStatus.UNVERIFIED -> AgentPublicToolResult.UNVERIFIED
    AgentStepStatus.FAILED -> AgentPublicToolResult.FAILED
    AgentStepStatus.DENIED -> AgentPublicToolResult.DENIED
    AgentStepStatus.WAITING,
    AgentStepStatus.RUNNING,
    AgentStepStatus.AWAITING_CONFIRMATION -> null
}

fun AgentBudgetStatus.toPublicMetrics(
    engineVersion: AgentEngineVersion = AgentEngineVersion.SCREENSHOT
): AgentPublicMetrics = AgentPublicMetrics(
    engineVersion = engineVersion,
    modelCalls = modelCalls,
    modelCallLimit = modelCallLimit,
    visionCalls = visionCalls,
    visionCallLimit = visionCallLimit,
    replans = replans,
    replanLimit = replanLimit,
    promptTokens = usage.promptTokens,
    completionTokens = usage.completionTokens,
    cachedTokens = usage.cachedTokens,
    totalTokens = usage.totalTokens,
    estimatedCost = estimatedCost,
    deviceActions = deviceActions,
    deviceActionLimit = deviceActionLimit
)

fun AgentRunPhase.toPublicStage(): AgentPublicStage = when (this) {
    AgentRunPhase.IDLE -> AgentPublicStage.UNDERSTANDING
    AgentRunPhase.OBSERVING -> AgentPublicStage.READING
    AgentRunPhase.THINKING -> AgentPublicStage.PLANNING
    AgentRunPhase.RETRYING -> AgentPublicStage.RECOVERING
    AgentRunPhase.EXECUTING -> AgentPublicStage.EXECUTING
    AgentRunPhase.VERIFYING -> AgentPublicStage.VERIFYING
    AgentRunPhase.AWAITING_CONFIRMATION -> AgentPublicStage.WAITING_CONFIRMATION
    AgentRunPhase.COMPLETED -> AgentPublicStage.COMPLETED
    AgentRunPhase.FAILED -> AgentPublicStage.FAILED
    AgentRunPhase.CANCELLED -> AgentPublicStage.CANCELLED
}

fun agentFailureFrom(message: String?): AgentFailure {
    val source = message.orEmpty()
    val normalized = source.lowercase()
    val category = when {
        normalized.contains("qadb bridge") -> AgentFailureCategory.PROTOCOL
        source.contains(AGENT_OPERATION_TIMEOUT_MESSAGE) ||
            normalized.contains("operation deadline") || normalized.contains("operation timed out") ->
            AgentFailureCategory.VERIFICATION
        normalized.contains("401") || normalized.contains("403") ||
            normalized.contains("credential") || normalized.contains("authorization") ||
            normalized.contains("api key") || normalized.contains("api_key") ||
            normalized.contains("authentication") || normalized.contains("认证失败") ->
            AgentFailureCategory.AUTHENTICATION
        normalized.contains("429") || normalized.contains("rate limit") -> AgentFailureCategory.RATE_LIMIT
        normalized.contains("no longer connected") || normalized.contains("device") && normalized.contains("offline") ->
            AgentFailureCategory.DEVICE_DISCONNECTED
        normalized.contains("observation") || normalized.contains("ui hierarchy") ||
            normalized.contains("screenshot") || normalized.contains("bridge") && normalized.contains("observe") ->
            AgentFailureCategory.OBSERVATION
        normalized.contains("adb") || normalized.contains("device action") ||
            normalized.contains("launch action") || normalized.contains("device rejected") ->
            AgentFailureCategory.DEVICE_EXECUTION
        normalized.contains("timeout") || normalized.contains("timed out") ||
            normalized.contains("network") || normalized.contains("connection") -> AgentFailureCategory.NETWORK
        normalized.contains("protocol") || normalized.contains("invalid argument") ||
            normalized.contains("missing") || normalized.contains("json") -> AgentFailureCategory.PROTOCOL
        normalized.contains("verification") || normalized.contains("unverified") -> AgentFailureCategory.VERIFICATION
        normalized.contains("blocked") || normalized.contains("denied") || normalized.contains("safety") ->
            AgentFailureCategory.SAFETY_BLOCKED
        normalized.contains("budget") || normalized.contains("token") && normalized.contains("limit") ->
            AgentFailureCategory.BUDGET_EXHAUSTED
        else -> AgentFailureCategory.UNKNOWN
    }
    val action = when (category) {
        AgentFailureCategory.AUTHENTICATION -> AgentFailureAction.OPEN_MODEL_SETTINGS
        AgentFailureCategory.DEVICE_DISCONNECTED -> AgentFailureAction.OPEN_DEVICES
        AgentFailureCategory.DEVICE_EXECUTION,
        AgentFailureCategory.OBSERVATION,
        AgentFailureCategory.NETWORK,
        AgentFailureCategory.RATE_LIMIT,
        AgentFailureCategory.PROTOCOL,
        AgentFailureCategory.UNKNOWN -> AgentFailureAction.RETRY
        else -> AgentFailureAction.NONE
    }
    return AgentFailure(
        category = category,
        retryable = action == AgentFailureAction.RETRY,
        suggestedAction = action,
        code = category.defaultFailureCode(),
        technicalDetail = sanitizeAgentDiagnostic(source).ifBlank { null }
    )
}

fun agentFailureFrom(failure: Throwable): AgentFailure {
    if (failure is QadbBridgeException) {
        val category = when (failure.statusCode) {
            401, 403 -> AgentFailureCategory.UNKNOWN
            null -> if (failure.retryable) AgentFailureCategory.NETWORK else AgentFailureCategory.PROTOCOL
            else -> if (failure.retryable) AgentFailureCategory.NETWORK else AgentFailureCategory.PROTOCOL
        }
        return AgentFailure(
            category = category,
            retryable = failure.retryable,
            suggestedAction = if (failure.retryable) AgentFailureAction.RETRY else AgentFailureAction.NONE,
            code = AgentFailureCode.ENGINE_UNAVAILABLE,
            subsystem = AgentFailureSubsystem.ORCHESTRATOR,
            stage = AgentFailureStage.CONFIGURATION,
            technicalDetail = sanitizeAgentDiagnostic(failure.message.orEmpty()).ifBlank { null }
        )
    }
    if (failure is AgentOperationTimeoutException) {
        return AgentFailure(
            category = AgentFailureCategory.VERIFICATION,
            retryable = true,
            suggestedAction = AgentFailureAction.RETRY,
            code = AgentFailureCode.OPERATION_TIMED_OUT,
            technicalDetail = "operation_timed_out"
        )
    }
    if (failure is AgentModelCallTimeoutException) {
        return AgentFailure(
            category = AgentFailureCategory.NETWORK,
            retryable = true,
            suggestedAction = AgentFailureAction.RETRY,
            code = AgentFailureCode.MODEL_CALL_TIMED_OUT,
            subsystem = AgentFailureSubsystem.PROVIDER,
            stage = AgentFailureStage.PLANNING,
            technicalDetail = "model_call_timed_out"
        )
    }
    if (failure is AgentModelWallClockBudgetException) {
        return AgentFailure(
            category = AgentFailureCategory.BUDGET_EXHAUSTED,
            retryable = false,
            suggestedAction = AgentFailureAction.NONE,
            code = AgentFailureCode.BUDGET_EXHAUSTED,
            subsystem = AgentFailureSubsystem.ORCHESTRATOR,
            stage = AgentFailureStage.PLANNING,
            technicalDetail = "model_wall_clock_budget_exhausted"
        )
    }
    val category = when (failure) {
        is ModelProtocolIssueException,
        is ModelProtocolException -> AgentFailureCategory.PROTOCOL
        is ModelHttpException -> when (failure.statusCode) {
            401, 403 -> AgentFailureCategory.AUTHENTICATION
            429 -> AgentFailureCategory.RATE_LIMIT
            else -> AgentFailureCategory.NETWORK
        }
        else -> return agentFailureFrom(failure.message)
    }
    return agentFailure(
        category = category,
        detail = when (failure) {
            is ModelProtocolIssueException -> failure.issue.code.name
            is ModelHttpException -> "HTTP ${failure.statusCode}"
            else -> failure::class.simpleName.orEmpty()
        }
    )
}

fun agentFailure(
    category: AgentFailureCategory,
    detail: String? = null,
    code: AgentFailureCode = category.defaultFailureCode()
): AgentFailure {
    val action = when (category) {
        AgentFailureCategory.AUTHENTICATION -> AgentFailureAction.OPEN_MODEL_SETTINGS
        AgentFailureCategory.DEVICE_DISCONNECTED -> AgentFailureAction.OPEN_DEVICES
        AgentFailureCategory.DEVICE_EXECUTION,
        AgentFailureCategory.OBSERVATION,
        AgentFailureCategory.NETWORK,
        AgentFailureCategory.RATE_LIMIT,
        AgentFailureCategory.PROTOCOL,
        AgentFailureCategory.UNKNOWN -> AgentFailureAction.RETRY
        else -> AgentFailureAction.NONE
    }
    return AgentFailure(
        category = category,
        retryable = action == AgentFailureAction.RETRY,
        suggestedAction = action,
        code = code,
        technicalDetail = detail?.let(::sanitizeAgentDiagnostic)?.ifBlank { null }
    )
}

private fun AgentFailureCategory.defaultFailureCode(): AgentFailureCode = when (this) {
    AgentFailureCategory.PROTOCOL -> AgentFailureCode.PROTOCOL_INVALID
    AgentFailureCategory.NETWORK -> AgentFailureCode.NETWORK_UNAVAILABLE
    AgentFailureCategory.RATE_LIMIT -> AgentFailureCode.RATE_LIMITED
    AgentFailureCategory.AUTHENTICATION -> AgentFailureCode.AUTHENTICATION_FAILED
    AgentFailureCategory.DEVICE_DISCONNECTED -> AgentFailureCode.DEVICE_DISCONNECTED
    AgentFailureCategory.DEVICE_EXECUTION -> AgentFailureCode.DEVICE_EXECUTION_FAILED
    AgentFailureCategory.OBSERVATION -> AgentFailureCode.OBSERVATION_FAILED
    AgentFailureCategory.VERIFICATION -> AgentFailureCode.VERIFICATION_FAILED
    AgentFailureCategory.SAFETY_BLOCKED -> AgentFailureCode.SAFETY_BLOCKED
    AgentFailureCategory.BUDGET_EXHAUSTED -> AgentFailureCode.BUDGET_EXHAUSTED
    AgentFailureCategory.UNKNOWN -> AgentFailureCode.UNKNOWN
}

private fun AgentFailureCategory.defaultFailureSubsystem(): AgentFailureSubsystem = when (this) {
    AgentFailureCategory.PROTOCOL,
    AgentFailureCategory.NETWORK,
    AgentFailureCategory.RATE_LIMIT,
    AgentFailureCategory.AUTHENTICATION -> AgentFailureSubsystem.PROVIDER
    AgentFailureCategory.DEVICE_DISCONNECTED -> AgentFailureSubsystem.DEVICE_TRANSPORT
    AgentFailureCategory.DEVICE_EXECUTION -> AgentFailureSubsystem.DEVICE_ACTION
    AgentFailureCategory.OBSERVATION -> AgentFailureSubsystem.OBSERVATION
    AgentFailureCategory.VERIFICATION -> AgentFailureSubsystem.VERIFICATION
    AgentFailureCategory.SAFETY_BLOCKED -> AgentFailureSubsystem.SAFETY_POLICY
    AgentFailureCategory.BUDGET_EXHAUSTED -> AgentFailureSubsystem.ORCHESTRATOR
    AgentFailureCategory.UNKNOWN -> AgentFailureSubsystem.UNKNOWN
}

private fun AgentFailureCategory.defaultFailureStage(): AgentFailureStage = when (this) {
    AgentFailureCategory.AUTHENTICATION -> AgentFailureStage.CONFIGURATION
    AgentFailureCategory.PROTOCOL,
    AgentFailureCategory.NETWORK,
    AgentFailureCategory.RATE_LIMIT -> AgentFailureStage.PLANNING
    AgentFailureCategory.DEVICE_DISCONNECTED,
    AgentFailureCategory.DEVICE_EXECUTION -> AgentFailureStage.EXECUTING
    AgentFailureCategory.OBSERVATION -> AgentFailureStage.OBSERVING
    AgentFailureCategory.VERIFICATION -> AgentFailureStage.VERIFYING
    AgentFailureCategory.SAFETY_BLOCKED -> AgentFailureStage.PLANNING
    AgentFailureCategory.BUDGET_EXHAUSTED -> AgentFailureStage.RECOVERING
    AgentFailureCategory.UNKNOWN -> AgentFailureStage.UNKNOWN
}

internal fun sanitizeAgentDiagnostic(value: String): String = value
    .replace(Regex("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;]+"), "$1[redacted]")
    .replace(Regex("(?i)((?:api[-_ ]?key|token|password|secret)\\s*[:=]\\s*)[^\\s,;]+"), "$1[redacted]")
    .replace(Regex("https?://[^\\s]+"), "[url redacted]")
    .replace(Regex("[\\r\\n\\t]+"), " ")
    .replace(Regex("\\s{2,}"), " ")
    .trim()
    .take(300)
