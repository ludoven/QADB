package com.ludoven.adbtool.pages

import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.ui.icons.IconParkIcons

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.ai_add_model
import adbtool_desktop.composeapp.generated.resources.ai_api_key
import adbtool_desktop.composeapp.generated.resources.ai_api_key_hint
import adbtool_desktop.composeapp.generated.resources.ai_api_key_required_hint
import adbtool_desktop.composeapp.generated.resources.ai_base_url
import adbtool_desktop.composeapp.generated.resources.ai_base_url_hint
import adbtool_desktop.composeapp.generated.resources.ai_clear_key
import adbtool_desktop.composeapp.generated.resources.ai_config_saved
import adbtool_desktop.composeapp.generated.resources.ai_context_window
import adbtool_desktop.composeapp.generated.resources.ai_context_window_hint
import adbtool_desktop.composeapp.generated.resources.ai_edit_model
import adbtool_desktop.composeapp.generated.resources.ai_agent_beta_disabled_desc
import adbtool_desktop.composeapp.generated.resources.ai_agent_beta_enabled_desc
import adbtool_desktop.composeapp.generated.resources.ai_agent_beta_settings
import adbtool_desktop.composeapp.generated.resources.ai_agent_beta_title
import adbtool_desktop.composeapp.generated.resources.ai_agent_reduce_motion_desc
import adbtool_desktop.composeapp.generated.resources.ai_agent_reduce_motion_title
import adbtool_desktop.composeapp.generated.resources.ai_hide_key
import adbtool_desktop.composeapp.generated.resources.ai_key_cleared
import adbtool_desktop.composeapp.generated.resources.ai_model_configured
import adbtool_desktop.composeapp.generated.resources.ai_model_configured_desc
import adbtool_desktop.composeapp.generated.resources.ai_model_name
import adbtool_desktop.composeapp.generated.resources.ai_model_name_hint
import adbtool_desktop.composeapp.generated.resources.ai_model_not_configured
import adbtool_desktop.composeapp.generated.resources.ai_model_not_configured_desc
import adbtool_desktop.composeapp.generated.resources.ai_model_settings
import adbtool_desktop.composeapp.generated.resources.ai_memory_clear
import adbtool_desktop.composeapp.generated.resources.ai_memory_clear_desc
import adbtool_desktop.composeapp.generated.resources.ai_memory_clear_title
import adbtool_desktop.composeapp.generated.resources.ai_memory_content
import adbtool_desktop.composeapp.generated.resources.ai_memory_delete
import adbtool_desktop.composeapp.generated.resources.ai_memory_disabled_desc
import adbtool_desktop.composeapp.generated.resources.ai_memory_edit
import adbtool_desktop.composeapp.generated.resources.ai_memory_edit_title
import adbtool_desktop.composeapp.generated.resources.ai_memory_empty
import adbtool_desktop.composeapp.generated.resources.ai_memory_enabled_desc
import adbtool_desktop.composeapp.generated.resources.ai_memory_keywords
import adbtool_desktop.composeapp.generated.resources.ai_memory_manage
import adbtool_desktop.composeapp.generated.resources.ai_memory_manage_title
import adbtool_desktop.composeapp.generated.resources.ai_memory_privacy
import adbtool_desktop.composeapp.generated.resources.ai_memory_settings
import adbtool_desktop.composeapp.generated.resources.ai_memory_stats
import adbtool_desktop.composeapp.generated.resources.ai_memory_title
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_install
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_installed
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_missing
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_no_device
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_settings
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_test
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_title
import adbtool_desktop.composeapp.generated.resources.ai_input_helper_uninstall
import adbtool_desktop.composeapp.generated.resources.agent_memory_consent_desc
import adbtool_desktop.composeapp.generated.resources.agent_memory_consent_title
import adbtool_desktop.composeapp.generated.resources.agent_memory_enable
import adbtool_desktop.composeapp.generated.resources.agent_memory_not_now
import adbtool_desktop.composeapp.generated.resources.agent_approval_settings
import adbtool_desktop.composeapp.generated.resources.agent_approval_policy
import adbtool_desktop.composeapp.generated.resources.agent_approval_smart
import adbtool_desktop.composeapp.generated.resources.agent_approval_cautious
import adbtool_desktop.composeapp.generated.resources.agent_approval_smart_desc
import adbtool_desktop.composeapp.generated.resources.agent_approval_cautious_desc
import adbtool_desktop.composeapp.generated.resources.ai_openai_compatible_badge
import adbtool_desktop.composeapp.generated.resources.ai_privacy_notice
import adbtool_desktop.composeapp.generated.resources.ai_save_config
import adbtool_desktop.composeapp.generated.resources.ai_show_key
import adbtool_desktop.composeapp.generated.resources.ai_test_connection
import adbtool_desktop.composeapp.generated.resources.ai_test_success
import adbtool_desktop.composeapp.generated.resources.ai_vision_auto
import adbtool_desktop.composeapp.generated.resources.ai_vision_disabled
import adbtool_desktop.composeapp.generated.resources.ai_vision_enabled
import adbtool_desktop.composeapp.generated.resources.ai_vision_mode
import adbtool_desktop.composeapp.generated.resources.adb_auto_detect
import adbtool_desktop.composeapp.generated.resources.adb_current_using
import adbtool_desktop.composeapp.generated.resources.adb_environment_checking
import adbtool_desktop.composeapp.generated.resources.adb_environment_failed
import adbtool_desktop.composeapp.generated.resources.adb_environment_ready
import adbtool_desktop.composeapp.generated.resources.adb_open_help
import adbtool_desktop.composeapp.generated.resources.adb_path_label
import adbtool_desktop.composeapp.generated.resources.adb_path_setting
import adbtool_desktop.composeapp.generated.resources.adb_restore_bundled
import adbtool_desktop.composeapp.generated.resources.adb_select_adb
import adbtool_desktop.composeapp.generated.resources.adb_source_bundled
import adbtool_desktop.composeapp.generated.resources.adb_source_custom
import adbtool_desktop.composeapp.generated.resources.adb_source_none
import adbtool_desktop.composeapp.generated.resources.adb_source_system
import adbtool_desktop.composeapp.generated.resources.adb_version_label
import adbtool_desktop.composeapp.generated.resources.cancel
import adbtool_desktop.composeapp.generated.resources.check_update
import adbtool_desktop.composeapp.generated.resources.current_version
import adbtool_desktop.composeapp.generated.resources.download_and_install
import adbtool_desktop.composeapp.generated.resources.downloading_update
import adbtool_desktop.composeapp.generated.resources.language_changed
import adbtool_desktop.composeapp.generated.resources.not_set
import adbtool_desktop.composeapp.generated.resources.open_release_page
import adbtool_desktop.composeapp.generated.resources.preferences_setting
import adbtool_desktop.composeapp.generated.resources.select_language
import adbtool_desktop.composeapp.generated.resources.select_theme
import adbtool_desktop.composeapp.generated.resources.settings_adb_path_desc
import adbtool_desktop.composeapp.generated.resources.settings_adb_source_desc
import adbtool_desktop.composeapp.generated.resources.settings_adb_version_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_auto_detect_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_auto_detect_title
import adbtool_desktop.composeapp.generated.resources.settings_pref_language_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_minimize_on_close_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_minimize_on_close_title
import adbtool_desktop.composeapp.generated.resources.settings_pref_minimize_on_launch_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_minimize_on_launch_title
import adbtool_desktop.composeapp.generated.resources.settings_pref_remember_device_desc
import adbtool_desktop.composeapp.generated.resources.settings_pref_remember_device_title
import adbtool_desktop.composeapp.generated.resources.settings_pref_theme_desc
import adbtool_desktop.composeapp.generated.resources.settings_status_label
import adbtool_desktop.composeapp.generated.resources.settings_subtitle
import adbtool_desktop.composeapp.generated.resources.settings_update_current_version_desc
import adbtool_desktop.composeapp.generated.resources.settings_update_status
import adbtool_desktop.composeapp.generated.resources.settings_update_status_desc
import adbtool_desktop.composeapp.generated.resources.settings_update_status_latest
import adbtool_desktop.composeapp.generated.resources.set
import adbtool_desktop.composeapp.generated.resources.theme_mode_dark
import adbtool_desktop.composeapp.generated.resources.theme_mode_light
import adbtool_desktop.composeapp.generated.resources.theme_mode_system
import adbtool_desktop.composeapp.generated.resources.update_available_no_asset
import adbtool_desktop.composeapp.generated.resources.update_available_with_version
import adbtool_desktop.composeapp.generated.resources.update_check_failed
import adbtool_desktop.composeapp.generated.resources.update_checking
import adbtool_desktop.composeapp.generated.resources.update_download_failed
import adbtool_desktop.composeapp.generated.resources.update_download_success
import adbtool_desktop.composeapp.generated.resources.update_section_title
import adbtool_desktop.composeapp.generated.resources.update_up_to_date
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ludoven.adbtool.AppVersion
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.agent.AiConfiguration
import com.ludoven.adbtool.agent.AgentProviderRuntime
import com.ludoven.adbtool.agent.AgentCapabilityProbe
import com.ludoven.adbtool.agent.AgentCapabilityReport
import com.ludoven.adbtool.agent.AgentCapabilityTier
import com.ludoven.adbtool.agent.AgentModelRole
import com.ludoven.adbtool.agent.AiModelConfig
import com.ludoven.adbtool.agent.AgentInputHelper
import com.ludoven.adbtool.agent.AgentInputHelperStatus
import com.ludoven.adbtool.agent.AgentModelCatalog
import com.ludoven.adbtool.agent.AgentApprovalPolicy
import com.ludoven.adbtool.agent.AgentApprovalRuntime
import com.ludoven.adbtool.agent.AgentFeatureRuntime
import com.ludoven.adbtool.agent.ArtemisEngineRuntime
import com.ludoven.adbtool.agent.VisionMode
import com.ludoven.adbtool.agent.artemis.ArtemisProfile
import com.ludoven.adbtool.ui.icons.CompatIconVectors
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.ui.mac.AlertDialog
import com.ludoven.adbtool.ui.mac.Button
import com.ludoven.adbtool.ui.mac.ButtonDefaults
import com.ludoven.adbtool.ui.mac.CircularProgressIndicator
import com.ludoven.adbtool.ui.mac.DropdownMenu
import com.ludoven.adbtool.ui.mac.DropdownMenuItem
import com.ludoven.adbtool.ui.mac.HorizontalDivider
import com.ludoven.adbtool.ui.mac.Icon
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.OutlinedButton
import com.ludoven.adbtool.ui.mac.OutlinedTextField
import com.ludoven.adbtool.ui.mac.Surface
import com.ludoven.adbtool.ui.mac.Switch
import com.ludoven.adbtool.ui.mac.Text
import com.ludoven.adbtool.ui.mac.TextButton
import com.ludoven.adbtool.ui.mac.bodyLarge
import com.ludoven.adbtool.ui.mac.bodyMedium
import com.ludoven.adbtool.ui.mac.bodySmall
import com.ludoven.adbtool.ui.mac.headlineSmall
import com.ludoven.adbtool.ui.mac.titleMedium
import com.ludoven.adbtool.util.AdbPathManager
import com.ludoven.adbtool.util.AppBehaviorPreferences
import com.ludoven.adbtool.util.FileUtils
import com.ludoven.adbtool.util.GitHubUpdateManager
import com.ludoven.adbtool.widget.FeedbackToast
import com.ludoven.adbtool.util.LanguageManager
import com.ludoven.adbtool.util.ThemeManager
import com.ludoven.adbtool.widget.PageHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.io.File

