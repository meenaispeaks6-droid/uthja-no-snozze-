package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.alarm.rememberAppleHaptics
import com.example.data.model.DismissType
import com.example.data.model.WakeMode
import com.example.ui.ActiveAlarmState

/**
 * Wake-Up Mission Selection Screen matching the exact UI in the user attachment:
 * - Off-white background (#FBF6F5)
 * - Top navigation bar: "LUNE" title, 5-bar progress capsules ("5/5"), "Skip" text action
 * - Header: "Choose a wake-up mission" + "Complete a small challenge to turn off your alarm." + Sun character illustration
 * - Mission options:
 *     1. Math (+- x+ icon, "Most Popular" purple badge, "Solve simple math questions")
 *     2. Find Color Tiles (4 colored tiles icon, "👑 Pro" badge, "Find and tap the right colors")
 *     3. Typing (keyboard icon, "👑 Pro" badge, "Type the given sentence")
 *     4. Shake (vibrating phone icon, "Shake your phone multiple times")
 *     5. Off (bell off icon, "No challenge, just an alarm")
 * - Bottom motivation card: "Small challenges. Big habits." with glowing light bulb
 * - Bottom action button: Pill gradient "Next >"
 * - Connecting seamlessly to existing mission screens (especially ActiveAlarmScreen for Shake)
 */
