package com.ludoven.adbtool.pages

import adbtool_desktop.composeapp.generated.resources.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.QadbTokens
import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.entity.AppInfoData
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.ui.mac.*
import com.ludoven.adbtool.util.copyPlainTextToClipboard
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.AppViewModel
import com.ludoven.adbtool.widget.FeedbackToast
import com.ludoven.adbtool.widget.FramedStateSurface
import com.ludoven.adbtool.widget.DeviceRequiredState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

private fun AppInfoData.toAppInfo(): AppInfo = AppInfo(
    appName = appName,
    packageName = packageName,
    apkPath = apkPath,
    isSystemApp = isSystemApp,
    isRunning = isRunning
)

private object AppVisualTokens {
    val Primary: Color @Composable get() = QadbTokens.brand
    val BrandAction: Color @Composable get() = QadbTokens.brandAction
    val BrandSoft: Color @Composable get() = QadbTokens.brandSoft
    val Text: Color @Composable get() = QadbTokens.textPrimary
    val Muted: Color @Composable get() = QadbTokens.textSecondary
    val Tertiary: Color @Composable get() = QadbTokens.textTertiary
    val Border: Color @Composable get() = QadbTokens.border
    val BorderStrong: Color @Composable get() = QadbTokens.borderStrong
    val Divider: Color @Composable get() = QadbTokens.divider
    val Surface: Color @Composable get() = QadbTokens.bg1
    val Soft: Color @Composable get() = QadbTokens.bg2
    val Inset: Color @Composable get() = QadbTokens.bg3
    val Selected: Color @Composable get() = QadbTokens.brandSoft
    val Success: Color @Composable get() = QadbTokens.success
    val Warning: Color @Composable get() = QadbTokens.warning
    val Danger: Color @Composable get() = QadbTokens.danger
}

internal fun appListEmptyReason(
    hasSelectedDevice: Boolean,
    displayedListIsEmpty: Boolean,
    isLoading: Boolean
): AppListEmptyReason? {
    if (isLoading || !displayedListIsEmpty) return null
    return if (hasSelectedDevice) AppListEmptyReason.NO_RESULTS else AppListEmptyReason.NO_DEVICE
}

internal fun filterApps(
    apps: List<AppInfo>,
    selectedFilter: AppFilter,
    searchText: String
): List<AppInfo> {
    val query = searchText.trim()
    return apps.filter { app ->
        val matchesSearch = query.isBlank() ||
            app.appName.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        matchesSearch && selectedFilter.matches(app)
    }
}

internal fun appFilterCounts(apps: List<AppInfo>): Map<AppFilter, Int> {
    return AppFilter.entries.associateWith { filter ->
        apps.count { app -> filter.matches(app) }
    }
}

@Composable
private fun AppAvatar(app: AppInfo, icon: ImageBitmap?, size: Int = 36) {
    val radius = (size * 0.18f).coerceIn(4f, 10f).dp
    val shape = RoundedCornerShape(radius)

    if (icon != null) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = icon,
                contentDescription = app.appName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        val letter = app.appName.trimStart().take(1).uppercase().ifBlank { "?" }
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(shape)
                .background(AppVisualTokens.BrandSoft)
                .border(1.dp, AppVisualTokens.Border.copy(alpha = 0.6f), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = AppVisualTokens.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.38f).sp
            )
        }
    }
}

