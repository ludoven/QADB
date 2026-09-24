package com.ludoven.adbtool.pages

import com.ludoven.adbtool.agent.AgentMessage
import com.ludoven.adbtool.agent.AgentMessageRole
import com.ludoven.adbtool.agent.AgentUsage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiAgentScreenLayoutTest {
    @Test
    fun `layout breakpoints select single overlay and permanent device panels`() {
        assertEquals(AgentScreenLayout.SINGLE_COLUMN, agentScreenLayout(1079.9f))
        assertEquals(AgentScreenLayout.OVERLAY_DEVICE_PANEL, agentScreenLayout(1080f))
        assertEquals(AgentScreenLayout.OVERLAY_DEVICE_PANEL, agentScreenLayout(1279.9f))
        assertEquals(AgentScreenLayout.PERMANENT_DEVICE_PANEL, agentScreenLayout(1280f))
    }

    @Test
    fun `latest conversation item starts at a valid scroll offset`() {
        assertEquals(0, agentLatestItemScrollOffset())
    }

    @Test
    fun `guide remains visible when only internal system messages exist`() {
        assertTrue(shouldShowAgentGuide(emptyList()))
        assertTrue(
            shouldShowAgentGuide(
                listOf(AgentMessage(id = "system", role = AgentMessageRole.SYSTEM, text = "internal"))
            )
        )
        assertFalse(
            shouldShowAgentGuide(
                listOf(AgentMessage(id = "user", role = AgentMessageRole.USER, text = "打开设置"))
            )
        )
    }

    @Test
    fun `context meter uses last request input rather than cumulative task usage`() {
        assertEquals(null, agentContextFillFraction(null, 128_000))
        assertEquals(null, agentContextFillFraction(AgentUsage(totalTokens = 200_000), 128_000))
        assertEquals(null, agentContextFillFraction(AgentUsage(promptTokens = 12_000), null))
        assertEquals(0.25f, agentContextFillFraction(AgentUsage(promptTokens = 32_000, totalTokens = 200_000), 128_000))
    }

    @Test
    fun `enter does not submit while an input method is composing`() {
        assertFalse(shouldSubmitAgentComposerOnEnter(true, true, false, true))
        assertFalse(shouldSubmitAgentComposerOnEnter(true, true, true, false))
        assertTrue(shouldSubmitAgentComposerOnEnter(true, true, false, false))
    }
}
