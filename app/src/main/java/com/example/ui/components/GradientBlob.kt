package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

enum class GradientBlobTone {
    PEACH,
    SAGE,
    LAVENDER,
    GOLD
}

@Composable
fun GradientBlob(
    modifier: Modifier = Modifier,
    tone: GradientBlobTone = GradientBlobTone.PEACH,
    size: Dp = 160.dp
) {
    val toneColor = when (tone) {
        GradientBlobTone.PEACH -> LuneColors.surfacePeach
        GradientBlobTone.SAGE -> LuneColors.surfaceSage
        GradientBlobTone.LAVENDER -> LuneColors.surfaceLavender
        GradientBlobTone.GOLD -> LuneColors.surfaceGold
    }

    Box(
        modifier = modifier
            .size(size)
            .offset(x = size * 0.25f, y = -size * 0.35f)
            .clip(CircleShape)
            .background(toneColor)
            .alpha(0.45f)
    )
}