@Composable
private fun SettingSection.label(): String = when (this) {
    SettingSection.OVERVIEW -> l10n("概览", "Overview")
    SettingSection.GENERAL -> l10n("通用", "General")
    SettingSection.ADB -> stringResource(Res.string.adb_path_setting)
    SettingSection.AI_AGENT -> l10n("实验功能", "Experimental")
    SettingSection.ABOUT -> l10n("关于与更新", "About & Updates")
}

private fun SettingSection.icon(): ImageVector = when (this) {
    SettingSection.OVERVIEW -> IconParkIcons.List
    SettingSection.GENERAL -> IconParkIcons.Setting
    SettingSection.ADB -> IconParkIcons.Usb
    SettingSection.AI_AGENT -> IconParkIcons.Sparkles
    SettingSection.ABOUT -> IconParkIcons.Info
}

private object SettingColors {
    val PageBackground: Color @Composable get() = QadbColors.background
    val Surface: Color @Composable get() = QadbColors.surface
    val SoftSurface: Color @Composable get() = QadbColors.surfaceVariant
    val Text: Color @Composable get() = QadbColors.textPrimary
    val SecondaryText: Color @Composable get() = QadbColors.textSecondary
    val Muted: Color @Composable get() = QadbColors.textTertiary
    val Border: Color @Composable get() = QadbColors.border
    val Divider: Color @Composable get() = QadbColors.divider
    val Primary: Color @Composable get() = QadbColors.primary
    val PrimarySoft: Color @Composable get() = QadbColors.primaryContainer
    val PrimaryBorder: Color @Composable get() = QadbColors.primary.copy(alpha = 0.34f)
    val ButtonBorder: Color @Composable get() = QadbColors.border
    val ControlBackground: Color @Composable get() = QadbColors.disabledSurface
    val Danger: Color @Composable get() = QadbColors.danger
    val Success: Color @Composable get() = QadbColors.success
}

