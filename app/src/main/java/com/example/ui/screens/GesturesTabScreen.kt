package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.GestureEntity
import com.example.service.ActionExecutor
import com.example.ui.components.FloatingActivatorBar
import com.example.ui.components.GesturePreviewThumb
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun GesturesTabScreen(
    gestures: List<GestureEntity>,
    isServiceRunning: Boolean,
    hasOverlayPermission: Boolean,
    onToggleService: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenTestPad: () -> Unit,
    onToggleGestureEnabled: (Long, Boolean) -> Unit,
    onUpdateSensitivity: (Long, Float) -> Unit,
    onDeleteGesture: (GestureEntity) -> Unit,
    onAddNewGesture: () -> Unit,
    onRestoreDefaults: () -> Unit,
    onOpenTutorial: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var gestureToDelete by remember { mutableStateOf<GestureEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Floating Activator card
            item {
                FloatingActivatorBar(
                    isServiceRunning = isServiceRunning,
                    hasOverlayPermission = hasOverlayPermission,
                    onToggleService = onToggleService,
                    onRequestPermission = onRequestPermission,
                    onOpenTestPad = onOpenTestPad
                )
            }

            // Quick Tutorial guide banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenTutorial() }
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💡", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Przewodnik dla nowych użytkowników",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Zobacz jak rysować litery i włączyć bąbelek",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Samouczek", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Zapisane gesty",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${gestures.size} skonfigurowanych gestów",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(onClick = onRestoreDefaults) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Domyślne", color = NeonCyan, fontSize = 13.sp)
                    }
                }
            }

            // Empty state or list
            if (gestures.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("✨", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Brak zapisanych gestów",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Kliknij poniższy przycisk, aby przywrócić zalecane gesty lub stwórz własny.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = onRestoreDefaults,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Przywróć gesty początkowe", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(gestures, key = { it.id }) { gesture ->
                    GestureItemCard(
                        gesture = gesture,
                        onToggleEnabled = { onToggleGestureEnabled(gesture.id, it) },
                        onUpdateSensitivity = { onUpdateSensitivity(gesture.id, it) },
                        onDelete = { gestureToDelete = gesture },
                        onTestExecute = {
                            ActionExecutor.execute(
                                context,
                                gesture.getActionTypeEnum(),
                                gesture.actionTarget,
                                gesture.actionLabel
                            )
                        }
                    )
                }
            }
        }

        // FAB to add new gesture
        FloatingActionButton(
            onClick = onAddNewGesture,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = NeonCyan,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj nowy gest")
        }
    }

    // Delete confirmation dialog
    gestureToDelete?.let { gesture ->
        AlertDialog(
            onDismissRequest = { gestureToDelete = null },
            title = { Text("Usunąć gest?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Czy na pewno chcesz usunąć gest '${gesture.name}'?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGesture(gesture)
                        gestureToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { gestureToDelete = null }) {
                    Text("Anuluj", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun GestureItemCard(
    gesture: GestureEntity,
    onToggleEnabled: (Boolean) -> Unit,
    onUpdateSensitivity: (Float) -> Unit,
    onDelete: () -> Unit,
    onTestExecute: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var localSensitivity by remember(gesture.sensitivity) { mutableFloatStateOf(gesture.sensitivity) }

    val actionType = gesture.getActionTypeEnum()
    val typeColor = when (actionType) {
        ActionType.APP -> NeonCyan
        ActionType.SYSTEM -> NeonGreen
        ActionType.WEB_URL -> NeonViolet
        ActionType.FLOW -> NeonOrange
    }
    val typeIcon = when (actionType) {
        ActionType.APP -> Icons.Default.Apps
        ActionType.SYSTEM -> Icons.Default.Settings
        ActionType.WEB_URL -> Icons.Default.Language
        ActionType.FLOW -> Icons.Default.Timeline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (gesture.isEnabled) DarkSurfaceBorder else DarkSurfaceBorder.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gesture.isEnabled) DarkSurface else DarkSurface.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniature preview of gesture stroke
                GesturePreviewThumb(
                    points = gesture.getPoints(),
                    size = 54.dp,
                    strokeColor = Color(gesture.strokeColor)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gesture.name,
                        color = if (gesture.isEnabled) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(typeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = actionType.label,
                                    color = typeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = gesture.actionLabel,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Switch(
                    checked = gesture.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer controls: Sensitivity slider & test execute
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(
                        text = if (isExpanded) "Zwiń czułość ▲" else "Czułość: ${(gesture.sensitivity * 100).roundToInt()}% ▼",
                        color = NeonCyan,
                        fontSize = 12.sp
                    )
                }

                Row {
                    IconButton(
                        onClick = onTestExecute,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Testuj akcję",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Usuń gest",
                            tint = NeonPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Próg czułości rozpoznawania:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${(localSensitivity * 100).roundToInt()}%",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Slider(
                        value = localSensitivity,
                        onValueChange = {
                            localSensitivity = it
                            onUpdateSensitivity(it)
                        },
                        valueRange = 0.50f..0.90f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = DarkSurfaceBorder
                        )
                    )
                }
            }
        }
    }
}
