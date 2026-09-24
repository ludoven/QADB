package com.ludoven.adbtool.viewmodel

import androidx.lifecycle.viewModelScope
import com.ludoven.adbtool.agent.AgentBudgetStatus
import com.ludoven.adbtool.agent.AgentMessage
import com.ludoven.adbtool.agent.AgentMessageRole
import com.ludoven.adbtool.agent.AgentPublicActivityReducer
import com.ludoven.adbtool.agent.AgentPublicActivityState
import com.ludoven.adbtool.agent.AgentPublicEvent
import com.ludoven.adbtool.agent.AgentPublicEventPayload
import com.ludoven.adbtool.agent.AgentPublicEventSequencer
import com.ludoven.adbtool.agent.AgentPublicRunStatus
import com.ludoven.adbtool.agent.AgentPublicToolKind
import com.ludoven.adbtool.agent.AgentPublicToolResult
import com.ludoven.adbtool.agent.AgentPublicToolSummary
import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentStopOutcome
import com.ludoven.adbtool.agent.AgentStepStatus
import com.ludoven.adbtool.agent.AgentTaskUiState
import com.ludoven.adbtool.agent.AgentTaskMode
import com.ludoven.adbtool.agent.AgentTaskLogEntry
import com.ludoven.adbtool.agent.AgentTaskLogRuntime
import com.ludoven.adbtool.agent.AgentSessionHistoryRecord
import com.ludoven.adbtool.agent.AgentSessionHistoryRuntime
import com.ludoven.adbtool.agent.AgentSessionHistoryStore
import com.ludoven.adbtool.agent.AgentRunMetrics
import com.ludoven.adbtool.agent.AiConfigRepository
import com.ludoven.adbtool.agent.AiConfiguration
import com.ludoven.adbtool.agent.AgentProviderRuntime
import com.ludoven.adbtool.agent.AgentProviderRepository
import com.ludoven.adbtool.agent.AgentProviderAuthType
import com.ludoven.adbtool.agent.AgentModelRole
import com.ludoven.adbtool.agent.AgentCapabilityAttestation
import com.ludoven.adbtool.agent.AgentCapabilityTier
import com.ludoven.adbtool.agent.AgentDeviceGateway
import com.ludoven.adbtool.agent.AgentEngineVersion
import com.ludoven.adbtool.agent.AgentEngineKind
import com.ludoven.adbtool.agent.AgentExecutionStrategy
import com.ludoven.adbtool.agent.AgentTaskRunner
import com.ludoven.adbtool.agent.CancellableAgentTaskRunner
import com.ludoven.adbtool.agent.AgentTaskRunnerReadiness
import com.ludoven.adbtool.agent.AgentTaskRunnerRecovery
import com.ludoven.adbtool.agent.ArtemisEngineRuntime
import com.ludoven.adbtool.agent.ArtemisEnginePreferences
import com.ludoven.adbtool.agent.SelectableAgentTaskRunner
import com.ludoven.adbtool.agent.artemis.DeviceLeaseManager
import com.ludoven.adbtool.agent.artemis.DeviceLeaseRuntime
import com.ludoven.adbtool.agent.artemis.ExternalTaskRecord
import com.ludoven.adbtool.agent.artemis.ExternalTaskRuntime
import com.ludoven.adbtool.agent.ResolvedAgentProvider
import com.ludoven.adbtool.agent.RealAgentDeviceGateway
import com.ludoven.adbtool.agent.RoutedScreenshotAgentGateway
import com.ludoven.adbtool.agent.ScreenshotAgentEngine
import com.ludoven.adbtool.agent.AgentUsage
import com.ludoven.adbtool.agent.agentFailureFrom
import com.ludoven.adbtool.agent.toPublicMetrics
import com.ludoven.adbtool.agent.toPublicStage
import com.ludoven.adbtool.agent.toPublicToolResult
import com.ludoven.adbtool.agent.toPublicToolSummary
import com.ludoven.adbtool.util.l10n
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal fun ResolvedAgentProvider?.isReadyForAgentResponse(): Boolean =
    this != null && (
        profile.authType == AgentProviderAuthType.NONE ||
            !authSecret.isNullOrBlank()
        ) && capabilities.text

internal fun ResolvedAgentProvider?.isReadyForVisualAgent(
    attestation: AgentCapabilityAttestation?
): Boolean = isReadyForAgentResponse() &&
    attestation != null &&
    attestation.tier >= AgentCapabilityTier.L3_VISUAL_AGENT

internal fun ExternalTaskRecord.blocksNewDeviceTask(deviceId: String): Boolean =
    this.deviceId == deviceId && status !in setOf(
        "completed", "engine_reported_success", "failed", "rejected", "stopped", "orphaned_released"
    )

