package com.ludoven.adbtool.pages

import com.ludoven.adbtool.UiTokens
import com.ludoven.adbtool.QadbColors
import com.ludoven.adbtool.QadbTokens
import com.ludoven.adbtool.ui.icons.IconParkIcons
import com.ludoven.adbtool.ui.mac.*
import com.ludoven.adbtool.entity.FileInfo
import com.ludoven.adbtool.entity.FileSortBy
import com.ludoven.adbtool.entity.FileSortOrder
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.l10n
import com.ludoven.adbtool.viewmodel.FileBrowserViewModel
import com.ludoven.adbtool.widget.FeedbackToast
import com.ludoven.adbtool.widget.FileEmptyKind
import com.ludoven.adbtool.widget.FileEmptyState
import com.ludoven.adbtool.widget.FramedStateSurface
import com.ludoven.adbtool.widget.DeviceRequiredState
import com.ludoven.adbtool.widget.PageHeader
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.datatransfer.DataFlavor
import java.io.File
import kotlin.math.max
import org.jetbrains.compose.resources.stringResource

internal fun fileBrowserDeviceActionsEnabled(selectedDevice: String?): Boolean =
    !selectedDevice.isNullOrBlank()

internal fun fileBrowserAvailableSpaceCommand(path: String): String =
    AdbTool.buildShellCommand("df", "-h", path)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel,
    selectedDevice: String?,
    onRefreshDevices: () -> Unit,
    onOpenWirelessConnection: () -> Unit,
    onOpenTroubleshooting: () -> Unit
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val files by viewModel.files.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorText by viewModel.errorText.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val showHidden by viewModel.showHidden.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val clipboardMode by viewModel.clipboardMode.collectAsState()
    val clipboardFiles by viewModel.clipboardFiles.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val toastMessage by viewModel.feedbackToastMessage.collectAsState()
    val deviceActionsEnabled = fileBrowserDeviceActionsEnabled(selectedDevice)

    FeedbackToast(toastMessage)

    val filteredFiles by remember {
        derivedStateOf {
            val base = if (showHidden) files else files.filter { !it.name.startsWith(".") }
            val keyword = searchKeyword.trim()
            if (keyword.isBlank()) base else base.filter { it.name.contains(keyword, ignoreCase = true) }
        }
    }
    val listState = rememberLazyListState()

    // Dialog states
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FileInfo?>(null) }
    var renameText by remember { mutableStateOf("") }
    var showNewDirDialog by remember { mutableStateOf(false) }
    var newDirName by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteTargets by remember { mutableStateOf<List<String>>(emptyList()) }

    // Context menu state
    var contextMenuTarget by remember { mutableStateOf<FileInfo?>(null) }
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var fileListRootPx by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    var showAdvancedFields by remember { mutableStateOf(false) }
    var selectedPaths by remember { mutableStateOf(setOf<String>()) }
    var toolbarMenuExpanded by remember { mutableStateOf(false) }

    // Drag-and-drop (upload local files)
    var isDragActive by remember { mutableStateOf(false) }
    val dragTarget = remember(selectedDevice) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) { isDragActive = true }
            override fun onEntered(event: DragAndDropEvent) { isDragActive = true }
            override fun onExited(event: DragAndDropEvent) { isDragActive = false }
            override fun onEnded(event: DragAndDropEvent) { isDragActive = false }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragActive = false
                val transferable = event.awtTransferable
                if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return false
                val fileList = runCatching {
                    transferable.getTransferData(DataFlavor.javaFileListFlavor)
                }.getOrNull()
                val paths = (fileList as? List<*>)?.mapNotNull { (it as? File)?.absolutePath } ?: emptyList()
                if (paths.isEmpty()) return false
                viewModel.pushFiles(paths, selectedDevice)
                return true
            }
        }
    }

    val availableSpace by produceState(initialValue = "--", currentPath, selectedDevice) {
        value = "--"
        if (!selectedDevice.isNullOrBlank()) {
            runCatching {
                val output = AdbTool.execShellAsync(fileBrowserAvailableSpaceCommand(currentPath), selectedDevice)
                if (output.success) {
                    output.output.lines().lastOrNull { it.trim().isNotEmpty() && !it.contains("Filesystem", true) }
                        ?.split(Regex("\\s+"))
                        ?.let { if (it.size >= 4) it[it.size - 3] else "--" }
                        ?: "--"
                } else "--"
            }.onSuccess { value = it }
        }
    }

    LaunchedEffect(selectedDevice) {
        viewModel.loadFiles(deviceId = selectedDevice)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelActiveLoad()
        }
    }

    // Dialogs
    if (showRenameDialog && renameTarget != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(l10n("重命名", "Rename")) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(l10n("新名称", "New name")) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameFile(
                        "${currentPath}/${renameTarget!!.name}",
                        renameText,
                        selectedDevice
                    )
                    showRenameDialog = false
                }) { Text(l10n("确定", "Confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text(l10n("取消", "Cancel")) }
            }
        )
    }

    if (showNewDirDialog) {
        AlertDialog(
            onDismissRequest = { showNewDirDialog = false },
            title = { Text(l10n("新建文件夹", "New folder")) },
            text = {
                OutlinedTextField(
                    value = newDirName,
                    onValueChange = { newDirName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(l10n("文件夹名称", "Folder name")) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createDirectory(newDirName, selectedDevice)
                    showNewDirDialog = false
                    newDirName = ""
                }) { Text(l10n("创建", "Create")) }
            },
            dismissButton = {
                TextButton(onClick = { showNewDirDialog = false; newDirName = "" }) { Text(l10n("取消", "Cancel")) }
            }
        )
    }

    if (showDeleteConfirm && deleteTargets.isNotEmpty()) {
        val count = deleteTargets.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(l10n("确认删除", "Confirm delete")) },
            text = {
                if (count == 1) {
                    val name = deleteTargets.first().substringAfterLast('/')
                    Text(l10n("确定要删除 \"$name\" 吗？此操作不可撤销。", "Delete \"$name\"? This action cannot be undone."))
                } else {
                    Text(l10n("确定要删除选中的 $count 个项目吗？此操作不可撤销。", "Delete $count selected items? This action cannot be undone."))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val targets = deleteTargets
                    viewModel.deleteFiles(targets, selectedDevice)
                    selectedPaths = selectedPaths - targets.toSet()
                    deleteTargets = emptyList()
                    showDeleteConfirm = false
                }) { Text(l10n("删除", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(l10n("取消", "Cancel")) }
            }
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = UiTokens.PagePadding, vertical = UiTokens.SectionSpacingCompact),
        verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceMedium)
    ) {
        // ── Header: Title & Actions ───────────────────────────────────────────
        PageHeader(
            title = l10n("文件管理", "File Manager"),
            subtitle = l10n("浏览和管理设备文件", "Browse and manage device files")
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Upload
                Button(
                    onClick = { viewModel.pushFile(selectedDevice) },
                    enabled = deviceActionsEnabled,
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = QadbColors.onPrimary
                    ),
                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                    contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 0.dp)
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(UiTokens.IconSmall),
                        tint = QadbColors.onPrimary
                    )
                    Spacer(Modifier.width(UiTokens.SpaceXSmall))
                    Text(
                        l10n("上传", "Upload"),
                        style = MaterialTheme.typography.labelLarge,
                        color = QadbColors.onPrimary
                    )
                }

                // Secondary Action: New Folder
                OutlinedButton(
                    onClick = { showNewDirDialog = true },
                    enabled = deviceActionsEnabled,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(UiTokens.RadiusMedium),
                    contentPadding = PaddingValues(horizontal = UiTokens.SpaceMedium, vertical = 0.dp)
                ) {
                    Icon(
                        IconParkIcons.FolderAdd,
                        contentDescription = null,
                        modifier = Modifier.size(UiTokens.IconSmall)
                    )
                    Spacer(Modifier.width(UiTokens.SpaceXSmall))
                    Text(
                        l10n("新建文件夹", "New folder"),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Tool Action: More
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                            .clickable { toolbarMenuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            IconParkIcons.More,
                            contentDescription = l10n("更多", "More"),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = toolbarMenuExpanded,
                        onDismissRequest = { toolbarMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (showAdvancedFields) l10n("隐藏高级字段", "Hide advanced columns") else l10n("显示高级字段", "Show advanced columns")) },
                            onClick = {
                                showAdvancedFields = !showAdvancedFields
                                toolbarMenuExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Tune, null, modifier = Modifier.size(UiTokens.IconSmall)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (showHidden) l10n("隐藏隐藏文件", "Hide hidden files") else l10n("显示隐藏文件", "Show hidden files")) },
                            onClick = {
                                viewModel.toggleShowHidden()
                                toolbarMenuExpanded = false
                            },
                            leadingIcon = { Icon(if (showHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(UiTokens.IconSmall)) }
                        )
                    }
                }
            }
        }

        if (selectedDevice.isNullOrBlank()) {
            DeviceRequiredState(
                title = l10n("未选择设备", "No device selected"),
                description = l10n("请先连接并选择设备后查看文件。", "Connect and select a device before browsing files."),
                onRefreshDevices = onRefreshDevices,
                onOpenWirelessConnection = onOpenWirelessConnection,
                onOpenTroubleshooting = onOpenTroubleshooting,
                modifier = Modifier.fillMaxSize()
            )
            return
        }

        // ── Navigation Toolbar ────────────────────────────────────────────────
        NavigationToolbar(
            currentPath = currentPath,
            canGoBack = viewModel.canGoBack(),
            canGoForward = viewModel.canGoForward(),
            searchKeyword = searchKeyword,
            onBack = { viewModel.goBack(selectedDevice) },
            onForward = { viewModel.goForward(selectedDevice) },
            onUp = { viewModel.navigateUp(selectedDevice) },
            onRefresh = { viewModel.loadFiles(deviceId = selectedDevice, forceRefresh = true) },
            onNavigateToPath = { viewModel.navigateTo(it, selectedDevice) },
            onSearchChange = { viewModel.setSearchKeyword(it) },
        )

        // ── Quick Path Bar ────────────────────────────────────────────────────
        QuickPathBar(
            currentPath = currentPath,
            onNavigate = { viewModel.navigateTo(it, selectedDevice) }
        )

        // ── Sorting & Count Row ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Text(
                    text = l10n("${filteredFiles.size} 项", "${filteredFiles.size} items"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = UiTokens.TextBody
                )
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SortChip(l10n("名称", "Name"), sortBy == FileSortBy.NAME, sortOrder) { viewModel.setSortBy(FileSortBy.NAME) }
                SortChip(l10n("大小", "Size"), sortBy == FileSortBy.SIZE, sortOrder) { viewModel.setSortBy(FileSortBy.SIZE) }
                SortChip(l10n("日期", "Date"), sortBy == FileSortBy.DATE, sortOrder) { viewModel.setSortBy(FileSortBy.DATE) }
            }
        }

        // ── Multi-selection Action Bar (Conditional) ──────────────────────────
        if (selectedPaths.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.70f), RoundedCornerShape(UiTokens.RadiusMedium))
                    .padding(horizontal = UiTokens.SpaceMedium, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = l10n("已选 ${selectedPaths.size} 项", "${selectedPaths.size} selected"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedPaths.forEach { viewModel.pullFile(it, selectedDevice) }
                        },
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(UiTokens.RadiusSmall),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(IconParkIcons.Download, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(l10n("下载", "Download"), style = MaterialTheme.typography.labelSmall)
                    }
                    if (selectedPaths.size == 1) {
                        OutlinedButton(
                            onClick = {
                                selectedPaths.firstOrNull()?.let {
                                    val f = filteredFiles.firstOrNull { file -> "$currentPath/${file.name}" == it }
                                    if (f != null) {
                                        renameTarget = f
                                        renameText = f.name
                                        showRenameDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(UiTokens.RadiusSmall),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(l10n("重命名", "Rename"), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            val copied = copyToClipboardText(selectedPaths.joinToString("\n"))
                            viewModel.showToast(
                                MsgContent.Text(if (copied) l10n("已复制 ${selectedPaths.size} 个路径", "Copied ${selectedPaths.size} paths") else l10n("复制失败", "Copy failed"))
                            )
                        },
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(UiTokens.RadiusSmall),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(l10n("复制路径", "Copy path"), style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = {
                            if (selectedPaths.isNotEmpty()) {
                                deleteTargets = selectedPaths.toList()
                                showDeleteConfirm = true
                            }
                        },
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(UiTokens.RadiusSmall),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(l10n("删除", "Delete"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // ── File List Area (FramedStateSurface) ────────────────────────────────
        FramedStateSurface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .dragAndDropTarget(shouldStartDragAndDrop = { !selectedDevice.isNullOrBlank() && it.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor) }, target = dragTarget)
        ) {
            Box(Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    errorText != null -> {
                        FileEmptyState(
                            kind = FileEmptyKind.EmptyFolder,
                            title = l10n("目录读取失败", "Failed to load folder"),
                            description = errorText.orEmpty(),
                            actionLabel = l10n("重试", "Retry"),
                            onAction = { viewModel.loadFiles(deviceId = selectedDevice, forceRefresh = true) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    filteredFiles.isEmpty() -> {
                        if (searchKeyword.isNotBlank()) {
                            FileEmptyState(
                                kind = FileEmptyKind.NoMatchingFiles,
                                title = l10n("没有匹配文件", "No matching files"),
                                description = l10n("调整搜索词或清除筛选后再试。", "Adjust the search term or clear the filter."),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            FileEmptyState(
                                kind = FileEmptyKind.EmptyFolder,
                                title = l10n("此目录为空", "This folder is empty"),
                                description = l10n("当前路径没有可显示的文件或文件夹。", "This path has no visible files or folders."),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { coordinates ->
                                    fileListRootPx = coordinates.positionInRoot()
                                }
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Header row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                                        .padding(horizontal = UiTokens.SpaceMedium, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(UiTokens.IndicatorWidth + UiTokens.SpaceSmall))
                                    Text(
                                        text = l10n("名称", "Name").uppercase(),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = l10n("大小", "Size").uppercase(),
                                        modifier = Modifier.width(80.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = l10n("修改日期", "Modified").uppercase(),
                                        modifier = Modifier.width(130.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (showAdvancedFields) {
                                        Text(
                                            text = l10n("权限", "Permission").uppercase(),
                                            modifier = Modifier.width(110.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                letterSpacing = 0.6.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = l10n("所有者", "Owner").uppercase(),
                                            modifier = Modifier.width(80.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                letterSpacing = 0.6.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = l10n("类型", "Type").uppercase(),
                                            modifier = Modifier.width(90.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                letterSpacing = 0.6.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.60f))

                                // File rows
                                Box(modifier = Modifier.weight(1f)) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(end = UiTokens.SpaceSmall),
                                        state = listState
                                    ) {
                                        items(filteredFiles, key = { it.name }) { file ->
                                            FileRow(
                                                file = file,
                                                isHighlighted = selectedPaths.contains("$currentPath/${file.name}") || clipboardFiles.contains("$currentPath/${file.name}"),
                                                showAdvancedFields = showAdvancedFields,
                                                onClick = {
                                                    val full = "$currentPath/${file.name}"
                                                    selectedPaths = if (selectedPaths.contains(full)) selectedPaths - full else selectedPaths + full
                                                },
                                                onOpen = {
                                                    if (file.isDirectory) {
                                                        viewModel.navigateTo("$currentPath/${file.name}", selectedDevice)
                                                    } else {
                                                        viewModel.openFile("$currentPath/${file.name}", selectedDevice)
                                                    }
                                                },
                                                onRightClick = { clickInRoot ->
                                                    contextMenuTarget = file
                                                    val clickInList = clickInRoot - fileListRootPx
                                                    contextMenuOffsetPx = Offset(
                                                        x = max(0f, clickInList.x),
                                                        y = max(0f, clickInList.y)
                                                    )
                                                    contextMenuExpanded = true
                                                }
                                            )
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f))
                                        }
                                    }

                                    VerticalScrollbar(
                                        adapter = rememberScrollbarAdapter(listState),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .fillMaxHeight()
                                            .padding(end = UiTokens.SpaceXSmall)
                                            .padding(vertical = UiTokens.SpaceSmall)
                                    )
                                }
                            }

                            // Context menu
                            if (contextMenuExpanded && contextMenuTarget != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { contextMenuExpanded = false }
                                )
                                androidx.compose.material.DropdownMenu(
                                    expanded = contextMenuExpanded,
                                    onDismissRequest = { contextMenuExpanded = false },
                                    offset = with(density) {
                                        DpOffset(contextMenuOffsetPx.x.toDp(), contextMenuOffsetPx.y.toDp())
                                    },
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    val file = contextMenuTarget!!
                                    val fullPath = "$currentPath/${file.name}"

                                    if (file.isDirectory) {
                                        DropdownMenuItem(
                                            text = { Text(l10n("打开", "Open")) },
                                            onClick = {
                                                viewModel.navigateTo(fullPath, selectedDevice)
                                                contextMenuExpanded = false
                                            },
                                            leadingIcon = { Icon(IconParkIcons.Folder, l10n("打开", "Open"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text(l10n("打开", "Open")) },
                                            onClick = {
                                                viewModel.openFile(fullPath, selectedDevice)
                                                contextMenuExpanded = false
                                            },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, l10n("打开", "Open"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.SpaceXSmall))

                                    DropdownMenuItem(
                                        text = { Text(l10n("复制路径", "Copy path")) },
                                        onClick = {
                                            val copied = copyToClipboardText(fullPath)
                                            viewModel.showToast(
                                                MsgContent.Text(if (copied) l10n("已复制路径", "Path copied") else l10n("复制失败", "Copy failed"))
                                            )
                                            contextMenuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, l10n("复制路径", "Copy path"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.SpaceXSmall))

                                    if (!file.isDirectory) {
                                        DropdownMenuItem(
                                            text = { Text(l10n("拉取到本地", "Pull to local")) },
                                            onClick = {
                                                viewModel.pullFile(fullPath, selectedDevice)
                                                contextMenuExpanded = false
                                            },
                                            leadingIcon = { Icon(IconParkIcons.Download, l10n("拉取", "Pull"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text(l10n("重命名", "Rename")) },
                                        onClick = {
                                            renameTarget = file
                                            renameText = file.name
                                            showRenameDialog = true
                                            contextMenuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, l10n("重命名", "Rename"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.SpaceXSmall))

                                    DropdownMenuItem(
                                        text = { Text(l10n("查看权限", "View permission")) },
                                        onClick = {
                                            viewModel.getFilePermission(fullPath, selectedDevice)
                                            contextMenuExpanded = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Security, l10n("权限", "Permission"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(l10n("属性", "Properties")) },
                                        onClick = {
                                            viewModel.getFilePermission(fullPath, selectedDevice)
                                            contextMenuExpanded = false
                                        },
                                        leadingIcon = { Icon(IconParkIcons.Info, l10n("属性", "Properties"), modifier = Modifier.size(UiTokens.IconMedium)) }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = UiTokens.SpaceXSmall))

                                    DropdownMenuItem(
                                        text = { Text(l10n("删除", "Delete"), color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            deleteTargets = listOf("${currentPath}/${file.name}")
                                            showDeleteConfirm = true
                                            contextMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                l10n("删除", "Delete"),
                                                modifier = Modifier.size(UiTokens.IconMedium),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                if (isDragActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .padding(UiTokens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                        ) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(UiTokens.IconLarge))
                            Text(
                                text = l10n("松开鼠标以上传到", "Release to upload into") + " $currentPath",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }

        // ── Bottom Summary Bar ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UiTokens.SpaceSmall, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = l10n("${filteredFiles.size} 项 · 已选择 ${selectedPaths.size} 项", "${filteredFiles.size} items · selected ${selectedPaths.size}"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = l10n("可用空间: $availableSpace", "Free: $availableSpace"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Navigation Toolbar ────────────────────────────────────────────────────────
@Composable
private fun NavigationToolbar(
    currentPath: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    searchKeyword: String,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onUp: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToPath: (String) -> Unit,
    onSearchChange: (String) -> Unit,
) {
    var pathInput by remember(currentPath) { mutableStateOf(currentPath) }
    var isEditingPath by remember(currentPath) { mutableStateOf(false) }
    val crumbs = remember(currentPath) {
        currentPath.trim('/').split("/").filter { it.isNotBlank() }
    }
    val crumbPaths = remember(currentPath, crumbs) {
        crumbs.mapIndexed { index, _ ->
            "/" + crumbs.take(index + 1).joinToString("/")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
    ) {
        // Nav button group (Back, Forward, Up, Refresh)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                tooltip = l10n("后退", "Back"),
                enabled = canGoBack,
                onClick = onBack
            )
            NavIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                tooltip = l10n("前进", "Forward"),
                enabled = canGoForward,
                onClick = onForward
            )
            NavIconButton(
                icon = Icons.Default.ArrowUpward,
                tooltip = l10n("上一级", "Up"),
                enabled = currentPath != "/" && currentPath.isNotBlank(),
                onClick = onUp
            )
            NavIconButton(
                icon = IconParkIcons.Refresh,
                tooltip = l10n("刷新", "Refresh"),
                enabled = true,
                onClick = onRefresh
            )
        }

        // Path breadcrumbs or text input field
        if (isEditingPath) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(UiTokens.RadiusMedium))
                    .padding(horizontal = UiTokens.SpaceSmall),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
                ) {
                    Icon(
                        IconParkIcons.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    BasicTextField(
                        value = pathInput,
                        onValueChange = { pathInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace
                        ),
                        decorationBox = { inner ->
                            if (pathInput.isBlank()) {
                                Text(
                                    l10n("输入路径...", "Input path..."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            inner()
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .clickable {
                                if (pathInput.isNotBlank()) onNavigateToPath(pathInput.trim())
                                isEditingPath = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = l10n("确认", "Confirm"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                    .padding(horizontal = UiTokens.SpaceSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceXSmall)
            ) {
                // Root / Storage icon + click
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                        .clickable { onNavigateToPath("/sdcard") }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        IconParkIcons.HardDisk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = l10n("内部存储", "Internal"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                crumbs.forEachIndexed { index, crumb ->
                    Icon(
                        IconParkIcons.Right,
                        null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = crumb,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index == crumbs.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (index == crumbs.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                            .clickable { onNavigateToPath(crumbPaths.getOrNull(index) ?: "/sdcard") }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Toggle edit icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(UiTokens.RadiusSmall))
                        .clickable { isEditingPath = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = l10n("编辑路径", "Edit path"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Search input field
        Box(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 280.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(UiTokens.RadiusMedium))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(UiTokens.RadiusMedium))
                .padding(horizontal = UiTokens.SpaceSmall),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
            ) {
                Icon(
                    IconParkIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                BasicTextField(
                    value = searchKeyword,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { inner ->
                        if (searchKeyword.isEmpty()) {
                            Text(
                                l10n("搜索文件名...", "Search files..."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        inner()
                    }
                )
                if (searchKeyword.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(UiTokens.BadgeRadius))
                            .clickable { onSearchChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = l10n("清除", "Clear"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(UiTokens.RadiusMedium))
            .background(if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else Color.Transparent)
            .border(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                RoundedCornerShape(UiTokens.RadiusMedium)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(15.dp)
        )
    }
}

// ── Quick Path Bar ────────────────────────────────────────────────────────────
@Composable
private fun QuickPathBar(
    currentPath: String,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileBrowserViewModel.QUICK_PATHS.forEach { (path, label) ->
            val isActive = currentPath == path || currentPath.startsWith("$path/")
            val localizedLabel = when (path) {
                "/" -> l10n("根目录", "Root")
                "/sdcard" -> l10n("内部存储", "Internal")
                "/sdcard/Download" -> l10n("下载", "Download")
                "/sdcard/DCIM" -> l10n("相册", "DCIM")
                "/sdcard/Documents" -> l10n("文档", "Documents")
                "/data/data" -> l10n("应用数据", "App Data")
                "/system" -> l10n("系统", "System")
                "/tmp" -> l10n("临时文件", "Temp")
                else -> label
            }
            val shape = RoundedCornerShape(UiTokens.BadgeRadius)
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                    .border(
                        1.dp,
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape
                    )
                    .clickable { onNavigate(path) }
                    .padding(horizontal = UiTokens.SpaceSmall, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        when (path) {
                            "/" -> IconParkIcons.HardDisk
                            "/sdcard" -> IconParkIcons.HardDisk
                            "/sdcard/Download" -> IconParkIcons.Download
                            "/sdcard/DCIM" -> IconParkIcons.Camera
                            "/sdcard/Documents" -> IconParkIcons.FileText
                            "/data/data" -> IconParkIcons.Application
                            "/system" -> IconParkIcons.Setting
                            else -> IconParkIcons.Folder
                        },
                        localizedLabel,
                        modifier = Modifier.size(13.dp),
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localizedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Sort Chip ─────────────────────────────────────────────────────────────────
@Composable
private fun SortChip(
    label: String,
    isActive: Boolean,
    order: FileSortOrder,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(UiTokens.BadgeRadius)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.60f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
            )
            .border(
                1.dp,
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f),
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isActive) {
                Text(
                    text = if (order == FileSortOrder.ASC) "↑" else "↓",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── File Row ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FileRow(
    file: FileInfo,
    isHighlighted: Boolean,
    showAdvancedFields: Boolean,
    onClick: () -> Unit,
    onOpen: () -> Unit,
    onRightClick: (Offset) -> Unit
) {
    val rowInteraction = remember { MutableInteractionSource() }
    val isHovered by rowInteraction.collectIsHoveredAsState()
    val bgColor = when {
        isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        isHovered -> QadbColors.surfaceHover
        else -> Color.Transparent
    }
    var rowRootPx by remember(file.name) { mutableStateOf(Offset.Zero) }
    val fileType = when {
        file.isDirectory -> l10n("文件夹", "Folder")
        file.name.endsWith(".apk", true) -> "APK"
        file.name.endsWith(".zip", true) || file.name.endsWith(".tar", true) || file.name.endsWith(".gz", true) -> l10n("压缩包", "Archive")
        file.name.endsWith(".jpg", true) || file.name.endsWith(".png", true) || file.name.endsWith(".gif", true) -> l10n("图片", "Image")
        file.name.endsWith(".mp4", true) || file.name.endsWith(".avi", true) || file.name.endsWith(".mkv", true) -> l10n("视频", "Video")
        else -> l10n("文件", "File")
    }

    val warningColor = QadbTokens.warning
    val brandColor = QadbTokens.brand

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp, max = 40.dp)
            .background(bgColor)
            .hoverable(rowInteraction)
            .onGloballyPositioned { coordinates ->
                rowRootPx = coordinates.positionInRoot()
            }
            .pointerInput(file.name) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.button == PointerButton.Secondary && event.type == PointerEventType.Press) {
                            val clickInRoot = rowRootPx + event.changes.first().position
                            onRightClick(clickInRoot)
                        }
                    }
                }
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onRightClick(rowRootPx) },
                onDoubleClick = onOpen
            )
            .padding(horizontal = UiTokens.SpaceMedium, vertical = UiTokens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection indicator bar
        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .width(UiTokens.IndicatorWidth)
                    .height(20.dp)
                    .padding(end = UiTokens.SpaceSmall)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(UiTokens.RadiusSmall)
                    )
            )
        } else {
            Spacer(modifier = Modifier.width(UiTokens.IndicatorWidth))
        }

        // Name column with icon
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiTokens.SpaceSmall)
        ) {
            Icon(
                imageVector = when {
                    file.isDirectory -> IconParkIcons.Folder
                    file.isSymlink -> IconParkIcons.Link
                    file.name.endsWith(".apk") -> Icons.Default.Android
                    file.name.endsWith(".zip") || file.name.endsWith(".tar") || file.name.endsWith(".gz") -> Icons.Default.Archive
                    file.name.endsWith(".jpg") || file.name.endsWith(".png") || file.name.endsWith(".gif") -> Icons.Default.Image
                    file.name.endsWith(".mp3") || file.name.endsWith(".wav") || file.name.endsWith(".ogg") -> Icons.Default.MusicNote
                    file.name.endsWith(".mp4") || file.name.endsWith(".avi") || file.name.endsWith(".mkv") -> IconParkIcons.Video
                    file.name.endsWith(".txt") || file.name.endsWith(".log") || file.name.endsWith(".json") || file.name.endsWith(".xml") -> IconParkIcons.FileText
                    file.name.endsWith(".sh") || file.name.endsWith(".py") || file.name.endsWith(".java") || file.name.endsWith(".kt") -> IconParkIcons.Code
                    else -> IconParkIcons.File
                },
                contentDescription = null,
                modifier = Modifier.size(UiTokens.IconMedium),
                tint = when {
                    file.isDirectory -> MaterialTheme.colorScheme.primary
                    file.isSymlink -> MaterialTheme.colorScheme.tertiary
                    file.name.endsWith(".apk") -> MaterialTheme.colorScheme.secondary
                    file.name.endsWith(".zip") || file.name.endsWith(".tar") || file.name.endsWith(".gz") -> warningColor
                    file.name.endsWith(".jpg") || file.name.endsWith(".png") || file.name.endsWith(".gif") -> brandColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (file.isDirectory) FontWeight.SemiBold else FontWeight.Normal
                )
                if (file.isSymlink && file.symlinkTarget.isNotEmpty()) {
                    Text(
                        text = "-> ${file.symlinkTarget}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Size
        Text(
            text = if (file.isDirectory) "-" else file.displaySize,
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Date
        Text(
            text = "${file.date} ${file.time}",
            modifier = Modifier.width(130.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Advanced / Type
        if (showAdvancedFields) {
            Text(
                text = file.permissions,
                modifier = Modifier.width(110.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.owner,
                modifier = Modifier.width(80.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = fileType,
                modifier = Modifier.width(90.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun copyToClipboardText(text: String): Boolean {
    return runCatching {
        val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
    }.isSuccess
}
