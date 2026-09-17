package com.ludoven.adbtool.viewmodel

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.dialog_clear_and_restart_success
import adbtool_desktop.composeapp.generated.resources.dialog_export_failed
import adbtool_desktop.composeapp.generated.resources.dialog_export_success
import adbtool_desktop.composeapp.generated.resources.dialog_get_install_path_failed
import adbtool_desktop.composeapp.generated.resources.dialog_no_export_path
import adbtool_desktop.composeapp.generated.resources.dialog_operation_failed
import adbtool_desktop.composeapp.generated.resources.dialog_reset_permissions_and_restart_success
import adbtool_desktop.composeapp.generated.resources.dialog_restart_success
import adbtool_desktop.composeapp.generated.resources.dialog_uninstall_failed
import adbtool_desktop.composeapp.generated.resources.dialog_uninstall_success
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.viewModelScope
import com.ludoven.adbtool.domain.adb.AppIconHelperClient
import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.entity.MsgContent
import com.ludoven.adbtool.entity.AppInfoData
import com.ludoven.adbtool.pages.AppInfo
import com.ludoven.adbtool.util.AdbPathManager
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.FileUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile

sealed class AppIconState {
    object Placeholder : AppIconState()
    object Loading : AppIconState()
    data class Success(
        val imagePath: String,
        val source: String,
        val cacheHit: Boolean,
        val elapsedMs: Long
    ) : AppIconState()
    data class Failed(val reason: String) : AppIconState()
}

internal fun parseRunningPackagesFromActivityProcesses(output: String): Set<String> {
    val processPattern = Regex("""ProcessRecord\{[^}]*\s(?:\d+:)?([A-Za-z0-9_.]+(?::[A-Za-z0-9_.-]+)?)/""")
    return output.lineSequence()
        .mapNotNull { line ->
            processPattern.find(line)
                ?.groupValues
                ?.getOrNull(1)
                ?.substringBefore(":")
                ?.takeIf { it.isNotBlank() }
        }
        .toSet()
}

internal fun appListLoadShouldApply(requestedDeviceId: String?, currentDeviceId: String?): Boolean {
    val requested = requestedDeviceId?.trim().orEmpty()
    val current = currentDeviceId?.trim().orEmpty()
    return requested.isNotEmpty() && requested == current
}

private val packageLineRegex = Regex("""package:(.+)=([A-Za-z0-9._]+)""")

internal suspend fun loadInstalledAppsForDevice(): List<AppInfo> = coroutineScope {
    val allAppsDef = async { AdbTool.exec("pm list packages -f") }
    val sysAppsDef = async { AdbTool.exec("pm list packages -s") }
    val disabledAppsDef = async { AdbTool.exec("pm list packages -d") }
    val versionsDef = async {
        runCatching { parseAllVersionNames(AdbTool.exec("dumpsys package")) }
            .getOrDefault(emptyMap())
    }

    val allApps = allAppsDef.await() ?: return@coroutineScope emptyList()
    val sysApps = sysAppsDef.await() ?: ""
    val disabledApps = disabledAppsDef.await() ?: ""
    val versionNames = versionsDef.await()
    val sysPackages = sysApps.lines()
        .mapNotNull { it.substringAfter("package:", "").takeIf { p -> p.isNotEmpty() } }
        .toSet()
    val disabledPackages = disabledApps.lines()
        .mapNotNull { it.substringAfter("package:", "").takeIf { p -> p.isNotEmpty() } }
        .toSet()

    val apps = allApps.lines().mapNotNull { line ->
        val match = packageLineRegex.find(line)
        val (apkPath, packageName) = match?.destructured ?: return@mapNotNull null
        val debugHint = packageName.contains("debug", ignoreCase = true) || packageName.endsWith(".dev")
        AppInfo(
            appName = packageName,
            packageName = packageName,
            apkPath = apkPath,
            isSystemApp = sysPackages.contains(packageName),
            isDebuggable = debugHint,
            isDisabled = disabledPackages.contains(packageName)
        )
    }
    // 版本与大小直接获取，不做懒加载
    val sizeBytesMap = queryBulkApkSizes(apps.map { it.apkPath })
    apps.map { app ->
        val sizeBytes = sizeBytesMap[app.apkPath]
        app.copy(
            versionName = versionNames[app.packageName] ?: "-",
            size = sizeBytes?.let { formatAppSize(it) } ?: "-",
            sizeBytes = sizeBytes
        )
    }.sortedBy { it.packageName }
}

private fun formatAppSize(bytes: Long): String {
    if (bytes <= 0L) return "-"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
    }
}

// 从 dumpsys package 一次性解析所有应用的 versionName
private fun parseAllVersionNames(dumpsys: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var currentPackage: String? = null
    val packageHeaderRegex = Regex("""^  Package \[([^\]]+)] \(""")
    val versionNameRegex = Regex("""^    versionName=(.*)$""")
    dumpsys.lineSequence().forEach { line ->
        val pkgMatch = packageHeaderRegex.find(line)
        if (pkgMatch != null) {
            currentPackage = pkgMatch.groupValues[1]
        } else if (currentPackage != null) {
            val versionMatch = versionNameRegex.find(line)
            if (versionMatch != null) {
                result[currentPackage!!] = versionMatch.groupValues[1].trim()
                currentPackage = null
            }
        }
    }
    return result
}

// 批量获取 APK 大小：优先 du，失败回退 stat
private suspend fun queryBulkApkSizes(paths: List<String>): Map<String, Long> {
    val distinctPaths = paths.filter { it.isNotBlank() }.distinct()
    if (distinctPaths.isEmpty()) return emptyMap()

    val duSizes = queryBulkDuSizeBytes(distinctPaths)
    if (duSizes.isNotEmpty()) {
        return distinctPaths.mapNotNull { path ->
            duSizes[path]?.let { path to it }
        }.toMap()
    }
    return queryBulkStatSizeBytes(distinctPaths)
}

private fun queryBulkDuSizeBytes(paths: List<String>): Map<String, Long> {
    val result = mutableMapOf<String, Long>()
    for (chunk in paths.chunked(80)) {
        val output = AdbTool.exec(AdbTool.buildShellCommand("du", "-s", *chunk.toTypedArray()))
        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) return@forEach
            val parts = trimmed.split(Regex("\\s+"), limit = 2)
            if (parts.size < 2) return@forEach
            val sizeKb = parts[0].toLongOrNull() ?: return@forEach
            result[parts[1]] = sizeKb * 1024L
        }
    }
    return result
}

private fun queryBulkStatSizeBytes(paths: List<String>): Map<String, Long> {
    val result = mutableMapOf<String, Long>()
    for (chunk in paths.chunked(80)) {
        val output = AdbTool.exec(AdbTool.buildShellCommand("stat", "-c", "%s", *chunk.toTypedArray()))
        val sizes = output.lineSequence().mapNotNull { it.trim().toLongOrNull() }.toList()
        chunk.forEachIndexed { index, path ->
            sizes.getOrNull(index)?.let { result[path] = it }
        }
    }
    return result
}

