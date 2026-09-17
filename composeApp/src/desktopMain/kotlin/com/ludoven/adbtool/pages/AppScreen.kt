package com.ludoven.adbtool.pages

import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.ui.icons.IconParkIcons

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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import com.ludoven.adbtool.ui.mac.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.entity.AppInfoData
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.AppViewModel
import com.ludoven.adbtool.widget.EmptyStatePanel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource

data class AppInfo(
    val appName: String,
    val packageName: String,
    val apkPath: String = "",
    val isSystemApp: Boolean = false,
    val isDebuggable: Boolean = false,
    val isDisabled: Boolean = false,
    val versionName: String = "-",
    val installTime: String = "-",
    val size: String = "-",
    val sizeBytes: Long? = null,
    val installTimestamp: Long? = null,
    val lastUsedTimestamp: Long? = null,
    val isRunning: Boolean = false
)

private fun AppInfoData.toAppInfo(): AppInfo = AppInfo(
    appName = appName,
    packageName = packageName,
    apkPath = apkPath,
    isSystemApp = isSystemApp,
    isRunning = isRunning
)

internal enum class AppFilter(val key: String) {
    ALL("all"),
    USER("user"),
    SYSTEM("system"),
    DEBUGGABLE("debug"),
    RECENT("recent"),
    RUNNING("running");

    fun matches(app: AppInfo): Boolean {
        return when (this) {
            ALL -> true
            USER -> !app.isSystemApp
            SYSTEM -> app.isSystemApp
            DEBUGGABLE -> app.isDebuggable
            RECENT -> (app.lastUsedTimestamp ?: app.installTimestamp ?: 0L) > 0L
            RUNNING -> app.isRunning
        }
    }

    companion object {
        fun fromKey(value: String): AppFilter {
            return entries.firstOrNull { it.key == value } ?: when (value) {
                "全部应用" -> ALL
                "用户应用" -> USER
                "系统应用" -> SYSTEM
                "可调试应用" -> DEBUGGABLE
                "最近使用" -> RECENT
                "运行中" -> RUNNING
                else -> ALL
            }
        }
    }
}

private enum class AppSortMode {
    Name,
    Size,
    Version,
    InstallTime,
    Recent
}

internal enum class AppListEmptyReason {
    NO_DEVICE,
    NO_RESULTS
}

