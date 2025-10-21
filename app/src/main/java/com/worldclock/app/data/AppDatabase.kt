package com.worldclock.app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

@Database(
    entities = [Meter::class, Reading::class, Tariff::class],
    version = 2,
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
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем индексы для meterId колонок
                db.execSQL("CREATE INDEX IF NOT EXISTS index_readings_meterId ON readings(meterId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tariffs_meterId ON tariffs(meterId)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meters_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}