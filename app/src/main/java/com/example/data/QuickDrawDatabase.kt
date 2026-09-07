package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.GestureEntity

@Database(entities = [GestureEntity::class], version = 1, exportSchema = false)
abstract class QuickDrawDatabase : RoomDatabase() {
    abstract fun gestureDao(): GestureDao

    companion object {
        @Volatile
        private var INSTANCE: QuickDrawDatabase? = null

        fun getDatabase(context: Context): QuickDrawDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuickDrawDatabase::class.java,
                    "quickdraw_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