@Composable
private fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(AppVisualTokens.Soft.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (isFocused) AppVisualTokens.Primary.copy(alpha = 0.65f) else AppVisualTokens.Border,
                shape = shape
            )
            .padding(horizontal = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = IconParkIcons.Search,
            contentDescription = null,
            tint = if (isFocused) AppVisualTokens.Primary else AppVisualTokens.Muted,
            modifier = Modifier.size(14.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = AppVisualTokens.Text),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(AppVisualTokens.Primary),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = l10n("搜索应用名称或包名", "Search app name or package"),
                        color = AppVisualTokens.Tertiary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        )
        if (value.isNotBlank()) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(AppVisualTokens.Muted.copy(alpha = 0.2f))
                    .clickable { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = l10n("清除", "Clear"),
                    tint = AppVisualTokens.Text,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRefreshIconButton(onClick: () -> Unit, enabled: Boolean) {
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    TooltipArea(
        tooltip = {
            Surface(
                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                color = MaterialTheme.colorScheme.inverseSurface
            ) {
                Text(
                    text = l10n("刷新应用列表", "Refresh app list"),
                    modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        delayMillis = 350,
        tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(0.dp, 10.dp))
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(shape)
                .background(if (enabled) AppVisualTokens.Soft.copy(alpha = 0.5f) else Color.Transparent)
                .border(
                    1.dp,
                    if (enabled) AppVisualTokens.Border else AppVisualTokens.Border.copy(alpha = 0.4f),
                    shape
                )
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                IconParkIcons.Refresh,
                contentDescription = l10n("刷新", "Refresh"),
                modifier = Modifier.size(UiTokens.IconSmall),
                tint = if (enabled) AppVisualTokens.Text else AppVisualTokens.Muted.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun AppSortDropdownButton(
    sortMode: AppSortMode,
    onSortModeChange: (AppSortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)

    Box {
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(shape)
                .background(AppVisualTokens.Soft.copy(alpha = 0.5f))
                .border(1.dp, AppVisualTokens.Border, shape)
                .clickable { expanded = true }
                .padding(horizontal = UiTokens.SpaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = AppVisualTokens.Muted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = appSortModeLabel(sortMode),
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                fontWeight = FontWeight.Medium
            )
            Icon(
                IconParkIcons.ArrowDown,
                contentDescription = null,
                tint = AppVisualTokens.Muted,
                modifier = Modifier.size(12.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(AppVisualTokens.Surface, RoundedCornerShape(UiTokens.RadiusMedium))
                .border(1.dp, AppVisualTokens.Border, RoundedCornerShape(UiTokens.RadiusMedium))
        ) {
            AppSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = appSortModeLabel(mode),
                            color = if (mode == sortMode) AppVisualTokens.Primary else AppVisualTokens.Text,
                            fontWeight = if (mode == sortMode) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSortModeChange(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ViewModeSegmentedControl(
    isGridView: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .background(AppVisualTokens.Soft.copy(alpha = 0.5f))
            .border(1.dp, AppVisualTokens.Border, shape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegmentedIconButton(
            icon = Icons.AutoMirrored.Filled.ViewList,
            selected = !isGridView,
            tooltip = l10n("列表视图", "List view"),
            onClick = { onModeChange(false) }
        )
        SegmentedIconButton(
            icon = Icons.Default.GridView,
            selected = isGridView,
            tooltip = l10n("网格视图", "Grid view"),
            onClick = { onModeChange(true) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SegmentedIconButton(
    icon: ImageVector,
    selected: Boolean,
    tooltip: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(UiTokens.RadiusSmall)
    TooltipArea(
        tooltip = {
            Surface(
                shape = RoundedCornerShape(UiTokens.RadiusSmall),
                color = MaterialTheme.colorScheme.inverseSurface
            ) {
                Text(
                    text = tooltip,
                    modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        delayMillis = 400
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(shape)
                .background(if (selected) AppVisualTokens.Surface else Color.Transparent)
                .border(
                    1.dp,
                    if (selected) AppVisualTokens.Border else Color.Transparent,
                    shape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = if (selected) AppVisualTokens.Primary else AppVisualTokens.Muted,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun AppFilterTabsRow(
    tabs: List<AppFilter>,
    selectedFilter: AppFilter,
    tabCountMap: Map<AppFilter, Int>,
    onSelectFilter: (AppFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tabs, key = { it.key }) { tab ->
            val isSelected = selectedFilter == tab
            val shape = RoundedCornerShape(UiTokens.RadiusMedium)
            val count = tabCountMap[tab] ?: 0

            Row(
                modifier = Modifier
                    .height(32.dp)
                    .clip(shape)
                    .background(
                        if (isSelected) AppVisualTokens.BrandSoft
                        else AppVisualTokens.Soft.copy(alpha = 0.35f)
                    )
                    .border(
                        1.dp,
                        if (isSelected) AppVisualTokens.Primary.copy(alpha = 0.45f)
                        else AppVisualTokens.Border.copy(alpha = 0.65f),
                        shape
                    )
                    .clickable { onSelectFilter(tab) }
                    .padding(horizontal = UiTokens.SpaceMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = appTabLabel(tab),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.5.sp,
                    color = if (isSelected) AppVisualTokens.Primary else AppVisualTokens.Text,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                        .background(
                            if (isSelected) AppVisualTokens.Primary.copy(alpha = 0.15f)
                            else AppVisualTokens.Muted.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (isSelected) AppVisualTokens.Primary else AppVisualTokens.Muted,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun AppScreen(
    viewModel: AppViewModel,
    selectedDevice: String?,
    onRefreshDevices: () -> Unit,
    onOpenWirelessConnection: () -> Unit,
    onOpenTroubleshooting: () -> Unit
) {
    val appList by viewModel.appList.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appIcons by viewModel.appIcons.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val toastMessage by viewModel.feedbackToastMessage.collectAsState()
    val appInfo by viewModel.appInfo.collectAsState()

    FeedbackToast(toastMessage)

    var pendingDangerAction by remember { mutableStateOf<Pair<AdbFunctionType, AppInfo>?>(null) }
    var confirmActionLabel by remember { mutableStateOf("") }
    var confirmActionMessage by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(AppSortMode.Name) }

    val hasSelectedDevice = !selectedDevice.isNullOrBlank()

    LaunchedEffect(selectedDevice) { viewModel.getAppList(selectedDevice) }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelPageLoads()
        }
    }

    val tabs = remember { AppFilter.entries.toList() }
    val selectedFilter = remember(selectedTab) { AppFilter.fromKey(selectedTab) }
    LaunchedEffect(selectedFilter, selectedDevice) {
        if (selectedFilter == AppFilter.RUNNING && hasSelectedDevice) {
            viewModel.refreshRunningStatusAsync()
        }
    }
    val filteredList = remember(appList, selectedFilter, searchText) {
        filterApps(appList, selectedFilter, searchText)
    }
    val displayedList = remember(filteredList, sortMode) {
        when (sortMode) {
            AppSortMode.Name -> filteredList.sortedBy { it.appName.lowercase() }
            AppSortMode.Size -> filteredList.sortedByDescending { it.sizeBytes ?: -1L }
            AppSortMode.Version -> filteredList.sortedByDescending { it.versionName }
            AppSortMode.InstallTime -> filteredList.sortedByDescending { it.installTimestamp ?: 0L }
            AppSortMode.Recent -> filteredList.sortedByDescending { it.lastUsedTimestamp ?: it.installTimestamp ?: 0L }
        }
    }
    val tabCountMap = remember(appList) {
        appFilterCounts(appList)
    }
    val isInstalling by viewModel.isInstalling.collectAsState()
    val installProgress by viewModel.currentInstallingProgress.collectAsState()

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        if (appInfo != null) {
            AppDetailPage(
                appInfo = appInfo!!,
                icon = appIcons[appInfo!!.packageName],
                onBack = { viewModel.clearAppInfo() },
                onAction = { type -> viewModel.executeAdbAction(type, appInfo!!.packageName) },
                onCopy = { text -> viewModel.copyToClipboard(text) },
                onRequestDangerAction = { type, label, message ->
                    pendingDangerAction = type to (appInfo!!.toAppInfo())
                    confirmActionLabel = label
                    confirmActionMessage = message
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(UiTokens.PagePaddingCompact)
            )
            if (showDialog) {
                dialogMessage?.let {
                    TipDialog(
                        dialogText = when (it) {
                            is MsgContent.Resource -> stringResource(it.stringResource, *it.args.toTypedArray())
                            is MsgContent.Text -> it.text
                        }
                    ) { viewModel.dismissTipDialog() }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UiTokens.PagePadding, vertical = UiTokens.SectionSpacingCompact),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            // ── Header Toolbar ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = UiTokens.SpaceMedium),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
                ) {
                    Text(
                        text = l10n("应用列表", "Application List"),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = UiTokens.TextSection,
                        fontWeight = FontWeight.SemiBold,
                        color = AppVisualTokens.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = l10n("查看、搜索与管理当前连接设备上的应用程序", "View, search, and manage applications on the current device"),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppVisualTokens.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (hasSelectedDevice) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppSearchField(
                            value = searchText,
                            onValueChange = { viewModel.setSearchText(it) },
                            modifier = Modifier.width(220.dp)
                        )

                        AppRefreshIconButton(
                            onClick = { viewModel.getAppList(selectedDevice, forceRefresh = true) },
                            enabled = !isLoading
                        )

                        AppSortDropdownButton(
                            sortMode = sortMode,
                            onSortModeChange = { sortMode = it }
                        )

                        ViewModeSegmentedControl(
                            isGridView = isGridView,
                            onModeChange = { viewModel.setGridView(it) }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.batchInstallFromFolder(selectedDevice)
                        },
                        enabled = hasSelectedDevice && !isInstalling,
                        shape = RoundedCornerShape(UiTokens.RadiusMedium),
                        border = BorderStroke(1.dp, AppVisualTokens.BorderStrong),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AppVisualTokens.Surface,
                            contentColor = AppVisualTokens.Text,
                            disabledContentColor = AppVisualTokens.Muted.copy(alpha = 0.45f)
                        ),
                        contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(UiTokens.IconSmall))
                        Spacer(Modifier.width(UiTokens.SpaceSmall))
                        Text(
                            text = if (isInstalling) {
                                val progress = installProgress?.let { " ($it)" } ?: ""
                                l10n("安装中", "Installing") + progress
                            } else {
                                l10n("批量安装应用", "Batch install")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Filter Tabs Row ─────────────────────────────────────────
            if (hasSelectedDevice && appList.isNotEmpty()) {
                AppFilterTabsRow(
                    tabs = tabs,
                    selectedFilter = selectedFilter,
                    tabCountMap = tabCountMap,
                    onSelectFilter = { viewModel.setSelectedTab(it.key) }
                )
            }

            // ── Main Content Area ───────────────────────────────────────
            FramedStateSurface(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                val emptyReason = appListEmptyReason(hasSelectedDevice, displayedList.isEmpty(), isLoading)

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = l10n("正在读取应用列表...", "Loading installed applications..."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppVisualTokens.Muted
                                )
                            }
                        }
                    }

                    emptyReason != null -> {
                        AppEmptyState(
                            reason = emptyReason,
                            onRefresh = { viewModel.getAppList(selectedDevice, forceRefresh = true) },
                            onRefreshDevices = onRefreshDevices,
                            onOpenWirelessConnection = onOpenWirelessConnection,
                            onOpenTroubleshooting = onOpenTroubleshooting
                        )
                    }

                    isGridView -> {
                        val gridState = rememberLazyGridState()
                        LaunchedEffect(gridState, displayedList) {
                            snapshotFlow {
                                gridState.layoutInfo.visibleItemsInfo
                                    .mapNotNull { item -> displayedList.getOrNull(item.index)?.packageName }
                            }.distinctUntilChanged().collect { visiblePackages ->
                                viewModel.ensureAppAssetsVisible(visiblePackages)
                            }
                        }
                        Column(modifier = Modifier.fillMaxSize().padding(UiTokens.SpaceMedium)) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(164.dp),
                                state = gridState,
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(displayedList, key = { it.packageName }) { app ->
                                    AppGridCard(
                                        app = app,
                                        icon = appIcons[app.packageName],
                                        onAction = { type -> viewModel.executeAdbAction(type, app.packageName) },
                                        onCopyPackageName = { viewModel.copyToClipboard(app.packageName) },
                                        onRequestDangerAction = { type, label, message ->
                                            pendingDangerAction = type to app
                                            confirmActionLabel = label
                                            confirmActionMessage = message
                                        },
                                        onOpen = { viewModel.openAppInfo(app.packageName) }
                                    )
                                }
                            }
                            AppListFooter(displayedList.size)
                        }
                    }

                    else -> {
                        val listState = rememberLazyListState()
                        LaunchedEffect(listState, displayedList) {
                            snapshotFlow {
                                listState.layoutInfo.visibleItemsInfo
                                    .mapNotNull { item -> displayedList.getOrNull(item.index)?.packageName }
                            }.distinctUntilChanged().collect { visiblePackages ->
                                viewModel.ensureAppAssetsVisible(visiblePackages)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall)
                        ) {
                            AppListColumnHeader()
                            HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.6f))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize().padding(end = UiTokens.SpaceSmall)
                                ) {
                                    items(displayedList, key = { it.packageName }) { app ->
                                        AppListRow(
                                            app = app,
                                            icon = appIcons[app.packageName],
                                            onAction = { type -> viewModel.executeAdbAction(type, app.packageName) },
                                            onCopyPackageName = { viewModel.copyToClipboard(app.packageName) },
                                            onRequestDangerAction = { type, label, message ->
                                                pendingDangerAction = type to app
                                                confirmActionLabel = label
                                                confirmActionMessage = message
                                            },
                                            onOpen = { viewModel.openAppInfo(app.packageName) }
                                        )
                                        HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.35f))
                                    }
                                }
                                VerticalScrollbar(
                                    adapter = rememberScrollbarAdapter(listState),
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                                )
                            }
                            AppListFooter(displayedList.size)
                        }
                    }
                }
            }
        }
    }

    pendingDangerAction?.let { (type, app) ->
        AlertDialog(
            onDismissRequest = { pendingDangerAction = null },
            title = { Text(confirmActionLabel) },
            text = { Text(confirmActionMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.executeAdbAction(type, app.packageName)
                        pendingDangerAction = null
                    }
                ) { Text(l10n("确认", "Confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDangerAction = null }) {
                    Text(l10n("取消", "Cancel"))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showDialog) {
        dialogMessage?.let {
            TipDialog(
                dialogText = when (it) {
                    is MsgContent.Resource -> stringResource(it.stringResource, *it.args.toTypedArray())
                    is MsgContent.Text -> it.text
                }
            ) { viewModel.dismissTipDialog() }
        }
    }
}

private fun appSortModeLabel(mode: AppSortMode): String {
    return when (mode) {
        AppSortMode.Name -> l10n("按名称", "By name")
        AppSortMode.Size -> l10n("按大小", "By size")
        AppSortMode.Version -> l10n("按版本", "By version")
        AppSortMode.InstallTime -> l10n("按安装时间", "By install time")
        AppSortMode.Recent -> l10n("按最近使用", "By recent")
    }
}

private fun appTabLabel(filter: AppFilter): String {
    return when (filter) {
        AppFilter.ALL -> l10n("全部应用", "All")
        AppFilter.USER -> l10n("用户应用", "User")
        AppFilter.SYSTEM -> l10n("系统应用", "System")
        AppFilter.DEBUGGABLE -> l10n("可调试", "Debuggable")
        AppFilter.RECENT -> l10n("最近使用", "Recent")
        AppFilter.RUNNING -> l10n("运行中", "Running")
    }
}

@Composable
private fun AppTypeTag(
    text: String,
    accent: Color = AppVisualTokens.Muted
) {
    val isPrimary = accent == AppVisualTokens.Primary
    val isSystem = text.contains("系统") || text.contains("System")
    val bg = if (isPrimary) AppVisualTokens.BrandSoft
             else if (isSystem) AppVisualTokens.Soft.copy(alpha = 0.65f)
             else AppVisualTokens.BrandSoft.copy(alpha = 0.5f)
    val fg = if (isPrimary) AppVisualTokens.Primary
             else if (isSystem) AppVisualTokens.Tertiary
             else AppVisualTokens.BrandAction

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(UiTokens.BadgeRadius))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.25f), RoundedCornerShape(UiTokens.BadgeRadius))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatusPill(isRunning: Boolean, isDisabled: Boolean = false) {
    val text = when {
        isDisabled -> l10n("已禁用", "Disabled")
        isRunning -> l10n("运行中", "Running")
        else -> l10n("未运行", "Stopped")
    }
    val color = when {
        isDisabled -> AppVisualTokens.Danger
        isRunning -> AppVisualTokens.Success
        else -> AppVisualTokens.Tertiary
    }
    val bg = when {
        isDisabled -> AppVisualTokens.Danger.copy(alpha = 0.10f)
        isRunning -> AppVisualTokens.Success.copy(alpha = 0.12f)
        else -> AppVisualTokens.Soft.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(UiTokens.BadgeRadius))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(UiTokens.BadgeRadius))
            .padding(horizontal = 8.dp, vertical = 2.5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AppListColumnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppColumnHeaderText(l10n("应用", "App"), Modifier.weight(2.4f))
        AppColumnHeaderText(l10n("类型", "Type"), Modifier.weight(1.1f))
        AppColumnHeaderText(l10n("版本", "Version"), Modifier.weight(0.85f))
        AppColumnHeaderText(l10n("大小", "Size"), Modifier.weight(0.8f))
        AppColumnHeaderText(l10n("状态", "Status"), Modifier.weight(0.9f))
        AppColumnHeaderText(l10n("操作", "Actions"), Modifier.weight(0.55f))
    }
}

@Composable
private fun AppColumnHeaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = AppVisualTokens.Tertiary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AppListRow(
    app: AppInfo,
    icon: ImageBitmap?,
    onAction: (AdbFunctionType) -> Unit,
    onCopyPackageName: () -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit,
    onOpen: () -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    val isRowHovered by rowInteraction.collectIsHoveredAsState()
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val appName = app.appName.takeIf { it.isNotBlank() && it != app.packageName } ?: app.packageName
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(rowInteraction)
            .clip(shape)
            .background(if (isRowHovered) AppVisualTokens.Soft.copy(alpha = 0.55f) else Color.Transparent)
            .combinedClickable(onClick = {}, onDoubleClick = onOpen)
            .pointerInput(app.packageName) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.button == PointerButton.Secondary && event.type == PointerEventType.Press) {
                            val point = event.changes.firstOrNull()?.position
                            if (point != null) {
                                contextMenuOffset = with(density) { DpOffset(point.x.toDp(), point.y.toDp()) }
                            }
                            contextMenuExpanded = true
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = UiTokens.SpaceMedium, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App & Package
            Row(
                modifier = Modifier.weight(2.4f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                AppAvatar(app = app, icon = icon, size = 34)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppVisualTokens.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontSize = 11.sp,
                        color = AppVisualTokens.Tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Type
            Row(
                modifier = Modifier.weight(1.1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTypeTag(text = if (app.isSystemApp) l10n("系统", "System") else l10n("用户", "User"))
                if (app.isDebuggable) {
                    AppTypeTag(text = l10n("调试", "Debug"), accent = AppVisualTokens.Primary)
                }
            }

            // Version
            Text(
                text = app.versionName.takeIf { it.isNotBlank() && it != "-" } ?: "-",
                modifier = Modifier.weight(0.85f).padding(end = UiTokens.SpaceSmall),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                fontSize = 12.sp,
                color = if (app.versionName.isNotBlank() && app.versionName != "-") AppVisualTokens.Text else AppVisualTokens.Tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Size
            Text(
                text = app.size.takeIf { it.isNotBlank() && it != "-" } ?: "-",
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                fontSize = 12.sp,
                color = if (app.size.isNotBlank() && app.size != "-") AppVisualTokens.Text else AppVisualTokens.Tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Status
            Box(modifier = Modifier.weight(0.9f)) {
                StatusPill(isRunning = app.isRunning, isDisabled = app.isDisabled)
            }

            // Actions
            Box(
                modifier = Modifier.weight(0.55f),
                contentAlignment = Alignment.CenterStart
            ) {
                AppActionMenu(
                    app = app,
                    alpha = if (isRowHovered) 1f else 0.6f,
                    onAction = onAction,
                    onCopyPackageName = onCopyPackageName,
                    onRequestDangerAction = onRequestDangerAction
                )
            }
        }

        Box(modifier = Modifier.offset(x = contextMenuOffset.x, y = contextMenuOffset.y)) {
            AppActionDropdownMenu(
                expanded = contextMenuExpanded,
                onDismiss = { contextMenuExpanded = false },
                app = app,
                appName = appName,
                onAction = onAction,
                onCopyPackageName = onCopyPackageName,
                onRequestDangerAction = onRequestDangerAction
            )
        }
    }
}

@Composable
private fun AppActionMenu(
    app: AppInfo,
    onAction: (AdbFunctionType) -> Unit,
    onCopyPackageName: () -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit,
    alpha: Float = 1f
) {
    var expanded by remember { mutableStateOf(false) }
    val appName = app.appName.takeIf { it.isNotBlank() && it != app.packageName } ?: app.packageName
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)

    Box {
        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { this.alpha = alpha }
                .clip(shape)
                .background(AppVisualTokens.Soft.copy(alpha = 0.6f))
                .border(1.dp, AppVisualTokens.Border, shape)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconParkIcons.More,
                contentDescription = l10n("更多操作", "More actions"),
                tint = AppVisualTokens.Muted,
                modifier = Modifier.size(UiTokens.IconSmall)
            )
        }

        AppActionDropdownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            app = app,
            appName = appName,
            onAction = onAction,
            onCopyPackageName = onCopyPackageName,
            onRequestDangerAction = onRequestDangerAction
        )
    }
}

@Composable
private fun AppActionDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    app: AppInfo,
    appName: String,
    onAction: (AdbFunctionType) -> Unit,
    onCopyPackageName: () -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(185.dp)
            .background(AppVisualTokens.Surface, RoundedCornerShape(UiTokens.RadiusMedium))
            .border(1.dp, AppVisualTokens.Border, RoundedCornerShape(UiTokens.RadiusMedium))
            .padding(vertical = UiTokens.SpaceXSmall)
    ) {
        AppActionMenuItem(
            icon = Icons.Default.ContentCopy,
            text = l10n("复制包名", "Copy package"),
            iconTint = AppVisualTokens.Primary
        ) {
            onDismiss()
            onCopyPackageName()
        }
        AppActionMenuItem(
            icon = IconParkIcons.Right,
            text = l10n("打开详情", "Open details"),
            iconTint = AppVisualTokens.Primary
        ) {
            onDismiss()
            onAction(AdbFunctionType.APP_INFO)
        }
        AppActionMenuItem(
            icon = if (app.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            text = if (app.isRunning) l10n("强行停止", "Force stop") else l10n("启动应用", "Launch app"),
            iconTint = if (app.isRunning) AppVisualTokens.Warning else AppVisualTokens.Success
        ) {
            onDismiss()
            if (app.isRunning) {
                onRequestDangerAction(
                    AdbFunctionType.FORCE_STOP,
                    l10n("确认强行停止应用", "Confirm force stop"),
                    l10n("将强行停止 $appName。", "App $appName will be force stopped.")
                )
            } else {
                onAction(AdbFunctionType.LAUNCH)
            }
        }
        AppActionMenuItem(
            icon = Icons.Default.DeleteSweep,
            text = l10n("清除数据", "Clear data"),
            iconTint = AppVisualTokens.Primary
        ) {
            onDismiss()
            onRequestDangerAction(
                AdbFunctionType.CLEAR_DATA,
                l10n("确认清除应用数据", "Confirm clear app data"),
                l10n("将清除 $appName 的本地数据，该操作不可撤销。", "This will clear local data of $appName. This action cannot be undone.")
            )
        }
        AppActionMenuItem(
            icon = IconParkIcons.Download,
            text = l10n("导出 APK", "Export APK"),
            iconTint = AppVisualTokens.Success
        ) {
            onDismiss()
            onAction(AdbFunctionType.EXPORT_APK)
        }
        AppActionMenuItem(
            icon = Icons.Default.PrivacyTip,
            text = l10n("查看权限", "View permissions"),
            iconTint = AppVisualTokens.Primary
        ) {
            onDismiss()
            onAction(AdbFunctionType.APP_INFO)
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
            color = AppVisualTokens.Divider.copy(alpha = 0.5f)
        )
        AppActionMenuItem(
            icon = Icons.Default.Delete,
            text = l10n("卸载应用", "Uninstall app"),
            iconTint = AppVisualTokens.Danger,
            textColor = AppVisualTokens.Danger
        ) {
            onDismiss()
            onRequestDangerAction(
                AdbFunctionType.UNINSTALL,
                l10n("确认卸载应用", "Confirm uninstall"),
                l10n("将从设备卸载 $appName，请确认继续。", "App $appName will be uninstalled from device. Continue?")
            )
        }
    }
}

@Composable
private fun AppActionMenuItem(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    textColor: Color = AppVisualTokens.Text,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
            }
        },
        contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 6.dp)
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun AppGridCard(
    app: AppInfo,
    icon: ImageBitmap?,
    onAction: (AdbFunctionType) -> Unit,
    onCopyPackageName: () -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit,
    onOpen: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val appName = app.appName.takeIf { it.isNotBlank() && it != app.packageName } ?: app.packageName
    val shape = RoundedCornerShape(UiTokens.RadiusLarge)

    Box(
        modifier = Modifier
            .hoverable(interactionSource)
            .clip(shape)
            .background(if (isHovered) AppVisualTokens.Soft.copy(alpha = 0.5f) else AppVisualTokens.Surface)
            .border(
                1.dp,
                if (isHovered) AppVisualTokens.Primary.copy(alpha = 0.45f) else AppVisualTokens.Border,
                shape
            )

            .pointerInput(app.packageName) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.button == PointerButton.Secondary && event.type == PointerEventType.Press) {
                            val point = event.changes.firstOrNull()?.position
                            if (point != null) {
                                contextMenuOffset = with(density) { DpOffset(point.x.toDp(), point.y.toDp()) }
                            }
                            contextMenuExpanded = true
                        }
                    }
                }
            }
            .combinedClickable(onClick = { onAction(AdbFunctionType.APP_INFO) }, onDoubleClick = onOpen)
            .padding(UiTokens.SpaceMedium)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Avatar + Action menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppAvatar(app = app, icon = icon, size = 38)
                AppActionMenu(
                    app = app,
                    alpha = if (isHovered) 1f else 0.7f,
                    onAction = onAction,
                    onCopyPackageName = onCopyPackageName,
                    onRequestDangerAction = onRequestDangerAction
                )
            }

            // Middle: Name & Package
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppVisualTokens.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    fontSize = 11.sp,
                    color = AppVisualTokens.Tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppTypeTag(text = if (app.isSystemApp) l10n("系统", "System") else l10n("用户", "User"))
                if (app.isRunning) {
                    StatusPill(isRunning = true)
                } else if (app.size.isNotBlank() && app.size != "-") {
                    Text(
                        text = app.size,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = AppVisualTokens.Tertiary,
                        fontSize = 11.sp
                    )
                } else if (app.versionName.isNotBlank() && app.versionName != "-") {
                    Text(
                        text = "v${app.versionName}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = AppVisualTokens.Tertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(modifier = Modifier.offset(x = contextMenuOffset.x, y = contextMenuOffset.y)) {
            AppActionDropdownMenu(
                expanded = contextMenuExpanded,
                onDismiss = { contextMenuExpanded = false },
                app = app,
                appName = appName,
                onAction = onAction,
                onCopyPackageName = onCopyPackageName,
                onRequestDangerAction = onRequestDangerAction
            )
        }
    }
}

@Composable
private fun AppListFooter(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = UiTokens.SpaceSmall, start = UiTokens.SpaceXSmall, end = UiTokens.SpaceSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = l10n("共 $count 个应用", "$count apps in total"),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Tertiary,
            fontSize = 11.5.sp
        )
        Text(
            text = l10n("右键点击应用可快速执行操作", "Right-click any item for quick actions"),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Tertiary.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}

// ── Custom Canvas Empty State Illustrations ─────────────────────────────────

@Composable
private fun NoDeviceAppIllustration() {
    val brand = QadbTokens.brand
    val muted = QadbTokens.textMuted

    Canvas(modifier = Modifier.size(88.dp, 68.dp)) {
        val w = size.width
        val h = size.height
        val strokeStyle = Stroke(width = 2.2f, cap = StrokeCap.Round)

        val phoneW = w * 0.36f
        val phoneH = h * 0.70f
        val phoneL = w * 0.12f
        val phoneT = h * 0.08f
        drawRoundRect(
            color = brand,
            topLeft = Offset(phoneL, phoneT),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(8f),
            style = strokeStyle
        )
        drawRoundRect(
            color = brand.copy(alpha = 0.4f),
            topLeft = Offset(phoneL + phoneW * 0.3f, phoneT + 4f),
            size = Size(phoneW * 0.4f, 2.5f),
            cornerRadius = CornerRadius(1f)
        )
        drawLine(
            color = brand.copy(alpha = 0.4f),
            start = Offset(phoneL + phoneW * 0.3f, phoneT + phoneH - 6f),
            end = Offset(phoneL + phoneW * 0.7f, phoneT + phoneH - 6f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        val iconSize = 13f
        val appOffsets = listOf(
            Offset(w * 0.58f, h * 0.15f),
            Offset(w * 0.78f, h * 0.18f),
            Offset(w * 0.60f, h * 0.44f),
            Offset(w * 0.80f, h * 0.48f)
        )
        appOffsets.forEachIndexed { idx, offset ->
            drawRoundRect(
                color = if (idx % 2 == 0) brand.copy(alpha = 0.6f) else muted.copy(alpha = 0.4f),
                topLeft = offset,
                size = Size(iconSize, iconSize),
                cornerRadius = CornerRadius(3.5f),
                style = Stroke(width = 1.6f)
            )
        }

        val dashPath = listOf(
            Offset(w * 0.50f, h * 0.35f),
            Offset(w * 0.53f, h * 0.35f),
            Offset(w * 0.56f, h * 0.35f)
        )
        dashPath.forEach { pt ->
            drawCircle(color = muted.copy(alpha = 0.5f), radius = 1.5f, center = pt)
        }
    }
}

@Composable
private fun NoMatchingAppsIllustration() {
    val warning = QadbTokens.warning
    val muted = QadbTokens.textMuted

    Canvas(modifier = Modifier.size(80.dp, 64.dp)) {
        val w = size.width
        val h = size.height

        val gridSize = 16f
        val gridGap = 6f
        val startX = w * 0.12f
        val startY = h * 0.18f
        val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round)

        for (row in 0..1) {
            for (col in 0..1) {
                val gx = startX + col * (gridSize + gridGap)
                val gy = startY + row * (gridSize + gridGap)
                drawRoundRect(
                    color = muted.copy(alpha = 0.35f),
                    topLeft = Offset(gx, gy),
                    size = Size(gridSize, gridSize),
                    cornerRadius = CornerRadius(4f),
                    style = stroke
                )
            }
        }

        val cx = w * 0.58f
        val cy = h * 0.42f
        val r = w * 0.22f
        val strokeW = 2.4f

        drawCircle(color = warning, radius = r, center = Offset(cx, cy), style = Stroke(width = strokeW, cap = StrokeCap.Round))
        val cos45 = 0.7071f
        val hx = cx + r * cos45
        val hy = cy + r * cos45
        drawLine(color = warning, start = Offset(hx, hy), end = Offset(hx + r * 0.55f, hy + r * 0.55f), strokeWidth = strokeW + 0.8f, cap = StrokeCap.Round)
        val off = r * 0.40f
        drawLine(color = muted, start = Offset(cx - off, cy - off), end = Offset(cx + off, cy + off), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
        drawLine(color = muted, start = Offset(cx - off, cy + off), end = Offset(cx + off, cy - off), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    }
}

@Composable
private fun AppEmptyState(
    reason: AppListEmptyReason,
    onRefresh: (() -> Unit)? = null,
    onRefreshDevices: () -> Unit,
    onOpenWirelessConnection: () -> Unit,
    onOpenTroubleshooting: () -> Unit
) {
    if (reason == AppListEmptyReason.NO_DEVICE) {
        DeviceRequiredState(
            title = l10n("未连接设备", "No device connected"),
            description = l10n(
                "连接并选择设备后即可查看、搜索和管理应用。",
                "Connect and select a device to view, search, and manage apps."
            ),
            onRefreshDevices = onRefreshDevices,
            onOpenWirelessConnection = onOpenWirelessConnection,
            onOpenTroubleshooting = onOpenTroubleshooting,
            modifier = Modifier.fillMaxSize()
        )
        return
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
            modifier = Modifier.padding(UiTokens.SpaceXXLarge)
        ) {
            NoMatchingAppsIllustration()

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = l10n("暂无匹配应用", "No matching apps found"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppVisualTokens.Text,
                textAlign = TextAlign.Center
            )

            Text(
                text = l10n("尝试调整搜索关键字、切换分类筛选或刷新应用列表。", "Try adjusting your search terms, changing filter categories, or refreshing."),
                style = MaterialTheme.typography.bodyMedium,
                color = AppVisualTokens.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 340.dp)
            )

            if (onRefresh != null) {
                Spacer(modifier = Modifier.height(UiTokens.SpaceSmall))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppVisualTokens.Primary,
                        contentColor = QadbColors.onPrimary
                    ),
                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(IconParkIcons.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(UiTokens.SpaceSmall))
                    Text(text = l10n("刷新应用列表", "Refresh app list"), color = QadbColors.onPrimary, fontSize = 12.5.sp)
                }
            }
        }
    }
}

// ── Detail Page Components ──────────────────────────────────────────────────

private data class DetailTab(
    val title: String,
    val count: Int? = null
)

@Composable
private fun AppDetailPage(
    appInfo: AppInfoData,
    icon: ImageBitmap?,
    onBack: () -> Unit,
    onAction: (AdbFunctionType) -> Unit,
    onCopy: (String) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) },
    modifier: Modifier = Modifier
) {
    val detailTabs = remember(appInfo) {
        listOf(
            DetailTab(l10n("概览", "Overview")),
            DetailTab(l10n("权限", "Permissions"), appInfo.totalPermissionCount.takeIf { it > 0 }),
            DetailTab(l10n("Activity", "Activity"), appInfo.activityDetails.size.takeIf { it > 0 }),
            DetailTab(l10n("Service", "Service"), appInfo.serviceDetails.size.takeIf { it > 0 }),
            DetailTab(l10n("Receiver", "Receiver"), appInfo.receiverDetails.size.takeIf { it > 0 }),
            DetailTab(l10n("Provider", "Provider"), appInfo.providerDetails.size.takeIf { it > 0 }),
            DetailTab(l10n("签名", "Signature"), appInfo.signatureDetails.size.takeIf { it > 0 })
        )
    }
    var selectedTab by remember { mutableStateOf(detailTabs.first().title) }
    val scrollState = rememberScrollState()

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(end = UiTokens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
        ) {
            // Top Navigation Bar: Back button + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                        .background(AppVisualTokens.Soft.copy(alpha = 0.6f))
                        .border(1.dp, AppVisualTokens.Border, RoundedCornerShape(UiTokens.RadiusMedium))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = l10n("返回", "Back"),
                        tint = AppVisualTokens.Text,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = l10n("应用详情", "Application Details"),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppVisualTokens.Text,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = appInfo.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontSize = 11.5.sp,
                        color = AppVisualTokens.Tertiary
                    )
                }
            }

            // Hero Header Card
            DetailHeaderCard(
                appInfo = appInfo,
                icon = icon,
                onAction = onAction,
                onCopy = onCopy,
                onRequestDangerAction = onRequestDangerAction
            )

            // Sub-Tab Navigation Bar
            val tabListState = rememberLazyListState()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                LazyRow(
                    state = tabListState,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(detailTabs) { tab ->
                        DetailTabItem(
                            title = tab.title,
                            badgeCount = tab.count,
                            selected = selectedTab == tab.title,
                            onClick = { selectedTab = tab.title }
                        )
                    }
                }
                HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.5f))
            }

            // Tab Content
            when (selectedTab) {
                l10n("概览", "Overview") -> AppInfoDetailContent(appInfo, onCopy)
                l10n("权限", "Permissions") -> PermissionDetailContent(appInfo, onCopy)
                l10n("Activity", "Activity") -> ActivityDetailContent(appInfo, onCopy)
                l10n("Service", "Service") -> ServiceDetailContent(appInfo, onCopy)
                l10n("Receiver", "Receiver") -> ReceiverDetailContent(appInfo, onCopy)
                l10n("Provider", "Provider") -> ProviderDetailContent(appInfo, onCopy)
                l10n("签名", "Signature") -> SignatureDetailContent(appInfo, onCopy)
                else -> DetailPlaceholder(selectedTab)
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(8.dp)
        )
    }
}

