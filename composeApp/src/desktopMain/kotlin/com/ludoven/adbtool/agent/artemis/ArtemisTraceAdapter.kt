package com.ludoven.adbtool.agent.artemis

import com.ludoven.adbtool.agent.*
import java.util.Base64
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Maps upstream Artemis trace and SSE events into QADB's PublicActivity timeline and UI steps.
 */
class ArtemisTraceAdapter(
    private val runId: String,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val sequenceProvider: () -> Long = { System.nanoTime() }
) {
    private var lastStage: AgentPublicStage? = null
    private var activeTool: AgentPublicToolSummary? = null
    private var activeStepId: String? = null

    data class AdaptedTraceResult(
        val publicEvents: List<AgentPublicEvent> = emptyList(),
        val step: AgentStep? = null,
        val screenshotPng: ByteArray? = null,
        val summaryDetail: String? = null
    )

    fun adapt(event: ArtemisSseEvent): AdaptedTraceResult {
        val payload = runCatching { Json.parseToJsonElement(event.data).jsonObject }.getOrNull()
            ?: return AdaptedTraceResult(summaryDetail = event.type.ifBlank { null })

        val events = mutableListOf<AgentPublicEvent>()
        val occurredAt = timeProvider()

        // 1. Stage mapping
        val stageStr = payload.string("stage") ?: payload.string("module") ?: payload.string("role")
        val targetStage = stageStr?.let(::mapStage)
        if (targetStage != null && targetStage != lastStage) {
            lastStage = targetStage
            events += AgentPublicEvent(
                runId = runId,
                sequence = sequenceProvider(),
                occurredAtMs = occurredAt,
                payload = AgentPublicEventPayload.StageChanged(targetStage)
            )
        }

        // 2. Action / Tool mapping
        val actionKind = payload.string("action") ?: payload.string("tool")
        val toolKind = actionKind?.let(::mapToolKind)
        val textParam = payload.string("text") ?: payload.string("input") ?: payload.string("query")
        val targetParam = payload.string("target") ?: payload.string("label") ?: payload.string("element")

        var generatedStep: AgentStep? = null

        if (toolKind != null) {
            val toolSummary = AgentPublicToolSummary(
                kind = toolKind,
                inputCharacterCount = textParam?.length
            )

            val statusStr = payload.string("status")?.lowercase()
            val isFinished = event.type == "action_finished" ||
                statusStr == "success" || statusStr == "succeeded" || statusStr == "completed"

            if (isFinished) {
                events += AgentPublicEvent(
                    runId = runId,
                    sequence = sequenceProvider(),
                    occurredAtMs = occurredAt,
                    payload = AgentPublicEventPayload.ToolFinished(
                        tool = activeTool ?: toolSummary,
                        result = AgentPublicToolResult.SUCCEEDED
                    )
                )
                generatedStep = AgentStep(
                    id = activeStepId ?: UUID.randomUUID().toString(),
                    action = toAgentAction(toolKind, textParam, targetParam),
                    status = AgentStepStatus.COMPLETED,
                    result = payload.string("message") ?: targetParam ?: "Action completed"
                )
                activeTool = null
                activeStepId = null
            } else {
                activeTool = toolSummary
                val stepId = UUID.randomUUID().toString()
                activeStepId = stepId
                events += AgentPublicEvent(
                    runId = runId,
                    sequence = sequenceProvider(),
                    occurredAtMs = occurredAt,
                    payload = AgentPublicEventPayload.ToolStarted(toolSummary)
                )
                generatedStep = AgentStep(
                    id = stepId,
                    action = toAgentAction(toolKind, textParam, targetParam),
                    status = AgentStepStatus.RUNNING,
                    result = payload.string("message") ?: targetParam.orEmpty()
                )
            }
        }

        // 3. Screenshot frame extraction
        val screenshotBase64 = payload.string("screenshot")
            ?: payload.string("image")
            ?: payload.string("screenshot_base64")
            ?: payload.string("frame")
        val screenshotBytes = screenshotBase64?.let { raw ->
            runCatching {
                val clean = raw.substringAfter("base64,").trim()
                Base64.getDecoder().decode(clean)
            }.getOrNull()
        }

        // 4. Detail message
        val detailMsg = payload.string("message")
            ?: payload.string("summary")
            ?: payload.string("detail")
            ?: actionKind?.let { "Artemis: $it" }

        return AdaptedTraceResult(
            publicEvents = events,
            step = generatedStep,
            screenshotPng = screenshotBytes,
            summaryDetail = detailMsg
        )
    }

    private fun mapStage(stage: String): AgentPublicStage = when (stage.lowercase()) {
        "planner", "planning", "plan" -> AgentPublicStage.PLANNING
        "operator", "operating", "think", "thinking" -> AgentPublicStage.EXECUTING
        "explorer", "vision_search", "explore" -> AgentPublicStage.READING
        "safety_net", "validation", "checker", "verify", "verifying" -> AgentPublicStage.VERIFYING
        "recovery", "retry", "replan", "recovering" -> AgentPublicStage.RECOVERING
        else -> AgentPublicStage.EXECUTING
    }

    private fun mapToolKind(action: String): AgentPublicToolKind = when (action.lowercase()) {
        "click", "tap" -> AgentPublicToolKind.TAP
        "input", "input_text", "type", "text" -> AgentPublicToolKind.INPUT_TEXT
        "swipe", "scroll" -> AgentPublicToolKind.SWIPE
        "observe", "screenshot", "read_screen" -> AgentPublicToolKind.OBSERVE_DEVICE
        "open", "launch", "start_app" -> AgentPublicToolKind.OPEN_APP
        "key", "key_event", "back", "home", "enter" -> AgentPublicToolKind.KEY_EVENT
        "wait", "sleep" -> AgentPublicToolKind.WAIT
        else -> AgentPublicToolKind.TAP
    }

    private fun toAgentAction(
        kind: AgentPublicToolKind,
        text: String?,
        target: String?
    ): AgentAction = when (kind) {
        AgentPublicToolKind.TAP -> AgentAction.Tap(x = 500, y = 500)
        AgentPublicToolKind.INPUT_TEXT -> AgentAction.InputText(text = text ?: target.orEmpty())
        AgentPublicToolKind.SWIPE -> AgentAction.Swipe(startX = 500, startY = 800, endX = 500, endY = 200, durationMs = 300)
        AgentPublicToolKind.OBSERVE_DEVICE -> AgentAction.Observe
        AgentPublicToolKind.KEY_EVENT -> AgentAction.KeyEvent(AgentKey.BACK)
        else -> AgentAction.Observe
    }

    private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
}
