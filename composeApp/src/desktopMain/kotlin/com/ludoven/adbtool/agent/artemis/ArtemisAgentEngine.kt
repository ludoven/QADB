package com.ludoven.adbtool.agent.artemis

import com.ludoven.adbtool.agent.AgentMessage
import com.ludoven.adbtool.agent.AgentMessageRole
import com.ludoven.adbtool.agent.AgentAction
import com.ludoven.adbtool.agent.AgentRiskLevel
import com.ludoven.adbtool.agent.AgentExecutionStrategy
import com.ludoven.adbtool.agent.AgentStep
import com.ludoven.adbtool.agent.AgentStepStatus
import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentTaskRunner
import com.ludoven.adbtool.agent.AgentTaskUiState
import com.ludoven.adbtool.agent.CancellableAgentTaskRunner
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Thin QADB-side adapter for a locally managed Artemis daemon.  It owns only
 * QADB-generated session ids and deliberately polls the exact session instead
 * of consuming the daemon's global activity stream.
 */
class ArtemisAgentEngine(
    private val client: ArtemisHttpClient,
    private val profile: ArtemisProfile,
    private val bridge: ArtemisBridge,
    private val registry: ExternalTaskRegistry = ExternalTaskRuntime.registry,
    private val pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS
) : CancellableAgentTaskRunner {
    private val externalSessions = ConcurrentHashMap<String, String>()

    init {
        require(pollIntervalMs in 100L..10_000L) { "Artemis polling interval must be between 100ms and 10s" }
    }

    override suspend fun run(
        task: String,
        deviceId: String,
        initialState: AgentTaskUiState,
        runId: String?,
        acceptedAtMs: Long?,
        firstFeedbackMs: Long?,
        onState: (AgentTaskUiState) -> Unit,
        confirmSensitiveAction: suspend (com.ludoven.adbtool.agent.AgentStep) -> Boolean
    ): AgentTaskUiState {
        val qadbRunId = runId?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString()
        val sessionId = qadbRunId
        externalSessions[qadbRunId] = sessionId
        fun persist(status: String) = registry.upsert(
            ExternalTaskRecord(qadbRunId, sessionId, deviceId, profile.wireValue, status, System.currentTimeMillis())
        )
        val pendingEvents = ConcurrentLinkedQueue<ArtemisSseEvent>()
        val decidedApprovals = mutableSetOf<String>()
        var streamJob: Job? = null
        var heartbeatJob: Job? = null
        persist("submitting")
        var state = initialState.copy(
            isRunning = true,
            needsUser = false,
            boundDeviceId = deviceId,
            phase = AgentRunPhase.THINKING,
            executionStrategy = AgentExecutionStrategy.SEMANTIC_V2,
            errorMessage = null,
            failure = null,
            executionDetails = (initialState.executionDetails +
                "Artemis ${profile.wireValue} submitted with session $sessionId").takeLast(40)
        )

        fun publish() = onState(state)
        fun terminal(phase: AgentRunPhase, message: String): AgentTaskUiState {
            state = state.copy(
                isRunning = false,
                needsUser = false,
                pendingConfirmation = null,
                phase = phase,
                messages = state.messages + AgentMessage(
                    id = UUID.randomUUID().toString(),
                    role = AgentMessageRole.ASSISTANT,
                    text = message,
                    runId = qadbRunId
                ),
                errorMessage = if (phase == AgentRunPhase.FAILED) message else null
            )
            publish()
            return state
        }

        try {
            publish()
            try {
                bridge.register(qadbRunId, deviceId)
            } catch (failure: Throwable) {
                persist("bridge_registration_failed")
                throw failure
            }
            heartbeatJob = CoroutineScope(coroutineContext).launch {
                while (isActive) { bridge.heartbeat(qadbRunId); delay(3_000) }
            }
            when (val submitted = client.submit(
                ArtemisRunRequest(
                    goal = task,
                    profile = profile,
                    deviceSerial = deviceId,
                    sessionId = sessionId
                )
            )) {
                is ArtemisSubmitResult.Rejected -> return terminal(
                    AgentRunPhase.FAILED,
                    "Artemis rejected the task: ${submitted.reason}"
                )
                is ArtemisSubmitResult.Accepted -> {
                    persist(submitted.handle.status.name.lowercase())
                    state = state.copy(
                        phase = AgentRunPhase.EXECUTING,
                        executionDetails = (state.executionDetails +
                            "Artemis admitted session ${submitted.handle.sessionId}").takeLast(40)
                    )
                    publish()
                    streamJob = CoroutineScope(coroutineContext).launch {
                        var reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS
                        while (isActive) {
                            runCatching {
                                client.streamEvents(sessionId) { event -> pendingEvents += event }
                            }
                            if (!isActive) break
                            delay(reconnectDelayMs)
                            reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(MAX_RECONNECT_DELAY_MS)
                        }
                    }
                }
            }

            while (true) {
                coroutineContext.ensureActive()
                bridge.pending(qadbRunId).filterNot(decidedApprovals::contains).forEach { approvalId ->
                    val metadata = bridge.approval(qadbRunId, approvalId)
                    if (metadata?.isReviewable(approvalId, deviceId) != true) {
                        bridge.decide(qadbRunId, approvalId, false)
                        decidedApprovals += approvalId
                        state = state.copy(
                            executionDetails = (state.executionDetails +
                                "Denied Artemis approval without complete bound action details").takeLast(40)
                        )
                        publish()
                        return@forEach
                    }
                    var approvalStep = AgentStep(
                        id = "artemis-$approvalId", action = AgentAction.ExternalApproval(
                            approvalId = metadata.approvalId,
                            actionKind = metadata.actionKind,
                            actionTarget = requireNotNull(metadata.actionTarget),
                            deviceId = metadata.deviceId,
                            actionDigest = metadata.actionDigest,
                            taskVersion = metadata.taskVersion,
                            expiresInMs = metadata.expiresInMs
                        ),
                        status = AgentStepStatus.AWAITING_CONFIRMATION,
                        riskLevel = AgentRiskLevel.CONFIRMATION_REQUIRED,
                        confirmationReason = "Artemis 请求执行设备操作；批准仅对该设备、目标和动作摘要有效一次。"
                    )
                    state = state.copy(
                        steps = state.steps + approvalStep,
                        pendingConfirmation = approvalStep,
                        phase = AgentRunPhase.AWAITING_CONFIRMATION
                    )
                    publish()
                    val requested = confirmSensitiveAction(approvalStep)
                    val approved = requested && bridge.approval(qadbRunId, approvalId)
                        ?.sameReviewableBinding(metadata, approvalId, deviceId) == true
                    bridge.decide(qadbRunId, approvalId, approved)
                    decidedApprovals += approvalId
                    approvalStep = approvalStep.copy(
                        status = if (approved) AgentStepStatus.RUNNING else AgentStepStatus.DENIED,
                        result = if (approved) "Approved for bound Artemis action" else "User denied this action"
                    )
                    state = state.replaceArtemisStep(approvalStep).copy(
                        pendingConfirmation = null,
                        phase = if (approved) AgentRunPhase.EXECUTING else AgentRunPhase.THINKING
                    )
                    publish()
                }
                while (true) {
                    val event = pendingEvents.poll() ?: break
                    ArtemisEventMapper.map(event, sessionId)?.let { mapped ->
                        mapped.status?.takeIf(ArtemisTaskStatus::isProgressStatus)
                            ?.let { persist(it.name.lowercase()) }
                        state = state.copy(
                            phase = mapped.status.toStreamingPhaseOr(state.phase),
                            executionDetails = (state.executionDetails +
                                (mapped.detail ?: "Artemis event received")).takeLast(40)
                        )
                        publish()
                    }
                }
                when (val lookup = client.getSession(sessionId)) {
                    ArtemisSessionLookup.NotPersistedYet -> {
                        persist("pending_persistence")
                        state = state.copy(
                            phase = AgentRunPhase.EXECUTING,
                            executionDetails = (state.executionDetails + "Artemis session pending persistence").takeLast(40)
                        )
                        publish()
                    }
                    is ArtemisSessionLookup.Found -> when (lookup.handle.status) {
                        ArtemisTaskStatus.QUEUED,
                        ArtemisTaskStatus.PAUSED -> {
                            persist(lookup.handle.status.name.lowercase())
                            state = state.copy(
                                phase = AgentRunPhase.THINKING,
                                executionDetails = (state.executionDetails +
                                    "Artemis ${lookup.handle.status.name.lowercase()}").takeLast(40)
                            )
                            publish()
                        }
                        ArtemisTaskStatus.RUNNING,
                        ArtemisTaskStatus.UNKNOWN -> {
                            persist(lookup.rawStatus ?: "running")
                            state = state.copy(
                                phase = AgentRunPhase.EXECUTING,
                                executionDetails = (state.executionDetails +
                                    "Artemis ${lookup.rawStatus ?: "running"}").takeLast(40)
                            )
                            publish()
                        }
                        ArtemisTaskStatus.COMPLETED -> {
                            persist("completed")
                            return terminal(
                            AgentRunPhase.COMPLETED,
                            lookup.summary?.takeIf(String::isNotBlank)
                                ?.let { "$it\n\nArtemis 报告执行结束，设备最终效果尚未独立核实。" }
                                ?: "Artemis 报告执行结束，设备最终效果尚未独立核实。"
                            )
                        }
                        ArtemisTaskStatus.FAILED,
                        ArtemisTaskStatus.REJECTED -> {
                            persist(lookup.handle.status.name.lowercase())
                            return terminal(
                            AgentRunPhase.FAILED,
                            lookup.summary?.takeIf(String::isNotBlank)
                                ?: "Artemis ${lookup.handle.status.name.lowercase()} the task."
                            )
                        }
                    }
                }
                delay(pollIntervalMs)
            }
        } finally {
            streamJob?.cancel()
            heartbeatJob?.cancel()
            externalSessions.remove(qadbRunId, sessionId)
        }
    }

    /** Stops only the session created for this QADB run; it never stops all daemon work. */
    override suspend fun cancel(runId: String): Boolean {
        val sessionId = externalSessions[runId] ?: return false
        var bridgeQuiescent = runCatching { bridge.stop(runId) }.getOrDefault(false)
        val stopped = client.stop(sessionId) == ArtemisStopStatus.STOPPED
        if (stopped && !bridgeQuiescent) {
            for (attempt in 0 until 12) {
                delay(250)
                bridgeQuiescent = runCatching { bridge.quiescent(runId) }.getOrDefault(false)
                if (bridgeQuiescent) break
            }
        }
        val confirmed = bridgeQuiescent && stopped
        registry.find(runId)?.let {
            registry.upsert(it.copy(status = if (confirmed) "stopped" else "stop_unconfirmed"))
        }
        return confirmed
    }

    private companion object {
        const val DEFAULT_POLL_INTERVAL_MS = 750L
        const val INITIAL_RECONNECT_DELAY_MS = 500L
        const val MAX_RECONNECT_DELAY_MS = 5_000L
    }
}