@Composable
private fun DetailHeaderCard(
    appInfo: AppInfoData,
    icon: ImageBitmap?,
    onAction: (AdbFunctionType) -> Unit,
    onCopy: (String) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) }
) {
    FramedStateSurface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(UiTokens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
                verticalAlignment = Alignment.Top
            ) {
                // Left: Avatar + Identity + Meta Pills
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
                    verticalAlignment = Alignment.Top
                ) {
                    AppAvatar(
                        app = AppInfo(appName = appInfo.appName.ifBlank { appInfo.packageName }, packageName = appInfo.packageName),
                        icon = icon,
                        size = 56
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                        ) {
                            Text(
                                text = appInfo.appName.ifBlank { appInfo.packageName },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppVisualTokens.Text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            StatusPill(isRunning = appInfo.isRunning)
                            AppTypeTag(text = if (appInfo.isSystemApp) l10n("系统应用", "System") else l10n("用户应用", "User"))
                        }

                        // Copyable Package Name pill
                        CopyableCodePill(
                            label = "package:",
                            value = appInfo.packageName,
                            copyText = appInfo.packageName,
                            onCopy = onCopy
                        )

                        // Quick info chips row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (appInfo.versionName.isNotBlank() && appInfo.versionName != "-") {
                                MetaBadgeChip(
                                    label = "v${appInfo.versionName}" + if (appInfo.versionCode != "-" && appInfo.versionCode.isNotBlank()) " (${appInfo.versionCode})" else ""
                                )
                            }
                            if (appInfo.targetSdk.isNotBlank() && appInfo.targetSdk != "-") {
                                MetaBadgeChip(label = "Target SDK ${appInfo.targetSdk}")
                            }
                            if (appInfo.installLocation.isNotBlank() && appInfo.installLocation != "-") {
                                MetaBadgeChip(label = appInfo.installLocation)
                            }
                        }
                    }
                }

                // Right: Storage usage card
                HeaderStorageUsageCard(
                    appInfo = appInfo,
                    modifier = Modifier.widthIn(min = 250.dp, max = 285.dp)
                )
            }

            // Divider
            HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.5f))

            // Action toolbar
            DetailActionsPanel(
                appInfo = appInfo,
                onAction = onAction,
                onRequestDangerAction = onRequestDangerAction
            )
        }
    }
}

