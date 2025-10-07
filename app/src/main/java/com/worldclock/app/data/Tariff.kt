package com.worldclock.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tariffs",
    foreignKeys = [
        ForeignKey(
            entity = Meter::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Tariff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,           // Связь с прибором учета
    val rate: Double,            // Значение тарифа (руб/ед.изм.)
    val startDate: Long,         // Дата начала действия (timestamp)
    val endDate: Long?           // Дата окончания действия (timestamp, null если действует)
)