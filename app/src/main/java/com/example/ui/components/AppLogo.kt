package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors

enum class AppLogoSize(val dimension: Dp) {
    SM(42.dp),
    MD(64.dp),
    LG(94.dp)
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: AppLogoSize = AppLogoSize.MD,
    showWordmark: Boolean = true
) {
    val dimension = size.dimension

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Outer glow & Peach circle container
        Box(
            modifier = Modifier
                .size(dimension)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color(0x339A77FF),
                    ambientColor = Color(0x229A77FF)
                )
                .clip(CircleShape)
                .background(LuneColors.surfacePeach)
                .border(1.dp, LuneColors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner primary deep purple circle
            Box(
                modifier = Modifier
                    .size(dimension * 0.5f)
                    .clip(CircleShape)
                    .background(LuneColors.primary)
            )

            // Crescent overlay (surfacePeach offset to form the crescent moon)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = (-dimension * 0.08f),
                        y = (dimension * 0.16f)
                    )
                    .size(dimension * 0.38f)
                    .clip(CircleShape)
                    .background(LuneColors.surfacePeach)
            )
        }

        if (showWordmark) {
            AppText(
                text = "LUNE",
                variant = AppTextVariant.TITLE,
                tone = AppTextTone.DEFAULT,
                textAlign = TextAlign.Center
            )
        }
    }
}