class AppViewModel : BaseViewModel() {
    companion object {
        private const val VISIBLE_ASSET_LOAD_DEBOUNCE_MS = 120L
        private const val MAX_VISIBLE_ASSET_BATCH = 24

        internal fun appPackageShellCommand(vararg args: String): String =
            AdbTool.appShellCommand(*args)

        internal fun appPathShellCommand(vararg args: String): String =
            AdbTool.buildShellCommand(*args)
    }

    private val _appInfo = MutableStateFlow<AppInfoData?>(null)
    val appInfo: StateFlow<AppInfoData?> = _appInfo.asStateFlow()

    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList: StateFlow<List<AppInfo>> = _appList

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedTab = MutableStateFlow("all")
    val selectedTab = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isInstalling = MutableStateFlow(false)
    val isInstalling = _isInstalling.asStateFlow()

    private val _currentInstallingProgress = MutableStateFlow<String?>(null)
    val currentInstallingProgress = _currentInstallingProgress.asStateFlow()

    // View mode: true = grid, false = list
    private val _isGridView = MutableStateFlow(false)
    val isGridView = _isGridView.asStateFlow()

    // Icon cache: packageName -> ImageBitmap
    private val _appIcons = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val appIcons = _appIcons.asStateFlow()

    private val _appIconStates = MutableStateFlow<Map<String, AppIconState>>(emptyMap())
    val appIconStates = _appIconStates.asStateFlow()

    // Label cache: packageName -> app label
    private val _appLabels = MutableStateFlow<Map<String, String>>(emptyMap())
    private val loadingPackages = mutableSetOf<String>()
    private var activeCacheDeviceKey: String = ""
    private var appListLoadDeviceId: String? = null
    private var loadedAppListDeviceId: String? = null
    private var appListLoadJob: Job? = null
    private var iconPrefetchJob: Job? = null
    private var runningStatusJob: Job? = null
    private var visibleAssetsJob: Job? = null
    private val iconTraceLock = Any()
    private var iconTraceSessionId = 0L
    private var iconTraceDeviceId = ""
    private var iconTraceStartedAt = 0L
    private var iconTraceListLoadedAt = 0L
    private var iconTraceTotal = 0
    private var iconTraceSuccess = 0
    private var iconTraceFailed = 0
    private val iconTraceCompletedPackages = mutableSetOf<String>()

    // Local persistent cache root, split by device id.
    private val iconCacheRootDir = File(System.getProperty("user.home"), ".qadb/icons").also { it.mkdirs() }
    private val iconHelperClient = AppIconHelperClient(iconCacheRootDir)
    private val iconLoadSemaphore = Semaphore(6)