private object AppVisualTokens {
    val Primary: Color @Composable get() = QadbColors.primary
    val Text: Color @Composable get() = QadbColors.textPrimary
    val Muted: Color @Composable get() = QadbColors.textSecondary
    val Border: Color @Composable get() = QadbColors.border
    val BorderStrong: Color @Composable get() = QadbColors.borderStrong
    val Divider: Color @Composable get() = QadbColors.divider
    val Surface: Color @Composable get() = QadbColors.surface
    val Soft: Color @Composable get() = QadbColors.surfaceVariant
    val Selected: Color @Composable get() = QadbColors.surfaceSelected
    val Success: Color @Composable get() = QadbColors.success
    val Warning: Color @Composable get() = QadbColors.warning
    val Danger: Color @Composable get() = QadbColors.danger
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
private fun AppAvatar(app: AppInfo, icon: ImageBitmap?, size: Int = 44) {
    val shape = RoundedCornerShape((size * 0.22f).dp)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(shape)
            .background(AppVisualTokens.Soft),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.appName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppVisualTokens.Soft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.appName.take(1).uppercase(),
                    color = QadbColors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (size * 0.34).sp
                )
            }
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

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = AppVisualTokens.Text),
        interactionSource = interactionSource,
        modifier = modifier
            .height(UiTokens.ControlHeight)
            .clip(shape)
            .background(AppVisualTokens.Surface)
            .border(
                width = 1.dp,
                color = if (isFocused) AppVisualTokens.Primary.copy(alpha = 0.45f) else AppVisualTokens.BorderStrong,
                shape = shape
            ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = UiTokens.SpaceMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    IconParkIcons.Search,
                    contentDescription = null,
                    tint = AppVisualTokens.Muted,
                    modifier = Modifier.size(UiTokens.IconSmall)
                )
                Spacer(Modifier.width(UiTokens.SpaceSmall))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = l10n("搜索应用名称或包名", "Search app name or package"),
                            color = AppVisualTokens.Muted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun AppScreen(
    viewModel: AppViewModel,
    selectedDevice: String?
) {
    val appList by viewModel.appList.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appIcons by viewModel.appIcons.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val appInfo by viewModel.appInfo.collectAsState()

    var sortMenuExpanded by remember { mutableStateOf(false) }
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
                .padding(horizontal = UiTokens.PagePadding, vertical = UiTokens.PagePaddingCompact),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.getAppList(selectedDevice, forceRefresh = true)
                        },
                        enabled = hasSelectedDevice,
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
                        Icon(IconParkIcons.Refresh, contentDescription = null, modifier = Modifier.size(UiTokens.IconSmall))
                        Spacer(Modifier.width(UiTokens.SpaceSmall))
                        Text(
                            text = l10n("刷新", "Refresh"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
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

                AppSearchField(
                    value = searchText,
                    onValueChange = { viewModel.setSearchText(it) },
                    modifier = Modifier.widthIn(min = 380.dp, max = 460.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)) {
                    items(tabs, key = { it.key }) { tab ->
                        val selected = selectedFilter == tab
                        Surface(
                            shape = RoundedCornerShape(UiTokens.RadiusMedium),
                            color = if (selected) AppVisualTokens.Selected else AppVisualTokens.Surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) QadbColors.selectedBorder else AppVisualTokens.Border
                            ),
                            modifier = Modifier.clickable { viewModel.setSelectedTab(tab.key) }
                        ) {
                            Text(
                                text = "${appTabLabel(tab)} ${tabCountMap[tab] ?: 0}",
                                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selected) AppVisualTokens.Primary else QadbColors.textSecondary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { sortMenuExpanded = true },
                            shape = RoundedCornerShape(UiTokens.RadiusMedium),
                            border = BorderStroke(1.dp, AppVisualTokens.Border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = AppVisualTokens.Surface,
                                contentColor = AppVisualTokens.Text
                            ),
                            contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(UiTokens.IconSmall))
                            Spacer(Modifier.width(UiTokens.SpaceSmall))
                            Text(
                                text = appSortModeLabel(sortMode),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(UiTokens.SpaceXSmall))
                            Icon(IconParkIcons.ArrowDown, contentDescription = null, modifier = Modifier.size(UiTokens.IconSmall))
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            AppSortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(appSortModeLabel(mode)) },
                                    onClick = {
                                        sortMode = mode
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(UiTokens.RadiusMedium),
                        border = BorderStroke(1.dp, AppVisualTokens.Border),
                        color = AppVisualTokens.Surface
                    ) {
                        Row(modifier = Modifier.padding(UiTokens.SpaceXSmall)) {
                            ViewToggleButton(
                                icon = Icons.Default.GridView,
                                selected = isGridView,
                                onClick = { viewModel.setGridView(true) }
                            )
                            ViewToggleButton(
                                icon = Icons.AutoMirrored.Filled.ViewList,
                                selected = !isGridView,
                                onClick = { viewModel.setGridView(false) }
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(UiTokens.RadiusLarge),
                color = AppVisualTokens.Surface,
                border = BorderStroke(1.dp, AppVisualTokens.Border)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = l10n("应用加载中...", "Loading apps..."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (appListEmptyReason(hasSelectedDevice, displayedList.isEmpty(), isLoading) != null) {
                    val emptyReason = appListEmptyReason(hasSelectedDevice, displayedList.isEmpty(), isLoading)
                    EmptyStatePanel(
                        title = when (emptyReason) {
                            AppListEmptyReason.NO_DEVICE -> l10n("未连接设备", "No device connected")
                            AppListEmptyReason.NO_RESULTS -> l10n("暂无匹配应用", "No matching apps")
                            null -> ""
                        },
                        description = when (emptyReason) {
                            AppListEmptyReason.NO_DEVICE -> l10n("连接并选择设备后可查看、搜索和管理应用。", "Connect and select a device to view, search, and manage apps.")
                            AppListEmptyReason.NO_RESULTS -> l10n("调整搜索词、筛选条件或刷新应用列表后再试。", "Adjust search, filters, or refresh the app list.")
                            null -> ""
                        },
                        icon = IconParkIcons.Application,
                        modifier = Modifier.fillMaxSize(),
                        actionLabel = when (emptyReason) {
                            AppListEmptyReason.NO_DEVICE -> null
                            AppListEmptyReason.NO_RESULTS -> l10n("刷新", "Refresh")
                            null -> null
                        },
                        onAction = when (emptyReason) {
                            AppListEmptyReason.NO_DEVICE -> null
                            AppListEmptyReason.NO_RESULTS -> ({ viewModel.getAppList(selectedDevice, forceRefresh = true) })
                            null -> null
                        }
                    )
                } else if (isGridView) {
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
                            columns = GridCells.Adaptive(152.dp),
                            state = gridState,
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
                            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
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
                } else {
                    val listState = rememberLazyListState()
                    LaunchedEffect(listState, displayedList) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo
                                .mapNotNull { item -> displayedList.getOrNull(item.index)?.packageName }
                        }.distinctUntilChanged().collect { visiblePackages ->
                            viewModel.ensureAppAssetsVisible(visiblePackages)
                        }
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall)) {
                        AppListColumnHeader()
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
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
                                    Spacer(modifier = Modifier.height(UiTokens.SpaceXSmall))
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
        AppFilter.ALL -> l10n("全部应用", "All apps")
        AppFilter.USER -> l10n("用户应用", "User apps")
        AppFilter.SYSTEM -> l10n("系统应用", "System apps")
        AppFilter.DEBUGGABLE -> l10n("可调试应用", "Debuggable")
        AppFilter.RECENT -> l10n("最近使用", "Recent")
        AppFilter.RUNNING -> l10n("运行中", "Running")
    }
}

@Composable
private fun AppStatBadge(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    val borderColor = if (emphasize) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val bgColor = if (emphasize) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    val valueColor = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AppTypeTag(
    text: String,
    accent: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        shape = RoundedCornerShape(UiTokens.RadiusSmall),
        color = accent.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall)
        )
    }
}

@Composable
private fun ViewToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(UiTokens.RadiusMedium))
            .background(if (selected) AppVisualTokens.Selected else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) AppVisualTokens.Primary else AppVisualTokens.Muted,
            modifier = Modifier.size(UiTokens.IconSmall)
        )
    }
}

private const val DOUBLE_CLICK_DELAY_NANOS = 500_000_000L

