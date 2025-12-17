package com.datasentry.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.datasentry.app.data.local.dao.PacketDao
import com.datasentry.app.data.local.entity.PacketEntity

@Database(entities = [PacketEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packetDao(): PacketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "datasentry_db"
                )
                .fallbackToDestructiveMigration() // Hackathon: Wipe DB on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
