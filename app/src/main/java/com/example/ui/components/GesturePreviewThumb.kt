package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.GesturePoint
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RainbowColors
import kotlin.math.max

@Composable
fun GesturePreviewThumb(
    points: List<GesturePoint>,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeColor: Color = NeonCyan
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B1020))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.size(size)) {
            if (points.size < 2) return@Canvas

            var minX = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE

            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }

            val rawW = max(maxX - minX, 1f)
            val rawH = max(maxY - minY, 1f)

            // Padding inside thumb
            val padding = this.size.minDimension * 0.22f
            val targetSize = this.size.minDimension - (padding * 2f)

            val scale = targetSize / max(rawW, rawH)
            val centerX = (minX + maxX) / 2f
            val centerY = (minY + maxY) / 2f

            val canvasCenterX = this.size.width / 2f
            val canvasCenterY = this.size.height / 2f

            val path = Path()
            var currentStroke = -1
            for (p in points) {
                val px = canvasCenterX + (p.x - centerX) * scale
                val py = canvasCenterY + (p.y - centerY) * scale
                if (p.strokeId != currentStroke) {
                    path.moveTo(px, py)
                    currentStroke = p.strokeId
                } else {
                    path.lineTo(px, py)
                }
            }

            val rainbowBrush = Brush.linearGradient(
                colors = RainbowColors,
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )

            // Glow stroke
            drawPath(
                path = path,
                brush = rainbowBrush,
                alpha = 0.45f,
                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Core stroke
            drawPath(
                path = path,
                brush = rainbowBrush,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