class AiAgentViewModel(
    private val configRepository: AiConfigRepository = AiConfiguration.repository,
    deviceGateway: AgentDeviceGateway = RealAgentDeviceGateway(),
    agentTaskRunner: AgentTaskRunner = SelectableAgentTaskRunner(
        ScreenshotAgentEngine(
            model = RoutedScreenshotAgentGateway(),
            deviceGateway = deviceGateway
        )
    ),
    private val providerRepository: AgentProviderRepository = AgentProviderRuntime.repository,
    private val deviceLeaseManager: DeviceLeaseManager = DeviceLeaseRuntime.manager,
    private val artemisPreferences: ArtemisEnginePreferences = ArtemisEngineRuntime.preferences,
    private val sessionHistoryStore: AgentSessionHistoryStore = AgentSessionHistoryRuntime.store
) : BaseViewModel() {
    private val agentTaskRunner: AgentTaskRunner = agentTaskRunner
    private val _state = MutableStateFlow(AgentTaskUiState())
    val state: StateFlow<AgentTaskUiState> = _state.asStateFlow()

    val modelConfig = configRepository.config
    val apiKeyAvailable = configRepository.hasApiKeyState
    private val _configurationReady = MutableStateFlow(false)
    val configurationReady: StateFlow<Boolean> = _configurationReady.asStateFlow()
    private val _advisoryReady = MutableStateFlow(false)
    val advisoryReady: StateFlow<Boolean> = _advisoryReady.asStateFlow()
    private val _modelConfigured = MutableStateFlow(false)
    val modelConfigured: StateFlow<Boolean> = _modelConfigured.asStateFlow()
    private val _externalEngineSelected = MutableStateFlow(false)
    val externalEngineSelected: StateFlow<Boolean> = _externalEngineSelected.asStateFlow()
    private val _configurationChecked = MutableStateFlow(false)
    val configurationChecked: StateFlow<Boolean> = _configurationChecked.asStateFlow()
    private val _taskLogs = MutableStateFlow(emptyList<AgentTaskLogEntry>())
    val taskLogs: StateFlow<List<AgentTaskLogEntry>> = _taskLogs.asStateFlow()
    private val _runMetrics = MutableStateFlow(emptyList<AgentRunMetrics>())
    val runMetrics: StateFlow<List<AgentRunMetrics>> = _runMetrics.asStateFlow()
    private val _sessionHistory = MutableStateFlow(emptyList<AgentSessionHistoryRecord>())
    val sessionHistory: StateFlow<List<AgentSessionHistoryRecord>> = _sessionHistory.asStateFlow()
    private val _externalStopCheckMessage = MutableStateFlow<Pair<String, String>?>(null)
    val externalStopCheckMessage: StateFlow<Pair<String, String>?> = _externalStopCheckMessage.asStateFlow()

    private val taskRunHandles = AgentTaskRunHandleRegistry()
    private val configurationRefreshVersion = AtomicLong()
    private val unconfirmedStopDevices = ConcurrentHashMap.newKeySet<String>()
    private var publicEventAdapter: AgentOrchestratorPublicEventAdapter? = null
    private val publicStateSynchronizer = AgentPublicStateSynchronizer()

    init {
        viewModelScope.launch {
            artemisPreferences.configuration.collect {
                refreshConfigurationStatus()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { (agentTaskRunner as? AgentTaskRunnerRecovery)?.reconcileExternalTasks() }
        }
        refreshTaskLogs()
        refreshRunMetrics()
        refreshSessionHistory()
    }

    @Synchronized
    fun refreshConfigurationStatus() {
        val refreshVersion = configurationRefreshVersion.incrementAndGet()
        _configurationChecked.value = false
        viewModelScope.launch {
            // Tests and embedding callers can supply a runner that is independent of
            // the process-wide engine choice.  Only query managed Artemis readiness
            // when this ViewModel actually owns a runner that exposes that contract.
            val artemisEnabled = artemisPreferences.configuration.value.enabled &&
                agentTaskRunner is AgentTaskRunnerReadiness
            runCatching { configRepository.isReady() }
            val provider = runCatching {
                providerRepository.ensureMigration()
                providerRepository.resolve(AgentModelRole.BRAIN)
            }.getOrNull()
            val responseProvider = runCatching {
                providerRepository.resolve(AgentModelRole.RESPONDER)
            }.getOrNull()
            val runnerReadiness = if (artemisEnabled) {
                runCatching {
                    (agentTaskRunner as? AgentTaskRunnerReadiness)?.readiness()
                        ?: error("Selected engine does not expose readiness")
                }.getOrNull()
            } else {
                null
            }
            val artemisSelected = artemisEnabled &&
                runnerReadiness?.selectedEngine == AgentEngineKind.ARTEMIS
            val executionReady = if (artemisSelected) {
                runnerReadiness?.ready == true
            } else {
                val attestation = provider?.let { providerRepository.capabilityAttestation(it.profile) }
                provider.isReadyForVisualAgent(attestation)
            }
            synchronized(this@AiAgentViewModel) {
                if (refreshVersion != configurationRefreshVersion.get()) return@synchronized
                _externalEngineSelected.value = artemisSelected
                _advisoryReady.value = responseProvider.isReadyForAgentResponse()
                _modelConfigured.value = artemisSelected || provider.isReadyForAgentResponse()
                _configurationReady.value = executionReady
                _configurationChecked.value = true
            }
        }
    }

    fun startTask(
        prompt: String,
        selectedDeviceId: String?,
        mode: AgentTaskMode = AgentTaskMode.EXECUTE,
        parentRunId: String? = null,
        priorOutcomeChecked: Boolean = false,
        readDeviceEvidence: Boolean = false
    ): Boolean {
        val task = prompt.trim()
        if (task.isEmpty() || _state.value.isRunning) return false
        if (readDeviceEvidence && mode == AgentTaskMode.EXECUTE) return false
        if (parentRunId != null) {
            val parent = runCatching { sessionHistoryStore.find(parentRunId) }.getOrNull()
            if (!priorOutcomeChecked || mode != AgentTaskMode.EXECUTE || parent == null || parent.finishedAtMs == null ||
                parent.phase !in setOf(AgentRunPhase.COMPLETED, AgentRunPhase.FAILED, AgentRunPhase.CANCELLED) ||
                parent.deviceId != selectedDeviceId || parent.stopOutcome == AgentStopOutcome.UNCONFIRMED ||
                parent.stopOutcome == AgentStopOutcome.REQUESTED
            ) {
                publicStateSynchronizer.serialized {
                    _state.value = _state.value.copy(errorMessage = l10n(
                        "无法从该历史段继续；请先核对设备效果、原设备与停止状态。",
                        "Cannot continue this segment. Check the device outcome, original device and stop state first."
                    ))
                }
                return false
            }
        }
        if (if (mode == AgentTaskMode.EXECUTE) !configurationReady.value else !_advisoryReady.value) {
            publicStateSynchronizer.serialized {
                _state.value = _state.value.copy(
                    errorMessage = if (mode != AgentTaskMode.EXECUTE) {
                        l10n("请先配置可回答文本的 Provider。", "Configure a text-capable provider first.")
                    } else if (_externalEngineSelected.value) {
                        l10n("请先启动本机 Artemis 服务，并通过其设备和 Provider 就绪检查", "Start the local Artemis service and pass its device/provider readiness check")
                    } else {
                        l10n(
                            "请先在设置中配置并测试支持视觉与工具调用的 BRAIN Provider（L3）",
                            "Configure and test a BRAIN provider with L3 vision and tool calling"
                        )
                    }
                )
            }
            return false
        }
        if ((mode == AgentTaskMode.EXECUTE || readDeviceEvidence) && selectedDeviceId.isNullOrBlank()) {
            publicStateSynchronizer.serialized {
                _state.value = _state.value.copy(
                    errorMessage = l10n("请先连接并选择一台设备", "Connect and select a device first")
                )
            }
            return false
        }
        if (mode == AgentTaskMode.EXECUTE && (selectedDeviceId.orEmpty() in unconfirmedStopDevices ||
            (agentTaskRunner is AgentTaskRunnerRecovery &&
                ExternalTaskRuntime.registry.recent().any { it.blocksNewDeviceTask(selectedDeviceId.orEmpty()) }))
        ) {
            publicStateSynchronizer.serialized {
                _state.value = _state.value.copy(errorMessage = l10n(
                    "该设备有未决的 Artemis 会话；请先核实远端执行与停止状态。",
                    "This device has an unresolved Artemis session. Verify its remote execution and stop state first."
                ))
            }
            return false
        }
        val acceptedAtMs = System.currentTimeMillis()
        val executionDeviceId = if (mode == AgentTaskMode.EXECUTE || readDeviceEvidence) selectedDeviceId.orEmpty() else ""
        val runId = UUID.randomUUID().toString()
        val taskJob = publicStateSynchronizer.serialized {
            if (_state.value.isRunning) return@serialized null
            // Every send is a new run. A linked continuation retains only its
            // parent ID in history, never the prior model or conversation context.
            val initialState = AgentTaskUiState()
            if (mode == AgentTaskMode.EXECUTE && !deviceLeaseManager.tryAcquire(executionDeviceId, runId)) {
                _state.value = _state.value.copy(
                    errorMessage = l10n(
                        "该设备已有一个 Agent 任务在运行，请先停止或等待它完成",
                        "Another Agent task is already running on this device"
                    )
                )
                return@serialized null
            }
            val adapter = AgentOrchestratorPublicEventAdapter(
                runId = runId,
                baseline = initialState
            )
            publicEventAdapter = adapter
            val startedActivity = AgentPublicActivityReducer.reduce(
                initialState.publicActivity,
                adapter.startEvent()
            )
            // The accepted user message must be part of the state handed to the runner.
            // Otherwise the first orchestrator snapshot (built from this pre-send state)
            // would erase the optimistic user message from the conversation as soon as
            // the engine publishes (mergeAgentOrchestratorSnapshot replaces _state).
            val acceptedState = initialState.copy(
                messages = initialState.messages + AgentMessage(
                    id = UUID.randomUUID().toString(),
                    role = AgentMessageRole.USER,
                    text = task,
                    runId = runId
                ),
                steps = emptyList(),
                isRunning = true,
                taskMode = mode,
                deviceEvidenceAuthorized = readDeviceEvidence,
                needsUser = false,
                boundDeviceId = executionDeviceId.takeIf(String::isNotBlank),
                pendingConfirmation = null,
                errorMessage = null,
                failure = null,
                phase = AgentRunPhase.OBSERVING,
                usage = AgentUsage(),
                lastRequestUsage = null,
                stopOutcome = AgentStopOutcome.NONE,
                budgetStatus = AgentBudgetStatus(),
                executionDetails = emptyList(),
                deviceState = null,
                pageDiff = null,
                latestScreenshot = null,
                publicActivity = startedActivity
            )
            _state.value = acceptedState
            val firstFeedbackMs = (System.currentTimeMillis() - acceptedAtMs).coerceAtLeast(0)
            val runHandle = taskRunHandles.begin(runId, adapter, firstFeedbackMs, executionDeviceId)
            viewModelScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
                executeTask(
                    task = task,
                    executionDeviceId = executionDeviceId,
                    initialState = acceptedState,
                    runHandle = runHandle,
                    acceptedAtMs = acceptedAtMs,
                    firstFeedbackMs = runHandle.firstFeedbackMs,
                    parentRunId = parentRunId
                )
            }.also { job ->
                if (!taskRunHandles.attachJob(runHandle, job)) {
                    deviceLeaseManager.release(executionDeviceId, runId)
                    error("Agent run was replaced before launch")
                }
                job.invokeOnAgentCancellation {
                    if (runHandle.stopOutcome == AgentStopOutcome.NONE ||
                        runHandle.stopOutcome == AgentStopOutcome.CONFIRMED
                    ) {
                        deviceLeaseManager.release(executionDeviceId, runId)
                    }
                    completeCancelledRun(runHandle)
                }
            }
        } ?: return false
        taskJob.start()
        return true
    }

    private suspend fun executeTask(
        task: String,
        executionDeviceId: String,
        initialState: AgentTaskUiState,
        runHandle: AgentTaskRunHandle,
        acceptedAtMs: Long,
        firstFeedbackMs: Long,
        parentRunId: String? = null
    ) {
        try {
            runCatching {
                sessionHistoryStore.start(runHandle.runId, task, executionDeviceId, acceptedAtMs, parentRunId)
            }.onFailure { System.err.println("Agent history start failed: ${it::class.simpleName}") }
            val terminalState = agentTaskRunner.run(
                task = task,
                deviceId = executionDeviceId,
                initialState = initialState,
                runId = runHandle.runId,
                acceptedAtMs = acceptedAtMs,
                firstFeedbackMs = firstFeedbackMs,
                onState = { publishOrchestratorState(runHandle.runId, runHandle.adapter, it) },
                confirmSensitiveAction = { step ->
                    val deferred = CompletableDeferred<Boolean>()
                    if (!taskRunHandles.registerConfirmation(runHandle, step.id, deferred)) {
                        deferred.cancel()
                        throw CancellationException("Agent run is no longer active")
                    }
                    try {
                        deferred.await()
                    } finally {
                        taskRunHandles.clearConfirmation(runHandle, deferred)
                    }
                }
            )
            // A runner normally emits incremental snapshots, but its return value is
            // still the authoritative terminal state.  Publishing it prevents an
            // adapter that returns early from leaving the UI in the last in-progress
            // phase it happened to emit.
            publishOrchestratorState(runHandle.runId, runHandle.adapter, terminalState)
        } catch (_: CancellationException) {
            // Also covers cancellation before the orchestrator enters its own cancellation handler.
            completeCancelledRun(runHandle)
        } catch (failure: Throwable) {
            // A background runner failure must always close the visible run.  Do not
            // surface raw transport/provider text here: it can be untrusted and is
            // not needed for the user to retry safely.
            val report = agentFailureFrom(failure)
            publishOrchestratorState(
                runHandle.runId,
                runHandle.adapter,
                initialState.copy(
                    isRunning = false,
                    needsUser = false,
                    phase = AgentRunPhase.FAILED,
                    errorMessage = l10n("智能体任务失败，请重试。", "The agent task failed. Please retry."),
                    failure = report
                )
            )
        } finally {
            val finalState = publicStateSynchronizer.serialized {
                _state.value.takeIf { it.publicActivity.activeRunId == runHandle.runId }
            }
            finalState?.let { snapshot ->
                runCatching { sessionHistoryStore.finish(runHandle.runId, snapshot) }
                    .onFailure { System.err.println("Agent history finish failed: ${it::class.simpleName}") }
            }
            if (runHandle.stopOutcome != AgentStopOutcome.UNCONFIRMED &&
                runHandle.stopOutcome != AgentStopOutcome.REQUESTED
            ) {
                deviceLeaseManager.release(executionDeviceId, runHandle.runId)
            }
            publicStateSynchronizer.serialized {
                releaseRunHandleLocked(runHandle)
            }
            refreshTaskLogs()
            refreshRunMetrics()
            refreshSessionHistory()
        }
    }

    /** Entry point for streaming/model integrations that can emit native public events directly. */
    fun acceptPublicEvent(event: AgentPublicEvent) {
        publicStateSynchronizer.serialized {
            acceptPublicEventLocked(event)
        }
    }

    fun respondToConfirmation(runId: String, stepId: String, approved: Boolean) {
        publicStateSynchronizer.serialized {
            if (_state.value.publicActivity.activeRunId == runId &&
                _state.value.pendingConfirmation?.id == stepId
            ) {
                taskRunHandles.respondToActiveConfirmation(runId, stepId, approved)
            }
        }
    }

    fun cancelTask() {
        val cancellation = publicStateSynchronizer.serialized {
            publicEventAdapter?.next(AgentPublicEventPayload.Cancelling)?.let(::acceptPublicEventLocked)
            taskRunHandles.detachActiveForCancellation().also {
                _state.value = _state.value.copy(
                    pendingConfirmation = null,
                    stopOutcome = if (it.runId != null) AgentStopOutcome.REQUESTED else _state.value.stopOutcome
                )
            }
        }
        cancellation.confirmation?.cancel()
        cancellation.runId?.let { runId ->
            val cancellableRunner = agentTaskRunner as? CancellableAgentTaskRunner
            if (cancellableRunner != null) {
                // Keep the external ownership mapping alive until the targeted stop was
                // attempted; cancelling the local job first can erase that mapping.
                viewModelScope.launch(Dispatchers.IO) {
                    val confirmed = withTimeoutOrNull(5_000) {
                        runCatching { cancellableRunner.cancel(runId) }.getOrDefault(false)
                    } == true
                    cancellation.handle?.stopOutcome = if (confirmed) {
                        AgentStopOutcome.CONFIRMED
                    } else {
                        AgentStopOutcome.UNCONFIRMED
                    }
                    cancellation.handle?.deviceId?.let { deviceId ->
                        if (confirmed) unconfirmedStopDevices.remove(deviceId)
                        else unconfirmedStopDevices.add(deviceId)
                    }
                    publicStateSynchronizer.serialized {
                        if (_state.value.publicActivity.activeRunId == runId) {
                            _state.value = _state.value.copy(stopOutcome = cancellation.handle?.stopOutcome ?: AgentStopOutcome.UNCONFIRMED)
                        }
                    }
                    runCatching { sessionHistoryStore.finish(runId, _state.value) }
                        .onFailure { System.err.println("Agent history stop update failed: ${it::class.simpleName}") }
                    cancellation.job?.cancel()
                    if (confirmed && cancellation.job?.isCompleted == true) {
                        cancellation.handle?.let { deviceLeaseManager.release(it.deviceId, runId) }
                    }
                    refreshSessionHistory()
                }
            } else {
                cancellation.handle?.stopOutcome = AgentStopOutcome.CONFIRMED
                publicStateSynchronizer.serialized {
                    if (_state.value.publicActivity.activeRunId == runId) {
                        _state.value = _state.value.copy(stopOutcome = AgentStopOutcome.CONFIRMED)
                    }
                }
                cancellation.job?.cancel()
            }
            return
        }
        cancellation.job?.cancel()
    }

    fun newTask() {
        if (_state.value.isRunning) {
            cancelTask()
            return
        }
        publicStateSynchronizer.serialized {
            taskRunHandles.abandonActive()
            publicEventAdapter = null
            _state.value = AgentTaskUiState()
        }
    }

    fun clearError() {
        publicStateSynchronizer.serialized {
            _state.value = _state.value.copy(errorMessage = null)
        }
    }

    fun refreshTaskLogs() = viewModelScope.launch {
        _taskLogs.value = runCatching { AgentTaskLogRuntime.store.recent() }.getOrDefault(emptyList())
    }

    fun refreshRunMetrics() = viewModelScope.launch {
        _runMetrics.value = runCatching { AgentTaskLogRuntime.store.recentMetrics() }.getOrDefault(emptyList())
    }

    fun refreshSessionHistory() = viewModelScope.launch(Dispatchers.IO) {
        _sessionHistory.value = runCatching { sessionHistoryStore.recent() }
            .onFailure { System.err.println("Agent history read failed: ${it::class.simpleName}") }
            .getOrDefault(emptyList())
    }

    suspend fun deleteSessionHistory(runId: String): Boolean = withContext(Dispatchers.IO) {
        if (_state.value.isRunning) return@withContext false
        val record = runCatching { sessionHistoryStore.find(runId) }
            .onFailure { System.err.println("Agent history delete lookup failed: ${it::class.simpleName}") }
            .getOrNull()
            ?: return@withContext false
        val external = runCatching { ExternalTaskRuntime.registry.find(runId) }
            .onFailure { System.err.println("Agent external record lookup failed: ${it::class.simpleName}") }
            .getOrElse { return@withContext false }
        if (external?.blocksNewDeviceTask(record.deviceId) == true) {
            return@withContext false
        }
        val deleted = runCatching { sessionHistoryStore.deleteFinished(runId) }
            .onFailure { System.err.println("Agent history delete failed: ${it::class.simpleName}") }
            .getOrDefault(false)
        if (deleted) {
            _sessionHistory.value = runCatching { sessionHistoryStore.recent() }
                .onFailure { System.err.println("Agent history refresh after delete failed: ${it::class.simpleName}") }
                .getOrElse { _sessionHistory.value.filterNot { it.runId == runId } }
        }
        deleted
    }

    /** Rechecks one owned session; it never submits, resumes, or manually overrides a device action. */
    fun recheckExternalStop(runId: String) {
        _externalStopCheckMessage.value = runId to l10n("正在核查所属会话和执行器…", "Checking the owned session and worker…")
        viewModelScope.launch(Dispatchers.IO) {
            val record = ExternalTaskRuntime.registry.find(runId)
            val recovery = agentTaskRunner as? AgentTaskRunnerRecovery
            if (record == null || recovery == null) {
                _externalStopCheckMessage.value = runId to l10n("找不到可核查的 Artemis 会话。", "No owned Artemis session is available to check.")
                return@launch
            }
            val failure = runCatching { recovery.reconcileExternalTasks() }.exceptionOrNull()
            val updated = ExternalTaskRuntime.registry.find(runId)
            if (failure == null && updated?.status == "stopped") {
                if (ExternalTaskRuntime.registry.recent().none { it.blocksNewDeviceTask(record.deviceId) }) {
                    unconfirmedStopDevices.remove(record.deviceId)
                }
                deviceLeaseManager.release(record.deviceId, runId)
                runCatching { sessionHistoryStore.confirmStop(runId) }
                    .onFailure { System.err.println("Agent history stop confirmation failed: ${it::class.simpleName}") }
                publicStateSynchronizer.serialized {
                    if (_state.value.publicActivity.activeRunId == runId &&
                        _state.value.stopOutcome == AgentStopOutcome.UNCONFIRMED
                    ) {
                        _state.value = _state.value.copy(stopOutcome = AgentStopOutcome.CONFIRMED)
                    }
                }
                _externalStopCheckMessage.value = runId to l10n(
                    "已确认执行器静止；此会话不再阻止同设备新任务。",
                    "Worker quiescence confirmed; this session no longer blocks the device."
                )
            } else {
                val detail = if (failure != null) {
                    l10n("外部服务无响应或连接失败（${failure.message?.take(60) ?: "connection error"}）。若确认设备已静止，可点击强制解除。", "External service unreachable. If device is idle, click Force Release to unlock.")
                } else {
                    l10n("仍无法确认执行器静止；同设备任务继续受阻。若外部进程已终止，可点击强制解除。", "Worker quiescence unconfirmed; device remains blocked. If process terminated, click Force Release.")
                }
                _externalStopCheckMessage.value = runId to detail
            }
            refreshSessionHistory()
        }
    }

    /** Manually releases the device lease and unconfirmed stop blocker for an orphaned or dead external session. */
    fun forceReleaseExternalBlock(runId: String): Boolean {
        if (_state.value.isRunning) return false
        val record = ExternalTaskRuntime.registry.find(runId) ?: return false
        val deviceId = record.deviceId
        val updated = record.copy(
            status = "orphaned_released",
            updatedAtMs = System.currentTimeMillis()
        )
        ExternalTaskRuntime.registry.upsert(updated)
        if (ExternalTaskRuntime.registry.recent().none { it.blocksNewDeviceTask(deviceId) }) {
            unconfirmedStopDevices.remove(deviceId)
        }
        deviceLeaseManager.release(deviceId, runId)
        runCatching { sessionHistoryStore.confirmStop(runId) }
            .onFailure { System.err.println("Agent history stop confirmation failed during force release: ${it::class.simpleName}") }
        publicStateSynchronizer.serialized {
            if (_state.value.publicActivity.activeRunId == runId &&
                _state.value.stopOutcome == AgentStopOutcome.UNCONFIRMED
            ) {
                _state.value = _state.value.copy(stopOutcome = AgentStopOutcome.CONFIRMED)
            }
        }
        _externalStopCheckMessage.value = runId to l10n(
            "已人工解除该会话对设备的占用（标记为孤儿会话释放）；此设备可重新开始新任务。",
            "Manually released the device lock for this session (marked as orphaned); this device is now available."
        )
        refreshSessionHistory()
        return true
    }

    private fun publishOrchestratorState(
        runId: String,
        adapter: AgentOrchestratorPublicEventAdapter,
        snapshot: AgentTaskUiState
    ) {
        publicStateSynchronizer.serialized {
            val current = _state.value
            if (current.publicActivity.activeRunId != runId || publicEventAdapter !== adapter) {
                return@serialized
            }

            var publicActivity = current.publicActivity
            adapter.eventsFor(snapshot).forEach { event ->
                publicActivity = AgentPublicActivityReducer.reduce(publicActivity, event)
            }
            val taggedMessages = snapshot.messages
                .mapIndexed { index, message ->
                    if (index >= adapter.baselineMessageCount) message.copy(runId = runId) else message
                }
                .filterNot { message ->
                    message.role == AgentMessageRole.SYSTEM &&
                        snapshot.phase == AgentRunPhase.FAILED &&
                        message.text == snapshot.errorMessage
                }

            _state.value = mergeAgentOrchestratorSnapshot(
                current = current,
                snapshot = snapshot.copy(messages = taggedMessages),
                publicActivity = publicActivity
            )
        }
    }

    private fun completeCancelledRun(runHandle: AgentTaskRunHandle) {
        publicStateSynchronizer.serialized {
            val current = _state.value
            if (
                current.publicActivity.activeRunId == runHandle.runId &&
                publicEventAdapter === runHandle.adapter
            ) {
                val status = current.publicActivity.activeRun?.status
                _state.value = if (status?.isTerminal == true) {
                    current.copy(pendingConfirmation = null)
                } else {
                    current.applyCancelledEvent(
                        runHandle.adapter.next(AgentPublicEventPayload.Cancelled)
                    )
                }
            }
            releaseRunHandleLocked(runHandle)
        }
    }

    /** Must be called while [publicStateSynchronizer] is held. */
    private fun releaseRunHandleLocked(runHandle: AgentTaskRunHandle) {
        if (taskRunHandles.finish(runHandle) && publicEventAdapter === runHandle.adapter) {
            publicEventAdapter = null
        }
    }

    /** Must be called while [publicStateSynchronizer] is held. */
    private fun acceptPublicEventLocked(event: AgentPublicEvent) {
        val current = _state.value
        val reduced = AgentPublicActivityReducer.reduce(current.publicActivity, event)
        if (reduced != current.publicActivity) {
            _state.value = current.copy(publicActivity = reduced)
        }
    }
}

