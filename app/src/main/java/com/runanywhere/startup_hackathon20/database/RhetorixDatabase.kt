package com.runanywhere.startup_hackathon20.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, DebateHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RhetorixDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun debateHistoryDao(): DebateHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RhetorixDatabase? = null

        fun getDatabase(context: Context): RhetorixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhetorixDatabase::class.java,
                    "rhetorix_database"
                )
                    // Removed .fallbackToDestructiveMigration() to preserve data
                    // Data will persist even when device disconnects
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
