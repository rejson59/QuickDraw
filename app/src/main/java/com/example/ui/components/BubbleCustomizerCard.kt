package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.service.QuickDrawOverlayService
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BubbleColorOption(
    val name: String,
    val hexColor: Int,
    val composeColor: Color
)

val BUBBLE_COLOR_OPTIONS = listOf(
    BubbleColorOption("Cyan", 0xFF00E5FF.toInt(), NeonCyan),
    BubbleColorOption("Szmaragd", 0xFF00E676.toInt(), NeonGreen),
    BubbleColorOption("Fiolet", 0xFFD500F9.toInt(), Color(0xFFD500F9)),
    BubbleColorOption("Pomarańcz", 0xFFFF9100.toInt(), NeonOrange),
    BubbleColorOption("Róż", 0xFFFF1744.toInt(), Color(0xFFFF1744)),
    BubbleColorOption("Biel", 0xFFFFFFFF.toInt(), Color.White)
)

@Composable
fun BubbleCustomizerCard(
    isServiceRunning: Boolean,
    hasOverlayPermission: Boolean,
    onToggleService: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var edge by remember { mutableStateOf(AppSettings.getBubbleEdge(context)) }
    var shape by remember { mutableStateOf(AppSettings.getBubbleShape(context)) }
    var size by remember { mutableStateOf(AppSettings.getBubbleSize(context)) }
    var colorHex by remember { mutableIntStateOf(AppSettings.getBubbleColor(context)) }
    var opacity by remember { mutableFloatStateOf(AppSettings.getBubbleOpacity(context)) }
    var indicator by remember { mutableStateOf(AppSettings.getBubbleIndicator(context)) }
    var haptic by remember { mutableStateOf(AppSettings.isBubbleHapticEnabled(context)) }

    // Test click feedback state
    var previewClicked by remember { mutableStateOf(false) }

    fun notifySettingsChanged() {
        QuickDrawOverlayService.updateBubble(context)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Pływający bąbelek na krawędzi",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Dostosuj uchwyt wywołujący panel rysowania",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 1. Interactive Phone Screen Preview
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Podgląd w czasie rzeczywistym",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                // Phone mockup canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkBackground)
                        .border(1.5.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Top camera punch-hole notch
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(width = 36.dp, height = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DarkSurfaceBorder)
                    )

                    // Mockup desktop / app background hint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    ) {
                        Text(
                            text = "Dotknij bąbelka na krawędzi",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (previewClicked) "⚡ Wywołano panel gestów!" else "Możesz go przesuwać w górę i w dół",
                            color = if (previewClicked) NeonGreen else TextTertiary.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = if (previewClicked) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Floating bubble representation in preview
                    val isLeftEdge = edge == AppSettings.BubbleEdge.LEFT
                    val previewAlignment = if (isLeftEdge) Alignment.CenterStart else Alignment.CenterEnd

                    val bubbleScale by animateFloatAsState(
                        targetValue = if (previewClicked) 1.25f else 1.0f,
                        animationSpec = tween(durationMillis = 150),
                        label = "bubble_scale"
                    )

                    // Dimensions for preview
                    val (previewWidth, previewHeight) = when (shape) {
                        AppSettings.BubbleShape.CIRCLE -> when (size) {
                            AppSettings.BubbleSize.SMALL -> 34.dp to 34.dp
                            AppSettings.BubbleSize.MEDIUM -> 42.dp to 42.dp
                            AppSettings.BubbleSize.LARGE -> 52.dp to 52.dp
                        }
                        AppSettings.BubbleShape.TAB -> when (size) {
                            AppSettings.BubbleSize.SMALL -> 16.dp to 54.dp
                            AppSettings.BubbleSize.MEDIUM -> 22.dp to 72.dp
                            AppSettings.BubbleSize.LARGE -> 28.dp to 94.dp
                        }
                        AppSettings.BubbleShape.PILL -> when (size) {
                            AppSettings.BubbleSize.SMALL -> 16.dp to 50.dp
                            AppSettings.BubbleSize.MEDIUM -> 22.dp to 68.dp
                            AppSettings.BubbleSize.LARGE -> 28.dp to 90.dp
                        }
                    }

                    val previewShape = when (shape) {
                        AppSettings.BubbleShape.CIRCLE -> CircleShape
                        AppSettings.BubbleShape.PILL -> RoundedCornerShape(percent = 50)
                        AppSettings.BubbleShape.TAB -> {
                            if (isLeftEdge) {
                                RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                            } else {
                                RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                            }
                        }
                    }

                    val currentBubbleColor = Color(colorHex).copy(alpha = opacity)

                    Box(
                        modifier = Modifier
                            .align(previewAlignment)
                            .scale(bubbleScale)
                            .size(width = previewWidth, height = previewHeight)
                            .clip(previewShape)
                            .background(currentBubbleColor)
                            .border(1.dp, Color.White.copy(alpha = 0.8f), previewShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (haptic) {
                                    try {
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator?.vibrate(20)
                                        }
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                                previewClicked = true
                                coroutineScope.launch {
                                    delay(400)
                                    previewClicked = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (indicator) {
                            AppSettings.BubbleIndicator.DOT -> {
                                val dotSize = if (size == AppSettings.BubbleSize.SMALL) 5.dp else 6.dp
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                            AppSettings.BubbleIndicator.BOLT -> {
                                Text(
                                    text = "⚡",
                                    color = Color.White,
                                    fontSize = if (size == AppSettings.BubbleSize.SMALL) 10.sp else 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            AppSettings.BubbleIndicator.DRAW -> {
                                Text(
                                    text = "✏️",
                                    color = Color.White,
                                    fontSize = if (size == AppSettings.BubbleSize.SMALL) 9.sp else 11.sp
                                )
                            }
                            AppSettings.BubbleIndicator.NONE -> {}
                        }
                    }
                }
            }

            // 2. Krawędź ekranu (Left vs Right)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Krawędź ekranu",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val edgeOptions = listOf(
                        AppSettings.BubbleEdge.LEFT to "⬅️ Lewa krawędź",
                        AppSettings.BubbleEdge.RIGHT to "➡️ Prawa krawędź"
                    )

                    edgeOptions.forEach { (optionEdge, label) ->
                        val isSelected = edge == optionEdge
                        Button(
                            onClick = {
                                edge = optionEdge
                                AppSettings.setBubbleEdge(context, optionEdge)
                                notifySettingsChanged()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else DarkSurfaceVariant,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 3. Kształt bąbelka (Shape)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kształt bąbelka",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shapeOptions = listOf(
                        AppSettings.BubbleShape.PILL to "💊 Pigułka",
                        AppSettings.BubbleShape.CIRCLE to "⚪ Okrąg",
                        AppSettings.BubbleShape.TAB to "📑 Uchwyt"
                    )

                    shapeOptions.forEach { (optionShape, label) ->
                        val isSelected = shape == optionShape
                        Button(
                            onClick = {
                                shape = optionShape
                                AppSettings.setBubbleShape(context, optionShape)
                                notifySettingsChanged()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else DarkSurfaceVariant,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 4. Rozmiar bąbelka (Size)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rozmiar",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizeOptions = listOf(
                        AppSettings.BubbleSize.SMALL to "Mały\n(18dp)",
                        AppSettings.BubbleSize.MEDIUM to "Średni\n(24dp)",
                        AppSettings.BubbleSize.LARGE to "Duży\n(32dp)"
                    )

                    sizeOptions.forEach { (optionSize, label) ->
                        val isSelected = size == optionSize
                        Button(
                            onClick = {
                                size = optionSize
                                AppSettings.setBubbleSize(context, optionSize)
                                notifySettingsChanged()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else DarkSurfaceVariant,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 5. Kolor neonowy (Neon Color Palette)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kolor neonowy",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    val selectedColorName = BUBBLE_COLOR_OPTIONS.firstOrNull { it.hexColor == colorHex }?.name ?: "Własny"
                    Text(
                        text = selectedColorName,
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BUBBLE_COLOR_OPTIONS.forEach { colorOpt ->
                        val isSelected = colorHex == colorOpt.hexColor
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colorOpt.composeColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    colorHex = colorOpt.hexColor
                                    AppSettings.setBubbleColor(context, colorOpt.hexColor)
                                    notifySettingsChanged()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Wybrany",
                                    tint = if (colorOpt.composeColor == Color.White) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Przezroczystość / Krycie (Opacity)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Przezroczystość / Krycie",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opacityOptions = listOf(
                        0.40f to "40%\nDyskretny",
                        0.65f to "65%\nSubtelny",
                        0.85f to "85%\nZalecany",
                        1.00f to "100%\nPełny"
                    )

                    opacityOptions.forEach { (value, label) ->
                        val isSelected = kotlin.math.abs(opacity - value) < 0.05f
                        Button(
                            onClick = {
                                opacity = value
                                AppSettings.setBubbleOpacity(context, value)
                                notifySettingsChanged()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else DarkSurfaceVariant,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 7. Wskaźnik wewnętrzny (Indicator)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Symbol wewnątrz bąbelka",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val indicatorOptions = listOf(
                        AppSettings.BubbleIndicator.DOT to "• Kropka",
                        AppSettings.BubbleIndicator.BOLT to "⚡ Piorun",
                        AppSettings.BubbleIndicator.DRAW to "✏️ Ołówek",
                        AppSettings.BubbleIndicator.NONE to "Brak"
                    )

                    indicatorOptions.forEach { (optionInd, label) ->
                        val isSelected = indicator == optionInd
                        Button(
                            onClick = {
                                indicator = optionInd
                                AppSettings.setBubbleIndicator(context, optionInd)
                                notifySettingsChanged()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else DarkSurfaceVariant,
                                contentColor = if (isSelected) Color.Black else TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 8. Wibracja przy dotknięciu (Haptic Feedback)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = if (haptic) NeonCyan else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Wibracja przy dotknięciu",
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Krótkie haptyczne potwierdzenie naciśnięcia bąbelka",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = haptic,
                    onCheckedChange = { checked ->
                        haptic = checked
                        AppSettings.setBubbleHapticEnabled(context, checked)
                        notifySettingsChanged()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            // 9. Quick Service Status & Activation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isServiceRunning) NeonGreen.copy(alpha = 0.12f)
                        else NeonYellow.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (isServiceRunning) NeonGreen.copy(alpha = 0.4f)
                        else NeonYellow.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isServiceRunning) "Bąbelek aktywny na ekranie" else "Bąbelek wyłączony",
                        color = if (isServiceRunning) NeonGreen else NeonYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isServiceRunning) "Bąbelek jest teraz widoczny na krawędzi i natychmiast reaguje na powyższe zmiany."
                        else "Włącz usługę, aby bąbelek pojawił się nad wszystkimi aplikacjami.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = { enable ->
                        if (enable && !hasOverlayPermission) {
                            onRequestOverlayPermission()
                        } else {
                            onToggleService(enable)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            // 10. Reset bubble to defaults button
            OutlinedButton(
                onClick = {
                    edge = AppSettings.BubbleEdge.RIGHT
                    shape = AppSettings.BubbleShape.PILL
                    size = AppSettings.BubbleSize.MEDIUM
                    colorHex = AppSettings.DEFAULT_BUBBLE_COLOR
                    opacity = AppSettings.DEFAULT_BUBBLE_OPACITY
                    indicator = AppSettings.BubbleIndicator.DOT
                    haptic = true

                    AppSettings.setBubbleEdge(context, AppSettings.BubbleEdge.RIGHT)
                    AppSettings.setBubbleShape(context, AppSettings.BubbleShape.PILL)
                    AppSettings.setBubbleSize(context, AppSettings.BubbleSize.MEDIUM)
                    AppSettings.setBubbleColor(context, AppSettings.DEFAULT_BUBBLE_COLOR)
                    AppSettings.setBubbleOpacity(context, AppSettings.DEFAULT_BUBBLE_OPACITY)
                    AppSettings.setBubbleIndicator(context, AppSettings.BubbleIndicator.DOT)
                    AppSettings.setBubbleHapticEnabled(context, true)

                    notifySettingsChanged()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Przywróć domyślny wygląd bąbelka",
                    fontSize = 12.sp
                )
            }
        }
    }
}