@Composable
private fun AppListColumnHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppColumnHeaderText(l10n("应用", "App"), Modifier.weight(2.25f))
        AppColumnHeaderText(l10n("类型", "Type"), Modifier.weight(1.25f))
        AppColumnHeaderText(l10n("版本", "Version"), Modifier.weight(0.85f))
        AppColumnHeaderText(l10n("大小", "Size"), Modifier.weight(0.75f))
        AppColumnHeaderText(l10n("状态", "Status"), Modifier.weight(0.9f))
        AppColumnHeaderText(l10n("操作", "Actions"), Modifier.weight(0.52f))
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
        color = AppVisualTokens.Muted,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun AppTableHeader(
    sortBySizeEnabled: Boolean,
    sortAscending: Boolean,
    onSizeSortToggle: () -> Unit,
    showPackageName: Boolean,
    onToggleNameDisplay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(2.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
        ) {
            Text(
                text = if (showPackageName) l10n("包名", "Package") else l10n("应用名称", "App name"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = l10n("切换显示", "Toggle display"),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(UiTokens.SpaceXSmall))
                    .clickable(onClick = onToggleNameDisplay)
                    .padding(UiTokens.SpaceXSmall)
            )
        }
        Text(
            text = l10n("版本", "Version"),
            modifier = Modifier.weight(1.0f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SortableHeaderLabel(
            text = l10n("大小", "Size"),
            modifier = Modifier.weight(1.0f),
            active = sortBySizeEnabled,
            ascending = sortAscending,
            onClick = onSizeSortToggle
        )
        Text(
            text = l10n("状态", "Status"),
            modifier = Modifier.weight(0.95f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = l10n("操作", "Actions"),
            modifier = Modifier.weight(1.7f),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SortableHeaderLabel(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean,
    ascending: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(UiTokens.RadiusSmall))
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = UiTokens.SpaceXSmall, vertical = UiTokens.SpaceXSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(UiTokens.SpaceXSmall))
        Icon(
            imageVector = if (ascending) Icons.Default.KeyboardArrowUp else IconParkIcons.ArrowDown,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isHovered) 0.9f else 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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

    Box(
        modifier = Modifier
            .hoverable(rowInteraction)
            .clip(RoundedCornerShape(UiTokens.RadiusMedium))
            .background(if (isRowHovered) AppVisualTokens.Soft else Color.Transparent)
            .pointerInput(app.packageName) {
                var lastPrimaryPressNanos = 0L
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type != PointerEventType.Press) continue
                        when (event.button) {
                            PointerButton.Secondary -> {
                                val point = event.changes.firstOrNull()?.position
                                if (point != null) {
                                    contextMenuOffset = with(density) { DpOffset(point.x.toDp(), point.y.toDp()) }
                                }
                                contextMenuExpanded = true
                            }
                            PointerButton.Primary -> {
                                val now = System.nanoTime()
                                if (now - lastPrimaryPressNanos < DOUBLE_CLICK_DELAY_NANOS) {
                                    onOpen()
                                    lastPrimaryPressNanos = 0L
                                } else {
                                    lastPrimaryPressNanos = now
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(2.25f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppAvatar(app = app, icon = icon, size = 32)
                Spacer(Modifier.width(UiTokens.SpaceSmall))
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)) {
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
                        style = MaterialTheme.typography.bodySmall,
                        color = AppVisualTokens.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1.25f),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTypeTag(text = if (app.isSystemApp) l10n("系统应用", "System") else l10n("用户应用", "User"))
                if (app.isDebuggable) {
                    AppTypeTag(text = l10n("可调试", "Debuggable"), accent = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                text = app.versionName.takeUnless { it.isBlank() || it == "-" } ?: l10n("待读取", "On demand"),
                modifier = Modifier
                    .weight(0.85f)
                    .padding(end = UiTokens.SpaceSmall),
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = app.size.takeUnless { it.isBlank() || it == "-" } ?: l10n("待读取", "On demand"),
                modifier = Modifier.weight(0.75f),
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(modifier = Modifier.weight(0.9f)) {
                val statusText = when {
                    app.isDisabled -> l10n("已禁用", "Disabled")
                    app.isRunning -> l10n("运行中", "Running")
                    else -> l10n("未运行", "Stopped")
                }
                val statusColor = when {
                    app.isDisabled -> QadbColors.textDisabled
                    app.isRunning -> AppVisualTokens.Success
                    else -> AppVisualTokens.Muted
                }
                Surface(
                    color = statusColor.copy(alpha = if (app.isRunning) 0.14f else 0.1f),
                    shape = RoundedCornerShape(UiTokens.BadgeRadius)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall)
                    )
                }
            }

            Box(
                modifier = Modifier.weight(0.52f),
                contentAlignment = Alignment.CenterStart
            ) {
                AppActionMenu(
                    app = app,
                    alpha = if (isRowHovered) 1f else 0.72f,
                    onAction = onAction,
                    onCopyPackageName = onCopyPackageName,
                    onRequestDangerAction = onRequestDangerAction
                )
            }
        }

        Box(modifier = Modifier.offset(x = contextMenuOffset.x, y = contextMenuOffset.y)) {
            DropdownMenu(
                expanded = contextMenuExpanded,
                onDismissRequest = { contextMenuExpanded = false },
                modifier = Modifier
                    .width(180.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(UiTokens.RadiusLarge)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        RoundedCornerShape(UiTokens.RadiusLarge)
                    )
                    .padding(vertical = UiTokens.SpaceXSmall)
            ) {
            AppActionMenuItem(
                icon = Icons.Default.ContentCopy,
                text = l10n("复制包名", "Copy package"),
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                contextMenuExpanded = false
                onCopyPackageName()
            }
            AppActionMenuItem(
                icon = IconParkIcons.Right,
                text = l10n("打开详情", "Open details"),
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.APP_INFO)
            }
            AppActionMenuItem(
                icon = Icons.Default.DeleteSweep,
                text = l10n("清除数据", "Clear data"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onRequestDangerAction(
                    AdbFunctionType.CLEAR_DATA,
                    l10n("确认清除应用数据", "Confirm clear app data"),
                    l10n("将清除 ${appName} 的本地数据，该操作不可撤销。", "This will clear local data of ${appName}. This action cannot be undone.")
                )
            }
            AppActionMenuItem(
                icon = IconParkIcons.Download,
                text = l10n("导出 APK", "Export APK"),
                iconTint = AppVisualTokens.Success
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.EXPORT_APK)
            }
            AppActionMenuItem(
                icon = Icons.Default.PrivacyTip,
                text = l10n("查看权限", "View permissions"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.APP_INFO)
            }
            AppActionMenuItem(
                icon = if (app.isDisabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                text = if (app.isDisabled) l10n("启用应用", "Enable app")
                else l10n("禁用应用", "Disable app"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onAction(if (app.isDisabled) AdbFunctionType.ENABLE_APP else AdbFunctionType.DISABLE_APP)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
            AppActionMenuItem(
                icon = Icons.Default.Delete,
                text = l10n("卸载应用", "Uninstall app"),
                iconTint = AppVisualTokens.Danger,
                textColor = AppVisualTokens.Danger
            ) {
                contextMenuExpanded = false
                onRequestDangerAction(
                    AdbFunctionType.UNINSTALL,
                    l10n("确认卸载应用", "Confirm uninstall"),
                    l10n("将从设备卸载 ${appName}，请确认继续。", "App ${appName} will be uninstalled from device. Continue?")
                )
            }
            }
        }
    }
}

@Composable
private fun RowActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary,
    alpha: Float = 1f,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(28.dp).graphicsLayer { this.alpha = alpha },
        border = BorderStroke(1.dp, tint.copy(alpha = if (enabled) 0.3f else 0.1f))
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(13.dp), tint = tint)
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

    Box {
        Surface(
            shape = RoundedCornerShape(UiTokens.RadiusMedium),
            border = BorderStroke(1.dp, AppVisualTokens.Border),
            color = AppVisualTokens.Surface,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer { this.alpha = alpha }
                .clickable { expanded = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = IconParkIcons.More,
                    contentDescription = l10n("更多操作", "More actions"),
                    tint = AppVisualTokens.Muted,
                    modifier = Modifier.size(UiTokens.IconSmall)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(180.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(UiTokens.RadiusLarge)
                )
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    RoundedCornerShape(UiTokens.RadiusLarge)
                )
                .padding(vertical = UiTokens.SpaceXSmall)
        ) {
            AppActionMenuItem(
                icon = Icons.Default.ContentCopy,
                text = l10n("复制包名", "Copy package"),
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                expanded = false
                onCopyPackageName()
            }
            AppActionMenuItem(
                icon = IconParkIcons.Right,
                text = l10n("打开详情", "Open details"),
                iconTint = MaterialTheme.colorScheme.primary
            ) {
                expanded = false
                onAction(AdbFunctionType.APP_INFO)
            }
            AppActionMenuItem(
                icon = if (app.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                text = if (app.isRunning) l10n("强行停止", "Force stop") else l10n("启动应用", "Launch app"),
                iconTint = if (app.isRunning) AppVisualTokens.Warning else AppVisualTokens.Success
            ) {
                expanded = false
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
                expanded = false
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
                expanded = false
                onAction(AdbFunctionType.EXPORT_APK)
            }
            AppActionMenuItem(
                icon = Icons.Default.PrivacyTip,
                text = l10n("查看权限", "View permissions"),
                iconTint = AppVisualTokens.Primary
            ) {
                expanded = false
                onAction(AdbFunctionType.APP_INFO)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
            AppActionMenuItem(
                icon = Icons.Default.Delete,
                text = l10n("卸载应用", "Uninstall app"),
                iconTint = AppVisualTokens.Danger,
                textColor = AppVisualTokens.Danger
            ) {
                expanded = false
                onRequestDangerAction(
                    AdbFunctionType.UNINSTALL,
                    l10n("确认卸载应用", "Confirm uninstall"),
                    l10n("将从设备卸载 $appName，请确认继续。", "App $appName will be uninstalled from device. Continue?")
                )
            }
        }
    }
}

@Composable
private fun AppActionMenuItem(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Surface(
                shape = RoundedCornerShape(UiTokens.RadiusMedium),
                color = iconTint.copy(alpha = 0.1f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(UiTokens.IconSmall))
                }
            }
        },
        contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall)
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppGridCard(
    app: AppInfo,
    icon: ImageBitmap?,
    onAction: (AdbFunctionType) -> Unit,
    onCopyPackageName: () -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit,
    onOpen: () -> Unit
) {
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier.pointerInput(app.packageName) {
            var lastPrimaryPressNanos = 0L
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type != PointerEventType.Press) continue
                    when (event.button) {
                        PointerButton.Secondary -> {
                            val point = event.changes.firstOrNull()?.position
                            if (point != null) {
                                contextMenuOffset = with(density) { DpOffset(point.x.toDp(), point.y.toDp()) }
                            }
                            contextMenuExpanded = true
                        }
                        PointerButton.Primary -> {
                            val now = System.nanoTime()
                            if (now - lastPrimaryPressNanos < DOUBLE_CLICK_DELAY_NANOS) {
                                onOpen()
                                lastPrimaryPressNanos = 0L
                            } else {
                                lastPrimaryPressNanos = now
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(UiTokens.RadiusMedium),
            color = AppVisualTokens.Surface,
            border = BorderStroke(1.dp, AppVisualTokens.Border)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 128.dp)
                .padding(UiTokens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppAvatar(app = app, icon = icon, size = 34)
                AppActionMenu(
                    app = app,
                    alpha = 0.86f,
                    onAction = onAction,
                    onCopyPackageName = onCopyPackageName,
                    onRequestDangerAction = onRequestDangerAction
                )
            }

            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppVisualTokens.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = AppVisualTokens.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(UiTokens.SpaceXSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTypeTag(text = if (app.isSystemApp) l10n("系统", "System") else l10n("用户", "User"))
                Spacer(modifier = Modifier.weight(1f))
                if (app.isRunning) {
                    AppTypeTag(text = l10n("运行中", "Running"), accent = AppVisualTokens.Success)
                }
            }
        }
        }

        Box(modifier = Modifier.offset(x = contextMenuOffset.x, y = contextMenuOffset.y)) {
            DropdownMenu(
                expanded = contextMenuExpanded,
                onDismissRequest = { contextMenuExpanded = false },
                modifier = Modifier
                    .width(180.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(UiTokens.RadiusLarge)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        RoundedCornerShape(UiTokens.RadiusLarge)
                    )
                    .padding(vertical = UiTokens.SpaceXSmall)
            ) {
            AppActionMenuItem(
                icon = Icons.Default.ContentCopy,
                text = l10n("复制包名", "Copy package"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onCopyPackageName()
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
            AppActionMenuItem(
                icon = IconParkIcons.Info,
                text = l10n("应用详情", "App details"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.APP_INFO)
            }
            AppActionMenuItem(
                icon = Icons.Default.PlayArrow,
                text = l10n("启动应用", "Launch app"),
                iconTint = AppVisualTokens.Success
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.LAUNCH)
            }
            AppActionMenuItem(
                icon = Icons.Default.Stop,
                text = l10n("停止应用", "Stop app"),
                iconTint = AppVisualTokens.Warning
            ) {
                contextMenuExpanded = false
                onRequestDangerAction(
                    AdbFunctionType.FORCE_STOP,
                    l10n("确认强行停止应用", "Confirm force stop"),
                    l10n("将强行停止 ${app.appName}。", "App ${app.appName} will be force stopped.")
                )
            }
            AppActionMenuItem(
                icon = Icons.Default.DeleteSweep,
                text = l10n("清除数据", "Clear data"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onRequestDangerAction(
                    AdbFunctionType.CLEAR_DATA,
                    l10n("确认清除应用数据", "Confirm clear app data"),
                    l10n("将清除 ${app.appName} 的本地数据，该操作不可撤销。", "This will clear local data of ${app.appName}. This action cannot be undone.")
                )
            }
            AppActionMenuItem(
                icon = IconParkIcons.Download,
                text = l10n("导出 APK", "Export APK"),
                iconTint = AppVisualTokens.Success
            ) {
                contextMenuExpanded = false
                onAction(AdbFunctionType.EXPORT_APK)
            }
            AppActionMenuItem(
                icon = if (app.isDisabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                text = if (app.isDisabled) l10n("启用应用", "Enable app")
                else l10n("禁用应用", "Disable app"),
                iconTint = AppVisualTokens.Primary
            ) {
                contextMenuExpanded = false
                onAction(if (app.isDisabled) AdbFunctionType.ENABLE_APP else AdbFunctionType.DISABLE_APP)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
            AppActionMenuItem(
                icon = Icons.Default.Delete,
                text = l10n("卸载应用", "Uninstall app"),
                iconTint = AppVisualTokens.Danger,
                textColor = AppVisualTokens.Danger
            ) {
                contextMenuExpanded = false
                onRequestDangerAction(
                    AdbFunctionType.UNINSTALL,
                    l10n("确认卸载应用", "Confirm uninstall"),
                    l10n("将从设备卸载 ${app.appName}，请确认继续。", "App ${app.appName} will be uninstalled from device. Continue?")
                )
            }
            }
        }
    }
}

@Composable
private fun AppListFooter(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = UiTokens.SpaceSmall, start = UiTokens.SpaceXSmall, end = UiTokens.SpaceSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = l10n("共 $count 个应用", "$count apps"),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted
        )
        Text(
            text = l10n("右键点击可快速操作", "Right click for quick actions"),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted.copy(alpha = 0.62f)
        )
    }
}

@Composable
private fun AppDetailPage(
    appInfo: AppInfoData,
    icon: ImageBitmap?,
    onBack: () -> Unit,
    onAction: (AdbFunctionType) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) },
    modifier: Modifier = Modifier
) {
    val detailTabs = listOf(
        DetailTab(l10n("概览", "Overview")),
        DetailTab(l10n("权限", "Permissions")),
        DetailTab(l10n("Activity", "Activity")),
        DetailTab(l10n("Service", "Service")),
        DetailTab(l10n("Receiver", "Receiver")),
        DetailTab(l10n("Provider", "Provider")),
        DetailTab(l10n("签名", "Signature"))
    )
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = l10n("返回", "Back"), tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = l10n("应用详情", "App details"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            DetailHeaderCard(
                appInfo = appInfo,
                icon = icon,
                onAction = onAction,
                onRequestDangerAction = onRequestDangerAction
            )

            val tabListState = rememberLazyListState()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                LazyRow(
                    state = tabListState,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(detailTabs) { tab ->
                        DetailTabItem(
                            tab = tab,
                            selected = selectedTab == tab.title,
                            onClick = { selectedTab = tab.title }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            }

            when (selectedTab) {
                l10n("概览", "Overview") -> AppInfoDetailContent(appInfo)
                l10n("权限", "Permissions") -> PermissionDetailContent(appInfo)
                l10n("Activity", "Activity") -> ActivityDetailContent(appInfo)
                l10n("Service", "Service") -> ServiceDetailContent(appInfo)
                l10n("Receiver", "Receiver") -> ReceiverDetailContent(appInfo)
                l10n("Provider", "Provider") -> ProviderDetailContent(appInfo)
                l10n("签名", "Signature") -> SignatureDetailContent(appInfo)
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

private data class DetailTab(
    val title: String
)

@Composable
private fun DetailHeaderCard(
    appInfo: AppInfoData,
    icon: ImageBitmap?,
    onAction: (AdbFunctionType) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        color = AppVisualTokens.Surface,
        border = BorderStroke(1.dp, AppVisualTokens.Border)
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
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXLarge),
                    verticalAlignment = Alignment.Top
                ) {
                    AppAvatar(
                        app = AppInfo(appName = appInfo.appName.ifBlank { appInfo.packageName }, packageName = appInfo.packageName),
                        icon = icon,
                        size = 64
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                        ) {
                            Text(
                                text = appInfo.appName.ifBlank { appInfo.packageName },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppVisualTokens.Text,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            StatusPill(appInfo.isRunning)
                        }
                        DetailHeaderRow(l10n("包名", "Package"), appInfo.packageName)
                        DetailHeaderRow(l10n("版本", "Version"), listOf(appInfo.versionName, appInfo.versionCode.takeIf { it != "-" }?.let { "($it)" }).filterNotNull().joinToString(" "))
                        DetailHeaderRow(l10n("类型", "Type"), if (appInfo.isSystemApp) l10n("系统应用", "System app") else l10n("用户应用", "User app"))
                        DetailHeaderRow(l10n("更新于", "Updated"), appInfo.lastUpdateTime)
                        DetailHeaderRow(l10n("安装路径", "Install path"), appInfo.apkPath.ifBlank { "-" }, maxLines = 2)
                    }
                }

                HeaderStorageUsageCard(
                    appInfo = appInfo,
                    modifier = Modifier.widthIn(min = 248.dp, max = 280.dp)
                )
            }

            DetailActionsPanel(
                appInfo = appInfo,
                onAction = onAction,
                onRequestDangerAction = onRequestDangerAction
            )
        }
    }
}

@Composable
private fun DetailHeaderRow(
    label: String,
    value: String,
    maxLines: Int = 1
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.width(84.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted
        )
        SelectionContainer {
            SelectableValueText(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            text = l10n("存储占用", "Storage"),
            style = MaterialTheme.typography.labelMedium,
            color = AppVisualTokens.Muted,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StorageRing(total = appInfo.totalSize, size = 72.dp, innerSize = 50.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                StorageLegend(AppVisualTokens.Primary, l10n("应用大小", "App size"), appInfo.appSize)
                StorageLegend(AppVisualTokens.Muted, l10n("应用数据", "App data"), appInfo.dataSize)
                StorageLegend(AppVisualTokens.Muted, l10n("缓存数据", "Cache"), appInfo.cacheSize)
                HorizontalDivider(color = AppVisualTokens.Divider)
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
                        text = appInfo.totalSize,
                        style = MaterialTheme.typography.bodySmall,
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
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        DetailActionButton(
            icon = if (appInfo.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            text = if (appInfo.isRunning) l10n("停止应用", "Stop app") else l10n("启动应用", "Launch app"),
            tint = QadbColors.onPrimary,
            bgColor = AppVisualTokens.Primary,
            modifier = Modifier.width(140.dp)
        ) {
            onAction(if (appInfo.isRunning) AdbFunctionType.FORCE_STOP else AdbFunctionType.LAUNCH)
        }
        DetailActionButton(
            icon = IconParkIcons.Refresh,
            text = l10n("刷新", "Refresh"),
            tint = AppVisualTokens.Text,
            bgColor = AppVisualTokens.Surface,
            modifier = Modifier.width(110.dp)
        ) { onAction(AdbFunctionType.APP_INFO) }
        DetailMoreActionMenu(onAction = onAction, onRequestDangerAction = onRequestDangerAction)
    }
}

@Composable
private fun StatusPill(isRunning: Boolean) {
    val color = if (isRunning) AppVisualTokens.Success else AppVisualTokens.Muted
    Surface(
        color = if (isRunning) QadbColors.successSurface else color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(UiTokens.BadgeRadius)
    ) {
        Text(
            text = if (isRunning) l10n("运行中", "Running") else l10n("未运行", "Stopped"),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceXSmall)
        )
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
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(UiTokens.ControlHeight),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = if (enabled) bgColor else AppVisualTokens.Soft,
        border = BorderStroke(
            1.dp,
            if (bgColor == AppVisualTokens.Surface) AppVisualTokens.Border
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
                modifier = Modifier.size(UiTokens.IconSmall)
            )
            Spacer(Modifier.width(UiTokens.SpaceSmall))
            Text(
                text,
                color = if (enabled) tint else AppVisualTokens.Muted.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DetailMoreActionMenu(
    onAction: (AdbFunctionType) -> Unit,
    onRequestDangerAction: (AdbFunctionType, String, String) -> Unit = { type, _, _ -> onAction(type) }
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        DetailActionButton(
            icon = IconParkIcons.More,
            text = l10n("更多操作", "More"),
            tint = AppVisualTokens.Text,
            bgColor = AppVisualTokens.Surface,
            modifier = Modifier.width(124.dp)
        ) { expanded = true }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(190.dp)
                .background(AppVisualTokens.Surface, RoundedCornerShape(UiTokens.RadiusMedium))
                .border(BorderStroke(1.dp, AppVisualTokens.Border), RoundedCornerShape(UiTokens.RadiusMedium))
        ) {
            AppActionMenuItem(
                icon = Icons.Default.DeleteSweep,
                text = l10n("清除数据", "Clear data"),
                iconTint = AppVisualTokens.Muted
            ) {
                expanded = false
                onRequestDangerAction(
                    AdbFunctionType.CLEAR_DATA,
                    l10n("确认清除数据", "Confirm Clear Data"),
                    l10n("将清除该应用的全部数据，此操作不可撤销。是否继续？", "This will clear all app data. This cannot be undone. Continue?")
                )
            }
            AppActionMenuItem(
                icon = IconParkIcons.Download,
                text = l10n("导出 APK", "Export APK"),
                iconTint = AppVisualTokens.Muted
            ) { expanded = false; onAction(AdbFunctionType.EXPORT_APK) }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceXSmall),
                color = AppVisualTokens.Divider
            )
            AppActionMenuItem(
                icon = Icons.Default.Delete,
                text = l10n("卸载应用", "Uninstall app"),
                iconTint = AppVisualTokens.Danger,
                textColor = AppVisualTokens.Danger
            ) {
                expanded = false
                onRequestDangerAction(
                    AdbFunctionType.UNINSTALL,
                    l10n("确认卸载", "Confirm Uninstall"),
                    l10n("将卸载此应用，应用数据将被清除。是否继续？", "This app will be uninstalled and its data will be erased. Continue?")
                )
            }
        }
    }
}

@Composable
private fun DetailTabItem(
    tab: DetailTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) AppVisualTokens.Text else AppVisualTokens.Muted
    Column(
        modifier = Modifier
            .widthIn(min = 92.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (tab.title) {
                "概览" -> l10n("概览", "Overview")
                "权限" -> l10n("权限", "Permissions")
                "签名" -> l10n("签名", "Signature")
                else -> tab.title
            },
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall)
        )
        Box(
            modifier = Modifier
                .height(2.5.dp)
                .fillMaxWidth()
                .background(if (selected) AppVisualTokens.Primary else Color.Transparent)
        )
    }
}

@Composable
private fun AppInfoDetailContent(appInfo: AppInfoData) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 980.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                BasicInfoCard(appInfo)
                ProcessInfoCard(appInfo)
                PermissionSummaryCard(appInfo)
                OperationLogCard(appInfo)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceLarge)) {
                    BasicInfoCard(appInfo, Modifier.weight(1.05f))
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
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(UiTokens.RadiusLarge),
        color = AppVisualTokens.Surface,
        border = BorderStroke(1.dp, AppVisualTokens.Border)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(UiTokens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppVisualTokens.Text
            )
            content()
        }
    }
}

