package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuneColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hand-drawn Retro Pink Alarm Clock component matching the user's uploaded doodle art.
 * Features:
 * - Hand-drawn ink style with twin bells, 3D bottom rim, arched wire handle, and legs.
 * - Dynamic sound wave rings radiating from bells.
 * - Moving real-time hands (hour, minute, ticking second pointer).
 * - Animated hammer clapper and bell jiggle.
 * - Interactive tap: Triggers a high-energy comic alarm ringing spree with speech bubble & bounce!
 */
@Composable
fun DoodleAlarmClock(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    isRingingOverride: Boolean = false,
    showRealTimeHands: Boolean = true,
    targetHour: Int? = null,
    targetMinute: Int? = null,
    onClockTap: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var isHappy by remember { mutableStateOf(false) }
    var happyBubbleText by remember { mutableStateOf("") }
    var happyBubbleIndex by remember { mutableIntStateOf(0) }

    val happyPhrases = remember {
        listOf(
            "Yay! (˶ᵔ ᵕ ᵔ˶) 💕",
            "Hehe, tickles! ✨",
            "Feeling so happy! 🌸",
            "You made my day! 💖",
            "Good morning, bestie! ☀️",
            "Sending you love! 🥰"
        )
    }

    val isAlarmActive = isRingingOverride

    // Live clock time tracking for hands
    var currentSeconds by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentSeconds = System.currentTimeMillis()
        }
    }

    val (displayHour, displayMinute, displaySecond) = remember(currentSeconds, showRealTimeHands, targetHour, targetMinute) {
        if (!showRealTimeHands && targetHour != null && targetMinute != null) {
            Triple(targetHour, targetMinute, 0)
        } else {
            val cal = Calendar.getInstance()
            Triple(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
        }
    }

    // Tap & Emotion Animations
    val scaleAnim = remember { Animatable(1f) }
    val happyBounceY = remember { Animatable(0f) }
    val happyWiggleAngle = remember { Animatable(0f) }
    val happyExpressionAlpha = remember { Animatable(0f) }
    val heartsProgress = remember { Animatable(0f) }

    // Tap handler with cute happy emotion effect
    fun triggerHappyGesture() {
        onClockTap?.invoke()
        coroutineScope.launch {
            happyBubbleText = happyPhrases[happyBubbleIndex % happyPhrases.size]
            happyBubbleIndex++
            isHappy = true

            // 1. Cute happy facial expression fades in
            launch {
                happyExpressionAlpha.snapTo(0f)
                happyExpressionAlpha.animateTo(1f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                delay(2200)
                happyExpressionAlpha.animateTo(0f, animationSpec = tween(350, easing = LinearEasing))
            }

            // 2. Floating hearts & sparkles drift upward
            launch {
                heartsProgress.snapTo(0f)
                heartsProgress.animateTo(1f, animationSpec = tween(2400, easing = FastOutSlowInEasing))
            }

            // 3. Cheerful squash & stretch bounce
            launch {
                scaleAnim.animateTo(0.85f, animationSpec = tween(80))
                scaleAnim.animateTo(1.16f, animationSpec = spring(dampingRatio = 0.45f, stiffness = 550f))
                scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f))
            }

            // 4. Joyful hop upward
            launch {
                happyBounceY.animateTo(-16f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                happyBounceY.animateTo(0f, animationSpec = spring(dampingRatio = 0.45f, stiffness = 380f))
            }

            // 5. Cheerful head-tilt wiggle dance
            launch {
                happyWiggleAngle.animateTo(12f, animationSpec = tween(100))
                happyWiggleAngle.animateTo(-12f, animationSpec = tween(130))
                happyWiggleAngle.animateTo(9f, animationSpec = tween(120))
                happyWiggleAngle.animateTo(-6f, animationSpec = tween(110))
                happyWiggleAngle.animateTo(3f, animationSpec = tween(90))
                happyWiggleAngle.animateTo(0f, animationSpec = tween(90))
            }

            delay(2600)
            isHappy = false
            happyBubbleText = ""
        }
    }

    // Continuous Animations
    val infiniteTransition = rememberInfiniteTransition(label = "doodle_clock_loop")

    // Idle gentle float (always active)
    val idleFloatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idleFloatY"
    )

    // Alarm ringing shake (active when ringing, very subtle when idle)
    val shakeAngle by infiniteTransition.animateFloat(
        initialValue = if (isAlarmActive) -7f else -1f,
        targetValue = if (isAlarmActive) 7f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAlarmActive) 65 else 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeAngle"
    )

    // Hammer clatter angle between the bells
    val hammerAngle by infiniteTransition.animateFloat(
        initialValue = if (isAlarmActive) -14f else -2f,
        targetValue = if (isAlarmActive) 14f else 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAlarmActive) 55 else 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hammerAngle"
    )

    // Bell vibrating displacement
    val bellWobble by infiniteTransition.animateFloat(
        initialValue = if (isAlarmActive) -3.5f else 0f,
        targetValue = if (isAlarmActive) 3.5f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAlarmActive) 70 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellWobble"
    )

    // Sound wave pulse waves
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAlarmActive) 450 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePulse"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = if (isAlarmActive) 0.95f else 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAlarmActive) 450 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
            .offset(y = (idleFloatY + happyBounceY.value).dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = (size / 2)),
                onClick = { triggerHappyGesture() }
            )
            .testTag("doodle_alarm_clock"),
        contentAlignment = Alignment.Center
    ) {
        // Floating cute hearts & sparkles during happy emotion
        if (heartsProgress.value in 0.01f..0.99f) {
            val p = heartsProgress.value
            val alpha = (1f - p).coerceIn(0f, 1f)

            // Left floating heart
            Text(
                text = "💖",
                modifier = Modifier
                    .offset(
                        x = (-30 - (p * 16)).dp,
                        y = (-24 - (p * 45)).dp
                    )
                    .alpha(alpha)
                    .rotate(-15f * (1f + p))
            )

            // Center-top floating sparkle
            Text(
                text = "✨",
                modifier = Modifier
                    .offset(
                        x = (p * 6).dp,
                        y = (-40 - (p * 50)).dp
                    )
                    .alpha(alpha)
                    .scale(1f + p * 0.3f)
            )

            // Right floating flower/heart
            Text(
                text = "🌸",
                modifier = Modifier
                    .offset(
                        x = (30 + (p * 16)).dp,
                        y = (-20 - (p * 42)).dp
                    )
                    .alpha(alpha)
                    .rotate(18f * (1f + p))
            )

            // Extra floating sparkle heart
            Text(
                text = "💕",
                modifier = Modifier
                    .offset(
                        x = (-10 + (p * 12)).dp,
                        y = (-46 - (p * 38)).dp
                    )
                    .alpha(alpha)
            )
        }

        // Cute Happy Emotion Speech Bubble
        if (happyBubbleText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-32).dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF0F5))
                    .border(1.5.dp, Color(0xFFFFB2C5), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                AppText(
                    text = happyBubbleText,
                    variant = AppTextVariant.CAPTION,
                    tone = AppTextTone.DEFAULT
                )
            }
        }

        // Animated Doodle Clock Canvas
        Canvas(
            modifier = Modifier
                .size(size)
                .rotate(shakeAngle + happyWiggleAngle.value)
        ) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val scale = canvasW / 180f // Base reference coordinate system: 180x180

            val inkColor = Color(0xFF141215)
            val pinkBody = Color(0xFFF18C9D)
            val pinkShadow = Color(0xFFD8677B)
            val creamFace = Color(0xFFFFFDFC)
            val whiteHighlight = Color(0xFFFFFFFF)

            // Drawing center offset (tilted ~10 degrees like user's drawing)
            translate(left = 0f, top = 6f * scale) {
                // 1. SOUND WAVES (Radiating lines on Left and Right)
                drawSoundWaves(
                    inkColor = inkColor,
                    scale = scale,
                    pulse = wavePulse,
                    alpha = waveAlpha,
                    isAlarmActive = isAlarmActive
                )

                // 2. LEGS (Peg legs at bottom)
                drawClockLegs(inkColor = inkColor, scale = scale)

                // 3. ARCHED HANDLE (Behind the bells)
                drawArchedHandle(inkColor = inkColor, scale = scale)

                // 4. TOP HAMMER / CLAPPER
                drawCenterHammer(
                    inkColor = inkColor,
                    scale = scale,
                    hammerAngle = hammerAngle
                )

                // 5. TWIN BELLS (Left & Right with wobble)
                val happyBellWobble = if (isHappy) (sin(happyWiggleAngle.value * 2f) * 4f) else 0f
                drawTwinBells(
                    inkColor = inkColor,
                    pinkFill = pinkBody,
                    highlight = whiteHighlight,
                    scale = scale,
                    bellWobble = bellWobble + happyBellWobble
                )

                // 6. MAIN BODY (Pink casing with 3D bottom depth rim)
                drawClockBody(
                    inkColor = inkColor,
                    pinkBody = pinkBody,
                    pinkShadow = pinkShadow,
                    creamFace = creamFace,
                    scale = scale
                )

                // 7. CLOCK FACE DETAILS (Ticks & Center Pin)
                drawClockFaceTicks(inkColor = inkColor, scale = scale)

                // 8. CUTE HAPPY FACE EMOTION (Shown when happy!)
                if (happyExpressionAlpha.value > 0.01f) {
                    drawCuteHappyFace(
                        inkColor = inkColor,
                        scale = scale,
                        alpha = happyExpressionAlpha.value
                    )
                }

                // 9. ANIMATED CLOCK HANDS
                drawClockHands(
                    inkColor = inkColor,
                    scale = scale,
                    hour = displayHour,
                    minute = displayMinute,
                    second = displaySecond,
                    isAlarmActive = isAlarmActive
                )
            }
        }
    }
}

