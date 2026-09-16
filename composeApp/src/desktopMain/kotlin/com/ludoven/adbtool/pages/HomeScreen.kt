package com.ludoven.adbtool.pages

import com.ludoven.adbtool.ui.mac.*
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.QadbPalette
import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.entity.MsgContent

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.*
import adbtool_desktop.composeapp.generated.resources.battery_level
import adbtool_desktop.composeapp.generated.resources.connected
import adbtool_desktop.composeapp.generated.resources.connected_for
import adbtool_desktop.composeapp.generated.resources.connection_info
import adbtool_desktop.composeapp.generated.resources.connection_type
import adbtool_desktop.composeapp.generated.resources.cpu_usage
import adbtool_desktop.composeapp.generated.resources.device_info
import adbtool_desktop.composeapp.generated.resources.android_version
import adbtool_desktop.composeapp.generated.resources.build_fingerprint
import adbtool_desktop.composeapp.generated.resources.device_overview
import adbtool_desktop.composeapp.generated.resources.device_model
import adbtool_desktop.composeapp.generated.resources.device_mirror
import adbtool_desktop.composeapp.generated.resources.device_status
import adbtool_desktop.composeapp.generated.resources.disconnected
import adbtool_desktop.composeapp.generated.resources.disconnect
import adbtool_desktop.composeapp.generated.resources.font_scale
import adbtool_desktop.composeapp.generated.resources.ip_address
import adbtool_desktop.composeapp.generated.resources.install_apk_short
import adbtool_desktop.composeapp.generated.resources.key_screenshot_short
import adbtool_desktop.composeapp.generated.resources.kernel_version
import adbtool_desktop.composeapp.generated.resources.last_refresh
import adbtool_desktop.composeapp.generated.resources.mac_address
import adbtool_desktop.composeapp.generated.resources.manufacturer
import adbtool_desktop.composeapp.generated.resources.memory_usage
import adbtool_desktop.composeapp.generated.resources.no_device
import adbtool_desktop.composeapp.generated.resources.no_device_available
import adbtool_desktop.composeapp.generated.resources.no_device_info
import adbtool_desktop.composeapp.generated.resources.offline
import adbtool_desktop.composeapp.generated.resources.online
import adbtool_desktop.composeapp.generated.resources.open_shell
import adbtool_desktop.composeapp.generated.resources.quick_actions
import adbtool_desktop.composeapp.generated.resources.refresh
import adbtool_desktop.composeapp.generated.resources.port
import adbtool_desktop.composeapp.generated.resources.rom_version
import adbtool_desktop.composeapp.generated.resources.screen_resolution
import adbtool_desktop.composeapp.generated.resources.select_device
import adbtool_desktop.composeapp.generated.resources.select_device_hint
import adbtool_desktop.composeapp.generated.resources.storage_usage
import adbtool_desktop.composeapp.generated.resources.transfer_speed
import adbtool_desktop.composeapp.generated.resources.updated_ago
import adbtool_desktop.composeapp.generated.resources.latency
import adbtool_desktop.composeapp.generated.resources.usb_connection
import adbtool_desktop.composeapp.generated.resources.wireless_connection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.ludoven.adbtool.ui.mac.DropdownMenu
import com.ludoven.adbtool.ui.mac.DropdownMenuItem
import com.ludoven.adbtool.ui.mac.ExperimentalMaterial3Api
import com.ludoven.adbtool.ui.mac.Icon
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ludoven.adbtool.entity.DeviceInfoData
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.AdbPathManager
import com.ludoven.adbtool.util.NetworkConnectMemory
import com.ludoven.adbtool.util.isWirelessAdbConnection
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.DevicesViewModel
import com.ludoven.adbtool.widget.DashboardMetricCard
import com.ludoven.adbtool.widget.DashboardPanel
import com.ludoven.adbtool.widget.DeviceInfoCell
import com.ludoven.adbtool.widget.DeviceMetaChip
import com.ludoven.adbtool.widget.DiagnosticAlertRow
import com.ludoven.adbtool.widget.GlassCard
import com.ludoven.adbtool.widget.HomeToolbarActionButton
import com.ludoven.adbtool.widget.LabeledValueRow
import com.ludoven.adbtool.widget.OutlineActionButton
import com.ludoven.adbtool.widget.QuickActionTile
import com.ludoven.adbtool.widget.QuickActionCard
import com.ludoven.adbtool.widget.ConnectionStatusBadge
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

internal fun homeDeviceConnected(selectedDevice: String?): Boolean =
    !selectedDevice.isNullOrBlank()

private object HomeVisualTokens {
    val Primary: Color @Composable get() = QadbColors.primary
    val Text: Color @Composable get() = QadbColors.textPrimary
    val Muted: Color @Composable get() = QadbColors.textSecondary
    val Border: Color @Composable get() = QadbColors.border
    val Soft: Color @Composable get() = QadbColors.surfaceVariant
    val Divider: Color @Composable get() = QadbColors.divider
    val Success: Color @Composable get() = QadbColors.success
    val Warning: Color @Composable get() = QadbColors.warning
    val Danger: Color @Composable get() = QadbColors.danger
    val Purple: Color @Composable get() = QadbColors.purple
    val Teal: Color @Composable get() = QadbColors.teal
    val Cyan: Color @Composable get() = QadbColors.cyan
}

