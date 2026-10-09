package com.ludoven.adbtool.agent.artemis

import java.nio.file.Path
import java.net.URI
import java.security.SecureRandom
import java.nio.file.Files

enum class ArtemisRuntimeState {
    STOPPED,
    STARTING,
    READY,
    UNHEALTHY,
    STOPPING,
    CRASHED;

    val isRunning: Boolean
        get() = this == STARTING || this == READY || this == UNHEALTHY
}

/** Configuration contains paths and ports only; model keys must never become command arguments. */
data class ManagedArtemisRuntimeSpec(
    val pythonExecutable: Path,
    val workingDirectory: Path,
    val port: Int,
    val bridgeModuleDirectory: Path,
    val bridgeLedgerPath: Path,
    val bridgeToken: String,
    val extraEnvironment: Map<String, String> = emptyMap()
) {
    init {
        require(port in 1024..65535) { "Managed Artemis port must be a user port" }
        require(bridgeToken.length >= 32) { "Bridge token is too short" }
    }

    fun command(): List<String> = listOf(
        pythonExecutable.toString(), "-m", "artemis.interfaces.cli.main", "ui",
        "--host", "127.0.0.1", "--port", port.toString(), "--no-open"
    )

    /** Never persist or display this map: it contains the per-instance control token. */
    fun environment(): Map<String, String> = mapOf(
        "PYTHONPATH" to bridgeModuleDirectory.toString(),
        "QADB_BRIDGE_ENABLED" to "1",
        "QADB_BRIDGE_URL" to "http://127.0.0.1:$port",
        "QADB_BRIDGE_TOKEN" to bridgeToken,
        "QADB_BRIDGE_LEDGER" to bridgeLedgerPath.toString(),
        "QADB_TASK_VERSION" to "0"
    ) + extraEnvironment
}

interface ArtemisOwnedProcess {
    val alive: Boolean
    fun destroy()
}

fun interface ArtemisRuntimeLauncher {
    fun start(command: List<String>, workingDirectory: Path, environment: Map<String, String>): ArtemisOwnedProcess
}

class ProcessBuilderArtemisRuntimeLauncher : ArtemisRuntimeLauncher {
    override fun start(command: List<String>, workingDirectory: Path, environment: Map<String, String>): ArtemisOwnedProcess {
        val process = ProcessBuilder(command)
            .directory(workingDirectory.toFile())
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .apply { this.environment().putAll(environment) }
            .start()
        return object : ArtemisOwnedProcess {
            override val alive: Boolean get() = process.isAlive
            override fun destroy() = process.destroy()
        }
    }
}

data class ManagedArtemisConnection(val baseUrl: String, internal val bridgeToken: String)

/** Starts only a configured local bundle; no external bare daemon is accepted. */
class ManagedArtemisRuntime(
    private val manager: ArtemisRuntimeManager = ArtemisRuntimeManager(ProcessBuilderArtemisRuntimeLauncher()),
    private val environment: Map<String, String> = System.getenv(),
    private val userDirectory: Path = Path.of(System.getProperty("user.dir")),
    private val temporaryDirectory: Path = Path.of(System.getProperty("java.io.tmpdir"))
) {
    private var connection: ManagedArtemisConnection? = null
    private var _state: ArtemisRuntimeState = ArtemisRuntimeState.STOPPED
    val state: ArtemisRuntimeState get() = _state

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread({ stop() }, "qadb-artemis-shutdown")
        )
    }

    @Synchronized fun checkHealth(): ArtemisRuntimeState {
        if (connection != null && !manager.isAlive()) {
            connection = null
            _state = ArtemisRuntimeState.CRASHED
        }
        return _state
    }

    @Synchronized fun requireConnection(
        baseUrl: String,
        extraEnvironment: Map<String, String> = emptyMap()
    ): ManagedArtemisConnection {
        checkHealth()
        connection?.let {
            if (_state == ArtemisRuntimeState.READY && manager.isAlive()) {
                return it
            }
            connection = null
        }
        check(_state != ArtemisRuntimeState.CRASHED) {
            "Managed Artemis runtime crashed; manual restart or reconciliation required"
        }

        val uri = URI(baseUrl)
        require(uri.host in setOf("127.0.0.1", "localhost")) { "Managed Artemis must use loopback" }
        val paths = resolveManagedArtemisPaths(environment, userDirectory, temporaryDirectory)
        val port = if (uri.port > 0) uri.port else 80
        val ledgerDir = Path.of(System.getProperty("user.home"), ".qadb", "artemis")
        Files.createDirectories(ledgerDir)
        val spec = ArtemisRuntimeManager.newSpec(
            paths.pythonExecutable,
            paths.runtimeDirectory,
            port,
            paths.bridgeDirectory,
            ledgerDir.resolve("bridge.sqlite"),
            extraEnvironment = extraEnvironment
        )
        _state = ArtemisRuntimeState.STARTING
        try {
            manager.start(spec)
            _state = ArtemisRuntimeState.READY
            val conn = ManagedArtemisConnection("http://127.0.0.1:$port", spec.bridgeToken)
            connection = conn
            return conn
        } catch (t: Throwable) {
            _state = ArtemisRuntimeState.CRASHED
            connection = null
            throw t
        }
    }

    @Synchronized fun stop(): Boolean {
        _state = ArtemisRuntimeState.STOPPING
        val stopped = manager.stopOwnedRuntime()
        connection = null
        _state = ArtemisRuntimeState.STOPPED
        return stopped
    }

    @Synchronized fun resetCrashed() {
        if (_state == ArtemisRuntimeState.CRASHED) {
            _state = ArtemisRuntimeState.STOPPED
            connection = null
        }
    }
}

