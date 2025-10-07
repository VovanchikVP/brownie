package com.worldclock.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "meters")
@TypeConverters(MeterTypeConverter::class)
data class Meter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,           // Номер прибора учета
    val address: String,          // Адрес установки
    val type: MeterType          // Тип прибора учета
)