@Composable
private fun Modifier.homeNoRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = clickable(
    enabled = enabled,
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DevicesViewModel,
    onScreenshot: () -> Unit = {},
    onScreenRecord: () -> Unit = {},
    onInstallApk: () -> Unit = {},
    onMirrorDevice: () -> Unit = {},
    onOpenShell: () -> Unit = {},
    onOpenLogcat: () -> Unit = {},
    onOpenFileManager: () -> Unit = {},
    onOpenAppManager: () -> Unit = {},
    onOpenDiagnostics: () -> Unit = {},
    onOpenAppDetails: (String) -> Unit = {}
) {
    val devices by viewModel.devices.collectAsState()
    val deviceDisplayNames by viewModel.deviceDisplayNames.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val centerInfo by viewModel.centerInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()

    var showDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var foregroundApp by remember { mutableStateOf<HomeForegroundAppModel?>(null) }
    var foregroundError by remember { mutableStateOf<String?>(null) }
    var isForegroundLoading by remember { mutableStateOf(false) }
    var foregroundUpdatedAt by remember { mutableStateOf("--") }

    if (showDialog) {
        dialogMessage?.let { message ->
            TipDialog(
                dialogText = when (message) {
                    is MsgContent.Resource -> stringResource(message.stringResource, *message.args.toTypedArray())
                    is MsgContent.Text -> message.text
                }
            ) { viewModel.dismissTipDialog() }
        }
    }

    LaunchedEffect(Unit) {
        if (devices.isEmpty()) {
            viewModel.refreshDevices()
        }
    }

    // 首页可见期间，CPU 使用率每 5 秒轮询刷新一次
    DisposableEffect(selectedDevice) {
        viewModel.startCpuPolling(selectedDevice)
        onDispose { viewModel.stopCpuPolling() }
    }

    val isConnected = homeDeviceConnected(selectedDevice)
    val wirelessConnection = isWirelessAdbConnection(selectedDevice, deviceInfo?.ipAddress)
    val connectionType = stringResource(
        if (wirelessConnection) Res.string.wireless_connection else Res.string.usb_connection
    )
    val connectionStatus = stringResource(
        if (isConnected) Res.string.online else Res.string.offline
    )
    val noConnectedDeviceMessage = l10n("没有连接设备", "No connected device")
    val foregroundUnavailableMessage = l10n("未读取到当前应用", "Current app not found")

    suspend fun loadForegroundAppForDevice(deviceId: String) {
        isForegroundLoading = true
        foregroundError = null
        val result = withContext(Dispatchers.IO) {
            AdbTool.execShellAsync("dumpsys window | grep mCurrentFocus", deviceId)
        }
        if (result.success) {
            val parsedApp = parseForegroundAppFocus(result.output)
            foregroundApp = parsedApp
            foregroundError = if (parsedApp == null) foregroundUnavailableMessage else null
            foregroundUpdatedAt = formatRelativeRefresh(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()))
        } else {
            foregroundApp = null
            foregroundError = result.errorMessage ?: result.output.ifBlank { foregroundUnavailableMessage }
        }
        isForegroundLoading = false
    }

    fun refreshForegroundApp() {
        val deviceId = selectedDevice
        if (deviceId.isNullOrBlank()) {
            foregroundApp = null
            foregroundError = noConnectedDeviceMessage
            return
        }
        coroutineScope.launch {
            loadForegroundAppForDevice(deviceId)
        }
    }

    LaunchedEffect(selectedDevice) {
        val deviceId = selectedDevice
        if (deviceId.isNullOrBlank()) {
            foregroundApp = null
            foregroundError = null
            foregroundUpdatedAt = "--"
        } else {
            loadForegroundAppForDevice(deviceId)
        }
    }

    val batterySupporting = centerInfo?.batteryStatus?.let { stringResource(it.stringResource) }.orEmpty()
    val batteryValue = buildInlineMetricValue(
        primary = centerInfo?.batteryLevel.orDash(),
        suffix = batterySupporting
    )
    val batteryProgress = parsePercentProgress(centerInfo?.batteryLevel)
    val batteryAccent = if ((batteryProgress ?: 1f) <= 0.2f) HomeVisualTokens.Warning else HomeVisualTokens.Success
    val headerBatteryInfo = centerInfo?.batteryLevel.orDash()
    val selectedDeviceLabel = selectedDevice
        ?.let { formatPrimaryDeviceName(deviceInfo?.deviceModel, deviceDisplayNames[it], it) }
        ?: stringResource(Res.string.no_device)
    val androidHeadline = formatAndroidVersionWithApi(deviceInfo?.androidVersion, deviceInfo?.sdkVersion)
    val relativeUpdated = formatRelativeRefresh(lastRefreshTime)
    val trackedOpenShell = { onOpenShell() }
    val trackedInstallApk = { onInstallApk() }
    val trackedScreenshot = { onScreenshot() }
    val trackedScreenRecord = { onScreenRecord() }
    val trackedMirrorDevice = { onMirrorDevice() }
    val trackedOpenLogcat = { onOpenLogcat() }
    val trackedOpenFileManager = { onOpenFileManager() }
    val trackedOpenAppManager = { onOpenAppManager() }
    val trackedOpenDiagnostics = { onOpenDiagnostics() }
    val statusCards = listOf(
        HomeStatusCardModel(
            title = stringResource(Res.string.cpu_usage),
            value = centerInfo?.cpuUsage.orDash(),
            supporting = l10n("当前 CPU 使用率", "Current CPU usage"),
            icon = IconParkIcons.Cpu,
            accentColor = HomeVisualTokens.Purple,
            progress = parsePercentProgress(centerInfo?.cpuUsage)
        ),
        HomeStatusCardModel(
            title = stringResource(Res.string.storage_usage),
            value = centerInfo?.storageUsage.orDash("-- / --"),
            supporting = formatStorageSupporting(centerInfo?.storageUsage),
            icon = IconParkIcons.HardDisk,
            accentColor = HomeVisualTokens.Teal,
            progress = parseStorageProgress(centerInfo?.storageUsage)
        ),
        HomeStatusCardModel(
            title = stringResource(Res.string.memory_usage),
            value = centerInfo?.memoryUsage.orDash(),
            supporting = formatMemorySupporting(centerInfo?.memoryUsage),
            icon = IconParkIcons.StorageCard,
            accentColor = HomeVisualTokens.Cyan,
            progress = parsePercentProgress(centerInfo?.memoryUsage)
        ),
        HomeStatusCardModel(
            title = stringResource(Res.string.battery_status),
            value = centerInfo?.batteryLevel.orDash(),
            supporting = batterySupporting.ifBlank { l10n("状态未采集", "Status not collected") },
            icon = IconParkIcons.BatteryFull,
            accentColor = batteryAccent,
            progress = batteryProgress
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val compactLayout = maxWidth < 760.dp
        val mediumLayout = maxWidth < 1080.dp
        val spacing = when {
            compactLayout -> 12.dp
            mediumLayout -> 10.dp
            else -> 16.dp
        }
        val horizontalPadding = when {
            compactLayout -> 14.dp
            mediumLayout -> 16.dp
            else -> 16.dp
        }
        val verticalPadding = if (mediumLayout) 12.dp else 18.dp
        val topPadding = UiTokens.SpaceXSmall
        val scrollBarEndPadding = 0.dp
        val scrollContentEndPadding = if (compactLayout) 22.dp else 28.dp

        if (!isConnected) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding + scrollContentEndPadding,
                            top = topPadding,
                            bottom = verticalPadding
                        )
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    HomeNetworkConnectCard(
                        isLoading = isLoading,
                        onConnect = { ip, port -> viewModel.connectNetworkDevice(ip, port) }
                    )
                    EmptyDeviceStatePanel(
                        compactLayout = compactLayout,
                        relativeUpdated = relativeUpdated,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshDevices() }
                    )
                }

                if (scrollState.maxValue > 0) {
                    VerticalScrollbar(
                        adapter = rememberScrollbarAdapter(scrollState),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = scrollBarEndPadding, top = UiTokens.SpaceSmall, bottom = UiTokens.SpaceSmall)
                            .width(8.dp)
                            .fillMaxHeight()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 0.dp,
                                end = scrollContentEndPadding
                            )
                            .padding(top = topPadding, bottom = spacing)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                    HomeNetworkConnectCard(
                        isLoading = isLoading,
                        onConnect = { ip, port -> viewModel.connectNetworkDevice(ip, port) }
                    )
                    HomeTopCommandPanel(
                        compactLayout = compactLayout,
                        denseLayout = mediumLayout,
                        deviceName = selectedDeviceLabel,
                        androidVersion = androidHeadline,
                        connectionType = connectionType,
                        batteryInfo = headerBatteryInfo,
                        isConnected = true,
                        isLoading = isLoading,
                        devices = devices,
                        deviceDisplayNames = deviceDisplayNames,
                        selectorExpanded = showDropdown,
                        onSelectorExpandedChange = { showDropdown = it },
                        onDeviceSelected = { viewModel.selectDevice(it) },
                        onRefresh = { viewModel.refreshDevices() },
                        onDisconnect = { viewModel.disconnectSelectedDevice() }
                    )

                    HomeStatusCardsSection(
                        cards = statusCards,
                        compactLayout = compactLayout,
                        denseLayout = mediumLayout
                    )

                    if (compactLayout) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
	                            HomeQuickActionsSection(
	                                modifier = Modifier.fillMaxWidth(),
	                                compactLayout = true,
	                                onInstallApk = trackedInstallApk,
	                                onScreenshot = trackedScreenshot,
	                                onScreenRecord = trackedScreenRecord,
	                                onOpenFileManager = trackedOpenFileManager,
	                                onOpenAppManager = trackedOpenAppManager,
	                                onMirrorDevice = trackedMirrorDevice,
	                            )
                            HomeDeviceInfoSummarySection(
                                compactLayout = true,
                                deviceInfo = deviceInfo,
                                connectionType = connectionType,
                                ipAddress = deviceInfo?.ipAddress.orDash()
                            )
                            HomeForegroundAppSection(
                                compactLayout = true,
                                foregroundApp = foregroundApp,
                                isLoading = isForegroundLoading,
                                errorMessage = foregroundError,
                                lastUpdated = foregroundUpdatedAt,
                                onRefresh = { refreshForegroundApp() },
                                onOpenDetails = onOpenAppDetails
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.36f),
                                    verticalArrangement = Arrangement.spacedBy(spacing)
                                ) {
                                    HomeQuickActionsSection(
                                        modifier = Modifier.fillMaxWidth(),
                                        compactLayout = mediumLayout,
                                        onInstallApk = trackedInstallApk,
                                        onScreenshot = trackedScreenshot,
                                        onScreenRecord = trackedScreenRecord,
                                        onOpenFileManager = trackedOpenFileManager,
                                        onOpenAppManager = trackedOpenAppManager,
                                        onMirrorDevice = trackedMirrorDevice,
                                    )
                                    HomeForegroundAppSection(
                                        modifier = Modifier.fillMaxWidth(),
                                        compactLayout = mediumLayout,
                                        foregroundApp = foregroundApp,
                                        isLoading = isForegroundLoading,
                                        errorMessage = foregroundError,
                                        lastUpdated = foregroundUpdatedAt,
                                        onRefresh = { refreshForegroundApp() },
                                        onOpenDetails = onOpenAppDetails
                                    )
                                }
                                HomeDeviceInfoSummarySection(
                                    modifier = Modifier.weight(1f),
                                    compactLayout = mediumLayout,
                                    deviceInfo = deviceInfo,
                                    connectionType = connectionType,
                                    ipAddress = deviceInfo?.ipAddress.orDash()
                                )
                            }
                        }
                    }
                }

                    if (scrollState.maxValue > 0) {
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = scrollBarEndPadding, top = UiTokens.SpaceSmall, bottom = UiTokens.SpaceSmall)
                                .width(8.dp)
                                .fillMaxHeight()
                        )
                    }
                }

                HomeBottomStatusBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing),
                    connectionType = connectionType,
                    ipAddress = deviceInfo?.ipAddress.orDash(),
                    port = extractPort(selectedDevice, deviceInfo?.ipAddress),
                    latency = deviceInfo?.latency.orDash(),
                    relativeUpdated = relativeUpdated
                )
            }
        }
    }
}

