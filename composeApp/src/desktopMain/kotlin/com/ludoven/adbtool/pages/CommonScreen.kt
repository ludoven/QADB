package com.ludoven.adbtool.pages

import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.QadbPalette
import com.ludoven.adbtool.ui.icons.IconParkIcons

import com.ludoven.adbtool.ui.mac.*

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.confirm
import adbtool_desktop.composeapp.generated.resources.tip_title
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import com.ludoven.adbtool.ui.mac.AlertDialog
import com.ludoven.adbtool.ui.mac.Card
import com.ludoven.adbtool.ui.mac.CardDefaults
import com.ludoven.adbtool.ui.mac.Icon
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.OutlinedTextField
import com.ludoven.adbtool.ui.mac.OutlinedTextFieldDefaults
import com.ludoven.adbtool.ui.mac.Scaffold
import com.ludoven.adbtool.ui.mac.Text
import com.ludoven.adbtool.ui.mac.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.CommandFavoritesManager
import com.ludoven.adbtool.util.CustomCommand
import com.ludoven.adbtool.util.CustomCommandManager
import com.ludoven.adbtool.util.copyPlainTextToClipboard
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.CommonModel
import com.ludoven.adbtool.viewmodel.BatchDeviceResult
import com.ludoven.adbtool.viewmodel.BatchDeviceStatus
import com.ludoven.adbtool.viewmodel.BatchExecutionUiState
import com.ludoven.adbtool.viewmodel.BatchExecutionViewModel
import com.ludoven.adbtool.widget.InlineStatusBanner
import com.ludoven.adbtool.widget.InlineStatusTone
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private enum class CommandTrigger {
    DIRECT,
    PACKAGE_INPUT,
    SHELL_INPUT
}

