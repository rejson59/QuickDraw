package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gestures")
data class GestureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val pointsData: String,
    val actionType: String,
    val actionTarget: String,
    val actionLabel: String,
    val sensitivity: Float = 0.70f,
    val isEnabled: Boolean = true,
    val strokeColor: Long = 0xFF00E5FF, // Default neon cyan
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getPoints(): List<GesturePoint> {
        return GesturePointConverter.deserialize(pointsData)
    }

    fun getActionTypeEnum(): ActionType {
        return try {
            ActionType.valueOf(actionType)
        } catch (e: Exception) {
            ActionType.SYSTEM
        }
    }
}