@Composable
private fun CopyableCodePill(
    label: String,
    value: String,
    copyText: String,
    onCopy: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(UiTokens.RadiusSmall)

    Row(
        modifier = Modifier
            .hoverable(interactionSource)
            .clip(shape)
            .background(if (isHovered) AppVisualTokens.BrandSoft else AppVisualTokens.Soft.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (isHovered) AppVisualTokens.Primary.copy(alpha = 0.4f) else AppVisualTokens.Border,
                shape
            )
            .clickable { onCopy(copyText) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            fontSize = 11.sp,
            color = AppVisualTokens.Muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (isHovered) AppVisualTokens.Primary else AppVisualTokens.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            Icons.Default.ContentCopy,
            contentDescription = l10n("复制", "Copy"),
            tint = if (isHovered) AppVisualTokens.Primary else AppVisualTokens.Muted,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun MetaBadgeChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(UiTokens.RadiusSmall))
            .background(AppVisualTokens.Soft.copy(alpha = 0.4f))
            .border(0.5.dp, AppVisualTokens.Border, RoundedCornerShape(UiTokens.RadiusSmall))
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            fontSize = 11.sp,
            color = AppVisualTokens.Tertiary
        )
    }
}

@Composable
private fun HeaderStorageUsageCard(
    appInfo: AppInfoData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        Text(
            text = l10n("存储占用", "Storage Breakdown"),
            style = MaterialTheme.typography.labelMedium,
            color = AppVisualTokens.Muted,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StorageRing(total = appInfo.totalSize, size = 68.dp, innerSize = 48.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StorageLegend(AppVisualTokens.Primary, l10n("应用大小", "App size"), appInfo.appSize)
                StorageLegend(AppVisualTokens.Warning, l10n("应用数据", "App data"), appInfo.dataSize)
                StorageLegend(AppVisualTokens.Muted, l10n("缓存数据", "Cache"), appInfo.cacheSize)
                HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = l10n("总计", "Total"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppVisualTokens.Text
                    )
                    Text(
                        text = appInfo.totalSize.takeIf { it.isNotBlank() && it != "-" } ?: "-",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = AppVisualTokens.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailActionsPanel(
    appInfo: AppInfoData,
    onAction: (AdbFunctionType) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Launch / Stop action button
        DetailActionButton(
            icon = if (appInfo.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            text = if (appInfo.isRunning) l10n("停止应用", "Stop app") else l10n("启动应用", "Launch app"),
            tint = QadbColors.onPrimary,
            bgColor = if (appInfo.isRunning) AppVisualTokens.Warning else AppVisualTokens.Primary,
            modifier = Modifier.width(128.dp)
        ) {
            onAction(if (appInfo.isRunning) AdbFunctionType.FORCE_STOP else AdbFunctionType.LAUNCH)
        }

        // Refresh button
        DetailActionButton(
            icon = IconParkIcons.Refresh,
            text = l10n("刷新", "Refresh"),
            tint = AppVisualTokens.Text,
            bgColor = AppVisualTokens.Soft.copy(alpha = 0.6f),
            modifier = Modifier.width(96.dp)
        ) { onAction(AdbFunctionType.APP_INFO) }

        // Clear Data button
        DetailActionButton(
            icon = Icons.Default.DeleteSweep,
            text = l10n("清除数据", "Clear data"),
            tint = AppVisualTokens.Text,
            bgColor = AppVisualTokens.Soft.copy(alpha = 0.6f),
            modifier = Modifier.width(116.dp)
        ) {
            onRequestDangerAction(
                AdbFunctionType.CLEAR_DATA,
                l10n("确认清除数据", "Confirm Clear Data"),
                l10n("将清除该应用的全部数据，此操作不可撤销。是否继续？", "This will clear all app data. This cannot be undone. Continue?")
            )
        }

        // Export APK button
        DetailActionButton(
            icon = IconParkIcons.Download,
            text = l10n("导出 APK", "Export APK"),
            tint = AppVisualTokens.Text,
            bgColor = AppVisualTokens.Soft.copy(alpha = 0.6f),
            modifier = Modifier.width(116.dp)
        ) { onAction(AdbFunctionType.EXPORT_APK) }

        Spacer(modifier = Modifier.weight(1f))

        // Danger uninstall button
        DetailActionButton(
            icon = Icons.Default.Delete,
            text = l10n("卸载应用", "Uninstall"),
            tint = AppVisualTokens.Danger,
            bgColor = AppVisualTokens.Danger.copy(alpha = 0.08f),
            modifier = Modifier.width(110.dp)
        ) {
            onRequestDangerAction(
                AdbFunctionType.UNINSTALL,
                l10n("确认卸载应用", "Confirm Uninstall"),
                l10n("将彻底卸载此应用，应用数据将被清除。是否继续？", "This app will be uninstalled and its data will be erased. Continue?")
            )
        }
    }
}

@Composable
private fun DetailActionButton(
    icon: ImageVector,
    text: String,
    tint: Color,
    bgColor: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(34.dp)
            .hoverable(interactionSource),
        shape = shape,
        color = if (enabled) {
            if (isHovered && bgColor.alpha >= 0.9f) bgColor.copy(alpha = 0.88f)
            else if (isHovered) bgColor.copy(alpha = (bgColor.alpha + 0.15f).coerceAtMost(1f))
            else bgColor
        } else AppVisualTokens.Soft,
        border = BorderStroke(
            1.dp,
            if (isHovered && bgColor == AppVisualTokens.Soft.copy(alpha = 0.6f)) AppVisualTokens.Primary.copy(alpha = 0.4f)
            else if (bgColor == AppVisualTokens.Surface || bgColor.alpha < 0.9f) AppVisualTokens.Border
            else bgColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = UiTokens.SpaceMedium),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = if (enabled) tint else AppVisualTokens.Muted.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(UiTokens.SpaceSmall))
            Text(
                text,
                color = if (enabled) tint else AppVisualTokens.Muted.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DetailTabItem(
    title: String,
    badgeCount: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = UiTokens.SpaceMedium, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = if (selected) AppVisualTokens.Primary else if (isHovered) AppVisualTokens.Text else AppVisualTokens.Muted,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                        .background(
                            if (selected) AppVisualTokens.Primary.copy(alpha = 0.12f)
                            else AppVisualTokens.Soft
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) AppVisualTokens.Primary else AppVisualTokens.Muted
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.5.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                .background(if (selected) AppVisualTokens.Primary else Color.Transparent)
        )
    }
}

@Composable
private fun AppInfoDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 980.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                BasicInfoCard(appInfo, onCopy)
                ProcessInfoCard(appInfo)
                PermissionSummaryCard(appInfo)
                OperationLogCard(appInfo)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                    BasicInfoCard(appInfo, onCopy, Modifier.weight(1.08f))
                    ProcessInfoCard(appInfo, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                    PermissionSummaryCard(appInfo, Modifier.weight(1f))
                    OperationLogCard(appInfo, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FramedStateSurface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(UiTokens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppVisualTokens.Text
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppVisualTokens.Muted
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun BasicInfoCard(appInfo: AppInfoData, onCopy: (String) -> Unit, modifier: Modifier = Modifier) {
    val infoItems = listOf(
        l10n("应用名称", "App name") to (appInfo.appName.ifBlank { appInfo.packageName } to false),
        l10n("包名", "Package") to (appInfo.packageName to true),
        l10n("版本名称", "Version name") to (appInfo.versionName to true),
        l10n("版本号", "Version code") to (appInfo.versionCode to true),
        l10n("最小 SDK", "Min SDK") to (appInfo.minSdk to true),
        l10n("目标 SDK", "Target SDK") to (appInfo.targetSdk to true),
        l10n("安装位置", "Install location") to (appInfo.installLocation to false),
        l10n("应用类型", "App type") to ((if (appInfo.isSystemApp) l10n("系统应用", "System app") else l10n("用户应用", "User app")) to false),
        l10n("应用大小", "App size") to (appInfo.appSize to true),
        l10n("数据目录", "Data dir") to (appInfo.dataDir to true),
        l10n("APK 路径", "APK path") to (appInfo.apkPath to true)
    )
    DetailSectionCard(title = l10n("基本信息", "Basic info"), modifier = modifier) {
        DetailInfoGrid(items = infoItems, onCopy = onCopy)
    }
}

@Composable
private fun DetailInfoGrid(items: List<Pair<String, Pair<String, Boolean>>>, onCopy: (String) -> Unit) {
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXLarge)
            ) {
                row.forEach { (label, valuePair) ->
                    val (value, isMono) = valuePair
                    DetailInfoGridCell(
                        label = label,
                        value = value,
                        isMono = isMono,
                        onCopy = onCopy,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index != rows.lastIndex) {
                HorizontalDivider(color = AppVisualTokens.Divider.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun DetailInfoGridCell(
    label: String,
    value: String,
    isMono: Boolean = false,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val canCopy = isMono && value.isNotBlank() && value != "-"

    Row(
        modifier = modifier
            .heightIn(min = 40.dp)
            .hoverable(interactionSource)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppVisualTokens.Muted
            )
            SelectionContainer {
                Text(
                    text = value.ifBlank { "-" },
                    style = if (isMono) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodySmall,
                    color = AppVisualTokens.Text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (canCopy && isHovered) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(AppVisualTokens.Soft)
                    .clickable { onCopy(value) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = l10n("复制", "Copy"),
                    tint = AppVisualTokens.Primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageRing(
    total: String,
    size: Dp = 126.dp,
    innerSize: Dp = 92.dp
) {
    val compact = size <= 92.dp
    val normalized = total.takeIf { it.isNotBlank() && it != "-" } ?: "-"
    val valueText = normalized.substringBefore(" ").ifBlank { normalized }
    val unitText = normalized.substringAfter(" ", "")

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AppVisualTokens.Selected),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(AppVisualTokens.Surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (compact) 2.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = valueText,
                    style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppVisualTokens.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (unitText.isNotBlank()) {
                    Text(
                        text = unitText,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppVisualTokens.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageLegend(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
            Spacer(Modifier.width(UiTokens.SpaceSmall))
            Text(label, style = MaterialTheme.typography.bodySmall, color = AppVisualTokens.Muted)
        }
        Text(value.takeIf { it.isNotBlank() && it != "-" } ?: "-", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = AppVisualTokens.Text)
    }
}

@Composable
private fun ProcessInfoCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    DetailSectionCard(title = l10n("进程信息", "Process info"), modifier = modifier) {
        DetailInfoRow(l10n("主进程", "Main process"), appInfo.packageName, isMono = true)
        DetailInfoRow(l10n("进程 ID (PID)", "Process ID (PID)"), appInfo.processId, isMono = true)
        DetailInfoRow(l10n("内存占用", "Memory usage"), appInfo.memoryUsage, isMono = true)
        DetailInfoRow(l10n("状态", "Status"), if (appInfo.isRunning) l10n("运行中", "Running") else l10n("未运行", "Stopped"))
        DetailInfoRow(l10n("启动时间", "Start time"), appInfo.startTime)
    }
}

@Composable
private fun PermissionSummaryCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    DetailSectionCard(title = l10n("权限统计", "Permission summary"), modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            PermissionTile(l10n("危险权限", "Dangerous"), appInfo.dangerousPermissionCount, Icons.Default.GppMaybe, AppVisualTokens.Danger, Modifier.weight(1f))
            PermissionTile(l10n("隐私权限", "Privacy"), appInfo.privacyPermissionCount, Icons.Default.Lock, AppVisualTokens.Primary, Modifier.weight(1f))
            PermissionTile(l10n("普通权限", "Normal"), appInfo.normalPermissionCount, IconParkIcons.Info, AppVisualTokens.Muted, Modifier.weight(1f))
            PermissionTile(l10n("全部权限", "All"), appInfo.totalPermissionCount, IconParkIcons.Application, AppVisualTokens.Tertiary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PermissionTile(
    label: String,
    value: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    Box(
        modifier = modifier
            .height(76.dp)
            .clip(shape)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.22f), shape)
            .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall, Alignment.CenterVertically)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp)
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppVisualTokens.Muted)
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = AppVisualTokens.Text,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OperationLogCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    DetailSectionCard(title = l10n("操作记录", "Operation log"), modifier = modifier) {
        OperationRow(l10n("首次安装", "Installed"), appInfo.firstInstallTime)
        OperationRow(l10n("最近更新", "Updated"), appInfo.lastUpdateTime)
        OperationRow(l10n("读取详情", "Inspected"), l10n("当前会话", "Current session"))
        OperationRow(l10n("运行状态", "State"), if (appInfo.isRunning) l10n("运行中", "Running") else l10n("未运行", "Stopped"))
    }
}

@Composable
private fun OperationRow(action: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AppVisualTokens.Primary.copy(alpha = 0.7f)))
            Text(action, style = MaterialTheme.typography.bodySmall, color = AppVisualTokens.Text)
        }
        Text(time.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = AppVisualTokens.Muted)
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(104.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted
        )
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = value.ifBlank { "-" },
                style = if (isMono) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PermissionDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    var selectedFilter by remember { mutableStateOf(PermissionFilter.ALL) }

    val dangerousKeywords = listOf(
        "CAMERA", "LOCATION", "RECORD_AUDIO", "CONTACTS", "SMS", "PHONE",
        "CALENDAR", "BODY_SENSORS", "STORAGE", "BLUETOOTH"
    )
    val privacyKeywords = listOf("CAMERA", "LOCATION", "RECORD_AUDIO", "CONTACTS", "SMS", "PHONE")

    val filteredPermissions = remember(appInfo.permissionDetails, selectedFilter) {
        when (selectedFilter) {
            PermissionFilter.DANGEROUS -> appInfo.permissionDetails.filter { permission ->
                dangerousKeywords.any { keyword -> permission.contains(keyword) }
            }
            PermissionFilter.PRIVACY -> appInfo.permissionDetails.filter { permission ->
                privacyKeywords.any { keyword -> permission.contains(keyword) }
            }
            PermissionFilter.NORMAL -> appInfo.permissionDetails.filter { permission ->
                dangerousKeywords.none { keyword -> permission.contains(keyword) }
            }
            PermissionFilter.ALL -> appInfo.permissionDetails
        }
    }

    DetailSectionCard(
        title = l10n("权限管理", "Permission Management"),
        subtitle = l10n(
            "已声明 ${appInfo.totalPermissionCount} 项权限，其中 ${appInfo.dangerousPermissionCount} 项涉及敏感硬件或个人隐私数据。",
            "Declared ${appInfo.totalPermissionCount} permissions, ${appInfo.dangerousPermissionCount} involve sensitive hardware or privacy data."
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Summary metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            PermissionTile(l10n("危险权限", "Dangerous"), appInfo.dangerousPermissionCount, Icons.Default.GppMaybe, AppVisualTokens.Danger, Modifier.weight(1f))
            PermissionTile(l10n("隐私权限", "Privacy"), appInfo.privacyPermissionCount, Icons.Default.Lock, AppVisualTokens.Primary, Modifier.weight(1f))
            PermissionTile(l10n("普通权限", "Normal"), appInfo.normalPermissionCount, IconParkIcons.Info, AppVisualTokens.Muted, Modifier.weight(1f))
        }

        // Filter pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
            modifier = Modifier.fillMaxWidth()
        ) {
            PermissionFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                val count = when (filter) {
                    PermissionFilter.ALL -> appInfo.totalPermissionCount
                    PermissionFilter.DANGEROUS -> appInfo.dangerousPermissionCount
                    PermissionFilter.PRIVACY -> appInfo.privacyPermissionCount
                    PermissionFilter.NORMAL -> appInfo.normalPermissionCount
                }
                val shape = RoundedCornerShape(UiTokens.RadiusMedium)
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(shape)
                        .background(if (selected) AppVisualTokens.BrandSoft else AppVisualTokens.Soft.copy(alpha = 0.4f))
                        .border(
                            1.dp,
                            if (selected) AppVisualTokens.Primary.copy(alpha = 0.45f) else AppVisualTokens.Border,
                            shape
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = UiTokens.SpaceMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when (filter) {
                                PermissionFilter.ALL -> l10n("全部", "All")
                                PermissionFilter.DANGEROUS -> l10n("危险权限", "Dangerous")
                                PermissionFilter.PRIVACY -> l10n("隐私权限", "Privacy")
                                PermissionFilter.NORMAL -> l10n("普通权限", "Normal")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) AppVisualTokens.Primary else AppVisualTokens.Text,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                                .background(if (selected) AppVisualTokens.Primary.copy(alpha = 0.15f) else AppVisualTokens.Soft)
                                .padding(horizontal = 5.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) AppVisualTokens.Primary else AppVisualTokens.Muted
                            )
                        }
                    }
                }
            }
        }

        // Permissions items list or empty state
        if (filteredPermissions.isEmpty()) {
            DetailEmptyState(
                illustration = { EmptyPermissionIllustration() },
                title = l10n("无相关权限", "No permissions found"),
                subtitle = l10n("当前筛选条件下未检测到相关权限条目。", "No permission entries matching current filter.")
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredPermissions.forEach { permission ->
                    val isDangerous = dangerousKeywords.any { permission.contains(it) }
                    val isPrivacy = privacyKeywords.any { permission.contains(it) }
                    DetailPermissionRow(
                        permission = permission,
                        isDangerous = isDangerous,
                        isPrivacy = isPrivacy,
                        onCopy = onCopy
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailPermissionRow(
    permission: String,
    isDangerous: Boolean,
    isPrivacy: Boolean,
    onCopy: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clip(shape)
            .background(if (isHovered) AppVisualTokens.Soft.copy(alpha = 0.6f) else AppVisualTokens.Soft.copy(alpha = 0.35f))
            .border(
                1.dp,
                if (isHovered) AppVisualTokens.Primary.copy(alpha = 0.35f) else AppVisualTokens.Border,
                shape
            )
            .padding(horizontal = UiTokens.SpaceMedium, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(
                        when {
                            isDangerous -> AppVisualTokens.Danger.copy(alpha = 0.12f)
                            isPrivacy -> AppVisualTokens.Primary.copy(alpha = 0.12f)
                            else -> AppVisualTokens.Soft
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isDangerous -> Icons.Default.GppMaybe
                        isPrivacy -> Icons.Default.Lock
                        else -> IconParkIcons.Info
                    },
                    contentDescription = null,
                    tint = when {
                        isDangerous -> AppVisualTokens.Danger
                        isPrivacy -> AppVisualTokens.Primary
                        else -> AppVisualTokens.Muted
                    },
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SelectionContainer {
                    Text(
                        text = permission,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppVisualTokens.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isDangerous) {
                AppTypeTag(text = l10n("危险", "Dangerous"), accent = AppVisualTokens.Danger)
            } else if (isPrivacy) {
                AppTypeTag(text = l10n("隐私", "Privacy"), accent = AppVisualTokens.Primary)
            }
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                        .background(AppVisualTokens.Soft)
                        .clickable { onCopy(permission) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = l10n("复制", "Copy"),
                        tint = AppVisualTokens.Primary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    DetailSectionCard(
        title = l10n("活动（Activity）", "Activities"),
        subtitle = l10n("该应用包含的所有 Activity 界面组件清单，共 ${appInfo.activityDetails.size} 个。", "All Activity UI components for this application (${appInfo.activityDetails.size} total)."),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailComponentSection(
            items = appInfo.activityDetails,
            type = "Activity",
            icon = IconParkIcons.Application,
            emptyTitle = l10n("未解析到 Activity", "No Activities Found"),
            emptySubtitle = l10n("该应用未导出或未检测到 Activity 组件定义。", "No Activity component definitions detected in package."),
            onCopy = onCopy
        )
    }
}

@Composable
private fun ServiceDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    DetailSectionCard(
        title = l10n("服务（Service）", "Services"),
        subtitle = l10n("后台常驻或绑定服务组件清单，共 ${appInfo.serviceDetails.size} 个。", "Background and bound service components (${appInfo.serviceDetails.size} total)."),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailComponentSection(
            items = appInfo.serviceDetails,
            type = "Service",
            icon = IconParkIcons.Cpu,
            emptyTitle = l10n("未解析到 Service", "No Services Found"),
            emptySubtitle = l10n("该应用未声明任何后台 Service 服务组件。", "No Service components declared by this application."),
            onCopy = onCopy
        )
    }
}

@Composable
private fun ReceiverDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    DetailSectionCard(
        title = l10n("广播接收器（Broadcast Receiver）", "Receivers"),
        subtitle = l10n("静态注册的广播接收器清单，共 ${appInfo.receiverDetails.size} 个。", "Statically registered Broadcast Receivers (${appInfo.receiverDetails.size} total)."),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailComponentSection(
            items = appInfo.receiverDetails,
            type = "Receiver",
            icon = IconParkIcons.Wifi,
            emptyTitle = l10n("未解析到 Receiver", "No Receivers Found"),
            emptySubtitle = l10n("该应用未静态注册广播接收器组件。", "No static Broadcast Receivers declared in package."),
            onCopy = onCopy
        )
    }
}

@Composable
private fun ProviderDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    DetailSectionCard(
        title = l10n("内容提供者（Content Provider）", "Providers"),
        subtitle = l10n("数据共享与存储提供者清单，共 ${appInfo.providerDetails.size} 个。", "Data sharing and Content Providers (${appInfo.providerDetails.size} total)."),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailComponentSection(
            items = appInfo.providerDetails,
            type = "Provider",
            icon = IconParkIcons.HardDisk,
            emptyTitle = l10n("未解析到 Provider", "No Providers Found"),
            emptySubtitle = l10n("该应用未声明 Content Provider 内容提供者组件。", "No Content Providers declared by this application."),
            onCopy = onCopy
        )
    }
}

@Composable
private fun DetailComponentSection(
    items: List<String>,
    type: String,
    icon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String,
    onCopy: (String) -> Unit
) {
    if (items.isEmpty()) {
        DetailEmptyState(
            illustration = { EmptyComponentIllustration(type) },
            title = emptyTitle,
            subtitle = emptySubtitle
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                DetailComponentCard(componentName = item, icon = icon, type = type, onCopy = onCopy)
            }
        }
    }
}

@Composable
private fun DetailComponentCard(
    componentName: String,
    icon: ImageVector,
    type: String,
    onCopy: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(UiTokens.RadiusMedium)
    val simpleName = componentName.substringAfterLast(".").ifBlank { componentName }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clip(shape)
            .background(if (isHovered) AppVisualTokens.Soft.copy(alpha = 0.6f) else AppVisualTokens.Soft.copy(alpha = 0.35f))
            .border(
                1.dp,
                if (isHovered) AppVisualTokens.Primary.copy(alpha = 0.35f) else AppVisualTokens.Border,
                shape
            )
            .padding(horizontal = UiTokens.SpaceMedium, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(AppVisualTokens.Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type,
                    tint = AppVisualTokens.Primary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = simpleName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = AppVisualTokens.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SelectionContainer {
                    Text(
                        text = componentName,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontSize = 11.sp,
                        color = AppVisualTokens.Tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (isHovered) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                    .background(AppVisualTokens.Soft)
                    .clickable { onCopy(componentName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = l10n("复制", "Copy"),
                    tint = AppVisualTokens.Primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun SignatureDetailContent(appInfo: AppInfoData, onCopy: (String) -> Unit) {
    DetailSectionCard(
        title = l10n("签名与证书信息", "Signature & Certificates"),
        subtitle = l10n("应用的数字签名证书指纹及校验数据。", "Digital signature certificate fingerprints and verification data."),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (appInfo.signatureDetails.isEmpty()) {
            DetailEmptyState(
                illustration = { EmptySignatureIllustration() },
                title = l10n("未解析到签名信息", "No Signature Info"),
                subtitle = l10n("当前设备或系统未返回该应用的签名证书详细信息。", "Device did not return signature certificate details for this app.")
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
            ) {
                // Code block for raw signature lines
                val shape = RoundedCornerShape(UiTokens.RadiusMedium)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(AppVisualTokens.Soft.copy(alpha = 0.45f))
                        .border(1.dp, AppVisualTokens.Border, shape)
                        .padding(UiTokens.SpaceMedium),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    appInfo.signatureDetails.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectionContainer(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    fontSize = 11.5.sp,
                                    color = AppVisualTokens.Text,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                                    .background(AppVisualTokens.Soft)
                                    .clickable { onCopy(line) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = l10n("复制", "Copy"),
                                    tint = AppVisualTokens.Muted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Dedicated Canvas Illustrations ──────────────────────────────────────────

@Composable
private fun EmptyComponentIllustration(type: String) {
    val brand = AppVisualTokens.Primary
    val muted = AppVisualTokens.Muted

    Canvas(modifier = Modifier.size(88.dp, 68.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = muted.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.76f),
            cornerRadius = CornerRadius(6f),
            style = Stroke(width = 1.5f)
        )

        drawLine(
            color = muted.copy(alpha = 0.25f),
            start = Offset(w * 0.12f, h * 0.32f),
            end = Offset(w * 0.88f, h * 0.32f),
            strokeWidth = 1.2f
        )

        drawCircle(color = muted.copy(alpha = 0.4f), radius = 2f, center = Offset(w * 0.22f, h * 0.22f))
        drawCircle(color = muted.copy(alpha = 0.3f), radius = 2f, center = Offset(w * 0.29f, h * 0.22f))
        drawCircle(color = muted.copy(alpha = 0.3f), radius = 2f, center = Offset(w * 0.36f, h * 0.22f))

        val cx = w * 0.50f
        val cy = h * 0.60f

        drawLine(
            color = brand.copy(alpha = 0.7f),
            start = Offset(cx - 16f, cy),
            end = Offset(cx - 8f, cy - 8f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = brand.copy(alpha = 0.7f),
            start = Offset(cx - 16f, cy),
            end = Offset(cx - 8f, cy + 8f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = muted.copy(alpha = 0.6f),
            start = Offset(cx - 2f, cy + 10f),
            end = Offset(cx + 2f, cy - 10f),
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = brand.copy(alpha = 0.7f),
            start = Offset(cx + 16f, cy),
            end = Offset(cx + 8f, cy - 8f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = brand.copy(alpha = 0.7f),
            start = Offset(cx + 16f, cy),
            end = Offset(cx + 8f, cy + 8f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun EmptyPermissionIllustration() {
    val brand = AppVisualTokens.Primary

    Canvas(modifier = Modifier.size(80.dp, 68.dp)) {
        val w = size.width
        val h = size.height

        val cx = w * 0.5f
        val cy = h * 0.48f

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx, cy - 24f)
            lineTo(cx + 22f, cy - 16f)
            cubicTo(cx + 22f, cy + 8f, cx + 12f, cy + 22f, cx, cy + 28f)
            cubicTo(cx - 12f, cy + 22f, cx - 22f, cy + 8f, cx - 22f, cy - 16f)
            close()
        }

        drawPath(
            path = path,
            color = brand.copy(alpha = 0.10f)
        )
        drawPath(
            path = path,
            color = brand.copy(alpha = 0.65f),
            style = Stroke(width = 1.8f, cap = StrokeCap.Round)
        )

        drawLine(
            color = brand,
            start = Offset(cx - 8f, cy + 2f),
            end = Offset(cx - 2f, cy + 8f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = brand,
            start = Offset(cx - 2f, cy + 8f),
            end = Offset(cx + 9f, cy - 5f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun EmptySignatureIllustration() {
    val warning = QadbTokens.warning
    val muted = AppVisualTokens.Muted

    Canvas(modifier = Modifier.size(80.dp, 68.dp)) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.45f

        drawRoundRect(
            color = muted.copy(alpha = 0.3f),
            topLeft = Offset(cx - 24f, cy - 22f),
            size = Size(48f, 44f),
            cornerRadius = CornerRadius(4f),
            style = Stroke(width = 1.5f)
        )

        drawLine(
            color = muted.copy(alpha = 0.4f),
            start = Offset(cx - 16f, cy - 12f),
            end = Offset(cx + 16f, cy - 12f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = muted.copy(alpha = 0.4f),
            start = Offset(cx - 16f, cy - 4f),
            end = Offset(cx + 10f, cy - 4f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = warning.copy(alpha = 0.8f),
            radius = 9f,
            center = Offset(cx + 14f, cy + 14f)
        )
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = Offset(cx + 14f, cy + 14f)
        )
    }
}

@Composable
private fun DetailEmptyState(
    illustration: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = UiTokens.SpaceXXLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            illustration()
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppVisualTokens.Text,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 340.dp)
            )
        }
    }
}

@Composable
private fun DetailPlaceholder(title: String) {
    DetailSectionCard(title = title, modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = l10n("当前版本暂未解析该分类数据", "This category is not parsed in current version"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