@Composable
private fun SettingsSectionTabs(
    selectedSection: SettingSection,
    onSectionSelected: (SettingSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingSection.entries.forEach { section ->
            val isSelected = section == selectedSection
            val onSelect = remember(section, onSectionSelected) {
                { onSectionSelected(section) }
            }
            SettingsSectionTab(
                section = section,
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun SettingsSectionTab(
    section: SettingSection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val foreground = when {
        selected -> SettingColors.Primary
        hovered -> SettingColors.Text
        else -> SettingColors.SecondaryText
    }
    val background = when {
        hovered && !selected -> SettingColors.SoftSurface
        else -> Color.Transparent
    }
    val indicatorColor = SettingColors.Primary

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(background)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                if (selected) {
                    val strokeWidth = 2.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = indicatorColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = section.icon(),
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = section.label(),
                color = foreground,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1
            )
            if (section == SettingSection.AI_AGENT) {
                Text(
                    text = "Beta",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) SettingColors.PrimarySoft else SettingColors.SoftSurface)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    color = if (selected) SettingColors.Primary else SettingColors.Muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val clickable = enabled && !loading

    val containerColor by animateColorAsState(
        targetValue = when {
            !clickable -> (if (primary) SettingColors.Primary else SettingColors.SoftSurface).copy(alpha = 0.45f)
            primary && isPressed -> Color(0xFF0039CB)
            primary && isHovered -> Color(0xFF1E53E5)
            primary -> SettingColors.Primary
            isPressed -> SettingColors.SoftSurface
            isHovered -> SettingColors.SoftSurface.copy(alpha = 0.85f)
            else -> SettingColors.Surface
        },
        animationSpec = tween(UiTokens.HoverDurationMillis)
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            !clickable -> Color.Transparent
            primary -> containerColor
            isHovered -> SettingColors.Primary.copy(alpha = 0.45f)
            else -> SettingColors.Border
        },
        animationSpec = tween(UiTokens.HoverDurationMillis)
    )

    val contentColor = when {
        !clickable -> SettingColors.Muted
        primary -> QadbColors.onPrimary
        isHovered -> SettingColors.Primary
        else -> SettingColors.Text
    }
    val iconColor = when {
        !clickable -> SettingColors.Muted
        primary -> QadbColors.onPrimary
        isHovered -> SettingColors.Primary
        else -> SettingColors.SecondaryText
    }

    Box(
        modifier = modifier
            .height(28.dp)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .hoverable(interactionSource)
            .pointerHoverIcon(if (clickable) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                enabled = clickable,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = if (primary && enabled) QadbColors.onPrimary else SettingColors.Primary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingHeaderTrailingWidget(
    currentVersionLabel: String,
    updateStatus: UpdateStatus,
    releaseUrl: String?,
    downloadableAsset: GitHubUpdateManager.GitHubAssetResponse?,
    isCheckingUpdate: Boolean,
    isDownloadingUpdate: Boolean,
    onCheckUpdate: () -> Unit,
    onDownloadAndInstall: () -> Unit,
    onOpenReleasePage: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Version badge
        Box(
            modifier = Modifier
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SettingColors.SoftSurface.copy(alpha = 0.7f))
                .border(1.dp, SettingColors.Border.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "v$currentVersionLabel",
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Update actions / status indicator
        when (val status = updateStatus) {
            is UpdateStatus.Checking -> {
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SettingColors.SoftSurface)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 2.dp,
                        color = SettingColors.Primary
                    )
                    Text(
                        text = stringResource(Res.string.update_checking),
                        color = SettingColors.SecondaryText,
                        fontSize = 11.5.sp
                    )
                }
            }
            is UpdateStatus.Downloading -> {
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SettingColors.PrimarySoft)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 2.dp,
                        color = SettingColors.Primary
                    )
                    Text(
                        text = stringResource(Res.string.downloading_update),
                        color = SettingColors.Primary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is UpdateStatus.UpdateAvailable -> {
                if (downloadableAsset != null) {
                    SettingActionButton(
                        text = "${stringResource(Res.string.download_and_install)} (${status.latestVersion})",
                        icon = IconParkIcons.Download,
                        onClick = onDownloadAndInstall,
                        primary = true,
                        enabled = !isCheckingUpdate && !isDownloadingUpdate,
                        loading = isDownloadingUpdate
                    )
                } else if (!releaseUrl.isNullOrBlank()) {
                    SettingActionButton(
                        text = "${stringResource(Res.string.open_release_page)} (${status.latestVersion})",
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = onOpenReleasePage,
                        primary = true
                    )
                }
            }
            is UpdateStatus.DownloadSuccess -> {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SettingColors.Success.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.update_download_success, status.path),
                        color = SettingColors.Success,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is UpdateStatus.UpToDate -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SettingColors.Success.copy(alpha = 0.1f))
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = l10n("已是最新版", "Up to date"),
                            color = SettingColors.Success,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    SettingActionButton(
                        text = stringResource(Res.string.check_update),
                        icon = IconParkIcons.Refresh,
                        onClick = onCheckUpdate,
                        enabled = !isCheckingUpdate && !isDownloadingUpdate,
                        loading = isCheckingUpdate
                    )
                }
            }
            else -> {
                SettingActionButton(
                    text = stringResource(Res.string.check_update),
                    icon = IconParkIcons.Refresh,
                    onClick = onCheckUpdate,
                    enabled = !isCheckingUpdate && !isDownloadingUpdate,
                    loading = isCheckingUpdate
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SettingColors.SoftSurface.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, SettingColors.Border.copy(alpha = 0.6f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = SettingColors.Text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
    tone: StatusTone? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = if (hovered) SettingColors.Primary else SettingColors.Text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (tone != null) {
            StatusPill(statusText = value, tone = tone)
        } else {
            Text(
                text = value,
                color = SettingColors.SecondaryText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = IconParkIcons.Right,
            contentDescription = null,
            tint = if (hovered) SettingColors.Primary else SettingColors.Muted,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun SettingsValueRow(
    title: String,
    description: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, color = SettingColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(description, color = SettingColors.SecondaryText, fontSize = 11.5.sp)
        }
        Spacer(Modifier.width(16.dp))
        Text(value, color = SettingColors.SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun updateStatusSummary(status: UpdateStatus): String = when (status) {
    UpdateStatus.Idle -> l10n("尚未检查", "Not checked")
    UpdateStatus.Checking -> stringResource(Res.string.update_checking)
    UpdateStatus.Downloading -> stringResource(Res.string.downloading_update)
    is UpdateStatus.UpToDate -> stringResource(Res.string.update_up_to_date, status.latestVersion)
    is UpdateStatus.UpdateAvailable -> if (status.hasAutoInstall) {
        stringResource(Res.string.update_available_with_version, status.latestVersion)
    } else {
        stringResource(Res.string.update_available_no_asset, status.latestVersion)
    }
    is UpdateStatus.DownloadSuccess -> stringResource(Res.string.update_download_success, status.path)
    is UpdateStatus.CheckFailed -> stringResource(Res.string.update_check_failed, status.reason)
    is UpdateStatus.DownloadFailed -> stringResource(Res.string.update_download_failed, status.reason)
}

@Composable
private fun SettingsOverviewContent(
    selectedDeviceId: String?,
    adbEnvironment: AdbPathManager.AdbEnvironment,
    agentFeatureEnabled: Boolean,
    updateStatus: UpdateStatus,
    autoDetectDeviceOnLaunch: Boolean,
    rememberLastDevice: Boolean,
    onAutoDetectDeviceOnLaunchChange: (Boolean) -> Unit,
    onRememberLastDeviceChange: (Boolean) -> Unit,
    onOpenAdb: () -> Unit,
    onOpenAgent: () -> Unit,
    onOpenUpdates: () -> Unit,
    onAutoDetectAdb: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSectionTitle(l10n("应用健康", "App health"))
        SettingsGroup {
            SettingsNavigationRow(
                title = if (adbEnvironment.isReady) l10n("一切正常", "Everything is ready") else l10n("需要处理", "Needs attention"),
                description = if (adbEnvironment.isReady) {
                    l10n("ADB 环境可用，各项核心功能已就绪", "ADB is available and core features are ready")
                } else {
                    adbEnvironment.message ?: stringResource(Res.string.adb_environment_failed)
                },
                value = if (adbEnvironment.isReady) l10n("正常", "Healthy") else l10n("未就绪", "Not ready"),
                tone = if (adbEnvironment.isReady) StatusTone.Positive else StatusTone.Danger,
                onClick = onOpenAdb
            )
        }

        SettingsSectionTitle(l10n("常用设置", "Common settings"))
        SettingsGroup {
            SettingSwitchRow(
                title = stringResource(Res.string.settings_pref_auto_detect_title),
                description = stringResource(Res.string.settings_pref_auto_detect_desc),
                checked = autoDetectDeviceOnLaunch,
                onCheckedChange = onAutoDetectDeviceOnLaunchChange
            )
            SectionDivider()
            SettingSwitchRow(
                title = stringResource(Res.string.settings_pref_remember_device_title),
                description = stringResource(Res.string.settings_pref_remember_device_desc),
                checked = rememberLastDevice,
                onCheckedChange = onRememberLastDeviceChange
            )
        }

        SettingsSectionTitle(l10n("系统状态", "System status"))
        SettingsGroup {
            SettingsNavigationRow(
                title = stringResource(Res.string.adb_path_setting),
                description = adbEnvironment.path ?: stringResource(Res.string.not_set),
                value = settingAdbSourceText(adbEnvironment.source),
                tone = if (adbEnvironment.isReady) StatusTone.Positive else StatusTone.Danger,
                onClick = onOpenAdb
            )
            SectionDivider()
            SettingsNavigationRow(
                title = l10n("AI Agent（Beta）", "AI Agent (Beta)"),
                description = if (agentFeatureEnabled) {
                    stringResource(Res.string.ai_agent_beta_enabled_desc)
                } else {
                    stringResource(Res.string.ai_agent_beta_disabled_desc)
                },
                value = if (agentFeatureEnabled) l10n("已启用", "Enabled") else l10n("未启用", "Disabled"),
                tone = if (agentFeatureEnabled) StatusTone.Positive else StatusTone.Neutral,
                onClick = onOpenAgent
            )
            SectionDivider()
            SettingsNavigationRow(
                title = l10n("更新", "Updates"),
                description = l10n("查看当前版本与最近检查结果", "Review the installed version and latest check"),
                value = updateStatusSummary(updateStatus),
                onClick = onOpenUpdates
            )
        }

        SettingsSectionTitle(l10n("快速操作", "Quick actions"))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            SettingActionButton(
                text = stringResource(Res.string.adb_auto_detect),
                icon = IconParkIcons.Refresh,
                onClick = onAutoDetectAdb
            )
            SettingActionButton(
                text = stringResource(Res.string.adb_path_setting),
                icon = IconParkIcons.Usb,
                onClick = onOpenAdb
            )
            SettingActionButton(
                text = l10n("配置 AI Agent", "Configure AI Agent"),
                icon = IconParkIcons.Sparkles,
                onClick = onOpenAgent,
                primary = true
            )
        }

        if (selectedDeviceId == null) {
            Text(
                text = l10n("当前未连接设备，设备相关设置将在连接后生效。", "No device is connected. Device-specific settings apply after connection."),
                color = SettingColors.Muted,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun SettingsAboutContent(
    currentVersionLabel: String,
    updateStatus: UpdateStatus,
    isCheckingUpdate: Boolean,
    isDownloadingUpdate: Boolean,
    onCheckUpdate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSectionTitle(l10n("关于 QADB", "About QADB"))
        SettingsGroup {
            SettingsValueRow(
                title = l10n("当前版本", "Current version"),
                description = stringResource(Res.string.settings_update_current_version_desc),
                value = "v$currentVersionLabel"
            )
            SectionDivider()
            SettingsValueRow(
                title = stringResource(Res.string.settings_update_status),
                description = stringResource(Res.string.settings_update_status_desc),
                value = updateStatusSummary(updateStatus)
            )
            SectionDivider()
            SettingsValueRow(
                title = l10n("更新通道", "Update channel"),
                description = l10n("接收稳定版本更新", "Receive stable releases"),
                value = l10n("稳定版", "Stable")
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SettingActionButton(
                text = stringResource(Res.string.check_update),
                icon = IconParkIcons.Refresh,
                onClick = onCheckUpdate,
                primary = true,
                enabled = !isCheckingUpdate && !isDownloadingUpdate,
                loading = isCheckingUpdate
            )
        }
    }
}

@Composable
fun SettingScreen(selectedDeviceId: String? = null) {
    var selectedSection by remember { mutableStateOf(SettingSection.OVERVIEW) }
    val notSetText = stringResource(Res.string.not_set)
    val adbEnvironment by AdbPathManager.adbEnvironment.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf<UpdateStatus>(UpdateStatus.Idle) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var releaseUrl by remember { mutableStateOf<String?>(null) }
    var downloadableAsset by remember { mutableStateOf<GitHubUpdateManager.GitHubAssetResponse?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val currentThemeMode by ThemeManager.currentThemeMode.collectAsState()
    val supportedLanguages = LanguageManager.getSupportedLanguages()
    val supportedThemeModes = ThemeManager.ThemeMode.entries
    val currentVersionLabel = AppVersion.CURRENT
    val behaviorPreferences = remember { AppBehaviorPreferences.store }
    val autoDetectDeviceOnLaunch by behaviorPreferences.autoDetectDeviceOnLaunch.collectAsState()
    val rememberLastDevice by behaviorPreferences.rememberLastDevice.collectAsState()
    val minimizeToTrayOnLaunch by behaviorPreferences.minimizeToTrayOnLaunch.collectAsState()
    val minimizeToTrayOnClose by behaviorPreferences.minimizeToTrayOnClose.collectAsState()
    val agentFeaturePreferences = remember { AgentFeatureRuntime.preferences }
    val agentFeatureEnabled by agentFeaturePreferences.enabled.collectAsState()

    val triggerCheckUpdate: () -> Unit = {
        coroutineScope.launch {
            isCheckingUpdate = true
            updateStatus = UpdateStatus.Checking
            val result = GitHubUpdateManager.checkForUpdate(currentVersionLabel)
            when (result) {
                is GitHubUpdateManager.CheckResult.UpToDate -> {
                    latestVersion = result.latestVersion
                    releaseUrl = null
                    downloadableAsset = null
                    updateStatus = UpdateStatus.UpToDate(result.latestVersion)
                }
                is GitHubUpdateManager.CheckResult.UpdateAvailable -> {
                    latestVersion = result.latestVersion
                    releaseUrl = result.htmlUrl
                    downloadableAsset = result.asset
                    updateStatus = UpdateStatus.UpdateAvailable(
                        result.latestVersion,
                        hasAutoInstall = true
                    )
                }
                is GitHubUpdateManager.CheckResult.UpdateAvailableNoAsset -> {
                    latestVersion = result.latestVersion
                    releaseUrl = result.htmlUrl
                    downloadableAsset = null
                    updateStatus = UpdateStatus.UpdateAvailable(
                        result.latestVersion,
                        hasAutoInstall = false
                    )
                }
                is GitHubUpdateManager.CheckResult.Error -> {
                    updateStatus = UpdateStatus.CheckFailed(result.message)
                }
            }
            isCheckingUpdate = false
        }
    }

    val triggerDownloadAndInstall: () -> Unit = {
        val asset = downloadableAsset
        if (asset != null) {
            coroutineScope.launch {
                isDownloadingUpdate = true
                updateStatus = UpdateStatus.Downloading
                val result = GitHubUpdateManager.downloadAndInstall(asset)
                result.onSuccess { path ->
                    updateStatus = UpdateStatus.DownloadSuccess(path.toString())
                }.onFailure { error ->
                    updateStatus = UpdateStatus.DownloadFailed(error.message ?: "Unknown error")
                }
                isDownloadingUpdate = false
            }
        }
    }

    val triggerOpenReleasePage: () -> Unit = {
        val url = releaseUrl
        if (!url.isNullOrBlank()) {
            GitHubUpdateManager.openReleasePage(url).onFailure { error ->
                updateStatus = UpdateStatus.CheckFailed(error.message ?: "Unknown error")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SettingColors.PageBackground)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 816.dp)
                .fillMaxWidth()
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            PageHeader(
                title = stringResource(Res.string.set),
                subtitle = stringResource(Res.string.settings_subtitle),
                trailing = {
                    SettingHeaderTrailingWidget(
                        currentVersionLabel = currentVersionLabel,
                        updateStatus = updateStatus,
                        releaseUrl = releaseUrl,
                        downloadableAsset = downloadableAsset,
                        isCheckingUpdate = isCheckingUpdate,
                        isDownloadingUpdate = isDownloadingUpdate,
                        onCheckUpdate = triggerCheckUpdate,
                        onDownloadAndInstall = triggerDownloadAndInstall,
                        onOpenReleasePage = triggerOpenReleasePage
                    )
                }
            )

            Spacer(Modifier.height(10.dp))

            SettingsSectionTabs(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it }
            )

            HorizontalDivider(color = SettingColors.Divider.copy(alpha = 0.7f))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedSection,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(160)) + slideInVertically(animationSpec = tween(160)) { 6 }) togetherWith
                            fadeOut(animationSpec = tween(120))
                    },
                    label = "SettingSectionContent"
                ) { targetSection ->
                    when (targetSection) {
                        SettingSection.OVERVIEW -> {
                            SettingsOverviewContent(
                                selectedDeviceId = selectedDeviceId,
                                adbEnvironment = adbEnvironment,
                                agentFeatureEnabled = agentFeatureEnabled,
                                updateStatus = updateStatus,
                                autoDetectDeviceOnLaunch = autoDetectDeviceOnLaunch,
                                rememberLastDevice = rememberLastDevice,
                                onAutoDetectDeviceOnLaunchChange = behaviorPreferences::setAutoDetectDeviceOnLaunch,
                                onRememberLastDeviceChange = behaviorPreferences::setRememberLastDevice,
                                onOpenAdb = { selectedSection = SettingSection.ADB },
                                onOpenAgent = { selectedSection = SettingSection.AI_AGENT },
                                onOpenUpdates = { selectedSection = SettingSection.ABOUT },
                                onAutoDetectAdb = { coroutineScope.launch { AdbPathManager.autoDetect() } }
                            )
                        }

                        SettingSection.GENERAL -> {
                        SettingsGroup {
                            val currentThemeText = when (currentThemeMode) {
                                ThemeManager.ThemeMode.SYSTEM -> stringResource(Res.string.theme_mode_system)
                                ThemeManager.ThemeMode.LIGHT -> stringResource(Res.string.theme_mode_light)
                                ThemeManager.ThemeMode.DARK -> stringResource(Res.string.theme_mode_dark)
                            }

                            SettingsDropdownRow(
                                title = stringResource(Res.string.select_language),
                                description = stringResource(Res.string.settings_pref_language_desc),
                                value = currentLanguage.displayName,
                                expanded = showLanguageDropdown,
                                onExpandedChange = { showLanguageDropdown = it }
                            ) {
                                supportedLanguages.forEach { language ->
                                    DropdownMenuItem(
                                        text = { Text(language.displayName) },
                                        onClick = {
                                            LanguageManager.setLanguage(language)
                                            showLanguageDropdown = false
                                            showLanguageDialog = true
                                        }
                                    )
                                }
                            }

                            SectionDivider()

                            SettingsDropdownRow(
                                title = stringResource(Res.string.select_theme),
                                description = stringResource(Res.string.settings_pref_theme_desc),
                                value = currentThemeText,
                                expanded = showThemeDropdown,
                                onExpandedChange = { showThemeDropdown = it }
                            ) {
                                supportedThemeModes.forEach { themeMode ->
                                    val text = when (themeMode) {
                                        ThemeManager.ThemeMode.SYSTEM -> stringResource(Res.string.theme_mode_system)
                                        ThemeManager.ThemeMode.LIGHT -> stringResource(Res.string.theme_mode_light)
                                        ThemeManager.ThemeMode.DARK -> stringResource(Res.string.theme_mode_dark)
                                    }
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            ThemeManager.setThemeMode(themeMode)
                                            showThemeDropdown = false
                                        }
                                    )
                                }
                            }

                            SectionDivider()

                            SettingSwitchRow(
                                title = stringResource(Res.string.settings_pref_auto_detect_title),
                                description = stringResource(Res.string.settings_pref_auto_detect_desc),
                                checked = autoDetectDeviceOnLaunch,
                                onCheckedChange = behaviorPreferences::setAutoDetectDeviceOnLaunch
                            )

                            SectionDivider()

                            SettingSwitchRow(
                                title = stringResource(Res.string.settings_pref_remember_device_title),
                                description = stringResource(Res.string.settings_pref_remember_device_desc),
                                checked = rememberLastDevice,
                                onCheckedChange = behaviorPreferences::setRememberLastDevice
                            )

                            SectionDivider()

                            SettingSwitchRow(
                                title = stringResource(Res.string.settings_pref_minimize_on_launch_title),
                                description = stringResource(Res.string.settings_pref_minimize_on_launch_desc),
                                checked = minimizeToTrayOnLaunch,
                                onCheckedChange = behaviorPreferences::setMinimizeToTrayOnLaunch
                            )

                            SectionDivider()

                            SettingSwitchRow(
                                title = stringResource(Res.string.settings_pref_minimize_on_close_title),
                                description = stringResource(Res.string.settings_pref_minimize_on_close_desc),
                                checked = minimizeToTrayOnClose,
                                onCheckedChange = behaviorPreferences::setMinimizeToTrayOnClose
                            )
                        }
                    }

                    SettingSection.ADB -> {
                        val sourceText = settingAdbSourceText(adbEnvironment.source)
                        val isCheckingAdb = !adbEnvironment.isReady &&
                            adbEnvironment.source == AdbPathManager.AdbSource.NONE &&
                            adbEnvironment.message?.contains("检测") == true
                        val statusText = if (adbEnvironment.isReady) {
                            stringResource(Res.string.adb_environment_ready)
                        } else if (isCheckingAdb) {
                            stringResource(Res.string.adb_environment_checking)
                        } else {
                            adbEnvironment.message ?: stringResource(Res.string.adb_environment_failed)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SettingsGroup {
                                // 1. Status & Source Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(Res.string.settings_status_label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SettingColors.Text,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = stringResource(Res.string.settings_adb_source_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SettingColors.SecondaryText,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatusPill(
                                            statusText = statusText,
                                            tone = when {
                                                adbEnvironment.isReady -> StatusTone.Positive
                                                isCheckingAdb -> StatusTone.Neutral
                                                else -> StatusTone.Danger
                                            }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SettingColors.Surface)
                                                .border(1.dp, SettingColors.Border.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = sourceText,
                                                color = SettingColors.SecondaryText,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                SectionDivider()

                                // 2. ADB Path Row
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 11.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = stringResource(Res.string.adb_path_label),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SettingColors.Text,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = stringResource(Res.string.settings_adb_path_desc),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SettingColors.SecondaryText,
                                                fontSize = 11.5.sp
                                            )
                                        }

                                        // Finder action
                                        val revealInteraction = remember { MutableInteractionSource() }
                                        val isRevealHovered by revealInteraction.collectIsHoveredAsState()
                                        Row(
                                            modifier = Modifier
                                                .height(26.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isRevealHovered) SettingColors.Primary.copy(alpha = 0.08f) else SettingColors.Surface)
                                                .border(
                                                    1.dp,
                                                    if (isRevealHovered) SettingColors.Primary.copy(alpha = 0.35f) else SettingColors.Border,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .hoverable(revealInteraction)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = revealInteraction,
                                                    indication = null,
                                                    onClick = { openPathLocation(adbEnvironment.path) }
                                                )
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = IconParkIcons.FolderOpen,
                                                contentDescription = null,
                                                tint = if (isRevealHovered) SettingColors.Primary else SettingColors.SecondaryText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = l10n("在访达中显示", "Reveal in Finder"),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isRevealHovered) SettingColors.Primary else SettingColors.SecondaryText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SettingColors.Surface)
                                            .border(1.dp, SettingColors.Border.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        SelectionContainer {
                                            Text(
                                                text = adbEnvironment.path ?: notSetText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SettingColors.Text,
                                                fontSize = 11.5.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                SectionDivider()

                                // 3. ADB Version Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(Res.string.adb_version_label),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SettingColors.Text,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = stringResource(Res.string.settings_adb_version_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SettingColors.SecondaryText,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                    Text(
                                        text = adbEnvironment.version ?: notSetText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SettingColors.Text,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }

                            // 4. Action Buttons Toolbar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SettingActionButton(
                                    text = stringResource(Res.string.adb_open_help),
                                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                                    onClick = { AdbPathManager.openHelp().onFailure { _ -> } }
                                )
                                SettingActionButton(
                                    text = stringResource(Res.string.adb_restore_bundled),
                                    icon = IconParkIcons.Setting,
                                    onClick = { coroutineScope.launch { AdbPathManager.useBundledAdb() } }
                                )
                                SettingActionButton(
                                    text = stringResource(Res.string.adb_select_adb),
                                    icon = IconParkIcons.File,
                                    onClick = {
                                        coroutineScope.launch {
                                            val newPath = FileUtils.selectFile()
                                            if (newPath != null) {
                                                AdbPathManager.setAdbPath(newPath)
                                            }
                                        }
                                    }
                                )
                                SettingActionButton(
                                    text = stringResource(Res.string.adb_auto_detect),
                                    icon = IconParkIcons.Refresh,
                                    onClick = { coroutineScope.launch { AdbPathManager.autoDetect() } },
                                    primary = true
                                )
                            }
                        }
                    }

                    SettingSection.AI_AGENT -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            SettingsGroup {
                                SettingSwitchRow(
                                    title = stringResource(Res.string.ai_agent_beta_title),
                                    description = stringResource(
                                        if (agentFeatureEnabled) Res.string.ai_agent_beta_enabled_desc
                                        else Res.string.ai_agent_beta_disabled_desc
                                    ),
                                    checked = agentFeatureEnabled,
                                    onCheckedChange = agentFeaturePreferences::setEnabled
                                )
                            }

                            if (agentFeatureEnabled) {
                                SettingsGroup {
                                    AiModelSettingsSection()
                                    SectionDivider()
                                    AgentApprovalSettingsSection()
                                    SectionDivider()
                                    AgentInputHelperSettingsSection(selectedDeviceId = selectedDeviceId)
                                }
                            }
                        }
                    }

                    SettingSection.ABOUT -> {
                        SettingsAboutContent(
                            currentVersionLabel = currentVersionLabel,
                            updateStatus = updateStatus,
                            isCheckingUpdate = isCheckingUpdate,
                            isDownloadingUpdate = isDownloadingUpdate,
                            onCheckUpdate = triggerCheckUpdate
                        )
                    }
                }
            }
            }
        }
    }

    LaunchedEffect(showLanguageDialog) {
        if (showLanguageDialog) {
            delay(2400L)
            showLanguageDialog = false
        }
    }
    FeedbackToast(
        if (showLanguageDialog) MsgContent.Resource(Res.string.language_changed) else null
    )
}

