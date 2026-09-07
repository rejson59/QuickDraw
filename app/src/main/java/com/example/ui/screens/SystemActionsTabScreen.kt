package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.SystemAction
import com.example.service.ActionExecutor
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SystemActionsTabScreen(
    onAssignGesture: (ActionType, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }

    val categories = listOf("Wszystkie", "Kontrola", "Dźwięk", "Narzędzia", "Ustawienia")

    fun getIconForAction(action: SystemAction): ImageVector {
        return when (action) {
            // Kontrola
            SystemAction.FLASHLIGHT_TOGGLE -> Icons.Default.FlashlightOn
            SystemAction.HOME_SCREEN -> Icons.Default.Home
            SystemAction.NOTIFICATION_SHADE -> Icons.Default.NotificationsActive
            SystemAction.QUICK_SETTINGS -> Icons.Default.Tune
            SystemAction.OPEN_CAMERA -> Icons.Default.CameraAlt
            SystemAction.OPEN_VIDEO_CAMERA -> Icons.Default.Videocam

            // Dźwięk
            SystemAction.VOLUME_DIALOG -> Icons.Default.VolumeUp
            SystemAction.VOLUME_UP -> Icons.Default.VolumeUp
            SystemAction.VOLUME_DOWN -> Icons.Default.VolumeDown
            SystemAction.VOLUME_MUTE -> Icons.Default.VolumeMute
            SystemAction.TOGGLE_RINGER -> Icons.Default.Notifications
            SystemAction.MEDIA_PLAY_PAUSE -> Icons.Default.PlayCircle
            SystemAction.MEDIA_NEXT -> Icons.Default.SkipNext
            SystemAction.MEDIA_PREVIOUS -> Icons.Default.SkipPrevious

            // Narzędzia
            SystemAction.OPEN_DIALER -> Icons.Default.Phone
            SystemAction.OPEN_CALCULATOR -> Icons.Default.Calculate
            SystemAction.OPEN_ALARM_CLOCK -> Icons.Default.Alarm
            SystemAction.OPEN_CALENDAR -> Icons.Default.CalendarMonth
            SystemAction.VOICE_SEARCH -> Icons.Default.Mic

            // Ustawienia
            SystemAction.OPEN_SETTINGS -> Icons.Default.Settings
            SystemAction.OPEN_WIFI_SETTINGS -> Icons.Default.Wifi
            SystemAction.OPEN_BLUETOOTH_SETTINGS -> Icons.Default.Bluetooth
            SystemAction.OPEN_DISPLAY_SETTINGS -> Icons.Default.Brightness6
            SystemAction.OPEN_SOUND_SETTINGS -> Icons.Default.Audiotrack
            SystemAction.OPEN_BATTERY_SETTINGS -> Icons.Default.BatteryChargingFull
            SystemAction.OPEN_APPLICATION_SETTINGS -> Icons.Default.Apps
            SystemAction.OPEN_ACCESSIBILITY_SETTINGS -> Icons.Default.Accessibility
            SystemAction.OPEN_LOCATION_SETTINGS -> Icons.Default.LocationOn
            SystemAction.OPEN_AIRPLANE_MODE -> Icons.Default.AirplanemodeActive
            SystemAction.OPEN_STORAGE_SETTINGS -> Icons.Default.Storage
            SystemAction.OPEN_SECURITY_SETTINGS -> Icons.Default.Security
            SystemAction.OPEN_PRIVACY_SETTINGS -> Icons.Default.PrivacyTip
            SystemAction.OPEN_DATE_SETTINGS -> Icons.Default.Schedule
            SystemAction.OPEN_DEVICE_INFO -> Icons.Default.Info
        }
    }

    fun getColorForCategory(cat: String): Color {
        return when (cat) {
            "Kontrola" -> NeonGreen
            "Dźwięk" -> NeonCyan
            "Narzędzia" -> NeonYellow
            "Ustawienia" -> NeonPurple
            else -> NeonGreen
        }
    }

    val filteredActions = remember(searchQuery, selectedCategory) {
        SystemAction.entries.filter { action ->
            val matchesCategory = (selectedCategory == "Wszystkie" || action.category == selectedCategory)
            val matchesQuery = searchQuery.isBlank() ||
                action.title.contains(searchQuery, ignoreCase = true) ||
                action.description.contains(searchQuery, ignoreCase = true) ||
                action.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Akcje systemowe (${SystemAction.entries.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Gesty do natychmiastowego sterowania wszystkimi funkcjami telefonu",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Szukaj akcji (np. wifi, latarka, głośność...)", fontSize = 13.sp, color = TextSecondary) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Wyczyść", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val catColor = if (cat == "Wszystkie") NeonCyan else getColorForCategory(cat)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) catColor else DarkSurfaceBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) catColor else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Action items
        if (filteredActions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nie znaleziono akcji dla zapytania \"$searchQuery\"",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredActions, key = { it.code }) { action ->
                val catColor = getColorForCategory(action.category)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp)),
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
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(catColor.copy(alpha = 0.15f))
                                .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForAction(action),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = action.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = action.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 2
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(catColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = action.category,
                                    color = catColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                ActionExecutor.execute(context, ActionType.SYSTEM, action.code, action.title)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Testuj",
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = {
                                onAssignGesture(ActionType.SYSTEM, action.code, action.title)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
