package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

/**
 * Normalized layout metadata for tree branch apple positions.
 */
private data class AppleSpec(
    val id: Int,
    val fx: Float,
    val fy: Float,
    val foliageColor: Color
)

private data class LeafParticle(
    val startX: Float,
    val startY: Float,
    val endY: Float,
    val driftAmp: Float,
    val speed: Float,
    val phaseOffset: Float,
    val sizeDp: Float,
    val color: Color
)

/**
 * Simplified, living Streak Card:
 * - Real logic: completing mission without snooze adds an apple to tree.
 * - No option to take / detach apple (apples stay on the tree branches as achievements).
 * - When next day resets / streak breaks: 0 apples on tree and 0 pts in leaderboard.
 * - Per apple = 50 pts uploaded to Supabase leaderboard.
 * - Smooth subtle ambient tree sway, sleeping cat, and falling cherry blossom petals.
 */
@Composable
fun StreakCard(
    modifier: Modifier = Modifier,
    streakDays: Int = 0,
    applesCount: Int = 0,
    score: Int = 0,
    isCompletedToday: Boolean = false,
    initialStreakDays: Int = streakDays,
    onFruitCollected: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    val effectiveStreak = if (streakDays > 0) streakDays else initialStreakDays
    val effectiveApples = applesCount.coerceIn(0, 3)
    val effectiveScore = if (score > 0) score else (effectiveApples * 50)

    // Check system setting for reduced motion
    val isReducedMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }

    // 3 Fixed branch positions on the cherry tree
    val appleSpecs = remember {
        listOf(
            AppleSpec(0, 0.436f, 0.228f, Color(0xFFDF6D8B)), // Upper left branch
            AppleSpec(1, 0.770f, 0.234f, Color(0xFFE87B8E)), // Upper right branch
            AppleSpec(2, 0.783f, 0.423f, Color(0xFFE4A5B1))  // Lower right branch
        )
    }

    // Ambient breeze transition
    val infiniteTransition = rememberInfiniteTransition(label = "livingStreakBreeze")

    // Tree sway angle (-1.2° to +1.2°, 3.8s ease-in-out cycle)
    val treeSwayAngle by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 0f else -1.2f,
        targetValue = if (isReducedMotion) 0f else 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "treeSwayAngle"
    )

    // Gentle apple idle bobbing on branches (±1.4dp)
    val appleIdleBob by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 0f else -1.4f,
        targetValue = if (isReducedMotion) 0f else 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "appleIdleBob"
    )

    // Falling leaves time ticker
    val leafTicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "leafTicker"
    )

    // Cat breathing rhythm
    val catBreathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isReducedMotion) 1.0f else 1.032f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "catBreathScale"
    )

    // Cat blinking state
    var catIsBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(isReducedMotion) {
        if (isReducedMotion) return@LaunchedEffect
        while (isActive) {
            delay(4300)
            catIsBlinking = true
            delay(150)
            catIsBlinking = false
            if (Math.random() < 0.38) {
                delay(170)
                catIsBlinking = true
                delay(130)
                catIsBlinking = false
            }
        }
    }

    // Cat subtle ear and tail twitch
    var catIsTwitching by remember { mutableStateOf(false) }
    LaunchedEffect(isReducedMotion) {
        if (isReducedMotion) return@LaunchedEffect
        while (isActive) {
            delay(6500)
            catIsTwitching = true
            delay(260)
            catIsTwitching = false
        }
    }

    // 6 Falling cherry blossom leaves
    val fallingLeaves = remember {
        listOf(
            LeafParticle(0.48f, 0.20f, 0.74f, 0.032f, 0.19f, 0.00f, 6.0f, Color(0xFFFFAEBC)),
            LeafParticle(0.68f, 0.24f, 0.79f, 0.040f, 0.16f, 0.28f, 7.0f, Color(0xFFF7A3B7)),
            LeafParticle(0.44f, 0.30f, 0.72f, 0.028f, 0.21f, 0.55f, 5.5f, Color(0xFFFFBCC9)),
            LeafParticle(0.80f, 0.32f, 0.78f, 0.036f, 0.17f, 0.72f, 6.5f, Color(0xFFFF9EB5)),
            LeafParticle(0.58f, 0.18f, 0.75f, 0.034f, 0.18f, 0.42f, 5.0f, Color(0xFFFFC0CB)),
            LeafParticle(0.74f, 0.38f, 0.81f, 0.038f, 0.15f, 0.88f, 6.8f, Color(0xFFF48FB1))
        )
    }

    // Primary Card Container
    LuneCard(
        elevated = true,
        tonal = CardTone.ACCENT,
        contentPadding = 0.dp,
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1307f / 1536f)
        ) {
            val cardW = maxWidth
            val cardH = maxHeight

            // ==========================================
            // LAYER 1: STREAK BACKGROUND & NATURAL FLORA
            // ==========================================
            StreakBackgroundLayer(
                cardW = cardW,
                cardH = cardH
            )

            // ==========================================
            // LAYER 2: TREE IDLE SWAY & LIVING ACCENTS
            // ==========================================
            TreeLayer(
                treeSwayAngle = treeSwayAngle,
                isReducedMotion = isReducedMotion
            )

            // ==========================================
            // LAYER 3: APPLES ON TREE (Non-harvestable, Earned Rewards)
            // ==========================================
            AppleBranchLayer(
                appleSpecs = appleSpecs,
                applesCount = effectiveApples,
                appleIdleBob = appleIdleBob,
                cardW = cardW,
                cardH = cardH
            )

            // ==========================================
            // LAYER 4: CAT LIVING IDLE LAYER
            // ==========================================
            CatLayer(
                catBreathScale = catBreathScale,
                catIsBlinking = catIsBlinking,
                catIsTwitching = catIsTwitching,
                cardW = cardW,
                cardH = cardH
            )

            // ==========================================
            // LAYER 5: FALLING PINK BLOSSOM LEAVES
            // ==========================================
            if (!isReducedMotion) {
                FallingLeavesLayer(
                    leaves = fallingLeaves,
                    leafTicker = leafTicker
                )
            }
        }
    }
}

