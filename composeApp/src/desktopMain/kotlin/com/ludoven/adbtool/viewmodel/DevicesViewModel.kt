package com.ludoven.adbtool.viewmodel

import androidx.lifecycle.viewModelScope
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.entity.DeviceInfoData
import com.ludoven.adbtool.entity.DeviceCenterInfoData
import com.ludoven.adbtool.entity.BatteryStatus
import com.ludoven.adbtool.entity.MsgContent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class DeviceCommandOutputs(
    val props: String,
    val kernel: String,
    val wmSize: String,
    val wmDensity: String,
    val fontScale: String,
    val ifconfig: String,
    val cpuStat: String,
    val memInfo: String,
    val dataDf: String,
    val battery: String,
    val latencyMs: Long
)

private data class CpuSample(
    val idle: Long,
    val total: Long
)

internal fun deviceInfoLoadShouldApply(requestedDeviceId: String?, selectedDeviceId: String?): Boolean {
    val requested = requestedDeviceId?.trim().orEmpty()
    val selected = selectedDeviceId?.trim().orEmpty()
    return requested.isNotEmpty() && requested == selected
}

internal fun deviceInfoLoadShouldCancelForSelectionChange(
    activeLoadDeviceId: String?,
    nextSelectedDeviceId: String?
): Boolean {
    val active = activeLoadDeviceId?.trim().orEmpty()
    if (active.isEmpty()) return false
    return active != nextSelectedDeviceId?.trim().orEmpty()
}

class DevicesViewModel : BaseViewModel() {
    companion object {
        internal fun normalizedDeviceId(deviceId: String?): String? =
            deviceId?.trim()?.takeIf { it.isNotBlank() }
    }

    private val refreshTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val propRegex = Regex("\\[(.*?)]\\s*:\\s*\\[(.*?)]")

    private val _devices = MutableStateFlow<List<String>>(emptyList())
    val devices: StateFlow<List<String>> = _devices.asStateFlow()

    private val _selectedDevice = MutableStateFlow<String?>(null)
    val selectedDevice: StateFlow<String?> = _selectedDevice.asStateFlow()

    private val _deviceDisplayNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceDisplayNames: StateFlow<Map<String, String>> = _deviceDisplayNames.asStateFlow()

    private val _deviceInfo = MutableStateFlow<DeviceInfoData?>(null)
    val deviceInfo: StateFlow<DeviceInfoData?> = _deviceInfo.asStateFlow()

    private val _centerInfo = MutableStateFlow<DeviceCenterInfoData?>(null)
    val centerInfo: StateFlow<DeviceCenterInfoData?> = _centerInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow("--")
    val lastRefreshTime: StateFlow<String> = _lastRefreshTime.asStateFlow()

    private var deviceInfoLoadJob: Job? = null
    private var deviceInfoLoadDeviceId: String? = null

    private var cpuPollJob: Job? = null
    private var cpuPrevSample: CpuSample? = null

