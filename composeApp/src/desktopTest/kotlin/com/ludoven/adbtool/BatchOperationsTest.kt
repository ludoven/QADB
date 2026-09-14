package com.ludoven.adbtool

import com.ludoven.adbtool.entity.AdbFunctionType
import com.ludoven.adbtool.pages.commonBatchCommandIsDestructive
import com.ludoven.adbtool.pages.commonBatchCommandSupported
import com.ludoven.adbtool.viewmodel.BatchDeviceResult
import com.ludoven.adbtool.viewmodel.BatchDeviceStatus
import com.ludoven.adbtool.viewmodel.BatchExecutionUiState
import com.ludoven.adbtool.viewmodel.BATCH_EXECUTION_CONCURRENCY_LIMIT
import com.ludoven.adbtool.viewmodel.normalizedBatchDeviceIds
import com.ludoven.adbtool.viewmodel.BatchExecutionViewModel
import com.ludoven.adbtool.util.AdbTool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BatchOperationsTest {
    @Test
    fun `batch targets are trimmed deduplicated and keep input order`() {
        assertEquals(
            listOf("device-a", "device-b"),
            normalizedBatchDeviceIds(listOf(" device-a ", "", "device-b", "device-a"))
        )
    }

    @Test
    fun `file and single-process actions stay outside batch v1`() {
        assertFalse(commonBatchCommandSupported(AdbFunctionType.INSTALL_APK))
        assertFalse(commonBatchCommandSupported(AdbFunctionType.DEVICE_MIRROR))
        assertFalse(commonBatchCommandSupported(AdbFunctionType.SCREEN_RECORD))
        assertTrue(commonBatchCommandSupported(AdbFunctionType.BATTERY_STATUS))
        assertEquals(4, BATCH_EXECUTION_CONCURRENCY_LIMIT)
    }

    @Test
    fun `destructive batch actions receive stronger confirmation`() {
        assertTrue(commonBatchCommandIsDestructive(AdbFunctionType.REBOOT_DEVICE))
        assertTrue(commonBatchCommandIsDestructive(AdbFunctionType.CLEAR_CACHE_AND_RESTART))
        assertFalse(commonBatchCommandIsDestructive(AdbFunctionType.WIFI_INFO))
    }

    @Test
    fun `batch progress counts terminal states and failures`() {
        val state = BatchExecutionUiState(
            devices = mapOf(
                "pending" to BatchDeviceResult("pending"),
                "running" to BatchDeviceResult("running", status = BatchDeviceStatus.RUNNING),
                "success" to BatchDeviceResult("success", status = BatchDeviceStatus.SUCCESS),
                "failed" to BatchDeviceResult("failed", status = BatchDeviceStatus.FAILED)
            )
        )

        assertEquals(2, state.completedCount)
        assertEquals(1, state.failedCount)
        assertTrue(state.isRunning)
    }
    @Test
    fun `late result from a cancelled batch cannot overwrite its replacement`() = runBlocking {
        withTimeout(5_000) {
            withContext(Dispatchers.Main) {
                val model = BatchExecutionViewModel()
                val entered = CompletableDeferred<Unit>()
                val release = CompletableDeferred<Unit>()
                val returned = CompletableDeferred<Unit>()
                model.execute("old", "old", listOf("device")) {
                    entered.complete(Unit)
                    try {
                        awaitCancellation()
                    } catch (_: CancellationException) {
                        // Simulate an executor that finishes after cancellation.
                        withContext(NonCancellable) { release.await() }
                        returned.complete(Unit)
                        AdbTool.AdbResult(true, "old result", null)
                    }
                }
                entered.await()
                try {
                    model.execute("new", "new", listOf("device")) {
                        AdbTool.AdbResult(true, "new result", null)
                    }
                    model.uiState.first { !it.isRunning }
                } finally {
                    release.complete(Unit)
                }
                returned.await()
                yield()
                assertEquals("new result", model.uiState.value.devices.getValue("device").output)
                assertEquals(0, model.uiState.value.failedCount)
            }
        }
    }

    @Test
    fun `cancelled executor does not publish failure into a running replacement`() = runBlocking {
        withTimeout(5_000) {
            withContext(Dispatchers.Main) {
                val model = BatchExecutionViewModel()
                val entered = CompletableDeferred<Unit>()
                val release = CompletableDeferred<Unit>()
                model.execute("old", "old", listOf("device")) {
                    entered.complete(Unit)
                    awaitCancellation()
                }
                entered.await()
                model.execute("new", "new", listOf("device")) {
                    release.await()
                    AdbTool.AdbResult(true, "new", null)
                }
                try {
                    yield()
                    assertTrue(model.uiState.value.isRunning)
                    assertEquals(0, model.uiState.value.failedCount)
                } finally {
                    release.complete(Unit)
                }
                model.uiState.first { !it.isRunning }
                assertEquals("new", model.uiState.value.devices.getValue("device").output)
            }
        }
    }

    @Test
    fun `execution limits concurrency and retries only failed devices`() = runBlocking {
        withTimeout(5_000) {
            withContext(Dispatchers.Main) {
                val model = BatchExecutionViewModel()
                val release = CompletableDeferred<Unit>()
                val full = CompletableDeferred<Unit>()
                val attempts = mutableMapOf<String, Int>()
                var active = 0
                var peak = 0
                model.execute("batch", "command", (1..6).map { "device-$it" }) { id ->
                    attempts[id] = attempts.getOrDefault(id, 0) + 1
                    active++
                    peak = maxOf(peak, active)
                    if (active == 4) full.complete(Unit)
                    try {
                        release.await()
                        if (id == "device-2" && attempts[id] == 1) {
                            error("temporary failure")
                        }
                        AdbTool.AdbResult(true, id, null)
                    } finally {
                        active--
                    }
                }
                try {
                    full.await()
                    assertEquals(4, attempts.size)
                    assertEquals(0, model.uiState.value.completedCount)
                } finally {
                    release.complete(Unit)
                }
                model.uiState.first { !it.isRunning }
                assertEquals(1, model.uiState.value.failedCount)
                model.retryFailed()
                model.uiState.first { !it.isRunning }
                assertEquals(4, peak)
                assertEquals(0, model.uiState.value.failedCount)
                assertEquals(2, attempts.getValue("device-2"))
                assertTrue(attempts.filterKeys { it != "device-2" }.values.all { it == 1 })
            }
        }
    }
}
