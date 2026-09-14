package com.ludoven.adbtool.pages

import com.ludoven.adbtool.ui.icons.AiAgentBrandIcon

import adbtool_desktop.composeapp.generated.resources.*
import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.agent_back_to_latest
import adbtool_desktop.composeapp.generated.resources.agent_badge_in_progress
import adbtool_desktop.composeapp.generated.resources.agent_collapse_panel
import adbtool_desktop.composeapp.generated.resources.agent_composer_shortcut
import adbtool_desktop.composeapp.generated.resources.agent_composer_waiting_device
import adbtool_desktop.composeapp.generated.resources.agent_composer_waiting_model
import adbtool_desktop.composeapp.generated.resources.agent_confirm_title
import adbtool_desktop.composeapp.generated.resources.agent_confirm_send
import adbtool_desktop.composeapp.generated.resources.agent_confirm_execute
import adbtool_desktop.composeapp.generated.resources.agent_expand
import adbtool_desktop.composeapp.generated.resources.agent_collapse
import adbtool_desktop.composeapp.generated.resources.agent_input_hint
import adbtool_desktop.composeapp.generated.resources.agent_new_task
import adbtool_desktop.composeapp.generated.resources.agent_no_device
import adbtool_desktop.composeapp.generated.resources.agent_obs_mode_title
import adbtool_desktop.composeapp.generated.resources.agent_obs_on
import adbtool_desktop.composeapp.generated.resources.agent_obs_off
import adbtool_desktop.composeapp.generated.resources.agent_obs_watching
import adbtool_desktop.composeapp.generated.resources.agent_obs_paused
import adbtool_desktop.composeapp.generated.resources.agent_obs_switch_desc
import adbtool_desktop.composeapp.generated.resources.agent_open_settings
import adbtool_desktop.composeapp.generated.resources.agent_open_devices
import adbtool_desktop.composeapp.generated.resources.agent_preview_title
import adbtool_desktop.composeapp.generated.resources.agent_preview_unavailable
import adbtool_desktop.composeapp.generated.resources.agent_quick_recognize
import adbtool_desktop.composeapp.generated.resources.agent_running
import adbtool_desktop.composeapp.generated.resources.agent_send
import adbtool_desktop.composeapp.generated.resources.agent_sensitive_reason
import adbtool_desktop.composeapp.generated.resources.agent_sensitive_desc
import adbtool_desktop.composeapp.generated.resources.agent_usage_status
import adbtool_desktop.composeapp.generated.resources.agent_topbar_android
import adbtool_desktop.composeapp.generated.resources.agent_topbar_android_unknown
import adbtool_desktop.composeapp.generated.resources.agent_text_mode
import adbtool_desktop.composeapp.generated.resources.agent_vision_mode
import adbtool_desktop.composeapp.generated.resources.agent_connected
import adbtool_desktop.composeapp.generated.resources.agent_disconnected
import adbtool_desktop.composeapp.generated.resources.agent_activity_progress
import adbtool_desktop.composeapp.generated.resources.agent_activity_steps_done
import adbtool_desktop.composeapp.generated.resources.agent_activity_budget_calls
import adbtool_desktop.composeapp.generated.resources.agent_activity_usage_calls
import adbtool_desktop.composeapp.generated.resources.agent_activity_budget_tokens
import adbtool_desktop.composeapp.generated.resources.agent_badge_completed
import adbtool_desktop.composeapp.generated.resources.agent_badge_waiting
import adbtool_desktop.composeapp.generated.resources.agent_badge_failed
import adbtool_desktop.composeapp.generated.resources.agent_badge_cancelled
import adbtool_desktop.composeapp.generated.resources.agent_tool_done
import adbtool_desktop.composeapp.generated.resources.agent_think_title
import adbtool_desktop.composeapp.generated.resources.agent_cmd_prefix
import adbtool_desktop.composeapp.generated.resources.agent_cmd_result
import adbtool_desktop.composeapp.generated.resources.agent_expand_panel
import adbtool_desktop.composeapp.generated.resources.agent_avatar_desc
import adbtool_desktop.composeapp.generated.resources.agent_page_id
import adbtool_desktop.composeapp.generated.resources.agent_changed
import adbtool_desktop.composeapp.generated.resources.agent_stable
import adbtool_desktop.composeapp.generated.resources.agent_token_title
import adbtool_desktop.composeapp.generated.resources.agent_token_compacted
import adbtool_desktop.composeapp.generated.resources.agent_token_usage
import adbtool_desktop.composeapp.generated.resources.agent_token_remaining
import adbtool_desktop.composeapp.generated.resources.agent_token_meter_desc
import adbtool_desktop.composeapp.generated.resources.agent_obs_recent
import adbtool_desktop.composeapp.generated.resources.agent_obs_ago
import adbtool_desktop.composeapp.generated.resources.agent_obs_freq
import adbtool_desktop.composeapp.generated.resources.agent_obs_observing
import adbtool_desktop.composeapp.generated.resources.agent_quick_settings
import adbtool_desktop.composeapp.generated.resources.agent_prompt_settings
import adbtool_desktop.composeapp.generated.resources.agent_prompt_recognize
import adbtool_desktop.composeapp.generated.resources.agent_ready_title
import adbtool_desktop.composeapp.generated.resources.agent_ready_desc
import adbtool_desktop.composeapp.generated.resources.agent_action_observe
import adbtool_desktop.composeapp.generated.resources.agent_action_find_app
import adbtool_desktop.composeapp.generated.resources.agent_action_launch
import adbtool_desktop.composeapp.generated.resources.agent_action_tap
import adbtool_desktop.composeapp.generated.resources.agent_action_tap_element
import adbtool_desktop.composeapp.generated.resources.agent_action_swipe
import adbtool_desktop.composeapp.generated.resources.agent_action_input
import adbtool_desktop.composeapp.generated.resources.agent_action_key
import adbtool_desktop.composeapp.generated.resources.agent_action_wait
import adbtool_desktop.composeapp.generated.resources.agent_action_finish
import adbtool_desktop.composeapp.generated.resources.agent_action_force_stop
import adbtool_desktop.composeapp.generated.resources.agent_action_clear_data
import adbtool_desktop.composeapp.generated.resources.agent_action_uninstall
import adbtool_desktop.composeapp.generated.resources.agent_action_reboot
import adbtool_desktop.composeapp.generated.resources.agent_public_tool_input
import adbtool_desktop.composeapp.generated.resources.file_browser_retry
import adbtool_desktop.composeapp.generated.resources.wireless_connection
import adbtool_desktop.composeapp.generated.resources.usb_connection
import adbtool_desktop.composeapp.generated.resources.cancel
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoven.adbtool.QadbTokens
import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.agent.AgentAction
import com.ludoven.adbtool.agent.AgentFailure
import com.ludoven.adbtool.agent.AgentFailureAction
import com.ludoven.adbtool.agent.AgentFailureCategory
import com.ludoven.adbtool.agent.AgentFailureCode
import com.ludoven.adbtool.agent.AgentFailureStage
import com.ludoven.adbtool.agent.AgentFailureSubsystem
import com.ludoven.adbtool.agent.AgentFeatureRuntime
import com.ludoven.adbtool.agent.AgentMessage
import com.ludoven.adbtool.agent.AgentMessageRole
import com.ludoven.adbtool.agent.AgentReadiness
import com.ludoven.adbtool.agent.resolveAgentReadiness
import com.ludoven.adbtool.agent.AgentObservationMode
import com.ludoven.adbtool.agent.AgentPublicActivityItem
import com.ludoven.adbtool.agent.AgentPublicActivityState
import com.ludoven.adbtool.agent.AgentPublicMetrics
import com.ludoven.adbtool.agent.AgentPublicRunStatus
import com.ludoven.adbtool.agent.AgentPublicStage
import com.ludoven.adbtool.agent.AgentPublicToolKind
import com.ludoven.adbtool.agent.AgentPublicToolResult
import com.ludoven.adbtool.agent.AgentPublicToolSummary
import com.ludoven.adbtool.agent.AgentRunPhase
import com.ludoven.adbtool.agent.AgentRunPresentation
import com.ludoven.adbtool.agent.AgentStep
import com.ludoven.adbtool.agent.SCREENSHOT_AGENT_HARD_ACTION_LIMIT
import com.ludoven.adbtool.entity.DeviceCenterInfoData
import com.ludoven.adbtool.entity.DeviceInfoData
import com.ludoven.adbtool.util.DesktopNotifier
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.ui.mac.AlertDialog
import com.ludoven.adbtool.ui.mac.Icon
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.OutlinedButton
import com.ludoven.adbtool.ui.mac.Surface
import com.ludoven.adbtool.ui.mac.Switch
import com.ludoven.adbtool.ui.mac.Text
import com.ludoven.adbtool.ui.mac.TextButton
import com.ludoven.adbtool.viewmodel.AiAgentViewModel
import com.ludoven.adbtool.viewmodel.DevicesViewModel
import com.ludoven.adbtool.widget.FramedStateSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skia.Image as SkiaImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_CONTEXT_WINDOW_TOKENS = 128_000

/** Phases in which the task actively occupies the agent (used for notifications). */
private val RUNNING_PHASES = setOf(
    AgentRunPhase.OBSERVING,
    AgentRunPhase.THINKING,
    AgentRunPhase.RETRYING,
    AgentRunPhase.EXECUTING,
    AgentRunPhase.VERIFYING,
    AgentRunPhase.AWAITING_CONFIRMATION
)

internal enum class AgentScreenLayout {
    SINGLE_COLUMN,
    OVERLAY_DEVICE_PANEL,
    PERMANENT_DEVICE_PANEL
}

internal fun agentScreenLayout(widthDp: Float): AgentScreenLayout = when {
    widthDp >= 1280f -> AgentScreenLayout.PERMANENT_DEVICE_PANEL
    widthDp >= 1080f -> AgentScreenLayout.OVERLAY_DEVICE_PANEL
    else -> AgentScreenLayout.SINGLE_COLUMN
}

internal fun agentLatestItemScrollOffset(): Int = 0

