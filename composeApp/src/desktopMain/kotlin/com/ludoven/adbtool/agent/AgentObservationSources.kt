package com.ludoven.adbtool.agent

import com.ludoven.adbtool.util.AdbPathManager
import com.ludoven.adbtool.util.AdbProcessTimeoutContext
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.util.ChildProcessRegistry
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class AdbObservationSource(
    private val uiParser: UiHierarchyParser = UiHierarchyParser(),
    private val screenshotProcessor: AgentScreenshotProcessor = AgentScreenshotProcessor()
) {
    private val displaySizeCache = ConcurrentHashMap<String, Pair<Int, Int>>()
    private val directDumpSupport = ConcurrentHashMap<String, Boolean>()

    suspend fun capabilities(deviceId: String): AgentDeviceCapabilities = AgentDeviceCapabilities(
        observationSource = AgentObservationSource.ADB,
        uiHierarchy = true,
        screenshots = true,
        semanticActions = false,
        incrementalEvents = false,
        unicodeInput = false
    )

    /** Reuses the observation parser for the final pre-dispatch foreground check. */
    suspend fun currentActivity(deviceId: String): String = loadCurrentActivity(deviceId)

    suspend fun observe(deviceId: String, includeScreenshot: Boolean, includeUiHierarchy: Boolean): AgentObservation = coroutineScope {
        val totalStarted = nowMs()
        val activityTask = async { timed { loadCurrentActivity(deviceId) } }
        val displayTask = async { timed { loadDisplaySize(deviceId) } }
        val hierarchyTask = if (includeUiHierarchy) async { timed { loadUiHierarchy(deviceId) } } else null
        val screenshotTask = if (includeScreenshot) async { timed { captureScreenshotPng(deviceId) } } else null

        val warnings = mutableListOf<String>()
        val activityResult = activityTask.await()
        val displayResult = displayTask.await()
        val hierarchyResult = hierarchyTask?.await()
        val screenshotResult = screenshotTask?.await()
        val screenshot = screenshotResult?.value?.getOrNull()?.let(screenshotProcessor::process)
        if (includeScreenshot && screenshot == null) warnings += "Screenshot unavailable"
        val displaySize = screenshot?.let { it.width to it.height }
            ?: displayResult.value.getOrNull()
            ?: throw AgentException("Unable to read the device display size")
        val rawHierarchy = hierarchyResult?.value?.getOrElse {
            warnings += "UI hierarchy unavailable"
            ""
        }.orEmpty()
        val parsed = uiParser.parse(rawHierarchy, displaySize.first, displaySize.second)
        val activity = activityResult.value.getOrElse {
            warnings += "Current Activity unavailable"
            ""
        }
        val capturedAt = System.currentTimeMillis()
        AgentObservation(
            screenshotPng = screenshot?.bytes,
            screenshotMimeType = screenshot?.mimeType ?: "image/jpeg",
            uiHierarchy = parsed.compactText,
            uiNodes = parsed.nodes,
            currentActivity = activity,
            screenWidth = displaySize.first,
            screenHeight = displaySize.second,
            warnings = warnings.distinct(),
            source = AgentObservationSource.ADB,
            capturedAtMs = capturedAt,
            revision = stableRevision(activity, parsed.compactText),
            timings = AgentObservationTimings(
                totalMs = nowMs() - totalStarted,
                hierarchyMs = hierarchyResult?.elapsedMs ?: 0,
                activityMs = activityResult.elapsedMs,
                displayMs = displayResult.elapsedMs,
                screenshotMs = screenshotResult?.elapsedMs ?: 0
            ),
            capabilities = capabilities(deviceId)
        )
    }

    suspend fun readSystemProbe(deviceId: String, probeId: String): String? = when (probeId) {
        "airplane_mode" -> settingsValue(deviceId, "global", "airplane_mode_on")?.normalizeToggle()
        "wifi" -> AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "cmd", "wifi", "status")
            .lineSequence().firstOrNull()?.contains("enabled", ignoreCase = true)?.toString()
        "bluetooth" -> settingsValue(deviceId, "global", "bluetooth_on")?.normalizeToggle()
        "brightness" -> settingsValue(deviceId, "system", "screen_brightness")
        "rotation_auto" -> settingsValue(deviceId, "system", "accelerometer_rotation")?.normalizeToggle()
        "rotation_locked" -> settingsValue(deviceId, "system", "accelerometer_rotation")
            ?.let { (it.trim() == "0").toString() }
        else -> null
    }

    private suspend fun settingsValue(deviceId: String, namespace: String, key: String): String? =
        AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "settings", "get", namespace, key)
            .trim().takeIf { it.isNotEmpty() && it != "null" }

    private suspend fun loadUiHierarchy(deviceId: String): String {
        if (directDumpSupport[deviceId] != false) {
            val direct = runCatching {
                AdbTool.execAdbOutputAsync("-s", deviceId, "exec-out", "uiautomator", "dump", "/dev/tty")
            }.getOrDefault("")
            val xml = direct.substringFromHierarchy()
            if (xml.isNotBlank()) {
                directDumpSupport[deviceId] = true
                return xml.take(MAX_RAW_UI_HIERARCHY_LENGTH)
            }
            directDumpSupport[deviceId] = false
        }
        val remotePath = "/sdcard/qadb_agent_window.xml"
        return try {
            val dump = AdbTool.execAdbAsync("-s", deviceId, "shell", "uiautomator", "dump", remotePath)
            if (!dump.success) throw AgentException("Unable to capture UI hierarchy")
            AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "cat", remotePath)
                .substringFromHierarchy().take(MAX_RAW_UI_HIERARCHY_LENGTH)
        } finally {
            runCatching { AdbTool.execAdbAsync("-s", deviceId, "shell", "rm", "-f", remotePath) }
        }
    }

    private suspend fun loadCurrentActivity(deviceId: String): String = parseCurrentActivity(
        AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "dumpsys", "activity", "activities")
    )

    private suspend fun loadDisplaySize(deviceId: String): Pair<Int, Int>? {
        displaySizeCache[deviceId]?.let { return it }
        val output = AdbTool.execAdbOutputAsync("-s", deviceId, "shell", "wm", "size")
        val match = SCREEN_SIZE_PATTERN.findAll(output).lastOrNull() ?: return null
        return (match.groupValues[1].toInt() to match.groupValues[2].toInt())
            .also { displaySizeCache[deviceId] = it }
    }

    private suspend fun captureScreenshotPng(deviceId: String): ByteArray? = withContext(Dispatchers.IO) {
        val commandTimeoutMillis = AdbProcessTimeoutContext.clampTimeoutMillis(ADB_TIMEOUT_MILLIS)
        if (commandTimeoutMillis <= 0L) return@withContext null
        val adbPath = AdbPathManager.getAdbPath() ?: return@withContext null
        if (AdbProcessTimeoutContext.clampTimeoutMillis(commandTimeoutMillis) <= 0L) {
            return@withContext null
        }
        val process = runCatching {
            ProcessBuilder(adbPath, "-s", deviceId, "exec-out", "screencap", "-p")
                .redirectErrorStream(false).start()
        }.getOrNull() ?: return@withContext null
        ChildProcessRegistry.register(process)
        val output = CompletableFuture.supplyAsync { process.inputStream.use { it.readBytes() } }
        try {
            val waitTimeoutMillis = AdbProcessTimeoutContext.clampTimeoutMillis(commandTimeoutMillis)
            if (waitTimeoutMillis <= 0L || !process.waitFor(waitTimeoutMillis, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                return@withContext null
            }
            val outputTimeoutMillis = AdbProcessTimeoutContext.clampTimeoutMillis(
                SCREENSHOT_OUTPUT_TIMEOUT_MILLIS
            )
            if (outputTimeoutMillis <= 0L) return@withContext null
            output.get(outputTimeoutMillis, TimeUnit.MILLISECONDS)
                .takeIf { process.exitValue() == 0 && it.isNotEmpty() }
        } catch (_: Exception) {
            null
        } finally {
            if (process.isAlive) process.destroyForcibly()
            output.cancel(true)
            ChildProcessRegistry.unregister(process)
        }
    }

    private fun parseCurrentActivity(output: String): String = output.lineSequence()
        .firstOrNull { it.contains("mResumedActivity") || it.contains("topResumedActivity") || it.contains("ResumedActivity") }
        ?.substringAfter('{')?.substringBefore('}')
        ?.let(::normalizeActivityComponent)
        .orEmpty()

    private fun String.substringFromHierarchy(): String {
        val start = indexOf("<?xml").takeIf { it >= 0 } ?: indexOf("<hierarchy")
        return if (start >= 0) substring(start).substringBeforeLast("</hierarchy>", "")
            .let { if (it.isBlank()) substring(start) else "$it</hierarchy>" } else ""
    }

    private fun String.normalizeToggle(): String? = when (trim()) {
        "1", "true", "enabled" -> "true"
        "0", "false", "disabled" -> "false"
        else -> null
    }

    private suspend fun <T> timed(block: suspend () -> T): TimedResult<T> {
        val started = nowMs()
        return TimedResult(runCatching { block() }, nowMs() - started)
    }

    private fun stableRevision(activity: String, hierarchy: String): Long =
        (31L * activity.hashCode() + hierarchy.hashCode()).let { if (it == Long.MIN_VALUE) 0 else kotlin.math.abs(it) }
}

private data class TimedResult<T>(val value: Result<T>, val elapsedMs: Long)

private fun nowMs(): Long = System.nanoTime() / 1_000_000

private val SCREEN_SIZE_PATTERN = Regex("(\\d+)x(\\d+)")
private const val MAX_RAW_UI_HIERARCHY_LENGTH = 120_000
private const val ADB_TIMEOUT_MILLIS = 30_000L
private const val SCREENSHOT_OUTPUT_TIMEOUT_MILLIS = 2_000L
