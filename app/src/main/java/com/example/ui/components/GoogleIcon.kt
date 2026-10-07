package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoogleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w / 2f

        val red = Color(0xFFEA4335)
        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC05)

        // Red segment (top arc)
        val redPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = -180f,
                sweepAngleDegrees = 115f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(redPath, red, style = Fill)

        // Blue segment (top-right arc)
        val bluePath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = -65f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(bluePath, blue, style = Fill)

        // Green segment (bottom arc)
        val greenPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 25f,
                sweepAngleDegrees = 110f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(greenPath, green, style = Fill)

        // Yellow segment (bottom-left arc)
        val yellowPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = Rect(0f, 0f, w, h),
                startAngleDegrees = 135f,
                sweepAngleDegrees = 45f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(yellowPath, yellow, style = Fill)

        // Inner center white cutout
        drawCircle(
            color = Color.White,
            radius = radius * 0.58f,
            center = Offset(cx, cy)
        )

        // Blue horizontal crossbar
        drawRect(
            color = blue,
            topLeft = Offset(cx - radius * 0.05f, cy - radius * 0.20f),
            size = Size(radius * 1.05f, radius * 0.40f)
        )

        // Small inner circle cutout behind bar
        drawCircle(
            color = Color.White,
            radius = radius * 0.42f,
            center = Offset(cx, cy)
        )

        // Re-draw right part of crossbar
        drawRect(
            color = blue,
            topLeft = Offset(cx, cy - radius * 0.20f),
            size = Size(radius, radius * 0.40f)
        )
    }
}