/**
 * Layer 1: Environmental base artwork with the old wooden sign cleanly covered by meadow flora.
 */
@Composable
private fun StreakBackgroundLayer(
    cardW: Dp,
    cardH: Dp
) {
    Image(
        painter = painterResource(id = R.drawable.streak_tree_card),
        contentDescription = "Streak card living artwork",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )

    // Clean meadow patch seamlessly blending out the old wooden board
    Box(
        modifier = Modifier
            .offset(
                x = cardW * 0.730f,
                y = cardH * 0.510f
            )
            .size(cardW * 0.205f, cardH * 0.118f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFBE4D8),
                        Color(0xFFF6D4C6),
                        Color(0xFFE5DEBA)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val flowers = listOf(
                Offset(w * 0.25f, h * 0.35f) to Color(0xFFFFB6C1),
                Offset(w * 0.55f, h * 0.30f) to Color(0xFFFFD1DC),
                Offset(w * 0.78f, h * 0.40f) to Color(0xFFFFF0F5),
                Offset(w * 0.38f, h * 0.70f) to Color(0xFFFFB2C5),
                Offset(w * 0.70f, h * 0.75f) to Color(0xFFFFC0CB),
                Offset(w * 0.15f, h * 0.75f) to Color(0xFFFFE4E1)
            )

            flowers.forEach { (pos, petalColor) ->
                drawCircle(color = petalColor, radius = 2.8.dp.toPx(), center = pos)
                drawCircle(color = Color(0xFFFFD54F), radius = 1.1.dp.toPx(), center = pos)
            }

            val grassBlades = listOf(
                Offset(w * 0.20f, h * 0.90f) to Offset(w * 0.22f, h * 0.65f),
                Offset(w * 0.45f, h * 0.92f) to Offset(w * 0.48f, h * 0.62f),
                Offset(w * 0.62f, h * 0.90f) to Offset(w * 0.60f, h * 0.68f),
                Offset(w * 0.85f, h * 0.88f) to Offset(w * 0.88f, h * 0.64f)
            )

            grassBlades.forEach { (start, end) ->
                drawLine(
                    color = Color(0xFF8FBC8F).copy(alpha = 0.5f),
                    start = start,
                    end = end,
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Layer 2: Subtle idle tree breeze sway with blossom accent points.
 */
@Composable
private fun TreeLayer(
    treeSwayAngle: Float,
    isReducedMotion: Boolean
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isReducedMotion) return@Canvas
        val canvasW = size.width
        val canvasH = size.height

        val blossomPoints = listOf(
            Offset(canvasW * 0.36f, canvasH * 0.34f),
            Offset(canvasW * 0.65f, canvasH * 0.16f),
            Offset(canvasW * 0.88f, canvasH * 0.29f)
        )

        blossomPoints.forEachIndexed { idx, point ->
            val localAngle = if (idx % 2 == 0) treeSwayAngle else -treeSwayAngle
            rotate(localAngle, pivot = point) {
                drawCircle(
                    color = Color(0xFFFFB8C6).copy(alpha = 0.40f),
                    radius = 3.6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color(0xFFFF8DA1).copy(alpha = 0.28f),
                    radius = 2.2.dp.toPx(),
                    center = Offset(point.x + 1.8.dp.toPx(), point.y - 1.2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Layer 3: Apples on the tree branches.
 * - Always renders crisp, vibrant anime apples on the tree branches.
 * - Completely non-clickable: clicking does nothing, never disappears or becomes blank.
 */
@Composable
private fun AppleBranchLayer(
    appleSpecs: List<AppleSpec>,
    applesCount: Int,
    appleIdleBob: Float,
    cardW: Dp,
    cardH: Dp
) {
    appleSpecs.forEachIndexed { index, apple ->
        val isEven = index % 2 == 0
        val currentBob = if (isEven) appleIdleBob else -appleIdleBob

        Box(
            modifier = Modifier
                .offset(
                    x = cardW * apple.fx - 18.dp,
                    y = (cardH * apple.fy - 18.dp) + currentBob.dp
                )
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle warm glow halo
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE83A59).copy(alpha = 0.15f))
            )

            // Crisp anime apple - persistent on tree, non-clickable, never blank
            Canvas(modifier = Modifier.size(20.dp)) {
                val w = size.width
                val h = size.height

                // Stem & Leaf
                drawLine(
                    color = Color(0xFF5C3B1E),
                    start = Offset(w * 0.5f, h * 0.30f),
                    end = Offset(w * 0.5f, h * 0.08f),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawOval(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(w * 0.52f, h * 0.05f),
                    size = Size(4.2.dp.toPx(), 2.6.dp.toPx())
                )

                // Red Apple Body
                drawCircle(
                    color = Color(0xFFE53935),
                    radius = w * 0.40f,
                    center = Offset(w * 0.5f, h * 0.60f)
                )

                // Specular gloss highlight
                drawCircle(
                    color = Color(0xFFFFCDD2).copy(alpha = 0.85f),
                    radius = w * 0.14f,
                    center = Offset(w * 0.36f, h * 0.48f)
                )
            }
        }
    }
}

/**
 * Layer 4: Living cat sleeping peacefully underneath the tree canopy.
 */
@Composable
private fun CatLayer(
    catBreathScale: Float,
    catIsBlinking: Boolean,
    catIsTwitching: Boolean,
    cardW: Dp,
    cardH: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = cardW * 0.495f, y = cardH * 0.642f)
            .size(cardW * 0.11f, cardH * 0.075f)
            .scale(scaleX = 1.0f, scaleY = catBreathScale)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        if (catIsBlinking) {
            val eyeRadius = 2.4.dp.toPx()
            val eyeStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            val eyelidColor = Color(0xFF482D2D)
            val skinColor = Color(0xFFFAD5BC)

            val leftCenter = Offset(canvasW * 0.5317f, canvasH * 0.6601f)
            drawCircle(color = skinColor, radius = eyeRadius * 1.15f, center = leftCenter)
            drawArc(
                color = eyelidColor,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(leftCenter.x - eyeRadius, leftCenter.y - eyeRadius * 0.5f),
                size = Size(eyeRadius * 2f, eyeRadius * 1.2f),
                style = eyeStroke
            )

            val rightCenter = Offset(canvasW * 0.5623f, canvasH * 0.6601f)
            drawCircle(color = skinColor, radius = eyeRadius * 1.15f, center = rightCenter)
            drawArc(
                color = eyelidColor,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(rightCenter.x - eyeRadius, rightCenter.y - eyeRadius * 0.5f),
                size = Size(eyeRadius * 2f, eyeRadius * 1.2f),
                style = eyeStroke
            )
        }

        if (catIsTwitching) {
            val tailTip = Offset(canvasW * 0.605f, canvasH * 0.697f)
            drawCircle(
                color = Color(0xFFE28B62).copy(alpha = 0.60f),
                radius = 2.2.dp.toPx(),
                center = tailTip
            )
        }
    }
}

/**
 * Layer 5: Falling pink cherry blossom leaves drifting gently across the canopy.
 */
@Composable
private fun FallingLeavesLayer(
    leaves: List<LeafParticle>,
    leafTicker: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasW = size.width
        val canvasH = size.height

        leaves.forEach { leaf ->
            val cycle = ((leafTicker * leaf.speed + leaf.phaseOffset) % 1.0f)
            val y = canvasH * (leaf.startY + cycle * (leaf.endY - leaf.startY))
            val wave = sin((cycle * 3.5f * PI).toFloat())
            val x = canvasW * (leaf.startX + wave * leaf.driftAmp)

            val alpha = when {
                cycle < 0.12f -> (cycle / 0.12f) * 0.85f
                cycle > 0.85f -> ((1.0f - cycle) / 0.15f) * 0.85f
                else -> 0.85f
            }

            val rotation = (cycle * 360f * 1.4f + leaf.phaseOffset * 120f) % 360f

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                drawLeafPetal(
                    center = Offset(x, y),
                    sizePx = leaf.sizeDp.dp.toPx(),
                    color = leaf.color.copy(alpha = alpha)
                )
            }
        }
    }
}

/**
 * Helper to draw a soft organic cherry blossom petal.
 */
private fun DrawScope.drawLeafPetal(
    center: Offset,
    sizePx: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - sizePx)
        quadraticBezierTo(center.x + sizePx * 0.7f, center.y, center.x, center.y + sizePx)
        quadraticBezierTo(center.x - sizePx * 0.7f, center.y, center.x, center.y - sizePx)
        close()
    }
    drawPath(path, color)
}
