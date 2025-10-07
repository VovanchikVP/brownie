package com.worldclock.app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [Meter::class, Reading::class, Tariff::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MeterTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun meterDao(): MeterDao
    abstract fun readingDao(): ReadingDao
    abstract fun tariffDao(): TariffDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meters_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}