internal fun shouldShowAgentGuide(messages: List<AgentMessage>): Boolean =
    messages.none { it.role != AgentMessageRole.SYSTEM }

@Composable
fun AiAgentScreen(
    viewModel: AiAgentViewModel,
    devicesViewModel: DevicesViewModel,
    onOpenDevices: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val taskState by viewModel.state.collectAsState()
    val modelConfig by viewModel.modelConfig.collectAsState()
    val apiKeyAvailable by viewModel.apiKeyAvailable.collectAsState()
    val configurationReady by viewModel.configurationReady.collectAsState()
    val configurationChecked by viewModel.configurationChecked.collectAsState()
    val selectedDevice by devicesViewModel.selectedDevice.collectAsState()
    val devices by devicesViewModel.devices.collectAsState()
    val deviceNames by devicesViewModel.deviceDisplayNames.collectAsState()
    val deviceInfo by devicesViewModel.deviceInfo.collectAsState()
    val centerInfo by devicesViewModel.centerInfo.collectAsState()
    val agentFeaturePreferences = remember { AgentFeatureRuntime.preferences }
    val reduceMotion by agentFeaturePreferences.reduceMotion.collectAsState()
    var prompt by remember { mutableStateOf("") }
    var showModelDialog by remember { mutableStateOf(false) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var devicePanelCollapsed by remember { mutableStateOf(false) }
    var obsSwitchOn by remember(taskState.observationMode) {
        mutableStateOf(taskState.observationMode == AgentObservationMode.VISION)
    }
    val isConnected = selectedDevice != null && selectedDevice in devices
    val readiness = resolveAgentReadiness(
        deviceConnected = isConnected,
        configurationChecked = configurationChecked,
        modelReady = configurationReady
    )
    val contextWindowTokens = modelConfig.contextWindowTokens ?: DEFAULT_CONTEXT_WINDOW_TOKENS

    val canSend = prompt.isNotBlank() && !taskState.isRunning && readiness == AgentReadiness.READY
    val recognizePrompt = stringResource(Res.string.agent_prompt_recognize)

    LaunchedEffect(Unit) {
        viewModel.refreshConfigurationStatus()
        devicesViewModel.refreshDevices()
    }

    // Tray notifications for state changes the user may miss while the window is in the background.
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(Unit) {
        var wasAwaitingConfirmation = false
        var wasRunning = false
        snapshotFlow {
                Pair(
                    taskState.pendingConfirmation != null,
                    taskState.phase
                )
            }
            .collect { (awaitingConfirmation, phase) ->
                val focused = windowInfo.isWindowFocused
                if (awaitingConfirmation && !wasAwaitingConfirmation) {
                    if (!focused) {
                        DesktopNotifier.notify(
                            caption = "QADB · AI Agent",
                            message = l10n("任务正在等待你的确认", "A task is waiting for your confirmation")
                        )
                    }
                } else if (wasRunning && !awaitingConfirmation) {
                    when (phase) {
                        AgentRunPhase.COMPLETED -> if (!focused) {
                            DesktopNotifier.notify(
                                caption = "QADB · AI Agent",
                                message = l10n("任务已完成", "Task completed")
                            )
                        }
                        AgentRunPhase.FAILED -> if (!focused) {
                            DesktopNotifier.notify(
                                caption = "QADB · AI Agent",
                                message = l10n("任务执行失败", "Task failed"),
                                isError = true
                            )
                        }
                        else -> Unit
                    }
                }
                wasAwaitingConfirmation = awaitingConfirmation
                wasRunning = phase in RUNNING_PHASES
            }
    }

    fun submit(task: String) {
        val trimmed = task.trim()
        if (trimmed.isEmpty() || taskState.isRunning) return
        when (readiness) {
            AgentReadiness.DEVICE_REQUIRED -> {
                onOpenDevices()
                return
            }
            AgentReadiness.MODEL_REQUIRED -> {
                showModelDialog = true
                return
            }
            AgentReadiness.CHECKING -> return
            AgentReadiness.READY -> Unit
        }
        // startTask rejects the prompt with an inline error when no device is
        // selected; only clear the input when the task can actually be handed off.
        viewModel.startTask(trimmed, selectedDevice)
        if (isConnected) {
            prompt = ""
        }
    }

    fun handleQuickPrompt(task: String) {
        prompt = task
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QadbTokens.bg1)
            .onPreviewKeyEvent { event ->
                // Escape aborts a running task regardless of which descendant holds focus.
                if (event.type == KeyEventType.KeyDown && event.key == Key.Escape && taskState.isRunning) {
                    viewModel.cancelTask()
                    true
                } else {
                    false
                }
            }
    ) {
        AgentTopStatusBar(
            selectedDevice = selectedDevice,
            displayName = selectedDevice?.let(deviceNames::get),
            deviceInfo = deviceInfo,
            centerInfo = centerInfo,
            connected = isConnected,
            running = taskState.isRunning
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val layout = agentScreenLayout(maxWidth.value)
            val panelWidth = devicePanelWidth(maxWidth.value)
            LaunchedEffect(layout) {
                if (layout == AgentScreenLayout.SINGLE_COLUMN) {
                    devicePanelCollapsed = false
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AgentConversation(
                        messages = taskState.messages,
                        readiness = readiness,
                        errorMessage = taskState.errorMessage,
                        publicActivity = taskState.publicActivity,
                        pendingConfirmation = taskState.pendingConfirmation,
                        reduceMotion = reduceMotion,
                        onRetry = ::submit,
                        onOpenSettings = { showModelDialog = true },
                        onOpenDevices = onOpenDevices,
                        canSubmitQuickPrompt = { !taskState.isRunning },
                        onQuickPrompt = ::handleQuickPrompt,
                        onConfirmApprove = { viewModel.respondToConfirmation(true) },
                        onConfirmReject = { viewModel.respondToConfirmation(false) },
                        modifier = Modifier.weight(1f)
                    )

                    AgentComposer(
                        prompt = prompt,
                        onPromptChange = { prompt = it },
                        canSend = canSend,
                        running = taskState.isRunning,
                        readiness = readiness,
                        observationMode = taskState.observationMode,
                        totalTokens = taskState.usage.totalTokens,
                        compactionCount = taskState.compactionCount,
                        reduceMotion = reduceMotion,
                        lastObservedAtMs = taskState.deviceState?.capturedAt,
                        obsSwitchOn = obsSwitchOn,
                        onObsSwitchChange = { obsSwitchOn = it },
                        onSend = { submit(prompt) },
                        onNewTask = {
                            if (taskState.messages.any { it.role != AgentMessageRole.SYSTEM }) {
                                showNewTaskDialog = true
                            } else {
                                viewModel.newTask()
                            }
                        },
                        onCancel = viewModel::cancelTask,
                        onOpenSettings = { showModelDialog = true },
                        onOpenDevices = onOpenDevices,
                        onQuickRecognize = { submit(recognizePrompt) }
                    )
                }

                if (layout == AgentScreenLayout.PERMANENT_DEVICE_PANEL && !devicePanelCollapsed) {
                    DevicePreviewPanel(
                        observationMode = taskState.observationMode,
                        screenshot = taskState.latestScreenshot,
                        pageSignature = taskState.deviceState?.pageSignature?.value,
                        pageChanged = taskState.pageDiff?.changed,
                        totalTokens = taskState.usage.totalTokens,
                        contextWindowTokens = contextWindowTokens,
                        compactionCount = taskState.compactionCount,
                        lastObservedAtMs = taskState.deviceState?.capturedAt,
                        reduceMotion = reduceMotion,
                        obsSwitchOn = obsSwitchOn,
                        onObsSwitchChange = { obsSwitchOn = it },
                        onCollapse = { devicePanelCollapsed = true },
                        modifier = Modifier
                            .width(panelWidth)
                            .fillMaxHeight()
                    )
                }
            }

            if (layout == AgentScreenLayout.OVERLAY_DEVICE_PANEL && !devicePanelCollapsed) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(320.dp)
                        .fillMaxHeight()
                        .shadow(8.dp, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    color = QadbTokens.bg1
                ) {
                    DevicePreviewPanel(
                        observationMode = taskState.observationMode,
                        screenshot = taskState.latestScreenshot,
                        pageSignature = taskState.deviceState?.pageSignature?.value,
                        pageChanged = taskState.pageDiff?.changed,
                        totalTokens = taskState.usage.totalTokens,
                        contextWindowTokens = contextWindowTokens,
                        compactionCount = taskState.compactionCount,
                        lastObservedAtMs = taskState.deviceState?.capturedAt,
                        reduceMotion = reduceMotion,
                        obsSwitchOn = obsSwitchOn,
                        onObsSwitchChange = { obsSwitchOn = it },
                        onCollapse = { devicePanelCollapsed = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (layout != AgentScreenLayout.SINGLE_COLUMN && devicePanelCollapsed) {
                val expandPanelDesc = stringResource(Res.string.agent_expand_panel)
                Surface(
                    onClick = { devicePanelCollapsed = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .semantics { contentDescription = expandPanelDesc },
                    shape = RoundedCornerShape(UiTokens.RadiusSmall),
                    color = QadbTokens.bg1,
                    border = BorderStroke(1.dp, QadbTokens.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = IconParkIcons.ChevronLeft,
                            contentDescription = null,
                            tint = QadbTokens.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = expandPanelDesc,
                            color = QadbTokens.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    if (showModelDialog) {
        AiModelConfigDialog(
            initialConfig = modelConfig,
            hasSavedKey = apiKeyAvailable,
            onSaved = {
                viewModel.refreshConfigurationStatus()
                showModelDialog = false
            },
            onKeyCleared = viewModel::refreshConfigurationStatus,
            onDismiss = { showModelDialog = false }
        )
    }

    if (showNewTaskDialog) {
        AlertDialog(
            onDismissRequest = { showNewTaskDialog = false },
            title = {
                Text(
                    text = l10n("开始新会话？", "Start a new session?"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            },
            text = {
                Text(
                    text = l10n(
                        "当前对话与运行记录将被清空，且无法恢复。",
                        "The current conversation and run history will be cleared. This cannot be undone."
                    ),
                    style = TextStyle(fontSize = 13.sp, lineHeight = 20.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNewTaskDialog = false
                    viewModel.newTask()
                }) {
                    Text(l10n("清空并开始", "Clear and start"), color = QadbTokens.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTaskDialog = false }) {
                    Text(l10n("取消", "Cancel"))
                }
            }
        )
    }
}

private fun devicePanelWidth(widthDp: Float): Dp =
    if (widthDp >= 1280f) 400.dp else 320.dp

// ============ Top device status bar (44dp full width) ============

@Composable
private fun AgentTopStatusBar(
    selectedDevice: String?,
    displayName: String?,
    deviceInfo: DeviceInfoData?,
    centerInfo: DeviceCenterInfoData?,
    connected: Boolean,
    running: Boolean,
    modifier: Modifier = Modifier
) {
    val deviceName = displayName.orEmpty().ifBlank {
        deviceInfo?.deviceModel.orEmpty().ifBlank { selectedDevice ?: stringResource(Res.string.agent_no_device) }
    }
    val androidVersion = deviceInfo?.androidVersion.orEmpty()
    val apiLevel = deviceInfo?.sdkVersion.orEmpty()
    val androidText = if (androidVersion.isBlank()) {
        stringResource(Res.string.agent_topbar_android_unknown)
    } else {
        stringResource(Res.string.agent_topbar_android, androidVersion, apiLevel.ifBlank { "--" })
    }
    val battery = centerInfo?.batteryLevel.orEmpty().ifBlank { "--" }
    val isWifi = selectedDevice?.contains(":") == true ||
        selectedDevice?.contains("_adb-tls") == true ||
        deviceInfo?.ipAddress?.isNotBlank() == true
    val connectionText = stringResource(if (isWifi) Res.string.wireless_connection else Res.string.usb_connection)
    val adbText = stringResource(if (connected) Res.string.agent_connected else Res.string.agent_disconnected)

    FramedStateSurface(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (connected) QadbTokens.success else QadbTokens.warning)
                )
                Text(
                    text = deviceName,
                    color = QadbTokens.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TopBarDivider()
                Text(
                    text = androidText,
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp
                )
                TopBarDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isWifi) IconParkIcons.Wifi else IconParkIcons.Usb,
                        contentDescription = null,
                        tint = QadbTokens.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = connectionText,
                        color = QadbTokens.textSecondary,
                        fontSize = 11.sp
                    )
                }
                TopBarDivider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = IconParkIcons.BatteryFull,
                        contentDescription = null,
                        tint = QadbTokens.textMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "$battery%",
                        fontFamily = FontFamily.Monospace,
                        color = QadbTokens.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                        .background(
                            if (running) QadbTokens.brand.copy(alpha = 0.12f)
                            else if (connected) QadbTokens.success.copy(alpha = 0.12f)
                            else QadbTokens.warning.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (running) QadbTokens.brand
                                    else if (connected) QadbTokens.success
                                    else QadbTokens.warning
                                )
                        )
                        Text(
                            text = if (running) l10n("任务执行中", "Running") else if (connected) l10n("就绪", "Ready") else l10n("未连接", "Disconnected"),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (running) QadbTokens.brand else if (connected) QadbTokens.success else QadbTokens.warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(14.dp)
            .background(QadbTokens.border)
    )
}

// ============ Main conversation area ============

@Composable
private fun AgentConversation(
    messages: List<AgentMessage>,
    readiness: AgentReadiness,
    errorMessage: String?,
    publicActivity: AgentPublicActivityState,
    pendingConfirmation: AgentStep?,
    reduceMotion: Boolean,
    onRetry: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevices: () -> Unit,
    canSubmitQuickPrompt: (String) -> Boolean,
    onQuickPrompt: (String) -> Unit,
    onConfirmApprove: () -> Unit,
    onConfirmReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeRun = publicActivity.activeRun
    val followThresholdPx = with(LocalDensity.current) { 64.dp.roundToPx() }
    val conversationItems = remember(messages, publicActivity, errorMessage, pendingConfirmation) {
        val visibleMessages = messages.filter { it.role != AgentMessageRole.SYSTEM }
        val assistantRunIds = visibleMessages.asSequence()
            .filter { it.role == AgentMessageRole.ASSISTANT }
            .mapNotNull { it.runId }
            .toSet()
        buildList<AgentConversationItem> {
            if (shouldShowAgentGuide(messages)) add(AgentConversationItem.Guide)
            visibleMessages.forEach { message ->
                add(AgentConversationItem.Message(message))
                if (message.role == AgentMessageRole.USER) {
                    message.runId?.let(publicActivity.runs::get)?.let { run ->
                        add(AgentConversationItem.Activity(message, run))
                        if (run.responseText.isNotBlank() && run.runId !in assistantRunIds) {
                            add(AgentConversationItem.Streaming(run.runId, run.responseText))
                        }
                    }
                }
            }
            errorMessage?.takeIf { activeRun?.failure == null }?.let {
                add(AgentConversationItem.Error(it))
            }
            pendingConfirmation?.let { add(AgentConversationItem.Confirmation(it)) }
        }
    }
    val isNearLatest by remember(listState, followThresholdPx) {
        derivedStateOf {
            val layout = listState.layoutInfo
            if (layout.totalItemsCount == 0) {
                true
            } else {
                val lastVisible = layout.visibleItemsInfo.lastOrNull()
                lastVisible != null &&
                    lastVisible.index == layout.totalItemsCount - 1 &&
                    lastVisible.offset + lastVisible.size - layout.viewportEndOffset <= followThresholdPx
            }
        }
    }
    var followsLatest by remember { mutableStateOf(true) }
    var programmaticScroll by remember { mutableStateOf(false) }

    LaunchedEffect(listState, followThresholdPx) {
        snapshotFlow { listState.isScrollInProgress to isNearLatest }
            .collect { (isScrolling, nearLatest) ->
                if (!programmaticScroll && isScrolling) {
                    followsLatest = nearLatest
                } else if (!isScrolling && nearLatest) {
                    followsLatest = true
                }
            }
    }
    LaunchedEffect(conversationItems, followsLatest) {
        if (followsLatest && conversationItems.isNotEmpty()) {
            programmaticScroll = true
            try {
                listState.scrollToItem(
                    conversationItems.lastIndex,
                    agentLatestItemScrollOffset()
                )
            } finally {
                programmaticScroll = false
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(
                items = conversationItems,
                key = AgentConversationItem::stableKey,
                contentType = AgentConversationItem::contentType
            ) { item ->
                when (item) {
                    AgentConversationItem.Guide -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AgentGuideHero(
                            readiness = readiness,
                            canSubmit = canSubmitQuickPrompt,
                            onPrompt = onQuickPrompt,
                            modifier = Modifier.widthIn(max = 680.dp)
                        )
                    }
                    is AgentConversationItem.Message -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AgentMessageBubble(item.message)
                    }
                    is AgentConversationItem.Activity -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AgentRunCard(
                            run = item.run,
                            reduceMotion = reduceMotion,
                            onRetry = { onRetry(item.userMessage.text) },
                            onOpenSettings = onOpenSettings,
                            onOpenDevices = onOpenDevices,
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                        )
                    }
                    is AgentConversationItem.Streaming -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AgentStreamingResponse(item.text)
                    }
                    is AgentConversationItem.Error -> Surface(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .border(1.dp, QadbTokens.danger, RoundedCornerShape(UiTokens.RadiusLarge)),
                        shape = RoundedCornerShape(UiTokens.RadiusLarge),
                        color = QadbTokens.dangerContainer
                    ) {
                        Text(
                            text = item.text,
                            modifier = Modifier.padding(UiTokens.SpaceLarge),
                            color = QadbTokens.dangerText,
                            fontSize = UiTokens.TextBody
                        )
                    }
                    is AgentConversationItem.Confirmation -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AgentInlineConfirmationCard(
                            step = item.step,
                            onApprove = onConfirmApprove,
                            onReject = onConfirmReject,
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (!followsLatest && !isNearLatest) {
            OutlinedButton(
                onClick = { followsLatest = true },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = UiTokens.SpaceSmall)
            ) {
                Text(stringResource(Res.string.agent_back_to_latest))
            }
        }
    }
}

private sealed interface AgentConversationItem {
    val stableKey: String
    val contentType: String

    data object Guide : AgentConversationItem {
        override val stableKey = "agent-guide"
        override val contentType = "guide"
    }

    data class Message(val message: AgentMessage) : AgentConversationItem {
        override val stableKey = "message:${message.id}"
        override val contentType = "message"
    }

    data class Activity(
        val userMessage: AgentMessage,
        val run: AgentRunPresentation
    ) : AgentConversationItem {
        override val stableKey = "activity:${run.runId}"
        override val contentType = "activity"
    }

    data class Streaming(val runId: String, val text: String) : AgentConversationItem {
        override val stableKey = "streaming:$runId"
        override val contentType = "streaming"
    }

    data class Error(val text: String) : AgentConversationItem {
        override val stableKey = "agent-error"
        override val contentType = "error"
    }

    data class Confirmation(val step: AgentStep) : AgentConversationItem {
        override val stableKey = "confirmation:${step.id}"
        override val contentType = "confirmation"
    }
}

// ============ Message bubbles ============

@Composable
private fun AgentMessageBubble(message: AgentMessage) {
    val isUser = message.role == AgentMessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isUser) {
            Surface(
                modifier = Modifier.widthIn(max = 560.dp),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomEnd = 4.dp,
                    bottomStart = 14.dp
                ),
                color = QadbTokens.bg2,
                border = BorderStroke(1.dp, QadbTokens.brand.copy(alpha = 0.45f))
            ) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = QadbTokens.textPrimary,
                        style = TextStyle(
                            fontSize = 13.5.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        } else {
            val avatarDesc = stringResource(Res.string.agent_avatar_desc)
            Row(
                modifier = Modifier.widthIn(max = 720.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(QadbTokens.ai)
                        .semantics { contentDescription = avatarDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = QadbTokens.bg1,
                        border = BorderStroke(1.dp, QadbTokens.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            AgentMarkdownText(message.text)
                        }
                    }
                    AgentCopyChip(text = message.text)
                }
            }
        }
    }
}

@Composable
private fun AgentCopyChip(text: String) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(UiTokens.RadiusSmall))
            .clickable {
                clipboard.setText(AnnotatedString(text))
                copied = true
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .semantics {
                contentDescription = if (copied) l10n("已复制", "Copied") else l10n("复制回复", "Copy reply")
            }
    ) {
        Text(
            text = if (copied) l10n("已复制", "Copied") else l10n("复制", "Copy"),
            fontSize = 11.sp,
            color = if (copied) QadbTokens.success else QadbTokens.textSecondary
        )
    }
}

@Composable
private fun AgentStreamingResponse(text: String) {
    AgentMarkdownText(
        text = text,
        modifier = Modifier.widthIn(max = 680.dp)
    )
}

// ============ Onboarding and quick prompts ============

@Composable
private fun AgentGuideHeroIcon() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        QadbTokens.ai.copy(alpha = 0.18f),
                        QadbTokens.brand.copy(alpha = 0.12f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        QadbTokens.ai.copy(alpha = 0.5f),
                        QadbTokens.brand.copy(alpha = 0.3f)
                    )
                ),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AiAgentBrandIcon(
            contentDescription = null,
            modifier = Modifier.size(38.dp)
        )
    }
}

