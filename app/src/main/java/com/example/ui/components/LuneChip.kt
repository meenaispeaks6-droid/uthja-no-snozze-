package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

enum class LuneChipVariant {
    DEFAULT,
    LAVENDER,
    PEACH,
    SAGE,
    GOLD
}

@Composable
fun LuneChip(
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    variant: LuneChipVariant = LuneChipVariant.DEFAULT,
    modifier: Modifier = Modifier
) {
    val selectedBg = when (variant) {
        LuneChipVariant.DEFAULT -> LuneColors.text
        LuneChipVariant.LAVENDER -> LuneColors.primary
        LuneChipVariant.PEACH -> LuneColors.peachDark
        LuneChipVariant.SAGE -> LuneColors.sageDark
        LuneChipVariant.GOLD -> LuneColors.goldDark
    }

    val selectedBorder = when (variant) {
        LuneChipVariant.DEFAULT -> LuneColors.text
        LuneChipVariant.LAVENDER -> LuneColors.primaryDeep
        LuneChipVariant.PEACH -> LuneColors.coral
        LuneChipVariant.SAGE -> LuneColors.sageDark
        LuneChipVariant.GOLD -> LuneColors.goldDark
    }

    val unselectedBg = when (variant) {
        LuneChipVariant.DEFAULT -> LuneColors.surfaceSoft
        LuneChipVariant.LAVENDER -> LuneColors.surfaceLavender
        LuneChipVariant.PEACH -> LuneColors.surfacePeach
        LuneChipVariant.SAGE -> LuneColors.surfaceSage
        LuneChipVariant.GOLD -> LuneColors.surfaceGold
    }

    val backgroundColor = if (selected) selectedBg else unselectedBg
    val borderColor = if (selected) selectedBorder else LuneColors.border
    val textTone = if (selected) AppTextTone.INVERSE else AppTextTone.DEFAULT
    val haptics = com.example.alarm.rememberAppleHaptics()

    val handleClick = {
        haptics.pulseAppleSelection()
        onClick?.invoke()
        onSelectedChange?.invoke(!selected)
    }

    Surface(
        shape = CircleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.clickable { handleClick() }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            AppText(
                text = label,
                variant = AppTextVariant.CAPTION,
                tone = textTone
            )
        }
    }
}
