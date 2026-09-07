package com.example.data

import android.content.Context
import com.example.model.ActionType
import com.example.model.GestureEntity
import com.example.model.GesturePoint
import com.example.model.GesturePointConverter
import com.example.model.SystemAction
import kotlinx.coroutines.flow.Flow
import kotlin.math.cos
import kotlin.math.sin

class GestureRepository(private val gestureDao: GestureDao) {

    val allGestures: Flow<List<GestureEntity>> = gestureDao.getAllGestures()
    val enabledGestures: Flow<List<GestureEntity>> = gestureDao.getEnabledGestures()

    suspend fun getEnabledGesturesSync(): List<GestureEntity> {
        return gestureDao.getEnabledGesturesSync()
    }

    suspend fun getGestureById(id: Long): GestureEntity? {
        return gestureDao.getGestureById(id)
    }

    suspend fun insertGesture(gesture: GestureEntity): Long {
        return gestureDao.insertGesture(gesture)
    }

    suspend fun updateGesture(gesture: GestureEntity) {
        gestureDao.updateGesture(gesture)
    }

    suspend fun deleteGesture(gesture: GestureEntity) {
        gestureDao.deleteGesture(gesture)
    }

    suspend fun deleteById(id: Long) {
        gestureDao.deleteById(id)
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        gestureDao.updateEnabled(id, enabled)
    }

    suspend fun updateSensitivity(id: Long, sensitivity: Float) {
        gestureDao.updateSensitivity(id, sensitivity)
    }

    suspend fun prePopulateDefaultsIfEmpty(context: Context) {
        if (gestureDao.getGesturesCount() > 0) return

        val defaults = listOf(
            GestureEntity(
                name = "Włącz / Wyłącz Latarkę",
                pointsData = GesturePointConverter.serialize(generateFPoints()),
                actionType = ActionType.SYSTEM.name,
                actionTarget = SystemAction.FLASHLIGHT_TOGGLE.code,
                actionLabel = "Latarka",
                sensitivity = 0.68f,
                strokeColor = 0xFFFFD600
            ),
            GestureEntity(
                name = "Uruchom Aparat",
                pointsData = GesturePointConverter.serialize(generateCPoints()),
                actionType = ActionType.SYSTEM.name,
                actionTarget = SystemAction.OPEN_CAMERA.code,
                actionLabel = "Aparat",
                sensitivity = 0.70f,
                strokeColor = 0xFF00E5FF
            ),
            GestureEntity(
                name = "Ekran główny (Pulpit)",
                pointsData = GesturePointConverter.serialize(generateRoofPoints()),
                actionType = ActionType.SYSTEM.name,
                actionTarget = SystemAction.HOME_SCREEN.code,
                actionLabel = "Ekran główny",
                sensitivity = 0.68f,
                strokeColor = 0xFF76FF03
            ),
            GestureEntity(
                name = "Wyszukiwarka Google",
                pointsData = GesturePointConverter.serialize(generateWPoints()),
                actionType = ActionType.WEB_URL.name,
                actionTarget = "https://www.google.com",
                actionLabel = "google.com",
                sensitivity = 0.68f,
                strokeColor = 0xFF2979FF
            ),
            GestureEntity(
                name = "Panel głośności",
                pointsData = GesturePointConverter.serialize(generateVPoints()),
                actionType = ActionType.SYSTEM.name,
                actionTarget = SystemAction.VOLUME_DIALOG.code,
                actionLabel = "Regulacja głośności",
                sensitivity = 0.70f,
                strokeColor = 0xFFFF4081
            )
        )

        gestureDao.insertGestures(defaults)
    }

    companion object {
        // Generates an arc / letter 'C'
        fun generateCPoints(): List<GesturePoint> {
            val points = mutableListOf<GesturePoint>()
            for (deg in 45..315 step 10) {
                val rad = Math.toRadians(deg.toDouble())
                val x = 150f + (80f * cos(rad)).toFloat()
                val y = 150f - (80f * sin(rad)).toFloat()
                points.add(GesturePoint(x, y))
            }
            return points
        }

        // Generates an inverted 'V' / roof / Home gesture ^
        fun generateRoofPoints(): List<GesturePoint> {
            val points = mutableListOf<GesturePoint>()
            // Up from bottom-left to peak
            for (step in 0..15) {
                val t = step / 15f
                points.add(GesturePoint(50f + t * 100f, 220f - t * 140f))
            }
            // Down from peak to bottom-right
            for (step in 0..15) {
                val t = step / 15f
                points.add(GesturePoint(150f + t * 100f, 80f + t * 140f))
            }
            return points
        }

        // Generates 'W' zigzag
        fun generateWPoints(): List<GesturePoint> {
            val points = mutableListOf<GesturePoint>()
            val keyframes = listOf(
                GesturePoint(50f, 70f),
                GesturePoint(100f, 230f),
                GesturePoint(150f, 120f),
                GesturePoint(200f, 230f),
                GesturePoint(250f, 70f)
            )
            for (i in 0 until keyframes.size - 1) {
                val p0 = keyframes[i]
                val p1 = keyframes[i + 1]
                for (s in 0..8) {
                    val t = s / 8f
                    points.add(GesturePoint(p0.x + t * (p1.x - p0.x), p0.y + t * (p1.y - p0.y)))
                }
            }
            return points
        }

        // Generates 'V' checkmark
        fun generateVPoints(): List<GesturePoint> {
            val points = mutableListOf<GesturePoint>()
            for (s in 0..12) {
                val t = s / 12f
                points.add(GesturePoint(60f + t * 80f, 80f + t * 140f))
            }
            for (s in 0..12) {
                val t = s / 12f
                points.add(GesturePoint(140f + t * 100f, 220f - t * 160f))
            }
            return points
        }

        // Generates lightning / 'F' / 'L' stroke
        fun generateFPoints(): List<GesturePoint> {
            val points = mutableListOf<GesturePoint>()
            for (s in 0..10) {
                val t = s / 10f
                points.add(GesturePoint(220f - t * 140f, 70f))
            }
            for (s in 0..15) {
                val t = s / 15f
                points.add(GesturePoint(80f, 70f + t * 160f))
            }
            for (s in 0..10) {
                val t = s / 10f
                points.add(GesturePoint(80f + t * 120f, 230f))
            }
            return points
        }
    }
}
