package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ActionType
import com.example.ui.QuickDrawMainViewModel
import com.example.ui.screens.AppsTabScreen
import com.example.ui.screens.CreateGestureDialog
import com.example.ui.screens.FlowsTabScreen
import com.example.ui.screens.GesturesTabScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SystemActionsTabScreen
import com.example.ui.screens.TestGesturePadDialog
import com.example.ui.screens.TutorialDialog
import com.example.ui.screens.WebUrlsTabScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.QuickDrawTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: QuickDrawMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuickDrawTheme {
                QuickDrawMainApp(
                    viewModel = viewModel,
                    onRequestOverlayPermission = { requestOverlayPermission() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermission(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: ImageVector,
    val activeColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDrawMainApp(
    viewModel: QuickDrawMainViewModel,
    onRequestOverlayPermission: () -> Unit
) {
    val context = LocalContext.current
    val gestures by viewModel.gestures.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val createDialogState by viewModel.createGestureState.collectAsStateWithLifecycle()
    val isTestPadOpen by viewModel.isTestPadOpen.collectAsStateWithLifecycle()
    val isTutorialOpen by viewModel.isTutorialOpen.collectAsStateWithLifecycle()

    // 5 primary tabs in bottom bar - Settings is accessed via the top-right button
    val tabs = listOf(
        NavigationTabItem("Gesty", Icons.Default.Draw, NeonCyan),
        NavigationTabItem("Aplikacje", Icons.Default.Apps, NeonCyan),
        NavigationTabItem("System", Icons.Default.Bolt, NeonGreen),
        NavigationTabItem("WWW", Icons.Default.Language, NeonViolet),
        NavigationTabItem("Flows", Icons.Default.Timeline, NeonOrange)
    )

    // Handle back press when in Settings screen
    BackHandler(enabled = selectedTab == 5) {
        viewModel.setSelectedTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    if (selectedTab == 5) {
                        IconButton(onClick = { viewModel.setSelectedTab(0) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Powrót do gestów",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = if (selectedTab == 5) "⚙️ Ustawienia" else "⚡ QuickDraw",
                        color = if (selectedTab == 5) NeonYellow else NeonCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    // Tutorial for new users
                    IconButton(onClick = { viewModel.openTutorial() }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Samouczek dla nowych użytkowników",
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // In-app Test Pad button
                    IconButton(onClick = { viewModel.openTestPad() }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = "Testuj gesty",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Settings button
                    IconButton(onClick = {
                        if (selectedTab == 5) {
                            viewModel.setSelectedTab(0)
                        } else {
                            viewModel.setSelectedTab(5)
                        }
                    }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedTab == 5) NeonYellow.copy(alpha = 0.35f)
                                    else NeonYellow.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (selectedTab == 5) NeonYellow else NeonYellow.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ustawienia i uprawnienia",
                                tint = NeonYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) item.activeColor else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) item.activeColor else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = item.activeColor.copy(alpha = 0.18f),
                            selectedIconColor = item.activeColor,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = item.activeColor,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> GesturesTabScreen(
                    gestures = gestures,
                    isServiceRunning = isServiceRunning,
                    hasOverlayPermission = hasOverlayPermission,
                    onToggleService = { enable -> viewModel.toggleService(context, enable) },
                    onRequestPermission = onRequestOverlayPermission,
                    onOpenTestPad = { viewModel.openTestPad() },
                    onToggleGestureEnabled = { id, enabled -> viewModel.toggleGestureEnabled(id, enabled) },
                    onUpdateSensitivity = { id, sensitivity -> viewModel.updateGestureSensitivity(id, sensitivity) },
                    onDeleteGesture = { gesture -> viewModel.deleteGesture(gesture) },
                    onAddNewGesture = { viewModel.openCreateGesture() },
                    onRestoreDefaults = { viewModel.restoreDefaults(context) },
                    onOpenTutorial = { viewModel.openTutorial() }
                )
                1 -> AppsTabScreen(
                    onAssignGesture = { actionType, target, label ->
                        viewModel.openCreateGesture(actionType, target, label, "Uruchom $label")
                    }
                )
                2 -> SystemActionsTabScreen(
                    onAssignGesture = { actionType, target, label ->
                        viewModel.openCreateGesture(actionType, target, label, label)
                    }
                )
                3 -> WebUrlsTabScreen(
                    onAssignGesture = { actionType, target, label ->
                        viewModel.openCreateGesture(actionType, target, label, "Otwórz $label")
                    }
                )
                4 -> FlowsTabScreen(
                    onAssignGesture = { actionType, target, label ->
                        viewModel.openCreateGesture(actionType, target, label, label)
                    }
                )
                5 -> SettingsScreen(
                    isServiceRunning = isServiceRunning,
                    onToggleService = { enable -> viewModel.toggleService(context, enable) },
                    gestureCount = gestures.size,
                    onRestoreDefaults = { viewModel.restoreDefaults(context) },
                    onOpenTutorial = { viewModel.openTutorial() },
                    onRequestOverlayPermission = onRequestOverlayPermission
                )
            }
        }

        // Create Gesture Dialog
        if (createDialogState.isOpen) {
            CreateGestureDialog(
                initialActionType = createDialogState.actionType,
                initialTarget = createDialogState.target,
                initialLabel = createDialogState.label,
                initialName = createDialogState.name,
                onDismiss = { viewModel.closeCreateGesture() },
                onSaveGesture = { entity -> viewModel.saveNewGesture(entity) }
            )
        }

        // In-App Test Pad Dialog
        if (isTestPadOpen) {
            TestGesturePadDialog(
                gestures = gestures.filter { it.isEnabled },
                onDismiss = { viewModel.closeTestPad() }
            )
        }

        // Onboarding Tutorial Dialog for new users
        if (isTutorialOpen) {
            TutorialDialog(
                hasOverlayPermission = hasOverlayPermission,
                isServiceRunning = isServiceRunning,
                onRequestPermission = onRequestOverlayPermission,
                onStartService = { viewModel.toggleService(context, true) },
                onOpenTestPad = { viewModel.openTestPad() },
                onDismiss = { viewModel.closeTutorial(context) }
            )
        }
    }
}
