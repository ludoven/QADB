package com.ludoven.adbtool.ui.icons

import adbtool_desktop.composeapp.generated.resources.Res
import adbtool_desktop.composeapp.generated.resources.ai_agent_logo
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource

@Composable
fun AiAgentBrandIcon(contentDescription: String?, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.ai_agent_logo),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
