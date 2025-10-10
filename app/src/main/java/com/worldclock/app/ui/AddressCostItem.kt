package com.worldclock.app.ui

import com.worldclock.app.data.MeterType

data class AddressCostItem(
    val address: String,
    val totalCost: Double,
    val metersCount: Int,
    val meters: List<CostItem>
)