    fun clearAppInfo() {
        _appInfo.value = null
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun setGridView(enabled: Boolean) {
        _isGridView.value = enabled
    }

    fun getAppList(deviceId: String? = AdbTool.selectDeviceId, forceRefresh: Boolean = false) {
        val normalizedDeviceId = deviceId?.trim().orEmpty()
        if (normalizedDeviceId.isBlank()) {
            AdbTool.selectDeviceId = null
            appListLoadJob?.cancel()
            appListLoadJob = null
            appListLoadDeviceId = null
            loadedAppListDeviceId = null
            iconPrefetchJob?.cancel()
            iconPrefetchJob = null
            runningStatusJob?.cancel()
            runningStatusJob = null
            visibleAssetsJob?.cancel()
            visibleAssetsJob = null
            _appList.value = emptyList()
            _appIcons.value = emptyMap()
            _appIconStates.value = emptyMap()
            _appLabels.value = emptyMap()
            _isLoading.value = false
            return
        }
        AdbTool.selectDeviceId = normalizedDeviceId
        if (!forceRefresh && appListLoadDeviceId == normalizedDeviceId && appListLoadJob?.isActive == true) {
            return
        }
        if (!forceRefresh && loadedAppListDeviceId == normalizedDeviceId && _appList.value.isNotEmpty()) {
            _isLoading.value = false
            return
        }
        appListLoadJob?.cancel()
        iconPrefetchJob?.cancel()
        runningStatusJob?.cancel()
        appListLoadDeviceId = normalizedDeviceId
        val traceSessionId = startIconTrace(normalizedDeviceId)
        appListLoadJob = viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val listStartedAt = System.currentTimeMillis()
            logIconTrace(
                "appListStart",
                "session=$traceSessionId device=$normalizedDeviceId"
            )
            try {
                refreshCacheContextIfNeeded()
                val list = loadInstalledAppsForDevice()
                if (!appListLoadShouldApply(normalizedDeviceId, appListLoadDeviceId)) return@launch
                loadedAppListDeviceId = normalizedDeviceId
                _appList.value = list
                hydrateCachedLabels(list)
                markAppListLoaded(traceSessionId, list.size, listStartedAt)
                // 列表重载后重新填充运行状态，避免“运行中”页在刷新后被清空
                scheduleRunningStatusRefresh(delayMillis = 0L)
                if (isFullIconPrefetchEnabled()) {
                    scheduleAllIconPrefetch(traceSessionId, normalizedDeviceId, list)
                }
            } finally {
                if (appListLoadShouldApply(normalizedDeviceId, appListLoadDeviceId)) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun batchInstallFromFolder(deviceId: String?) {
        if (deviceId.isNullOrBlank() || _isInstalling.value) return
        viewModelScope.launch {
            val folder = withContext(Dispatchers.IO) { FileUtils.selectFolder() } ?: return@launch
            val apks = withContext(Dispatchers.IO) {
                File(folder).walkTopDown()
                    .filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
                    .toList()
            }
            if (apks.isEmpty()) {
                showTipDialog(MsgContent.Text("所选文件夹中没有 APK 文件"))
                return@launch
            }
            _isInstalling.value = true
            var success = 0
            var fail = 0
            for (apk in apks) {
                val result = withContext(Dispatchers.IO) {
                    AdbTool.installApkAsync(apk.absolutePath, deviceId)
                }
                if (result.success) success++ else fail++
                _currentInstallingProgress.value = "$success/${apks.size}"
            }
            _isInstalling.value = false
            _currentInstallingProgress.value = null
            getAppList(forceRefresh = true)
            showTipDialog(
                if (fail > 0) {
                    MsgContent.Text("批量安装完成：成功 $success 个，失败 $fail 个，共 ${apks.size} 个 APK")
                } else {
                    MsgContent.Text("批量安装完成：成功安装 $success 个 APK")
                }
            )
        }
    }

    fun cancelPageLoads() {
        appListLoadJob?.cancel()
        appListLoadJob = null
        iconPrefetchJob?.cancel()
        iconPrefetchJob = null
        runningStatusJob?.cancel()
        runningStatusJob = null
        visibleAssetsJob?.cancel()
        visibleAssetsJob = null
        synchronized(loadingPackages) {
            loadingPackages.clear()
        }
        _isLoading.value = false
    }

    fun refreshRunningStatusAsync() {
        scheduleRunningStatusRefresh(delayMillis = 0L)
    }

    fun ensureAppAssetsVisible(packageNames: List<String>) {
        val distinctPackages = packageNames.distinct().take(MAX_VISIBLE_ASSET_BATCH)
        if (distinctPackages.isEmpty()) return

        visibleAssetsJob?.cancel()
        visibleAssetsJob = viewModelScope.launch(Dispatchers.IO) {
            delay(VISIBLE_ASSET_LOAD_DEBOUNCE_MS)
            refreshCacheContextIfNeeded()
            val claimedApps = mutableListOf<AppInfo>()
            for (packageName in distinctPackages) {
                val iconState = _appIconStates.value[packageName]
                val iconResolved = _appIcons.value.containsKey(packageName) || iconState is AppIconState.Failed
                if (iconResolved) continue
                if (!claimLoading(packageName)) continue
                val app = _appList.value.firstOrNull { it.packageName == packageName }
                if (app == null) {
                    releaseLoading(packageName)
                } else {
                    claimedApps.add(app)
                }
            }
            if (claimedApps.isEmpty()) return@launch

            try {
                iconLoadSemaphore.withPermit {
                    ensureAppAssetsBatch(claimedApps, "visible", iconTraceSessionId)
                }
            } catch (error: Exception) {
                val reason = error.message ?: "unknown asset load error"
                _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                    claimedApps.forEach { app -> put(app.packageName, AppIconState.Failed(reason)) }
                }
            } finally {
                claimedApps.forEach { releaseLoading(it.packageName) }
            }
        }
    }

    private fun refreshCacheContextIfNeeded() {
        val currentKey = currentDeviceCacheKey()
        if (activeCacheDeviceKey == currentKey) return

        activeCacheDeviceKey = currentKey
        _appIcons.value = emptyMap()
        _appLabels.value = emptyMap()
        _appIconStates.value = emptyMap()
        synchronized(loadingPackages) {
            loadingPackages.clear()
        }
    }

    private fun startIconTrace(deviceId: String): Long = synchronized(iconTraceLock) {
        iconTraceSessionId += 1
        iconTraceDeviceId = deviceId
        iconTraceStartedAt = System.currentTimeMillis()
        iconTraceListLoadedAt = 0L
        iconTraceTotal = 0
        iconTraceSuccess = 0
        iconTraceFailed = 0
        iconTraceCompletedPackages.clear()
        iconTraceSessionId
    }

    private fun markAppListLoaded(sessionId: Long, total: Int, listStartedAt: Long) {
        val now = System.currentTimeMillis()
        synchronized(iconTraceLock) {
            if (sessionId != iconTraceSessionId) return
            iconTraceListLoadedAt = now
            iconTraceTotal = total
        }
        logIconTrace(
            "appListLoaded",
            "session=$sessionId device=$iconTraceDeviceId total=$total listElapsedMs=${now - listStartedAt} totalElapsedMs=${now - iconTraceStartedAt}"
        )
    }

    private fun markIconTraceResult(sessionId: Long, packageName: String, success: Boolean) {
        var completed = false
        var successCount = 0
        var failedCount = 0
        var total = 0
        var totalElapsed = 0L
        var iconElapsed = 0L
        synchronized(iconTraceLock) {
            if (sessionId != iconTraceSessionId || iconTraceCompletedPackages.contains(packageName)) return
            iconTraceCompletedPackages.add(packageName)
            if (success) iconTraceSuccess += 1 else iconTraceFailed += 1
            completed = iconTraceTotal > 0 && iconTraceCompletedPackages.size >= iconTraceTotal
            successCount = iconTraceSuccess
            failedCount = iconTraceFailed
            total = iconTraceTotal
            val now = System.currentTimeMillis()
            totalElapsed = now - iconTraceStartedAt
            iconElapsed = if (iconTraceListLoadedAt > 0L) now - iconTraceListLoadedAt else 0L
        }
        if (completed) {
            logIconTrace(
                "allIconsComplete",
                "session=$sessionId device=$iconTraceDeviceId total=$total success=$successCount failed=$failedCount iconElapsedMs=$iconElapsed totalElapsedMs=$totalElapsed"
            )
        }
    }

    private fun isCurrentIconTrace(sessionId: Long, deviceId: String): Boolean = synchronized(iconTraceLock) {
        sessionId == iconTraceSessionId && deviceId == iconTraceDeviceId
    }

    private fun logIconTrace(event: String, message: String) {
        println("QADB app-icon-trace event=$event $message")
    }

    private fun isFullIconPrefetchEnabled(): Boolean =
        System.getProperty("qadb.icon.prefetchAll") == "true"

    private fun isApkFallbackEnabled(): Boolean =
        System.getProperty("qadb.icon.apkFallback") == "true"

    private fun scheduleAllIconPrefetch(sessionId: Long, deviceId: String, apps: List<AppInfo>) {
        iconPrefetchJob?.cancel()
        iconPrefetchJob = viewModelScope.launch(Dispatchers.IO) {
            val total = apps.size
            logIconTrace("iconPrefetchStart", "session=$sessionId device=$deviceId total=$total")
            for (chunk in apps.chunked(16)) {
                if (!isCurrentIconTrace(sessionId, deviceId)) return@launch
                val claimedApps = mutableListOf<AppInfo>()
                for (app in chunk) {
                    val packageName = app.packageName
                    val iconState = _appIconStates.value[packageName]
                    val iconResolved = _appIcons.value.containsKey(packageName) || iconState is AppIconState.Failed
                    if (iconResolved) {
                        markIconTraceResult(sessionId, packageName, iconState !is AppIconState.Failed)
                        continue
                    }
                    if (!claimLoading(packageName)) continue
                    claimedApps.add(app)
                }
                if (claimedApps.isEmpty()) continue
                try {
                    iconLoadSemaphore.withPermit {
                        ensureAppAssetsBatch(claimedApps, "prefetch", sessionId)
                    }
                } catch (error: Exception) {
                    val reason = error.message ?: "unknown asset load error"
                    _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                        claimedApps.forEach { app -> put(app.packageName, AppIconState.Failed(reason)) }
                    }
                    claimedApps.forEach { markIconTraceResult(sessionId, it.packageName, success = false) }
                } finally {
                    claimedApps.forEach { releaseLoading(it.packageName) }
                }
            }
        }
    }

    private fun currentDeviceCacheDir(): File {
        val dir = File(iconCacheRootDir, currentDeviceCacheKey())
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun currentDeviceCacheKey(): String {
        val raw = AdbTool.selectDeviceId?.trim().orEmpty().ifBlank { "default" }
        return raw.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private fun claimLoading(packageName: String): Boolean = synchronized(loadingPackages) {
        if (loadingPackages.contains(packageName)) return@synchronized false
        loadingPackages.add(packageName)
        true
    }

    private fun releaseLoading(packageName: String) = synchronized(loadingPackages) {
        loadingPackages.remove(packageName)
    }

    private fun hydrateCachedLabels(list: List<AppInfo>) {
        val labels = mutableMapOf<String, String>()
        for (app in list) {
            val label = readLabelCache(app.packageName) ?: continue
            labels[app.packageName] = label
        }
        if (labels.isEmpty()) return

        _appLabels.value = _appLabels.value.toMutableMap().apply { putAll(labels) }
        _appList.value = _appList.value.map { app ->
            labels[app.packageName]?.let { label -> app.copy(appName = label) } ?: app
        }
    }

    private suspend fun ensureAppAssets(app: AppInfo) {
        val packageName = app.packageName
        val cacheDir = currentDeviceCacheDir()
        val iconCacheFile = File(cacheDir, AppIconHelperClient.localIconCacheFileName(packageName))
        val labelCacheFile = File(cacheDir, "$packageName.label.txt")

        if (!_appLabels.value.containsKey(packageName)) {
            readLabelCache(packageName)?.let { updateAppLabel(packageName, it) }
        }
        if (!_appIcons.value.containsKey(packageName)) {
            loadIconFromCache(packageName, iconCacheFile)
            if (_appIcons.value.containsKey(packageName)) {
                _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                    put(
                        packageName,
                        AppIconState.Success(
                            imagePath = iconCacheFile.absolutePath,
                            source = "desktopApkCache",
                            cacheHit = true,
                            elapsedMs = 0L
                        )
                    )
                }
            }
        }
        if (_appLabels.value.containsKey(packageName) && _appIcons.value.containsKey(packageName)) return

        val shouldRequestDeviceIcon = !_appIcons.value.containsKey(packageName) &&
            _appIconStates.value[packageName] !is AppIconState.Failed
        if (shouldRequestDeviceIcon) {
            _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                put(packageName, AppIconState.Loading)
            }
            val helperResult = iconHelperClient.fetchIcon(packageName, AdbTool.selectDeviceId)
            helperResult.onSuccess { result ->
                persistHelperLabel(result.packageName, result.label)
                loadIconFromCache(packageName, File(result.localPath))
                _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                    put(
                        packageName,
                        AppIconState.Success(
                            imagePath = result.localPath,
                            source = result.source,
                            cacheHit = result.cacheHit,
                            elapsedMs = result.elapsedMs
                        )
                    )
                }
                println(
                    "QADB icon helper package=$packageName source=${result.source} " +
                        "cache=${if (result.cacheHit) "hit" else "miss"} path=${result.remotePath} elapsedMs=${result.elapsedMs}"
                )
            }.onFailure { error ->
                val reason = error.message ?: "unknown icon helper error"
                _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                    put(packageName, AppIconState.Failed(reason))
                }
                println("QADB icon helper failed package=$packageName reason=$reason")
            }
        }

