package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.GestureRepository
import com.example.data.QuickDrawDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuickDrawApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { QuickDrawDatabase.getDatabase(this) }
    val repository by lazy { GestureRepository(database.gestureDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()

        // Populate starter gestures if new install
        applicationScope.launch {
            repository.prePopulateDefaultsIfEmpty(this@QuickDrawApplication)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "QuickDraw Aktywator Gestów",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Utrzymuje pływający aktywator gestów QuickDraw w tle"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "quickdraw_overlay_service_channel"
        const val NOTIFICATION_ID = 4041

        lateinit var instance: QuickDrawApplication
            private set
    }
}
