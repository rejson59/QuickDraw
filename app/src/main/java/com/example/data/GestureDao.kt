package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.GestureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GestureDao {
    @Query("SELECT * FROM gestures ORDER BY createdAt DESC")
    fun getAllGestures(): Flow<List<GestureEntity>>

    @Query("SELECT * FROM gestures WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getEnabledGestures(): Flow<List<GestureEntity>>

    @Query("SELECT * FROM gestures WHERE isEnabled = 1")
    suspend fun getEnabledGesturesSync(): List<GestureEntity>

    @Query("SELECT * FROM gestures WHERE id = :id LIMIT 1")
    suspend fun getGestureById(id: Long): GestureEntity?

    @Query("SELECT COUNT(*) FROM gestures")
    suspend fun getGesturesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGesture(gesture: GestureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGestures(gestures: List<GestureEntity>)

    @Update
    suspend fun updateGesture(gesture: GestureEntity)

    @Delete
    suspend fun deleteGesture(gesture: GestureEntity)

    @Query("DELETE FROM gestures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE gestures SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE gestures SET sensitivity = :sensitivity WHERE id = :id")
    suspend fun updateSensitivity(id: Long, sensitivity: Float)
}
