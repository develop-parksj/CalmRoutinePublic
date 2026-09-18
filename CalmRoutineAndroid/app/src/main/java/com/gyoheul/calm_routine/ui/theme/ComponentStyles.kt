package com.gyoheul.calm_routine.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationStyleApi::class)
object ComponentStyles {
    fun routineCard(surfaceColor: Color) = Style {
        shape(RoundedCornerShape(12.dp))
        background(surfaceColor)
    }

    fun moodCard(surfaceColor: Color) = Style {
        shape(RoundedCornerShape(12.dp))
        background(surfaceColor)
    }

    fun secondaryButton(containerColor: Color, contentColor: Color) = Style {
        shape(RoundedCornerShape(8.dp))
        background(containerColor)
        contentColor(contentColor)
    }
}
