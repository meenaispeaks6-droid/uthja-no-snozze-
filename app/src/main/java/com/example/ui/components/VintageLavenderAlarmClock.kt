package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Aesthetic vintage pastel lavender twin-bell analog alarm clock
 * matching the 3D clay rendered clock from the uploaded design screenshot.
 */
@Composable
fun VintageLavenderAlarmClock(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    hour: Int = 6,
    minute: Int = 30
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.50f
            val cy = h * 0.54f
            val clockRadius = w * 0.35f

            // 1. Soft Ambient Shadow Underneath
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x334B2E83), Color.Transparent),
                    center = Offset(cx, cy + clockRadius * 1.05f),
                    radius = clockRadius * 0.9f
                ),
                topLeft = Offset(cx - clockRadius * 0.85f, cy + clockRadius * 0.82f),
                size = androidx.compose.ui.geometry.Size(clockRadius * 1.7f, clockRadius * 0.45f)
            )

            // 2. Twin Peg Feet (Angled Left and Right)
            val footColor = Color(0xFF7E66B4)
            val footHighlight = Color(0xFFA68EE0)

            // Left Foot
            val leftFootPath = Path().apply {
                moveTo(cx - clockRadius * 0.65f, cy + clockRadius * 0.65f)
                lineTo(cx - clockRadius * 0.82f, cy + clockRadius * 1.05f)
                lineTo(cx - clockRadius * 0.72f, cy + clockRadius * 1.05f)
                lineTo(cx - clockRadius * 0.55f, cy + clockRadius * 0.70f)
                close()
            }
            drawPath(leftFootPath, footColor)

            // Right Foot
            val rightFootPath = Path().apply {
                moveTo(cx + clockRadius * 0.65f, cy + clockRadius * 0.65f)
                lineTo(cx + clockRadius * 0.82f, cy + clockRadius * 1.05f)
                lineTo(cx + clockRadius * 0.72f, cy + clockRadius * 1.05f)
                lineTo(cx + clockRadius * 0.55f, cy + clockRadius * 0.70f)
                close()
            }
            drawPath(rightFootPath, footColor)

            // 3. Top Center Hammer & Handle
            // Handle Arch
            val handlePath = Path().apply {
                moveTo(cx - clockRadius * 0.45f, cy - clockRadius * 0.82f)
                cubicTo(
                    cx - clockRadius * 0.45f, cy - clockRadius * 1.25f,
                    cx + clockRadius * 0.45f, cy - clockRadius * 1.25f,
                    cx + clockRadius * 0.45f, cy - clockRadius * 0.82f
                )
            }
            drawPath(handlePath, Color(0xFF9077C6), style = Stroke(width = w * 0.045f, cap = StrokeCap.Round))

            // Center Hammer Peg
            drawCircle(
                color = Color(0xFF6E56A0),
                radius = w * 0.038f,
                center = Offset(cx, cy - clockRadius * 0.98f)
            )

            // 4. Twin Bells on Top Left & Top Right
            val bellColor = Color(0xFF9B82D4)
            val bellShadow = Color(0xFF755CAE)
            val bellShine = Color(0xFFD6C8F8)

            // Left Bell
            rotate(degrees = -36f, pivot = Offset(cx - clockRadius * 0.70f, cy - clockRadius * 0.70f)) {
                val bellCenter = Offset(cx - clockRadius * 0.70f, cy - clockRadius * 0.70f)
                val bellWidth = clockRadius * 0.56f
                val bellHeight = clockRadius * 0.38f
                drawOval(
                    brush = Brush.verticalGradient(listOf(bellShine, bellColor, bellShadow)),
                    topLeft = Offset(bellCenter.x - bellWidth / 2, bellCenter.y - bellHeight / 2),
                    size = androidx.compose.ui.geometry.Size(bellWidth, bellHeight)
                )
            }

            // Right Bell
            rotate(degrees = 36f, pivot = Offset(cx + clockRadius * 0.70f, cy - clockRadius * 0.70f)) {
                val bellCenter = Offset(cx + clockRadius * 0.70f, cy - clockRadius * 0.70f)
                val bellWidth = clockRadius * 0.56f
                val bellHeight = clockRadius * 0.38f
                drawOval(
                    brush = Brush.verticalGradient(listOf(bellShine, bellColor, bellShadow)),
                    topLeft = Offset(bellCenter.x - bellWidth / 2, bellCenter.y - bellHeight / 2),
                    size = androidx.compose.ui.geometry.Size(bellWidth, bellHeight)
                )
            }

            // 5. Main Clock Casing (Outer Rim with Smooth 3D Lighting)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFBCA7F5), Color(0xFF8D73C8), Color(0xFF674F9F)),
                    center = Offset(cx - clockRadius * 0.35f, cy - clockRadius * 0.35f),
                    radius = clockRadius * 1.3f
                ),
                radius = clockRadius,
                center = Offset(cx, cy)
            )

            // Inner Metallic Rim Bezel
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE9E0FD), Color(0xFF735C9E))
                ),
                radius = clockRadius * 0.88f,
                center = Offset(cx, cy),
                style = Stroke(width = clockRadius * 0.06f)
            )

            // 6. Clock Face (Cream / Off-White Dial)
            val faceRadius = clockRadius * 0.82f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFCFAF6), Color(0xFFF1EBE1)),
                    center = Offset(cx, cy),
                    radius = faceRadius
                ),
                radius = faceRadius,
                center = Offset(cx, cy)
            )

            // 7. Dial Hour Tick Marks
            for (i in 0 until 12) {
                val angle = (i * 30f) * (PI / 180f)
                val tickOuter = faceRadius * 0.88f
                val isMajor = i % 3 == 0
                val tickInner = if (isMajor) faceRadius * 0.74f else faceRadius * 0.80f
                val strokeW = if (isMajor) 2.2f else 1.2f
                val tickColor = if (isMajor) Color(0xFF423456) else Color(0xFF8F859C)

                val x1 = cx + (tickInner * sin(angle)).toFloat()
                val y1 = cy - (tickInner * cos(angle)).toFloat()
                val x2 = cx + (tickOuter * sin(angle)).toFloat()
                val y2 = cy - (tickOuter * cos(angle)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // 8. Clock Hands (Hour & Minute)
            val hourAngle = ((hour % 12 + minute / 60f) * 30f) * (PI / 180f)
            val minuteAngle = (minute * 6f) * (PI / 180f)

            // Hour Hand
            val hourLength = faceRadius * 0.50f
            val hx = cx + (hourLength * sin(hourAngle)).toFloat()
            val hy = cy - (hourLength * cos(hourAngle)).toFloat()
            drawLine(
                color = Color(0xFF2A1C3E),
                start = Offset(cx, cy),
                end = Offset(hx, hy),
                strokeWidth = w * 0.034f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minLength = faceRadius * 0.72f
            val mx = cx + (minLength * sin(minuteAngle)).toFloat()
            val my = cy - (minLength * cos(minuteAngle)).toFloat()
            drawLine(
                color = Color(0xFF2A1C3E),
                start = Offset(cx, cy),
                end = Offset(mx, my),
                strokeWidth = w * 0.024f,
                cap = StrokeCap.Round
            )

            // Second Hand (Delicate Soft Lilac)
            val secLength = faceRadius * 0.78f
            val secAngle = 45f * (PI / 180f)
            val sx = cx + (secLength * sin(secAngle)).toFloat()
            val sy = cy - (secLength * cos(secAngle)).toFloat()
            drawLine(
                color = Color(0xFFA889F4),
                start = Offset(cx, cy),
                end = Offset(sx, sy),
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )

            // Center Pin Cap
            drawCircle(
                color = Color(0xFF755EA8),
                radius = w * 0.038f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFFDED3F8),
                radius = w * 0.016f,
                center = Offset(cx - 1.5f, cy - 1.5f)
            )
        }
    }
}
