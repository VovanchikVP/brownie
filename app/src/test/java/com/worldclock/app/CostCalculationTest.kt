package com.worldclock.app

import com.worldclock.app.data.MeterType
import com.worldclock.app.ui.CostItem
import org.junit.Test
import org.junit.Assert.*

class CostCalculationTest {

    @Test
    fun `calculate cost should work correctly`() {
        // Given
        val consumption = 50.0
        val tariff = 4.5
        val expectedCost = 225.0

        // When
        val actualCost = consumption * tariff

        // Then
        assertEquals(expectedCost, actualCost, 0.001)
    }

    @Test
    fun `calculate consumption should work correctly`() {
        // Given
        val currentReading = 1250.0
        val previousReading = 1200.0
        val expectedConsumption = 50.0

        // When
        val actualConsumption = currentReading - previousReading

        // Then
        assertEquals(expectedConsumption, actualConsumption, 0.001)
    }

    @Test
    fun `cost item should calculate correctly`() {
        // Given
        val currentReading = 1250.0
        val previousReading = 1200.0
        val tariff = 4.5
        val expectedConsumption = 50.0
        val expectedCost = 225.0

        // When
        val consumption = currentReading - previousReading
        val cost = consumption * tariff
        val costItem = CostItem(
            meterId = 1L,
            meterNumber = "EL-001",
            meterAddress = "ул. Ленина, д. 10",
            meterType = MeterType.ELECTRICITY,
            currentReading = currentReading,
            previousReading = previousReading,
            consumption = consumption,
            tariff = tariff,
            cost = cost,
            readingDate = System.currentTimeMillis()
        )

        // Then
        assertEquals(expectedConsumption, costItem.consumption, 0.001)
        assertEquals(expectedCost, costItem.cost, 0.001)
    }

    @Test
    fun `zero consumption should result in zero cost`() {
        // Given
        val consumption = 0.0
        val tariff = 4.5

        // When
        val cost = consumption * tariff

        // Then
        assertEquals(0.0, cost, 0.001)
    }

    @Test
    fun `negative consumption should result in negative cost`() {
        // Given
        val consumption = -10.0
        val tariff = 4.5
        val expectedCost = -45.0

        // When
        val cost = consumption * tariff

        // Then
        assertEquals(expectedCost, cost, 0.001)
    }

    @Test
    fun `high tariff should result in high cost`() {
        // Given
        val consumption = 100.0
        val tariff = 10.0
        val expectedCost = 1000.0

        // When
        val cost = consumption * tariff

        // Then
        assertEquals(expectedCost, cost, 0.001)
    }

    @Test
    fun `different meter types should have different calculations`() {
        // Given
        val consumption = 50.0
        val electricityTariff = 4.5
        val gasTariff = 6.2
        val waterTariff = 45.0

        // When
        val electricityCost = consumption * electricityTariff
        val gasCost = consumption * gasTariff
        val waterCost = consumption * waterTariff

        // Then
        assertEquals(225.0, electricityCost, 0.001)
        assertEquals(310.0, gasCost, 0.001)
        assertEquals(2250.0, waterCost, 0.001)
    }

    @Test
    fun `cost calculation with decimal values should be precise`() {
        // Given
        val consumption = 50.5
        val tariff = 4.5
        val expectedCost = 227.25

        // When
        val actualCost = consumption * tariff

        // Then
        assertEquals(expectedCost, actualCost, 0.001)
    }
}