internal fun mergeAgentOrchestratorSnapshot(
    current: AgentTaskUiState,
    snapshot: AgentTaskUiState,
    publicActivity: AgentPublicActivityState
): AgentTaskUiState {
    val currentStatus = current.publicActivity.activeRun?.status
    val nextStatus = publicActivity.activeRun?.status
    val preserveCurrentSnapshot =
        (currentStatus == AgentPublicRunStatus.CANCELLING &&
            nextStatus == AgentPublicRunStatus.CANCELLING) ||
            (currentStatus?.isTerminal == true && nextStatus == currentStatus)
    return if (preserveCurrentSnapshot) {
        current.copy(pendingConfirmation = null, publicActivity = publicActivity)
    } else {
        snapshot.copy(
            publicActivity = publicActivity,
            stopOutcome = if (current.stopOutcome != AgentStopOutcome.NONE) current.stopOutcome else snapshot.stopOutcome
        )
    }
}

internal fun AgentTaskUiState.applyCancelledEvent(event: AgentPublicEvent): AgentTaskUiState {
    val reduced = AgentPublicActivityReducer.reduce(publicActivity, event)
    val cancelled = reduced.runs[event.runId]?.status == AgentPublicRunStatus.CANCELLED
    return if (cancelled) {
        copy(
            isRunning = false,
            pendingConfirmation = null,
            errorMessage = null,
            phase = AgentRunPhase.CANCELLED,
            publicActivity = reduced
        )
    } else {
        copy(pendingConfirmation = null, publicActivity = reduced)
    }
}

