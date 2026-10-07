package com.example.sleep

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveSleepState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

@Composable
fun ActiveSleepScreen(
    currentSound: SleepSoundItem,
    activeSleep: ActiveSleepState = ActiveSleepState(),
    nextAlarmText: String = "No upcoming alarms",
    onCollapse: () -> Unit,
    onOpenSoundPicker: () -> Unit,
    onFinishSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current live time
    var currentTimeFormatted by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000L)
        }
    }

    // Hold to quit progress state (0.0f to 1.0f)
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val step = 0.04f
            while (isHolding && holdProgress < 1f) {
                delay(30L)
                holdProgress = (holdProgress + step).coerceAtMost(1f)
            }
            if (holdProgress >= 1f) {
                delay(100L)
                onFinishSleep()
            }
        } else {
            // Decay back smoothly
            while (!isHolding && holdProgress > 0f) {
                delay(15L)
                holdProgress = (holdProgress - 0.08f).coerceAtLeast(0f)
            }
        }
    }

    // Ribbon wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "ribbon_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF18203D),
                        Color(0xFF0C0E18),
                        Color(0xFF050608)
                    ),
                    center = Offset(500f, 300f),
                    radius = 1100f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Starry Night Overlay
        Box(
            modifier = Modifier
                .offset(x = 80.dp, y = 80.dp)
                .size(2.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCEBFF).copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-100).dp, y = 60.dp)
                .size(2.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .offset(x = 130.dp, y = 140.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7).copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-70).dp, y = 240.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFFDE68A).copy(alpha = 0.8f))
        )
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 230.dp)
                .size(2.dp)
                .clip(CircleShape)
                .background(Color(0xFFBFDBFE).copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 30.dp, y = 110.dp)
                .size(2.dp)
                .clip(CircleShape)
                .background(Color(0xFFBFDBFE).copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-55).dp, y = (-220).dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF08A).copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Minimize Chevron Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .clickable(onClick = onCollapse)
                        .testTag("sleep_collapse_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize sleep screen",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Time & Alarm Status Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = currentTimeFormatted,
                    color = Color(0xFF1C88FF),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.testTag("sleep_clock_display")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = nextAlarmText,
                    color = Color(0xFF7E879C),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time sound tracking status & snoring indicator
                val elapsedMins = (activeSleep.elapsedSeconds / 60).toInt()
                val isSnoring = activeSleep.isSnoringNow
                val decibels = activeSleep.currentDecibels.toInt()

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSnoring) Color(0x33EF4444) else Color(0x221C88FF))
                        .border(
                            width = 1.dp,
                            color = if (isSnoring) Color(0x88EF4444) else Color(0x441C88FF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSnoring) Color(0xFFEF4444) else Color(0xFF1C88FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSnoring) "Snoring Sound Detected ($decibels dB)" else "Sound: $decibels dB • $elapsedMins min tracked",
                        color = if (isSnoring) Color(0xFFFCA5A5) else Color(0xFF93C5FD),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Sound Wave Ribbon Graphic (Middle Section)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow layer
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(30.dp)
                        .blur(16.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x0038BDF8),
                                    Color(0x660EA5E9),
                                    Color(0x80A855F7),
                                    Color(0x80EC4899),
                                    Color(0x66EAB308),
                                    Color(0x0038BDF8)
                                )
                            )
                        )
                )

                // Canvas with multi-colored wave lines
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val w = size.width
                    val midY = size.height / 2f

                    // Base axis line
                    drawLine(
                        color = Color(0xFF3A3F58).copy(alpha = 0.4f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw multi-color glowing curves
                    fun drawCurvedStrand(
                        color: Color,
                        amplitude: Float,
                        freq: Float,
                        phaseOffset: Float,
                        strokeWidthDp: Float
                    ) {
                        val path = Path()
                        path.moveTo(0f, midY)
                        val step = 4f
                        var x = 0f
                        while (x <= w) {
                            val normX = (x / w) * 2f - 1f // -1 to 1
                            val envelope = (1f - normX * normX).coerceAtLeast(0f)
                            val y = midY + sin((x * freq * 0.015f) + wavePhase + phaseOffset) * (amplitude * envelope)
                            path.lineTo(x, y)
                            x += step
                        }
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Cyan Wave Strand
                    drawCurvedStrand(Color(0xFF00D2FF).copy(alpha = 0.65f), amplitude = 26f, freq = 0.8f, phaseOffset = 0f, strokeWidthDp = 1.8f)
                    // Blue Wave Strand
                    drawCurvedStrand(Color(0xFF0088FF).copy(alpha = 0.75f), amplitude = 22f, freq = 0.9f, phaseOffset = 1.2f, strokeWidthDp = 2.0f)
                    // Purple Wave Strand
                    drawCurvedStrand(Color(0xFFB83BFA).copy(alpha = 0.70f), amplitude = 32f, freq = 0.7f, phaseOffset = 2.5f, strokeWidthDp = 2.2f)
                    // Magenta Wave Strand
                    drawCurvedStrand(Color(0xFFFA3B93).copy(alpha = 0.60f), amplitude = 28f, freq = 1.0f, phaseOffset = 3.8f, strokeWidthDp = 1.6f)
                    // Warm Yellow Wave Strand
                    drawCurvedStrand(Color(0xFFEAB308).copy(alpha = 0.55f), amplitude = 18f, freq = 0.6f, phaseOffset = 4.9f, strokeWidthDp = 1.6f)
                }
            }

            // Quit Action Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Text(
                    text = "Hold to quit",
                    color = Color(0xFF8C94A5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Track Bar
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF232733))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(holdProgress)
                            .height(4.dp)
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Quit Button with Hold Gesture Detection
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHolding = true
                                    tryAwaitRelease()
                                    isHolding = false
                                }
                            )
                        }
                        .padding(horizontal = 38.dp, vertical = 13.dp)
                        .testTag("sleep_hold_quit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quit",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1B1C20))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("sleep_bottom_dock")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Content: Audio Icon & Label (Clickable to open Sound Picker)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenSoundPicker
                            )
                            .padding(vertical = 4.dp)
                            .testTag("dock_sound_selector")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF253254)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Audio settings",
                                tint = Color(0xFF558AFF),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = currentSound.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.2.sp
                        )
                    }

                    // Right Action: App Tray / Grid Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenSoundPicker)
                            .testTag("dock_grid_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF7C8393)))
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF7C8393)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF7C8393)))
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF7C8393)))
                            }
                        }
                    }
                }
            }
        }
    }
}
