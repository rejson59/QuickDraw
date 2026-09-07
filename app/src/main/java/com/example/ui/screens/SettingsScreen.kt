package com.example.ui.screens

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AppSettings
import com.example.ui.components.BubbleCustomizerCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.RainbowColors
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isCritical: Boolean,
    val onGrantClicked: () -> Unit
)

@Composable
fun SettingsScreen(
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit,
    gestureCount: Int,
    onRestoreDefaults: () -> Unit,
    onOpenTutorial: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var strokeDelayMs by remember { mutableStateOf(AppSettings.getStrokeDelayMs(context)) }

    // Launcher for runtime CAMERA permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshKey++
    }

    // Launcher for runtime NOTIFICATIONS permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshKey++
    }

    // Determine current permissions status
    val hasOverlayPermission = Settings.canDrawOverlays(context)

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    val hasDndPermission = notificationManager?.isNotificationPolicyAccessGranted ?: false

    val permissionsList = listOf(
        PermissionItem(
            id = "overlay",
            title = "Wyświetlanie nad innymi aplikacjami",
            description = "Kluczowe: umożliwia wyświetlanie pływającego uchwytu i panelu rysowania gestów w dowolnym miejscu.",
            icon = Icons.Default.Layers,
            isGranted = hasOverlayPermission,
            isCritical = true,
            onGrantClicked = {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                }
            }
        ),
        PermissionItem(
            id = "notifications",
            title = "Powiadomienia i usługa w tle",
            description = "Wymagane, aby usługa QuickDraw nie była wyłączana przez system podczas przełączania aplikacji.",
            icon = Icons.Default.Notifications,
            isGranted = hasNotificationPermission,
            isCritical = true,
            onGrantClicked = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            }
        ),
        PermissionItem(
            id = "camera",
            title = "Aparat i latarka",
            description = "Pozwala na błyskawiczne włączanie latarki oraz otwieranie aparatu przypisanymi gestami.",
            icon = Icons.Default.CameraAlt,
            isGranted = hasCameraPermission,
            isCritical = false,
            onGrantClicked = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        ),
        PermissionItem(
            id = "battery",
            title = "Optymalizacja baterii",
            description = "Zalecane: wyłączenie ograniczeń oszczędzania energii chroni pływający aktywator przed uśpieniem.",
            icon = Icons.Default.BatteryChargingFull,
            isGranted = isIgnoringBatteryOptimizations,
            isCritical = false,
            onGrantClicked = {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            }
        ),
        PermissionItem(
            id = "dnd",
            title = "Dostęp do trybu Nie przeszkadzać",
            description = "Opcjonalne: umożliwia przełączanie dzwonka i trybu cichego za pomocą gestu.",
            icon = Icons.Default.DoNotDisturbOn,
            isGranted = hasDndPermission,
            isCritical = false,
            onGrantClicked = {
                try {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                } catch (e: Exception) {
                    // Fallback
                }
            }
        )
    )

    val allCriticalGranted = hasOverlayPermission && hasNotificationPermission
    val allGranted = permissionsList.all { it.isGranted }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ustawienia i Uprawnienia",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Centrum diagnostyki i konfiguracji QuickDraw",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { refreshKey++ }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odśwież uprawnienia",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (allGranted) NeonGreen else if (allCriticalGranted) NeonYellow else NeonPink,
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (allGranted) NeonGreen.copy(alpha = 0.2f)
                                else if (allCriticalGranted) NeonYellow.copy(alpha = 0.2f)
                                else NeonPink.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (allGranted) NeonGreen else if (allCriticalGranted) NeonYellow else NeonPink,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (allGranted) "Wszystkie uprawnienia przyznane!"
                            else if (allCriticalGranted) "Podstawowe uprawnienia aktywne"
                            else "Wymagane uprawnienie nakładki",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (allGranted) "QuickDraw działa ze 100% możliwościami systemu."
                            else if (allCriticalGranted) "Pływający aktywator działa. Możesz przyznać pozostałe uprawnienia dla dodatkowych funkcji."
                            else "Pływający aktywator wymaga uprawnienia 'Wyświetlanie nad aplikacjami'.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Section: Permissions Checklist
        item {
            Text(
                text = "Sprawdź uprawnienia systemowe",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(permissionsList.size) { index ->
            val perm = permissionsList[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (perm.isGranted) DarkSurfaceBorder else NeonPink.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = perm.icon,
                                contentDescription = null,
                                tint = if (perm.isGranted) NeonCyan else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = perm.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                if (perm.isCritical) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonOrange.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("Ważne", color = NeonOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (perm.isGranted) NeonGreen else NeonPink)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (perm.isGranted) "Przyznane" else "Brak uprawnienia",
                                    color = if (perm.isGranted) NeonGreen else NeonPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (!perm.isGranted) {
                            Button(
                                onClick = perm.onGrantClicked,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (perm.isCritical) NeonCyan else DarkSurfaceVariant,
                                    contentColor = if (perm.isCritical) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Przyznaj", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "OK",
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = perm.description,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Section: Floating Bubble Customization
        item {
            Text(
                text = "Pływający bąbelek na krawędzi",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            BubbleCustomizerCard(
                isServiceRunning = isServiceRunning,
                hasOverlayPermission = hasOverlayPermission,
                onToggleService = onToggleService,
                onRequestOverlayPermission = onRequestOverlayPermission
            )
        }

        // Section: Drawing Style
        item {
            Text(
                text = "Wygląd i Styl",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Styl rysowania gestów",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Gesty są rysowane w pełnym spektrum tęczowego neonu za każdym razem.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    // Rainbow preview bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(RainbowColors)
                            )
                    )
                }
            }
        }

        // Section: Multi-stroke & Delay Configuration
        item {
            Text(
                text = "Rysowanie wieloetapowe i odrywanie ręki",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Opóźnienie po oderwaniu palca",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Pozwala oderwać palec od ekranu, aby napisać litery wielokreskowe (np. T, X, E, +). System czeka określony czas przed zakończeniem gestu.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val delayOptions = listOf(
                            450L to "450ms\nSzybkie",
                            850L to "850ms\nZalecane",
                            1300L to "1.3s\nSpokojne",
                            1800L to "1.8s\nDługie"
                        )

                        delayOptions.forEach { (delayValue, label) ->
                            val isSelected = strokeDelayMs == delayValue
                            Button(
                                onClick = {
                                    strokeDelayMs = delayValue
                                    AppSettings.setStrokeDelayMs(context, delayValue)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Help & Tutorial
        item {
            Text(
                text = "Pomoc i samouczek",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Samouczek dla początkujących",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Zobacz ponownie przewodnik po rysowaniu liter wielokreskowych, bąbelku i uprawnieniach.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = onOpenTutorial,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan.copy(alpha = 0.2f),
                                contentColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Uruchom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Database & Reset
        item {
            Text(
                text = "Baza danych i gesty",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Zapisane gesty w bazie",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Liczba aktywnych wzorców: $gestureCount",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { showRestoreDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceVariant,
                                contentColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Przywróć domyślne", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Przywrócić gesty domyślne?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Doda to zestaw zalecanych gestów początkowych (Aparat, Latarka, Pulpit, Google) do Twojej listy.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDefaults()
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("Przywróć", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Anuluj", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