@Composable
private fun HomeNetworkConnectCard(
    isLoading: Boolean,
    onConnect: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val saved = remember { NetworkConnectMemory.load() }
    var ip by remember { mutableStateOf(saved.ip) }
    var port by remember { mutableStateOf(saved.port) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        borderStroke = BorderStroke(1.dp, HomeVisualTokens.Border.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiTokens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Icon(IconParkIcons.Link, null, tint = HomeVisualTokens.Primary, modifier = Modifier.size(18.dp))
                Text(
                    text = l10n("网络连接", "Network connection"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it.trim() },
                    modifier = Modifier.weight(1.4f),
                    singleLine = true,
                    placeholder = { Text(l10n("IP 地址", "IP address")) }
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter(Char::isDigit) },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    placeholder = { Text(l10n("端口", "Port")) }
                )
                PrimaryHomeButton(
                    text = if (isLoading) l10n("连接中...", "Connecting...") else l10n("连接", "Connect"),
                    onClick = {
                        if (!isLoading) {
                            NetworkConnectMemory.save(ip, port)
                            onConnect(ip, port)
                        }
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = l10n("连接后自动刷新设备列表", "Device list refreshes after connecting"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyDeviceStatePanel(
    compactLayout: Boolean,
    relativeUpdated: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compactLayout) 500.dp else 520.dp),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compactLayout) 20.dp else 48.dp, vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (compactLayout) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
                ) {
                    EmptyDeviceHero(
                        compactLayout = true,
                        relativeUpdated = relativeUpdated,
                        isLoading = isLoading,
                        onRefresh = onRefresh
                    )
                    EmptyConnectGuidePanel(
                        modifier = Modifier.fillMaxWidth(),
                        onRefresh = onRefresh
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXXXLarge)
                ) {
                    EmptyDeviceHero(
                        modifier = Modifier.weight(1f),
                        compactLayout = false,
                        relativeUpdated = relativeUpdated,
                        isLoading = isLoading,
                        onRefresh = onRefresh
                    )
                    EmptyConnectGuidePanel(
                        modifier = Modifier.weight(1f),
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDeviceHero(
    compactLayout: Boolean,
    relativeUpdated: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
    ) {
        EmptyDeviceIllustration()
        EmptyDeviceIdentity(relativeUpdated = relativeUpdated)
        EmptyDeviceActions(
            compactLayout = compactLayout,
            isLoading = isLoading,
            onRefresh = onRefresh
        )
    }
}

@Composable
private fun EmptyDeviceIdentity(
    relativeUpdated: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Text(
                text = stringResource(Res.string.no_device),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ConnectionStatusBadge(
                text = stringResource(Res.string.disconnected),
                active = false
            )
        }
        Text(
            text = stringResource(Res.string.select_device_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        DeviceMetaChip(
            text = stringResource(Res.string.updated_ago, relativeUpdated),
            icon = IconParkIcons.Refresh
        )
    }
}

@Composable
private fun EmptyDeviceIllustration() {
    val outerShape = RoundedCornerShape(UiTokens.RadiusLarge)
    val innerShape = RoundedCornerShape(UiTokens.RadiusMedium)
    Box(
        modifier = Modifier
            .size(92.dp)
            .background(HomeVisualTokens.Soft, outerShape)
            .border(1.dp, HomeVisualTokens.Border, outerShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surface, innerShape)
                .border(1.dp, HomeVisualTokens.Border, innerShape),
            contentAlignment = Alignment.Center
        ) {
            OpenSourceDeviceIcon(active = false)
        }
    }
}

@Composable
private fun EmptyDeviceActions(
    compactLayout: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    if (compactLayout) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
            EmptyDeviceActionButton(
                text = stringResource(Res.string.refresh),
                icon = IconParkIcons.Refresh,
                primary = true,
                enabled = !isLoading,
                onClick = onRefresh,
                modifier = Modifier.width(220.dp)
            )
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
            EmptyDeviceActionButton(
                text = stringResource(Res.string.refresh),
                icon = IconParkIcons.Refresh,
                primary = true,
                enabled = !isLoading,
                onClick = onRefresh,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
private fun EmptyDeviceActionButton(
    text: String,
    icon: ImageVector,
    primary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        primary && enabled -> MaterialTheme.colorScheme.primary
        primary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
    }
    val borderColor = if (primary) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.18f else 0.10f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f)
    }
    val contentColor = if (primary) QadbColors.onPrimary else MaterialTheme.colorScheme.onSurface
    val iconColor = if (primary) QadbColors.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .height(UiTokens.ControlHeight)
            .background(containerColor, RoundedCornerShape(UiTokens.RadiusLarge))
            .border(1.dp, borderColor, RoundedCornerShape(UiTokens.RadiusLarge))
            .homeNoRippleClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = UiTokens.SpaceLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier.size(UiTokens.IconSmall)
        )
        Spacer(modifier = Modifier.width(UiTokens.SpaceSmall))
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyConnectGuidePanel(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f), RoundedCornerShape(UiTokens.RadiusLarge))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(UiTokens.RadiusLarge))
            .padding(UiTokens.SpaceLarge),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.RadiusMedium))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), RoundedCornerShape(UiTokens.RadiusMedium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconParkIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = stringResource(Res.string.home_connect_guide_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        EmptyConnectStep(
            index = "1",
            title = stringResource(Res.string.home_connect_step_connect_title),
            description = stringResource(Res.string.home_connect_step_connect_desc)
        )
        EmptyConnectStep(
            index = "2",
            title = stringResource(Res.string.home_connect_step_authorize_title),
            description = stringResource(Res.string.home_connect_step_authorize_desc)
        )
        EmptyConnectStep(
            index = "3",
            title = stringResource(Res.string.home_connect_step_refresh_title),
            description = stringResource(Res.string.home_connect_step_refresh_desc)
        )

        EmptyDeviceRefreshHint(onRefresh = onRefresh)
    }
}

