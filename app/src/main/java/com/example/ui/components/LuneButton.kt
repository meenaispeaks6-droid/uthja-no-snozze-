package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuneColors

enum class LuneButtonVariant {
    PRIMARY,
    SECONDARY,
    GHOST
}

enum class LuneButtonSize {
    SM,
    MD,
    LG
}

@Composable
fun LuneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: LuneButtonVariant = LuneButtonVariant.PRIMARY,
    size: LuneButtonSize = LuneButtonSize.MD,
    fullWidth: Boolean = false,
    loading: Boolean = false,
    disabled: Boolean = false,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    val minHeight = when (size) {
        LuneButtonSize.SM -> 42.dp
        LuneButtonSize.MD -> 54.dp
        LuneButtonSize.LG -> 62.dp
    }

    val horizontalPadding = when (size) {
        LuneButtonSize.SM -> 16.dp
        LuneButtonSize.MD -> 20.dp
        LuneButtonSize.LG -> 24.dp
    }

    val containerColor = when (variant) {
        LuneButtonVariant.PRIMARY -> LuneColors.primary
        LuneButtonVariant.SECONDARY -> LuneColors.surfaceSoft
        LuneButtonVariant.GHOST -> Color.Transparent
    }

    val contentColor = when (variant) {
        LuneButtonVariant.PRIMARY -> LuneColors.white
        LuneButtonVariant.SECONDARY -> LuneColors.primaryDeep
        LuneButtonVariant.GHOST -> LuneColors.primaryDeep
    }

    val borderStroke = when (variant) {
        LuneButtonVariant.PRIMARY -> BorderStroke(1.dp, LuneColors.primary)
        LuneButtonVariant.SECONDARY -> BorderStroke(1.dp, LuneColors.border)
        LuneButtonVariant.GHOST -> null
    }

    val haptics = com.example.alarm.rememberAppleHaptics()

    OutlinedButton(
        onClick = {
            haptics.pulseAppleButtonClick()
            onClick()
        },
        enabled = !disabled && !loading,
        shape = shape,
        border = borderStroke,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor.copy(alpha = 0.55f)
        ),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
