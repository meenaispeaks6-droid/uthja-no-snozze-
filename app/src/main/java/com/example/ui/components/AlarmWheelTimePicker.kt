package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.rememberAppleHaptics
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Scrollable smooth wheel time picker matching the app's clean aesthetics:
 * - Magnetic snap-to-item physics using rememberSnapFlingBehavior
 * - Apple tactile haptic ticks on every item pass
 * - Infinite looping wrap-around
 * - Highlighted center white card pill (matching Screenshot 4 & 6)
 * - Muted upper and lower items with smooth scaling and fading
 * - Smooth AM/PM selector
 */
@Composable
fun AlarmWheelTimePicker(
    hour24: Int,
    minute: Int,
    onTimeChange: (hour24: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppleHaptics()
    val platformHaptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val itemHeight = 52.dp
    val totalHeight = itemHeight * 3 // Exactly 3 visible items
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val isPm = hour24 >= 12
    val displayHour = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    // Circular repeat configuration for infinite scroll
    val repeatMultiplier = 1000
    val hourBaseIndex = (repeatMultiplier / 2) * 12 + (displayHour - 1)
    val minuteBaseIndex = (repeatMultiplier / 2) * 60 + minute

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hourBaseIndex)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minuteBaseIndex)

    // Calculate currently centered items
    val centeredHourItem by remember {
        derivedStateOf {
            val idx = hourListState.firstVisibleItemIndex
            val offset = hourListState.firstVisibleItemScrollOffset
            val centerIdx = if (offset > itemHeightPx / 2) idx + 1 else idx
            val raw = centerIdx % 12
            raw + 1 // 1..12
        }
    }

    val centeredMinuteItem by remember {
        derivedStateOf {
            val idx = minuteListState.firstVisibleItemIndex
            val offset = minuteListState.firstVisibleItemScrollOffset
            val centerIdx = if (offset > itemHeightPx / 2) idx + 1 else idx
            centerIdx % 60 // 0..59
        }
    }

    // Emit haptic feedback and update time when hour scrolls
    LaunchedEffect(hourListState) {
        snapshotFlow { centeredHourItem }
            .distinctUntilChanged()
            .drop(1)
            .collect { newDisplayHour ->
                platformHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                haptics.pulseAppleSelection()
                val computed24 = if (isPm) {
                    if (newDisplayHour == 12) 12 else newDisplayHour + 12
                } else {
                    if (newDisplayHour == 12) 0 else newDisplayHour
                }
                onTimeChange(computed24, minute)
            }
    }

    // Emit haptic feedback and update time when minute scrolls
    LaunchedEffect(minuteListState) {
        snapshotFlow { centeredMinuteItem }
            .distinctUntilChanged()
            .drop(1)
            .collect { newMin ->
                platformHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                haptics.pulseAppleSelection()
                onTimeChange(hour24, newMin)
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
        contentAlignment = Alignment.Center
    ) {
        // Center Selected Highlight Card Pill (Floating White Pill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .padding(horizontal = 4.dp)
                .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0A000000))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
        )

        // 3 Scrolling Columns Row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Hours Column
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeight),
                contentAlignment = Alignment.Center
            ) {
                SmoothWheelColumn(
                    listState = hourListState,
                    itemCount = 12 * repeatMultiplier,
                    itemHeight = itemHeight,
                    getItemText = { idx ->
                        val h = (idx % 12) + 1
                        String.format(Locale.US, "%02d", h)
                    },
                    onItemClick = { clickedIdx ->
                        coroutineScope.launch {
                            haptics.pulseAppleSelection()
                            hourListState.animateScrollToItem(clickedIdx)
                        }
                    },
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxSize(),
                    testTag = "time_picker_hours"
                )
            }

            // Colon Divider
            Text(
                text = ":",
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
            )

            // Minutes Column
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeight),
                contentAlignment = Alignment.Center
            ) {
                SmoothWheelColumn(
                    listState = minuteListState,
                    itemCount = 60 * repeatMultiplier,
                    itemHeight = itemHeight,
                    getItemText = { idx ->
                        val m = idx % 60
                        String.format(Locale.US, "%02d", m)
                    },
                    onItemClick = { clickedIdx ->
                        coroutineScope.launch {
                            haptics.pulseAppleSelection()
                            minuteListState.animateScrollToItem(clickedIdx)
                        }
                    },
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxSize(),
                    testTag = "time_picker_minutes"
                )
            }

            // AM / PM Tap Selector
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(itemHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptics.pulseAppleSelection()
                        val newHour = if (isPm) {
                            if (displayHour == 12) 0 else displayHour
                        } else {
                            if (displayHour == 12) 12 else displayHour + 12
                        }
                        onTimeChange(newHour, minute)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPm) "p.m." else "a.m.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // Top & Bottom Cylindrical Depth Fade Gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF7F8FA).copy(alpha = 0.85f),
                            Color(0x00F7F8FA)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00F7F8FA),
                            Color(0xFFF7F8FA).copy(alpha = 0.85f)
                        )
                    )
                )
        )
    }
}

/**
 * Single column of the wheel with snapping fling physics & smooth scale animation
 */
@Composable
private fun SmoothWheelColumn(
    listState: LazyListState,
    itemCount: Int,
    itemHeight: Dp,
    getItemText: (Int) -> String,
    onItemClick: (Int) -> Unit,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = itemHeight),
        modifier = modifier.testTag(testTag)
    ) {
        items(
            count = itemCount,
            key = { it }
        ) { index ->
            val isCentered by remember {
                derivedStateOf {
                    val currentIndex = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset
                    val centerIdx = if (offset > itemHeightPx / 2) currentIndex + 1 else currentIndex
                    index == centerIdx
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (isCentered) 1.0f else 0.84f,
                animationSpec = tween(120),
                label = "scale"
            )

            val alpha by animateFloatAsState(
                targetValue = if (isCentered) 1.0f else 0.45f,
                animationSpec = tween(120),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .scale(scale)
                    .alpha(alpha)
                    .clickable { onItemClick(index) },
                contentAlignment = when (textAlign) {
                    TextAlign.End -> Alignment.CenterEnd
                    TextAlign.Start -> Alignment.CenterStart
                    else -> Alignment.Center
                }
            ) {
                Text(
                    text = getItemText(index),
                    fontSize = if (isCentered) 44.sp else 34.sp,
                    fontWeight = if (isCentered) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isCentered) Color(0xFF0F172A) else Color(0xFF64748B),
                    letterSpacing = (-1.2).sp,
                    textAlign = textAlign,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }
    }
}