@Composable
private fun BasicInfoCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    val infoItems = listOf(
        l10n("应用名称", "App name") to appInfo.appName.ifBlank { appInfo.packageName },
        l10n("包名", "Package") to appInfo.packageName,
        l10n("版本名称", "Version name") to appInfo.versionName,
        l10n("版本号", "Version code") to appInfo.versionCode,
        l10n("最小 SDK", "Min SDK") to appInfo.minSdk,
        l10n("目标 SDK", "Target SDK") to appInfo.targetSdk,
        l10n("安装位置", "Install location") to appInfo.installLocation,
        l10n("应用类型", "App type") to if (appInfo.isSystemApp) l10n("系统应用", "System app") else l10n("用户应用", "User app"),
        l10n("应用大小", "App size") to appInfo.appSize,
        l10n("数据目录", "Data dir") to appInfo.dataDir,
        l10n("APK 路径", "APK path") to appInfo.apkPath
    )
    DetailSectionCard(title = l10n("基本信息", "Basic info"), modifier = modifier) {
        DetailInfoGrid(items = infoItems)
    }
}

@Composable
private fun DetailInfoGrid(items: List<Pair<String, String>>) {
    val rows = items.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXLarge)
            ) {
                row.forEach { (label, value) ->
                    DetailInfoGridCell(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (index != rows.lastIndex) {
                HorizontalDivider(color = AppVisualTokens.Divider)
            }
        }
    }
}