@Composable
private fun AgentGuideHero(
    readiness: AgentReadiness,
    canSubmit: (String) -> Boolean,
    onPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickScenarios = listOf(
        ScenarioCardSpec(
            title = l10n("系统深度体检", "Deep Diagnostics"),
            desc = l10n("全面检测电池、内存、系统属性与网络", "Inspect battery, RAM, Android OS & network"),
            prompt = l10n("请帮我全面检测当前连接设备的状态，包括电池、内存、系统版本和网络属性", "Please check device status including battery, memory, OS version and network"),
            icon = IconParkIcons.Speed,
            accentColor = QadbTokens.brand
        ),
        ScenarioCardSpec(
            title = l10n("屏幕视觉速识", "Screen Vision"),
            desc = l10n("截屏并识别当前画面的文字与控件节点", "Capture screen & recognize text and UI elements"),
            prompt = stringResource(Res.string.agent_prompt_recognize),
            icon = IconParkIcons.Camera,
            accentColor = QadbTokens.ai
        ),
        ScenarioCardSpec(
            title = l10n("自动导航设置", "Navigate Settings"),
            desc = l10n("打开 Android 设置并定位到目标选项", "Open Android system settings & navigate to options"),
            prompt = stringResource(Res.string.agent_prompt_settings),
            icon = IconParkIcons.Setting,
            accentColor = QadbTokens.success
        ),
        ScenarioCardSpec(
            title = l10n("应用排查诊断", "App Inspection"),
            desc = l10n("列出已安装第三方包并分析运行状态", "List third-party packages & analyze running apps"),
            prompt = l10n("请列出当前设备上安装的第三方应用并分析正在运行的应用", "Please list installed third-party apps and inspect running processes"),
            icon = IconParkIcons.Application,
            accentColor = QadbTokens.warning
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = QadbTokens.bg1,
        border = BorderStroke(1.dp, QadbTokens.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AgentGuideHeroIcon()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = l10n("QADB 智能设备助理", "QADB AI Agent Assistant"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = QadbTokens.textPrimary
                )
                Text(
                    text = l10n(
                        "基于多模态大模型与 Android 视觉/无障碍感知，全自动执行设备操作与系统诊断",
                        "Autonomous device operation & debugging assistant powered by LLM and UI vision/accessibility"
                    ),
                    color = QadbTokens.textSecondary,
                    fontSize = 12.5.sp
                )
            }

            AgentOnboardingSteps(readiness = readiness)

            // 2x2 Grid of Scenario Prompt Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScenarioCardItem(
                        spec = quickScenarios[0],
                        enabled = canSubmit(quickScenarios[0].prompt),
                        onClick = { onPrompt(quickScenarios[0].prompt) },
                        modifier = Modifier.weight(1f)
                    )
                    ScenarioCardItem(
                        spec = quickScenarios[1],
                        enabled = canSubmit(quickScenarios[1].prompt),
                        onClick = { onPrompt(quickScenarios[1].prompt) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScenarioCardItem(
                        spec = quickScenarios[2],
                        enabled = canSubmit(quickScenarios[2].prompt),
                        onClick = { onPrompt(quickScenarios[2].prompt) },
                        modifier = Modifier.weight(1f)
                    )
                    ScenarioCardItem(
                        spec = quickScenarios[3],
                        enabled = canSubmit(quickScenarios[3].prompt),
                        onClick = { onPrompt(quickScenarios[3].prompt) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentOnboardingSteps(readiness: AgentReadiness) {
    val steps = listOf(
        l10n("连接设备", "Connect device"),
        l10n("配置视觉模型", "Configure vision model"),
        l10n("测试连接", "Test connection"),
        l10n("发送第一条指令", "Send first instruction")
    )
    val activeStep = when (readiness) {
        AgentReadiness.DEVICE_REQUIRED -> 0
        AgentReadiness.MODEL_REQUIRED -> 1
        AgentReadiness.CHECKING -> 2
        AgentReadiness.READY -> 3
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, label ->
            val completed = index < activeStep
            val active = index == activeStep
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                color = when {
                    completed -> QadbTokens.success.copy(alpha = 0.10f)
                    active -> QadbTokens.brand.copy(alpha = 0.10f)
                    else -> QadbTokens.bg2
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        completed -> QadbTokens.success.copy(alpha = 0.35f)
                        active -> QadbTokens.brand.copy(alpha = 0.40f)
                        else -> QadbTokens.border
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (completed) "✓" else "${index + 1}",
                        color = if (completed) QadbTokens.success else if (active) QadbTokens.brand else QadbTokens.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = label,
                        color = if (active || completed) QadbTokens.textPrimary else QadbTokens.textSecondary,
                        fontSize = 10.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private data class ScenarioCardSpec(
    val title: String,
    val desc: String,
    val prompt: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
private fun ScenarioCardItem(
    spec: ScenarioCardSpec,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isHovered && enabled) spec.accentColor.copy(alpha = 0.08f) else QadbTokens.bg2,
        border = BorderStroke(
            1.dp,
            if (isHovered && enabled) spec.accentColor.copy(alpha = 0.45f) else QadbTokens.border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(spec.accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = spec.icon,
                    contentDescription = null,
                    tint = if (enabled) spec.accentColor else QadbTokens.textSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = spec.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (enabled) QadbTokens.textPrimary else QadbTokens.textSecondary
                )
                Text(
                    text = spec.desc,
                    fontSize = 11.5.sp,
                    color = QadbTokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = IconParkIcons.Right,
                contentDescription = null,
                tint = if (isHovered && enabled) spec.accentColor else Color.Transparent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ============ Task run card (reasoning timeline, tool calls, and progress) ============

private data class ToolCallPresentation(
    val key: String,
    val tool: AgentPublicToolSummary,
    val startedAtMs: Long,
    val result: AgentPublicToolResult?,
    val stage: AgentPublicStage
)

private data class ThinkingPresentation(
    val key: String,
    val stage: AgentPublicStage,
    val occurredAtMs: Long
)

private data class TimelineStepPresentation(
    val key: String,
    val stage: AgentPublicStage,
    val occurredAtMs: Long,
    val tool: AgentPublicToolSummary?,
    val result: AgentPublicToolResult?
)

private enum class ToolCallStatus { DONE, PENDING, RUNNING, UNVERIFIED, FAILED }

/** Pairs ToolStarted and ToolFinished activity items into tool-call cards, favoring the later result. */
private fun buildToolCallPresentations(activities: List<AgentPublicActivityItem>): List<ToolCallPresentation> {
    val cards = mutableListOf<ToolCallPresentation>()
    activities.forEach { activity ->
        val tool = activity.tool ?: return@forEach
        val last = cards.lastOrNull()
        if (last != null && last.result == null && last.tool == tool) {
            cards[cards.size - 1] = last.copy(result = activity.result, stage = activity.stage)
        } else {
            cards.add(
                ToolCallPresentation("tool-${activity.sequence}", tool, activity.occurredAtMs, activity.result, activity.stage)
            )
        }
    }
    return cards
}

/** Collapses UNDERSTANDING and PLANNING activity into reasoning cards, merging adjacent stages and showing up to three. */
private fun buildThinkingPresentations(activities: List<AgentPublicActivityItem>): List<ThinkingPresentation> {
    val cards = mutableListOf<ThinkingPresentation>()
    activities.forEach { activity ->
        if (activity.tool == null && activity.stage in setOf(AgentPublicStage.UNDERSTANDING, AgentPublicStage.PLANNING)) {
            val last = cards.lastOrNull()
            if (last != null && last.stage == activity.stage) {
                cards[cards.size - 1] = last.copy(occurredAtMs = activity.occurredAtMs)
            } else {
                cards.add(ThinkingPresentation("think-${activity.sequence}", activity.stage, activity.occurredAtMs))
            }
        }
    }
    return cards.takeLast(3)
}

private fun timelineStepsFrom(
    run: AgentRunPresentation,
    toolCards: List<ToolCallPresentation>
): List<TimelineStepPresentation> {
    if (toolCards.isNotEmpty()) {
        return toolCards.map {
            TimelineStepPresentation(it.key, it.stage, it.startedAtMs, it.tool, it.result)
        }
    }
    // Fallback: de-duplicate the stage sequence.
    val steps = mutableListOf<TimelineStepPresentation>()
    run.activities.forEach { activity ->
        if (activity.tool == null) {
            val last = steps.lastOrNull()
            if (last == null || last.stage != activity.stage) {
                steps.add(
                    TimelineStepPresentation("stage-${activity.sequence}", activity.stage, activity.occurredAtMs, null, null)
                )
            }
        }
    }
    return steps.takeLast(8)
}

@Composable
private fun AgentRunCard(
    run: AgentRunPresentation,
    reduceMotion: Boolean,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thinkingCards = remember(run.runId, run.activities) { buildThinkingPresentations(run.activities) }
    val toolCards = remember(run.runId, run.activities) { buildToolCallPresentations(run.activities) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        thinkingCards.forEach { card ->
            AgentThinkingCard(
                stage = card.stage,
                occurredAtMs = card.occurredAtMs
            )
        }
        toolCards.forEach { card ->
            AgentToolCallCard(
                entry = card,
                runStatus = run.status,
                reduceMotion = reduceMotion
            )
        }
        AgentActivityProgressCard(
            run = run,
            toolCards = toolCards
        )
        run.failure?.let { failure ->
            AgentRunFailureCard(
                failure = failure,
                onRetry = onRetry,
                onOpenSettings = onOpenSettings,
                onOpenDevices = onOpenDevices
            )
        }
    }
}

@Composable
private fun AgentThinkingCard(
    stage: AgentPublicStage,
    occurredAtMs: Long,
    modifier: Modifier = Modifier
) {
    var expanded by remember(stage, occurredAtMs) { mutableStateOf(false) }
    val toggleDesc = stringResource(if (expanded) Res.string.agent_collapse else Res.string.agent_expand)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.aiBorder, RoundedCornerShape(UiTokens.RadiusMedium)),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.aiContainer
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconParkIcons.Star,
                    contentDescription = null,
                    tint = QadbTokens.ai,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(Res.string.agent_think_title),
                    color = QadbTokens.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = publicStageLabel(stage),
                    color = QadbTokens.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = IconParkIcons.ArrowDown,
                    contentDescription = toggleDesc,
                    tint = QadbTokens.textSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            if (expanded) {
                Text(
                    text = publicStageLabel(stage),
                    color = QadbTokens.textPrimary,
                    style = TextStyle(fontSize = 13.sp, lineHeight = 21.sp),
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun AgentToolCallCard(
    entry: ToolCallPresentation,
    runStatus: AgentPublicRunStatus,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember(entry.key) { mutableStateOf(false) }
    val status = when {
        entry.result == null && runStatus == AgentPublicRunStatus.WAITING_CONFIRMATION -> ToolCallStatus.PENDING
        entry.result == null -> ToolCallStatus.RUNNING
        entry.result == AgentPublicToolResult.SUCCEEDED ||
            entry.result == AgentPublicToolResult.RECOVERED -> ToolCallStatus.DONE
        entry.result == AgentPublicToolResult.UNVERIFIED -> ToolCallStatus.UNVERIFIED
        else -> ToolCallStatus.FAILED
    }
    val statusLabel = when (status) {
        ToolCallStatus.DONE -> stringResource(Res.string.agent_tool_done)
        ToolCallStatus.PENDING -> stringResource(Res.string.agent_step_confirm)
        ToolCallStatus.RUNNING -> stringResource(Res.string.agent_step_running)
        ToolCallStatus.UNVERIFIED -> stringResource(Res.string.agent_step_unverified)
        ToolCallStatus.FAILED -> stringResource(Res.string.agent_step_failed)
    }
    val statusColor = when (status) {
        ToolCallStatus.DONE -> QadbTokens.success
        ToolCallStatus.PENDING -> QadbTokens.warning
        ToolCallStatus.RUNNING -> QadbTokens.brand
        ToolCallStatus.UNVERIFIED -> QadbTokens.warning
        ToolCallStatus.FAILED -> QadbTokens.danger
    }
    val toggleDesc = stringResource(if (expanded) Res.string.agent_collapse else Res.string.agent_expand)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.aiBorder, RoundedCornerShape(UiTokens.RadiusMedium)),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.aiContainer
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconParkIcons.Terminal,
                    contentDescription = null,
                    tint = QadbTokens.ai,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = toolCommandName(entry.tool.kind),
                    color = QadbTokens.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = toolArgsSummary(entry.tool),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                AgentStatusDot(
                    color = statusColor,
                    breathing = status == ToolCallStatus.RUNNING,
                    reduceMotion = reduceMotion
                )
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatClockTime(entry.startedAtMs),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = IconParkIcons.ArrowDown,
                    contentDescription = toggleDesc,
                    tint = QadbTokens.textSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.agent_cmd_prefix, toolCommandWithArgs(entry.tool)),
                        color = QadbTokens.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(fontSize = 12.sp, lineHeight = 19.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(QadbTokens.bg3, RoundedCornerShape(UiTokens.RadiusSmall))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    entry.result?.let { result ->
                        Text(
                            text = stringResource(Res.string.agent_cmd_result, publicToolResultLabel(result)),
                            color = QadbTokens.textSecondary,
                            fontFamily = FontFamily.Monospace,
                            style = TextStyle(fontSize = 11.5.sp, lineHeight = 17.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentActivityProgressCard(
    run: AgentRunPresentation,
    toolCards: List<ToolCallPresentation>,
    modifier: Modifier = Modifier
) {
    val statusBadge = runStatusBadge(run.status)
    val steps = remember(run.runId, run.activities, toolCards) { timelineStepsFrom(run, toolCards) }
    val doneCount = steps.count {
        it.result == AgentPublicToolResult.SUCCEEDED || it.result == AgentPublicToolResult.RECOVERED
    }
    val metrics = run.metrics
    val stageLiveRegion = if (
        run.status == AgentPublicRunStatus.FAILED ||
        run.status == AgentPublicRunStatus.WAITING_CONFIRMATION
    ) {
        LiveRegionMode.Assertive
    } else {
        LiveRegionMode.Polite
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.border, RoundedCornerShape(UiTokens.RadiusMedium))
            .semantics { liveRegion = stageLiveRegion },
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.bg1
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.agent_activity_progress),
                    color = QadbTokens.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                AgentBadge(statusBadge)
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (metrics.deviceActionLimit > 0) {
                        l10n(
                            "步骤 %1\$d/%2\$d · 完成 %3\$d",
                            "Steps %1\$d/%2\$d · %3\$d done"
                        ).format(metrics.deviceActions, metrics.deviceActionLimit, doneCount)
                    } else {
                        stringResource(Res.string.agent_activity_steps_done, steps.size, doneCount)
                    },
                    color = QadbTokens.textSecondary,
                    fontSize = UiTokens.TextCaption
                )
            }
            if (steps.isEmpty()) {
                Text(
                    text = stringResource(Res.string.agent_stage_understanding),
                    color = QadbTokens.textSecondary,
                    fontSize = UiTokens.TextCaption,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            } else {
                steps.forEachIndexed { index, step ->
                    AgentTimelineStepRow(
                        step = step,
                        isLast = index == steps.lastIndex,
                        runStatus = run.status
                    )
                }
            }
            if (metrics.modelCalls > 0 || metrics.totalTokens > 0) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(QadbTokens.divider))
                Text(
                    text = metricsCallsLine(metrics),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = UiTokens.TextCaption,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(
                        Res.string.agent_activity_budget_tokens,
                        metrics.promptTokens,
                        metrics.completionTokens,
                        metrics.cachedTokens,
                        metrics.totalTokens
                    ),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = UiTokens.TextCaption
                )
            }
        }
    }
}

@Composable
private fun metricsCallsLine(metrics: AgentPublicMetrics): String = if (metrics.modelCallLimit > 0) {
    stringResource(
        Res.string.agent_activity_budget_calls,
        metrics.modelCalls,
        metrics.modelCallLimit,
        metrics.visionCalls,
        metrics.visionCallLimit,
        metrics.replans,
        metrics.replanLimit
    )
} else {
    stringResource(
        Res.string.agent_activity_usage_calls,
        metrics.modelCalls,
        metrics.visionCalls,
        metrics.replans
    )
}

@Composable
private fun AgentTimelineStepRow(
    step: TimelineStepPresentation,
    isLast: Boolean,
    runStatus: AgentPublicRunStatus,
    modifier: Modifier = Modifier
) {
    val stepColor = when {
        step.result == AgentPublicToolResult.SUCCEEDED ||
            step.result == AgentPublicToolResult.RECOVERED -> QadbTokens.success
        step.result == AgentPublicToolResult.FAILED ||
            step.result == AgentPublicToolResult.DENIED -> QadbTokens.danger
        step.result == AgentPublicToolResult.UNVERIFIED -> QadbTokens.warning
        step.result == null && isLast &&
            runStatus == AgentPublicRunStatus.WAITING_CONFIRMATION -> QadbTokens.warning
        step.result == null && isLast &&
            (runStatus == AgentPublicRunStatus.RUNNING || runStatus == AgentPublicRunStatus.CANCELLING) ->
            QadbTokens.brand
        else -> QadbTokens.textMuted
    }
    val stepText = step.tool?.let { publicToolLabel(it) } ?: publicStageLabel(step.stage)
    val stepSub = when {
        step.result == null && isLast &&
            runStatus == AgentPublicRunStatus.WAITING_CONFIRMATION ->
            stringResource(Res.string.agent_step_confirm)
        step.tool != null -> formatClockTime(step.occurredAtMs) + " · " + toolCommandName(step.tool.kind)
        else -> formatClockTime(step.occurredAtMs) + " · " + publicStageLabel(step.stage)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.width(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(stepColor)
            )
            if (!isLast) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(QadbTokens.divider)
                )
            }
        }
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = stepText,
                color = QadbTokens.textPrimary,
                fontSize = 13.sp
            )
            Text(
                text = stepSub,
                color = QadbTokens.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun AgentRunFailureCard(
    failure: AgentFailure,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionLabel = when (failure.suggestedAction) {
        AgentFailureAction.RETRY -> stringResource(Res.string.file_browser_retry)
        AgentFailureAction.OPEN_MODEL_SETTINGS -> stringResource(Res.string.agent_open_settings)
        AgentFailureAction.OPEN_DEVICES -> stringResource(Res.string.agent_open_devices)
        AgentFailureAction.NONE -> null
    }
    val failureDetailText = buildString {
        append(publicFailureLabel(failure))
        appendLine()
        appendLine(
            l10n(
                "阶段 ${publicFailureStageLabel(failure)} · 子系统 ${publicFailureSubsystemLabel(failure)} · 代码 ${failure.code.name.lowercase()}",
                "Stage ${publicFailureStageLabel(failure)} · subsystem ${publicFailureSubsystemLabel(failure)} · code ${failure.code.name.lowercase()}"
            )
        )
        failure.technicalDetail?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.danger, RoundedCornerShape(UiTokens.RadiusMedium)),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.dangerContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconParkIcons.ShieldAlert,
                    contentDescription = null,
                    tint = QadbTokens.danger,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = publicFailureLabel(failure),
                    color = QadbTokens.dangerText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = l10n(
                    "阶段 ${publicFailureStageLabel(failure)} · 子系统 ${publicFailureSubsystemLabel(failure)} · 代码 ${failure.code.name.lowercase()}",
                    "Stage ${publicFailureStageLabel(failure)} · subsystem ${publicFailureSubsystemLabel(failure)} · code ${failure.code.name.lowercase()}"
                ),
                color = QadbTokens.dangerText.copy(alpha = 0.85f),
                fontSize = UiTokens.TextCaption
            )
            if (actionLabel != null || failure.technicalDetail != null) {
                val clipboard = LocalClipboardManager.current
                var copied by remember { mutableStateOf(false) }
                LaunchedEffect(copied) {
                    if (copied) {
                        delay(1600)
                        copied = false
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (actionLabel != null) {
                        OutlinedButton(
                            onClick = when (failure.suggestedAction) {
                                AgentFailureAction.RETRY -> onRetry
                                AgentFailureAction.OPEN_MODEL_SETTINGS -> onOpenSettings
                                AgentFailureAction.OPEN_DEVICES -> onOpenDevices
                                AgentFailureAction.NONE -> ({})
                            }
                        ) {
                            Text(actionLabel)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(failureDetailText))
                            copied = true
                        }
                    ) {
                        Text(if (copied) l10n("已复制", "Copied") else l10n("复制详情", "Copy details"))
                    }
                }
            }
        }
    }
}

// ============ Inline confirmation card for sensitive actions (replaces AlertDialog) ============

@Composable
private fun AgentInlineConfirmationCard(
    step: AgentStep,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionName = actionDisplayName(step.action)
    val description = if (step.confirmationReason.isNotBlank()) {
        stringResource(Res.string.agent_sensitive_reason, actionName, step.confirmationReason)
    } else {
        stringResource(Res.string.agent_sensitive_desc, actionName)
    }
    val confirmLabel = if (
        step.confirmationReason.contains("发送", ignoreCase = true) ||
        step.confirmationReason.contains("send", ignoreCase = true)
    ) {
        stringResource(Res.string.agent_confirm_send)
    } else {
        stringResource(Res.string.agent_confirm_execute)
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.danger, RoundedCornerShape(UiTokens.RadiusMedium)),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.dangerContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconParkIcons.ShieldAlert,
                    contentDescription = null,
                    tint = QadbTokens.danger,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(Res.string.agent_confirm_title, actionName),
                    color = QadbTokens.dangerText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                AgentBadge(BadgeSpec(stringResource(Res.string.agent_badge_waiting), QadbTokens.warningContainer, QadbTokens.warningText))
            }
            Text(
                text = description,
                color = QadbTokens.dangerText,
                style = MaterialTheme.typography.body1.copy(fontSize = 13.sp, lineHeight = 20.sp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AgentActionButton(
                    label = confirmLabel,
                    containerColor = QadbTokens.danger,
                    contentColor = Color.White,
                    onClick = onApprove
                )
                AgentActionButton(
                    label = stringResource(Res.string.cancel),
                    containerColor = QadbTokens.bg1,
                    contentColor = QadbTokens.textPrimary,
                    borderColor = QadbTokens.border,
                    onClick = onReject
                )
            }
        }
    }
}

// ============ Bottom input area ============

@Composable
private fun AgentComposer(
    prompt: String,
    onPromptChange: (String) -> Unit,
    canSend: Boolean,
    running: Boolean,
    readiness: AgentReadiness,
    observationMode: AgentObservationMode,
    totalTokens: Int,
    compactionCount: Int,
    reduceMotion: Boolean,
    lastObservedAtMs: Long?,
    obsSwitchOn: Boolean,
    onObsSwitchChange: (Boolean) -> Unit,
    onSend: () -> Unit,
    onNewTask: () -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevices: () -> Unit,
    onQuickRecognize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(QadbTokens.bg1)
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Modern AI Agent Floating Composer Container
        Surface(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        if (focused) 1.5.dp else 1.dp,
                        if (focused) QadbTokens.brand else QadbTokens.border
                    ),
                    RoundedCornerShape(16.dp)
                )
                .shadow(
                    elevation = if (focused) 8.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = if (focused) QadbTokens.brand.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(16.dp),
            color = QadbTokens.bg1
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Text Field Area (stays editable while a task runs so the next
                //    message can be drafted; sending is gated by canSend instead)
                BasicTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    textStyle = TextStyle(
                        color = QadbTokens.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(QadbTokens.brand),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 160.dp)
                        .onFocusChanged { focused = it.isFocused }
                        .onPreviewKeyEvent { event ->
                            when {
                                event.type != KeyEventType.KeyDown -> false
                                event.key == Key.Enter && !event.isShiftPressed -> {
                                    if (canSend) onSend()
                                    true
                                }
                                event.key == Key.Escape && running -> {
                                    onCancel()
                                    true
                                }
                                else -> false
                            }
                        },
                    maxLines = 8,
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (prompt.isEmpty()) {
                                Text(
                                    text = l10n(
                                        "向智能体下达指令，例如：检查设备状态、截屏识别当前界面控件、自动打开系统设置...",
                                        "Instruct AI Agent, e.g.: Check device health, inspect screen UI elements, open system settings..."
                                    ),
                                    color = QadbTokens.textSecondary,
                                    fontSize = 13.5.sp,
                                    style = TextStyle(lineHeight = 20.sp)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Divider line inside composer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(QadbTokens.divider)
                )

                // 2. Integrated Bottom Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Quick Action Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Screen Vision Pill Button
                        ComposerPillButton(
                            icon = IconParkIcons.Camera,
                            text = l10n("屏幕速识", "Quick Vision"),
                            onClick = onQuickRecognize,
                            contentColor = QadbTokens.ai
                        )

                        // Model Settings Pill Button
                        ComposerPillButton(
                            icon = IconParkIcons.Setting,
                            text = if (readiness == AgentReadiness.READY) l10n("模型已就绪", "Model Ready") else l10n("配置模型", "Configure Model"),
                            onClick = onOpenSettings,
                            contentColor = if (readiness == AgentReadiness.READY) QadbTokens.success else QadbTokens.warning
                        )

                        // Observation Mode Switch Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                                .clickable { onObsSwitchChange(!obsSwitchOn) },
                            shape = RoundedCornerShape(UiTokens.BadgeRadius),
                            color = if (obsSwitchOn) QadbTokens.brand.copy(alpha = 0.12f) else QadbTokens.bg2,
                            border = BorderStroke(1.dp, if (obsSwitchOn) QadbTokens.brand.copy(alpha = 0.35f) else QadbTokens.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (obsSwitchOn) QadbTokens.brand else QadbTokens.textSecondary)
                                )
                                Text(
                                    text = if (obsSwitchOn) l10n("视觉感知: 开启", "Vision: ON") else l10n("视觉感知: 暂停", "Vision: OFF"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (obsSwitchOn) QadbTokens.brand else QadbTokens.textSecondary
                                )
                            }
                        }
                    }

                    // Right Actions (New Task + Send/Cancel Button)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (totalTokens > 0) {
                            Text(
                                text = "${totalTokens} tokens",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = QadbTokens.textSecondary
                            )
                        }

                        // New Session Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                                .clickable(enabled = !running, onClick = onNewTask),
                            shape = RoundedCornerShape(UiTokens.RadiusSmall),
                            color = QadbTokens.bg2,
                            border = BorderStroke(1.dp, QadbTokens.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = IconParkIcons.Refresh,
                                    contentDescription = null,
                                    tint = QadbTokens.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = l10n("新会话", "New"),
                                    fontSize = 12.sp,
                                    color = QadbTokens.textPrimary
                                )
                            }
                        }

                        // Prominent Send / Stop Action Button
                        if (running) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                                    .clickable(onClick = onCancel),
                                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                                color = QadbTokens.danger
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconParkIcons.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = l10n("终止任务", "Stop"),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            val primaryActionEnabled = when (readiness) {
                                AgentReadiness.CHECKING -> false
                                AgentReadiness.READY -> canSend
                                else -> true
                            }
                            val primaryAction = when (readiness) {
                                AgentReadiness.DEVICE_REQUIRED -> onOpenDevices
                                AgentReadiness.MODEL_REQUIRED -> onOpenSettings
                                AgentReadiness.READY -> onSend
                                AgentReadiness.CHECKING -> ({})
                            }
                            val primaryLabel = when (readiness) {
                                AgentReadiness.CHECKING -> l10n("正在检查", "Checking")
                                AgentReadiness.DEVICE_REQUIRED -> l10n("连接设备", "Connect device")
                                AgentReadiness.MODEL_REQUIRED -> l10n("配置模型", "Configure model")
                                AgentReadiness.READY -> l10n("发送指令", "Send")
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                                    .clickable(enabled = primaryActionEnabled, onClick = primaryAction),
                                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                                color = if (primaryActionEnabled) QadbTokens.brand else QadbTokens.bg3,
                                border = if (primaryActionEnabled) null else BorderStroke(1.dp, QadbTokens.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconParkIcons.Send,
                                        contentDescription = null,
                                        tint = if (primaryActionEnabled) Color.White else QadbTokens.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = primaryLabel,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (primaryActionEnabled) Color.White else QadbTokens.textSecondary
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

@Composable
private fun ComposerPillButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    contentColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(UiTokens.BadgeRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(UiTokens.BadgeRadius),
        color = if (isHovered) contentColor.copy(alpha = 0.12f) else QadbTokens.bg2,
        border = BorderStroke(
            1.dp,
            if (isHovered) contentColor.copy(alpha = 0.4f) else QadbTokens.border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = QadbTokens.textPrimary
            )
        }
    }
}

// ============ Right-side device panel ============

@Composable
private fun DevicePreviewPanel(
    observationMode: AgentObservationMode,
    screenshot: ByteArray?,
    pageSignature: String?,
    pageChanged: Boolean?,
    totalTokens: Int,
    contextWindowTokens: Int,
    compactionCount: Int,
    lastObservedAtMs: Long?,
    reduceMotion: Boolean,
    obsSwitchOn: Boolean,
    onObsSwitchChange: (Boolean) -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceImage = remember(screenshot) {
        screenshot?.let { bytes -> runCatching { SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap() }.getOrNull() }
    }
    val collapseDesc = stringResource(Res.string.agent_collapse_panel)
    Row(modifier = modifier) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(QadbTokens.divider))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(QadbTokens.bg1)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.agent_preview_title),
                color = QadbTokens.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCollapse)
                    .semantics { contentDescription = collapseDesc },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconParkIcons.ChevronUp,
                    contentDescription = null,
                    tint = QadbTokens.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QadbTokens.bg1, RoundedCornerShape(UiTokens.RadiusLarge))
                    .border(1.dp, QadbTokens.border, RoundedCornerShape(UiTokens.RadiusLarge))
                    .padding(12.dp)
            ) {
                if (deviceImage != null) {
                    Image(
                        bitmap = deviceImage,
                        contentDescription = stringResource(Res.string.agent_preview_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.agent_device_placeholder),
                        contentDescription = stringResource(Res.string.agent_preview_unavailable),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .background(Color(0xD10F1219), RoundedCornerShape(UiTokens.RadiusSmall))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(max = 210.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.agent_page_id, pageSignature?.take(28) ?: "—"),
                        color = Color(0xFFE8EAED),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (pageChanged != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp),
                        shape = RoundedCornerShape(UiTokens.BadgeRadius),
                        color = if (pageChanged) QadbTokens.infoContainer else QadbTokens.bg2
                    ) {
                        Text(
                            text = stringResource(if (pageChanged) Res.string.agent_changed else Res.string.agent_stable),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = if (pageChanged) QadbTokens.infoText else QadbTokens.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AgentObservationCard(
                obsSwitchOn = obsSwitchOn,
                onObsSwitchChange = onObsSwitchChange,
                lastObservedAtMs = lastObservedAtMs,
                pageSignature = pageSignature,
                pageChanged = pageChanged,
                reduceMotion = reduceMotion
            )
            AgentTokenCard(
                totalTokens = totalTokens,
                contextWindowTokens = contextWindowTokens,
                compactionCount = compactionCount
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        ) {
            Surface(
                onClick = onCollapse,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                color = QadbTokens.bg1,
                border = BorderStroke(1.dp, QadbTokens.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.agent_collapse_panel),
                        color = QadbTokens.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun AgentObservationCard(
    obsSwitchOn: Boolean,
    onObsSwitchChange: (Boolean) -> Unit,
    lastObservedAtMs: Long?,
    pageSignature: String?,
    pageChanged: Boolean?,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val switchDesc = stringResource(Res.string.agent_obs_switch_desc)
    val freq = stringResource(Res.string.agent_obs_freq, "2s")
    val caption = if (lastObservedAtMs != null) {
        stringResource(
            Res.string.agent_obs_recent,
            formatClockTime(lastObservedAtMs),
            relativeAgoLabel(lastObservedAtMs)
        ) + " · " + freq
    } else {
        stringResource(Res.string.agent_obs_observing) + " · " + freq
    }
    val pageState = stringResource(if (pageChanged == true) Res.string.agent_changed else Res.string.agent_stable)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (obsSwitchOn) QadbTokens.brand else QadbTokens.border,
                RoundedCornerShape(UiTokens.RadiusMedium)
            ),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.bg1
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.agent_obs_mode_title),
                        color = QadbTokens.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    AgentBadge(BadgeSpec("BETA", QadbTokens.aiContainer, QadbTokens.aiText))
                }
                Box(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = switchDesc
                    }
                ) {
                    Switch(checked = obsSwitchOn, onCheckedChange = onObsSwitchChange)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AgentStatusDot(
                        color = if (obsSwitchOn) QadbTokens.brand else QadbTokens.textMuted,
                        breathing = obsSwitchOn,
                        reduceMotion = reduceMotion
                    )
                    Text(
                        text = stringResource(if (obsSwitchOn) Res.string.agent_obs_watching else Res.string.agent_obs_paused),
                        color = QadbTokens.textSecondary,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = stringResource(Res.string.agent_obs_observing),
                    color = QadbTokens.textSecondary,
                    fontSize = 11.5.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = caption,
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(fontSize = 11.sp, lineHeight = 17.sp)
                )
                Text(
                    text = stringResource(Res.string.agent_page_id, pageSignature?.take(24) ?: "—") + " · " + pageState,
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AgentTokenCard(
    totalTokens: Int,
    contextWindowTokens: Int,
    compactionCount: Int,
    modifier: Modifier = Modifier
) {
    val window = contextWindowTokens.coerceAtLeast(1)
    val ratio = (totalTokens.toFloat() / window).coerceIn(0f, 1f)
    val meterColor = when {
        ratio >= 0.9f -> QadbTokens.danger
        ratio >= 0.75f -> QadbTokens.warning
        else -> QadbTokens.brand
    }
    val remaining = (window - totalTokens).coerceAtLeast(0)
    val meterDesc = stringResource(
        Res.string.agent_token_meter_desc,
        formatNumber(totalTokens),
        formatNumber(window),
        formatNumber(remaining)
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, QadbTokens.border, RoundedCornerShape(UiTokens.RadiusMedium)),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = QadbTokens.bg1
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.agent_token_title),
                    color = QadbTokens.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (compactionCount > 0) {
                    AgentBadge(
                        BadgeSpec(
                            stringResource(Res.string.agent_token_compacted, compactionCount),
                            QadbTokens.warningContainer,
                            QadbTokens.warningText
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                    .background(QadbTokens.bg3)
                    .semantics { contentDescription = meterDesc }
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(ratio)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                        .background(meterColor)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.agent_token_usage, formatNumber(totalTokens), formatNumber(window)),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp
                )
                Text(
                    text = stringResource(Res.string.agent_token_remaining, formatNumber(remaining)),
                    color = QadbTokens.textSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

// ============ Shared widgets ============

private data class BadgeSpec(val text: String, val container: Color, val textColor: Color)

@Composable
private fun AgentBadge(spec: BadgeSpec, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(UiTokens.BadgeRadius),
        color = spec.container
    ) {
        Text(
            text = spec.text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = spec.textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AgentStatusDot(
    color: Color,
    breathing: Boolean = false,
    reduceMotion: Boolean = true,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    val alpha = if (breathing && !reduceMotion) {
        val transition = rememberInfiniteTransition()
        val animatedAlpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 1200), RepeatMode.Reverse)
        )
        animatedAlpha
    } else {
        1f
    }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun MiniSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(36.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(UiTokens.BadgeRadius))
            .background(if (checked) QadbTokens.brand else QadbTokens.bg3)
            .then(
                if (checked) {
                    Modifier
                } else {
                    Modifier.border(BorderStroke(1.dp, QadbTokens.border), RoundedCornerShape(UiTokens.BadgeRadius))
                }
            )
            .clickable { onCheckedChange(!checked) }
            .semantics { contentDescription = description },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            Modifier
                .padding(2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun AgentActionButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(UiTokens.RadiusSmall),
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============ Copy mapping and formatting ============

@Composable
private fun runStatusBadge(status: AgentPublicRunStatus): BadgeSpec = when (status) {
    AgentPublicRunStatus.COMPLETED -> BadgeSpec(
        stringResource(Res.string.agent_badge_completed),
        QadbTokens.successContainer,
        QadbTokens.successText
    )
    AgentPublicRunStatus.FAILED -> BadgeSpec(
        stringResource(Res.string.agent_badge_failed),
        QadbTokens.dangerContainer,
        QadbTokens.dangerText
    )
    AgentPublicRunStatus.CANCELLED -> BadgeSpec(
        stringResource(Res.string.agent_badge_cancelled),
        QadbTokens.bg3,
        QadbTokens.textTertiary
    )
    AgentPublicRunStatus.WAITING_CONFIRMATION -> BadgeSpec(
        stringResource(Res.string.agent_badge_waiting),
        QadbTokens.warningContainer,
        QadbTokens.warningText
    )
    AgentPublicRunStatus.RUNNING,
    AgentPublicRunStatus.CANCELLING -> BadgeSpec(
        stringResource(Res.string.agent_badge_in_progress),
        QadbTokens.infoContainer,
        QadbTokens.infoText
    )
}

/** Monospaced, language-neutral tool command name mapped directly from the tool kind. */
private fun toolCommandName(kind: AgentPublicToolKind): String = when (kind) {
    AgentPublicToolKind.SEMANTIC_GOAL -> "semantic"
    AgentPublicToolKind.OBSERVE_DEVICE -> "observe"
    AgentPublicToolKind.FIND_APP -> "pm list packages"
    AgentPublicToolKind.OPEN_APP -> "am start"
    AgentPublicToolKind.TAP -> "input tap"
    AgentPublicToolKind.SWIPE -> "input swipe"
    AgentPublicToolKind.INPUT_TEXT -> "input text"
    AgentPublicToolKind.KEY_EVENT -> "input keyevent"
    AgentPublicToolKind.WAIT -> "sleep"
    AgentPublicToolKind.FINISH -> "finish"
    AgentPublicToolKind.FORCE_STOP_APP -> "am force-stop"
    AgentPublicToolKind.CLEAR_APP_DATA -> "pm clear"
    AgentPublicToolKind.UNINSTALL_APP -> "pm uninstall"
    AgentPublicToolKind.REBOOT_DEVICE -> "reboot"
}

/**
 * Displays the command shape with omitted arguments. The public activity model deliberately excludes
 * private details such as coordinates, text, and package names, so the expanded code block never invents them.
 */
private fun toolCommandWithArgs(tool: AgentPublicToolSummary): String = when (tool.kind) {
    AgentPublicToolKind.SEMANTIC_GOAL -> "semantic …"
    AgentPublicToolKind.OBSERVE_DEVICE -> "observe"
    AgentPublicToolKind.FIND_APP -> "pm list packages …"
    AgentPublicToolKind.OPEN_APP -> "am start …"
    AgentPublicToolKind.TAP -> "input tap …"
    AgentPublicToolKind.SWIPE -> "input swipe …"
    AgentPublicToolKind.INPUT_TEXT -> "input text \"…\""
    AgentPublicToolKind.KEY_EVENT -> "input keyevent …"
    AgentPublicToolKind.WAIT -> "sleep …"
    AgentPublicToolKind.FINISH -> "finish"
    AgentPublicToolKind.FORCE_STOP_APP -> "am force-stop …"
    AgentPublicToolKind.CLEAR_APP_DATA -> "pm clear …"
    AgentPublicToolKind.UNINSTALL_APP -> "pm uninstall …"
    AgentPublicToolKind.REBOOT_DEVICE -> "reboot"
}

@Composable
private fun toolArgsSummary(tool: AgentPublicToolSummary): String = when (tool.kind) {
    AgentPublicToolKind.INPUT_TEXT -> stringResource(Res.string.agent_public_tool_input, tool.inputCharacterCount ?: 0)
    else -> publicToolLabel(tool)
}

private val CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

private fun formatClockTime(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(CLOCK_FORMAT)

private fun formatNumber(value: Int): String = "%,d".format(value)

@Composable
private fun relativeAgoLabel(epochMs: Long): String {
    val seconds = ((System.currentTimeMillis() - epochMs) / 1000).coerceAtLeast(0)
    val compact = when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        else -> "${seconds / 3600}h"
    }
    return stringResource(Res.string.agent_obs_ago, compact)
}

@Composable
private fun publicStageLabel(stage: AgentPublicStage): String = stringResource(
    when (stage) {
        AgentPublicStage.UNDERSTANDING -> Res.string.agent_stage_understanding
        AgentPublicStage.READING -> Res.string.agent_stage_reading
        AgentPublicStage.PLANNING -> Res.string.agent_stage_planning
        AgentPublicStage.EXECUTING -> Res.string.agent_stage_executing
        AgentPublicStage.VERIFYING -> Res.string.agent_stage_verifying
        AgentPublicStage.RECOVERING -> Res.string.agent_stage_recovering
        AgentPublicStage.WAITING_CONFIRMATION -> Res.string.agent_stage_waiting_confirmation
        AgentPublicStage.RESPONDING -> Res.string.agent_stage_responding
        AgentPublicStage.CANCELLING -> Res.string.agent_stage_cancelling
        AgentPublicStage.COMPLETED -> Res.string.agent_stage_completed
        AgentPublicStage.FAILED -> Res.string.agent_stage_failed
        AgentPublicStage.CANCELLED -> Res.string.agent_stage_cancelled
    }
)

@Composable
private fun publicToolLabel(tool: AgentPublicToolSummary): String = when (tool.kind) {
    AgentPublicToolKind.SEMANTIC_GOAL -> l10n("执行语义设备任务", "Execute semantic device task")
    AgentPublicToolKind.OBSERVE_DEVICE -> stringResource(Res.string.agent_public_tool_observe)
    AgentPublicToolKind.FIND_APP -> stringResource(Res.string.agent_public_tool_find_app)
    AgentPublicToolKind.OPEN_APP -> stringResource(Res.string.agent_public_tool_open_app)
    AgentPublicToolKind.TAP -> stringResource(Res.string.agent_public_tool_tap)
    AgentPublicToolKind.SWIPE -> stringResource(Res.string.agent_public_tool_swipe)
    AgentPublicToolKind.INPUT_TEXT -> stringResource(
        Res.string.agent_public_tool_input,
        tool.inputCharacterCount ?: 0
    )
    AgentPublicToolKind.KEY_EVENT -> stringResource(Res.string.agent_public_tool_key)
    AgentPublicToolKind.WAIT -> stringResource(Res.string.agent_public_tool_wait)
    AgentPublicToolKind.FINISH -> stringResource(Res.string.agent_public_tool_finish)
    AgentPublicToolKind.FORCE_STOP_APP -> stringResource(Res.string.agent_public_tool_force_stop)
    AgentPublicToolKind.CLEAR_APP_DATA -> stringResource(Res.string.agent_public_tool_clear_data)
    AgentPublicToolKind.UNINSTALL_APP -> stringResource(Res.string.agent_public_tool_uninstall)
    AgentPublicToolKind.REBOOT_DEVICE -> stringResource(Res.string.agent_public_tool_reboot)
}

@Composable
private fun publicToolResultLabel(result: AgentPublicToolResult): String = stringResource(
    when (result) {
        AgentPublicToolResult.SUCCEEDED -> Res.string.agent_public_result_succeeded
        AgentPublicToolResult.RECOVERED -> Res.string.agent_public_result_recovered
        AgentPublicToolResult.UNVERIFIED -> Res.string.agent_public_result_unverified
        AgentPublicToolResult.FAILED -> Res.string.agent_public_result_failed
        AgentPublicToolResult.DENIED -> Res.string.agent_public_result_denied
    }
)

@Composable
private fun publicFailureLabel(failure: AgentFailure): String = stringResource(
    if (failure.code == AgentFailureCode.OUTCOME_UNCERTAIN) {
        Res.string.agent_failure_outcome_uncertain
    } else if (failure.code == AgentFailureCode.MODEL_CALL_TIMED_OUT) {
        Res.string.agent_failure_model_timeout
    } else when (failure.category) {
        AgentFailureCategory.PROTOCOL -> Res.string.agent_failure_protocol
        AgentFailureCategory.NETWORK -> Res.string.agent_failure_network
        AgentFailureCategory.RATE_LIMIT -> Res.string.agent_failure_rate_limit
        AgentFailureCategory.AUTHENTICATION -> Res.string.agent_failure_authentication
        AgentFailureCategory.DEVICE_DISCONNECTED -> Res.string.agent_failure_device
        AgentFailureCategory.DEVICE_EXECUTION -> Res.string.agent_failure_device_execution
        AgentFailureCategory.OBSERVATION -> Res.string.agent_failure_observation
        AgentFailureCategory.VERIFICATION -> Res.string.agent_failure_verification
        AgentFailureCategory.SAFETY_BLOCKED -> Res.string.agent_failure_safety
        AgentFailureCategory.BUDGET_EXHAUSTED -> Res.string.agent_failure_budget
        AgentFailureCategory.UNKNOWN -> Res.string.agent_failure_unknown
    }
)

private fun publicFailureStageLabel(failure: AgentFailure): String = when (failure.stage) {
    AgentFailureStage.CONFIGURATION -> l10n("配置", "configuration")
    AgentFailureStage.UNDERSTANDING -> l10n("理解", "understanding")
    AgentFailureStage.OBSERVING -> l10n("观察", "observing")
    AgentFailureStage.PLANNING -> l10n("规划", "planning")
    AgentFailureStage.EXECUTING -> l10n("执行", "executing")
    AgentFailureStage.VERIFYING -> l10n("验证", "verifying")
    AgentFailureStage.RECOVERING -> l10n("恢复", "recovering")
    AgentFailureStage.UNKNOWN -> l10n("未知", "unknown")
}

private fun publicFailureSubsystemLabel(failure: AgentFailure): String = when (failure.subsystem) {
    AgentFailureSubsystem.PROVIDER -> "Provider"
    AgentFailureSubsystem.DEVICE_TRANSPORT -> l10n("设备连接", "device transport")
    AgentFailureSubsystem.OBSERVATION -> l10n("设备观察", "observation")
    AgentFailureSubsystem.DEVICE_ACTION -> l10n("设备动作", "device action")
    AgentFailureSubsystem.VERIFICATION -> l10n("完成验证", "verification")
    AgentFailureSubsystem.SAFETY_POLICY -> l10n("安全策略", "safety policy")
    AgentFailureSubsystem.ORCHESTRATOR -> l10n("任务编排", "orchestrator")
    AgentFailureSubsystem.UNKNOWN -> l10n("未知", "unknown")
}

@Composable
private fun actionDisplayName(action: AgentAction): String = when (action) {
    AgentAction.Observe -> stringResource(Res.string.agent_action_observe)
    is AgentAction.FindApp -> stringResource(Res.string.agent_action_find_app, action.query)
    is AgentAction.OpenApp -> stringResource(Res.string.agent_action_launch, action.query)
    is AgentAction.Tap -> stringResource(Res.string.agent_action_tap, action.x, action.y)
    is AgentAction.TapElement -> stringResource(Res.string.agent_action_tap_element, action.elementId)
    is AgentAction.Swipe -> stringResource(Res.string.agent_action_swipe)
    is AgentAction.InputText -> stringResource(Res.string.agent_action_input)
    is AgentAction.KeyEvent -> stringResource(Res.string.agent_action_key, action.key.name)
    is AgentAction.LaunchPackage -> stringResource(Res.string.agent_action_launch, action.packageName)
    is AgentAction.Wait -> stringResource(Res.string.agent_action_wait, action.durationMs)
    is AgentAction.Finish -> stringResource(Res.string.agent_action_finish)
    is AgentAction.ForceStopPackage -> stringResource(Res.string.agent_action_force_stop, action.packageName)
    is AgentAction.ClearAppData -> stringResource(Res.string.agent_action_clear_data, action.packageName)
    is AgentAction.UninstallPackage -> stringResource(Res.string.agent_action_uninstall, action.packageName)
    AgentAction.RebootDevice -> stringResource(Res.string.agent_action_reboot)
}