internal data class ManagedArtemisPaths(
    val pythonExecutable: Path,
    val runtimeDirectory: Path,
    val bridgeDirectory: Path
)

/**
 * Environment variables remain authoritative for packaged/custom installs.
 * Development builds can use the checked-out Bridge plus the locked local
 * Artemis checkout without requiring shell-only launch configuration.
 */
internal fun resolveManagedArtemisPaths(
    environment: Map<String, String>,
    userDirectory: Path,
    temporaryDirectory: Path
): ManagedArtemisPaths {
    val configuredRuntime = environment["QADB_ARTEMIS_RUNTIME_DIR"]
        ?.takeIf(String::isNotBlank)
        ?.let(Path::of)
    val runtimeCandidates = buildList {
        configuredRuntime?.let(::add)
        add(temporaryDirectory.resolve("qadb-artemis-upstream"))
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            add(Path.of("/private/tmp/qadb-artemis-upstream"))
        }
    }.distinct()
    val runtime = runtimeCandidates.firstOrNull { candidate ->
        Files.isDirectory(candidate) && Files.isRegularFile(candidate.resolve("pyproject.toml"))
    } ?: error("Artemis runtime not found; set QADB_ARTEMIS_RUNTIME_DIR")

    val configuredPython = environment["QADB_ARTEMIS_PYTHON"]
        ?.takeIf(String::isNotBlank)
        ?.let(Path::of)
    val python = listOfNotNull(
        configuredPython,
        runtime.resolve(".venv/bin/python"),
        runtime.resolve(".venv/Scripts/python.exe")
    ).firstOrNull(Files::isRegularFile)
        ?: error("Artemis Python not found; set QADB_ARTEMIS_PYTHON")

    val configuredBridge = environment["QADB_ARTEMIS_BRIDGE_DIR"]
        ?.takeIf(String::isNotBlank)
        ?.let(Path::of)
    val repositoryBridgeCandidates = generateSequence(userDirectory.toAbsolutePath()) { it.parent }
        .take(6)
        .map { it.resolve("integrations/artemis/bridge") }
        .toList()
    val bridge = (listOfNotNull(configuredBridge) + repositoryBridgeCandidates).firstOrNull { candidate ->
        Files.isRegularFile(candidate.resolve("qadb_bridge.py")) &&
            Files.isRegularFile(candidate.resolve("qadb_bridge_app.py"))
    } ?: error("QADB Artemis Bridge not found; set QADB_ARTEMIS_BRIDGE_DIR")

    return ManagedArtemisPaths(
        pythonExecutable = python.toAbsolutePath().normalize(),
        runtimeDirectory = runtime.toAbsolutePath().normalize(),
        bridgeDirectory = bridge.toAbsolutePath().normalize()
    )
}

/** Manages only a process it started; it never discovers or kills arbitrary Python processes. */
class ArtemisRuntimeManager(private val launcher: ArtemisRuntimeLauncher) {
    private var ownedProcess: ArtemisOwnedProcess? = null

    fun start(spec: ManagedArtemisRuntimeSpec): ArtemisOwnedProcess {
        check(ownedProcess?.alive != true) { "A QADB-managed Artemis runtime is already running" }
        return launcher.start(spec.command(), spec.workingDirectory, spec.environment()).also { ownedProcess = it }
    }

    fun isAlive(): Boolean = ownedProcess?.alive == true

    fun stopOwnedRuntime(): Boolean {
        val process = ownedProcess ?: return false
        if (process.alive) process.destroy()
        ownedProcess = null
        return true
    }

    companion object {
        /** One-time token creation; callers must keep the returned spec process-local. */
        fun newSpec(
            pythonExecutable: Path,
            workingDirectory: Path,
            port: Int,
            bridgeModuleDirectory: Path,
            bridgeLedgerPath: Path,
            extraEnvironment: Map<String, String> = emptyMap()
        ): ManagedArtemisRuntimeSpec = ManagedArtemisRuntimeSpec(
            pythonExecutable = pythonExecutable,
            workingDirectory = workingDirectory,
            port = port,
            bridgeModuleDirectory = bridgeModuleDirectory,
            bridgeLedgerPath = bridgeLedgerPath,
            bridgeToken = ByteArray(32).also(SecureRandom()::nextBytes).joinToString("") { "%02x".format(it) },
            extraEnvironment = extraEnvironment
        )
    }
}