@Composable
fun WakeUpMissionPickerScreen(
    currentMission: DismissType = DismissType.MATH,
    onMissionConfirmed: (DismissType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppleHaptics()
    var selectedMission by remember { mutableStateOf(currentMission) }

    // Preview state for all mission types
    var previewMissionType by remember { mutableStateOf<DismissType?>(null) }
    var previewShakeCount by remember { mutableIntStateOf(0) }

    BackHandler {
        if (previewMissionType != null) {
            previewMissionType = null
            previewShakeCount = 0
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. TOP HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LUNE Brand
                Text(
                    text = "LUNE",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF24143D),
                    letterSpacing = 0.5.sp
                )

                // 5-bar progress segments + "5/5"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF8B64DC))
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "5/5",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B5885)
                    )
                }

                // Skip Action
                Text(
                    text = "Skip",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B5885),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.pulseAppleButtonClick()
                            onDismiss()
                        }
                        .padding(4.dp)
                        .testTag("skip_mission_picker")
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 2. HERO TITLE & ILLUSTRATION ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose a\nwake-up mission",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF24143D),
                            lineHeight = 34.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Complete a small challenge to\nturn off your alarm.",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF6B5885),
                            lineHeight = 20.sp
                        )
                    }

                    // Sun Artwork Graphic matching the attached screenshot
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 90.dp)
                            .padding(start = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_wake_mission_hero),
                            contentDescription = "Morning Mission Sun",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. MISSION CARDS LIST
                // Card 1: Math (Most Popular)
                MissionCard(
                    title = "Math",
                    subtitle = "Solve simple math questions",
                    icon = { MathIconBadge() },
                    isSelected = selectedMission == DismissType.MATH,
                    topBadge = "Most Popular",
                    testTag = "mission_card_math",
                    onTestClick = {
                        haptics.pulseAppleButtonClick()
                        selectedMission = DismissType.MATH
                        previewMissionType = DismissType.MATH
                    },
                    onClick = {
                        haptics.pulseAppleSelection()
                        if (selectedMission == DismissType.MATH) {
                            previewMissionType = DismissType.MATH
                        } else {
                            selectedMission = DismissType.MATH
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 2: Find Color Tiles (👑 Pro)
                MissionCard(
                    title = "Find Color Tiles",
                    subtitle = "Find and tap the right colors",
                    icon = { ColorTilesIconBadge() },
                    isSelected = selectedMission == DismissType.COLOR_TILES,
                    hasProBadge = true,
                    testTag = "mission_card_color_tiles",
                    onTestClick = {
                        haptics.pulseAppleButtonClick()
                        selectedMission = DismissType.COLOR_TILES
                        previewMissionType = DismissType.COLOR_TILES
                    },
                    onClick = {
                        haptics.pulseAppleSelection()
                        if (selectedMission == DismissType.COLOR_TILES) {
                            previewMissionType = DismissType.COLOR_TILES
                        } else {
                            selectedMission = DismissType.COLOR_TILES
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 3: Typing (👑 Pro)
                MissionCard(
                    title = "Typing",
                    subtitle = "Type the given sentence",
                    icon = { TypingIconBadge() },
                    isSelected = selectedMission == DismissType.TYPING,
                    hasProBadge = true,
                    testTag = "mission_card_typing",
                    onTestClick = {
                        haptics.pulseAppleButtonClick()
                        selectedMission = DismissType.TYPING
                        previewMissionType = DismissType.TYPING
                    },
                    onClick = {
                        haptics.pulseAppleSelection()
                        if (selectedMission == DismissType.TYPING) {
                            previewMissionType = DismissType.TYPING
                        } else {
                            selectedMission = DismissType.TYPING
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 4: Shake
                MissionCard(
                    title = "Shake",
                    subtitle = "Shake your phone multiple times",
                    icon = { ShakeIconBadge() },
                    isSelected = selectedMission == DismissType.SHAKE,
                    testTag = "mission_card_shake",
                    onTestClick = {
                        haptics.pulseAppleButtonClick()
                        selectedMission = DismissType.SHAKE
                        previewMissionType = DismissType.SHAKE
                    },
                    onClick = {
                        haptics.pulseAppleSelection()
                        if (selectedMission == DismissType.SHAKE) {
                            previewMissionType = DismissType.SHAKE
                        } else {
                            selectedMission = DismissType.SHAKE
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 5: Off
                MissionCard(
                    title = "Off",
                    subtitle = "No challenge, just an alarm",
                    icon = { OffIconBadge() },
                    isSelected = selectedMission == DismissType.OFF,
                    testTag = "mission_card_off",
                    onClick = {
                        haptics.pulseAppleSelection()
                        selectedMission = DismissType.OFF
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. MOTIVATIONAL TIP CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1EDE9), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Small challenges. Big habits.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF24143D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "These missions help you wake up, stay consistent and build a better you.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF6B5885),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. BOTTOM ACTION BUTTON ("Next >")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFF74E76),
                                    Color(0xFFFF6388)
                                )
                            )
                        )
                        .clickable {
                            haptics.pulseAppleButtonClick()
                            if (selectedMission != DismissType.OFF) {
                                // Open the corresponding mission preview screen
                                previewMissionType = selectedMission
                            } else {
                                onMissionConfirmed(selectedMission)
                            }
                        }
                        .testTag("mission_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Next >",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }

        // FULL SCREEN OVERLAYS: Corresponding Mission Screens
        AnimatedVisibility(
            visible = previewMissionType != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when (previewMissionType) {
                DismissType.MATH -> {
                    MathMissionScreen(
                        onDismissConquered = {
                            previewMissionType = null
                            onMissionConfirmed(DismissType.MATH)
                        }
                    )
                }
                DismissType.COLOR_TILES -> {
                    ColorTilesMissionScreen(
                        onDismissConquered = {
                            previewMissionType = null
                            onMissionConfirmed(DismissType.COLOR_TILES)
                        }
                    )
                }
                DismissType.TYPING -> {
                    TypingMissionScreen(
                        onDismissConquered = {
                            previewMissionType = null
                            onMissionConfirmed(DismissType.TYPING)
                        }
                    )
                }
                DismissType.SHAKE -> {
                    ActiveAlarmScreen(
                        state = ActiveAlarmState(
                            isRinging = true,
                            title = "Shake to Dismiss",
                            wakeMode = WakeMode.FOCUS,
                            dismissType = DismissType.SHAKE,
                            shakeTargetCount = 10,
                            shakeCurrentCount = previewShakeCount
                        ),
                        onDismissConquered = {
                            previewMissionType = null
                            previewShakeCount = 0
                            onMissionConfirmed(DismissType.SHAKE)
                        },
                        onAttemptSnooze = { },
                        onShake = {
                            previewShakeCount++
                            if (previewShakeCount >= 10) {
                                previewMissionType = null
                                previewShakeCount = 0
                                onMissionConfirmed(DismissType.SHAKE)
                            }
                        },
                        onSimulateShake = {
                            previewShakeCount++
                            if (previewShakeCount >= 10) {
                                previewMissionType = null
                                previewShakeCount = 0
                                onMissionConfirmed(DismissType.SHAKE)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * Reusable Mission Card Component matching the attached screenshot style.
 */
@Composable
private fun MissionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTestClick: (() -> Unit)? = null,
    topBadge: String? = null,
    hasProBadge: Boolean = false,
    testTag: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) Color(0xFFF7F3FD) else Color.White)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) Color(0xFF8B64DC) else Color(0xFFEAE5E2),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Icon
                icon()

                // Center Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF24143D)
                        )

                        if (hasProBadge) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "👑 Pro",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF6B5885)
                        )

                        if (isSelected && onTestClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEDE4FD))
                                    .clickable { onTestClick() }
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Try ▷",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }
                }

                // Right Radio / Selection indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF8B64DC) else Color.Transparent)
                        .border(
                            width = if (isSelected) 0.dp else 1.5.dp,
                            color = if (isSelected) Color.Transparent else Color(0xFFCBD5E1),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Top-right pill badge (e.g. "Most Popular")
            if (topBadge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECE0FD))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = topBadge,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }
            }
        }
    }
}

/**
 * Math Icon Badge: 48dp circle in lavender with 2x2 mathematical operators
 */
@Composable
private fun MathIconBadge() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFECE0FD)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "+", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B64DC))
                Text(text = "−", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B64DC))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "×", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B64DC))
                Text(text = "÷", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B64DC))
            }
        }
    }
}