@Composable
private fun EmptyConnectStep(
    index: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        Box(
            modifier = Modifier
                .size(UiTokens.IconLarge)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.BadgeRadius))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), RoundedCornerShape(UiTokens.BadgeRadius)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyDeviceRefreshHint(
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.RadiusLarge))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(UiTokens.RadiusLarge))
            .homeNoRippleClickable(onClick = onRefresh)
            .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Icon(
            imageVector = IconParkIcons.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = stringResource(Res.string.home_no_device_alert_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(Res.string.refresh),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopCommandPanel(
    compactLayout: Boolean,
    denseLayout: Boolean,
    deviceName: String,
    androidVersion: String,
    connectionType: String,
    batteryInfo: String,
    isConnected: Boolean,
    isLoading: Boolean,
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectorExpanded: Boolean,
    onSelectorExpandedChange: (Boolean) -> Unit,
    onDeviceSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (denseLayout) 14.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        if (compactLayout) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
            ) {
                HomeHeaderDeviceIdentity(
                    compactLayout = true,
                    denseLayout = denseLayout,
                    deviceName = deviceName,
                    androidVersion = androidVersion,
                    connectionType = connectionType,
                    batteryInfo = batteryInfo,
                    isConnected = isConnected,
                    devices = devices,
                    deviceDisplayNames = deviceDisplayNames,
                    selectorExpanded = selectorExpanded,
                    onSelectorExpandedChange = onSelectorExpandedChange,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                HomeHeaderDeviceActions(
                    isLoading = isLoading,
                    isConnected = isConnected,
                    onRefresh = onRefresh,
                    onDisconnect = onDisconnect,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 16.dp else 24.dp)
            ) {
                HomeHeaderDeviceIdentity(
                    compactLayout = false,
                    denseLayout = denseLayout,
                    deviceName = deviceName,
                    androidVersion = androidVersion,
                    connectionType = connectionType,
                    batteryInfo = batteryInfo,
                    isConnected = isConnected,
                    devices = devices,
                    deviceDisplayNames = deviceDisplayNames,
                    selectorExpanded = selectorExpanded,
                    onSelectorExpandedChange = onSelectorExpandedChange,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier.weight(1f)
                )
                HomeHeaderDeviceActions(
                    isLoading = isLoading,
                    isConnected = isConnected,
                    onRefresh = onRefresh,
                    onDisconnect = onDisconnect
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderDeviceActions(
    isLoading: Boolean,
    isConnected: Boolean,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeHeaderActionButton(
            icon = IconParkIcons.Refresh,
            label = stringResource(Res.string.refresh),
            tint = HomeVisualTokens.Primary,
            enabled = !isLoading,
            onClick = onRefresh
        )
        HomeHeaderActionButton(
            icon = IconParkIcons.LinkBreak,
            label = stringResource(Res.string.disconnect),
            tint = HomeVisualTokens.Danger,
            enabled = isConnected,
            onClick = onDisconnect
        )
    }
}

@Composable
private fun HomeHeaderActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val effectiveTint = if (enabled) tint else HomeVisualTokens.Muted.copy(alpha = 0.45f)
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
    ) {
        Box(
            modifier = Modifier
            .size(36.dp)
            .background(
                if (enabled && tint == HomeVisualTokens.Primary) HomeVisualTokens.Primary.copy(alpha = 0.08f)
                else HomeVisualTokens.Soft,
                shape
            )
            .border(1.dp, HomeVisualTokens.Border, shape)
            .homeNoRippleClickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = effectiveTint,
                modifier = Modifier.size(UiTokens.IconSmall)
            )
        }
        Text(
            text = label,
            color = HomeVisualTokens.Text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeHeaderDeviceIdentity(
    compactLayout: Boolean,
    denseLayout: Boolean,
    deviceName: String,
    androidVersion: String,
    connectionType: String,
    batteryInfo: String,
    isConnected: Boolean,
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectorExpanded: Boolean,
    onSelectorExpandedChange: (Boolean) -> Unit,
    onDeviceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .homeNoRippleClickable(enabled = devices.isNotEmpty()) { onSelectorExpandedChange(!selectorExpanded) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 14.dp else 16.dp)
        ) {
            OpenSourceDeviceIcon(active = isConnected, dense = denseLayout)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (denseLayout) 7.dp else 9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Text(
                        text = deviceName,
                        style = if (denseLayout) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ConnectionStatusBadge(
                        text = stringResource(if (isConnected) Res.string.connected else Res.string.disconnected),
                        active = isConnected
                    )
                }
                val androidDisplay = when {
                    androidVersion.isBlank() || androidVersion == "--" -> androidVersion
                    androidVersion.startsWith("Android", ignoreCase = true) -> androidVersion
                    else -> "Android $androidVersion"
                }
                HomeHeaderMetaLine(
                    compactLayout = compactLayout,
                    items = listOf(
                        HomeHeaderMeta(androidDisplay, IconParkIcons.Phone),
                        HomeHeaderMeta(connectionType, IconParkIcons.Link),
                        HomeHeaderMeta(batteryInfo, IconParkIcons.BatteryFull)
                    )
                )
            }
            if (!compactLayout) {
                Icon(
                    imageVector = IconParkIcons.ArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = selectorExpanded,
            onDismissRequest = { onSelectorExpandedChange(false) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            if (devices.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.no_device_available)) },
                    onClick = { onSelectorExpandedChange(false) },
                    enabled = false
                )
            } else {
                devices.forEach { deviceId ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = formatDeviceDisplay(deviceId, deviceDisplayNames[deviceId]),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onDeviceSelected(deviceId)
                            onSelectorExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderMetaLine(
    compactLayout: Boolean,
    items: List<HomeHeaderMeta>
) {
    val visibleItems = items.filter { it.text.isNotBlank() && it.text != "--" }
    if (compactLayout) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
            visibleItems.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
                    rowItems.forEach { item -> HomeHeaderMetaItem(item) }
                }
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            visibleItems.forEach { item -> HomeHeaderMetaItem(item) }
        }
    }
}

@Composable
private fun HomeHeaderMetaItem(item: HomeHeaderMeta) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = QadbColors.textSecondary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = item.text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OpenSourceDeviceIcon(active: Boolean = true, dense: Boolean = false) {
    val accent = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundAlpha = if (active) 0.14f else 0.08f
    val borderAlpha = if (active) 0.16f else 0.18f
    Box(
        modifier = Modifier
            .size(if (dense) 48.dp else 56.dp)
            .background(accent.copy(alpha = backgroundAlpha), RoundedCornerShape(if (dense) 16.dp else 18.dp))
            .border(1.dp, accent.copy(alpha = borderAlpha), RoundedCornerShape(if (dense) 16.dp else 18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconParkIcons.Phone,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(if (dense) 28.dp else 32.dp)
        )
    }
}

@Composable
private fun HomeCommandEntry(
    modifier: Modifier = Modifier,
    compactLayout: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(if (compactLayout) 52.dp else 64.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.RadiusLarge))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(UiTokens.RadiusLarge))
            .homeNoRippleClickable(onClick = onClick)
            .padding(horizontal = UiTokens.SpaceLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Icon(
            imageVector = IconParkIcons.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = stringResource(Res.string.home_command_palette_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f), RoundedCornerShape(UiTokens.RadiusMedium))
                .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌘K",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HomeStatusOverviewPanel(
    modifier: Modifier = Modifier,
    compactLayout: Boolean,
    deviceInfo: DeviceInfoData?,
    deviceName: String,
    selectedDevice: String?,
    connectionType: String,
    connectedDuration: String,
    relativeUpdated: String
) {
    HomePlainSection(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compactLayout) 14.dp else 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            HomeSectionTitle(text = stringResource(Res.string.home_device_summary))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HomeVisualTokens.Divider)
            )

            val leftRows = listOf(
                stringResource(Res.string.device_model) to deviceInfo?.deviceModel.orDash(),
                stringResource(Res.string.android_version) to formatAndroidVersionWithApi(deviceInfo?.androidVersion, deviceInfo?.sdkVersion),
                stringResource(Res.string.kernel_version) to deviceInfo?.kernelVersion.orDash(),
                stringResource(Res.string.manufacturer) to deviceInfo?.manufacturer.orDash(),
                stringResource(Res.string.screen_resolution) to deviceInfo?.screenResolution.orDash(),
                stringResource(Res.string.connected_for) to connectedDuration
            )
            val rightRows = listOf(
                stringResource(Res.string.ip_address) to deviceInfo?.ipAddress.orDash(),
                stringResource(Res.string.port) to extractPort(selectedDevice, deviceInfo?.ipAddress),
                stringResource(Res.string.connection_type) to connectionType,
                stringResource(Res.string.mac_address) to deviceInfo?.macAddress.orDash(),
                stringResource(Res.string.home_device_serial) to deviceName,
                stringResource(Res.string.last_refresh) to stringResource(Res.string.updated_ago, relativeUpdated)
            )

            if (compactLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                    (leftRows + rightRows).forEach { row ->
                        HomeCompactInfoRow(label = row.first, value = row.second)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXXLarge),
                    verticalAlignment = Alignment.Top
                ) {
                    HomeInfoColumn(
                        rows = leftRows,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(128.dp)
                            .background(HomeVisualTokens.Divider)
                    )
                    HomeInfoColumn(
                        rows = rightRows,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeForegroundAppSection(
    modifier: Modifier = Modifier,
    compactLayout: Boolean,
    foregroundApp: HomeForegroundAppModel?,
    isLoading: Boolean,
    errorMessage: String?,
    lastUpdated: String,
    onRefresh: () -> Unit,
    onOpenDetails: (String) -> Unit
) {
    val detailsPackageName = foregroundApp?.packageName?.takeIf { it.isNotBlank() && it != "--" }
    HomePlainSection(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compactLayout) 14.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeSectionTitle(text = l10n("当前打开的应用", "Current Open App"))
                Spacer(modifier = Modifier.weight(1f))
                HomeRefreshIconButton(
                    enabled = !isLoading,
                    onClick = onRefresh
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HomeVisualTokens.Divider)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactLayout) 116.dp else 104.dp)
                    .homeNoRippleClickable(enabled = detailsPackageName != null) {
                        detailsPackageName?.let(onOpenDetails)
                    },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(if (compactLayout) 12.dp else 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compactLayout) 54.dp else 50.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f), RoundedCornerShape(UiTokens.RadiusLarge)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconParkIcons.Application,
                        contentDescription = null,
                        tint = QadbColors.textDisabled,
                        modifier = Modifier.size(if (compactLayout) 26.dp else 24.dp)
                    )
                }

                SelectionContainer(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        val title = when {
                            isLoading -> l10n("正在读取当前应用", "Reading current app")
                            foregroundApp != null -> foregroundApp.appName
                            else -> l10n("暂未读取到应用信息", "App information not loaded")
                        }
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (foregroundApp != null) {
                            foregroundApp.rows().drop(1).take(2).forEach { (label, value) ->
                                LabeledValueRow(
                                    label = label,
                                    value = value
                                )
                            }
                        } else {
                            Text(
                                text = errorMessage ?: l10n("设备连接后会自动读取当前打开的应用。", "The current app is read once after the device connects."),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (lastUpdated != "--") {
                            Text(
                                text = stringResource(Res.string.updated_ago, lastUpdated),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (detailsPackageName != null) {
                    Icon(
                        imageVector = IconParkIcons.Right,
                        contentDescription = null,
                        tint = HomeVisualTokens.Muted,
                        modifier = Modifier
                            .padding(top = UiTokens.SpaceXSmall)
                            .size(UiTokens.IconSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeRefreshIconButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val rotationTransition = rememberInfiniteTransition()
    val loadingRotation by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val iconRotation = if (enabled) 0f else loadingRotation

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.RadiusMedium))
            .border(1.dp, QadbColors.border, RoundedCornerShape(UiTokens.RadiusMedium))
            .homeNoRippleClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconParkIcons.Refresh,
            contentDescription = stringResource(Res.string.refresh),
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier
                .size(UiTokens.IconSmall)
                .rotate(iconRotation)
        )
    }
}

@Composable
private fun PrimaryHomeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(UiTokens.RadiusMedium))
            .homeNoRippleClickable(onClick = onClick)
            .padding(horizontal = UiTokens.SpaceSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SecondaryHomeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(UiTokens.RadiusMedium))
            .border(1.dp, QadbColors.border, RoundedCornerShape(UiTokens.RadiusMedium))
            .homeNoRippleClickable(onClick = onClick)
            .padding(horizontal = UiTokens.SpaceSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeStatusCardsSection(
    cards: List<HomeStatusCardModel>,
    compactLayout: Boolean,
    denseLayout: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (compactLayout) {
            val rows = cards.chunked(2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = UiTokens.SpaceMedium),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                rows.forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        rowCards.forEach { card ->
                            HomeStatusCard(
                                card = card,
                                denseLayout = denseLayout,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCards.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 10.dp else 12.dp)
            ) {
                cards.forEach { card ->
                    HomeStatusCard(
                        card = card,
                        denseLayout = denseLayout,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStatusCard(
    card: HomeStatusCardModel,
    denseLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(UiTokens.RadiusLarge)
    Row(
        modifier = modifier
            .height(if (denseLayout) 86.dp else 92.dp)
            .background(MaterialTheme.colorScheme.surface, cardShape)
            .border(1.dp, HomeVisualTokens.Border, cardShape)
            .padding(horizontal = if (denseLayout) 12.dp else 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 9.dp else 11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (denseLayout) 32.dp else 34.dp)
                .background(HomeVisualTokens.Soft, RoundedCornerShape(UiTokens.RadiusMedium)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.title,
                tint = card.accentColor,
                modifier = Modifier.size(if (denseLayout) 18.dp else 19.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Text(
                text = card.title,
                color = HomeVisualTokens.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.value,
                color = HomeVisualTokens.Text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.supporting.ifBlank { "--" },
                color = HomeVisualTokens.Muted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HomeStatusUsageChart(
            progress = card.progress,
            accentColor = card.accentColor,
            chartSize = if (denseLayout) 42.dp else 46.dp
        )
    }
}

@Composable
private fun HomeStatusUsageChart(
    progress: Float?,
    accentColor: Color,
    chartSize: Dp
) {
    val normalizedProgress = progress?.coerceIn(0f, 1f)
    val trackColor = HomeVisualTokens.Soft
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedProgress ?: 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
    )
    val percentText = normalizedProgress
        ?.let { "${(it * 100).roundToInt()}%" }
        ?: "--"

    Box(
        modifier = Modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth
            )
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = percentText,
            color = HomeVisualTokens.Text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeMetricsStripPanel(
    metricItems: List<HomeMetricModel>,
    compactLayout: Boolean,
    denseLayout: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(QadbColors.divider))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compactLayout) 14.dp else 16.dp,
                    vertical = if (denseLayout) 12.dp else 13.dp
                )
        ) {
            HomeMetricOverviewRow(
                items = metricItems,
                compactLayout = compactLayout
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(QadbColors.divider))
    }
}

@Composable
private fun HomeMetricOverviewRow(
    items: List<HomeMetricModel>,
    compactLayout: Boolean
) {
    if (compactLayout) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    rowItems.forEach { item ->
                        HomeMetricOverviewCell(
                            modifier = Modifier.weight(1f),
                            item = item
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                HomeMetricOverviewCell(
                    modifier = Modifier.weight(1f),
                    item = item
                )
                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(54.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeMetricOverviewCell(
    item: HomeMetricModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(UiTokens.IconMedium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = QadbColors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = stringResource(item.titleKey),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = item.value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.supporting.ifBlank { "--" },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HomeStatusUsageChart(
            progress = item.progress,
            accentColor = item.accentColor,
            chartSize = 44.dp
        )
    }
}

@Composable
private fun HomePlainSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val sectionShape = RoundedCornerShape(UiTokens.RadiusLarge)
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, sectionShape)
            .border(1.dp, HomeVisualTokens.Border, sectionShape)
    ) {
        content()
    }
}

@Composable
private fun HomeSectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun HomeInfoColumn(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        rows.forEach { row ->
            HomeCompactInfoRow(label = row.first, value = row.second)
        }
    }
}

@Composable
private fun HomeCompactInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.72f)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceHeroPanel(
    compactLayout: Boolean,
    denseLayout: Boolean,
    deviceName: String,
    androidVersion: String,
    connectionType: String,
    connectionStatus: String,
    batteryInfo: String,
    screenResolution: String,
    selectedDeviceAddress: String,
    ipAddress: String,
    relativeUpdated: String,
    isConnected: Boolean,
    isLoading: Boolean,
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectorExpanded: Boolean,
    onSelectorExpandedChange: (Boolean) -> Unit,
    onDeviceSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenShell: () -> Unit,
    onInstallApk: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenLogcat: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        borderStroke = BorderStroke(1.dp, QadbColors.selectedBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = when {
                        compactLayout -> UiTokens.SpaceLarge
                        denseLayout -> UiTokens.SpaceLarge
                        else -> UiTokens.SpaceXLarge
                    },
                    vertical = if (denseLayout) 14.dp else 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(if (denseLayout) 12.dp else 16.dp)
        ) {
            if (compactLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
                    DeviceHeroIdentity(
                        compactLayout = compactLayout,
                        denseLayout = denseLayout,
                        deviceName = deviceName,
                        androidVersion = androidVersion,
                        connectionType = connectionType,
                        batteryInfo = batteryInfo,
                        screenResolution = screenResolution,
                        isConnected = isConnected
                    )
                    HomeToolbarActionButton(
                        text = stringResource(Res.string.refresh),
                        icon = IconParkIcons.Refresh,
                        tint = MaterialTheme.colorScheme.primary,
                        enabled = !isLoading,
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXXLarge)
                ) {
                    DeviceHeroIdentity(
                        modifier = Modifier.weight(1f),
                        compactLayout = compactLayout,
                        denseLayout = denseLayout,
                        deviceName = deviceName,
                        androidVersion = androidVersion,
                        connectionType = connectionType,
                        batteryInfo = batteryInfo,
                        screenResolution = screenResolution,
                        isConnected = isConnected
                    )
                    HomeToolbarActionButton(
                        text = stringResource(Res.string.refresh),
                        icon = IconParkIcons.Refresh,
                        tint = MaterialTheme.colorScheme.onSurface,
                        enabled = !isLoading,
                        onClick = onRefresh,
                        modifier = Modifier.width(if (denseLayout) 116.dp else 124.dp)
                    )
                }
            }

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, RoundedCornerShape(UiTokens.RadiusMedium))
                        .homeNoRippleClickable(enabled = devices.isNotEmpty()) { onSelectorExpandedChange(!selectorExpanded) }
                        .padding(horizontal = if (compactLayout) 2.dp else if (denseLayout) 96.dp else 116.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
                    ) {
                        Text(
                            text = selectedDeviceAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOf(
                                connectionStatus,
                                stringResource(Res.string.updated_ago, relativeUpdated),
                                ipAddress.takeIf { it != selectedDeviceAddress }.orEmpty()
                            ).filter { it.isNotBlank() && it != "--" }.joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isConnected) QadbColors.success else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(UiTokens.BadgeRadius)
                            )
                    )
                    Icon(
                        imageVector = IconParkIcons.ArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = selectorExpanded,
                    onDismissRequest = { onSelectorExpandedChange(false) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    if (devices.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.no_device_available)) },
                            onClick = { onSelectorExpandedChange(false) },
                            enabled = false
                        )
                    } else {
                        devices.forEach { deviceId ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = formatDeviceDisplay(deviceId, deviceDisplayNames[deviceId]),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    onDeviceSelected(deviceId)
                                    onSelectorExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }

            HomeToolbarActions(
                compactLayout = compactLayout,
                enabled = true,
                onOpenShell = onOpenShell,
                onInstallApk = onInstallApk,
                onScreenshot = onScreenshot,
                onOpenLogcat = onOpenLogcat
            )
        }
    }
}

@Composable
private fun DeviceHeroIdentity(
    compactLayout: Boolean,
    denseLayout: Boolean,
    deviceName: String,
    androidVersion: String,
    connectionType: String,
    batteryInfo: String,
    screenResolution: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 16.dp else 18.dp)
    ) {
        OpenSourceDeviceIcon(active = isConnected, dense = denseLayout)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(if (denseLayout) 8.dp else 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Text(
                    text = deviceName,
                    style = if (denseLayout) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ConnectionStatusBadge(
                    text = stringResource(if (isConnected) Res.string.connected else Res.string.disconnected),
                    active = isConnected
                )
            }
            val chips = listOf<@Composable () -> Unit>(
                { DeviceMetaChip(text = connectionType, icon = IconParkIcons.Link) },
                { DeviceMetaChip(text = androidVersion, icon = IconParkIcons.Phone, tint = QadbColors.success) },
                { DeviceMetaChip(text = screenResolution, icon = IconParkIcons.HardDisk) },
                { DeviceMetaChip(text = batteryInfo, icon = IconParkIcons.BatteryFull, tint = QadbColors.textSecondary) }
            )
            if (compactLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                    chips.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
                            rowItems.forEach { chip -> chip() }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (denseLayout) 14.dp else 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    chips.forEach { chip -> chip() }
                }
            }
        }
    }
}