internal fun Job.invokeOnAgentCancellation(onCancelled: () -> Unit) {
    invokeOnCompletion { cause ->
        if (cause is CancellationException) onCancelled()
    }
}

/** Serializes public run state with adapter sequencing across UI and orchestrator threads. */
internal class AgentPublicStateSynchronizer {
    private val lock = Any()

    fun <T> serialized(block: () -> T): T = synchronized(lock) { block() }
}

internal data class AgentTaskRunCancellation(
    val runId: String? = null,
    val job: Job?,
    val confirmation: CompletableDeferred<Boolean>?,
    val handle: AgentTaskRunHandle? = null
)

internal class AgentTaskRunHandle internal constructor(
    val runId: String,
    val adapter: AgentOrchestratorPublicEventAdapter,
    val firstFeedbackMs: Long,
    val deviceId: String
) {
    @Volatile internal var stopOutcome: AgentStopOutcome = AgentStopOutcome.NONE
    internal var job: Job? = null
    internal var confirmation: CompletableDeferred<Boolean>? = null
    internal var confirmationStepId: String? = null
    internal var cancellationRequested: Boolean = false
}

/** Keeps cancellation and confirmation handles scoped to the run that created them. */
internal class AgentTaskRunHandleRegistry {
    private var active: AgentTaskRunHandle? = null

