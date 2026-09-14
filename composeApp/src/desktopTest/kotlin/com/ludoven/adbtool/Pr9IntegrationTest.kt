package com.ludoven.adbtool

import com.ludoven.adbtool.entity.LogEntry
import com.ludoven.adbtool.pages.snapshotLogSelection
import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.viewmodel.AppViewModel
import com.ludoven.adbtool.viewmodel.snapshotUploadTargets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Pr9IntegrationTest {
    @Test
    fun `upload plan retains directory and exact local filenames`() {
        var directory = "/sdcard/first"
        val paths = mutableListOf("/tmp/first.apk", "/tmp/file with trailing space ")
        val targets = snapshotUploadTargets(paths, directory)
        directory = "/sdcard/second"
        paths.clear()
        assertEquals("/sdcard/second", directory)
        assertEquals(listOf(
            "/tmp/first.apk" to "/sdcard/first/first.apk",
            "/tmp/file with trailing space " to "/sdcard/first/file with trailing space "
        ), targets)
        assertEquals(listOf("C:\\local\\app.apk" to "/app.apk"), snapshotUploadTargets(listOf("C:\\local\\app.apk"), "/"))
    }

    @Test
    fun `selected log snapshot survives filtering and buffer eviction`() {
        val logs = mutableListOf(LogEntry(message = "one"), LogEntry(message = "two"), LogEntry(message = "three"))
        val selected = snapshotLogSelection(logs, 1, 0)
        logs.removeAt(0)
        logs.add(LogEntry(message = "four"))
        logs.clear()
        assertEquals(listOf("one", "two"), selected.map { it.message })
    }

    @Test
    fun `folder picker is single flight and cancellation releases install lock`() = runBlocking {
        withTimeout(5_000) {
            withContext(Dispatchers.Main) {
                val model = AppViewModel()
                val picker = CompletableDeferred<String?>()
                var picks = 0
                val choose: suspend () -> String? = { picks++; picker.await() }
                val install: suspend (String, String) -> AdbTool.AdbResult = { _, _ -> error("Unexpected install") }
                model.batchInstallFromFolder("device", choose, install, {})
                try {
                    assertTrue(model.isInstalling.value)
                    model.batchInstallFromFolder("device", choose, install, {})
                    assertEquals(1, picks)
                } finally {
                    picker.complete(null)
                }
                model.isInstalling.first { !it }
                assertNull(model.currentInstallingProgress.value)
                model.batchInstallFromFolder("device", { throw CancellationException("cancelled") }, install, {})
                model.isInstalling.first { !it }
                model.batchInstallFromFolder("device", { throw IllegalStateException("picker failed") }, install, {})
                model.isInstalling.first { !it }
                assertFalse(model.isInstalling.value)
                model.dismissTipDialog()
            }
        }
    }

    @Test
    fun `installation continues after failure and progress counts all attempts`() = runBlocking {
        val folder = Files.createTempDirectory("qadb-install-test").toFile()
        try {
            folder.resolve("a.apk").writeText("fixture")
            folder.resolve("b.APK").writeText("fixture")
            folder.resolve("ignore.txt").writeText("fixture")
            withTimeout(5_000) {
                withContext(Dispatchers.Main) {
                    val model = AppViewModel()
                    val calls = mutableListOf<String>()
                    val progress = mutableListOf<String?>()
                    var refreshed = false
                    model.batchInstallFromFolder("original-device", { folder.absolutePath }, { path, device ->
                        assertEquals("original-device", device)
                        calls += path
                        progress += model.currentInstallingProgress.value
                        if (calls.size == 1) error("install failure")
                        AdbTool.AdbResult(true, "installed")
                    }, {
                        progress += model.currentInstallingProgress.value
                        refreshed = true
                    })
                    model.isInstalling.first { !it }
                    assertEquals(2, calls.size)
                    assertEquals(listOf<String?>("0/2", "1/2", "2/2"), progress)
                    assertTrue(refreshed)
                    assertNull(model.currentInstallingProgress.value)
                    model.dismissTipDialog()
                }
            }
        } finally {
            folder.deleteRecursively()
        }
    }
}