/**
 * Radiating vibration sound lines on top-left and top-right matching the doodle.
 */
private fun DrawScope.drawSoundWaves(
    inkColor: Color,
    scale: Float,
    pulse: Float,
    alpha: Float,
    isAlarmActive: Boolean
) {
    val strokeWidth = 3.6f * scale

    // Left Bell sound dashes (3 curved radiating marks)
    val leftCenter = Offset(36f * scale, 48f * scale)
    val leftColor = inkColor.copy(alpha = if (isAlarmActive) alpha else (alpha * 0.45f))

    val leftAngles = listOf(-165f, -140f, -115f)
    leftAngles.forEachIndexed { i, deg ->
        val rad = Math.toRadians(deg.toDouble())
        val dist1 = (18f + (i * 5f)) * scale * pulse
        val dist2 = dist1 + (9f * scale)
        val p1 = Offset(
            (leftCenter.x + dist1 * cos(rad)).toFloat(),
            (leftCenter.y + dist1 * sin(rad)).toFloat()
        )
        val p2 = Offset(
            (leftCenter.x + dist2 * cos(rad)).toFloat(),
            (leftCenter.y + dist2 * sin(rad)).toFloat()
        )
        drawLine(
            color = leftColor,
            start = p1,
            end = p2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Right Bell sound dashes (3 radiating marks)
    val rightCenter = Offset(140f * scale, 34f * scale)
    val rightColor = inkColor.copy(alpha = if (isAlarmActive) alpha else (alpha * 0.45f))
    val rightAngles = listOf(-65f, -40f, -15f)
    rightAngles.forEachIndexed { i, deg ->
        val rad = Math.toRadians(deg.toDouble())
        val dist1 = (18f + (i * 5f)) * scale * pulse
        val dist2 = dist1 + (9f * scale)
        val p1 = Offset(
            (rightCenter.x + dist1 * cos(rad)).toFloat(),
            (rightCenter.y + dist1 * sin(rad)).toFloat()
        )
        val p2 = Offset(
            (rightCenter.x + dist2 * cos(rad)).toFloat(),
            (rightCenter.y + dist2 * sin(rad)).toFloat()
        )
        drawLine(
            color = rightColor,
            start = p1,
            end = p2,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Peg legs matching the user's doodle.
 */
private fun DrawScope.drawClockLegs(inkColor: Color, scale: Float) {
    val legStroke = 3.6f * scale
    val white = Color(0xFFFFFDFC)

    // Left Front Leg
    val leftLeg = Path().apply {
        moveTo(86f * scale, 146f * scale)
        lineTo(86f * scale, 168f * scale)
        cubicTo(86f * scale, 172f * scale, 93f * scale, 172f * scale, 93f * scale, 168f * scale)
        lineTo(96f * scale, 148f * scale)
        close()
    }
    drawPath(leftLeg, white)
    drawPath(leftLeg, inkColor, style = Stroke(width = legStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Right Front Leg (tilted)
    val rightLeg = Path().apply {
        moveTo(150f * scale, 134f * scale)
        lineTo(162f * scale, 153f * scale)
        cubicTo(164f * scale, 156f * scale, 170f * scale, 151f * scale, 168f * scale, 148f * scale)
        lineTo(158f * scale, 130f * scale)
        close()
    }
    drawPath(rightLeg, white)
    drawPath(rightLeg, inkColor, style = Stroke(width = legStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Back Tripod Leg (gives 3D depth)
    val backLeg = Path().apply {
        moveTo(154f * scale, 126f * scale)
        lineTo(166f * scale, 142f * scale)
    }
    drawLine(
        color = inkColor,
        start = Offset(154f * scale, 126f * scale),
        end = Offset(166f * scale, 140f * scale),
        strokeWidth = legStroke,
        cap = StrokeCap.Round
    )
}

/**
 * Top arched wire handle.
 */
private fun DrawScope.drawArchedHandle(inkColor: Color, scale: Float) {
    val handleStroke = 3.8f * scale
    val handlePath = Path().apply {
        moveTo(64f * scale, 34f * scale)
        cubicTo(
            58f * scale, 10f * scale,
            116f * scale, 8f * scale,
            118f * scale, 24f * scale
        )
    }
    drawPath(
        path = handlePath,
        color = inkColor,
        style = Stroke(width = handleStroke, cap = StrokeCap.Round)
    )
}

/**
 * Top clapper / hammer button between the bells.
 */
private fun DrawScope.drawCenterHammer(
    inkColor: Color,
    scale: Float,
    hammerAngle: Float
) {
    val pivot = Offset(94f * scale, 48f * scale)
    val hammerStroke = 3.5f * scale

    rotate(hammerAngle, pivot) {
        // Stalk
        drawLine(
            color = inkColor,
            start = pivot,
            end = Offset(94f * scale, 36f * scale),
            strokeWidth = hammerStroke,
            cap = StrokeCap.Round
        )
        // Clapper head
        val headPath = Path().apply {
            moveTo(84f * scale, 36f * scale)
            lineTo(104f * scale, 36f * scale)
            cubicTo(
                105f * scale, 31f * scale,
                83f * scale, 31f * scale,
                84f * scale, 36f * scale
            )
            close()
        }
        drawPath(headPath, Color.White)
        drawPath(headPath, inkColor, style = Stroke(width = hammerStroke, cap = StrokeCap.Round))
    }
}

/**
 * Twin Bells matching the doodle with wobble and highlights.
 */
private fun DrawScope.drawTwinBells(
    inkColor: Color,
    pinkFill: Color,
    highlight: Color,
    scale: Float,
    bellWobble: Float
) {
    val strokeW = 3.8f * scale

    // 1. LEFT BELL (tilted ~ -45°)
    rotate(-42f + bellWobble, pivot = Offset(48f * scale, 46f * scale)) {
        // Stalk to clock body
        drawLine(
            color = inkColor,
            start = Offset(48f * scale, 46f * scale),
            end = Offset(56f * scale, 56f * scale),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Top knob
        drawLine(
            color = inkColor,
            start = Offset(48f * scale, 33f * scale),
            end = Offset(48f * scale, 29f * scale),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Bell Dome (oval dome)
        val bellRect = Rect(30f * scale, 33f * scale, 66f * scale, 57f * scale)
        val bellPath = Path().apply {
            moveTo(bellRect.left, bellRect.bottom)
            cubicTo(
                bellRect.left, bellRect.top,
                bellRect.right, bellRect.top,
                bellRect.right, bellRect.bottom
            )
            close()
        }
        drawPath(bellPath, pinkFill)
        drawPath(bellPath, inkColor, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // White oval highlight spot
        drawCircle(
            color = highlight,
            radius = 3.8f * scale,
            center = Offset(44f * scale, 39f * scale)
        )
    }

    // 2. RIGHT BELL (tilted ~ +35°)
    rotate(38f - bellWobble, pivot = Offset(136f * scale, 32f * scale)) {
        // Stalk to clock body
        drawLine(
            color = inkColor,
            start = Offset(136f * scale, 32f * scale),
            end = Offset(128f * scale, 44f * scale),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Top knob
        drawLine(
            color = inkColor,
            start = Offset(136f * scale, 21f * scale),
            end = Offset(136f * scale, 17f * scale),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Bell dome
        val bellRect = Rect(118f * scale, 21f * scale, 154f * scale, 45f * scale)
        val bellPath = Path().apply {
            moveTo(bellRect.left, bellRect.bottom)
            cubicTo(
                bellRect.left, bellRect.top,
                bellRect.right, bellRect.top,
                bellRect.right, bellRect.bottom
            )
            close()
        }
        drawPath(bellPath, pinkFill)
        drawPath(bellPath, inkColor, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // White highlight spot
        drawCircle(
            color = highlight,
            radius = 3.6f * scale,
            center = Offset(132f * scale, 27f * scale)
        )
    }
}

/**
 * Clock Body: Tilted perspective casing with shaded bottom rim.
 */
private fun DrawScope.drawClockBody(
    inkColor: Color,
    pinkBody: Color,
    pinkShadow: Color,
    creamFace: Color,
    scale: Float
) {
    val strokeW = 4.2f * scale

    // 1. Shaded lower casing (giving 3D curved perspective)
    val bottomRimPath = Path().apply {
        moveTo(42f * scale, 98f * scale)
        cubicTo(
            44f * scale, 148f * scale,
            126f * scale, 162f * scale,
            158f * scale, 116f * scale
        )
        cubicTo(
            132f * scale, 150f * scale,
            58f * scale, 142f * scale,
            42f * scale, 98f * scale
        )
        close()
    }
    drawPath(bottomRimPath, pinkShadow)
    drawPath(bottomRimPath, inkColor, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // 2. Pink outer rim / casing
    val outerCenter = Offset(104f * scale, 92f * scale)
    val outerRadius = 60f * scale
    drawCircle(
        color = pinkBody,
        radius = outerRadius,
        center = outerCenter
    )
    drawCircle(
        color = inkColor,
        radius = outerRadius,
        center = outerCenter,
        style = Stroke(width = strokeW)
    )

    // 3. Inner White Face
    val faceCenter = Offset(104f * scale, 92f * scale)
    val faceRadius = 49f * scale
    drawCircle(
        color = creamFace,
        radius = faceRadius,
        center = faceCenter
    )
    drawCircle(
        color = inkColor,
        radius = faceRadius,
        center = faceCenter,
        style = Stroke(width = 3.4f * scale)
    )
}

/**
 * Adorable emotional happy face with closed smiling eyes, anime blush, and open cheerful mouth.
 */
private fun DrawScope.drawCuteHappyFace(
    inkColor: Color,
    scale: Float,
    alpha: Float
) {
    val faceCenter = Offset(104f * scale, 92f * scale)
    val eyeColor = inkColor.copy(alpha = alpha)

    // 1. ROSY BLUSH CHEEKS
    val blushColor = Color(0xFFFF6584).copy(alpha = 0.55f * alpha)
    val leftCheekCenter = Offset(faceCenter.x - 24f * scale, faceCenter.y + 11f * scale)
    val rightCheekCenter = Offset(faceCenter.x + 24f * scale, faceCenter.y + 11f * scale)
    val cheekRadius = 8.5f * scale

    drawCircle(
        color = blushColor,
        radius = cheekRadius,
        center = leftCheekCenter
    )
    drawCircle(
        color = blushColor,
        radius = cheekRadius,
        center = rightCheekCenter
    )

    // White shine highlights on cheeks
    val highlightColor = Color.White.copy(alpha = 0.75f * alpha)
    drawCircle(
        color = highlightColor,
        radius = 2.2f * scale,
        center = Offset(leftCheekCenter.x - 2.5f * scale, leftCheekCenter.y - 2.5f * scale)
    )
    drawCircle(
        color = highlightColor,
        radius = 2.2f * scale,
        center = Offset(rightCheekCenter.x - 2.5f * scale, rightCheekCenter.y - 2.5f * scale)
    )

    // Cute anime blush diagonal lines on cheeks
    val blushLineStroke = 1.8f * scale
    val blushLineColor = Color(0xFFD83A52).copy(alpha = 0.5f * alpha)
    for (i in -1..1) {
        val dx = i * 3.5f * scale
        drawLine(
            color = blushLineColor,
            start = Offset(leftCheekCenter.x + dx - 2f * scale, leftCheekCenter.y + 4f * scale),
            end = Offset(leftCheekCenter.x + dx + 2f * scale, leftCheekCenter.y - 4f * scale),
            strokeWidth = blushLineStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = blushLineColor,
            start = Offset(rightCheekCenter.x + dx - 2f * scale, rightCheekCenter.y + 4f * scale),
            end = Offset(rightCheekCenter.x + dx + 2f * scale, rightCheekCenter.y - 4f * scale),
            strokeWidth = blushLineStroke,
            cap = StrokeCap.Round
        )
    }

    // 2. HAPPY SMILING CLOSED EYES (^ ^)
    val eyeStroke = 3.6f * scale

    // Left happy eye arch
    val leftEyePath = Path().apply {
        moveTo(faceCenter.x - 27f * scale, faceCenter.y - 3f * scale)
        cubicTo(
            faceCenter.x - 23f * scale, faceCenter.y - 15f * scale,
            faceCenter.x - 14f * scale, faceCenter.y - 15f * scale,
            faceCenter.x - 10f * scale, faceCenter.y - 3f * scale
        )
    }
    drawPath(
        path = leftEyePath,
        color = eyeColor,
        style = Stroke(width = eyeStroke, cap = StrokeCap.Round)
    )

    // Left cute eyelash
    drawLine(
        color = eyeColor,
        start = Offset(faceCenter.x - 27f * scale, faceCenter.y - 3f * scale),
        end = Offset(faceCenter.x - 30f * scale, faceCenter.y - 1f * scale),
        strokeWidth = 2.4f * scale,
        cap = StrokeCap.Round
    )

    // Right happy eye arch
    val rightEyePath = Path().apply {
        moveTo(faceCenter.x + 10f * scale, faceCenter.y - 3f * scale)
        cubicTo(
            faceCenter.x + 14f * scale, faceCenter.y - 15f * scale,
            faceCenter.x + 23f * scale, faceCenter.y - 15f * scale,
            faceCenter.x + 27f * scale, faceCenter.y - 3f * scale
        )
    }
    drawPath(
        path = rightEyePath,
        color = eyeColor,
        style = Stroke(width = eyeStroke, cap = StrokeCap.Round)
    )

    // Right cute eyelash
    drawLine(
        color = eyeColor,
        start = Offset(faceCenter.x + 27f * scale, faceCenter.y - 3f * scale),
        end = Offset(faceCenter.x + 30f * scale, faceCenter.y - 1f * scale),
        strokeWidth = 2.4f * scale,
        cap = StrokeCap.Round
    )

    // 3. JOYFUL OPEN SMILING MOUTH
    val mouthPath = Path().apply {
        moveTo(faceCenter.x - 9f * scale, faceCenter.y + 13f * scale)
        cubicTo(
            faceCenter.x - 5f * scale, faceCenter.y + 24f * scale,
            faceCenter.x + 5f * scale, faceCenter.y + 24f * scale,
            faceCenter.x + 9f * scale, faceCenter.y + 13f * scale
        )
        close()
    }
    // Mouth interior pink fill
    drawPath(mouthPath, Color(0xFFE84D72).copy(alpha = alpha))
    // Tongue inner highlight
    val tonguePath = Path().apply {
        moveTo(faceCenter.x - 5f * scale, faceCenter.y + 17f * scale)
        cubicTo(
            faceCenter.x - 3f * scale, faceCenter.y + 23f * scale,
            faceCenter.x + 3f * scale, faceCenter.y + 23f * scale,
            faceCenter.x + 5f * scale, faceCenter.y + 17f * scale
        )
        close()
    }
    drawPath(tonguePath, Color(0xFFFF8DA1).copy(alpha = alpha))
    // Mouth outline
    drawPath(
        path = mouthPath,
        color = eyeColor,
        style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 4. GOLDEN SPARKLE NEAR RIGHT EYE
    val starCenter = Offset(faceCenter.x + 33f * scale, faceCenter.y - 15f * scale)
    val starColor = Color(0xFFFFB800).copy(alpha = alpha)
    val starSize = 5f * scale
    drawLine(
        color = starColor,
        start = Offset(starCenter.x - starSize, starCenter.y),
        end = Offset(starCenter.x + starSize, starCenter.y),
        strokeWidth = 2.2f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = starColor,
        start = Offset(starCenter.x, starCenter.y - starSize),
        end = Offset(starCenter.x, starCenter.y + starSize),
        strokeWidth = 2.2f * scale,
        cap = StrokeCap.Round
    )
}

/**
 * Hand-drawn ticks on the clock face matching the doodle.
 */
private fun DrawScope.drawClockFaceTicks(inkColor: Color, scale: Float) {
    val faceCenter = Offset(104f * scale, 92f * scale)
    val tickStroke = 3.2f * scale

    // 12 o'clock tick mark (slightly curved dash)
    drawLine(
        color = inkColor,
        start = Offset(faceCenter.x - 1f * scale, faceCenter.y - 41f * scale),
        end = Offset(faceCenter.x - 2f * scale, faceCenter.y - 34f * scale),
        strokeWidth = tickStroke,
        cap = StrokeCap.Round
    )

    // 3 o'clock mark (small dot/dash)
    drawCircle(
        color = inkColor,
        radius = 2.4f * scale,
        center = Offset(faceCenter.x + 39f * scale, faceCenter.y - 2f * scale)
    )

    // 6 o'clock mark (small vertical dash)
    drawLine(
        color = inkColor,
        start = Offset(faceCenter.x + 10f * scale, faceCenter.y + 36f * scale),
        end = Offset(faceCenter.x + 10f * scale, faceCenter.y + 42f * scale),
        strokeWidth = 2.8f * scale,
        cap = StrokeCap.Round
    )

    // 8 o'clock curved tick mark (distinctive detail from doodle!)
    val eightMark = Path().apply {
        moveTo(faceCenter.x - 40f * scale, faceCenter.y + 7f * scale)
        cubicTo(
            faceCenter.x - 36f * scale, faceCenter.y + 11f * scale,
            faceCenter.x - 32f * scale, faceCenter.y + 9f * scale,
            faceCenter.x - 30f * scale, faceCenter.y + 5f * scale
        )
    }
    drawPath(
        path = eightMark,
        color = inkColor,
        style = Stroke(width = 2.8f * scale, cap = StrokeCap.Round)
    )
}

/**
 * Animated Comic Clock Hands with animated smooth rotation or real-time tracking.
 */
private fun DrawScope.drawClockHands(
    inkColor: Color,
    scale: Float,
    hour: Int,
    minute: Int,
    second: Int,
    isAlarmActive: Boolean
) {
    val center = Offset(104f * scale, 92f * scale)

    // Hand angles:
    // Default in doodle: Hour pointing at ~10 o'clock (-60°), Minute pointing at ~1 o'clock (+30°)
    val hourAngle = (hour % 12 + minute / 60f) * 30f - 90f
    val minuteAngle = (minute + second / 60f) * 6f - 90f
    val secondAngle = second * 6f - 90f

    // 1. HOUR HAND (Tapered bold comic pen stroke)
    rotate(hourAngle, center) {
        val hourPath = Path().apply {
            moveTo(center.x, center.y - 4f * scale)
            lineTo(center.x + 28f * scale, center.y - 1.5f * scale)
            cubicTo(
                center.x + 31f * scale, center.y,
                center.x + 31f * scale, center.y,
                center.x + 28f * scale, center.y + 1.5f * scale
            )
            lineTo(center.x, center.y + 4f * scale)
            close()
        }
        drawPath(hourPath, inkColor)
    }

    // 2. MINUTE HAND (Longer expressive hand)
    rotate(minuteAngle, center) {
        val minPath = Path().apply {
            moveTo(center.x, center.y - 3.2f * scale)
            lineTo(center.x + 38f * scale, center.y - 1.2f * scale)
            cubicTo(
                center.x + 41f * scale, center.y,
                center.x + 41f * scale, center.y,
                center.x + 38f * scale, center.y + 1.2f * scale
            )
            lineTo(center.x, center.y + 3.2f * scale)
            close()
        }
        drawPath(minPath, inkColor)
    }

    // 3. SECOND POINTER (Red / deep accent ticking tip)
    rotate(secondAngle, center) {
        drawLine(
            color = if (isAlarmActive) Color(0xFFD83A52) else LuneColors.primary,
            start = center,
            end = Offset(center.x + 42f * scale, center.y),
            strokeWidth = 2.2f * scale,
            cap = StrokeCap.Round
        )
        // Small counterbalance dot
        drawCircle(
            color = if (isAlarmActive) Color(0xFFD83A52) else LuneColors.primary,
            radius = 3f * scale,
            center = Offset(center.x - 9f * scale, center.y)
        )
    }

    // 4. CENTER PIVOT PIN (Bold black circle matching drawing)
    drawCircle(
        color = inkColor,
        radius = 5.5f * scale,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 1.8f * scale,
        center = center
    )
}
