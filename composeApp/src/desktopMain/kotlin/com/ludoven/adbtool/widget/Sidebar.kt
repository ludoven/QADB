package com.ludoven.adbtool.widget

import com.ludoven.adbtool.ui.icons.AiAgentBrandIcon

import com.ludoven.adbtool.ui.mac.*

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.connected
import adbtool_desktop.composeapp.generated.resources.disconnected
import adbtool_desktop.composeapp.generated.resources.no_device
import adbtool_desktop.composeapp.generated.resources.ic_logo_no_background
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.ui.mac.DropdownMenu
import com.ludoven.adbtool.ui.mac.DropdownMenuItem
import com.ludoven.adbtool.ui.mac.Icon
import com.ludoven.adbtool.ui.mac.MaterialTheme
import com.ludoven.adbtool.ui.mac.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ludoven.adbtool.TabItem
import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.util.l10n
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun Sidebar(
    groups: List<SidebarGroup>,
    selectedRoute: String,
    connectedDeviceCount: Int,
    devices: List<String>,
    selectedDevice: String?,
    deviceDisplayNames: Map<String, String>,
    onItemClick: (String) -> Unit,
    onDeviceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedGroupId by remember { mutableStateOf<String?>(null) }
    val visibleGroupIds = visibleSidebarGroupIds(groups, selectedRoute, expandedGroupId)
    val sidebarBackground = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFF9F9FA)
    }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(UiTokens.SidebarWidth),
        shape = RoundedCornerShape(0.dp),
        color = sidebarBackground,
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp, top = 28.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .width(150.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo_no_background),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "QADB",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            groups.forEach { group ->
                val isExpanded = group.collapsible && group.id in visibleGroupIds
                SidebarGroupHeader(
                    group = group,
                    isExpanded = isExpanded,
                    hasSelection = group.items.any { it.route == selectedRoute },
                    onClick = {
                        if (group.collapsible) {
                            expandedGroupId = when {
                                expandedGroupId == group.id -> null
                                else -> group.id
                            }
                        } else {
                            onItemClick(group.defaultRoute)
                        }
                    }
                )
                if (isExpanded) {
                    group.items.forEach { item ->
                        SidebarItem(
                            item = item,
                            isSelected = selectedRoute == item.route,
                            onClick = { onItemClick(item.route) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ConnectedStatusCard(
                connectedDeviceCount = connectedDeviceCount,
                devices = devices,
                selectedDevice = selectedDevice,
                deviceDisplayNames = deviceDisplayNames,
                onDeviceSelected = onDeviceSelected
            )
        }
    }
}

@Composable
private fun SidebarGroupHeader(
    group: SidebarGroup,
    isExpanded: Boolean,
    hasSelection: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(UiTokens.RadiusLarge)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundColor = when {
        hasSelection -> if (isDark) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
        } else {
            Color(0xFFEAF3FF)
        }
        isHovered -> if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
        } else {
            Color(0xFFF5F7FA)
        }
        else -> Color.Transparent
    }
    val contentColor = if (hasSelection) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isFocused && !hasSelection) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
                shape = shape
            )
            .semantics {
                role = Role.Button
                selected = hasSelection
                if (group.collapsible) stateDescription = if (isExpanded) "Expanded" else "Collapsed"
            }
            .onFocusChanged { isFocused = it.isFocused }
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (group.id == "ai") {
            AiAgentBrandIcon(
                contentDescription = group.label,
                modifier = Modifier.size(UiTokens.IconLarge)
            )
        } else {
            Icon(
                imageVector = group.icon,
                contentDescription = group.label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(UiTokens.SpaceLarge))
        Text(
            text = group.label,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) IconParkIcons.ArrowDown else IconParkIcons.Right,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (group.collapsible) 1f else 0f),
            modifier = Modifier.size(UiTokens.IconMedium)
        )
    }
}

@Composable
private fun SidebarItem(
    item: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(UiTokens.RadiusLarge)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundColor = when {
        isSelected -> if (isDark) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
        } else {
            Color(0xFFEAF3FF)
        }
        isHovered -> if (isDark) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
        } else {
            Color(0xFFF5F7FA)
        }
        else -> Color.Transparent
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(UiTokens.ToolbarHeight)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isFocused && !isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
                shape = shape
            )
            .semantics {
                role = Role.Button
                selected = isSelected
            }
            .onFocusChanged { isFocused = it.isFocused }
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .focusable(interactionSource = interactionSource)
            .padding(start = UiTokens.SpaceXLarge, end = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(UiTokens.SpaceSmall))
        } else {
            Spacer(modifier = Modifier.width(UiTokens.SpaceMedium))
        }

        if (item.route == "ai") {
            AiAgentBrandIcon(
                contentDescription = item.title,
                modifier = Modifier.size(UiTokens.IconMedium)
            )
        } else {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(UiTokens.IconMedium)
            )
        }

        Spacer(modifier = Modifier.width(UiTokens.SpaceSmall))

        Text(
            text = item.title,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConnectedStatusCard(
    connectedDeviceCount: Int,
    devices: List<String>,
    selectedDevice: String?,
    deviceDisplayNames: Map<String, String>,
    onDeviceSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(UiTokens.RadiusLarge)
    val isConnected = !selectedDevice.isNullOrBlank() && connectedDeviceCount > 0
    val statusColor = if (isConnected) QadbColors.success else MaterialTheme.colorScheme.onSurfaceVariant
    val noDeviceText = stringResource(Res.string.no_device)
    val selectedModel = selectedDevice
        ?.let { deviceDisplayNames[it]?.trim().orEmpty() }
        .orEmpty()
    val selectedTitle = selectedModel
        .takeIf { it.isNotBlank() }
        ?: selectedDevice
        ?: noDeviceText
    val selectedAddress = selectedDevice
        ?.let { formatSidebarDeviceAddress(it) }
        .orEmpty()
    val statusText = stringResource(if (isConnected) Res.string.connected else Res.string.disconnected)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = if (isFocused) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = shape
                )
                .semantics {
                    role = Role.Button
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                }
                .onFocusChanged { isFocused = it.isFocused }
                .clickable(
                    enabled = devices.isNotEmpty(),
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button
                ) { expanded = !expanded }
                .focusable(enabled = devices.isNotEmpty(), interactionSource = interactionSource)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(UiTokens.SpaceSmall))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
                Text(
                    text = if (isConnected) l10n("已连接设备", "Connected device") else statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isConnected && selectedAddress.isNotBlank()) {
                    Text(
                        text = selectedAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = IconParkIcons.Right,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(UiTokens.IconSmall)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            devices.forEach { deviceId ->
                val displayName = formatSidebarDeviceDisplay(deviceId, deviceDisplayNames[deviceId])
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayName,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        onDeviceSelected(deviceId)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatSidebarDeviceDisplay(deviceId: String, model: String?): String {
    val cleanModel = model?.trim().orEmpty()
    return if (cleanModel.isNotBlank()) "$cleanModel ($deviceId)" else deviceId
}

private fun formatSidebarDeviceAddress(deviceId: String): String =
    deviceId.substringBefore(":").takeIf { it.isNotBlank() } ?: deviceId