@Composable
private fun HomeToolbarActions(
    compactLayout: Boolean,
    enabled: Boolean,
    onOpenShell: () -> Unit,
    onInstallApk: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenLogcat: () -> Unit
) {
    val actions = listOf(
        HomeToolbarAction(stringResource(Res.string.install_apk_short), IconParkIcons.Download, HomeVisualTokens.Primary, onInstallApk),
        HomeToolbarAction(stringResource(Res.string.key_screenshot_short), IconParkIcons.Camera, HomeVisualTokens.Cyan, onScreenshot),
        HomeToolbarAction(stringResource(Res.string.shell_command), IconParkIcons.Terminal, HomeVisualTokens.Purple, onOpenShell),
        HomeToolbarAction(stringResource(Res.string.terminal_tab_logcat), IconParkIcons.Info, HomeVisualTokens.Warning, onOpenLogcat)
    )

    if (compactLayout) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
            actions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    rowItems.forEach { action ->
                        HomeToolbarActionButton(
                            text = action.title,
                            icon = action.icon,
                            tint = action.tint,
                            enabled = enabled,
                            onClick = action.onClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            actions.forEach { action ->
                HomeToolbarActionButton(
                    text = action.title,
                    icon = action.icon,
                    tint = action.tint,
                    enabled = enabled,
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NoDevicePanel() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = UiTokens.SpaceXXXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Icon(
                imageVector = IconParkIcons.Phone,
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = stringResource(Res.string.no_device_available),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.select_device_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun DeviceSummaryPanel(
    modifier: Modifier = Modifier,
    deviceInfo: DeviceInfoData?,
    connectionType: String,
    connectedDuration: String,
    denseLayout: Boolean = false
) {
    DashboardPanel(
        modifier = modifier,
        title = stringResource(Res.string.device_overview),
        icon = IconParkIcons.Info
    ) {
        if (deviceInfo == null) {
            Text(
                text = stringResource(Res.string.no_device_info),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            return@DashboardPanel
        }

        val rows = buildList {
            add(stringResource(Res.string.device_model) to deviceInfo.deviceModel.orDash())
            add(stringResource(Res.string.android_version) to formatAndroidVersionWithApi(deviceInfo.androidVersion, deviceInfo.sdkVersion))
            add(stringResource(Res.string.manufacturer) to deviceInfo.manufacturer.orDash())
            add(stringResource(Res.string.kernel_version) to deviceInfo.kernelVersion.orDash())
            add(stringResource(Res.string.ip_address) to deviceInfo.ipAddress.orDash())
            add(stringResource(Res.string.mac_address) to deviceInfo.macAddress.orDash())
            add(stringResource(Res.string.screen_resolution) to deviceInfo.screenResolution.orDash())
            if (!denseLayout) {
                add(stringResource(Res.string.connection_type) to connectionType)
                add(stringResource(Res.string.connected_for) to connectedDuration)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(if (denseLayout) 7.dp else 9.dp)) {
            rows.forEach { row ->
                LabeledValueRow(label = row.first, value = row.second)
            }
        }

        Spacer(modifier = Modifier.height(if (denseLayout) UiTokens.SpaceSmall else UiTokens.SpaceMedium))
        Text(
            text = stringResource(Res.string.home_view_details),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeQuickActionsSection(
    modifier: Modifier = Modifier,
    compactLayout: Boolean = false,
    onInstallApk: () -> Unit,
    onScreenshot: () -> Unit,
    onScreenRecord: () -> Unit,
    onOpenFileManager: () -> Unit,
    onOpenAppManager: () -> Unit,
    onMirrorDevice: () -> Unit
) {
    val actions = listOf(
        HomeToolbarAction(
            stringResource(Res.string.install_app),
            IconParkIcons.Download,
            HomeVisualTokens.Primary,
            onInstallApk,
            stringResource(Res.string.install_app_desc)
        ),
        HomeToolbarAction(
            stringResource(Res.string.key_screenshot_short),
            IconParkIcons.Camera,
            HomeVisualTokens.Cyan,
            onScreenshot,
            stringResource(Res.string.screenshot_desc)
        ),
        HomeToolbarAction(
            stringResource(Res.string.screen_record),
            IconParkIcons.Video,
            HomeVisualTokens.Danger,
            onScreenRecord,
            stringResource(Res.string.screen_record_desc)
        ),
        HomeToolbarAction(
            stringResource(Res.string.file_manager),
            IconParkIcons.Folder,
            HomeVisualTokens.Teal,
            onOpenFileManager,
            stringResource(Res.string.file_manager_desc)
        ),
        HomeToolbarAction(
            stringResource(Res.string.app),
            IconParkIcons.Application,
            HomeVisualTokens.Purple,
            onOpenAppManager,
            l10n("查看应用", "View apps")
        ),
        HomeToolbarAction(
            l10n("投屏控制", "Mirror Control"),
            IconParkIcons.CastScreen,
            HomeVisualTokens.Success,
            onMirrorDevice,
            l10n("投屏到电脑", "Mirror to desktop")
        )
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            HomeSectionTitle(text = l10n("常用操作", "Common Actions"))

            actions.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compactLayout) 8.dp else 12.dp)
                ) {
                    rowItems.forEach { action ->
                        HomeCompactActionCell(
                            action = action,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeDeviceInfoSummarySection(
    modifier: Modifier = Modifier,
    compactLayout: Boolean,
    deviceInfo: DeviceInfoData?,
    connectionType: String,
    ipAddress: String
) {
    val coreRows = listOf(
        HomeInfoSummaryRow(
            label = stringResource(Res.string.device_model),
            value = deviceInfo?.deviceModel.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.manufacturer),
            value = deviceInfo?.manufacturer.orDash()
        ),
        HomeInfoSummaryRow(
            label = l10n("系统版本", "System Version"),
            value = formatAndroidVersionWithApi(deviceInfo?.androidVersion, deviceInfo?.sdkVersion)
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.screen_resolution),
            value = deviceInfo?.screenResolution.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.font_scale),
            value = deviceInfo?.fontScale.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.connection_type),
            value = connectionType
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.ip_address),
            value = ipAddress
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.latency),
            value = deviceInfo?.latency.orDash()
        )
    )
    val detailRows = listOf(
        HomeInfoSummaryRow(
            label = stringResource(Res.string.rom_version),
            value = deviceInfo?.romVersion.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.kernel_version),
            value = deviceInfo?.kernelVersion.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.mac_address),
            value = deviceInfo?.macAddress.orDash()
        ),
        HomeInfoSummaryRow(
            label = stringResource(Res.string.build_fingerprint),
            value = deviceInfo?.buildFingerprint.orDash()
        )
    )

    HomePlainSection(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compactLayout) 14.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            HomeSectionTitle(text = stringResource(Res.string.device_info))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HomeVisualTokens.Divider)
            )
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(if (compactLayout) 8.dp else 9.dp)) {
                    HomeDeviceInfoGroupHeader(
                        text = l10n("核心信息", "Core"),
                        highlighted = true
                    )
                    coreRows.forEach { row ->
                        HomeDeviceInfoSummaryRow(row = row)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(HomeVisualTokens.Divider)
                    )
                    HomeDeviceInfoGroupHeader(
                        text = l10n("系统详情", "System details"),
                        highlighted = false
                    )
                    detailRows.forEach { row ->
                        HomeDeviceInfoSummaryRow(row = row)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDeviceInfoGroupHeader(
    text: String,
    highlighted: Boolean
) {
    Text(
        text = text,
        color = if (highlighted) HomeVisualTokens.Primary else HomeVisualTokens.Muted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun HomeDeviceInfoSummaryRow(row: HomeInfoSummaryRow) {
    val stacked = row.label.length > 12 || row.value.length > 34
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (stacked) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        if (stacked) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
            ) {
                Text(
                    text = row.label,
                    color = HomeVisualTokens.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = row.value,
                    color = HomeVisualTokens.Text,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = row.label,
                color = HomeVisualTokens.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.72f)
            )
            Text(
                text = row.value,
                color = HomeVisualTokens.Text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.28f)
            )
        }
    }
}

@Composable
private fun HomeCompactActionCell(
    action: HomeToolbarAction,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(UiTokens.RadiusMedium)
    Row(
        modifier = modifier
            .height(72.dp)
            .background(MaterialTheme.colorScheme.surface, cellShape)
            .border(1.dp, HomeVisualTokens.Border, cellShape)
            .homeNoRippleClickable(onClick = action.onClick)
            .padding(horizontal = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(action.tint.copy(alpha = 0.10f), RoundedCornerShape(UiTokens.RadiusMedium)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = action.tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
        ) {
            Text(
                text = action.title,
                color = HomeVisualTokens.Text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = action.description,
                color = HomeVisualTokens.Muted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeBottomStatusBar(
    modifier: Modifier = Modifier,
    connectionType: String,
    ipAddress: String,
    port: String,
    latency: String,
    relativeUpdated: String
) {
    Column(
        modifier = modifier
            .height(UiTokens.ListRowHeight)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            HomeBottomStatusItem(
                icon = IconParkIcons.Wifi,
                label = stringResource(Res.string.connected),
                value = connectionType
            )
            HomeBottomStatusItem(
                icon = IconParkIcons.Phone,
                label = "IP",
                value = ipAddress
            )
            HomeBottomStatusItem(
                icon = IconParkIcons.Terminal,
                label = l10n("端口", "Port"),
                value = port
            )
            HomeBottomStatusItem(
                icon = IconParkIcons.Speed,
                label = l10n("延迟", "Latency"),
                value = latency
            )
            HomeBottomStatusItem(
                icon = IconParkIcons.Schedule,
                label = l10n("刷新", "Refresh"),
                value = stringResource(Res.string.updated_ago, relativeUpdated)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(HomeVisualTokens.Primary.copy(alpha = 0.08f), RoundedCornerShape(UiTokens.RadiusMedium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconParkIcons.Refresh,
                    contentDescription = stringResource(Res.string.refresh),
                    tint = HomeVisualTokens.Primary,
                    modifier = Modifier.size(UiTokens.IconSmall)
                )
            }
        }
    }
}

@Composable
private fun HomeBottomStatusItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .padding(horizontal = UiTokens.SpaceXSmall, vertical = UiTokens.SpaceXSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = HomeVisualTokens.Primary.copy(alpha = 0.78f),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            color = HomeVisualTokens.Muted,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = HomeVisualTokens.Text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DiagnosticsPanel(
    codes: List<HomeDiagnosticCode>,
    compactLayout: Boolean,
    onOpenFileManager: () -> Unit,
    onOpenLogcat: () -> Unit,
    onRefresh: () -> Unit
) {
    DashboardPanel(
        title = stringResource(Res.string.home_diagnostics),
        icon = IconParkIcons.Info
    ) {
        val models = codes.map { code ->
            when (code) {
                HomeDiagnosticCode.NO_DEVICE -> HomeDiagnosticUiModel(
                    title = stringResource(Res.string.home_no_device_alert),
                    message = stringResource(Res.string.home_no_device_alert_desc),
                    actionText = stringResource(Res.string.refresh),
                    icon = IconParkIcons.Phone,
                    accentColor = QadbPalette.Slate,
                    onClick = onRefresh
                )
                HomeDiagnosticCode.STORAGE_LOW -> HomeDiagnosticUiModel(
                    title = stringResource(Res.string.home_storage_low),
                    message = stringResource(Res.string.home_storage_low_desc),
                    actionText = stringResource(Res.string.home_view_details),
                    icon = IconParkIcons.StorageCard,
                    accentColor = QadbPalette.Blue,
                    onClick = onOpenFileManager
                )
                HomeDiagnosticCode.LATENCY_HIGH -> HomeDiagnosticUiModel(
                    title = stringResource(Res.string.home_latency_high),
                    message = stringResource(Res.string.home_latency_high_desc),
                    actionText = stringResource(Res.string.home_view_details),
                    icon = IconParkIcons.Speed,
                    accentColor = QadbPalette.Orange,
                    onClick = onOpenLogcat
                )
                HomeDiagnosticCode.BATTERY_LOW -> HomeDiagnosticUiModel(
                    title = stringResource(Res.string.home_battery_low),
                    message = stringResource(Res.string.home_battery_low_desc),
                    actionText = stringResource(Res.string.refresh),
                    icon = IconParkIcons.BatteryFull,
                    accentColor = QadbPalette.Orange,
                    onClick = onRefresh
                )
                HomeDiagnosticCode.HEALTHY -> HomeDiagnosticUiModel(
                    title = stringResource(Res.string.home_device_healthy),
                    message = stringResource(Res.string.home_device_healthy_desc),
                    actionText = stringResource(Res.string.home_view_details),
                    icon = IconParkIcons.CheckCircle,
                    accentColor = QadbPalette.Green,
                    onClick = onRefresh
                )
            }
        }

        if (compactLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                models.forEach { model ->
                    DiagnosticAlertRow(
                        title = model.title,
                        message = model.message,
                        actionText = model.actionText,
                        icon = model.icon,
                        accentColor = model.accentColor,
                        onClick = model.onClick
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                models.chunked(2).forEach { rowModels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
                    ) {
                        rowModels.forEach { model ->
                            DiagnosticAlertRow(
                                modifier = Modifier.weight(1f),
                                title = model.title,
                                message = model.message,
                                actionText = model.actionText,
                                icon = model.icon,
                                accentColor = model.accentColor,
                                onClick = model.onClick
                            )
                        }
                        if (rowModels.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceControlPanel(
    compactLayout: Boolean,
    deviceName: String,
    androidVersion: String,
    connectionInfo: String,
    batteryInfo: String,
    isConnected: Boolean,
    isLoading: Boolean,
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectedDevice: String?,
    selectedDeviceLabel: String,
    selectedDeviceAddress: String,
    selectedDeviceStatus: String,
    selectorExpanded: Boolean,
    onSelectorExpandedChange: (Boolean) -> Unit,
    onDeviceSelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        borderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UiTokens.SpaceLarge, vertical = UiTokens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            if (compactLayout) {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
                    DeviceHeadline(
                        deviceName = deviceName,
                        androidVersion = androidVersion,
                        connectionInfo = connectionInfo,
                        batteryInfo = batteryInfo,
                        isConnected = isConnected
                    )
                    DeviceControlActions(
                        compactLayout = true,
                        isLoading = isLoading,
                        isConnected = isConnected,
                        onRefresh = onRefresh,
                        onDisconnect = onDisconnect
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXLarge)
                ) {
                    DeviceHeadline(
                        modifier = Modifier.weight(1f),
                        deviceName = deviceName,
                        androidVersion = androidVersion,
                        connectionInfo = connectionInfo,
                        batteryInfo = batteryInfo,
                        isConnected = isConnected
                    )
                    DeviceControlActions(
                        compactLayout = false,
                        isLoading = isLoading,
                        isConnected = isConnected,
                        onRefresh = onRefresh,
                        onDisconnect = onDisconnect
                    )
                }
            }

            DeviceSelectorCard(
                devices = devices,
                deviceDisplayNames = deviceDisplayNames,
                selectedDevice = selectedDevice,
                selectedDeviceLabel = selectedDeviceLabel,
                selectedDeviceAddress = selectedDeviceAddress,
                selectedDeviceStatus = selectedDeviceStatus,
                isConnected = isConnected,
                expanded = selectorExpanded,
                onExpandedChange = onSelectorExpandedChange,
                onDeviceSelected = onDeviceSelected
            )
        }
    }
}

@Composable
private fun DeviceControlActions(
    compactLayout: Boolean,
    isLoading: Boolean,
    isConnected: Boolean,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
        OutlineActionButton(
            modifier = if (compactLayout) Modifier.weight(1f) else Modifier,
            text = stringResource(Res.string.refresh),
            icon = IconParkIcons.Refresh,
            tint = MaterialTheme.colorScheme.primary,
            enabled = !isLoading,
            onClick = onRefresh,
            noRipple = true
        )
        OutlineActionButton(
            modifier = if (compactLayout) Modifier.weight(1f) else Modifier,
            text = stringResource(Res.string.disconnect),
            icon = IconParkIcons.Close,
            tint = MaterialTheme.colorScheme.error,
            enabled = isConnected && !isLoading,
            onClick = onDisconnect,
            noRipple = true
        )
    }
}

@Composable
private fun DeviceHeadline(
    deviceName: String,
    androidVersion: String,
    connectionInfo: String,
    batteryInfo: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Icon(
                imageVector = IconParkIcons.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = deviceName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ConnectionStatusBadge(
                text = stringResource(if (isConnected) Res.string.connected else Res.string.disconnected),
                active = isConnected
            )
        }
        Text(
            text = androidVersion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$connectionInfo · ${stringResource(Res.string.battery_level)} $batteryInfo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSelectorCard(
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectedDevice: String?,
    selectedDeviceLabel: String,
    selectedDeviceAddress: String,
    selectedDeviceStatus: String,
    isConnected: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDeviceSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Text(
            text = stringResource(Res.string.select_device),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        RoundedCornerShape(UiTokens.RadiusMedium)
                    )
                    .homeNoRippleClickable(
                        enabled = devices.isNotEmpty(),
                        onClick = { onExpandedChange(!expanded) }
                    )
                    .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(UiTokens.RadiusMedium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconParkIcons.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(UiTokens.IconSmall)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
                ) {
                    Text(
                        text = selectedDeviceLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedDeviceAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedDeviceStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Icon(
                        imageVector = IconParkIcons.ArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                if (isConnected) QadbColors.success else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(UiTokens.BadgeRadius)
                            )
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                if (devices.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.no_device_available)) },
                        onClick = { onExpandedChange(false) },
                        enabled = false
                    )
                } else {
                    devices.forEach { deviceId ->
                        val displayName = formatDeviceDisplay(deviceId, deviceDisplayNames[deviceId])
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                onDeviceSelected(deviceId)
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoPanel(
    modifier: Modifier = Modifier,
    deviceInfo: DeviceInfoData?
) {
    DashboardPanel(
        modifier = modifier,
        title = stringResource(Res.string.device_info),
        icon = IconParkIcons.Info
    ) {
        if (deviceInfo == null) {
            Text(
                text = stringResource(Res.string.no_device_info),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            return@DashboardPanel
        }

        val cellModifier = Modifier.weight(1f)
        val headlineModel = deviceInfo.deviceModel.orDash()
        val headlineSystem = listOf(
            formatAndroidVersionWithApi(deviceInfo.androidVersion, deviceInfo.sdkVersion),
            deviceInfo.romVersion.orDash(),
            deviceInfo.screenResolution.orDash()
        ).joinToString(" · ")

        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
            Text(
                text = headlineModel,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = headlineSystem,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(UiTokens.SpaceLarge))

        DeviceInfoRow(
            left = DeviceInfoEntry(
                label = stringResource(Res.string.manufacturer),
                value = deviceInfo.manufacturer.orDash()
            ),
            right = DeviceInfoEntry(
                label = stringResource(Res.string.kernel_version),
                value = deviceInfo.kernelVersion.orDash()
            ),
            cellModifier = cellModifier
        )
        Spacer(modifier = Modifier.height(UiTokens.SpaceLarge))

        DeviceInfoRow(
            left = DeviceInfoEntry(
                label = stringResource(Res.string.font_scale),
                value = deviceInfo.fontScale.orDash()
            ),
            right = DeviceInfoEntry(
                label = stringResource(Res.string.mac_address),
                value = deviceInfo.macAddress.orDash()
            ),
            cellModifier = cellModifier
        )
        Spacer(modifier = Modifier.height(UiTokens.SpaceLarge))

        DeviceInfoRow(
            left = DeviceInfoEntry(
                label = stringResource(Res.string.android_version),
                value = formatAndroidVersionWithApi(deviceInfo.androidVersion, deviceInfo.sdkVersion)
            ),
            right = DeviceInfoEntry(
                label = stringResource(Res.string.device_model),
                value = deviceInfo.deviceModel.orDash()
            ),
            cellModifier = cellModifier
        )
        Spacer(modifier = Modifier.height(UiTokens.SpaceLarge))

        DeviceInfoRow(
            left = DeviceInfoEntry(
                label = stringResource(Res.string.rom_version),
                value = deviceInfo.romVersion.orDash()
            ),
            right = DeviceInfoEntry(
                label = stringResource(Res.string.screen_resolution),
                value = deviceInfo.screenResolution.orDash()
            ),
            cellModifier = cellModifier
        )

        Spacer(modifier = Modifier.height(UiTokens.SpaceLarge))

        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                        RoundedCornerShape(UiTokens.RadiusLarge)
                    )
                    .padding(UiTokens.SpaceMedium),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Text(
                    text = stringResource(Res.string.build_fingerprint),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = deviceInfo.buildFingerprint.orDash(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ConnectionInfoPanel(
    connectionType: String,
    connectionStatus: String,
    ipAddress: String,
    port: String,
    latency: String,
    connectionSpeed: String,
    relativeUpdated: String,
    connectedDuration: String,
    isConnected: Boolean
) {
    DashboardPanel(
        title = stringResource(Res.string.connection_info),
        icon = IconParkIcons.Link
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
            LabeledValueRow(
                label = stringResource(Res.string.connection_type),
                value = connectionType,
                pillColor = MaterialTheme.colorScheme.primary
            )
            LabeledValueRow(
                label = stringResource(Res.string.device_status),
                value = connectionStatus,
                pillColor = if (isConnected) QadbColors.success else MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabeledValueRow(label = stringResource(Res.string.ip_address), value = ipAddress)
            LabeledValueRow(label = stringResource(Res.string.port), value = port)
            LabeledValueRow(
                label = stringResource(Res.string.latency),
                value = latency
            )
            LabeledValueRow(
                label = stringResource(Res.string.transfer_speed),
                value = connectionSpeed
            )
            LabeledValueRow(
                label = stringResource(Res.string.connected_for),
                value = connectedDuration
            )
            LabeledValueRow(
                label = stringResource(Res.string.last_refresh),
                value = stringResource(Res.string.updated_ago, relativeUpdated)
            )
        }
    }
}

@Composable
private fun QuickActionsPanel(
    modifier: Modifier = Modifier,
    compactLayout: Boolean = false,
    singleRow: Boolean = false,
    onScreenshot: () -> Unit = {},
    onInstallApk: () -> Unit = {},
    onMirrorDevice: () -> Unit = {},
    onOpenShell: () -> Unit = {},
    onOpenLogcat: () -> Unit = {},
    onOpenFileManager: () -> Unit = {}
) {
    DashboardPanel(
        modifier = modifier,
        title = stringResource(Res.string.quick_actions),
        icon = IconParkIcons.CheckCircle
    ) {
        val actions = listOf(
            HomeToolbarAction(stringResource(Res.string.install_apk_short), IconParkIcons.Download, HomeVisualTokens.Primary, onInstallApk),
            HomeToolbarAction(stringResource(Res.string.key_screenshot_short), IconParkIcons.Camera, HomeVisualTokens.Cyan, onScreenshot),
            HomeToolbarAction(stringResource(Res.string.shell_command), IconParkIcons.Terminal, HomeVisualTokens.Purple, onOpenShell),
            HomeToolbarAction(stringResource(Res.string.terminal_tab_logcat), IconParkIcons.Info, HomeVisualTokens.Warning, onOpenLogcat),
            HomeToolbarAction(stringResource(Res.string.file_browser), IconParkIcons.HardDisk, HomeVisualTokens.Teal, onOpenFileManager)
        )

        if (singleRow) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                actions.forEach { action ->
                    QuickActionTile(
                        modifier = Modifier.weight(1f),
                        title = action.title,
                        icon = action.icon,
                        accentColor = action.tint,
                        compact = true,
                        onClick = action.onClick
                    )
                }
            }
            return@DashboardPanel
        }

        Column(verticalArrangement = Arrangement.spacedBy(if (compactLayout) 10.dp else 12.dp)) {
            actions.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compactLayout) 10.dp else 12.dp)
                ) {
                    rowItems.forEach { action ->
                        QuickActionTile(
                            modifier = Modifier.weight(1f),
                            title = action.title,
                            icon = action.icon,
                            accentColor = action.tint,
                            compact = compactLayout,
                            onClick = action.onClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    left: DeviceInfoEntry,
    right: DeviceInfoEntry,
    cellModifier: Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        DeviceInfoCell(
            modifier = cellModifier,
            label = left.label,
            value = left.value
        )
        DeviceInfoCell(
            modifier = cellModifier,
            label = right.label,
            value = right.value
        )
    }
}

@Composable
private fun MetricsSection(
    items: List<HomeMetricModel>,
    compactLayout: Boolean
) {
    if (compactLayout) {
        Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    rowItems.forEach { item ->
                        DashboardMetricCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(item.titleKey),
                            value = item.value,
                            supportingText = item.supporting.takeIf { it.isNotBlank() },
                            icon = item.icon,
                            accentColor = item.accentColor,
                            progress = item.progress,
                            sparkline = item.sparkline
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            items.forEach { item ->
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(item.titleKey),
                    value = item.value,
                    supportingText = item.supporting.takeIf { it.isNotBlank() },
                    icon = item.icon,
                    accentColor = item.accentColor,
                    progress = item.progress,
                    sparkline = item.sparkline
                )
            }
        }
    }
}

private data class HomeToolbarAction(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit,
    val description: String = ""
)

private data class HomeStatusCardModel(
    val title: String,
    val value: String,
    val supporting: String,
    val icon: ImageVector,
    val accentColor: Color,
    val progress: Float? = null
)

private data class HomeInfoSummaryRow(
    val label: String,
    val value: String
)

private data class HomeHeaderMeta(
    val text: String,
    val icon: ImageVector
)

private fun parseForegroundAppFocus(output: String): HomeForegroundAppModel? {
    val focusLine = output
        .lineSequence()
        .firstOrNull { it.contains("/") }
        ?.trim()
        .orEmpty()
    if (focusLine.isBlank()) return null
    val component = Regex("""([A-Za-z0-9_.$]+)/(?:([A-Za-z0-9_.$]+))""")
        .find(focusLine)
        ?.value
        ?: return null
    val packageName = component.substringBefore("/")
    val activityName = component.substringAfter("/")
    val appName = packageName.substringAfterLast('.').ifBlank { packageName }
    return HomeForegroundAppModel(
        appName = appName,
        packageName = packageName,
        activityName = activityName,
        focusWindow = focusLine,
        status = l10n("已读取", "Loaded")
    )
}

private data class HomeForegroundAppModel(
    val appName: String = "--",
    val packageName: String = "--",
    val activityName: String = "--",
    val focusWindow: String = "--",
    val status: String = l10n("未采集", "Not collected")
) {
    fun rows(): List<Pair<String, String>> = listOf(
        l10n("应用名称", "App name") to appName,
        l10n("包名", "Package") to packageName,
        "Activity" to activityName,
        l10n("窗口焦点", "Window focus") to focusWindow,
        l10n("状态", "Status") to status
    )
}

private data class HomeDiagnosticUiModel(
    val title: String,
    val message: String,
    val actionText: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

internal enum class HomeDiagnosticCode {
    NO_DEVICE,
    STORAGE_LOW,
    LATENCY_HIGH,
    BATTERY_LOW,
    HEALTHY
}

internal fun homeDiagnosticCodes(
    isConnected: Boolean,
    storageProgress: Float,
    latencyMs: Int?,
    batteryProgress: Float
): List<HomeDiagnosticCode> {
    if (!isConnected) return listOf(HomeDiagnosticCode.NO_DEVICE)

    val codes = buildList {
        if (storageProgress >= 0.85f) add(HomeDiagnosticCode.STORAGE_LOW)
        if (latencyMs != null && latencyMs >= 250) add(HomeDiagnosticCode.LATENCY_HIGH)
        if (batteryProgress > 0f && batteryProgress <= 0.2f) add(HomeDiagnosticCode.BATTERY_LOW)
    }

    return codes.ifEmpty { listOf(HomeDiagnosticCode.HEALTHY) }
}

private data class HomeMetricModel(
    val titleKey: org.jetbrains.compose.resources.StringResource,
    val value: String,
    val icon: ImageVector,
    val accentColor: Color,
    val supporting: String = "",
    val progress: Float,
    val sparkline: Boolean = false
)

private data class DeviceInfoEntry(
    val label: String,
    val value: String
)

private fun parsePercentProgress(raw: String?): Float {
    val percent = raw?.trim()?.removeSuffix("%")?.toFloatOrNull() ?: return 0f
    return (percent / 100f).coerceIn(0f, 1f)
}

private fun parseStorageProgress(raw: String?): Float {
    if (raw.isNullOrBlank()) return 0f
    val match = Regex("""([\d.]+)\s*G?(?:B)?\s*/\s*([\d.]+)\s*G?(?:B)?""").find(raw) ?: return 0f
    val used = match.groupValues.getOrNull(1)?.toFloatOrNull() ?: return 0f
    val total = match.groupValues.getOrNull(2)?.toFloatOrNull() ?: return 0f
    if (total <= 0f) return 0f
    return (used / total).coerceIn(0f, 1f)
}

private fun formatMemorySupporting(raw: String?): String {
    return if (raw.isNullOrBlank() || raw == "--") {
        l10n("容量未采集", "Capacity not collected")
    } else {
        l10n("当前占用率", "Current usage")
    }
}

private fun formatStorageSupporting(raw: String?): String {
    val values = parseStorageValues(raw) ?: return l10n("可用空间未采集", "Free space not collected")
    val available = (values.second - values.first).coerceAtLeast(0f)
    return l10n("可用 ${formatStorageNumber(available)}G", "${formatStorageNumber(available)}G available")
}

private fun parseStorageValues(raw: String?): Pair<Float, Float>? {
    if (raw.isNullOrBlank()) return null
    val match = Regex("""([\d.]+)\s*G?(?:B)?\s*/\s*([\d.]+)\s*G?(?:B)?""").find(raw) ?: return null
    val used = match.groupValues.getOrNull(1)?.toFloatOrNull() ?: return null
    val total = match.groupValues.getOrNull(2)?.toFloatOrNull() ?: return null
    return used to total
}

private fun formatStorageNumber(value: Float): String {
    val rounded = kotlin.math.round(value * 10f) / 10f
    return if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
}

private fun parseLatencyMillis(raw: String?): Int? {
    if (raw.isNullOrBlank()) return null
    return Regex("""(\d+)""").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
}

private fun buildInlineMetricValue(primary: String, suffix: String): String {
    if (suffix.isBlank()) return primary
    if (primary.isBlank() || primary == "--") return suffix
    return "$primary $suffix"
}

private fun formatAndroidVersionWithApi(androidVersion: String?, sdkVersion: String?): String {
    val version = androidVersion.orDash()
    val sdk = sdkVersion?.takeIf { it.isNotBlank() } ?: return version
    return if (androidVersion.isNullOrBlank()) "API $sdk" else "$version (API $sdk)"
}

private fun formatDeviceDisplay(deviceId: String, model: String?): String {
    val cleanModel = model?.trim().orEmpty()
    return if (cleanModel.isNotBlank()) "$cleanModel （$deviceId）" else deviceId
}

private fun formatPrimaryDeviceName(
    modelFromInfo: String?,
    modelFromMap: String?,
    deviceId: String
): String {
    val model = modelFromInfo?.takeIf { it.isNotBlank() }
        ?: modelFromMap?.takeIf { it.isNotBlank() }
    return model ?: deviceId
}

private fun formatConnectedDuration(connectedSince: LocalDateTime?): String {
    if (connectedSince == null) return "--"
    val duration = Duration.between(connectedSince, LocalDateTime.now()).coerceAtLeast(Duration.ZERO)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatRelativeRefresh(rawTime: String): String {
    val parsed = runCatching {
        LocalDateTime.parse(rawTime, REFRESH_TIME_FORMATTER)
    }.getOrNull() ?: return "--"
    val duration = Duration.between(parsed, LocalDateTime.now()).coerceAtLeast(Duration.ZERO)
    val seconds = duration.seconds
    return when {
        seconds < 10 -> "0s"
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        else -> "${seconds / 3600}h"
    }
}

private fun extractPort(deviceId: String?, ipAddress: String?): String {
    val port = deviceId
        ?.substringAfterLast(':', "")
        ?.takeIf { it.all(Char::isDigit) && it.isNotBlank() }
    if (!port.isNullOrBlank()) return port
    return if (!ipAddress.isNullOrBlank()) "5555" else "--"
}

private fun String?.orDash(fallback: String = "--"): String {
    return if (this.isNullOrBlank()) fallback else this
}

private val REFRESH_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