/**
 * Color Tiles Icon Badge: 48dp circle in mint green with 2x2 colorful squares
 */
@Composable
private fun ColorTilesIconBadge() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFE3F5EC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.5.dp)).background(Color(0xFF0D9488)))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.5.dp)).background(Color(0xFF22C55E)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.5.dp)).background(Color(0xFFFBBF24)))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.5.dp)).background(Color(0xFFEC4899)))
            }
        }
    }
}

/**
 * Typing Icon Badge: 48dp circle in soft peach with keyboard representation
 */
@Composable
private fun TypingIconBadge() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFFDECE8)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF74E76).copy(alpha = 0.15f))
                .border(1.5.dp, Color(0xFFF74E76), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(2.5.dp).background(Color(0xFFF74E76), CircleShape))
                    Box(modifier = Modifier.size(2.5.dp).background(Color(0xFFF74E76), CircleShape))
                    Box(modifier = Modifier.size(2.5.dp).background(Color(0xFFF74E76), CircleShape))
                }
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(Color(0xFFF74E76), RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

/**
 * Shake Icon Badge: 48dp circle in lavender with smartphone and vibration waves
 */
@Composable
private fun ShakeIconBadge() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFECE0FD)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Left wave
            Text(text = "‹", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B64DC))
            // Smartphone chassis
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 22.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(1.5.dp, Color(0xFF8B64DC), RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 1.5.dp)
                        .align(Alignment.TopCenter)
                        .padding(top = 1.dp)
                        .background(Color(0xFF8B64DC), RoundedCornerShape(1.dp))
                )
            }
            // Right wave
            Text(text = "›", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B64DC))
        }
    }
}

/**
 * Off Icon Badge: 48dp circle in cool gray with crossed alarm bell
 */
@Composable
private fun OffIconBadge() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F1F3)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔔",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 1.dp)
            )
            // Diagonal slash line
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.dp)
                    .background(Color(0xFFEF4444), RoundedCornerShape(1.dp))
            )
        }
    }
}