        if (_appLabels.value.containsKey(packageName) && _appIcons.value.containsKey(packageName)) return
        if (!isApkFallbackEnabled()) return
        if (_appIcons.value.containsKey(packageName)) return
        if (app.apkPath.isBlank()) return
        val tmpApk = File(cacheDir, "$packageName.apk")
        val pulled = AdbTool.pullFile(app.apkPath, tmpApk.absolutePath)
        if (!pulled) {
            tmpApk.delete()
            return
        }

        try {
            if (!_appLabels.value.containsKey(packageName)) {
                extractLabelFromApk(tmpApk)?.let { label ->
                    updateAppLabel(packageName, label)
                    runCatching { labelCacheFile.writeText(label) }
                }
            }

            if (!_appIcons.value.containsKey(packageName)) {
                extractIconFromApk(tmpApk)?.let { iconBytes ->
                    runCatching { iconCacheFile.writeBytes(iconBytes) }
                    loadIconFromCache(packageName, iconCacheFile)
                    if (_appIcons.value.containsKey(packageName)) {
                        _appIconStates.value = _appIconStates.value.toMutableMap().apply {
                            put(
                                packageName,
                                AppIconState.Success(
                                    imagePath = iconCacheFile.absolutePath,
                                    source = "desktopApkFallback",
                                    cacheHit = false,
                                    elapsedMs = 0L
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            tmpApk.delete()
        }
    }

    private suspend fun ensureAppAssetsBatch(apps: List<AppInfo>, trigger: String, traceSessionId: Long) {
        val batchStartedAt = System.currentTimeMillis()
        logIconTrace(
            "iconBatchStart",
            "session=$traceSessionId trigger=$trigger size=${apps.size} packages=${apps.take(5).joinToString(",") { it.packageName }}"
        )
        val packagesNeedingDeviceIcon = mutableListOf<String>()
        val cachedLabels = linkedMapOf<String, String>()
        val loadedIcons = linkedMapOf<String, ImageBitmap>()
        val stateUpdates = linkedMapOf<String, AppIconState>()
        for (app in apps) {
            val packageName = app.packageName
            if (!_appLabels.value.containsKey(packageName)) {
                readLabelCache(packageName)?.let { cachedLabels[packageName] = it }
            }
            if (!_appIcons.value.containsKey(packageName) && !loadedIcons.containsKey(packageName)) {
                val iconCacheFile = File(currentDeviceCacheDir(), AppIconHelperClient.localIconCacheFileName(packageName))
                readIconFromCache(iconCacheFile)?.let { bitmap ->
                    loadedIcons[packageName] = bitmap
                    stateUpdates[packageName] = AppIconState.Success(
                        imagePath = iconCacheFile.absolutePath,
                        source = "desktopApkCache",
                        cacheHit = true,
                        elapsedMs = 0L
                    )
                }
            }
            val shouldRequestDeviceIcon =
                !_appIcons.value.containsKey(packageName) &&
                    !loadedIcons.containsKey(packageName) &&
                    _appIconStates.value[packageName] !is AppIconState.Failed
            if (shouldRequestDeviceIcon) {
                packagesNeedingDeviceIcon.add(packageName)
            }
        }
        applyLabelBatch(cachedLabels)
        applyIconBatch(loadedIcons)
        applyIconStateBatch(stateUpdates)

        if (packagesNeedingDeviceIcon.isNotEmpty()) {
            val helperStartedAt = System.currentTimeMillis()
            applyIconStateBatch(
                packagesNeedingDeviceIcon.associateWith { AppIconState.Loading }
            )
            iconHelperClient.fetchIcons(packagesNeedingDeviceIcon, AdbTool.selectDeviceId)
                .onSuccess { batch ->
                    val helperLabels = linkedMapOf<String, String>()
                    val helperIcons = linkedMapOf<String, ImageBitmap>()
                    val helperStates = linkedMapOf<String, AppIconState>()
                    batch.successes.values.forEach { result ->
                        cleanHelperLabel(result.packageName, result.label)?.let { label ->
                            helperLabels[result.packageName] = label
                            runCatching {
                                File(currentDeviceCacheDir(), "${result.packageName}.label.txt").writeText(label)
                            }
                        }
                        readIconFromCache(File(result.localPath))?.let { bitmap ->
                            helperIcons[result.packageName] = bitmap
                        }
                        helperStates[result.packageName] = AppIconState.Success(
                            imagePath = result.localPath,
                            source = result.source,
                            cacheHit = result.cacheHit,
                            elapsedMs = result.elapsedMs
                        )
                        println(
                            "QADB icon helper package=${result.packageName} source=${result.source} " +
                                "cache=${if (result.cacheHit) "hit" else "miss"} path=${result.remotePath} elapsedMs=${result.elapsedMs}"
                        )
                    }
                    batch.failures.forEach { (packageName, reason) ->
                        helperStates[packageName] = AppIconState.Failed(reason)
                        println("QADB icon helper failed package=$packageName reason=$reason")
                    }
                    applyLabelBatch(helperLabels)
                    applyIconBatch(helperIcons)
                    applyIconStateBatch(helperStates)
                    logIconTrace(
                        "iconHelperBatchEnd",
                        "session=$traceSessionId trigger=$trigger requested=${packagesNeedingDeviceIcon.size} success=${batch.successes.size} failed=${batch.failures.size} elapsedMs=${System.currentTimeMillis() - helperStartedAt}"
                    )
                }
                .onFailure { error ->
                    val reason = error.message ?: "unknown icon helper error"
                    applyIconStateBatch(
                        packagesNeedingDeviceIcon.associateWith { AppIconState.Failed(reason) }
                    )
                    println("QADB icon helper batch failed count=${packagesNeedingDeviceIcon.size} reason=$reason")
                    logIconTrace(
                        "iconHelperBatchEnd",
                        "session=$traceSessionId trigger=$trigger requested=${packagesNeedingDeviceIcon.size} success=0 failed=${packagesNeedingDeviceIcon.size} elapsedMs=${System.currentTimeMillis() - helperStartedAt} reason=$reason"
                    )
                }
        }

        for (app in apps) {
            val packageName = app.packageName
            if (_appIcons.value.containsKey(packageName)) continue
            val state = _appIconStates.value[packageName]
            if (state !is AppIconState.Failed) {
                stateUpdates[packageName] = AppIconState.Failed("device icon helper did not return an icon")
            }
        }
        applyIconStateBatch(stateUpdates)

        var successCount = 0
        var failedCount = 0
        for (app in apps) {
            val packageName = app.packageName
            val state = _appIconStates.value[packageName]
            val success = _appIcons.value.containsKey(packageName) || state is AppIconState.Success
            val failed = state is AppIconState.Failed
            when {
                success -> {
                    successCount += 1
                    markIconTraceResult(traceSessionId, packageName, success = true)
                }
                failed -> {
                    failedCount += 1
                    markIconTraceResult(traceSessionId, packageName, success = false)
                }
            }
        }
        logIconTrace(
            "iconBatchEnd",
            "session=$traceSessionId trigger=$trigger size=${apps.size} success=$successCount failed=$failedCount elapsedMs=${System.currentTimeMillis() - batchStartedAt}"
        )
    }

    private fun readLabelCache(packageName: String): String? {
        val labelCacheFile = File(currentDeviceCacheDir(), "$packageName.label.txt")
        if (!labelCacheFile.exists()) return null
        val text = runCatching { labelCacheFile.readText().trim() }.getOrNull().orEmpty()
        return text.takeIf { it.isNotEmpty() }
    }

    private fun persistHelperLabel(packageName: String, label: String?) {
        val cleanLabel = cleanHelperLabel(packageName, label) ?: return
        applyLabelBatch(mapOf(packageName to cleanLabel))
        runCatching {
            File(currentDeviceCacheDir(), "$packageName.label.txt").writeText(cleanLabel)
        }
    }

    private fun cleanHelperLabel(packageName: String, label: String?): String? {
        return label
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != packageName }
    }

    private fun loadIconFromCache(packageName: String, iconFile: File) {
        val bitmap = readIconFromCache(iconFile) ?: return
        applyIconBatch(mapOf(packageName to bitmap))
    }

    private fun readIconFromCache(iconFile: File): ImageBitmap? {
        if (!iconFile.exists() || iconFile.length() <= 0L) return null
        return runCatching {
            Image.makeFromEncoded(iconFile.readBytes()).toComposeImageBitmap()
        }.getOrNull()
    }

    private fun applyIconBatch(icons: Map<String, ImageBitmap>) {
        if (icons.isEmpty()) return
        _appIcons.value = _appIcons.value.toMutableMap().apply { putAll(icons) }
    }

    private fun applyIconStateBatch(states: Map<String, AppIconState>) {
        if (states.isEmpty()) return
        _appIconStates.value = _appIconStates.value.toMutableMap().apply { putAll(states) }
    }

    private fun applyLabelBatch(labels: Map<String, String>) {
        if (labels.isEmpty()) return
        val changedLabels = labels.filter { (packageName, label) ->
            label.isNotBlank() && _appLabels.value[packageName] != label
        }
        if (changedLabels.isEmpty()) return

        _appLabels.value = _appLabels.value.toMutableMap().apply { putAll(changedLabels) }
        _appList.value = _appList.value.map { app ->
            changedLabels[app.packageName]?.let { label -> app.copy(appName = label) } ?: app
        }
    }

    private fun updateAppLabel(packageName: String, label: String) {
        val cleanLabel = label.trim()
        if (cleanLabel.isEmpty()) return
        val old = _appLabels.value[packageName]
        if (old == cleanLabel) return

        _appLabels.value = _appLabels.value.toMutableMap().apply { put(packageName, cleanLabel) }
        _appList.value = _appList.value.map { app ->
            if (app.packageName == packageName) app.copy(appName = cleanLabel) else app
        }
    }

    private fun extractLabelFromApk(apkFile: File): String? {
        val output = runAaptBadging(apkFile) ?: return null
        val labelPatterns = listOf(
            Regex("""application-label-zh-CN:'([^']+)'"""),
            Regex("""application-label-zh_CN:'([^']+)'"""),
            Regex("""application-label-zh:'([^']+)'"""),
            Regex("""application-label:'([^']+)'"""),
            Regex("""application-label-[^:]+:'([^']+)'""")
        )

        return labelPatterns.asSequence()
            .mapNotNull { it.find(output)?.groupValues?.getOrNull(1)?.trim() }
            .firstOrNull { it.isNotEmpty() }
    }

    private fun runAaptBadging(apkFile: File): String? {
        val osName = System.getProperty("os.name").lowercase()
        val aaptName = if (osName.contains("windows")) "aapt.exe" else "aapt"
        val candidates = buildList {
            add(aaptName)
            val adbPath = AdbPathManager.currentAdbPath
            if (!adbPath.isNullOrBlank()) {
                val sdkRoot = File(adbPath).parentFile?.parentFile
                val buildToolsDir = sdkRoot?.resolve("build-tools")
                buildToolsDir?.listFiles()
                    ?.filter { it.isDirectory }
                    ?.sortedByDescending { it.name }
                    ?.forEach { dir ->
                        val file = dir.resolve(aaptName)
                        if (file.exists() && file.canExecute()) add(file.absolutePath)
                    }
            }
        }.distinct()

        for (candidate in candidates) {
            val output = runCatching {
                val process = ProcessBuilder(candidate, "dump", "badging", apkFile.absolutePath)
                    .redirectErrorStream(true)
                    .start()
                val text = process.inputStream.bufferedReader().readText()
                process.waitFor()
                if (process.exitValue() == 0) text else null
            }.getOrNull()
            if (!output.isNullOrBlank()) return output
        }
        return null
    }

    private fun extractIconFromApk(apkFile: File): ByteArray? {
        return try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries().toList()

                val rasterEntry = entries
                    .filter { entry ->
                    val name = entry.name.lowercase()
                    (name.startsWith("res/mipmap") || name.startsWith("res/drawable")) &&
                    (name.endsWith(".png") || name.endsWith(".webp")) &&
                    (name.contains("ic_launcher") || name.contains("launcher_foreground") || name.contains("app_icon") || name.contains("icon") || name.contains("logo"))
                    }
                    .minByOrNull { entry ->
                    val name = entry.name.lowercase()
                    when {
                        name.contains("ic_launcher.png") -> 0
                        name.contains("launcher_foreground") -> 1
                        name.contains("xxxhdpi") -> 0
                        name.contains("xxhdpi") -> 2
                        name.contains("xhdpi") -> 3
                        name.contains("hdpi") -> 4
                        name.contains("mdpi") -> 5
                        else -> 6
                    }
                }

                if (rasterEntry != null) {
                    return@use zip.getInputStream(rasterEntry).readBytes()
                }

                val adaptiveXml = entries.firstOrNull { entry ->
                    val name = entry.name.lowercase()
                    (name.startsWith("res/mipmap") || name.startsWith("res/drawable")) &&
                    name.endsWith(".xml") &&
                    name.contains("ic_launcher")
                } ?: return@use null

                val xmlContent = zip.getInputStream(adaptiveXml).bufferedReader().use { it.readText() }
                val refRegex = Regex("""android:drawable=\"@((?:mipmap|drawable)/[A-Za-z0-9_]+)\"""")
                val resourceRefs = refRegex.findAll(xmlContent).map { it.groupValues[1] }.toList()
                val densityOrder = listOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi", "anydpi")

                for (ref in resourceRefs) {
                    val type = ref.substringBefore("/")
                    val name = ref.substringAfter("/")
                    val candidate = densityOrder
                        .asSequence()
                        .mapNotNull { density ->
                            entries.firstOrNull { entry ->
                                val entryName = entry.name.lowercase()
                                entryName.startsWith("res/${type.lowercase()}-$density/$name".lowercase()) &&
                                    (entryName.endsWith(".png") || entryName.endsWith(".webp"))
                            }
                        }
                        .firstOrNull()
                    if (candidate != null) {
                        return@use zip.getInputStream(candidate).readBytes()
                    }
                }
                null
            }
        } catch (_: Exception) { null }
    }

    private fun scheduleRunningStatusRefresh(delayMillis: Long = 500L) {
        runningStatusJob?.cancel()
        runningStatusJob = viewModelScope.launch(Dispatchers.IO) {
            if (delayMillis > 0L) delay(delayMillis)
            refreshRunningStatus()
        }
    }

    private fun refreshRunningStatus() {
        val runningProcesses = AdbTool.exec("dumpsys activity processes")
        val runningPackages = parseRunningPackagesFromActivityProcesses(runningProcesses)
        // 优先用显式列 ps 解析运存，失败时回退默认 ps 格式
        val memoryBytesByPackage = parseMemoryBytes(AdbTool.exec("ps -A -o PID,RSS,NAME"))
            .ifEmpty { parseMemoryBytesFromDefaultPs(AdbTool.exec("ps -A")) }

        if (runningPackages.isEmpty() && memoryBytesByPackage.isEmpty()) return

        _appList.value = _appList.value.map { app ->
            app.copy(
                isRunning = if (runningPackages.isNotEmpty()) {
                    runningPackages.contains(app.packageName)
                } else {
                    app.isRunning
                },
                memoryBytes = memoryBytesByPackage[app.packageName]
            )
        }
    }

    // 解析 "ps -o PID,RSS,NAME" 输出：每行三列 PID RSS NAME
    private fun parseMemoryBytes(psOutput: String): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        psOutput.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("PID") || trimmed.startsWith("USER")) return@forEach
            val parts = trimmed.split(Regex("\\s+"), limit = 3)
            if (parts.size < 3) return@forEach
            val rssKb = parts[1].toLongOrNull() ?: return@forEach
            val name = parts[2]
            if (name.contains(".")) {
                val pkg = name.substringBefore(":")
                result[pkg] = (result[pkg] ?: 0L) + rssKb * 1024L
            }
        }
        return result
    }

    // 回退解析默认 "ps -A" 输出：RSS 为第 5 列
    private fun parseMemoryBytesFromDefaultPs(psOutput: String): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        psOutput.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("USER") || trimmed.startsWith("PID")) return@forEach
            val parts = trimmed.split(Regex("\\s+"))
            if (parts.size < 5) return@forEach
            val rssKb = parts[4].toLongOrNull() ?: return@forEach
            val name = parts.last()
            if (name.contains(".")) {
                val pkg = name.substringBefore(":")
                result[pkg] = (result[pkg] ?: 0L) + rssKb * 1024L
            }
        }
        return result
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun executeAdbAction(type: AdbFunctionType, packageName: String) {
        val appName = _appList.value.find { it.packageName == packageName }?.appName ?: packageName
        if (type == AdbFunctionType.APP_INFO) {
            openAppInfo(packageName)
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    when (type) {
                        AdbFunctionType.UNINSTALL -> {
                            val result = AdbTool.exec(appPackageShellCommand("pm", "uninstall", packageName))
                            if (result.contains("Success")) {
                                withContext(Dispatchers.Main) {
                                    getAppList(forceRefresh = true)
                                    showTipDialog(MsgContent.Resource(Res.string.dialog_uninstall_success))
                                }
                            } else {
                                showTipDialog(MsgContent.Resource(Res.string.dialog_uninstall_failed))
                            }
                        }
                        AdbFunctionType.SUPER_UNINSTALL -> {
                            // root 卸载：尝试将 system 重挂载为可写，然后以 root 卸载
                            runCatching {
                                AdbTool.exec(appPackageShellCommand("su", "0", "mount", "-o", "remount,rw", "/system"))
                                AdbTool.exec(appPackageShellCommand("su", "0", "mount", "-o", "remount,rw", "/"))
                            }
                            val result = AdbTool.exec(
                                appPackageShellCommand("su", "0", "pm", "uninstall", "--user", "0", packageName)
                            )
                            if (result.contains("Success")) {
                                withContext(Dispatchers.Main) {
                                    getAppList(forceRefresh = true)
                                    showTipDialog(MsgContent.Resource(Res.string.dialog_uninstall_success))
                                }
                            } else {
                                showTipDialog(MsgContent.Resource(Res.string.dialog_uninstall_failed))
                            }
                        }
                        AdbFunctionType.DISABLE_APP -> {
                            AdbTool.exec(appPackageShellCommand("su", "0", "pm", "disable-user", packageName))
                            withContext(Dispatchers.Main) { getAppList(forceRefresh = true) }
                        }
                        AdbFunctionType.ENABLE_APP -> {
                            AdbTool.exec(appPackageShellCommand("su", "0", "pm", "enable", packageName))
                            withContext(Dispatchers.Main) { getAppList(forceRefresh = true) }
                        }
                        AdbFunctionType.LAUNCH -> {
                            AdbTool.exec(
                                appPackageShellCommand(
                                    "monkey",
                                    "-p",
                                    packageName,
                                    "-c",
                                    "android.intent.category.LAUNCHER",
                                    "1"
                                )
                            )
                            _appList.value = _appList.value.map { app ->
                                if (app.packageName == packageName) app.copy(isRunning = true) else app
                            }
                        }
                        AdbFunctionType.FORCE_STOP -> {
                            AdbTool.exec(appPackageShellCommand("am", "force-stop", packageName))
                            _appList.value = _appList.value.map { app ->
                                if (app.packageName == packageName) app.copy(isRunning = false) else app
                            }
                        }
                        AdbFunctionType.CLEAR_DATA -> AdbTool.exec(appPackageShellCommand("pm", "clear", packageName))
                        AdbFunctionType.EXPORT_APK -> {
                            val path = AdbTool.exec(appPackageShellCommand("pm", "path", packageName))
                                .lineSequence()
                                .firstOrNull { it.startsWith("package:") }
                                ?.removePrefix("package:")
                                ?.trim()
                            if (path != null) {
                                val folderPath = withContext(Dispatchers.Main) { FileUtils.selectFolder() }
                                if (folderPath != null) {
                                    val savePath = "$folderPath/${appName}_${System.currentTimeMillis()}.apk"
                                    val success = AdbTool.pullFile(path, savePath)
                                    showTipDialog(MsgContent.Resource(if (success) Res.string.dialog_export_success else Res.string.dialog_export_failed))
                                } else {
                                    showTipDialog(MsgContent.Resource(Res.string.dialog_no_export_path))
                                }
                            } else {
                                showTipDialog(MsgContent.Resource(Res.string.dialog_get_install_path_failed))
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                showTipDialog(MsgContent.Resource(Res.string.dialog_operation_failed))
            }
        }
    }

    fun openAppInfo(packageName: String) {
        val app = _appList.value.firstOrNull { it.packageName == packageName }
        _appInfo.value = AppInfoData(
            appName = app?.appName ?: packageName,
            packageName = packageName,
            versionName = app?.versionName.orEmpty(),
            isSystemApp = app?.isSystemApp == true,
            isRunning = app?.isRunning == true,
            apkPath = app?.apkPath.orEmpty(),
            appSize = app?.size ?: "-",
            totalSize = app?.size ?: "-"
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val dumpsys = AdbTool.exec(appPackageShellCommand("dumpsys", "package", packageName))
                val versionName = parseVersionName(dumpsys) ?: "-"
                val versionCode = Regex("versionCode=([^\\s]+)").find(dumpsys)?.groupValues?.get(1) ?: "-"
                val minSdk = Regex("minSdk=([^\\s]+)").find(dumpsys)?.groupValues?.get(1) ?: "-"
                val targetSdk = Regex("targetSdk=([^\\s]+)").find(dumpsys)?.groupValues?.get(1) ?: "-"
                val uid = Regex("userId=([^\\s]+)").find(dumpsys)?.groupValues?.get(1) ?: "-"
                val firstInstallTime = Regex("firstInstallTime=([^\n]+)").find(dumpsys)?.groupValues?.get(1)?.trim() ?: "-"
                val lastUpdateTime = Regex("lastUpdateTime=([^\n]+)").find(dumpsys)?.groupValues?.get(1)?.trim() ?: "-"
                val supportedAbi = Regex("primaryCpuAbi=([^\n]+)").find(dumpsys)?.groupValues?.get(1)?.trim() ?: "-"
                val isSystemApp = dumpsys.contains("pkgFlags=[") && dumpsys.substringAfter("pkgFlags=[").substringBefore("]").contains("SYSTEM")
                val packagePath = app?.apkPath?.takeIf { it.isNotBlank() }
                    ?: AdbTool.exec(appPackageShellCommand("pm", "path", packageName)).lineSequence()
                        .firstOrNull { it.startsWith("package:") }
                        ?.removePrefix("package:")
                        ?.trim()
                        .orEmpty()
                val sizeBytes = queryPackageSizeBytes(packagePath, packageName) ?: app?.sizeBytes
                val sizeText = app?.size?.takeIf { it != "-" } ?: sizeBytes?.let { formatSize(it) } ?: "-"
                val processId = AdbTool.exec(appPackageShellCommand("pidof", packageName)).trim().takeIf { it.isNotEmpty() } ?: "-"
                val isRunning = processId != "-"
                val permissionStats = parsePermissionStats(dumpsys)
                val permissionDetails = parsePermissionDetails(dumpsys)
                val activityDetails = parseActivityDetails(dumpsys, packageName)
                val serviceDetails = parseServiceDetails(dumpsys, packageName)
                val receiverDetails = parseReceiverDetails(dumpsys, packageName)
                val providerDetails = parseProviderDetails(dumpsys, packageName)
                val signatureDetails = parseSignatureDetails(dumpsys)

                val info = AppInfoData(
                    appName = app?.appName ?: packageName,
                    packageName = packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    minSdk = minSdk,
                    targetSdk = targetSdk,
                    uid = uid,
                    firstInstallTime = firstInstallTime,
                    lastUpdateTime = lastUpdateTime,
                    supportedAbi = supportedAbi,
                    isSystemApp = isSystemApp,
                    isRunning = isRunning,
                    apkPath = packagePath,
                    dataDir = "/data/user/0/$packageName",
                    installLocation = if (isSystemApp) "系统分区" else "内部存储",
                    appSize = sizeText,
                    totalSize = sizeText,
                    processId = processId,
                    memoryUsage = if (isRunning) "运行中" else "-",
                    startTime = if (isRunning) "已启动" else "-",
                    dangerousPermissionCount = permissionStats.dangerous,
                    privacyPermissionCount = permissionStats.privacy,
                    normalPermissionCount = permissionStats.normal,
                    totalPermissionCount = permissionStats.total,
                    permissionDetails = permissionDetails,
                    activityDetails = activityDetails,
                    serviceDetails = serviceDetails,
                    receiverDetails = receiverDetails,
                    providerDetails = providerDetails,
                    signatureDetails = signatureDetails
                )

                withContext(Dispatchers.Main) {
                    if (app != null) {
                        _appList.value = _appList.value.map { current ->
                            if (current.packageName == packageName) {
                                current.copy(
                                    versionName = versionName,
                                    installTime = firstInstallTime,
                                    size = sizeText,
                                    sizeBytes = sizeBytes,
                                    installTimestamp = parseInstallTimestamp(firstInstallTime),
                                    lastUsedTimestamp = parseInstallTimestamp(lastUpdateTime)
                                        ?: parseInstallTimestamp(firstInstallTime),
                                    isDebuggable = pkgFlagsContainsDebuggable(dumpsys) || current.isDebuggable,
                                    isDisabled = parseDisabledState(dumpsys) || current.isDisabled,
                                    isRunning = isRunning
                                )
                            } else {
                                current
                            }
                        }
                    }
                    _appInfo.value = info
                }
            }.onFailure {
                // Keep initial detail page visible even if deep dumpsys parsing fails.
            }
        }
    }

    private data class PermissionStats(
        val dangerous: Int,
        val privacy: Int,
        val normal: Int,
        val total: Int
    )

    private fun parsePermissionStats(dumpsys: String): PermissionStats {
        val permissions = parsePermissionDetails(dumpsys).toSet()
        val dangerousKeywords = listOf(
            "CAMERA",
            "LOCATION",
            "RECORD_AUDIO",
            "CONTACTS",
            "SMS",
            "PHONE",
            "CALENDAR",
            "BODY_SENSORS",
            "STORAGE",
            "BLUETOOTH"
        )
        val dangerous = permissions.count { permission ->
            dangerousKeywords.any { keyword -> permission.contains(keyword) }
        }
        val privacy = permissions.count { permission ->
            listOf("CAMERA", "LOCATION", "RECORD_AUDIO", "CONTACTS", "SMS", "PHONE").any { keyword ->
                permission.contains(keyword)
            }
        }
        val normal = (permissions.size - dangerous).coerceAtLeast(0)
        return PermissionStats(
            dangerous = dangerous,
            privacy = privacy,
            normal = normal,
            total = permissions.size
        )
    }

    private fun pkgFlagsContainsDebuggable(dumpsys: String): Boolean {
        val flags = Regex("pkgFlags=\\[([^\\]]+)]").find(dumpsys)?.groupValues?.get(1).orEmpty()
        return flags.contains("DEBUGGABLE", ignoreCase = true)
    }

    private fun parseVersionName(dumpsys: String): String? {
        return Regex("""(?m)^\s*versionName=(.*)$""")
            .find(dumpsys)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != "null" }
    }

    private fun parseDisabledState(dumpsys: String): Boolean {
        val enabledToken = Regex("enabled=(\\S+)").find(dumpsys)?.groupValues?.get(1)?.lowercase()
        return enabledToken == "2" ||
            enabledToken == "3" ||
            enabledToken == "4" ||
            enabledToken == "false" ||
            enabledToken == "disabled" ||
            enabledToken == "disabled-user" ||
            enabledToken == "disabled_until_used"
    }

    private fun parsePermissionDetails(dumpsys: String): List<String> {
        val permissionPattern = Regex("""\b(?:[a-zA-Z0-9_]+\.)*permission\.[A-Za-z0-9_\.]+\b""")
        return permissionPattern.findAll(dumpsys)
            .map { it.value.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
            .sorted()
    }

    private fun parseActivityDetails(dumpsys: String, packageName: String): List<String> {
        return parseComponentDetails(dumpsys, packageName, listOf("activity", "launcheractivity", "resumedactivity"))
    }

    private fun parseServiceDetails(dumpsys: String, packageName: String): List<String> {
        return parseComponentDetails(dumpsys, packageName, listOf("service"))
    }

    private fun parseReceiverDetails(dumpsys: String, packageName: String): List<String> {
        return parseComponentDetails(dumpsys, packageName, listOf("receiver"))
    }

    private fun parseProviderDetails(dumpsys: String, packageName: String): List<String> {
        return parseComponentDetails(dumpsys, packageName, listOf("provider", "contentprovider"))
    }

    private fun parseComponentDetails(
        dumpsys: String,
        packageName: String,
        keywords: List<String>
    ): List<String> {
        val pkgEscaped = Regex.escape(packageName)
        val slashPattern = Regex("""\b$pkgEscaped/[A-Za-z0-9_.$]+\b""")
        val classPattern = Regex("""\b$pkgEscaped\.[A-Za-z0-9_.$]+\b""")
        val candidates = linkedSetOf<String>()

        dumpsys.lineSequence().forEach { line ->
            val lower = line.lowercase()
            if (keywords.none { lower.contains(it) }) return@forEach
            slashPattern.findAll(line).forEach { match ->
                normalizeComponentName(packageName, match.value)?.let { candidates.add(it) }
            }
            classPattern.findAll(line).forEach { match ->
                normalizeComponentName(packageName, match.value)?.let { candidates.add(it) }
            }
        }

        return candidates.toList().sorted()
    }

    private fun normalizeComponentName(packageName: String, raw: String): String? {
        val value = raw.trim().trim(',', ';', ')', '(', '[', ']')
        if (value.isEmpty()) return null
        if ('/' !in value) return value

        val classPart = value.substringAfter("/")
        if (classPart.isBlank()) return null
        return if (classPart.startsWith(".")) {
            "$packageName$classPart"
        } else {
            classPart
        }
    }

    private fun parseSignatureDetails(dumpsys: String): List<String> {
        val signatureKeywords = listOf("signature", "signing", "cert", "certificate", "sha-256", "sha1", "md5")
        val lines = dumpsys.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { line ->
                val lower = line.lowercase()
                signatureKeywords.any { lower.contains(it) }
            }
            .toList()

        return if (lines.isEmpty()) emptyList() else lines.distinct().take(50)
    }

    private fun queryPackageSizeBytes(packagePath: String, packageName: String): Long? {
        val paths = packageApkPaths(packagePath, packageName)
        if (paths.isEmpty()) return null

        val statSizes = paths.mapNotNull { path -> queryRemoteFileSizeBytes(path) }
        if (statSizes.isNotEmpty()) return statSizes.sum().takeIf { it > 0L }

        val duSizes = paths.mapNotNull { path -> queryRemoteDuSizeBytes(path) }
        return duSizes.sum().takeIf { it > 0L }
    }

    private fun packageApkPaths(packagePath: String, packageName: String): List<String> {
        val paths = linkedSetOf<String>()
        packagePath.trim().takeIf { it.isNotEmpty() }?.let { paths.add(it) }
        AdbTool.exec(appPackageShellCommand("pm", "path", packageName)).lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").trim() }
            .filter { it.isNotEmpty() }
            .forEach { paths.add(it) }
        return paths.toList()
    }

    private fun queryRemoteFileSizeBytes(path: String): Long? {
        return AdbTool.exec(appPathShellCommand("stat", "-c", "%s", path))
            .lineSequence()
            .mapNotNull { it.trim().toLongOrNull() }
            .firstOrNull { it > 0L }
    }

    private fun queryRemoteDuSizeBytes(path: String): Long? {
        val duOutput = AdbTool.exec(appPathShellCommand("du", "-k", path))
        val kb = Regex("""^\s*(\d+)""").find(duOutput)?.groupValues?.getOrNull(1)?.toLongOrNull()
        return kb?.takeIf { it > 0L }?.let { it * 1024L }
    }

    private fun queryPackageRunning(packageName: String): Boolean? {
        val output = AdbTool.exec(appPackageShellCommand("pidof", packageName)).trim()
        if (output.isNotEmpty() && !output.startsWith("Command failed")) return true
        return false
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0L) return "-"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun copyToClipboard(text: String) {
        val copied = try {
            val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
            true
        } catch (_: Exception) {
            false
        }
        showTipDialog(
            MsgContent.Text(if (copied) "已复制到剪贴板" else "复制失败"),
            autoDismiss = copied
        )
    }

    private fun parseInstallTimestamp(raw: String): Long? {
        if (raw.isBlank() || raw == "-") return null
        val normalized = raw.trim()
        val patterns = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS"
        )
        for (pattern in patterns) {
            val parsed = runCatching {
                LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern(pattern))
            }.getOrNull() ?: continue
            return parsed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        return null
    }
}