private data class CommandCategoryUi(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private data class CommandItemUi(
    val id: String,
    val title: String,
    val description: String,
    val commandPreview: String,
    val categoryKey: String,
    val icon: ImageVector,
    val iconTint: Color,
    val trigger: CommandTrigger,
    val actionType: AdbFunctionType?,
    val shellCommand: String? = null,
    val hints: List<String> = emptyList()
)

private fun displayCommandTitle(command: CommandItemUi): String {
    return if (command.categoryKey == "tv" && command.title.startsWith("TV ")) {
        command.title.removePrefix("TV ").trim()
    } else {
        command.title
    }
}

internal fun commonCommandRunEnabled(selectedDevice: String?): Boolean =
    !selectedDevice.isNullOrBlank()

internal fun commonBatchCommandSupported(actionType: AdbFunctionType?): Boolean =
    actionType !in setOf(
        AdbFunctionType.INSTALL_APK,
        AdbFunctionType.INSTALL_AND_LAUNCH,
        AdbFunctionType.DEVICE_MIRROR,
        AdbFunctionType.SCREENSHOT,
        AdbFunctionType.SCREEN_RECORD,
        AdbFunctionType.CAPTURE_LOGS
    )

internal fun commonBatchCommandIsDestructive(actionType: AdbFunctionType?): Boolean =
    actionType in setOf(
        AdbFunctionType.REBOOT_DEVICE,
        AdbFunctionType.CLEAR_CACHE_AND_RESTART
    )

private fun CommandItemUi.supportsBatchExecution(): Boolean =
    commonBatchCommandSupported(actionType) && (actionType != null || !shellCommand.isNullOrBlank())

@Composable
@Preview
fun CommonScreen(
    viewModel: CommonModel,
    batchViewModel: BatchExecutionViewModel,
    selectedDevice: String? = null,
    devices: List<String> = emptyList(),
    deviceDisplayNames: Map<String, String> = emptyMap()
) {
    val coroutineScope = rememberCoroutineScope()
    val batchState by batchViewModel.uiState.collectAsState()

    var favoriteIds by remember { mutableStateOf(CommandFavoritesManager.load()) }
    var customCommands by remember { mutableStateOf(CustomCommandManager.load()) }
    var showAddCommandDialog by remember { mutableStateOf(false) }
    var editingCommand by remember { mutableStateOf<CustomCommand?>(null) }

    val categories = remember(favoriteIds) {
        listOf(
            CommandCategoryUi("all", l10n("全部", "All"), Icons.Default.Tune),
            CommandCategoryUi("favorites", l10n("收藏", "Favorites"), Icons.Default.Bookmark),
            CommandCategoryUi("device", l10n("设备", "Device"), IconParkIcons.Home),
            CommandCategoryUi("app", l10n("应用管理", "Apps"), Icons.Default.InstallMobile),
            CommandCategoryUi("file", l10n("文件操作", "Files"), IconParkIcons.Folder),
            CommandCategoryUi("input", l10n("输入控制", "Input"), Icons.Default.Edit),
            CommandCategoryUi("screen", l10n("截图录屏", "Screen"), IconParkIcons.Camera),
            CommandCategoryUi("network", l10n("网络调试", "Network"), IconParkIcons.Wifi),
            CommandCategoryUi("log", l10n("日志", "Logs"), Icons.Default.Memory),
            CommandCategoryUi("tv", l10n("TV盒子", "TV"), IconParkIcons.CastScreen)
        )
    }

    val allCommands = rememberCommandLibrary()

    val mergedCommands = remember(allCommands, customCommands) {
        val builtin = allCommands
        val custom = customCommands.map { cmd ->
            CommandItemUi(
                id = cmd.id,
                title = cmd.title,
                description = cmd.description,
                commandPreview = cmd.commandPreview,
                categoryKey = cmd.categoryKey,
                icon = IconParkIcons.Code,
                iconTint = QadbPalette.Blue,
                trigger = if (cmd.shellCommand.isNotBlank()) CommandTrigger.SHELL_INPUT else CommandTrigger.DIRECT,
                actionType = null,
                shellCommand = cmd.shellCommand.takeIf { it.isNotBlank() },
                hints = emptyList()
            )
        }
        builtin + custom
    }

    var selectedCategory by remember { mutableStateOf("all") }
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCommandId by remember { mutableStateOf(mergedCommands.firstOrNull()?.id.orEmpty()) }
    var packageNameInput by remember { mutableStateOf("com.example.app") }
    var shellCommandInput by remember { mutableStateOf("getprop") }
    var textInput by remember { mutableStateOf("hello") }
    var executionResult by remember { mutableStateOf(l10n("等待执行", "Ready")) }
    var showRebootConfirm by remember { mutableStateOf(false) }
    var pendingRebootCommand by remember { mutableStateOf<CommandItemUi?>(null) }
    var batchMode by remember { mutableStateOf(false) }
    var batchDevicesExpanded by remember { mutableStateOf(true) }
    var selectedBatchDeviceIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchConfirm by remember { mutableStateOf(false) }
    var pendingBatchCommand by remember { mutableStateOf<CommandItemUi?>(null) }

    LaunchedEffect(devices) {
        selectedBatchDeviceIds = selectedBatchDeviceIds.intersect(devices.toSet())
    }

    val filteredCommands by remember(mergedCommands, selectedCategory, searchKeyword, favoriteIds) {
        derivedStateOf {
            val keyword = searchKeyword.trim().lowercase()
            mergedCommands.filter { command ->
                val matchCategory = when (selectedCategory) {
                    "all" -> true
                    "favorites" -> command.id in favoriteIds
                    else -> command.categoryKey == selectedCategory
                }
                val matchKeyword = keyword.isBlank() ||
                    command.title.lowercase().contains(keyword) ||
                    command.description.lowercase().contains(keyword) ||
                    command.commandPreview.lowercase().contains(keyword)
                matchCategory && matchKeyword
            }
        }
    }

    LaunchedEffect(filteredCommands, selectedCommandId) {
        if (filteredCommands.isEmpty()) {
            selectedCommandId = ""
        } else if (filteredCommands.none { it.id == selectedCommandId }) {
            selectedCommandId = filteredCommands.first().id
        }
    }

    val selectedCommand = filteredCommands.firstOrNull { it.id == selectedCommandId }
        ?: mergedCommands.firstOrNull { it.id == selectedCommandId }
        ?: filteredCommands.firstOrNull()
        ?: mergedCommands.firstOrNull()

    val resolvedCommandPreview by remember(selectedCommand, selectedDevice, packageNameInput, shellCommandInput, textInput) {
        derivedStateOf {
            if (selectedCommand == null) return@derivedStateOf ""
            when (selectedCommand.trigger) {
                CommandTrigger.PACKAGE_INPUT -> {
                    val pkg = packageNameInput.trim().ifEmpty { "com.example.app" }
                    when (selectedCommand.actionType) {
                        AdbFunctionType.LAUNCH_APP_BY_PACKAGE ->
                            "adb shell monkey -p $pkg -c android.intent.category.LAUNCHER 1"
                        AdbFunctionType.STOP_APP_BY_PACKAGE ->
                            "adb shell am force-stop $pkg"
                        AdbFunctionType.CLEAR_CACHE_AND_RESTART ->
                            "adb shell pm clear $pkg && adb shell monkey -p $pkg -c android.intent.category.LAUNCHER 1"
                        else -> selectedCommand.commandPreview
                            .replace("{package}", pkg)
                            .replace("{text}", textInput.trim())
                    }
                }

                CommandTrigger.SHELL_INPUT -> {
                    if (!selectedCommand.shellCommand.isNullOrBlank()) {
                        "adb shell ${selectedCommand.shellCommand
                            .replace("{package}", packageNameInput.trim())
                            .replace("{text}", textInput.trim())}"
                    } else {
                        val shell = shellCommandInput.trim().ifEmpty { "getprop" }
                        "adb shell $shell"
                    }
                }

                CommandTrigger.DIRECT -> {
                    if (selectedCommand.actionType == AdbFunctionType.INPUT_TEXT) {
                        val text = textInput.ifEmpty { "hello" }
                        "adb shell input text \"$text\""
                    } else if (selectedCommand.id.startsWith("custom_")) {
                        resolvedCustomAdbPreview(
                            rawCommand = selectedCommand.commandPreview,
                            deviceId = selectedDevice,
                            packageName = packageNameInput,
                            text = textInput
                        )
                    } else {
                        selectedCommand.commandPreview
                            .replace("{package}", packageNameInput.trim())
                            .replace("{text}", textInput.trim())
                    }
                }
            }
        }
    }

    fun copyCommand(command: String) {
        copyPlainTextToClipboard(command)
    }

    suspend fun executeCommandOnDevice(
        command: CommandItemUi,
        deviceId: String,
        packageNameInput: String,
        shellCommandInput: String,
        textInput: String
    ): AdbTool.AdbResult {
        return when (command.trigger) {
            CommandTrigger.PACKAGE_INPUT -> {
                val pkg = packageNameInput.trim()
                when (command.actionType) {
                    AdbFunctionType.LAUNCH_APP_BY_PACKAGE -> AdbTool.startAppAsync(pkg, deviceId)
                    AdbFunctionType.STOP_APP_BY_PACKAGE -> AdbTool.stopAppAsync(pkg, deviceId)
                    AdbFunctionType.CLEAR_CACHE_AND_RESTART -> {
                        val cleared = AdbTool.clearAppDataAsync(pkg, deviceId)
                        if (cleared.success) AdbTool.startAppAsync(pkg, deviceId) else cleared
                    }
                    else -> AdbTool.AdbResult(false, "", "Unsupported package action: ${command.actionType}")
                }
            }

            CommandTrigger.SHELL_INPUT -> {
                val shell = (command.shellCommand ?: shellCommandInput.trim())
                    .replace("{package}", packageNameInput.trim())
                    .replace("{text}", textInput.trim())
                AdbTool.execShellAsync(shell, deviceId)
            }

            CommandTrigger.DIRECT -> {
                if (!command.shellCommand.isNullOrBlank()) {
                    val shell = command.shellCommand
                        .replace("{package}", packageNameInput.trim())
                        .replace("{text}", textInput.trim())
                    return AdbTool.execShellAsync(shell, deviceId)
                }

                when (command.actionType) {
                    null -> {
                        val args = customAdbArgs(
                            rawCommand = command.commandPreview,
                            deviceId = deviceId,
                            packageName = packageNameInput,
                            text = textInput
                        )
                        if (args.isEmpty()) {
                            AdbTool.AdbResult(false, "", "ADB command cannot be empty")
                        } else {
                            AdbTool.execAdbAsync(*args.toTypedArray())
                        }
                    }

                    AdbFunctionType.INPUT_TEXT -> AdbTool.execShellAsync(
                        AdbTool.buildShellCommand("input", "text", textInput),
                        deviceId
                    )

                    else -> viewModel.executeAdbActionOnDevice(command.actionType, deviceId)
                }
            }
        }
    }

    fun executeCommand(command: CommandItemUi) {
        if (batchMode) {
            if (!command.supportsBatchExecution()) {
                executionResult = l10n("此命令暂不支持批量执行", "This command is not supported in batch mode")
                return
            }
            if (selectedBatchDeviceIds.isEmpty()) {
                executionResult = l10n("执行失败：请至少选择一台设备", "Failed: select at least one device")
                return
            }
            if (command.trigger == CommandTrigger.PACKAGE_INPUT && packageNameInput.isBlank()) {
                executionResult = l10n("执行失败：包名不能为空", "Failed: package name cannot be empty")
                return
            }
            if (command.trigger == CommandTrigger.SHELL_INPUT &&
                (command.shellCommand ?: shellCommandInput).isBlank()
            ) {
                executionResult = l10n("执行失败：Shell 命令不能为空", "Failed: shell command cannot be empty")
                return
            }
            if (command.actionType == AdbFunctionType.INPUT_TEXT && textInput.isBlank()) {
                executionResult = l10n("执行失败：输入文本不能为空", "Failed: input text cannot be empty")
                return
            }
            pendingBatchCommand = command
            showBatchConfirm = true
            executionResult = l10n("等待批量执行确认...", "Awaiting batch confirmation...")
            return
        }
        if (!commonCommandRunEnabled(selectedDevice)) {
            executionResult = l10n("执行失败：未选择设备", "Failed: no device selected")
            return
        }
        executionResult = l10n("执行中...", "Running...")

        coroutineScope.launch {
            runCatching {
                when (command.trigger) {
                    CommandTrigger.PACKAGE_INPUT -> {
                        val pkg = packageNameInput.trim()
                        if (pkg.isBlank()) {
                            executionResult = l10n("执行失败：包名不能为空", "Failed: package name cannot be empty")
                            return@launch
                        }
                        val success = withContext(Dispatchers.IO) {
                            when (command.actionType) {
                                AdbFunctionType.LAUNCH_APP_BY_PACKAGE -> AdbTool.startApp(pkg)
                                AdbFunctionType.STOP_APP_BY_PACKAGE -> AdbTool.stopApp(pkg)
                                AdbFunctionType.CLEAR_CACHE_AND_RESTART -> {
                                    val cleared = AdbTool.clearAppData(pkg)
                                    cleared && AdbTool.startApp(pkg)
                                }
                                else -> false
                            }
                        }
                        executionResult = if (success) {
                            "${l10n("执行成功", "Success")}：$resolvedCommandPreview"
                        } else {
                            "${l10n("执行失败", "Failed")}：$resolvedCommandPreview"
                        }
                    }

                    CommandTrigger.SHELL_INPUT -> {
                        val shell = (command.shellCommand ?: shellCommandInput.trim())
                            .replace("{package}", packageNameInput.trim())
                            .replace("{text}", textInput.trim())
                        if (shell.isBlank()) {
                            executionResult = l10n("执行失败：Shell 命令不能为空", "Failed: shell command cannot be empty")
                            return@launch
                        }
                        val output = withContext(Dispatchers.IO) {
                            AdbTool.execShell(shell)
                        }
                        executionResult = output.ifBlank { l10n("执行完成，无输出", "Done, no output") }
                    }

                    CommandTrigger.DIRECT -> {
                        if (!command.shellCommand.isNullOrBlank()) {
                            val resolvedShell = command.shellCommand
                                .replace("{package}", packageNameInput.trim())
                                .replace("{text}", textInput.trim())
                            val output = withContext(Dispatchers.IO) {
                                AdbTool.execShell(resolvedShell)
                            }
                            executionResult = output.ifBlank { l10n("执行完成，无输出", "Done, no output") }
                            return@launch
                        }

                        when (command.actionType) {
                            null -> {
                                val args = customAdbArgs(
                                    rawCommand = command.commandPreview,
                                    deviceId = selectedDevice,
                                    packageName = packageNameInput,
                                    text = textInput
                                )
                                if (args.isEmpty()) {
                                    executionResult = l10n("执行失败：ADB 命令不能为空", "Failed: ADB command cannot be empty")
                                    return@launch
                                }
                                val result = withContext(Dispatchers.IO) {
                                    AdbTool.execAdbAsync(*args.toTypedArray())
                                }
                                executionResult = AdbTool.outputText(result).ifBlank {
                                    l10n("执行完成，无输出", "Done, no output")
                                }
                            }

                            AdbFunctionType.INPUT_TEXT -> {
                                val input = textInput
                                if (input.isBlank()) {
                                    executionResult = l10n("执行失败：输入文本不能为空", "Failed: input text cannot be empty")
                                    return@launch
                                }
                                val success = withContext(Dispatchers.IO) {
                                    AdbTool.inputText(input)
                                }
                                executionResult = if (success) {
                                    "${l10n("执行成功", "Success")}：$resolvedCommandPreview"
                                } else {
                                    "${l10n("执行失败", "Failed")}：$resolvedCommandPreview"
                                }
                            }

                            AdbFunctionType.VIEW_CURRENT_ACTIVITY -> {
                                val output = withContext(Dispatchers.IO) {
                                    AdbTool.execShell("dumpsys window | grep mCurrentFocus")
                                }
                                executionResult = output.ifBlank { l10n("执行完成，无输出", "Done, no output") }
                            }

                            AdbFunctionType.KEY_BACK -> {
                                withContext(Dispatchers.IO) { AdbTool.execShell("input keyevent 4") }
                                executionResult = "${l10n("执行成功", "Success")}：$resolvedCommandPreview"
                            }

                            AdbFunctionType.KEY_HOME -> {
                                withContext(Dispatchers.IO) { AdbTool.execShell("input keyevent 3") }
                                executionResult = "${l10n("执行成功", "Success")}：$resolvedCommandPreview"
                            }

                            AdbFunctionType.NETWORK_STATUS -> {
                                val output = withContext(Dispatchers.IO) { AdbTool.execShell("dumpsys connectivity") }
                                executionResult = output.ifBlank { l10n("执行完成，无输出", "Done, no output") }
                            }

                            AdbFunctionType.REBOOT_DEVICE -> {
                                showRebootConfirm = true
                                pendingRebootCommand = command
                                executionResult = l10n("等待确认...", "Awaiting confirmation...")
                                return@launch
                            }

                            AdbFunctionType.DEVELOPER_OPTIONS -> {
                                withContext(Dispatchers.IO) {
                                    AdbTool.execShell("am start -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS")
                                }
                                executionResult = "${l10n("执行成功", "Success")}：$resolvedCommandPreview"
                            }

                            else -> {
                                command.actionType?.let(viewModel::executeAdbAction)
                                executionResult = l10n(
                                    "已触发执行：$resolvedCommandPreview\n具体结果请查看底部提示",
                                    "Triggered: $resolvedCommandPreview\nSee the bottom notification for details"
                                )
                            }
                        }
                    }
                }
            }.onFailure { error ->
                executionResult = l10n("执行异常", "Error") + "：${error.message ?: l10n("未知错误", "Unknown error")}"
            }
        }
    }

    Scaffold(containerColor = Color.Transparent) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = UiTokens.PagePaddingCompact, vertical = UiTokens.SectionSpacingCompact),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            CommandCenterHeader(
                searchKeyword = searchKeyword,
                onSearchKeywordChange = { searchKeyword = it },
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onAddCommand = { showAddCommandDialog = true },
                batchMode = batchMode,
                onBatchModeChange = { enabled ->
                    batchMode = enabled
                    if (enabled && selectedBatchDeviceIds.isEmpty()) {
                        selectedBatchDeviceIds = selectedDevice?.takeIf { it in devices }?.let(::setOf).orEmpty()
                    }
                }
            )

            if (!batchMode && !commonCommandRunEnabled(selectedDevice)) {
                InlineStatusBanner(
                    text = l10n("当前没有选择设备。命令可以浏览和复制，但执行前需要先连接并选择设备。", "No device is selected. Commands can be browsed and copied, but running them needs a connected selected device."),
                    tone = InlineStatusTone.Warning,
                    icon = IconParkIcons.Info
                )
            }

            if (batchMode) {
                BatchDeviceSelector(
                    devices = devices,
                    deviceDisplayNames = deviceDisplayNames,
                    selectedDeviceIds = selectedBatchDeviceIds,
                    expanded = batchDevicesExpanded,
                    onExpandedChange = { batchDevicesExpanded = it },
                    onSelectionChange = { selectedBatchDeviceIds = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
            ) {
                CommandLibraryPanel(
                    commands = filteredCommands,
                    selectedCommandId = selectedCommandId,
                    favoriteIds = favoriteIds,
                    onSelectCommand = { selectedCommandId = it.id },
                    onToggleFavorite = { cmd ->
                        favoriteIds = CommandFavoritesManager.toggle(cmd.id)
                    },
                    onEditCustomCommand = { cmd ->
                        val cc = customCommands.firstOrNull { it.id == cmd.id }
                        if (cc != null) editingCommand = cc
                    },
                    onDeleteCustomCommand = { cmd ->
                        customCommands = CustomCommandManager.remove(cmd.id)
                        if (selectedCommandId == cmd.id) {
                            selectedCommandId = mergedCommands.firstOrNull { it.id != cmd.id }?.id.orEmpty()
                        }
                    },
                    modifier = Modifier.width(330.dp)
                )

                CommandDetailPanel(
                    selectedCommand = selectedCommand,
                    resolvedCommandPreview = resolvedCommandPreview,
                    selectedDevice = selectedDevice,
                    packageNameInput = packageNameInput,
                    onPackageNameInputChange = { packageNameInput = it },
                    shellCommandInput = shellCommandInput,
                    onShellCommandInputChange = { shellCommandInput = it },
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    executionResult = executionResult,
                    onCopyCommand = ::copyCommand,
                    onClearResult = { executionResult = l10n("等待执行", "Ready") },
                    onExecuteCommand = ::executeCommand,
                    canRunCommand = if (batchMode) {
                        selectedBatchDeviceIds.isNotEmpty() && selectedCommand?.supportsBatchExecution() == true
                    } else {
                        commonCommandRunEnabled(selectedDevice)
                    },
                    modifier = Modifier.weight(1f)
                )

                if (batchState.visible) {
                    BatchResultPanel(
                        state = batchState,
                        deviceDisplayNames = deviceDisplayNames,
                        onRetryFailed = batchViewModel::retryFailed,
                        onClose = batchViewModel::closePanel,
                        modifier = Modifier.widthIn(min = 320.dp, max = 400.dp)
                    )
                }
            }
        }
    }

    if (showBatchConfirm) {
        val command = pendingBatchCommand
        val destructive = commonBatchCommandIsDestructive(command?.actionType)
        AlertDialog(
            onDismissRequest = {
                showBatchConfirm = false
                pendingBatchCommand = null
            },
            title = {
                Text(command?.let(::displayCommandTitle) ?: l10n("批量执行", "Batch execution"))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                    Text(
                        if (destructive) {
                            l10n(
                                "此操作不可撤销，将影响 ${selectedBatchDeviceIds.size} 台设备。",
                                "This cannot be undone and will affect ${selectedBatchDeviceIds.size} devices."
                            )
                        } else {
                            l10n(
                                "将在 ${selectedBatchDeviceIds.size} 台设备上执行：",
                                "Run on ${selectedBatchDeviceIds.size} devices:"
                            )
                        },
                        color = if (destructive) QadbColors.danger else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        resolvedCommandPreview,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    selectedBatchDeviceIds.forEach { deviceId ->
                        Text("• ${deviceDisplayNames[deviceId] ?: deviceId}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val confirmedCommand = pendingBatchCommand ?: return@TextButton
                        val targets = selectedBatchDeviceIds.toList()
                        val confirmedPackageName = packageNameInput
                        val confirmedShellCommand = shellCommandInput
                        val confirmedText = textInput
                        showBatchConfirm = false
                        pendingBatchCommand = null
                        executionResult = l10n("批量执行中...", "Batch running...")
                        batchViewModel.execute(
                            commandTitle = displayCommandTitle(confirmedCommand),
                            commandPreview = resolvedCommandPreview,
                            deviceIds = targets
                        ) { deviceId ->
                            executeCommandOnDevice(
                                confirmedCommand, deviceId,
                                confirmedPackageName, confirmedShellCommand, confirmedText
                            )
                        }
                    },
                    modifier = if (destructive) {
                        Modifier.background(QadbColors.danger.copy(alpha = 0.12f), RoundedCornerShape(UiTokens.RadiusSmall))
                    } else Modifier
                ) {
                    Text(
                        l10n("确认执行", "Run"),
                        color = if (destructive) QadbColors.danger else MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBatchConfirm = false
                    pendingBatchCommand = null
                    executionResult = l10n("已取消", "Cancelled")
                }) { Text(l10n("取消", "Cancel")) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showRebootConfirm) {
        AlertDialog(
            onDismissRequest = {
                showRebootConfirm = false
                pendingRebootCommand = null
            },
            title = { Text(l10n("确认重启设备", "Confirm Device Reboot")) },
            text = {
                Text(
                    l10n(
                        "即将重启连接的设备，设备上的未保存数据可能丢失。是否继续？",
                        "The connected device will reboot. Unsaved data may be lost. Continue?"
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val rebootShell = pendingRebootCommand?.shellCommand ?: "reboot"
                    val rebootPreview = pendingRebootCommand?.commandPreview ?: "adb shell reboot"
                    showRebootConfirm = false
                    executionResult = l10n("执行中...", "Running...")
                    coroutineScope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) { AdbTool.execShell(rebootShell) }
                        }.onSuccess {
                            executionResult = "${l10n("执行成功", "Success")}：$rebootPreview"
                        }.onFailure {
                            executionResult = "${l10n("执行失败", "Failed")}：${it.message}"
                        }
                        pendingRebootCommand = null
                    }
                }) { Text(l10n("确认重启", "Reboot")) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRebootConfirm = false
                    pendingRebootCommand = null
                    executionResult = l10n("已取消", "Cancelled")
                }) {
                    Text(l10n("取消", "Cancel"))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAddCommandDialog || editingCommand != null) {
        CustomCommandDialog(
            initialCommand = editingCommand,
            onDismiss = {
                showAddCommandDialog = false
                editingCommand = null
            },
            onConfirm = { cmd ->
                val savedCommands = if (editingCommand != null) {
                    CustomCommandManager.update(cmd)
                } else {
                    CustomCommandManager.add(cmd)
                }
                if (editingCommand != null) {
                    customCommands = savedCommands
                    selectedCommandId = cmd.id
                } else {
                    customCommands = savedCommands
                    selectedCommandId = cmd.id
                    selectedCategory = "all"
                }
                showAddCommandDialog = false
                editingCommand = null
            }
        )
    }

}

@Composable
private fun rememberCommandLibrary(): List<CommandItemUi> {
    return remember {
        loadCommandLibraryFromConfig().ifEmpty { fallbackCommands() }
    }
}

private fun loadCommandLibraryFromConfig(): List<CommandItemUi> {
    val fileName = if (l10n("zh", "en") == "en") "adb_commands_en.json" else "adb_commands.json"
    val stream = CommonModel::class.java.classLoader?.getResourceAsStream(fileName)
        ?: CommonModel::class.java.getResourceAsStream("/$fileName")
        ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(fileName)
        ?: ClassLoader.getSystemResourceAsStream(fileName)
        ?: return emptyList()

    val jsonText = InputStreamReader(stream, Charsets.UTF_8).use { it.readText() }
    val root = runCatching { Json.parseToJsonElement(jsonText).jsonArray }.getOrElse { return emptyList() }

    return root.mapNotNull { entry ->
        val obj = entry.jsonObject
        val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val description = obj["description"]?.jsonPrimitive?.contentOrNull ?: ""
        val commandPreview = obj["commandPreview"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val categoryKey = obj["category"]?.jsonPrimitive?.contentOrNull ?: "device"
        val iconKey = obj["icon"]?.jsonPrimitive?.contentOrNull ?: "info"
        val color = parseColor(obj["color"]?.jsonPrimitive?.contentOrNull)
        val trigger = obj["trigger"]?.jsonPrimitive?.contentOrNull
            ?.let { runCatching { CommandTrigger.valueOf(it) }.getOrNull() }
            ?: CommandTrigger.DIRECT
        val actionType = obj["actionType"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { AdbFunctionType.valueOf(it) }.getOrNull() }
        val shellCommand = obj["shellCommand"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        val hints = obj["hints"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

        CommandItemUi(
            id = id,
            title = title,
            description = description,
            commandPreview = commandPreview,
            categoryKey = categoryKey,
            icon = resolveIcon(iconKey),
            iconTint = color,
            trigger = trigger,
            actionType = actionType,
            shellCommand = shellCommand,
            hints = hints
        )
    }
}

private fun parseColor(raw: String?): Color {
    if (raw.isNullOrBlank()) return QadbPalette.Blue
    return runCatching {
        val hex = raw.removePrefix("#")
        val value = when (hex.length) {
            6 -> 0xFF000000 or hex.toLong(16)
            8 -> hex.toLong(16)
            else -> 0xFF3D73FF
        }
        Color(value)
    }.getOrDefault(QadbPalette.Blue)
}

private fun resolveIcon(iconKey: String): ImageVector = when (iconKey) {
    "visibility" -> Icons.Default.Visibility
    "install" -> Icons.Default.InstallMobile
    "play" -> Icons.Default.PlayArrow
    "stop" -> Icons.Default.Stop
    "delete" -> Icons.Default.DeleteSweep
    "code" -> IconParkIcons.Code
    "edit" -> Icons.Default.Edit
    "back" -> IconParkIcons.Close
    "home" -> IconParkIcons.Home
    "camera" -> IconParkIcons.Camera
    "screen" -> IconParkIcons.CastScreen
    "log" -> Icons.Default.Memory
    "folder" -> IconParkIcons.Folder
    "wifi" -> IconParkIcons.Wifi
    "reboot" -> Icons.Default.RestartAlt
    else -> IconParkIcons.Info
}

private fun categoryIconFor(categoryKey: String): ImageVector = when (categoryKey) {
    "device" -> IconParkIcons.Home
    "app" -> Icons.Default.InstallMobile
    "file" -> IconParkIcons.Folder
    "input" -> Icons.Default.Edit
    "screen" -> IconParkIcons.Camera
    "network" -> IconParkIcons.Wifi
    "log" -> Icons.Default.Memory
    "tv" -> IconParkIcons.CastScreen
    else -> IconParkIcons.Info
}

private fun deviceDisplayLabel(selectedDevice: String?): String {
    return selectedDevice?.takeIf { it.isNotBlank() }
        ?: l10n("未选择设备", "No device selected")
}

internal fun customAdbArgs(
    rawCommand: String,
    deviceId: String?,
    packageName: String,
    text: String
): List<String> {
    val resolved = rawCommand
        .replace("{package}", packageName.trim())
        .replace("{text}", text.trim())
        .trim()
    if (resolved.isBlank()) return emptyList()

    val tokens = tokenizeCommandLine(resolved).let { parts ->
        if (parts.firstOrNull() == "adb") parts.drop(1) else parts
    }
    if (tokens.isEmpty()) return emptyList()

    val hasExplicitDevice = tokens.withIndex().any { (index, token) ->
        token == "-d" ||
            token == "-e" ||
            token == "--one-device" ||
            token == "--serial" ||
            token.startsWith("--serial=") ||
            (token == "-s" && index + 1 < tokens.size)
    }

    return if (!hasExplicitDevice && customAdbSubcommandUsesSelectedDevice(tokens) && !deviceId.isNullOrBlank()) {
        listOf("-s", deviceId.trim()) + tokens
    } else {
        tokens
    }
}

internal fun resolvedCustomAdbPreview(
    rawCommand: String,
    deviceId: String?,
    packageName: String,
    text: String
): String {
    val args = customAdbArgs(rawCommand, deviceId, packageName, text)
    return if (args.isEmpty()) {
        "adb"
    } else {
        "adb ${args.joinToString(" ")}"
    }
}

private fun customAdbSubcommandUsesSelectedDevice(args: List<String>): Boolean {
    val firstCommand = args.firstOrNull { !it.startsWith("-") } ?: return true
    return firstCommand !in setOf(
        "connect",
        "disconnect",
        "devices",
        "help",
        "kill-server",
        "reconnect",
        "start-server",
        "version"
    )
}

private fun tokenizeCommandLine(input: String): List<String> {
    val regex = Regex("\"([^\"]*)\"|'([^']*)'|([^\\s]+)")
    return regex.findAll(input)
        .mapNotNull { match ->
            match.groups[1]?.value
                ?: match.groups[2]?.value
                ?: match.groups[3]?.value
        }
        .toList()
}

private fun commandItemAccentColor(index: Int): Color = listOf(
    QadbPalette.Blue,
    QadbPalette.Purple,
    QadbPalette.Orange,
    QadbPalette.Green
)[index.mod(4)]

private fun fallbackCommands(): List<CommandItemUi> = listOf(
    CommandItemUi(
        id = "fallback_shell",
        title = l10n("执行 Shell 命令", "Run Shell Command"),
        description = l10n("配置文件读取失败时的回退命令", "Fallback command when config loading fails"),
        commandPreview = "adb shell getprop",
        categoryKey = "device",
        icon = IconParkIcons.Code,
        iconTint = QadbPalette.Blue,
        trigger = CommandTrigger.SHELL_INPUT,
        actionType = AdbFunctionType.OPEN_SHELL
    )
)

@Composable
private fun BatchDeviceSelector(
    devices: List<String>,
    deviceDisplayNames: Map<String, String>,
    selectedDeviceIds: Set<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectionChange: (Set<String>) -> Unit
) {
    Card(
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        colors = CardDefaults.cardColors(containerColor = QadbColors.surfaceSelected),
        border = BorderStroke(1.dp, QadbColors.selectedBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(UiTokens.SpaceMedium)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenSearchDesktop,
                        contentDescription = null,
                        tint = QadbColors.primary,
                        modifier = Modifier.size(UiTokens.IconMedium)
                    )
                    Text(
                        l10n("已选择 ${selectedDeviceIds.size} 台设备", "${selectedDeviceIds.size} devices selected"),
                        fontWeight = FontWeight.SemiBold,
                        color = QadbColors.primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = QadbColors.primary,
                    modifier = Modifier.size(UiTokens.IconMedium)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(UiTokens.SpaceSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    TextButton(
                        onClick = { onSelectionChange(devices.toSet()) },
                        enabled = devices.isNotEmpty()
                    ) { Text(l10n("全选在线设备", "Select all online"), color = QadbColors.primary) }
                    TextButton(
                        onClick = { onSelectionChange(emptySet()) },
                        enabled = selectedDeviceIds.isNotEmpty()
                    ) { Text(l10n("清空", "Clear")) }
                }

                if (devices.isEmpty()) {
                    Text(
                        l10n("没有在线设备", "No online devices"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        devices.forEach { deviceId ->
                            val checked = deviceId in selectedDeviceIds
                            Row(
                                modifier = Modifier
                                    .widthIn(min = 210.dp)
                                    .background(
                                        if (checked) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        RoundedCornerShape(UiTokens.RadiusSmall)
                                    )
                                    .clickable {
                                        onSelectionChange(
                                            if (checked) selectedDeviceIds - deviceId else selectedDeviceIds + deviceId
                                        )
                                    }
                                    .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        onSelectionChange(
                                            if (isChecked) selectedDeviceIds + deviceId else selectedDeviceIds - deviceId
                                        )
                                    }
                                )
                                Box(
                                    Modifier.size(8.dp).background(QadbColors.success, RoundedCornerShape(UiTokens.BadgeRadius))
                                )
                                Column {
                                    Text(
                                        deviceDisplayNames[deviceId] ?: deviceId,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (deviceDisplayNames.containsKey(deviceId)) {
                                        Text(
                                            deviceId,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
}

@Composable
private fun BatchResultPanel(
    state: BatchExecutionUiState,
    deviceDisplayNames: Map<String, String>,
    onRetryFailed: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDeviceIds by remember(state.commandTitle, state.commandPreview) { mutableStateOf(emptySet<String>()) }
    Card(
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(UiTokens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.commandTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        l10n(
                            "${state.completedCount}/${state.devices.size} 完成",
                            "${state.completedCount}/${state.devices.size} complete"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onClose, enabled = !state.isRunning) { Text(l10n("关闭", "Close")) }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                itemsIndexed(state.devices.values.toList(), key = { _, item -> item.deviceId }) { _, result ->
                    BatchResultRow(
                        result = result,
                        displayName = deviceDisplayNames[result.deviceId] ?: result.deviceId,
                        expanded = result.deviceId in expandedDeviceIds,
                        onToggleExpanded = {
                            expandedDeviceIds = if (result.deviceId in expandedDeviceIds) {
                                expandedDeviceIds - result.deviceId
                            } else {
                                expandedDeviceIds + result.deviceId
                            }
                        }
                    )
                }
            }

            if (!state.isRunning && state.failedCount > 0) {
                CommandActionButton(
                    text = l10n("重试失败的 ${state.failedCount} 台", "Retry ${state.failedCount} failed"),
                    icon = Icons.Default.RestartAlt,
                    onClick = onRetryFailed,
                    primary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BatchResultRow(
    result: BatchDeviceResult,
    displayName: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val statusColor = when (result.status) {
        BatchDeviceStatus.PENDING, BatchDeviceStatus.RUNNING -> QadbColors.primary
        BatchDeviceStatus.SUCCESS -> QadbColors.success
        BatchDeviceStatus.FAILED -> QadbColors.danger
    }
    val statusMark = when (result.status) {
        BatchDeviceStatus.PENDING -> "…"
        BatchDeviceStatus.RUNNING -> "↻"
        BatchDeviceStatus.SUCCESS -> "✓"
        BatchDeviceStatus.FAILED -> "×"
    }
    val summary = when (result.status) {
        BatchDeviceStatus.PENDING -> l10n("等待中", "Pending")
        BatchDeviceStatus.RUNNING -> l10n("执行中", "Running")
        BatchDeviceStatus.SUCCESS -> l10n("成功 · ${result.durationMillis ?: 0} ms", "Success · ${result.durationMillis ?: 0} ms")
        BatchDeviceStatus.FAILED -> result.errorMessage ?: result.output.ifBlank { l10n("命令执行失败", "Command failed") }
    }
    val details = listOf(result.output, result.errorMessage.orEmpty()).filter(String::isNotBlank).distinct().joinToString("\n")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(UiTokens.RadiusSmall))
            .clickable(enabled = details.isNotBlank()) { onToggleExpanded() }
            .padding(UiTokens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
            Text(statusMark, color = statusColor, fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (expanded && details.isNotBlank()) {
            SelectionContainer {
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CommandCenterHeader(
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    categories: List<CommandCategoryUi>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onAddCommand: () -> Unit,
    batchMode: Boolean,
    onBatchModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
            ) {
                Text(
                    text = l10n("命令中心", "Command Center"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = l10n("浏览、收藏、编辑并执行常用 ADB 命令", "Browse, favorite, edit and run common ADB commands"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = searchKeyword,
                onValueChange = onSearchKeywordChange,
                leadingIcon = {
                    Icon(
                        imageVector = IconParkIcons.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(UiTokens.IconMedium)
                    )
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(UiTokens.RadiusSmall))
                            .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall)
                    ) {
                        Text(
                            text = "⌘K",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                singleLine = true,
                placeholder = { Text(l10n("搜索命令、分类、包名或参数", "Search commands, categories, packages or args")) },
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(UiTokens.RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .widthIn(min = 380.dp, max = 520.dp)
                    .heightIn(min = 44.dp)
            )

            CommandActionButton(
                text = l10n("新建命令", "New Command"),
                icon = Icons.Default.Add,
                onClick = onAddCommand,
                primary = true,
                modifier = Modifier.height(38.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            CommandActionButton(
                text = l10n("批量模式", "Batch"),
                icon = Icons.Default.ScreenSearchDesktop,
                onClick = { onBatchModeChange(!batchMode) },
                primary = batchMode,
                modifier = Modifier.width(120.dp).height(32.dp)
            )
        }

        CategoryTabs(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryTabs(
    categories: List<CommandCategoryUi>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
        maxItemsInEachRow = Int.MAX_VALUE
    ) {
        categories.forEach { category ->
            val selected = category.key == selectedCategory
            val chipBg = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                Color.Transparent
            }
            val chipColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            Row(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(UiTokens.RadiusMedium))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCategorySelected(category.key) }
                    .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = chipColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CommandLibraryPanel(
    commands: List<CommandItemUi>,
    selectedCommandId: String,
    favoriteIds: Set<String>,
    onSelectCommand: (CommandItemUi) -> Unit,
    onToggleFavorite: (CommandItemUi) -> Unit,
    onEditCustomCommand: (CommandItemUi) -> Unit,
    onDeleteCustomCommand: (CommandItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = UiTokens.SpaceXSmall, top = UiTokens.SpaceXSmall, end = UiTokens.SpaceMedium, bottom = UiTokens.SpaceXSmall),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                val listState = rememberLazyListState()
                if (commands.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = l10n("未找到匹配命令", "No matching commands"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                        contentPadding = PaddingValues(bottom = UiTokens.SpaceXSmall),
                        modifier = Modifier.fillMaxSize().padding(end = UiTokens.SpaceSmall)
                    ) {
                        itemsIndexed(commands, key = { _, item -> item.id }) { index, command ->
                            CommandListItem(
                                command = command,
                                itemIndex = index,
                                selected = selectedCommandId == command.id,
                                isFavorite = command.id in favoriteIds,
                                onSelect = { onSelectCommand(command) },
                                onToggleFavorite = { onToggleFavorite(command) },
                                onEditCustom = if (command.id.startsWith("custom_")) {
                                    { onEditCustomCommand(command) }
                                } else null,
                                onDeleteCustom = if (command.id.startsWith("custom_")) {
                                    { onDeleteCustomCommand(command) }
                                } else null
                            )
                        }
                    }
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UiTokens.SpaceXSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = l10n("更多命令...", "More commands..."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = l10n("共 ${commands.size} 条", "${commands.size} total"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun CommandListItem(
    command: CommandItemUi,
    itemIndex: Int,
    selected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditCustom: (() -> Unit)? = null,
    onDeleteCustom: (() -> Unit)? = null
) {
    val accent = commandItemAccentColor(itemIndex)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    var showContextMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor, RoundedCornerShape(UiTokens.RadiusMedium))
                .border(1.dp, borderColor, RoundedCornerShape(UiTokens.RadiusMedium))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onSelect() }
                .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.82f))), RoundedCornerShape(UiTokens.RadiusMedium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = command.icon,
                    contentDescription = command.title,
                    tint = QadbColors.onPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
            ) {
                Text(
                    text = displayCommandTitle(command),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = command.description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            com.ludoven.adbtool.ui.mac.IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) l10n("取消收藏", "Unfavorite") else l10n("收藏", "Favorite"),
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(17.dp)
                )
            }

            if (onEditCustom != null || onDeleteCustom != null) {
                com.ludoven.adbtool.ui.mac.IconButton(
                    onClick = { showContextMenu = true },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = l10n("更多操作", "More actions"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(UiTokens.IconSmall)
                    )
                }
            }
        }

        com.ludoven.adbtool.ui.mac.DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            if (onEditCustom != null) {
                com.ludoven.adbtool.ui.mac.DropdownMenuItem(
                    text = { Text(l10n("编辑命令", "Edit command")) },
                    onClick = {
                        showContextMenu = false
                        onEditCustom()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(UiTokens.IconSmall)
                        )
                    }
                )
            }
            if (onDeleteCustom != null) {
                com.ludoven.adbtool.ui.mac.DropdownMenuItem(
                    text = { Text(l10n("删除命令", "Delete command")) },
                    onClick = {
                        showContextMenu = false
                        onDeleteCustom()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(UiTokens.IconSmall)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CommandDetailPanel(
    selectedCommand: CommandItemUi?,
    resolvedCommandPreview: String,
    selectedDevice: String?,
    packageNameInput: String,
    onPackageNameInputChange: (String) -> Unit,
    shellCommandInput: String,
    onShellCommandInputChange: (String) -> Unit,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    executionResult: String,
    onCopyCommand: (String) -> Unit,
    onClearResult: () -> Unit,
    onExecuteCommand: (CommandItemUi) -> Unit,
    canRunCommand: Boolean,
    modifier: Modifier = Modifier
) {
    if (selectedCommand == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = l10n("请从左侧选择一条命令", "Select a command from the left"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = UiTokens.SpaceXSmall, top = UiTokens.SpaceXSmall, end = UiTokens.SpaceXSmall, bottom = UiTokens.SpaceXSmall),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = l10n("命令详情", "Command Details"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                    CommandActionButton(
                        text = l10n("复制", "Copy"),
                        icon = Icons.Default.ContentCopy,
                        onClick = { onCopyCommand(resolvedCommandPreview) }
                    )
                    CommandActionButton(
                        text = l10n("运行命令", "Run"),
                        icon = Icons.Default.PlayArrow,
                        onClick = { onExecuteCommand(selectedCommand) },
                        enabled = canRunCommand,
                        primary = true
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = UiTokens.SpaceXSmall),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(selectedCommand.iconTint, selectedCommand.iconTint.copy(alpha = 0.8f))
                                ),
                                RoundedCornerShape(UiTokens.RadiusMedium)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedCommand.icon,
                            contentDescription = selectedCommand.title,
                            tint = QadbColors.onPrimary,
                            modifier = Modifier.size(UiTokens.IconLarge)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
                    ) {
                        Text(
                            text = displayCommandTitle(selectedCommand),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = selectedCommand.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                Text(
                    text = l10n("完整命令预览", "Full command preview"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f), RoundedCornerShape(UiTokens.RadiusMedium))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onCopyCommand(resolvedCommandPreview) }
                        .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        Text(
                            text = "adb",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = resolvedCommandPreview.removePrefix("adb").trimStart(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = l10n("复制命令", "Copy command"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(UiTokens.IconMedium)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Text(
                        text = when (selectedCommand.trigger) {
                            CommandTrigger.PACKAGE_INPUT -> l10n("应用包名（可编辑）", "Package name")
                            CommandTrigger.SHELL_INPUT -> l10n("Shell 参数（可编辑）", "Shell args")
                            CommandTrigger.DIRECT -> if (selectedCommand.actionType == AdbFunctionType.INPUT_TEXT) {
                                l10n("输入文本（可编辑）", "Input text")
                            } else {
                                l10n("命令参数", "Command args")
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    when (selectedCommand.trigger) {
                        CommandTrigger.PACKAGE_INPUT -> {
                            OutlinedTextField(
                                value = packageNameInput,
                                onValueChange = onPackageNameInputChange,
                                singleLine = true,
                                placeholder = { Text("com.example.app") },
                                shape = RoundedCornerShape(UiTokens.RadiusMedium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                            )
                        }

                        CommandTrigger.SHELL_INPUT -> {
                            OutlinedTextField(
                                value = shellCommandInput,
                                onValueChange = onShellCommandInputChange,
                                singleLine = true,
                                placeholder = { Text("getprop") },
                                shape = RoundedCornerShape(UiTokens.RadiusMedium),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                            )
                        }

                        CommandTrigger.DIRECT -> {
                            if (selectedCommand.actionType == AdbFunctionType.INPUT_TEXT) {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = onTextInputChange,
                                    singleLine = true,
                                    placeholder = { Text("hello") },
                                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 44.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(UiTokens.ListRowHeight)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(UiTokens.RadiusMedium))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                                        .padding(horizontal = UiTokens.SpaceMedium),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = l10n("无需额外参数", "No extra args"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Text(
                        text = l10n("目标设备", "Target device"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UiTokens.ListRowHeight)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(UiTokens.RadiusMedium))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                            .padding(horizontal = UiTokens.SpaceMedium),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .background(
                                        if (canRunCommand) QadbColors.success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                        RoundedCornerShape(UiTokens.BadgeRadius)
                                    )
                            )
                            Text(
                                text = deviceDisplayLabel(selectedDevice),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = IconParkIcons.ArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(UiTokens.IconMedium)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Text(
                    text = l10n("执行结果", "Execution result"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Card(
                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                    colors = CardDefaults.cardColors(containerColor = QadbPalette.TerminalBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, QadbPalette.TerminalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 240.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(UiTokens.SpaceMedium),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(QadbPalette.Green, RoundedCornerShape(UiTokens.BadgeRadius))
                                )
                                Text(
                                    text = l10n("执行状态", "Status"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QadbPalette.TerminalSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                                TerminalActionButton(l10n("清空", "Clear"), Icons.Default.Delete, onClearResult)
                                TerminalActionButton(l10n("复制", "Copy"), Icons.Default.ContentCopy) {
                                    onCopyCommand(executionResult)
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(QadbPalette.TerminalText.copy(alpha = 0.08f))
                        )
                        val resultScroll = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = UiTokens.SpaceSmall)
                                    .verticalScroll(resultScroll)
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = terminalResultText(resolvedCommandPreview, executionResult),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QadbPalette.TerminalText,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(resultScroll),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun terminalResultText(
    commandPreview: String,
    executionResult: String
): AnnotatedString = buildAnnotatedString {
    appendStyledLine("${'$'} $commandPreview", QadbPalette.TerminalBlue, FontWeight.SemiBold)
    executionResult.lines().forEachIndexed { index, line ->
        if (index > 0) append('\n')
        val color = when {
            line.contains("执行成功") || line.contains("Success", ignoreCase = true) -> QadbPalette.TerminalSuccess
            line.contains("执行失败") ||
                line.contains("执行异常") ||
                line.contains("Failed", ignoreCase = true) ||
                line.contains("Error", ignoreCase = true) -> QadbPalette.TerminalError
            line.contains("执行中") || line.contains("Running", ignoreCase = true) -> QadbPalette.TerminalWarning
            else -> QadbPalette.TerminalText
        }
        appendStyled(line, color)
    }
}

private fun AnnotatedString.Builder.appendStyledLine(
    text: String,
    color: Color,
    fontWeight: FontWeight? = null
) {
    appendStyled(text, color, fontWeight)
    append('\n')
}

private fun AnnotatedString.Builder.appendStyled(
    text: String,
    color: Color,
    fontWeight: FontWeight? = null
) {
    pushStyle(SpanStyle(color = color, fontWeight = fontWeight))
    append(text)
    pop()
}

@Composable
private fun TerminalActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 58.dp),
        contentPadding = PaddingValues(horizontal = UiTokens.SpaceSmall, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = QadbPalette.TerminalText.copy(alpha = 0.9f),
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                color = QadbPalette.TerminalText.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CommandActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        primary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val backgroundColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
        primary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
    }
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        primary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    }

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(32.dp)
            .background(backgroundColor, RoundedCornerShape(UiTokens.RadiusMedium))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(UiTokens.RadiusMedium))
            .padding(horizontal = UiTokens.SpaceSmall, vertical = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TipDialog(dialogText: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.tip_title)) },
        text = { Text(dialogText) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.confirm))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun CustomCommandDialog(
    initialCommand: CustomCommand?,
    onDismiss: () -> Unit,
    onConfirm: (CustomCommand) -> Unit
) {
    val isEditing = initialCommand != null
    var title by remember { mutableStateOf(initialCommand?.title.orEmpty()) }
    var commandPreview by remember { mutableStateOf(initialCommand?.commandPreview.orEmpty()) }
    var shellCommand by remember { mutableStateOf(initialCommand?.shellCommand.orEmpty()) }
    var description by remember { mutableStateOf(initialCommand?.description.orEmpty()) }
    var categoryKey by remember { mutableStateOf(initialCommand?.categoryKey ?: "device") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf(
        "device" to l10n("设备", "Device"),
        "app" to l10n("应用管理", "Apps"),
        "file" to l10n("文件操作", "Files"),
        "input" to l10n("输入控制", "Input"),
        "screen" to l10n("截图录屏", "Screen"),
        "network" to l10n("网络调试", "Network"),
        "log" to l10n("日志", "Logs"),
        "tv" to l10n("TV盒子", "TV")
    )
    val selectedCategoryLabel = categoryOptions.firstOrNull { it.first == categoryKey }?.second ?: categoryOptions[0].second

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                Row(
                    modifier = Modifier.widthIn(min = 520.dp, max = 560.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(UiTokens.RadiusMedium)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(UiTokens.IconMedium)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
                        Text(
                            text = if (isEditing) l10n("编辑自定义命令", "Edit Custom Command")
                            else l10n("添加自定义命令", "Add Custom Command"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = l10n("配置命令标题、执行内容和分类", "Configure title, command body and category"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .widthIn(min = 520.dp, max = 560.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .widthIn(min = 520.dp, max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
            ) {
                CustomCommandDialogField(
                    label = l10n("命令标题", "Command title"),
                    value = title,
                    onValueChange = { title = it },
                    placeholder = l10n("例：查看设备信息", "e.g.: Show device info"),
                    required = true
                )

                CustomCommandDialogField(
                    label = l10n("ADB 命令", "ADB command"),
                    value = commandPreview,
                    onValueChange = { commandPreview = it },
                    placeholder = l10n("例：adb shell getprop ro.build.display.id", "e.g.: adb shell getprop ro.build.display.id"),
                    required = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
                ) {
                    CustomCommandDialogField(
                        label = l10n("Shell 命令", "Shell command"),
                        value = shellCommand,
                        onValueChange = { shellCommand = it },
                        placeholder = l10n("可选，留空则直接执行 ADB 命令", "Optional, leave empty to run ADB command"),
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        Text(
                            text = l10n("分类", "Category"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box {
                            OutlinedTextField(
                                value = selectedCategoryLabel,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                shape = RoundedCornerShape(UiTokens.RadiusMedium),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 42.dp)
                                    .clickable { categoryExpanded = !categoryExpanded }
                            )
                            com.ludoven.adbtool.ui.mac.DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categoryOptions.forEach { (key, label) ->
                                    com.ludoven.adbtool.ui.mac.DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            categoryKey = key
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                CustomCommandDialogField(
                    label = l10n("描述", "Description"),
                    value = description,
                    onValueChange = { description = it },
                    placeholder = l10n("可选，命令的简要说明", "Optional, brief description of the command")
                )

                Text(
                    text = l10n("可使用 {package} 和 {text} 作为占位符。", "You can use {package} and {text} as placeholders."),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            CommandActionButton(
                text = if (isEditing) l10n("保存", "Save") else l10n("添加", "Add"),
                icon = Icons.Default.Add,
                onClick = {
                    val finalTitle = title.trim()
                    val finalCommand = commandPreview.trim()
                    if (finalTitle.isNotBlank() && finalCommand.isNotBlank()) {
                        val cmd = CustomCommand(
                            id = initialCommand?.id ?: "custom_${java.util.UUID.randomUUID()}",
                            title = finalTitle,
                            description = description.trim(),
                            commandPreview = finalCommand,
                            categoryKey = categoryKey,
                            shellCommand = shellCommand.trim()
                        )
                        onConfirm(cmd)
                    }
                },
                enabled = title.trim().isNotBlank() && commandPreview.trim().isNotBlank(),
                primary = true,
                modifier = Modifier.height(32.dp)
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 0.dp)
            ) {
                Text(l10n("取消", "Cancel"))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun CustomCommandDialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    required: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (required) {
                Text(
                    text = "*",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(UiTokens.RadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
        )
    }
}