@Composable
private fun DetailInfoGridCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 42.dp)
            .padding(vertical = UiTokens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppVisualTokens.Muted
        )
        SelectionContainer {
            SelectableValueText(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StorageInfoCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    DetailSectionCard(title = l10n("存储占用", "Storage"), modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StorageRing(total = appInfo.totalSize)
            Column(
                modifier = Modifier.weight(1f).padding(start = UiTokens.SpaceXLarge),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                StorageLegend(AppVisualTokens.Primary, l10n("应用大小", "App size"), appInfo.appSize)
                StorageLegend(AppVisualTokens.Success, l10n("应用数据", "App data"), appInfo.dataSize)
                StorageLegend(AppVisualTokens.Warning, l10n("缓存数据", "Cache"), appInfo.cacheSize)
                HorizontalDivider(color = AppVisualTokens.Divider)
                DetailInfoRow(l10n("总计", "Total"), appInfo.totalSize)
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
                    .padding(horizontal = if (compact) 3.dp else 6.dp),
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
        Text(value, style = MaterialTheme.typography.bodySmall, color = AppVisualTokens.Text)
    }
}

@Composable
private fun ProcessInfoCard(appInfo: AppInfoData, modifier: Modifier = Modifier) {
    DetailSectionCard(title = l10n("进程信息", "Process info"), modifier = modifier) {
        DetailInfoRow(l10n("主进程", "Main process"), appInfo.packageName)
        DetailInfoRow(l10n("进程 ID (PID)", "Process ID (PID)"), appInfo.processId)
        DetailInfoRow(l10n("内存占用", "Memory usage"), appInfo.memoryUsage)
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
            PermissionTile(l10n("全部权限", "All"), appInfo.totalPermissionCount, IconParkIcons.Application, AppVisualTokens.Muted, Modifier.weight(1f))
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
    Surface(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(UiTokens.RadiusMedium),
        color = AppVisualTokens.Surface,
        border = BorderStroke(1.dp, AppVisualTokens.Border)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = UiTokens.SpaceSmall, vertical = UiTokens.SpaceSmall),
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
        OperationRow(l10n("安装应用", "Install app"), appInfo.firstInstallTime)
        OperationRow(l10n("更新应用", "Update app"), appInfo.lastUpdateTime)
        OperationRow(l10n("读取详情", "Read details"), l10n("当前会话", "Current session"))
        OperationRow(l10n("运行状态", "Run status"), if (appInfo.isRunning) l10n("运行中", "Running") else l10n("未运行", "Stopped"))
    }
}

@Composable
private fun OperationRow(action: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(action, style = MaterialTheme.typography.bodySmall, color = AppVisualTokens.Text)
        Text(time, style = MaterialTheme.typography.bodySmall, color = AppVisualTokens.Muted)
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted
        )
        SelectionContainer(modifier = Modifier.weight(1f)) {
            SelectableValueText(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PermissionDetailContent(appInfo: AppInfoData) {
    var selectedFilter by remember { mutableStateOf("全部") }

    val dangerousKeywords = listOf(
        "CAMERA", "LOCATION", "RECORD_AUDIO", "CONTACTS", "SMS", "PHONE",
        "CALENDAR", "BODY_SENSORS", "STORAGE", "BLUETOOTH"
    )
    val privacyKeywords = listOf("CAMERA", "LOCATION", "RECORD_AUDIO", "CONTACTS", "SMS", "PHONE")

    val filteredPermissions = remember(appInfo.permissionDetails, selectedFilter) {
        when (selectedFilter) {
            "危险权限" -> appInfo.permissionDetails.filter { permission ->
                dangerousKeywords.any { keyword -> permission.contains(keyword) }
            }
            "隐私权限" -> appInfo.permissionDetails.filter { permission ->
                privacyKeywords.any { keyword -> permission.contains(keyword) }
            }
            "普通权限" -> appInfo.permissionDetails.filter { permission ->
                dangerousKeywords.none { keyword -> permission.contains(keyword) }
            }
            else -> appInfo.permissionDetails
        }
    }

    DetailSectionCard(title = l10n("权限信息", "Permission info"), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = l10n(
                "已解析到 ${appInfo.totalPermissionCount} 项权限，其中 ${appInfo.dangerousPermissionCount} 项可能涉及敏感能力。",
                "Detected ${appInfo.totalPermissionCount} permissions, ${appInfo.dangerousPermissionCount} of them may be sensitive."
            ),
            style = MaterialTheme.typography.bodySmall,
            color = AppVisualTokens.Muted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)) {
            PermissionTile(l10n("危险权限", "Dangerous"), appInfo.dangerousPermissionCount, Icons.Default.GppMaybe, AppVisualTokens.Danger, Modifier.width(132.dp))
            PermissionTile(l10n("隐私权限", "Privacy"), appInfo.privacyPermissionCount, Icons.Default.Lock, AppVisualTokens.Primary, Modifier.width(132.dp))
            PermissionTile(l10n("普通权限", "Normal"), appInfo.normalPermissionCount, IconParkIcons.Info, AppVisualTokens.Muted, Modifier.width(132.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("全部", "危险权限", "隐私权限", "普通权限").forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                    color = if (selected) AppVisualTokens.Selected else AppVisualTokens.Surface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) QadbColors.selectedBorder else AppVisualTokens.Border
                    ),
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = when (filter) {
                            "全部" -> l10n("全部", "All")
                            "危险权限" -> l10n("危险权限", "Dangerous")
                            "隐私权限" -> l10n("隐私权限", "Privacy")
                            "普通权限" -> l10n("普通权限", "Normal")
                            else -> filter
                        },
                        modifier = Modifier.padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) AppVisualTokens.Primary else AppVisualTokens.Muted,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        DetailItemList(
            title = l10n("权限明细 (${filteredPermissions.size})", "Permissions (${filteredPermissions.size})"),
            items = filteredPermissions,
            emptyText = l10n("当前筛选条件下无权限", "No permissions under current filter")
        )
    }
}

@Composable
private fun ActivityDetailContent(appInfo: AppInfoData) {
    DetailSectionCard(title = l10n("活动（Activity）", "Activities"), modifier = Modifier.fillMaxWidth()) {
        DetailItemList(
            title = l10n("Activity 列表", "Activity List"),
            items = appInfo.activityDetails,
            emptyText = l10n("当前未解析到 Activity 信息", "No Activity information parsed")
        )
    }
}

@Composable
private fun ServiceDetailContent(appInfo: AppInfoData) {
    DetailSectionCard(title = l10n("服务（Service）", "Services"), modifier = Modifier.fillMaxWidth()) {
        DetailItemList(
            title = l10n("Service 列表", "Service List"),
            items = appInfo.serviceDetails,
            emptyText = l10n("当前未解析到 Service 信息", "No Service information parsed")
        )
    }
}

@Composable
private fun ReceiverDetailContent(appInfo: AppInfoData) {
    DetailSectionCard(title = l10n("广播接收器", "Receivers"), modifier = Modifier.fillMaxWidth()) {
        DetailItemList(
            title = l10n("Receiver 列表", "Receiver List"),
            items = appInfo.receiverDetails,
            emptyText = l10n("当前未解析到 Receiver 信息", "No Receiver information parsed")
        )
    }
}

@Composable
private fun ProviderDetailContent(appInfo: AppInfoData) {
    DetailSectionCard(title = l10n("内容提供者", "Providers"), modifier = Modifier.fillMaxWidth()) {
        DetailItemList(
            title = l10n("Provider 列表", "Provider List"),
            items = appInfo.providerDetails,
            emptyText = l10n("当前未解析到 Provider 信息", "No Provider information parsed")
        )
    }
}

@Composable
private fun SignatureDetailContent(appInfo: AppInfoData) {
    DetailSectionCard(title = l10n("签名信息", "Signature"), modifier = Modifier.fillMaxWidth()) {
        DetailItemList(
            title = l10n("签名相关", "Signature details"),
            items = appInfo.signatureDetails,
            emptyText = l10n("当前未解析到签名信息", "No signature information parsed"),
            maxLines = 4
        )
    }
}

@Composable
private fun DetailItemList(
    title: String,
    items: List<String>,
    emptyText: String,
    maxLines: Int = 2
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = AppVisualTokens.Text
    )
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = UiTokens.SpaceLarge),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = AppVisualTokens.Muted
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(UiTokens.RadiusMedium),
            color = AppVisualTokens.Surface,
            border = BorderStroke(1.dp, AppVisualTokens.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall)
            ) {
                items.forEachIndexed { index, item ->
                    SelectionContainer {
                        SelectableValueText(
                            text = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = UiTokens.SpaceSmall),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppVisualTokens.Text,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = AppVisualTokens.Divider)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableValueText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    maxLines: Int,
    overflow: TextOverflow,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow
    )
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
