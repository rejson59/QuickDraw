package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.model.GesturePoint
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RainbowColors
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GestureDrawingCanvas(
    modifier: Modifier = Modifier,
    strokeColor: Color = NeonCyan,
    onStrokeFinished: (List<GesturePoint>) -> Unit,
    hintText: String = "Narysuj gest (możesz odrywać rękę, by napisać np. literę T, X, +)"
) {
    val context = LocalContext.current
    val strokeDelayMs = remember { AppSettings.getStrokeDelayMs(context) }
    val points = remember { mutableStateListOf<GesturePoint>() }
    var currentStrokeId by remember { mutableIntStateOf(0) }
    var isWaitingForNextStroke by remember { mutableStateOf(false) }
    var timerProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    var finishJob by remember { mutableStateOf<Job?>(null) }

    fun finalizeGesture() {
        finishJob?.cancel()
        finishJob = null
        isWaitingForNextStroke = false
        timerProgress = 0f
        if (points.size >= 3) {
            onStrokeFinished(points.toList())
        }
    }

    fun startDelayTimer() {
        finishJob?.cancel()
        isWaitingForNextStroke = true
        timerProgress = 1f
        finishJob = scope.launch {
            val steps = 20
            val interval = strokeDelayMs / steps
            for (i in (steps - 1) downTo 0) {
                delay(interval)
                timerProgress = i.toFloat() / steps.toFloat()
            }
            isWaitingForNextStroke = false
            timerProgress = 0f
            if (points.size >= 3) {
                onStrokeFinished(points.toList())
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF070B16))
            .border(1.5.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp))
    ) {
        // Subtle futuristic grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color(0x0C00E5FF),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color(0x0C00E5FF),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        // Hint text if empty
        if (points.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✍️",
                    fontSize = 32.sp
                )
                Text(
                    text = hintText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 18.sp
                )
                Text(
                    text = "Obsługuje litery wielokreskowe z odrywaniem ręki",
                    color = NeonCyan.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Active drawing canvas with pointerInput
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            finishJob?.cancel()
                            finishJob = null
                            isWaitingForNextStroke = false
                            timerProgress = 0f

                            if (points.isEmpty()) {
                                currentStrokeId = 0
                            } else {
                                // User lifted hand and is now drawing the next stroke of the gesture!
                                currentStrokeId++
                            }
                            points.add(GesturePoint(offset.x, offset.y, currentStrokeId))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val pos = change.position
                            points.add(GesturePoint(pos.x, pos.y, currentStrokeId))
                        },
                        onDragEnd = {
                            if (points.size >= 2) {
                                startDelayTimer()
                            }
                        },
                        onDragCancel = {
                            if (points.size >= 2) {
                                startDelayTimer()
                            }
                        }
                    )
                }
        ) {
            if (points.size >= 2) {
                var minX = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxY = -Float.MAX_VALUE

                val path = Path()
                var activeStroke = -1

                for (p in points) {
                    if (p.x < minX) minX = p.x
                    if (p.x > maxX) maxX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.y > maxY) maxY = p.y

                    if (p.strokeId != activeStroke) {
                        path.moveTo(p.x, p.y)
                        activeStroke = p.strokeId
                    } else {
                        path.lineTo(p.x, p.y)
                    }
                }

                val startOffset = Offset(minX, minY)
                val endOffset = Offset(
                    if (maxX - minX < 20f) minX + 50f else maxX,
                    if (maxY - minY < 20f) minY + 50f else maxY
                )

                val rainbowBrush = Brush.linearGradient(
                    colors = RainbowColors,
                    start = startOffset,
                    end = endOffset
                )
                val ambientRainbowBrush = Brush.linearGradient(
                    colors = RainbowColors.map { it.copy(alpha = 0.30f) },
                    start = startOffset,
                    end = endOffset
                )

                // Outer ambient rainbow glow
                drawPath(
                    path = path,
                    brush = ambientRainbowBrush,
                    style = Stroke(
                        width = 22.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Main vibrant rainbow stroke
                drawPath(
                    path = path,
                    brush = rainbowBrush,
                    style = Stroke(
                        width = 9.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // White bright core
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Top Action controls (Clear & Strokes indicator)
        if (points.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stroke count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC121829))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Kreski: ${currentStrokeId + 1}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick finish button
                    Button(
                        onClick = { finalizeGesture() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Gotowe", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zatwierdź", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Clear button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xAA121829),
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(
                            onClick = {
                                finishJob?.cancel()
                                finishJob = null
                                isWaitingForNextStroke = false
                                points.clear()
                                currentStrokeId = 0
                                onStrokeFinished(emptyList())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Wyczyść gest",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom multi-stroke countdown prompt
        AnimatedVisibility(
            visible = isWaitingForNextStroke,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xEA111927))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Oderwano palec — możesz dorysować kolejną kreskę...",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { timerProgress },
                    modifier = Modifier
                        .width(180.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonCyan,
                    trackColor = Color(0x3300E5FF)
                )
            }
        }
    }
}
