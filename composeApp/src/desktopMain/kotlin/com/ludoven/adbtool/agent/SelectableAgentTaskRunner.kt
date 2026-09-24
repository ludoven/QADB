package com.ludoven.adbtool.agent

import com.ludoven.adbtool.agent.artemis.ArtemisAgentEngine
import com.ludoven.adbtool.agent.artemis.ArtemisHttpClient
import com.ludoven.adbtool.agent.artemis.ExternalTaskReconciler
import com.ludoven.adbtool.agent.artemis.ExternalTaskRuntime
import com.ludoven.adbtool.agent.artemis.QadbBridgeHttpClient
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay

/**
 * Chooses the engine once at run admission.  A later settings change therefore
 * cannot redirect an already-running task to a different device controller.
 */
class SelectableAgentTaskRunner(
    private val screenshotRunner: AgentTaskRunner,
    private val artemisSettings: ArtemisEnginePreferences = ArtemisEngineRuntime.preferences,
    private val advisoryRunner: AgentTaskRunner = AdvisoryAgentRunner()
) : CancellableAgentTaskRunner, AgentTaskRunnerReadiness, AgentTaskRunnerRecovery {
    private val active = ConcurrentHashMap<String, CancellableAgentTaskRunner>()

    override suspend fun run(
        task: String,
        deviceId: String,
        initialState: AgentTaskUiState,
        runId: String?,
        acceptedAtMs: Long?,
        firstFeedbackMs: Long?,
        onState: (AgentTaskUiState) -> Unit,
        confirmSensitiveAction: suspend (AgentStep) -> Boolean
    ): AgentTaskUiState {
        val resolvedRunId = runId.orEmpty()
        val configuration = artemisSettings.configuration.value
        val runner = if (initialState.taskMode != AgentTaskMode.EXECUTE) {
            advisoryRunner
        } else if (configuration.enabled) {
            requireArtemisRunner(configuration)
        } else {
            screenshotRunner
        }
        (runner as? CancellableAgentTaskRunner)?.let { cancellable ->
            if (resolvedRunId.isNotBlank()) active[resolvedRunId] = cancellable
        }
        return try {
            runner.run(
                task = task,
                deviceId = deviceId,
                initialState = initialState,
                runId = runId,
                acceptedAtMs = acceptedAtMs,
                firstFeedbackMs = firstFeedbackMs,
                onState = onState,
                confirmSensitiveAction = confirmSensitiveAction
            )
        } finally {
            if (resolvedRunId.isNotBlank()) active.remove(resolvedRunId)
        }
    }

    override suspend fun cancel(runId: String): Boolean = active.remove(runId)?.cancel(runId) ?: false

    override suspend fun readiness(): AgentRunnerReadiness {
        val configuration = artemisSettings.configuration.value
        if (!configuration.enabled) return AgentRunnerReadiness(ready = true)
        val artemisReadiness = probeArtemis(configuration)
        return artemisReadiness.copy(selectedEngine = AgentEngineKind.ARTEMIS)
    }

    override suspend fun reconcileExternalTasks() {
        val configuration = artemisSettings.configuration.value
        if (!configuration.enabled) return
        val connection = ArtemisEngineRuntime.managedRuntime.requireConnection(configuration.baseUrl)
        ExternalTaskReconciler(
            ExternalTaskRuntime.registry,
            ArtemisHttpClient(connection.baseUrl),
            QadbBridgeHttpClient(connection.baseUrl, connection.bridgeToken)
        ).reconcile()
    }

    private suspend fun requireArtemisRunner(configuration: ArtemisEngineConfiguration): AgentTaskRunner {
        check(configuration.enabled) { "Artemis engine is not enabled" }
        val candidate = createArtemisCandidate(configuration)
        val readiness = awaitAgentRunnerReadiness { candidate.readiness() }
        check(readiness.ready) { readiness.message ?: "Artemis engine is unavailable" }
        return candidate.runner
    }

    private suspend fun probeArtemis(configuration: ArtemisEngineConfiguration): AgentRunnerReadiness {
        if (!configuration.enabled) return AgentRunnerReadiness(ready = false)
        val candidate = runCatching { createArtemisCandidate(configuration) }
            .getOrElse { error ->
                return AgentRunnerReadiness(
                    ready = false,
                    message = error.message?.take(240) ?: "Local Artemis service is unavailable"
                )
            }
        return awaitAgentRunnerReadiness { candidate.readiness() }
    }

    private fun createArtemisCandidate(configuration: ArtemisEngineConfiguration): ArtemisCandidate {
        val connection = ArtemisEngineRuntime.managedRuntime.requireConnection(configuration.baseUrl)
        val client = ArtemisHttpClient(connection.baseUrl)
        val bridge = QadbBridgeHttpClient(connection.baseUrl, connection.bridgeToken)
        return ArtemisCandidate(
            runner = ArtemisAgentEngine(
                client = client,
                profile = configuration.profile,
                bridge = bridge
            ),
            readiness = {
                val readiness = client.getReadiness()
                if (!readiness.ready && readiness.serviceBlockers.isNotEmpty()) {
                    AgentRunnerReadiness(
                        ready = false,
                        message = readiness.serviceBlockers.joinToString().ifBlank { null }
                    )
                } else {
                    // Artemis' global probe can report another connected device as
                    // locked. QADB validates and binds the user's exact serial when
                    // registering the run, so that unrelated device must not block it.
                    bridge.handshake()
                    AgentRunnerReadiness(ready = true)
                }
            }
        )
    }
}

private data class ArtemisCandidate(
    val runner: AgentTaskRunner,
    val readiness: suspend () -> AgentRunnerReadiness
)

/** Gives the owned loopback process time to bind its port and mount the Bridge routes. */
internal suspend fun awaitAgentRunnerReadiness(
    maxAttempts: Int = 20,
    retryDelayMs: Long = 250,
    probe: suspend () -> AgentRunnerReadiness
): AgentRunnerReadiness {
    require(maxAttempts > 0)
    var lastFailure: Throwable? = null
    repeat(maxAttempts) { attempt ->
        val result = runCatching { probe() }
        result.getOrNull()?.let { readiness ->
            // A successful readiness response with real blockers is authoritative;
            // an empty not-ready response is also a common cold-start state.
            if (readiness.ready || !readiness.message.isNullOrBlank()) return readiness
        }
        lastFailure = result.exceptionOrNull()
        if (attempt < maxAttempts - 1) delay(retryDelayMs)
    }
    return AgentRunnerReadiness(
        ready = false,
        message = lastFailure?.message?.take(240) ?: "Local Artemis service is unavailable"
    )
}
