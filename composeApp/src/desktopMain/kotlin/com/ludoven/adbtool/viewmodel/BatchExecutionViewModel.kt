package com.ludoven.adbtool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludoven.adbtool.util.AdbTool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal const val BATCH_EXECUTION_CONCURRENCY_LIMIT = 4

enum class BatchDeviceStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}

data class BatchDeviceResult(
    val deviceId: String,
    val status: BatchDeviceStatus = BatchDeviceStatus.PENDING,
    val output: String = "",
    val errorMessage: String? = null,
    val durationMillis: Long? = null
)

data class BatchExecutionUiState(
    val visible: Boolean = false,
    val commandTitle: String = "",
    val commandPreview: String = "",
    val devices: Map<String, BatchDeviceResult> = emptyMap()
) {
    val completedCount: Int
        get() = devices.values.count { it.status == BatchDeviceStatus.SUCCESS || it.status == BatchDeviceStatus.FAILED }

    val failedCount: Int
        get() = devices.values.count { it.status == BatchDeviceStatus.FAILED }

    val isRunning: Boolean
        get() = devices.values.any { it.status == BatchDeviceStatus.PENDING || it.status == BatchDeviceStatus.RUNNING }
}

internal fun normalizedBatchDeviceIds(deviceIds: List<String>): List<String> =
    deviceIds.map(String::trim).filter(String::isNotEmpty).distinct()

class BatchExecutionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BatchExecutionUiState())
    val uiState: StateFlow<BatchExecutionUiState> = _uiState.asStateFlow()

    private var executionJob: Job? = null
    private var executionId = 0L
    private var currentAction: (suspend (String) -> AdbTool.AdbResult)? = null

    fun execute(
        commandTitle: String,
        commandPreview: String,
        deviceIds: List<String>,
        action: suspend (deviceId: String) -> AdbTool.AdbResult
    ) {
        val targets = normalizedBatchDeviceIds(deviceIds)
        if (targets.isEmpty()) return

        executionId += 1
        executionJob?.cancel()
        currentAction = action
        _uiState.value = BatchExecutionUiState(
            visible = true,
            commandTitle = commandTitle,
            commandPreview = commandPreview,
            devices = targets.associateWith { BatchDeviceResult(deviceId = it) }
        )
        launchTargets(targets, action, executionId)
    }

    fun retryFailed() {
        if (_uiState.value.isRunning) return
        val action = currentAction ?: return
        val targets = _uiState.value.devices.values
            .filter { it.status == BatchDeviceStatus.FAILED }
            .map { it.deviceId }
        if (targets.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                visible = true,
                devices = state.devices.mapValues { (deviceId, result) ->
                    if (deviceId in targets) BatchDeviceResult(deviceId = deviceId) else result
                }
            )
        }
        executionId += 1
        launchTargets(targets, action, executionId)
    }

    fun closePanel() {
        _uiState.update { it.copy(visible = false) }
    }

    private fun launchTargets(
        targets: List<String>,
        action: suspend (deviceId: String) -> AdbTool.AdbResult,
        batchId: Long
    ) {
        executionJob = viewModelScope.launch {
            val semaphore = Semaphore(BATCH_EXECUTION_CONCURRENCY_LIMIT)
            coroutineScope {
                targets.map { deviceId ->
                    async {
                        semaphore.withPermit {
                            updateDevice(batchId, deviceId) { it.copy(status = BatchDeviceStatus.RUNNING) }
                            val startedAt = System.nanoTime()
                            val result = runCatching { action(deviceId) }
                                .getOrElse { error ->
                                    if (error is CancellationException) throw error
                                    AdbTool.AdbResult(
                                        success = false,
                                        output = "",
                                        errorMessage = error.message ?: error::class.simpleName ?: "Unknown error"
                                    )
                                }
                            val durationMillis = (System.nanoTime() - startedAt) / 1_000_000
                            updateDevice(batchId, deviceId) { current ->
                                current.copy(
                                    status = if (result.success) BatchDeviceStatus.SUCCESS else BatchDeviceStatus.FAILED,
                                    output = result.output,
                                    errorMessage = result.errorMessage,
                                    durationMillis = durationMillis
                                )
                            }
                        }
                    }
                }.awaitAll()
            }
        }
    }

    private fun updateDevice(batchId: Long, deviceId: String, transform: (BatchDeviceResult) -> BatchDeviceResult) {
        _uiState.update { state ->
            if (batchId != executionId) return@update state
            val current = state.devices[deviceId] ?: return@update state
            state.copy(devices = state.devices + (deviceId to transform(current)))
        }
    }
}
