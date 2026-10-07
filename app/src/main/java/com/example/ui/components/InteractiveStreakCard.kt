package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Legacy wrapper for [StreakCard].
 * Maintained for backward compatibility.
 */
@Composable
fun InteractiveStreakCard(
    modifier: Modifier = Modifier
) {
    StreakCard(modifier = modifier)
}
