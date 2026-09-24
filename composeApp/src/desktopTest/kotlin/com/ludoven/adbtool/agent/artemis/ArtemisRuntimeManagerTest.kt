package com.ludoven.adbtool.agent.artemis

import java.nio.file.Path
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArtemisRuntimeManagerTest {
    @Test
    fun `development runtime and repository bridge are discovered without shell variables`() {
        val root = Files.createTempDirectory("qadb-artemis-paths")
        try {
            val repositoryDirectory = root.resolve("qadb")
            val userDirectory = repositoryDirectory.resolve("composeApp")
            val temporaryDirectory = root.resolve("tmp")
            val runtime = temporaryDirectory.resolve("qadb-artemis-upstream")
            val python = runtime.resolve(".venv/bin/python")
            val bridge = repositoryDirectory.resolve("integrations/artemis/bridge")
            Files.createDirectories(python.parent)
            Files.createDirectories(bridge)
            Files.writeString(runtime.resolve("pyproject.toml"), "[project]\nname='artemis'\n")
            Files.writeString(python, "")
            Files.writeString(bridge.resolve("qadb_bridge.py"), "")
            Files.writeString(bridge.resolve("qadb_bridge_app.py"), "")

            val paths = resolveManagedArtemisPaths(emptyMap(), userDirectory, temporaryDirectory)

            assertEquals(python.toAbsolutePath(), paths.pythonExecutable)
            assertEquals(runtime.toAbsolutePath(), paths.runtimeDirectory)
            assertEquals(bridge.toAbsolutePath(), paths.bridgeDirectory)
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    @Test
    fun `manager starts loopback command and stops only its owned process`() {
        val process = FakeProcess()
        var command: List<String>? = null
        var environment: Map<String, String>? = null
        val manager = ArtemisRuntimeManager(ArtemisRuntimeLauncher { args, _, env -> command = args; environment = env; process })
        val spec = ManagedArtemisRuntimeSpec(
            Path.of("/runtime/python"),
            Path.of("/runtime"),
            18761,
            Path.of("/runtime/bridge"),
            Path.of("/runtime/data/bridge.sqlite"),
            "a".repeat(64)
        )

        manager.start(spec)
        assertEquals(listOf("/runtime/python", "-m", "artemis.interfaces.cli.main", "ui", "--host", "127.0.0.1", "--port", "18761", "--no-open"), command)
        assertEquals("1", environment?.get("QADB_BRIDGE_ENABLED"))
        assertEquals("http://127.0.0.1:18761", environment?.get("QADB_BRIDGE_URL"))
        assertEquals("/runtime/bridge", environment?.get("PYTHONPATH"))
        assertTrue(manager.stopOwnedRuntime())
        assertTrue(process.destroyed)
        assertFalse(manager.stopOwnedRuntime())
    }

    @Test
    fun `manager rejects a second live owned runtime`() {
        val manager = ArtemisRuntimeManager(ArtemisRuntimeLauncher { _, _, _ -> FakeProcess() })
        val spec = ManagedArtemisRuntimeSpec(
            Path.of("python"), Path.of("runtime"), 18761,
            Path.of("bridge"), Path.of("data/bridge.sqlite"), "a".repeat(64)
        )
        manager.start(spec)

        assertFailsWith<IllegalStateException> { manager.start(spec) }
    }

    private class FakeProcess : ArtemisOwnedProcess {
        override var alive: Boolean = true
        var destroyed = false
        override fun destroy() {
            destroyed = true
            alive = false
        }
    }
}
