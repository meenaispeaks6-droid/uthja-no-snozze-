package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Alignment

enum class CardTone {
    DEFAULT,
    MUTED,
    ACCENT,
    SAGE,
    LAVENDER
}

fun resolveCardToneColor(tone: CardTone): Color = when (tone) {
    CardTone.DEFAULT -> LuneColors.surface
    CardTone.MUTED -> LuneColors.surfaceSoft
    CardTone.ACCENT -> LuneColors.surfacePeach
    CardTone.SAGE -> LuneColors.surfaceSage
    CardTone.LAVENDER -> LuneColors.surfaceLavender
}

@Composable
fun LuneCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    tonal: CardTone = CardTone.DEFAULT,
    blobTone: GradientBlobTone? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val containerColor = backgroundColor ?: resolveCardToneColor(tonal)
    val resolvedBorderColor = borderColor ?: LuneColors.border
    val elevation = if (elevated) 4.dp else 0.dp

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = BorderStroke(borderWidth, resolvedBorderColor)
    ) {
        Box(
            modifier = Modifier
                .clipToBounds()
                .padding(contentPadding)
        ) {
            if (blobTone != null) {
                GradientBlob(
                    tone = blobTone,
                    size = 140.dp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            content()
        }
    }
}
