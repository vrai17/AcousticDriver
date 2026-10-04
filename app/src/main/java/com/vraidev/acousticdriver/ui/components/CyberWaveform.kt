package com.vraidev.acousticdriver.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun CyberWaveform(
    gains: List<Float>,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            // Draw center baseline (0 dB)
            drawLine(
                color = BorderSubtle,
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Draw +6dB and -6dB guidelines
            val step6Db = (height / 2f) * (6f / 12f)
            drawLine(
                color = BorderSubtle.copy(alpha = 0.5f),
                start = Offset(0f, midY - step6Db),
                end = Offset(width, midY - step6Db),
                strokeWidth = 1f
            )
            drawLine(
                color = BorderSubtle.copy(alpha = 0.5f),
                start = Offset(0f, midY + step6Db),
                end = Offset(width, midY + step6Db),
                strokeWidth = 1f
            )

            if (gains.isEmpty()) return@Canvas

            val path = Path()
            val fillPath = Path()

            val stepX = width / (gains.size + 1)
            var prevX = 0f
            var prevY = midY

            fillPath.moveTo(0f, height)
            fillPath.lineTo(0f, midY)
            path.moveTo(0f, midY)

            for (i in gains.indices) {
                val currentX = (i + 1) * stepX
                val gain = if (isActive) gains[i].coerceIn(-12f, 12f) else 0f
                val currentY = midY - (gain / 12f) * (height * 0.42f)

                val controlX = (prevX + currentX) / 2f
                path.cubicTo(controlX, prevY, controlX, currentY, currentX, currentY)
                fillPath.cubicTo(controlX, prevY, controlX, currentY, currentX, currentY)

                prevX = currentX
                prevY = currentY
            }

            path.lineTo(width, midY)
            fillPath.lineTo(width, midY)
            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw gradient area under the curve
            val fillBrush = Brush.verticalGradient(
                colors = listOf(
                    CyberCyan.copy(alpha = if (isActive) 0.35f * pulseAlpha else 0.05f),
                    ElectricIce.copy(alpha = if (isActive) 0.15f * pulseAlpha else 0.02f),
                    Color.Transparent
                )
            )
            drawPath(fillPath, fillBrush)

            // Draw neon stroke line
            val strokeColor = if (isActive) CyberCyan else TextMuted
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = 3.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw band nodes
            if (isActive) {
                for (i in gains.indices) {
                    val currentX = (i + 1) * stepX
                    val gain = gains[i].coerceIn(-12f, 12f)
                    val currentY = midY - (gain / 12f) * (height * 0.42f)

                    drawCircle(
                        color = BgDark,
                        radius = 5.dp.toPx(),
                        center = Offset(currentX, currentY)
                    )
                    drawCircle(
                        color = CyberCyan,
                        radius = 3.5.dp.toPx(),
                        center = Offset(currentX, currentY)
                    )
                }
            }
        }
    }
}