    @Synchronized
    fun begin(
        runId: String,
        adapter: AgentOrchestratorPublicEventAdapter,
        firstFeedbackMs: Long,
        deviceId: String
    ): AgentTaskRunHandle = AgentTaskRunHandle(runId, adapter, firstFeedbackMs, deviceId).also { active = it }

    @Synchronized
    fun attachJob(handle: AgentTaskRunHandle, job: Job): Boolean {
        if (active !== handle || handle.cancellationRequested) return false
        handle.job = job
        return true
    }

    @Synchronized
    fun registerConfirmation(
        handle: AgentTaskRunHandle,
        stepId: String,
        confirmation: CompletableDeferred<Boolean>
    ): Boolean {
        if (active !== handle || handle.cancellationRequested) return false
        handle.confirmation = confirmation
        handle.confirmationStepId = stepId
        return true
    }

    @Synchronized
    fun clearConfirmation(
        handle: AgentTaskRunHandle,
        confirmation: CompletableDeferred<Boolean>
    ) {
        if (handle.confirmation === confirmation) {
            handle.confirmation = null
            handle.confirmationStepId = null
        }
    }

    @Synchronized
    fun respondToActiveConfirmation(runId: String, stepId: String, approved: Boolean): Boolean =
        active?.takeIf { it.runId == runId && it.confirmationStepId == stepId && !it.cancellationRequested }
            ?.confirmation
            ?.takeIf { !it.isCompleted }
            ?.complete(approved) == true