private fun ArtemisTaskStatus?.toStreamingPhaseOr(fallback: AgentRunPhase): AgentRunPhase = when (this) {
    ArtemisTaskStatus.QUEUED, ArtemisTaskStatus.PAUSED -> AgentRunPhase.THINKING
    ArtemisTaskStatus.RUNNING, ArtemisTaskStatus.UNKNOWN -> AgentRunPhase.EXECUTING
    ArtemisTaskStatus.COMPLETED,
    ArtemisTaskStatus.FAILED,
    ArtemisTaskStatus.REJECTED -> fallback
    null -> fallback
}

private fun ArtemisTaskStatus.isProgressStatus(): Boolean = when (this) {
    ArtemisTaskStatus.QUEUED,
    ArtemisTaskStatus.PAUSED,
    ArtemisTaskStatus.RUNNING,
    ArtemisTaskStatus.UNKNOWN -> true
    ArtemisTaskStatus.COMPLETED,
    ArtemisTaskStatus.FAILED,
    ArtemisTaskStatus.REJECTED -> false
}

private fun AgentTaskUiState.replaceArtemisStep(updated: AgentStep): AgentTaskUiState =
    copy(steps = steps.map { if (it.id == updated.id) updated else it })

internal fun ArtemisApprovalMetadata.isReviewable(approvalId: String, deviceId: String): Boolean =
    this.approvalId == approvalId && this.deviceId == deviceId && taskVersion == 0 &&
        pending && expiresInMs > 0 && actionDigest.matches(Regex("^[0-9a-f]{64}$")) &&
        actionKind in setOf(
            "press_key_enter",
            "manage_app_stop", "manage_app_force_stop", "manage_app_clear",
            "manage_app_clear_data", "manage_app_uninstall"
        ) && actionTarget?.trim()?.length?.let { it in 1..200 } == true

private fun ArtemisApprovalMetadata.sameReviewableBinding(
    original: ArtemisApprovalMetadata,
    approvalId: String,
    deviceId: String
): Boolean = isReviewable(approvalId, deviceId) &&
    actionKind == original.actionKind && actionTarget == original.actionTarget &&
    actionDigest == original.actionDigest && taskVersion == original.taskVersion

/**
 * A process-wide lease avoids two QADB engine instances operating on the same
 * physical device concurrently.  The lease is intentionally in-memory: it is
 * released on application restart and does not claim ownership of other tools.
 */
class DeviceLeaseManager {
    private val holders = ConcurrentHashMap<String, String>()

    fun tryAcquire(deviceId: String, runId: String): Boolean =
        holders.putIfAbsent(deviceId, runId) == null

    fun release(deviceId: String, runId: String) {
        holders.remove(deviceId, runId)
    }

    fun holder(deviceId: String): String? = holders[deviceId]
}

object DeviceLeaseRuntime {
    val manager: DeviceLeaseManager = DeviceLeaseManager()
}
