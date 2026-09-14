package com.ludoven.adbtool

import com.ludoven.adbtool.util.AdbTool
import com.ludoven.adbtool.viewmodel.AppViewModel
import com.ludoven.adbtool.viewmodel.FileBrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Opt-in smoke test. Uses two dedicated, permission-free fixture packages only. */
class Pr9DeviceSmokeTest {
    @Test
    fun `batch install and upload reach the selected physical device`() = runBlocking {
        val device = System.getenv("QADB_PR9_DEVICE").orEmpty()
        val fixturePath = System.getenv("QADB_PR9_APKS").orEmpty()
        assumeTrue("Requires QADB_PR9_DEVICE and QADB_PR9_APKS", device.isNotBlank() && fixturePath.isNotBlank())
        val packages = listOf("com.ludoven.qadb.pr9test.one", "com.ludoven.qadb.pr9test.two")
        val fixtureFolder = File(fixturePath)
        assertEquals(setOf("one.apk", "two.apk"), fixtureFolder.listFiles()?.filter { it.extension == "apk" }?.map { it.name }?.toSet())
        for (pkg in packages) {
            assertTrue(AdbTool.execShellAsync("pm path $pkg", device).output.isBlank(), "Fixture package already installed: $pkg")
        }
        val local = Files.createTempDirectory("qadb-pr9-device").toFile()
        val remote = "/sdcard/Download/qadb-pr9-${UUID.randomUUID()}"
        val first = local.resolve("one.txt").apply { writeText("QADB PR9 one") }
        val second = local.resolve("two.txt").apply { writeText("QADB PR9 two") }
        var files: FileBrowserViewModel? = null
        var apps: AppViewModel? = null
        try {
            withTimeout(120_000) {
                withContext(Dispatchers.Main) {
                    val appModel = AppViewModel().also { apps = it }
                    appModel.batchInstallFromFolder(device, { fixtureFolder.absolutePath }, { path, target ->
                        AdbTool.installApkAsync(path, target)
                    }, {})
                    appModel.isInstalling.first { !it }
                    for (pkg in packages) {
                        assertTrue(AdbTool.execShellAsync("pm path $pkg", device).output.startsWith("package:"), "Not installed: $pkg")
                    }
                    assertTrue(AdbTool.execShellAsync("mkdir -p $remote/original $remote/other", device).success)
                    val fileModel = FileBrowserViewModel().also { files = it }
                    fileModel.loadFiles("$remote/original", device)
                    fileModel.currentPath.first { it == "$remote/original" }
                    fileModel.pushFiles(listOf(first.absolutePath, second.absolutePath), device)
                    fileModel.loadFiles("$remote/other", device)
                    fileModel.feedbackToastMessage.first { it != null }
                    fileModel.currentPath.first { it == "$remote/other" }
                    for (file in listOf(first, second)) {
                        assertEquals(file.readText(), AdbTool.execShellAsync("cat $remote/original/${file.name}", device).output.trim())
                    }
                    assertTrue(AdbTool.execShellAsync("ls -A $remote/other", device).output.isBlank())
                }
            }
        } finally {
            withContext(NonCancellable + Dispatchers.Main) {
                files?.cancelActiveLoad()
                files?.dismissToast()
                files?.dismissTipDialog()
                apps?.dismissTipDialog()
                for (pkg in packages) {
                    val result = AdbTool.execAdbAsync("-s", device, "uninstall", pkg)
                    assertTrue(result.success, "Could not remove test package $pkg: ${result.output}")
                }
                assertTrue(AdbTool.execShellAsync("rm -rf $remote", device).success)
            }
            local.deleteRecursively()
        }
    }
}