@Composable
private fun AgentApprovalSettingsSection() {
    val preferences = remember { AgentApprovalRuntime.preferences }
    val policy by preferences.policy.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val policyLabel = stringResource(
        when (policy) {
            AgentApprovalPolicy.SMART -> Res.string.agent_approval_smart
            AgentApprovalPolicy.CAUTIOUS -> Res.string.agent_approval_cautious
        }
    )

    SettingsDropdownRow(
        title = stringResource(Res.string.agent_approval_policy),
        description = stringResource(
            when (policy) {
                AgentApprovalPolicy.SMART -> Res.string.agent_approval_smart_desc
                AgentApprovalPolicy.CAUTIOUS -> Res.string.agent_approval_cautious_desc
            }
        ),
        value = policyLabel,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        AgentApprovalPolicy.entries.forEach { candidate ->
            val title = stringResource(
                if (candidate == AgentApprovalPolicy.SMART) {
                    Res.string.agent_approval_smart
                } else {
                    Res.string.agent_approval_cautious
                }
            )
            val description = stringResource(
                if (candidate == AgentApprovalPolicy.SMART) {
                    Res.string.agent_approval_smart_desc
                } else {
                    Res.string.agent_approval_cautious_desc
                }
            )
            DropdownMenuItem(
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(title, color = SettingColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(description, color = SettingColors.SecondaryText, fontSize = 11.5.sp)
                    }
                },
                onClick = {
                    preferences.setPolicy(candidate)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun AgentInputHelperSettingsSection(selectedDeviceId: String?) {
    val helper = remember { AgentInputHelper() }
    val coroutineScope = rememberCoroutineScope()
    var status by remember(selectedDeviceId) { mutableStateOf<AgentInputHelperStatus?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    fun refresh() {
        val deviceId = selectedDeviceId ?: return
        coroutineScope.launch {
            status = runCatching { helper.status(deviceId) }
                .onFailure { statusMessage = it.message }
                .getOrNull()
        }
    }

    LaunchedEffect(selectedDeviceId) {
        if (selectedDeviceId != null) refresh()
    }

    val description = when {
        selectedDeviceId == null -> stringResource(Res.string.ai_input_helper_no_device)
        status?.installed == true -> stringResource(Res.string.ai_input_helper_installed)
        else -> stringResource(Res.string.ai_input_helper_missing)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.ai_input_helper_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SettingColors.Text,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    StatusPill(
                        statusText = if (status?.installed == true) stringResource(Res.string.set) else stringResource(Res.string.not_set),
                        tone = if (status?.installed == true) StatusTone.Positive else StatusTone.Neutral
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingColors.SecondaryText,
                    fontSize = 11.5.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingActionButton(
                    text = stringResource(Res.string.ai_input_helper_install),
                    icon = CompatIconVectors.Keyboard,
                    onClick = {
                        val deviceId = selectedDeviceId ?: return@SettingActionButton
                        coroutineScope.launch {
                            isBusy = true
                            val result = helper.install(deviceId)
                            statusMessage = result.output
                            isBusy = false
                            refresh()
                        }
                    },
                    enabled = selectedDeviceId != null && !isBusy,
                    loading = isBusy
                )
                if (status?.installed == true) {
                    SettingActionButton(
                        text = stringResource(Res.string.ai_input_helper_test),
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = {
                            val deviceId = selectedDeviceId ?: return@SettingActionButton
                            coroutineScope.launch { statusMessage = helper.openTestScreen(deviceId).output }
                        },
                        enabled = selectedDeviceId != null && !isBusy
                    )
                    SettingActionButton(
                        text = stringResource(Res.string.ai_input_helper_uninstall),
                        icon = CompatIconVectors.Delete,
                        onClick = {
                            val deviceId = selectedDeviceId ?: return@SettingActionButton
                            coroutineScope.launch {
                                isBusy = true
                                statusMessage = helper.uninstall(deviceId).output
                                isBusy = false
                                refresh()
                            }
                        },
                        enabled = selectedDeviceId != null && !isBusy
                    )
                }
            }
        }

        statusMessage?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun AiModelSettingsSection() {
    val repository = remember { AiConfiguration.repository }
    val providerRepository = remember { AgentProviderRuntime.repository }
    val artemisPreferences = remember { ArtemisEngineRuntime.preferences }
    val artemisConfiguration by artemisPreferences.configuration.collectAsState()
    val providerProfiles by providerRepository.profiles.collectAsState()
    val savedConfig by repository.config.collectAsState()
    val hasSavedKey by repository.hasApiKeyState.collectAsState()
    var showModelDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    val configSavedText = stringResource(Res.string.ai_config_saved)
    val keyClearedText = stringResource(Res.string.ai_key_cleared)

    LaunchedEffect(Unit) {
        repository.hasApiKey()
    }

    val isConfigured = savedConfig.model.isNotBlank() && hasSavedKey
    val summaryTitle = if (isConfigured) {
        savedConfig.model
    } else {
        stringResource(Res.string.ai_model_not_configured)
    }
    val summaryDescription = if (isConfigured) {
        val configured = stringResource(
            Res.string.ai_model_configured_desc,
            visionModeText(savedConfig.visionMode)
        )
        val brainProfileId = providerRepository.providerFor(AgentModelRole.BRAIN)?.id
        val profile = providerProfiles.firstOrNull { it.id == brainProfileId }
        val attestation = profile?.let(providerRepository::capabilityAttestation)
        if (attestation == null) {
            "$configured · ${l10n("能力未验证", "Capabilities not verified")}"
        } else {
            "$configured · ${l10n("已验证", "Verified")} ${attestation.tier.name}"
        }
    } else {
        stringResource(Res.string.ai_model_not_configured_desc)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.ai_model_settings),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SettingColors.Text,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    if (isConfigured) {
                        StatusPill(
                            statusText = summaryTitle,
                            tone = StatusTone.Positive
                        )
                    }
                }
                Text(
                    text = summaryDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingColors.SecondaryText,
                    fontSize = 11.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(16.dp))

            SettingActionButton(
                text = stringResource(
                    if (isConfigured) Res.string.ai_edit_model else Res.string.ai_add_model
                ),
                icon = if (isConfigured) CompatIconVectors.Edit else CompatIconVectors.Add,
                onClick = {
                    statusMessage = null
                    showModelDialog = true
                },
                primary = !isConfigured
            )
        }

        SettingSwitchRow(
            title = l10n("Artemis 实验引擎", "Experimental Artemis engine"),
            description = l10n(
                "启用后仅使用本机 Artemis；未通过就绪检查时阻止发送，不会回退 Screenshot。运行中的任务不受设置变更影响。",
                "Uses local Artemis exclusively when enabled. Sending is blocked until ready and never falls back to Screenshot. Active tasks are unaffected."
            ),
            checked = artemisConfiguration.enabled,
            onCheckedChange = { enabled ->
                artemisPreferences.setConfiguration(artemisConfiguration.copy(enabled = enabled))
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n(
                    "实验配置：${artemisConfiguration.profile.name} · ${artemisConfiguration.baseUrl}",
                    "Experimental profile: ${artemisConfiguration.profile.name} · ${artemisConfiguration.baseUrl}"
                ),
                modifier = Modifier.weight(1f),
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SettingActionButton(
                text = if (artemisConfiguration.profile == ArtemisProfile.FLASH) "Flash" else "Pro",
                icon = CompatIconVectors.Edit,
                onClick = {
                    artemisPreferences.setConfiguration(
                        artemisConfiguration.copy(
                            profile = if (artemisConfiguration.profile == ArtemisProfile.FLASH) {
                                ArtemisProfile.PRO
                            } else {
                                ArtemisProfile.FLASH
                            }
                        )
                    )
                }
            )
        }

        statusMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                color = if (statusIsError) SettingColors.Danger else SettingColors.Success,
                fontSize = 11.5.sp
            )
        }
    }

    if (showModelDialog) {
        AiModelConfigDialog(
            initialConfig = savedConfig,
            hasSavedKey = hasSavedKey,
            onSaved = {
                statusIsError = false
                statusMessage = configSavedText
                showModelDialog = false
            },
            onKeyCleared = {
                statusIsError = false
                statusMessage = keyClearedText
            },
            onDismiss = { showModelDialog = false }
        )
    }
}

@Composable
internal fun AiModelConfigDialog(
    initialConfig: AiModelConfig,
    hasSavedKey: Boolean,
    onSaved: () -> Unit,
    onKeyCleared: () -> Unit,
    onDismiss: () -> Unit
) {
    val repository = remember { AiConfiguration.repository }
    val providerRepository = remember { AgentProviderRuntime.repository }
    val capabilityProbe = remember { AgentCapabilityProbe() }
    val coroutineScope = rememberCoroutineScope()
    var baseUrl by remember(initialConfig.baseUrl) { mutableStateOf(initialConfig.baseUrl) }
    var model by remember(initialConfig.model) { mutableStateOf(initialConfig.model) }
    var apiKey by remember { mutableStateOf("") }
    val visionMode = VisionMode.ENABLED
    var keyAvailable by remember(hasSavedKey) { mutableStateOf(hasSavedKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }
    var capabilityReport by remember { mutableStateOf<AgentCapabilityReport?>(null) }
    var modelOptions by remember { mutableStateOf<List<String>?>(null) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var modelFetchError by remember { mutableStateOf<String?>(null) }
    val testSuccessText = stringResource(Res.string.ai_test_success)
    val keyClearedText = stringResource(Res.string.ai_key_cleared)
    val isBusy = isSaving || isTesting

    fun currentConfig() = AiModelConfig(
        baseUrl = baseUrl,
        model = model,
        visionMode = visionMode,
        contextWindowTokens = initialConfig.contextWindowTokens
    )

    fun invalidateCapabilityResult() {
        capabilityReport = null
        statusMessage = null
    }

    fun fetchModels() {
        coroutineScope.launch {
            isLoadingModels = true
            modelFetchError = null
            val result = runCatching {
                val key = apiKey.trim().ifBlank { repository.loadApiKey().orEmpty() }
                require(key.isNotBlank()) {
                    l10n("请先填写 API Key 再获取模型列表", "Enter an API key before fetching models")
                }
                AgentModelCatalog.fetchModelIds(providerRepository.legacyPreview(currentConfig(), key))
            }
            result.fold(
                onSuccess = { ids ->
                    modelOptions = ids
                    modelFetchError = ids.takeIf { it.isEmpty() }?.let {
                        l10n("接口未返回任何模型", "The endpoint returned no models")
                    }
                },
                onFailure = { error ->
                    modelOptions = null
                    modelFetchError = error.message ?: l10n("获取模型列表失败", "Failed to fetch model list")
                }
            )
            isLoadingModels = false
        }
    }

    fun applyPreset(preset: AgentProviderPreset) {
        baseUrl = preset.baseUrl
        if (model.isBlank()) model = preset.suggestedModel
        modelOptions = null
        modelFetchError = null
        invalidateCapabilityResult()
    }

    Dialog(onDismissRequest = { if (!isBusy) onDismiss() }) {
        Surface(
            modifier = Modifier.width(540.dp),
            shape = RoundedCornerShape(14.dp),
            color = SettingColors.Surface,
            border = BorderStroke(1.dp, SettingColors.Border),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            if (hasSavedKey || initialConfig.model.isNotBlank()) {
                                Res.string.ai_edit_model
                            } else {
                                Res.string.ai_add_model
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = SettingColors.Text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SettingColors.SoftSurface
                    ) {
                        Text(
                            text = stringResource(Res.string.ai_openai_compatible_badge),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            color = SettingColors.SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(enabled = !isBusy, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconParkIcons.Close,
                            contentDescription = stringResource(Res.string.cancel),
                            modifier = Modifier.size(15.dp),
                            tint = SettingColors.Muted
                        )
                    }
                }

                HorizontalDivider(color = SettingColors.Divider.copy(alpha = 0.6f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = l10n("服务商预设", "Provider presets"),
                            color = SettingColors.SecondaryText,
                            fontSize = 11.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AGENT_PROVIDER_PRESETS.take(3).forEach { preset ->
                                AiProviderPresetChip(preset.label) { applyPreset(preset) }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AGENT_PROVIDER_PRESETS.drop(3).forEach { preset ->
                                AiProviderPresetChip(preset.label) { applyPreset(preset) }
                            }
                        }
                    }

                    AiDialogTextField(
                        label = stringResource(Res.string.ai_base_url),
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            invalidateCapabilityResult()
                        },
                        placeholder = stringResource(Res.string.ai_base_url_hint)
                    )

                    AiDialogTextField(
                        label = stringResource(Res.string.ai_api_key),
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            invalidateCapabilityResult()
                        },
                        placeholder = stringResource(
                            if (keyAvailable) {
                                Res.string.ai_api_key_hint
                            } else {
                                Res.string.ai_api_key_required_hint
                            }
                        ),
                        visualTransformation = if (showApiKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable { showApiKey = !showApiKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (showApiKey) {
                                        CompatIconVectors.EyeOff
                                    } else {
                                        CompatIconVectors.Eye
                                    },
                                    contentDescription = stringResource(
                                        if (showApiKey) Res.string.ai_hide_key else Res.string.ai_show_key
                                    ),
                                    modifier = Modifier.size(15.dp),
                                    tint = SettingColors.Muted
                                )
                            }
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.ai_model_name),
                                color = SettingColors.Text,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(enabled = !isBusy) {
                                        if (modelOptions != null) {
                                            modelOptions = null
                                        } else {
                                            fetchModels()
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (modelOptions == null) {
                                        l10n("获取模型列表", "Fetch model list")
                                    } else {
                                        l10n("收起模型列表", "Hide model list")
                                    },
                                    color = SettingColors.Primary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        AiDialogTextField(
                            label = "",
                            value = model,
                            onValueChange = {
                                model = it
                                invalidateCapabilityResult()
                            },
                            placeholder = stringResource(Res.string.ai_model_name_hint)
                        )

                        if (modelOptions != null || isLoadingModels) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = SettingColors.SoftSurface,
                                border = BorderStroke(1.dp, SettingColors.Border),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                when {
                                    isLoadingModels -> Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = SettingColors.Primary
                                        )
                                        Text(
                                            text = l10n("获取中…", "Loading…"),
                                            color = SettingColors.SecondaryText,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                    modelFetchError != null -> Text(
                                        text = modelFetchError.orEmpty(),
                                        modifier = Modifier.padding(12.dp),
                                        color = SettingColors.Danger,
                                        fontSize = 11.5.sp
                                    )
                                    else -> Column(
                                        modifier = Modifier
                                            .heightIn(max = 150.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        modelOptions.orEmpty().forEach { id ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        model = id
                                                        invalidateCapabilityResult()
                                                        modelOptions = null
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = id,
                                                    color = SettingColors.Text,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (id == model.trim()) {
                                                    Icon(
                                                        imageVector = IconParkIcons.CheckCircle,
                                                        contentDescription = null,
                                                        tint = SettingColors.Primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = l10n("视觉能力", "Vision capability"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SettingColors.Text,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = l10n(
                                    "Agent 每一步都需要最新截图，不能切换为纯文本模式。",
                                    "Every Agent step requires a fresh screenshot; text-only mode is unavailable."
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = SettingColors.SecondaryText,
                                fontSize = 11.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SettingColors.Primary.copy(alpha = 0.1f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = l10n("强制开启", "Required"),
                                color = SettingColors.Primary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = SettingColors.SoftSurface,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            text = stringResource(Res.string.ai_privacy_notice),
                            modifier = Modifier.padding(10.dp),
                            color = SettingColors.SecondaryText,
                            fontSize = 11.5.sp
                        )
                    }

                    statusMessage?.let { message ->
                        Text(
                            text = message,
                            color = if (statusIsError) SettingColors.Danger else SettingColors.Success,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = SettingColors.Divider.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (keyAvailable) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = repository.clearApiKey()
                                    statusIsError = result.isFailure
                                    statusMessage = result.exceptionOrNull()?.message ?: keyClearedText
                                    if (result.isSuccess) {
                                        apiKey = ""
                                        keyAvailable = false
                                        onKeyCleared()
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            enabled = !isBusy,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SettingColors.Border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SettingColors.Surface,
                                contentColor = SettingColors.Danger
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.ai_clear_key),
                                color = SettingColors.Danger,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isTesting = true
                                val key = apiKey.trim().ifBlank { repository.loadApiKey().orEmpty() }
                                val result = runCatching {
                                    require(key.isNotBlank()) { "API Key is required" }
                                    val provider = providerRepository.legacyPreview(currentConfig(), key)
                                    capabilityProbe.probe(provider)
                                }
                                capabilityReport = result.getOrNull()
                                statusIsError = result.isFailure ||
                                    result.getOrNull()?.tier?.let { it < AgentCapabilityTier.L3_VISUAL_AGENT } == true
                                statusMessage = result.exceptionOrNull()?.message ?: result.getOrNull()?.let { report ->
                                    if (report.tier >= AgentCapabilityTier.L3_VISUAL_AGENT) {
                                        "$testSuccessText · ${l10n("能力等级", "Capability tier")} ${report.tier.name}"
                                    } else {
                                        l10n(
                                            "连接成功，但模型未通过视觉与工具调用能力测试（需要 L3，当前 ${report.tier.name}）。",
                                            "Connection succeeded, but the model did not pass the visual tool-use test (L3 required, current ${report.tier.name})."
                                        )
                                    }
                                } ?: testSuccessText
                                isTesting = false
                            }
                        },
                        modifier = Modifier
                            .height(32.dp)
                            .pointerHoverIcon(if (!isBusy && (apiKey.isNotBlank() || keyAvailable)) PointerIcon.Hand else PointerIcon.Default),
                        enabled = !isBusy && (apiKey.isNotBlank() || keyAvailable),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, SettingColors.Border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SettingColors.Surface,
                            contentColor = SettingColors.Text
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = SettingColors.Primary
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = stringResource(Res.string.ai_test_connection),
                            color = if (isBusy && !isTesting) SettingColors.Muted else SettingColors.Text,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(32.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        enabled = !isBusy,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, SettingColors.Border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SettingColors.Surface,
                            contentColor = SettingColors.Text
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cancel),
                            color = SettingColors.Text,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                val result = repository.save(currentConfig(), apiKey)
                                statusIsError = result.isFailure
                                statusMessage = result.exceptionOrNull()?.message
                                if (result.isSuccess) {
                                    providerRepository.syncLegacy(currentConfig(), apiKey)
                                    // Declaring vision support is not compatibility evidence.
                                    // Keep a still-valid attestation for unchanged settings, but
                                    // only create a new one from an actual capability probe.
                                    capabilityReport?.let { report ->
                                        providerRepository.resolve(AgentModelRole.BRAIN)
                                            ?.let { provider ->
                                                providerRepository.attestCapabilities(provider.profile, report.tier)
                                            }
                                    }
                                    onSaved()
                                }
                                isSaving = false
                            }
                        },
                        modifier = Modifier
                            .height(32.dp)
                            .pointerHoverIcon(if (!isBusy) PointerIcon.Hand else PointerIcon.Default),
                        enabled = !isBusy,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SettingColors.Primary,
                            contentColor = QadbColors.onPrimary,
                            disabledContainerColor = SettingColors.ControlBackground,
                            disabledContentColor = SettingColors.Muted
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = QadbColors.onPrimary
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = stringResource(Res.string.ai_save_config),
                            color = if (isBusy && !isSaving) SettingColors.Muted else QadbColors.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiDialogTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = SettingColors.Text,
                fontWeight = FontWeight.Medium,
                fontSize = 12.5.sp
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = SettingColors.Muted,
                    fontSize = 12.sp
                )
            },
            trailingIcon = trailingContent,
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(6.dp)
        )
    }
}

private data class AgentProviderPreset(
    val label: String,
    val baseUrl: String,
    val suggestedModel: String
)

private val AGENT_PROVIDER_PRESETS = listOf(
    AgentProviderPreset("OpenAI", "https://api.openai.com/v1", "gpt-4o"),
    AgentProviderPreset("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
    AgentProviderPreset("智谱 GLM", "https://open.bigmodel.cn/api/paas/v4", "glm-4v-plus"),
    AgentProviderPreset("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-vl-max"),
    AgentProviderPreset("Kimi", "https://api.moonshot.cn/v1", "moonshot-v1-8k-vision-preview"),
    AgentProviderPreset("OpenRouter", "https://openrouter.ai/api/v1", "openai/gpt-4o")
)

@Composable
private fun AiProviderPresetChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = SettingColors.SoftSurface,
        border = BorderStroke(1.dp, SettingColors.Border)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            color = SettingColors.SecondaryText,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = SettingColors.Divider.copy(alpha = 0.45f)
    )
}

@Composable
private fun StatusPill(statusText: String, tone: StatusTone) {
    val (color, bgColor) = when (tone) {
        StatusTone.Positive -> Pair(SettingColors.Success, SettingColors.Success.copy(alpha = 0.12f))
        StatusTone.Neutral -> Pair(SettingColors.SecondaryText, SettingColors.SoftSurface)
        StatusTone.Danger -> Pair(SettingColors.Danger, SettingColors.Danger.copy(alpha = 0.12f))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsDropdownRow(
    title: String,
    description: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = SettingColors.Text,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Box {
            val triggerBorder by animateColorAsState(
                targetValue = if (isHovered || expanded) SettingColors.Primary.copy(alpha = 0.5f) else SettingColors.Border,
                animationSpec = tween(UiTokens.HoverDurationMillis)
            )
            val triggerBg by animateColorAsState(
                targetValue = if (isHovered || expanded) SettingColors.SoftSurface else SettingColors.Surface,
                animationSpec = tween(UiTokens.HoverDurationMillis)
            )

            Row(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(triggerBg)
                    .border(1.dp, triggerBorder, RoundedCornerShape(6.dp))
                    .hoverable(interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onExpandedChange(!expanded) }
                    )
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SettingColors.Text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = IconParkIcons.ArrowDown,
                    contentDescription = null,
                    tint = if (isHovered || expanded) SettingColors.Primary else SettingColors.Muted,
                    modifier = Modifier.size(12.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(SettingColors.Surface),
                content = menuContent
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = SettingColors.Text,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SettingColors.SecondaryText,
                fontSize = 11.5.sp
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = { value -> if (enabled) onCheckedChange(value) },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        )
    }
}

@Composable
private fun visionModeText(mode: VisionMode): String = stringResource(
    when (mode) {
        VisionMode.AUTO -> Res.string.ai_vision_auto
        VisionMode.ENABLED -> Res.string.ai_vision_enabled
        VisionMode.DISABLED -> Res.string.ai_vision_disabled
    }
)

@Composable
private fun settingAdbSourceText(source: AdbPathManager.AdbSource): String {
    return when (source) {
        AdbPathManager.AdbSource.SYSTEM -> stringResource(Res.string.adb_source_system)
        AdbPathManager.AdbSource.BUNDLED -> stringResource(Res.string.adb_source_bundled)
        AdbPathManager.AdbSource.CUSTOM -> stringResource(Res.string.adb_source_custom)
        AdbPathManager.AdbSource.NONE -> stringResource(Res.string.adb_source_none)
    }
}

private fun openPathLocation(path: String?) {
    if (path.isNullOrBlank()) return
    runCatching {
        if (!Desktop.isDesktopSupported()) return
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.OPEN)) return
        val file = File(path)
        val target = if (file.isDirectory) file else file.parentFile
        if (target != null && target.exists()) {
            desktop.open(target)
        }
    }
}

private enum class StatusTone {
    Positive,
    Neutral,
    Danger
}

private sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object Downloading : UpdateStatus
    data class UpToDate(val latestVersion: String) : UpdateStatus
    data class UpdateAvailable(
        val latestVersion: String,
        val hasAutoInstall: Boolean
    ) : UpdateStatus
    data class DownloadSuccess(val path: String) : UpdateStatus
    data class CheckFailed(val reason: String) : UpdateStatus
    data class DownloadFailed(val reason: String) : UpdateStatus
}
