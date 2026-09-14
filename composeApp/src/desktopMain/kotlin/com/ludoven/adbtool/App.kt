package com.ludoven.adbtool

import com.ludoven.adbtool.ui.mac.*

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.app
import adbtool_desktop.composeapp.generated.resources.file_browser
import adbtool_desktop.composeapp.generated.resources.home
import adbtool_desktop.composeapp.generated.resources.set
import adbtool_desktop.composeapp.generated.resources.terminal
import com.ludoven.adbtool.agent.AgentFeatureRuntime
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.ui.mac.ExperimentalMaterial3Api
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ludoven.adbtool.pages.AppScreen
import com.ludoven.adbtool.pages.AiAgentScreen
import com.ludoven.adbtool.pages.CommonScreen
import com.ludoven.adbtool.pages.DeviceControlScreen
import com.ludoven.adbtool.pages.DeviceControlTab
import com.ludoven.adbtool.pages.DiagnosticsScreen
import com.ludoven.adbtool.pages.DiagnosticsTab
import com.ludoven.adbtool.pages.FileBrowserScreen
import com.ludoven.adbtool.pages.HomeScreen
import com.ludoven.adbtool.widget.SidebarGroup
import com.ludoven.adbtool.pages.SettingScreen
import com.ludoven.adbtool.pages.TerminalScreen
import com.ludoven.adbtool.pages.TipDialog
import com.ludoven.adbtool.util.AdbPathManager
import com.ludoven.adbtool.util.LanguageManager
import com.ludoven.adbtool.util.LocalAppLanguage
import com.ludoven.adbtool.util.ThemeManager
import com.ludoven.adbtool.util.WirelessAdbConnectionManager
import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.AppBehaviorPreferences
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.AppViewModel
import com.ludoven.adbtool.viewmodel.AiAgentViewModel
import com.ludoven.adbtool.viewmodel.CommonModel
import com.ludoven.adbtool.viewmodel.DeviceMirrorViewModel
import com.ludoven.adbtool.viewmodel.DevicesViewModel
import com.ludoven.adbtool.viewmodel.FileBrowserViewModel
import com.ludoven.adbtool.viewmodel.KeyEventViewModel
import com.ludoven.adbtool.viewmodel.LogViewModel
import com.ludoven.adbtool.widget.FeedbackToast
import com.ludoven.adbtool.widget.WirelessConnectionDialog
import com.ludoven.adbtool.viewmodel.ScreenRecordUiState
import com.ludoven.adbtool.viewmodel.BatchExecutionViewModel
import com.ludoven.adbtool.viewmodel.TerminalViewModel
import com.ludoven.adbtool.widget.Sidebar
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val aiAgentViewModelFactory = viewModelFactory {
    initializer { AiAgentViewModel() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val devicesViewModel: DevicesViewModel = viewModel()
    val appViewModel: AppViewModel = viewModel()
    val commonModel: CommonModel = viewModel()
    val batchExecutionViewModel: BatchExecutionViewModel = viewModel()
    val deviceMirrorViewModel: DeviceMirrorViewModel = viewModel()
    val keyEventViewModel: KeyEventViewModel = viewModel()
    val logViewModel: LogViewModel = viewModel()
    val terminalViewModel: TerminalViewModel = viewModel()
    val fileBrowserViewModel: FileBrowserViewModel = viewModel()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val agentFeaturePreferences = remember { AgentFeatureRuntime.preferences }
    val agentFeatureEnabled by agentFeaturePreferences.enabled.collectAsState()
    val behaviorPreferences = remember { AppBehaviorPreferences.store }
    LaunchedEffect(Unit) {
        LanguageManager.initialize()
        ThemeManager.initialize()
        AdbPathManager.getAdbPath()
        if (behaviorPreferences.autoDetectDeviceOnLaunch.value) {
            devicesViewModel.refreshDevices()
        }
    }

    CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
    val homeLabel = stringResource(Res.string.home)
    val appLabel = stringResource(Res.string.app)
    val fileBrowserLabel = stringResource(Res.string.file_browser)
    val terminalLabel = stringResource(Res.string.terminal)
    val setLabel = stringResource(Res.string.set)

    val tabGroups = remember(currentLanguage, agentFeatureEnabled, homeLabel, appLabel, fileBrowserLabel, terminalLabel, setLabel) {
        listOf(
            SidebarGroup(
                id = "home",
                label = homeLabel,
                icon = IconParkIcons.Home,
                defaultRoute = "home",
                items = listOf(TabItem(homeLabel, IconParkIcons.Home, "home")),
                collapsible = false
            ),
            SidebarGroup(
                id = "common",
                label = l10n("命令", "Commands"),
                icon = IconParkIcons.Command,
                defaultRoute = "common",
                items = listOf(TabItem(l10n("命令", "Commands"), IconParkIcons.Command, "common")),
                collapsible = false
            ),
            SidebarGroup(
                id = "device-control",
                label = l10n("设备控制", "Device Control"),
                icon = IconParkIcons.Phone,
                defaultRoute = "device-control",
                items = listOf(TabItem(l10n("设备控制", "Device Control"), IconParkIcons.Phone, "device-control")),
                collapsible = false
            ),
            SidebarGroup(
                id = "app",
                label = appLabel,
                icon = IconParkIcons.System,
                defaultRoute = "app",
                items = listOf(TabItem(appLabel, IconParkIcons.System, "app")),
                collapsible = false
            ),
            SidebarGroup(
                id = "filebrowser",
                label = fileBrowserLabel,
                icon = IconParkIcons.File,
                defaultRoute = "filebrowser",
                items = listOf(TabItem(fileBrowserLabel, IconParkIcons.File, "filebrowser")),
                collapsible = false
            ),
            SidebarGroup(
                id = "diagnostics",
                label = l10n("诊断", "Diagnostics"),
                icon = IconParkIcons.ChartLine,
                defaultRoute = "diagnostics",
                items = listOf(TabItem(l10n("诊断", "Diagnostics"), IconParkIcons.ChartLine, "diagnostics")),
                collapsible = false
            ),
            SidebarGroup(
                id = "terminal",
                label = terminalLabel,
                icon = IconParkIcons.Terminal,
                defaultRoute = "terminal",
                items = listOf(TabItem(terminalLabel, IconParkIcons.Terminal, "terminal")),
                collapsible = false
            ),
            *listOfNotNull(
                SidebarGroup(
                    id = "ai",
                    label = l10n("AI Agent", "AI Agent"),
                    icon = IconParkIcons.AiAssistant,
                    defaultRoute = "ai",
                    items = listOf(TabItem(l10n("AI Agent", "AI Agent"), IconParkIcons.AiAssistant, "ai")),
                    collapsible = false
                ).takeIf { agentFeatureEnabled }
            ).toTypedArray(),
            SidebarGroup(
                id = "settings",
                label = setLabel,
                icon = IconParkIcons.Setting,
                defaultRoute = "setting",
                items = listOf(TabItem(setLabel, IconParkIcons.Setting, "setting")),
                collapsible = false
            )
        )
    }

    val navController = rememberNavController()
    val stateHolder = rememberSaveableStateHolder()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    val selectedPrimaryRoute = normalizeRouteForPrimaryNavigation(currentRoute)
    var showRebootConfirm by remember { mutableStateOf(false) }
    var showWirelessConnectionDialog by remember { mutableStateOf(false) }
    var showScreenRecordDialog by remember { mutableStateOf(false) }
    var screenRecordDurationText by remember { mutableStateOf("15") }
    val screenRecordState by commonModel.screenRecordState.collectAsState()
    val wirelessConnectionManager = remember { WirelessAdbConnectionManager() }
    val wirelessConnectionHistory by wirelessConnectionManager.history.collectAsState()
    val currentThemeMode by ThemeManager.currentThemeMode.collectAsState()
    val resolvedDarkTheme = when (currentThemeMode) {
        ThemeManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeManager.ThemeMode.LIGHT -> false
        ThemeManager.ThemeMode.DARK -> true
    }

    fun navigateToRoute(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    fun handleGlobalShortcut(key: Key): Boolean {
        fun navigatePrimary(index: Int): Boolean {
            val route = primaryShortcutRoute(index, agentFeatureEnabled) ?: return false
            navigateToRoute(route)
            return true
        }
        return when (key) {
            Key.One -> navigatePrimary(1)
            Key.Two -> navigatePrimary(2)
            Key.Three -> navigatePrimary(3)
            Key.Four -> navigatePrimary(4)
            Key.Five -> navigatePrimary(5)
            Key.Six -> navigatePrimary(6)
            Key.Seven -> navigatePrimary(7)
            Key.Eight -> navigatePrimary(8)
            Key.Zero -> { navigateToRoute("setting"); true }
            Key.Comma -> { navigateToRoute("setting"); true }
            Key.K -> { navigateToRoute("common"); true }
            Key.R -> { devicesViewModel.refreshDevices(); true }
            Key.T -> { navigateToRoute("terminal"); true }
            else -> false
        }
    }

    LaunchedEffect(Unit) {
        AppMenuCommandBus.commands.collect { command ->
            when (command) {
                is AppMenuCommand.Navigate -> navigateToRoute(command.route)
                AppMenuCommand.OpenWirelessConnection -> showWirelessConnectionDialog = true
                AppMenuCommand.RefreshDevices -> devicesViewModel.refreshDevices()
                AppMenuCommand.RebootDevice -> showRebootConfirm = true
                AppMenuCommand.Screenshot -> commonModel.executeAdbAction(AdbFunctionType.SCREENSHOT)
                AppMenuCommand.ScreenRecord -> commonModel.executeAdbAction(AdbFunctionType.SCREEN_RECORD)
                AppMenuCommand.OpenTerminalTool -> navigateToRoute("terminal")
                AppMenuCommand.InstallApk -> commonModel.executeAdbAction(AdbFunctionType.INSTALL_APK)
                AppMenuCommand.ViewCurrentActivity -> commonModel.executeAdbAction(AdbFunctionType.VIEW_CURRENT_ACTIVITY)
                AppMenuCommand.ViewDeviceInfo -> {
                    navigateToRoute("home")
                    devicesViewModel.refreshDevices()
                }
                AppMenuCommand.ExportLogs -> commonModel.executeAdbAction(AdbFunctionType.CAPTURE_LOGS)
            }
        }
    }

    LaunchedEffect(Unit) {
        commonModel.screenRecordConfigRequests.collect {
            showScreenRecordDialog = true
        }
    }

    LaunchedEffect(agentFeatureEnabled, currentRoute) {
        if (!agentFeatureEnabled && currentRoute == "ai") {
            navigateToRoute("home")
        }
    }

    AdbToolTheme(useDarkTheme = resolvedDarkTheme) {
        val commonDialogMessage by commonModel.dialogMessage.collectAsState()
        val commonShowDialog by commonModel.showDialog.collectAsState()
        val commonToastMessage by commonModel.feedbackToastMessage.collectAsState()

        FeedbackToast(commonToastMessage)
        if (commonShowDialog) {
            commonDialogMessage?.let { message ->
                TipDialog(
                    dialogText = when (message) {
                        is MsgContent.Resource -> stringResource(
                            message.stringResource,
                            *message.args.toTypedArray()
                        )
                        is MsgContent.Text -> message.text
                    },
                    onDismiss = commonModel::dismissTipDialog
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && (event.isMetaPressed || event.isCtrlPressed)) {
                        handleGlobalShortcut(event.key)
                    } else {
                        false
                    }
                },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SidebarContainer(
                    groups = tabGroups,
                    selectedRoute = selectedPrimaryRoute,
                    devicesViewModel = devicesViewModel,
                    onItemClick = { route ->
                        navigateToRoute(route)
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = null
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                enterTransition = { EnterTransition.None },
                                exitTransition = { ExitTransition.None },
                                popEnterTransition = { EnterTransition.None },
                                popExitTransition = { ExitTransition.None }
                            ) {
                                composable("home") {
                                    stateHolder.SaveableStateProvider("home") {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            HomeScreen(
                                                viewModel = devicesViewModel,
                                                onScreenshot = { commonModel.executeAdbAction(AdbFunctionType.SCREENSHOT) },
                                                onScreenRecord = { commonModel.executeAdbAction(AdbFunctionType.SCREEN_RECORD) },
                                                onInstallApk = { commonModel.executeAdbAction(AdbFunctionType.INSTALL_APK) },
                                                onMirrorDevice = { navigateToRoute("device-control") },
                                                onOpenShell = {
                                                    navigateToRoute("terminal")
                                                },
                                                onOpenLogcat = { navigateToRoute("diagnostics") },
                                                onOpenFileManager = { navigateToRoute("filebrowser") },
                                                onOpenAppManager = { navigateToRoute("app") },
                                                onOpenDiagnostics = { navigateToRoute("diagnostics") },
                                                onOpenWirelessConnection = {
                                                    showWirelessConnectionDialog = true
                                                },
                                                onOpenAppDetails = { packageName ->
                                                    appViewModel.openAppInfo(packageName)
                                                    navigateToRoute("app")
                                                }
                                            )
                                        }
                                    }
                                }
                                composable("ai") {
                                    if (agentFeatureEnabled) {
                                        val aiAgentViewModel: AiAgentViewModel = viewModel(
                                            factory = aiAgentViewModelFactory
                                        )
                                        stateHolder.SaveableStateProvider("ai") {
                                            AiAgentScreen(
                                                viewModel = aiAgentViewModel,
                                                devicesViewModel = devicesViewModel,
                                                onOpenDevices = { navigateToRoute("home") },
                                                onOpenSettings = { navigateToRoute("setting") }
                                            )
                                        }
                                    }
                                }
                                composable("device-control") {
                                    stateHolder.SaveableStateProvider("device-control") {
                                        DeviceControlRoute(
                                            mirrorViewModel = deviceMirrorViewModel,
                                            keyEventViewModel = keyEventViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DeviceControlTab.Mirror
                                        )
                                    }
                                }
                                composable("mirror") {
                                    stateHolder.SaveableStateProvider("mirror") {
                                        DeviceControlRoute(
                                            mirrorViewModel = deviceMirrorViewModel,
                                            keyEventViewModel = keyEventViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DeviceControlTab.Mirror
                                        )
                                    }
                                }
                                composable("common") {
                                    stateHolder.SaveableStateProvider("common") {
                                        val devices by devicesViewModel.devices.collectAsState()
                                        val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
                                        val deviceDisplayNames by devicesViewModel.deviceDisplayNames.collectAsState()
                                        CommonScreen(
                                            viewModel = commonModel,
                                            batchViewModel = batchExecutionViewModel,
                                            selectedDevice = selectedDevice,
                                            devices = devices,
                                            deviceDisplayNames = deviceDisplayNames
                                        )
                                    }
                                }
                                composable("keyevent") {
                                    stateHolder.SaveableStateProvider("keyevent") {
                                        DeviceControlRoute(
                                            mirrorViewModel = deviceMirrorViewModel,
                                            keyEventViewModel = keyEventViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DeviceControlTab.Keys
                                        )
                                    }
                                }
                                composable("terminal") {
                                    stateHolder.SaveableStateProvider("terminal") {
                                        val devices by devicesViewModel.devices.collectAsState()
                                        val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
                                        val deviceDisplayNames by devicesViewModel.deviceDisplayNames.collectAsState()
                                        TerminalScreen(
                                            viewModel = terminalViewModel,
                                            selectedDevice = selectedDevice,
                                            devices = devices,
                                            deviceDisplayNames = deviceDisplayNames,
                                            onSelectDevice = { deviceId ->
                                                devicesViewModel.selectDevice(deviceId)
                                            },
                                            onRefreshDevices = {
                                                devicesViewModel.refreshDevices()
                                            }
                                        )
                                    }
                                }
                                composable("app") {
                                    stateHolder.SaveableStateProvider("app") {
                                        val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
                                        AppScreen(
                                            viewModel = appViewModel,
                                            selectedDevice = selectedDevice,
                                            onRefreshDevices = devicesViewModel::refreshDevices,
                                            onOpenWirelessConnection = { showWirelessConnectionDialog = true },
                                            onOpenTroubleshooting = { navigateToRoute("home") }
                                        )
                                    }
                                }
                                composable("filebrowser") {
                                    stateHolder.SaveableStateProvider("filebrowser") {
                                        val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
                                        FileBrowserScreen(
                                            viewModel = fileBrowserViewModel,
                                            selectedDevice = selectedDevice,
                                            onRefreshDevices = devicesViewModel::refreshDevices,
                                            onOpenWirelessConnection = { showWirelessConnectionDialog = true },
                                            onOpenTroubleshooting = { navigateToRoute("home") }
                                        )
                                    }
                                }
                                composable("setting") {
                                    stateHolder.SaveableStateProvider("setting") {
                                        val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
                                        SettingScreen(selectedDeviceId = selectedDevice)
                                    }
                                }
                                composable("diagnostics") {
                                    stateHolder.SaveableStateProvider("diagnostics") {
                                        DiagnosticsRoute(
                                            logViewModel = logViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DiagnosticsTab.Logs,
                                            onOpenWirelessConnection = { showWirelessConnectionDialog = true },
                                            onOpenTroubleshooting = { navigateToRoute("home") }
                                        )
                                    }
                                }
                                composable("log") {
                                    stateHolder.SaveableStateProvider("log") {
                                        DiagnosticsRoute(
                                            logViewModel = logViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DiagnosticsTab.Logs,
                                            onOpenWirelessConnection = { showWirelessConnectionDialog = true },
                                            onOpenTroubleshooting = { navigateToRoute("home") }
                                        )
                                    }
                                }
                                composable("process") {
                                    stateHolder.SaveableStateProvider("process") {
                                        DiagnosticsRoute(
                                            logViewModel = logViewModel,
                                            devicesViewModel = devicesViewModel,
                                            initialTab = DiagnosticsTab.Processes,
                                            onOpenWirelessConnection = { showWirelessConnectionDialog = true },
                                            onOpenTroubleshooting = { navigateToRoute("home") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWirelessConnectionDialog) {
        WirelessConnectionDialog(
            history = wirelessConnectionHistory,
            onConnect = wirelessConnectionManager::connect,
            onPair = wirelessConnectionManager::pair,
            onRemoveHistory = wirelessConnectionManager::removeHistory,
            onDevicesChanged = devicesViewModel::refreshDevices,
            onDismiss = { showWirelessConnectionDialog = false }
        )
    }

    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = { showRebootConfirm = false },
            title = { Text(l10n("确认重启设备", "Confirm Device Reboot")) },
            text = { Text(l10n("即将重启连接的设备，设备上的未保存数据可能丢失。是否继续？", "The connected device will reboot. Unsaved data may be lost. Continue?")) },
            confirmButton = {
                TextButton(onClick = {
                    showRebootConfirm = false
                    commonModel.executeAdbAction(AdbFunctionType.REBOOT_DEVICE)
                }) { Text(l10n("确认重启", "Reboot")) }
            },
            dismissButton = {
                TextButton(onClick = { showRebootConfirm = false }) {
                    Text(l10n("取消", "Cancel"))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showScreenRecordDialog) {
        ScreenRecordConfigDialog(
            state = screenRecordState,
            durationText = screenRecordDurationText,
            onDurationTextChange = { value ->
                screenRecordDurationText = value.filter(Char::isDigit).take(3)
            },
            onStartFixed = {
                val duration = screenRecordDurationText.toIntOrNull()?.coerceIn(1, 180) ?: 15
                commonModel.startScreenRecord(duration)
            },
            onStartManual = {
                commonModel.startScreenRecord(durationSeconds = null)
            },
            onStop = {
                commonModel.stopScreenRecord()
            },
            onDismiss = {
                showScreenRecordDialog = false
            }
        )
    }
    }
}

@Composable
private fun ScreenRecordConfigDialog(
    state: ScreenRecordUiState,
    durationText: String,
    onDurationTextChange: (String) -> Unit,
    onStartFixed: () -> Unit,
    onStartManual: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(ScreenRecordMode.Manual) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val fixedDuration = durationText.toIntOrNull()
    val fixedDurationValid = fixedDuration != null && fixedDuration in 1..180
    val primaryActionEnabled = state.isRecording || selectedMode == ScreenRecordMode.Manual || fixedDurationValid
    val actionContainerColor = if (state.isRecording) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val actionContentColor = if (state.isRecording) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    LaunchedEffect(state.isRecording, state.startedAtMillis) {
        while (state.isRecording) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .width(520.dp)
                    .padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScreenRecordDialogHeader(isRecording = state.isRecording)
                if (state.isRecording) {
                    ScreenRecordRunningPanel(
                        state = state,
                        nowMillis = nowMillis
                    )
                } else {
                    ScreenRecordSetupPanel(
                        selectedMode = selectedMode,
                        durationText = durationText,
                        fixedDurationValid = fixedDurationValid,
                        onModeSelected = { mode ->
                            selectedMode = mode
                            if (mode == ScreenRecordMode.Fixed && durationText.isBlank()) {
                                onDurationTextChange("15")
                            }
                        },
                        onDurationTextChange = onDurationTextChange
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(if (state.isRecording) l10n("隐藏", "Hide") else l10n("取消", "Cancel"))
                }
                Button(
                    onClick = {
                        if (state.isRecording) {
                            onStop()
                        } else if (selectedMode == ScreenRecordMode.Manual) {
                            onStartManual()
                        } else {
                            onStartFixed()
                        }
                    },
                    enabled = if (state.isRecording) !state.isStopping else primaryActionEnabled,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(UiTokens.RadiusSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionContainerColor,
                        contentColor = actionContentColor
                    )
                ) {
                    Text(
                        text = when {
                            state.isStopping -> l10n("正在保存", "Saving")
                            state.isRecording -> l10n("停止并保存", "Stop and save")
                            selectedMode == ScreenRecordMode.Manual -> l10n("开始录屏", "Start recording")
                            else -> l10n("按 ${durationText.ifBlank { "15" }} 秒录制", "Record ${durationText.ifBlank { "15" }}s")
                        },
                        color = actionContentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

private enum class ScreenRecordMode {
    Manual,
    Fixed
}

private object ScreenRecordVisualTokens {
    val Primary = Color(0xFF0A84FF)
    val Text = Color(0xFF111827)
    val Muted = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
    val Soft = Color(0xFFF3F4F6)
    val Divider = Color(0xFFE7EAF0)
    val Danger = Color(0xFFEF4444)
    val Warning = Color(0xFFF97316)
    val Success = Color(0xFF16A34A)
}

@Composable
private fun Modifier.screenRecordNoRippleClickable(onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick
)

@Composable
private fun ScreenRecordDialogHeader(isRecording: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (isRecording) ScreenRecordVisualTokens.Danger.copy(alpha = 0.12f)
                    else ScreenRecordVisualTokens.Primary.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconParkIcons.Video,
                contentDescription = null,
                tint = if (isRecording) ScreenRecordVisualTokens.Danger else ScreenRecordVisualTokens.Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = if (isRecording) l10n("录屏中", "Recording") else l10n("录屏设置", "Screen record settings"),
                color = ScreenRecordVisualTokens.Text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isRecording) {
                    l10n("停止后自动保存到本地", "Stop to save the recording locally")
                } else {
                    l10n("选择录制方式、时长和保存行为", "Choose mode, duration, and save behavior")
                },
                color = ScreenRecordVisualTokens.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ScreenRecordSetupPanel(
    selectedMode: ScreenRecordMode,
    durationText: String,
    fixedDurationValid: Boolean,
    onModeSelected: (ScreenRecordMode) -> Unit,
    onDurationTextChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenRecordModeCard(
                title = l10n("手动停止", "Manual stop"),
                description = l10n("开始后由你控制结束时间", "Start now and stop when ready"),
                icon = IconParkIcons.Video,
                accentColor = ScreenRecordVisualTokens.Primary,
                selected = selectedMode == ScreenRecordMode.Manual,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(ScreenRecordMode.Manual) }
            )
            ScreenRecordModeCard(
                title = l10n("固定时长", "Fixed duration"),
                description = l10n("到时自动结束并保存", "Stop automatically and save"),
                icon = IconParkIcons.Time,
                accentColor = ScreenRecordVisualTokens.Primary,
                selected = selectedMode == ScreenRecordMode.Fixed,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(ScreenRecordMode.Fixed) }
            )
        }

        ScreenRecordOptionsCard(
            selectedMode = selectedMode,
            durationText = durationText,
            fixedDurationValid = fixedDurationValid,
            onDurationTextChange = onDurationTextChange
        )
    }
}

@Composable
private fun ScreenRecordModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .heightIn(min = 86.dp)
            .background(
                if (selected) accentColor.copy(alpha = 0.08f) else Color.White,
                shape
            )
            .border(
                width = 1.dp,
                color = if (selected) accentColor.copy(alpha = 0.38f) else ScreenRecordVisualTokens.Border,
                shape = shape
            )
            .screenRecordNoRippleClickable(onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                color = ScreenRecordVisualTokens.Text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = ScreenRecordVisualTokens.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (selected) {
            Icon(
                imageVector = IconParkIcons.CheckCircle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ScreenRecordOptionsCard(
    selectedMode: ScreenRecordMode,
    durationText: String,
    fixedDurationValid: Boolean,
    onDurationTextChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape)
            .border(1.dp, ScreenRecordVisualTokens.Border, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n("录制选项", "Recording options"),
                color = ScreenRecordVisualTokens.Text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            ScreenRecordStatusPill(
                text = if (selectedMode == ScreenRecordMode.Manual) l10n("手动", "Manual") else l10n("固定", "Fixed"),
                color = ScreenRecordVisualTokens.Primary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ScreenRecordVisualTokens.Divider)
        )

        if (selectedMode == ScreenRecordMode.Fixed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 60, 120).forEach { seconds ->
                    ScreenRecordDurationChip(
                        seconds = seconds,
                        selected = durationText == seconds.toString(),
                        modifier = Modifier.weight(1f),
                        onClick = { onDurationTextChange(seconds.toString()) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = durationText,
                    onValueChange = onDurationTextChange,
                    singleLine = true,
                    label = { Text(l10n("自定义秒数", "Custom seconds")) },
                    placeholder = { Text("15") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ScreenRecordVisualTokens.Primary.copy(alpha = 0.48f),
                        unfocusedBorderColor = ScreenRecordVisualTokens.Border,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                ScreenRecordStatusPill(
                    text = if (fixedDurationValid) l10n("1-180 秒", "1-180s") else l10n("需 1-180 秒", "Needs 1-180s"),
                    color = if (fixedDurationValid) ScreenRecordVisualTokens.Success else ScreenRecordVisualTokens.Danger
                )
            }
        } else {
            ScreenRecordInfoRow(
                label = l10n("时长", "Duration"),
                value = l10n("不固定，由你手动停止", "Open-ended until manually stopped"),
                valueColor = ScreenRecordVisualTokens.Primary
            )
            ScreenRecordInfoRow(
                label = l10n("保存", "Save"),
                value = l10n("停止后自动拉取到本地", "Pulled locally after stop"),
                valueColor = ScreenRecordVisualTokens.Text
            )
        }

        Text(
            text = l10n(
                "Android 原生 screenrecord 单次最长通常为 180 秒；长时间录制建议使用手动模式分段保存。",
                "Android screenrecord usually caps a single session at 180 seconds. Use manual mode for segmented long recordings."
            ),
            color = ScreenRecordVisualTokens.Muted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ScreenRecordDurationChip(
    seconds: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .background(
                if (selected) ScreenRecordVisualTokens.Primary.copy(alpha = 0.10f)
                else ScreenRecordVisualTokens.Soft,
                shape
            )
            .border(
                1.dp,
                if (selected) ScreenRecordVisualTokens.Primary.copy(alpha = 0.36f)
                else ScreenRecordVisualTokens.Border,
                shape
            )
            .screenRecordNoRippleClickable(onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${seconds}s",
            color = if (selected) ScreenRecordVisualTokens.Primary else ScreenRecordVisualTokens.Text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun ScreenRecordRunningPanel(
    state: ScreenRecordUiState,
    nowMillis: Long
) {
    val shape = RoundedCornerShape(12.dp)
    val elapsedSeconds = screenRecordElapsedSeconds(state.startedAtMillis, nowMillis)
    val remainingSeconds = state.durationSeconds?.let { (it - elapsedSeconds).coerceAtLeast(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape)
            .border(1.dp, ScreenRecordVisualTokens.Danger.copy(alpha = 0.22f), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(ScreenRecordVisualTokens.Danger.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconParkIcons.Video,
                    contentDescription = null,
                    tint = ScreenRecordVisualTokens.Danger,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = l10n("正在录制设备屏幕", "Recording device screen"),
                    color = ScreenRecordVisualTokens.Text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = l10n("停止后会自动保存并拉取到本地", "Stop to save and pull the file locally"),
                    color = ScreenRecordVisualTokens.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ScreenRecordStatusPill(
                text = if (state.isStopping) l10n("保存中", "Saving") else l10n("进行中", "Live"),
                color = if (state.isStopping) ScreenRecordVisualTokens.Warning else ScreenRecordVisualTokens.Danger
            )
        }

        ScreenRecordTimerPanel(
            elapsedSeconds = elapsedSeconds,
            remainingSeconds = remainingSeconds,
            totalSeconds = state.durationSeconds
        )

        ScreenRecordInfoRow(
            label = l10n("保存位置", "Save path"),
            value = state.localPath.ifBlank {
                l10n("停止后生成本地文件", "Local file will be created after stop")
            },
            valueColor = ScreenRecordVisualTokens.Text
        )
    }
}

@Composable
private fun ScreenRecordTimerPanel(
    elapsedSeconds: Long,
    remainingSeconds: Long?,
    totalSeconds: Int?
) {
    val primaryValue = remainingSeconds?.let(::formatScreenRecordDuration)
        ?: formatScreenRecordDuration(elapsedSeconds)
    val primaryLabel = if (remainingSeconds == null) l10n("已录制", "Elapsed") else l10n("剩余时间", "Remaining")
    val supporting = if (remainingSeconds == null) {
        l10n("手动停止模式", "Manual stop mode")
    } else {
        l10n("已录制 ${formatScreenRecordDuration(elapsedSeconds)}", "Elapsed ${formatScreenRecordDuration(elapsedSeconds)}")
    }
    val progress = if (totalSeconds == null || totalSeconds <= 0) {
        null
    } else {
        (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenRecordVisualTokens.Danger.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = primaryLabel,
                    color = ScreenRecordVisualTokens.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = primaryValue,
                    color = ScreenRecordVisualTokens.Text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = supporting,
                color = ScreenRecordVisualTokens.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(ScreenRecordVisualTokens.Danger.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(ScreenRecordVisualTokens.Danger, RoundedCornerShape(999.dp))
                )
            }
        }
    }
}

@Composable
private fun ScreenRecordInfoRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            color = ScreenRecordVisualTokens.Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScreenRecordStatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun screenRecordElapsedSeconds(startedAtMillis: Long, nowMillis: Long): Long {
    if (startedAtMillis <= 0L || nowMillis <= startedAtMillis) return 0L
    return ((nowMillis - startedAtMillis) / 1000L).coerceAtLeast(0L)
}

private fun formatScreenRecordDuration(totalSeconds: Long): String {
    val normalized = totalSeconds.coerceAtLeast(0L)
    val minutes = normalized / 60L
    val seconds = normalized % 60L
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

internal val PRIMARY_SHORTCUT_ROUTES = mapOf(
    1 to "home",
    2 to "ai",
    3 to "common",
    4 to "device-control",
    5 to "app",
    6 to "filebrowser",
    7 to "diagnostics",
    8 to "terminal"
)

internal fun normalizeRouteForPrimaryNavigation(route: String): String = when (route) {
    "mirror", "keyevent", "device-control" -> "device-control"
    "log", "process", "diagnostics" -> "diagnostics"
    else -> route
}

internal fun primaryShortcutRoute(index: Int, agentFeatureEnabled: Boolean): String? =
    PRIMARY_SHORTCUT_ROUTES[index]?.takeIf { route -> agentFeatureEnabled || route != "ai" }

@Composable
private fun DeviceControlRoute(
    mirrorViewModel: DeviceMirrorViewModel,
    keyEventViewModel: KeyEventViewModel,
    devicesViewModel: DevicesViewModel,
    initialTab: DeviceControlTab
) {
    val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
    val deviceDisplayNames by devicesViewModel.deviceDisplayNames.collectAsState()
    DeviceControlScreen(
        mirrorViewModel = mirrorViewModel,
        keyEventViewModel = keyEventViewModel,
        selectedDevice = selectedDevice,
        deviceDisplayNames = deviceDisplayNames,
        initialTab = initialTab
    )
}

@Composable
private fun DiagnosticsRoute(
    logViewModel: LogViewModel,
    devicesViewModel: DevicesViewModel,
    initialTab: DiagnosticsTab,
    onOpenWirelessConnection: () -> Unit,
    onOpenTroubleshooting: () -> Unit
) {
    val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
    DiagnosticsScreen(
        logViewModel = logViewModel,
        selectedDevice = selectedDevice,
        initialTab = initialTab,
        onRefreshDevices = devicesViewModel::refreshDevices,
        onOpenWirelessConnection = onOpenWirelessConnection,
        onOpenTroubleshooting = onOpenTroubleshooting
    )
}

@Composable
private fun SidebarContainer(
    groups: List<SidebarGroup>,
    selectedRoute: String,
    devicesViewModel: DevicesViewModel,
    onItemClick: (String) -> Unit
) {
    val devices by devicesViewModel.devices.collectAsState()
    val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
    val deviceDisplayNames by devicesViewModel.deviceDisplayNames.collectAsState()

    Sidebar(
        groups = groups,
        selectedRoute = selectedRoute,
        connectedDeviceCount = devices.size,
        devices = devices,
        selectedDevice = selectedDevice,
        deviceDisplayNames = deviceDisplayNames,
        onItemClick = onItemClick,
        onDeviceSelected = { deviceId ->
            devicesViewModel.selectDevice(deviceId)
        }
    )
}

data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)
