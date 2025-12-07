package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

//this speech bubble is for the one in the main menu to show off your to do n stuff
@Composable
fun SpeechBubbleWithContent(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val tailHeight = 20.dp

    val bubbleShape = remember(density) {
        GenericShape { size, _ ->
            val cornerRadius = with(density) { 24.dp.toPx() }
            val tailWidth = with(density) { 20.dp.toPx() }
            val tailHeightPx = with(density) { tailHeight.toPx() }
            val tailOverlap = with(density) { 8.dp.toPx() }

            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - tailHeightPx,
                    radiusX = cornerRadius,
                    radiusY = cornerRadius
                )
            )

            moveTo(x = size.width / 2 - tailWidth, y = size.height - tailHeightPx - tailOverlap)
            lineTo(x = size.width / 2, y = size.height)
            lineTo(x = size.width / 2 + tailWidth, y = size.height - tailHeightPx - tailOverlap)
            close()
        }
    }

    Box(
        modifier = Modifier
            .clip(bubbleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
            .padding(bottom = tailHeight)
    ) {
        content()
    }
}
