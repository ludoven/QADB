package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgentForegroundBoundaryTest {
    @Test
    fun `foreground action requires the observed application package`() {
        assertTrue(matchesAgentForegroundActivity("com.example/.Main", "com.example/com.example.Detail"))
        assertFalse(matchesAgentForegroundActivity("com.example/.Main", "com.other/.Main"))
        assertFalse(matchesAgentForegroundActivity("com.example/.Main", ""))
        assertFalse(matchesAgentForegroundActivity("", "com.example/.Main"))
    }

    @Test
    fun `all screen directed writes pass the foreground gate`() {
        assertTrue(AgentAction.Tap(1, 2).requiresAgentForegroundCheck())
        assertTrue(AgentAction.TapElement("observation", "node").requiresAgentForegroundCheck())
        assertTrue(AgentAction.Swipe(1, 2, 3, 4, 500).requiresAgentForegroundCheck())
        assertTrue(AgentAction.InputText("text").requiresAgentForegroundCheck())
        assertTrue(AgentAction.KeyEvent(AgentKey.ENTER).requiresAgentForegroundCheck())
        assertFalse(AgentAction.LaunchPackage("com.example").requiresAgentForegroundCheck())
    }
}
