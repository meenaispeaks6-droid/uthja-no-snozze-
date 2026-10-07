package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

enum class StatCardAccent {
    PEACH,
    SAGE,
    LAVENDER,
    GOLD
}

fun resolveStatAccentColor(accent: StatCardAccent): Color = when (accent) {
    StatCardAccent.PEACH -> LuneColors.accentPeach
    StatCardAccent.SAGE -> LuneColors.accentSage
    StatCardAccent.LAVENDER -> LuneColors.accentLavender
    StatCardAccent.GOLD -> LuneColors.accentGold
}

@Composable
fun LuneStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    helper: String? = null,
    accent: StatCardAccent = StatCardAccent.PEACH
) {
    val barColor = resolveStatAccentColor(accent)

    LuneCard(
        modifier = modifier.defaultMinSize(minWidth = 140.dp),
        tonal = CardTone.DEFAULT
    ) {
        Column {
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.height(10.dp))
            AppText(
                text = value,
                variant = AppTextVariant.TITLE_SMALL,
                tone = AppTextTone.DEFAULT
            )
            AppText(
                text = label,
                variant = AppTextVariant.CAPTION,
                tone = AppTextTone.SECONDARY
            )
            if (!helper.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = helper,
                    variant = AppTextVariant.CAPTION,
                    tone = AppTextTone.MUTED
                )
            }
        }
    }
}
