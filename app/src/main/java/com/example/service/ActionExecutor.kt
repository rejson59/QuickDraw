package com.example.service

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.KeyEvent
import android.widget.Toast
import com.example.model.ActionType
import com.example.model.SystemAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ActionExecutor {

    private var isFlashlightActive = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executorScope = CoroutineScope(Dispatchers.Main)

    fun execute(context: Context, actionType: ActionType, target: String, label: String) {
        when (actionType) {
            ActionType.APP -> launchApp(context, target, label)
            ActionType.SYSTEM -> executeSystemAction(context, target, label)
            ActionType.WEB_URL -> openWebUrl(context, target, label)
            ActionType.FLOW -> executeFlowSequence(context, target, label)
        }
    }

    private fun launchApp(context: Context, packageName: String, label: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                showFeedback(context, "Uruchamianie: $label")
            } else {
                showFeedback(context, "Nie znaleziono aplikacji: $label")
            }
        } catch (e: Exception) {
            showFeedback(context, "Błąd uruchamiania $label: ${e.localizedMessage}")
        }
    }

    private fun executeSystemAction(context: Context, code: String, label: String) {
        val action = SystemAction.fromCode(code)
        when (action) {
            // Kontrola i ekran
            SystemAction.FLASHLIGHT_TOGGLE -> toggleFlashlight(context)
            SystemAction.HOME_SCREEN -> goToHomeScreen(context)
            SystemAction.NOTIFICATION_SHADE -> expandNotificationShade(context)
            SystemAction.QUICK_SETTINGS -> openQuickSettings(context)
            SystemAction.OPEN_CAMERA -> openCamera(context)
            SystemAction.OPEN_VIDEO_CAMERA -> openVideoCamera(context)

            // Dźwięk i multimedia
            SystemAction.VOLUME_DIALOG -> showVolumeDialog(context)
            SystemAction.VOLUME_UP -> adjustVolume(context, AudioManager.ADJUST_RAISE, "Głośniej")
            SystemAction.VOLUME_DOWN -> adjustVolume(context, AudioManager.ADJUST_LOWER, "Ciszej")
            SystemAction.VOLUME_MUTE -> muteVolume(context)
            SystemAction.TOGGLE_RINGER -> toggleRingerMode(context)
            SystemAction.MEDIA_PLAY_PAUSE -> sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "Play / Pauza")
            SystemAction.MEDIA_NEXT -> sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT, "Następny utwór")
            SystemAction.MEDIA_PREVIOUS -> sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS, "Poprzedni utwór")

            // Narzędzia
            SystemAction.OPEN_DIALER -> openDialer(context)
            SystemAction.OPEN_CALCULATOR -> openCalculator(context)
            SystemAction.OPEN_ALARM_CLOCK -> openAlarmClock(context)
            SystemAction.OPEN_CALENDAR -> openCalendar(context)
            SystemAction.VOICE_SEARCH -> openVoiceSearch(context)

            // Ustawienia systemu
            SystemAction.OPEN_SETTINGS -> openSettings(context, Settings.ACTION_SETTINGS, "Ustawienia główne")
            SystemAction.OPEN_WIFI_SETTINGS -> openSettings(context, Settings.ACTION_WIFI_SETTINGS, "Ustawienia Wi-Fi")
            SystemAction.OPEN_BLUETOOTH_SETTINGS -> openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS, "Ustawienia Bluetooth")
            SystemAction.OPEN_DISPLAY_SETTINGS -> openSettings(context, Settings.ACTION_DISPLAY_SETTINGS, "Ekran i jasność")
            SystemAction.OPEN_SOUND_SETTINGS -> openSettings(context, Settings.ACTION_SOUND_SETTINGS, "Ustawienia dźwięku")
            SystemAction.OPEN_BATTERY_SETTINGS -> openSettings(context, Intent.ACTION_POWER_USAGE_SUMMARY, "Bateria")
            SystemAction.OPEN_APPLICATION_SETTINGS -> openSettings(context, Settings.ACTION_APPLICATION_SETTINGS, "Aplikacje")
            SystemAction.OPEN_ACCESSIBILITY_SETTINGS -> openSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS, "Dostępność")
            SystemAction.OPEN_LOCATION_SETTINGS -> openSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Lokalizacja GPS")
            SystemAction.OPEN_AIRPLANE_MODE -> openSettings(context, Settings.ACTION_AIRPLANE_MODE_SETTINGS, "Tryb samolotowy")
            SystemAction.OPEN_STORAGE_SETTINGS -> openSettings(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "Pamięć")
            SystemAction.OPEN_SECURITY_SETTINGS -> openSettings(context, Settings.ACTION_SECURITY_SETTINGS, "Bezpieczeństwo")
            SystemAction.OPEN_PRIVACY_SETTINGS -> openSettings(context, Settings.ACTION_PRIVACY_SETTINGS, "Prywatność")
            SystemAction.OPEN_DATE_SETTINGS -> openSettings(context, Settings.ACTION_DATE_SETTINGS, "Data i godzina")
            SystemAction.OPEN_DEVICE_INFO -> openSettings(context, Settings.ACTION_DEVICE_INFO_SETTINGS, "O telefonie")
        }
    }

    private fun toggleFlashlight(context: Context) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId != null) {
                isFlashlightActive = !isFlashlightActive
                cameraManager.setTorchMode(cameraId, isFlashlightActive)
                showFeedback(context, if (isFlashlightActive) "Latarka włączona" else "Latarka wyłączona")
            } else {
                showFeedback(context, "Urządzenie nie posiada latarki")
            }
        } catch (e: Exception) {
            showFeedback(context, "Latarka: niedostępna lub zajęta")
        }
    }

    private fun goToHomeScreen(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Ekran główny")
        } catch (e: Exception) {
            showFeedback(context, "Błąd przejścia do ekranu głównego")
        }
    }

    private fun showVolumeDialog(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustSuggestedStreamVolume(
                AudioManager.ADJUST_SAME,
                AudioManager.USE_DEFAULT_STREAM_TYPE,
                AudioManager.FLAG_SHOW_UI
            )
            showFeedback(context, "Panel głośności")
        } catch (e: Exception) {
            showFeedback(context, "Błąd regulacji głośności")
        }
    }

    private fun toggleRingerMode(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentMode = audioManager.ringerMode
            val newMode = when (currentMode) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            val modeName = when (newMode) {
                AudioManager.RINGER_MODE_NORMAL -> "Dzwonek normalny"
                AudioManager.RINGER_MODE_VIBRATE -> "Tylko wibracje"
                else -> "Tryb cichy"
            }
            try {
                audioManager.ringerMode = newMode
                showFeedback(context, "Dźwięk: $modeName")
            } catch (secEx: SecurityException) {
                // Device requires DND access policy, show volume controls
                audioManager.adjustSuggestedStreamVolume(
                    AudioManager.ADJUST_SAME,
                    AudioManager.USE_DEFAULT_STREAM_TYPE,
                    AudioManager.FLAG_SHOW_UI
                )
                showFeedback(context, "Dźwięk: Panel głośności")
            }
        } catch (e: Exception) {
            showFeedback(context, "Błąd zmiany trybu dźwięku")
        }
    }

    private fun adjustVolume(context: Context, direction: Int, label: String) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustSuggestedStreamVolume(
                direction,
                AudioManager.USE_DEFAULT_STREAM_TYPE,
                AudioManager.FLAG_SHOW_UI
            )
            showFeedback(context, label)
        } catch (e: Exception) {
            showFeedback(context, "Błąd regulacji głośności")
        }
    }

    private fun muteVolume(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustSuggestedStreamVolume(
                AudioManager.ADJUST_MUTE,
                AudioManager.USE_DEFAULT_STREAM_TYPE,
                AudioManager.FLAG_SHOW_UI
            )
            showFeedback(context, "Wyciszono multimedia")
        } catch (e: Exception) {
            showFeedback(context, "Błąd wyciszania")
        }
    }

    private fun sendMediaKeyEvent(context: Context, keyCode: Int, label: String) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(eventDown)
            audioManager.dispatchMediaKeyEvent(eventUp)
            showFeedback(context, label)
        } catch (e: Exception) {
            showFeedback(context, "Błąd sterowania mediami")
        }
    }

    private fun openCamera(context: Context) {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Aparat uruchomiony")
        } catch (e: Exception) {
            showFeedback(context, "Nie można otworzyć aparatu")
        }
    }

    private fun openVideoCamera(context: Context) {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Kamera wideo")
        } catch (e: Exception) {
            showFeedback(context, "Nie można otworzyć kamery wideo")
        }
    }

    private fun openDialer(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Telefon")
        } catch (e: Exception) {
            showFeedback(context, "Nie można otworzyć telefonu")
        }
    }

    private fun openCalculator(context: Context) {
        try {
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Kalkulator")
        } catch (e: Exception) {
            val calcPackages = listOf("com.google.android.calculator", "com.android.calculator2", "com.sec.android.app.popupcalculator")
            var opened = false
            for (pkg in calcPackages) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    opened = true
                    showFeedback(context, "Kalkulator")
                    break
                }
            }
            if (!opened) showFeedback(context, "Nie znaleziono kalkulatora")
        }
    }

    private fun openAlarmClock(context: Context) {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Zegar i budzik")
        } catch (e: Exception) {
            showFeedback(context, "Nie znaleziono zegara")
        }
    }

    private fun openCalendar(context: Context) {
        try {
            val builder = CalendarContract.CONTENT_URI.buildUpon()
            builder.appendPath("time")
            ContentUris.appendId(builder, System.currentTimeMillis())
            val intent = Intent(Intent.ACTION_VIEW).setData(builder.build()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Kalendarz")
        } catch (e: Exception) {
            showFeedback(context, "Nie znaleziono kalendarza")
        }
    }

    private fun openVoiceSearch(context: Context) {
        try {
            val intent = Intent(RecognizerIntent.ACTION_WEB_SEARCH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, "Wyszukiwanie głosowe")
        } catch (e: Exception) {
            showFeedback(context, "Wyszukiwanie niedostępne")
        }
    }

    private fun openSettings(context: Context, action: String, label: String) {
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showFeedback(context, label)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
                showFeedback(context, "Ustawienia")
            } catch (err: Exception) {
                showFeedback(context, "Nie można otworzyć ustawień")
            }
        }
    }

    private fun openQuickSettings(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandSettingsPanel")
            method.invoke(statusBarService)
            showFeedback(context, "Szybkie ustawienia")
        } catch (e: Exception) {
            openSettings(context, Settings.ACTION_SETTINGS, "Szybkie ustawienia")
        }
    }

    private fun expandNotificationShade(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(statusBarService)
        } catch (e: Exception) {
            showFeedback(context, "Powiadomienia")
        }
    }

    private fun openWebUrl(context: Context, urlInput: String, label: String) {
        try {
            var url = urlInput.trim()
            if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                url = "https://$url"
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
            showFeedback(context, "Otwieranie: ${label.ifBlank { url }}")
        } catch (e: Exception) {
            showFeedback(context, "Błąd otwierania strony: $urlInput")
        }
    }

    private fun executeFlowSequence(context: Context, flowPayload: String, label: String) {
        executorScope.launch {
            showFeedback(context, "Rozpoczynam sekwencję: $label")
            // Flow payload format: "TYPE|TARGET|LABEL;;TYPE|TARGET|LABEL"
            val steps = flowPayload.split(";;").filter { it.isNotBlank() }
            for (step in steps) {
                val parts = step.split("|")
                if (parts.size >= 3) {
                    val typeStr = parts[0]
                    val target = parts[1]
                    val stepLabel = parts[2]
                    val actionType = try {
                        ActionType.valueOf(typeStr)
                    } catch (e: Exception) {
                        ActionType.SYSTEM
                    }
                    execute(context, actionType, target, stepLabel)
                    delay(400L) // Wait between sequence steps
                }
            }
        }
    }

    private fun showFeedback(context: Context, message: String) {
        mainHandler.post {
            Toast.makeText(context.applicationContext, "⚡ QuickDraw: $message", Toast.LENGTH_SHORT).show()
        }
    }
}