    @Synchronized
    fun detachActiveForCancellation(): AgentTaskRunCancellation {
        val handle = active ?: return AgentTaskRunCancellation(job = null, confirmation = null)
        if (handle.cancellationRequested) return AgentTaskRunCancellation(job = null, confirmation = null)
        handle.cancellationRequested = true
        val cancellation = AgentTaskRunCancellation(job = handle.job, confirmation = handle.confirmation, handle = handle)
        handle.job = null
        handle.confirmation = null
        handle.confirmationStepId = null
        return cancellation.copy(runId = handle.runId)
    }

    @Synchronized
    fun finish(handle: AgentTaskRunHandle): Boolean {
        handle.job = null
        handle.confirmation = null
        if (active !== handle) return false
        active = null
        return true
    }

    @Synchronized
    fun abandonActive() {
        active?.job = null
        active?.confirmation = null
        active = null
    }
}

/**
 * Compatibility adapter for the current snapshot-based orchestrator. New execution code can emit
 * [AgentPublicEvent] directly through [AiAgentViewModel.acceptPublicEvent].
 */
internal class AgentOrchestratorPublicEventAdapter(
    runId: String,
    baseline: AgentTaskUiState,
    clock: () -> Long = System::currentTimeMillis
) {
    private val sequencer = AgentPublicEventSequencer(runId, clock)
    private var previous = baseline
    private val baselineMessageIds = baseline.messages.asSequence().map { it.id }.toHashSet()
    private val emittedAssistantTextById = mutableMapOf<String, String>()
    private var responseCompletedEmitted = false
    val baselineMessageCount: Int = baseline.messages.size

    @Synchronized
    fun startEvent(): AgentPublicEvent = sequencer.next(AgentPublicEventPayload.RunStarted)

    @Synchronized
    fun next(payload: AgentPublicEventPayload): AgentPublicEvent = sequencer.next(payload)

    @Synchronized
    fun eventsFor(snapshot: AgentTaskUiState): List<AgentPublicEvent> {
        val events = mutableListOf<AgentPublicEvent>()
        if (
            snapshot.phase != previous.phase &&
            snapshot.phase !in setOf(
                AgentRunPhase.COMPLETED,
                AgentRunPhase.FAILED,
                AgentRunPhase.CANCELLED
            )
        ) {
            if (snapshot.phase == AgentRunPhase.RETRYING) {
                events += next(AgentPublicEventPayload.Retrying())
            } else {
                events += next(AgentPublicEventPayload.StageChanged(snapshot.phase.toPublicStage()))
            }
        }

        val previousSteps = previous.steps.associateBy { it.id }
        snapshot.steps.forEach { step ->
            val old = previousSteps[step.id]
            val tool = step.action.toPublicToolSummary()
            when {
                step.status == AgentStepStatus.AWAITING_CONFIRMATION &&
                    old?.status != AgentStepStatus.AWAITING_CONFIRMATION -> {
                    events += next(AgentPublicEventPayload.ConfirmationRequested(tool))
                }
                step.status == AgentStepStatus.RUNNING && old?.status != AgentStepStatus.RUNNING -> {
                    events += next(AgentPublicEventPayload.ToolStarted(tool))
                }
                step.status.toPublicToolResult() != null && old?.status != step.status -> {
                    if (old == null) events += next(AgentPublicEventPayload.ToolStarted(tool))
                    events += next(
                        AgentPublicEventPayload.ToolFinished(
                            tool = tool,
                            result = requireNotNull(step.status.toPublicToolResult())
                        )
                    )
                }
            }
        }

        val semanticTool = AgentPublicToolSummary(AgentPublicToolKind.SEMANTIC_GOAL)
        val oldSemantic = previous.semanticActivity
        snapshot.semanticActivity?.let { semantic ->
            when {
                semantic.status == com.ludoven.adbtool.agent.AgentSemanticActivityStatus.RUNNING &&
                    oldSemantic != semantic -> events += next(AgentPublicEventPayload.ToolStarted(semanticTool))
                semantic.status != com.ludoven.adbtool.agent.AgentSemanticActivityStatus.RUNNING &&
                    oldSemantic != semantic -> {
                    if (oldSemantic == null) events += next(AgentPublicEventPayload.ToolStarted(semanticTool))
                    events += next(
                        AgentPublicEventPayload.ToolFinished(
                            semanticTool,
                            if (semantic.status == com.ludoven.adbtool.agent.AgentSemanticActivityStatus.SUCCEEDED) {
                                AgentPublicToolResult.SUCCEEDED
                            } else {
                                AgentPublicToolResult.FAILED
                            }
                        )
                    )
                }
            }
        }

        if (snapshot.budgetStatus != previous.budgetStatus) {
            events += next(
                AgentPublicEventPayload.MetricsUpdated(
                    snapshot.budgetStatus.toPublicMetrics(
                        if (snapshot.executionStrategy == AgentExecutionStrategy.SEMANTIC_V2) {
                            AgentEngineVersion.V2
                        } else {
                            AgentEngineVersion.SCREENSHOT
                        }
                    )
                )
            )
        }

        snapshot.messages
            .asSequence()
            .filter { it.id !in baselineMessageIds && it.role == AgentMessageRole.ASSISTANT }
            .forEach { message ->
                val emittedText = emittedAssistantTextById[message.id]
                val delta = when {
                    emittedText == null -> message.text
                    message.text.startsWith(emittedText) -> message.text.removePrefix(emittedText)
                    else -> ""
                }
                if (delta.isNotEmpty()) {
                    events += next(AgentPublicEventPayload.ResponseDelta(delta))
                }
                if (emittedText == null || message.text.startsWith(emittedText)) {
                    emittedAssistantTextById[message.id] = message.text
                }
            }

        val reachedTerminal = snapshot.phase in setOf(
            AgentRunPhase.COMPLETED,
            AgentRunPhase.FAILED,
            AgentRunPhase.CANCELLED
        )
        if (
            reachedTerminal &&
            !responseCompletedEmitted &&
            emittedAssistantTextById.isNotEmpty()
        ) {
            events += next(AgentPublicEventPayload.ResponseCompleted)
            responseCompletedEmitted = true
        }

        when {
            snapshot.phase == AgentRunPhase.FAILED && previous.phase != AgentRunPhase.FAILED -> {
                events += next(
                    AgentPublicEventPayload.Failed(
                        snapshot.failure ?: agentFailureFrom(snapshot.errorMessage)
                    )
                )
            }
            snapshot.phase == AgentRunPhase.CANCELLED && previous.phase != AgentRunPhase.CANCELLED -> {
                events += next(AgentPublicEventPayload.Cancelled)
            }
            snapshot.phase == AgentRunPhase.COMPLETED && previous.phase != AgentRunPhase.COMPLETED -> {
                events += next(AgentPublicEventPayload.Completed)
            }
        }
        previous = snapshot
        return events
    }
}
