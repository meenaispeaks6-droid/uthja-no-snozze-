package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

@Composable
fun LuneToggle(
    value: Boolean,
    onValueChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    disabled: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = com.example.alarm.rememberAppleHaptics()

    val trackColor by animateColorAsState(
        targetValue = if (value) LuneColors.primary else LuneColors.surfaceMuted,
        label = "trackColor"
    )

    // Track is 58dp wide, padding is 3dp each side => available space for 26dp thumb is 58 - 6 - 26 = 26dp
    val thumbOffset by animateDpAsState(
        targetValue = if (value) 26.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "thumbOffset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !disabled && onValueChange != null,
                role = Role.Switch,
                onClick = {
                    haptics.pulseAppleSelection()
                    onValueChange?.invoke(!value)
                }
            )
            .padding(vertical = 4.dp)
    ) {
        if (label != null) {
            AppText(
                text = label,
                variant = AppTextVariant.BODY_SMALL,
                tone = if (disabled) AppTextTone.MUTED else AppTextTone.DEFAULT,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(58.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(if (disabled) trackColor.copy(alpha = 0.55f) else trackColor)
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(26.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
