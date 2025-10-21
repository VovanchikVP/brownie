package com.worldclock.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = Meter::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["meterId"])]
)
data class Reading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,           // Связь с прибором учета
    val value: Double,           // Значение показания
    val date: Long              // Дата показания (timestamp)
)