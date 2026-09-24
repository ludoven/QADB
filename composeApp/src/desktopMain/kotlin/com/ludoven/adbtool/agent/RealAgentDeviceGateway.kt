package com.ludoven.adbtool.agent

import com.ludoven.adbtool.util.AdbTool
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay

class RealAgentDeviceGateway(
    private val appCatalog: InstalledAppCatalog = RealInstalledAppCatalog(),
    private val uiParser: UiHierarchyParser = UiHierarchyParser(),
    private val screenshotProcessor: AgentScreenshotProcessor = AgentScreenshotProcessor(),
    private val inputHelper: AgentInputHelper = AgentInputHelper(),
    private val deviceStatusRepository: DeviceStatusRepository = DeviceStatusRuntime.repository
) : AgentDeviceGateway, AgentDeviceStatusGateway, AgentAppCatalogGateway, CuratedDeviceCommandGateway {
    private val observations = ConcurrentHashMap<String, AgentObservation>()
    private val adbObservationSource = AdbObservationSource(uiParser, screenshotProcessor)

    override suspend fun isConnected(deviceId: String): Boolean =
        AdbTool.getConnectedDevices().contains(deviceId)

    override suspend fun capabilities(deviceId: String): AgentDeviceCapabilities =
        adbObservationSource.capabilities(deviceId)

    override suspend fun confirmationRequirement(
        deviceId: String,
        action: AgentAction,
        observation: AgentObservation
    ): String? = when {
        action is AgentAction.InputText && inputHelper.requiresUnicodeHelper(action.text) ->
            "Unicode input temporarily installs, enables, or switches the QADB input method"
        action is AgentAction.Tap && action.meta.target == "visual candidate" ->
            "The target is available only as an unstructured visual location; confirm this coordinate action"
        else -> null
    }

    override suspend fun readStatus(deviceId: String, forceRefresh: Boolean): DeviceStatusSnapshot {
        if (!isConnected(deviceId)) {
            throw AgentException("The selected device is no longer connected")
        }
        return deviceStatusRepository.readStatus(deviceId, forceRefresh)
    }

    override suspend fun readInstalledApps(
        deviceId: String,
        forceRefresh: Boolean
    ): List<InstalledAgentApp> {
        if (!isConnected(deviceId)) {
            throw AgentException("The selected device is no longer connected")
        }
        return appCatalog.list(deviceId, forceRefresh)
    }

    override suspend fun observe(deviceId: String): AgentObservation {
        if (!isConnected(deviceId)) {
            throw AgentException("The selected device is no longer connected")
        }
        val observation = adbObservationSource.observe(
            deviceId = deviceId,
            includeScreenshot = true,
            includeUiHierarchy = true
        )
        observations[deviceId] = observation
        return observation
    }

    override suspend fun observeLightweight(
        deviceId: String,
        includeUiHierarchy: Boolean
    ): AgentObservation {
        if (!isConnected(deviceId)) {
            throw AgentException("The selected device is no longer connected")
        }
        val observation = adbObservationSource.observe(
            deviceId = deviceId,
            includeScreenshot = false,
            includeUiHierarchy = includeUiHierarchy
        )
        observations[deviceId] = observation
        return observation
    }

    override suspend fun readSystemProbe(deviceId: String, probeId: String): String? =
        adbObservationSource.readSystemProbe(deviceId, probeId)

    override suspend fun readSetting(
        deviceId: String,
        setting: CuratedDeviceSetting
    ): CuratedSettingValue? {
        if (!isConnected(deviceId)) return null
        return when (setting) {
            CuratedDeviceSetting.WIFI -> readSystemProbe(deviceId, "wifi")?.toBooleanStrictOrNull()
                ?.let(CuratedSettingValue::Toggle)
            CuratedDeviceSetting.BLUETOOTH -> readSystemProbe(deviceId, "bluetooth")?.toBooleanStrictOrNull()
                ?.let(CuratedSettingValue::Toggle)
            CuratedDeviceSetting.BRIGHTNESS -> readSystemProbe(deviceId, "brightness")?.toIntOrNull()
                ?.coerceIn(0, 255)
                ?.let { CuratedSettingValue.Level((it * 100 + 127) / 255) }
            CuratedDeviceSetting.ROTATION_AUTO -> readSystemProbe(deviceId, "rotation_auto")?.toBooleanStrictOrNull()
                ?.let(CuratedSettingValue::Toggle)
        }
    }

    override suspend fun writeSetting(
        deviceId: String,
        setting: CuratedDeviceSetting,
        value: CuratedSettingValue
    ): AgentToolResult {
        if (!isConnected(deviceId)) {
            return AgentToolResult(false, "The selected device is no longer connected")
        }
        val args = when (setting) {
            CuratedDeviceSetting.WIFI -> {
                val enabled = (value as? CuratedSettingValue.Toggle)?.enabled
                    ?: return AgentToolResult(false, "Wi-Fi requires a toggle value")
                arrayOf("shell", "svc", "wifi", if (enabled) "enable" else "disable")
            }
            CuratedDeviceSetting.BLUETOOTH -> {
                val enabled = (value as? CuratedSettingValue.Toggle)?.enabled
                    ?: return AgentToolResult(false, "Bluetooth requires a toggle value")
                arrayOf("shell", "svc", "bluetooth", if (enabled) "enable" else "disable")
            }
            CuratedDeviceSetting.BRIGHTNESS -> {
                val percent = (value as? CuratedSettingValue.Level)?.value
                    ?: return AgentToolResult(false, "Brightness requires a level value")
                val platformValue = (percent * 255 + 50) / 100
                arrayOf("shell", "settings", "put", "system", "screen_brightness", platformValue.toString())
            }
            CuratedDeviceSetting.ROTATION_AUTO -> {
                val enabled = (value as? CuratedSettingValue.Toggle)?.enabled
                    ?: return AgentToolResult(false, "Rotation requires a toggle value")
                arrayOf("shell", "settings", "put", "system", "accelerometer_rotation", if (enabled) "1" else "0")
            }
        }
        return adbResult(AdbTool.execAdbWithTimeoutAsync(ADB_ACTION_TIMEOUT_MILLIS, "-s", deviceId, *args))
    }

    override suspend fun execute(deviceId: String, action: AgentAction): AgentToolResult {
        if (!isConnected(deviceId)) {
            return AgentToolResult(false, "The selected device is no longer connected")
        }
        if (action.requiresAgentForegroundCheck() && !currentForegroundMatchesObservation(deviceId)) {
            return AgentToolResult(false, "Foreground app changed or could not be verified; observe again")
        }
        return when (action) {
            AgentAction.Observe -> AgentToolResult(true, "Observation refreshed")
            is AgentAction.FindApp -> findApp(deviceId, action.query)
            is AgentAction.OpenApp -> openApp(deviceId, action.query)
            is AgentAction.Tap -> {
                if (observations[deviceId]?.observationId != action.observationId) {
                    AgentToolResult(false, "The coordinate action references a stale observation")
                } else {
                    tap(deviceId, action.x, action.y)
                }
            }
            is AgentAction.TapElement -> {
                val node = resolveNode(deviceId, action.observationId, action.elementId)
                    ?: return AgentToolResult(false, "The element reference is stale; observe again")
                tap(deviceId, node.bounds.centerX, node.bounds.centerY)
            }
            is AgentAction.Swipe -> adbResult(AdbTool.execAdbWithTimeoutAsync(
                    ADB_ACTION_TIMEOUT_MILLIS, "-s", deviceId, "shell", "input", "swipe",
                    action.startX.toString(), action.startY.toString(),
                    action.endX.toString(), action.endY.toString(), action.durationMs.toString()
                ))
            is AgentAction.InputText -> {
                if (observations[deviceId]?.observationId != action.observationId) {
                    return AgentToolResult(false, "Text input references a stale observation")
                }
                var targetNode: UiNodeSnapshot? = null
                action.elementId?.let { elementId ->
                    val observationId = action.observationId
                        ?: return AgentToolResult(false, "Input element requires an observation ID")
                    val node = resolveNode(deviceId, observationId, elementId)
                        ?: return AgentToolResult(false, "The input element reference is stale; observe again")
                    targetNode = node
                    val focusResult = tap(deviceId, node.bounds.centerX, node.bounds.centerY)
                    if (!focusResult.success) return focusResult
                    delay(INPUT_FOCUS_SETTLE_MILLIS)
                }
                if (action.replaceExisting && !targetNode?.text.isNullOrEmpty()) {
                    val clearResult = clearFocusedText(deviceId, requireNotNull(targetNode).text)
                    if (!clearResult.success) return clearResult
                }
                inputHelper.input(deviceId, action.text, allowInstall = true)
            }
            is AgentAction.KeyEvent -> adbResult(AdbTool.execAdbWithTimeoutAsync(
                    ADB_ACTION_TIMEOUT_MILLIS, "-s", deviceId, "shell", "input", "keyevent", action.key.androidKeyCode
                ))
            is AgentAction.LaunchPackage -> {
                if (!appCatalog.isInstalled(deviceId, action.packageName)) {
                    AgentToolResult(false, "Package is not in the installed application catalog")
                } else {
                    adbResult(AdbTool.startAppAsync(action.packageName, deviceId))
                }
            }
            is AgentAction.Wait -> {
                delay(action.durationMs.toLong())
                AgentToolResult(true, "Waited ${action.durationMs} ms")
            }
            is AgentAction.ForceStopPackage -> adbResult(AdbTool.stopAppAsync(action.packageName, deviceId))
            is AgentAction.ClearAppData -> adbResult(AdbTool.clearAppDataAsync(action.packageName, deviceId))
            is AgentAction.UninstallPackage -> adbResult(
                AdbTool.execAdbWithTimeoutAsync(
                    ADB_ACTION_TIMEOUT_MILLIS, "-s", deviceId, "uninstall", action.packageName
                )
            ).also { appCatalog.invalidate(deviceId) }
            AgentAction.RebootDevice -> adbResult(
                AdbTool.execAdbWithTimeoutAsync(
                    ADB_ACTION_TIMEOUT_MILLIS, "-s", deviceId, "reboot"
                )
            )
            is AgentAction.Finish -> AgentToolResult(true, action.summary)
            is AgentAction.ExternalApproval -> AgentToolResult(false, "Bridge approvals cannot execute through ADB")
        }
    }

    private suspend fun findApp(deviceId: String, query: String): AgentToolResult {
        val matches = appCatalog.find(deviceId, query)
        if (matches.isEmpty()) {
            return AgentToolResult(false, "No installed launchable application matched the query")
        }
        val output = matches.joinToString("\n") {
            "label=${it.label.take(100)} package=${it.packageName}"
        }
        return AgentToolResult(
            success = true,
            output = "Installed app matches (untrusted labels):\n$output",
            ambiguous = matches.size > 1,
            resolvedPackages = matches.map { it.packageName }
        )
    }

    private suspend fun openApp(deviceId: String, query: String): AgentToolResult {
        val matches = appCatalog.find(deviceId, query)
        if (matches.isEmpty()) {
            return AgentToolResult(false, "No installed launchable application matched the query")
        }
        if (matches.size != 1) {
            return AgentToolResult(
                success = false,
                output = "The application query is ambiguous; refine the app name",
                ambiguous = true,
                resolvedPackages = matches.map { it.packageName }
            )
        }
        val resolved = matches.single()
        val launched = adbResult(AdbTool.startAppAsync(resolved.packageName, deviceId))
        return launched.copy(
            output = if (launched.success) {
                "Opened installed app ${resolved.label.take(100)}"
            } else {
                launched.output
            },
            resolvedPackages = listOf(resolved.packageName)
        )
    }

    private suspend fun tap(deviceId: String, x: Int, y: Int): AgentToolResult = adbResult(
        AdbTool.execAdbWithTimeoutAsync(
            ADB_ACTION_TIMEOUT_MILLIS,
            "-s", deviceId, "shell", "input", "tap", x.toString(), y.toString()
        )
    )

    private suspend fun clearFocusedText(deviceId: String, currentText: String): AgentToolResult {
        val deleteCount = currentText.codePointCount(0, currentText.length).coerceIn(1, MAX_REPLACE_DELETE_KEYS)
        val keyCodes = Array(deleteCount + 1) { index ->
            if (index == 0) KEYCODE_MOVE_END else KEYCODE_DEL
        }
        return adbResult(
            AdbTool.execAdbWithTimeoutAsync(
                ADB_ACTION_TIMEOUT_MILLIS,
                "-s", deviceId, "shell", "input", "keyevent", *keyCodes
            )
        )
    }

    private fun resolveNode(deviceId: String, observationId: String, elementId: String): UiNodeSnapshot? =
        observations[deviceId]
            ?.takeIf { it.observationId == observationId }
            ?.uiNodes
            ?.firstOrNull { it.elementId == elementId }

    private suspend fun currentForegroundMatchesObservation(deviceId: String): Boolean = runCatching {
        matchesAgentForegroundActivity(
            observations[deviceId]?.currentActivity.orEmpty(),
            adbObservationSource.currentActivity(deviceId)
        )
    }.getOrDefault(false)

    private fun adbResult(result: AdbTool.AdbResult): AgentToolResult = AgentToolResult(
        success = result.success,
        output = if (result.success) {
            result.output.ifBlank { "Completed" }
        } else {
            result.errorMessage ?: result.output.ifBlank { "ADB action failed" }
        }
    )
}

internal fun matchesAgentForegroundActivity(expected: String, current: String): Boolean {
    val expectedPackage = expected.substringBefore('/').takeIf { '/' in expected && it.isNotBlank() }
    val currentPackage = current.substringBefore('/').takeIf { '/' in current && it.isNotBlank() }
    return expectedPackage != null && expectedPackage == currentPackage
}

internal fun AgentAction.requiresAgentForegroundCheck(): Boolean = when (this) {
    is AgentAction.Tap, is AgentAction.TapElement, is AgentAction.Swipe,
    is AgentAction.InputText, is AgentAction.KeyEvent -> true
    else -> false
}

private const val ADB_ACTION_TIMEOUT_SECONDS = 30L
private const val ADB_ACTION_TIMEOUT_MILLIS = ADB_ACTION_TIMEOUT_SECONDS * 1_000L
private const val INPUT_FOCUS_SETTLE_MILLIS = 250L
private const val KEYCODE_MOVE_END = "123"
private const val KEYCODE_DEL = "67"
private const val MAX_REPLACE_DELETE_KEYS = 2_000
