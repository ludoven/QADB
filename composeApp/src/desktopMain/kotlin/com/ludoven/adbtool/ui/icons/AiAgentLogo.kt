package com.ludoven.adbtool.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** QADB AI Agent logo: an assistant bubble framing an active intelligence core. */
internal val AiAgentLogo: ImageVector by lazy {
    ImageVector.Builder(
        name = "Qadb.AiAgentLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M7.75 3.5H15.25C17.597 3.5 19.5 5.403 19.5 7.75V13.25" +
                    "C19.5 15.597 17.597 17.5 15.25 17.5H10L6 21V17.11" +
                    "C4.52 16.45 3.5 14.96 3.5 13.25V7.75C3.5 5.403 5.403 3.5 7.75 3.5Z"
            ).toNodes(),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.75f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
        addPath(
            pathData = PathParser().parsePathString(
                "M11.5 6.4C11.83 8.68 13.32 10.17 15.6 10.5" +
                    "C13.32 10.83 11.83 12.32 11.5 14.6" +
                    "C11.17 12.32 9.68 10.83 7.4 10.5" +
                    "C9.68 10.17 11.17 8.68 11.5 6.4Z"
            ).toNodes(),
            fill = SolidColor(Color.Black),
            stroke = null
        )
    }.build()
}