    /**
     * 每隔 5 秒轮询一次 CPU 使用率，并更新首页指标卡片。
     */
    fun startCpuPolling(deviceId: String?) {
        cpuPollJob?.cancel()
        val normalizedDeviceId = normalizedDeviceId(deviceId)
        if (normalizedDeviceId == null) {
            cpuPrevSample = null
            return
        }
        cpuPollJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cpuPrevSample = readCpuSample(normalizedDeviceId)
            }
            while (isActive) {
                delay(5_000)
                withContext(Dispatchers.IO) {
                    val sample = readCpuSample(normalizedDeviceId) ?: return@withContext
                    val prev = cpuPrevSample
                    cpuPrevSample = sample
                    if (prev != null && sample.total > prev.total && sample.idle >= prev.idle) {
                        val idleDelta = sample.idle - prev.idle
                        val totalDelta = sample.total - prev.total
                        val percent = (100 - (idleDelta * 100) / totalDelta).coerceIn(0, 100)
                        _centerInfo.update { it?.copy(cpuUsage = "$percent%") }
                    }
                }
            }
        }
    }

    fun stopCpuPolling() {
        cpuPollJob?.cancel()
        cpuPollJob = null
        cpuPrevSample = null
    }

    private suspend fun readCpuSample(deviceId: String): CpuSample? {
        val cpuLine = AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "cat", "/proc/stat")
            .lines()
            .firstOrNull { it.startsWith("cpu ") }
        val values = cpuLine?.split(Regex("\\s+"))?.drop(1)?.mapNotNull { it.toLongOrNull() }
        if (values == null || values.size < 7) return null
        return CpuSample(idle = values[3] + values[4], total = values.sum())
    }

    fun refreshDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            val newDevices = withContext(Dispatchers.IO) { AdbTool.getConnectedDevices() }
            _devices.value = newDevices
            _deviceDisplayNames.value = withContext(Dispatchers.IO) {
                buildDeviceDisplayNameMap(newDevices)
            }
            _selectedDevice.value = when {
                _selectedDevice.value == null && newDevices.isNotEmpty() -> newDevices.first()
                _selectedDevice.value != null && _selectedDevice.value !in newDevices -> newDevices.firstOrNull()
                else -> _selectedDevice.value
            }
            selectDevice(_selectedDevice.value)
            if (_selectedDevice.value == null) {
                updateLastRefreshTime()
            }
            _isLoading.value = false
        }
    }

    fun selectDevice(deviceId: String?) {
        val normalizedDeviceId = normalizedDeviceId(deviceId)
        if (deviceInfoLoadShouldCancelForSelectionChange(deviceInfoLoadDeviceId, normalizedDeviceId)) {
            deviceInfoLoadJob?.cancel()
            deviceInfoLoadJob = null
            deviceInfoLoadDeviceId = null
            _isLoading.value = false
        }
        _selectedDevice.value = normalizedDeviceId
        if (normalizedDeviceId != null) {
            AdbTool.selectDeviceId = normalizedDeviceId
            loadDeviceInfo(normalizedDeviceId)
        } else {
            deviceInfoLoadJob?.cancel()
            deviceInfoLoadJob = null
            deviceInfoLoadDeviceId = null
            AdbTool.selectDeviceId = null
            _deviceInfo.value = null
            _centerInfo.value = null
            updateLastRefreshTime()
        }
    }

    fun disconnectSelectedDevice() {
        val deviceId = _selectedDevice.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) { AdbTool.disconnectDevice(deviceId) }
            showTipDialog(MsgContent.Text(if (result.success) result.output else (result.errorMessage ?: result.output)), autoDismiss = true)
            refreshDevices()
            _isLoading.value = false
        }
    }

    fun connectNetworkDevice(host: String, port: String) {
        val hostPort = when {
            host.isBlank() -> return
            port.isNotBlank() -> "$host:$port"
            else -> host
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) { AdbTool.connectDevice(hostPort) }
            showTipDialog(
                MsgContent.Text(if (result.success) result.output else (result.errorMessage ?: result.output)),
                autoDismiss = true
            )
            refreshDevices()
            _isLoading.value = false
        }
    }

    private fun loadDeviceInfo(deviceId: String) {
        deviceInfoLoadJob?.cancel()
        deviceInfoLoadDeviceId = deviceId
        deviceInfoLoadJob = viewModelScope.launch {
            _isLoading.value = true

            withContext(Dispatchers.IO) {
                val outputs = coroutineScope {
                    val props = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "getprop") }
                    val kernel = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "uname", "-r") }
                    val wmSize = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "wm", "size") }
                    val wmDensity = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "wm", "density") }
                    val fontScale = async {
                        AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "settings", "get", "system", "font_scale")
                    }
                    val ifconfig = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "ip addr show wlan0") }
                    val cpuStat = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "cat", "/proc/stat") }
                    val memInfo = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "cat", "/proc/meminfo") }
                    val dataDf = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "df", "/data") }
                    val battery = async { AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "dumpsys", "battery") }
                    val latency = async {
                        try {
                            val start = System.currentTimeMillis()
                            AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "echo", "ping")
                            System.currentTimeMillis() - start
                        } catch (_: Exception) {
                            -1L
                        }
                    }
                    DeviceCommandOutputs(
                        props = props.await(),
                        kernel = kernel.await(),
                        wmSize = wmSize.await(),
                        wmDensity = wmDensity.await(),
                        fontScale = fontScale.await(),
                        ifconfig = ifconfig.await(),
                        cpuStat = cpuStat.await(),
                        memInfo = memInfo.await(),
                        dataDf = dataDf.await(),
                        battery = battery.await(),
                        latencyMs = latency.await()
                    )
                }

                val propsOutput = outputs.props
                val propMap = mutableMapOf<String, String>()

                propsOutput.lines().forEach { line ->
                    propRegex
                        .find(line)?.let { match ->
                        propMap[match.groupValues[1]] = match.groupValues[2]
                    }
                }

                val androidVersion = propMap["ro.build.version.release"] ?: ""
                val sdkVersion = propMap["ro.build.version.sdk"] ?: ""
                val deviceModel = propMap["ro.product.model"] ?: ""
                val manufacturer = propMap["ro.product.manufacturer"] ?: ""
                val romVersion = propMap["ro.build.display.id"] ?: ""
                val buildFingerprint = propMap["ro.build.fingerprint"] ?: ""
                val kernelVersion = outputs.kernel
                    .lineSequence()
                    .firstOrNull { it.isNotBlank() }
                    ?.trim()
                    .orEmpty()
                val screenResolution = formatScreenResolution(
                    sizeOutput = outputs.wmSize,
                    densityOutput = outputs.wmDensity
                )
                val fontScale = formatFontScale(outputs.fontScale)

                // IP 和 MAC
                val ifconfigOutput = outputs.ifconfig
                val ipMatch = Regex("inet ([0-9.]+)").find(ifconfigOutput)
                val macMatch = Regex("link/ether ([0-9a-f:]+)").find(ifconfigOutput)
                val ipAddress = ipMatch?.groupValues?.get(1) ?: ""
                val macAddress = macMatch?.groupValues?.get(1) ?: ""

                // CPU 使用率（解析 idle 和总时间计算）
                val cpuStatOutput = outputs.cpuStat
                val cpuLine = cpuStatOutput.lines().firstOrNull { it.startsWith("cpu ") }
                val cpuUsage = cpuLine?.let {
                    val values = it.split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
                    if (values.size >= 7) {
                        val idle = values[3] + values[4]
                        val total = values.sum()
                        // 简单快照不太准，可以两次取值做差更准，这里简单用 snapshot
                        val usagePercent = 100 - ((idle * 100) / total)
                        "$usagePercent%"
                    } else {
                        ""
                    }
                } ?: ""

                // 内存使用率
                val memOutput = outputs.memInfo
                val totalMem = Regex("MemTotal:\\s+(\\d+)").find(memOutput)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val freeMem = Regex("MemAvailable:\\s+(\\d+)").find(memOutput)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val memoryUsage = if (totalMem > 0) {
                    val usedPercent = ((totalMem - freeMem) * 100 / totalMem)
                    "$usedPercent%"
                } else {
                    ""
                }

                // 存储空间（剩余/总共, 单位 GB）
                val dfOutput = outputs.dataDf
                val storageLine = dfOutput.lines().drop(1).firstOrNull()
                val storageParts = storageLine?.split(Regex("\\s+")) ?: emptyList()
                val storageUsage = if (storageParts.size >= 5) {
                    val total = storageParts[1].toLongOrNull() ?: 0L
                    val used = storageParts[2].toLongOrNull() ?: 0L
                    val totalGB = total / 1024.0 / 1024.0
                    val usedGB = used / 1024.0 / 1024.0
                    String.format(Locale.US, "%.1f/%.1fG", usedGB, totalGB)
                } else {
                    ""
                }

                // 电量
                val batteryOutput = outputs.battery
                val level = Regex("level: (\\d+)").find(batteryOutput)?.groupValues?.get(1)
                val status = Regex("status: (\\d+)").find(batteryOutput)?.groupValues?.get(1)
                val batteryStatus = BatteryStatus.fromStatusCode(status)
                val batteryLevel = if (level != null) "$level%" else ""

                // Latency measurement: time a simple adb shell echo command
                val latencyMs = outputs.latencyMs
                val latencyStr = if (latencyMs >= 0) "${latencyMs}ms" else "--"

                // Connection speed: detect USB vs WiFi
                val connectionSpeedStr = if (deviceId.contains(":") || deviceId.matches(Regex(".*\\.\\d+\\.\\d+.*"))) {
                    "Wi-Fi"
                } else {
                    "USB"
                }

                // 创建数据对象
                val deviceInfo = DeviceInfoData(
                    androidVersion = androidVersion,
                    sdkVersion = sdkVersion,
                    kernelVersion = kernelVersion,
                    deviceModel = deviceModel,
                    manufacturer = manufacturer,
                    romVersion = romVersion,
                    screenResolution = screenResolution,
                    fontScale = fontScale,
                    buildFingerprint = buildFingerprint,
                    ipAddress = ipAddress,
                    macAddress = macAddress,
                    latency = latencyStr,
                    connectionSpeed = connectionSpeedStr
                )

                val centerInfo = DeviceCenterInfoData(
                    cpuUsage = cpuUsage,
                    memoryUsage = memoryUsage,
                    storageUsage = storageUsage,
                    batteryLevel = batteryLevel,
                    batteryStatus = batteryStatus
                )

                if (!deviceInfoLoadShouldApply(deviceId, _selectedDevice.value)) return@withContext
                _deviceInfo.value = deviceInfo
                _centerInfo.value = centerInfo
                if (deviceModel.isNotBlank()) {
                    _deviceDisplayNames.update { it + (deviceId to deviceModel) }
                }
                updateLastRefreshTime()
            }

            if (deviceInfoLoadShouldApply(deviceId, _selectedDevice.value)) {
                _isLoading.value = false
            }
        }
    }

    private suspend fun buildDeviceDisplayNameMap(deviceIds: List<String>): Map<String, String> {
        if (deviceIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        deviceIds.forEach { deviceId ->
            val model = readDeviceModel(deviceId)
            if (model.isNotBlank()) {
                result[deviceId] = model
            }
        }
        return result
    }

    private suspend fun readDeviceModel(deviceId: String): String {
        return runCatching {
            AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "getprop", "ro.product.model")
                .lineSequence()
                .firstOrNull { it.isNotBlank() }
                ?.trim()
                .orEmpty()
        }.getOrDefault("")
    }

    private fun formatScreenResolution(sizeOutput: String, densityOutput: String): String {
        val size = parseWmMetric(sizeOutput, metric = "size")
        val density = parseWmMetric(densityOutput, metric = "density")
        return when {
            size.isNotBlank() && density.isNotBlank() -> "$size(${density}dpi)"
            size.isNotBlank() -> size
            else -> ""
        }
    }

    private fun parseWmMetric(output: String, metric: String): String {
        val override = Regex("Override $metric:\\s*([^\\n\\r]+)").find(output)?.groupValues?.get(1)?.trim()
        if (!override.isNullOrBlank()) return override
        return Regex("Physical $metric:\\s*([^\\n\\r]+)").find(output)?.groupValues?.get(1)?.trim().orEmpty()
    }

    private fun formatFontScale(rawScale: String): String {
        val normalizedRaw = rawScale.trim()
        if (normalizedRaw.isBlank() || normalizedRaw.equals("null", ignoreCase = true)) return ""
        val scale = normalizedRaw.toFloatOrNull() ?: return normalizedRaw
        val scaleText = if (scale.toInt().toFloat() == scale) {
            scale.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", scale).trimEnd('0').trimEnd('.')
        }
        return "${scaleText}x"
    }

    private fun updateLastRefreshTime() {
        _lastRefreshTime.value = LocalDateTime.now().format(refreshTimeFormatter)
    }
}
