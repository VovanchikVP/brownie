package com.worldclock.app.ui

import com.worldclock.app.data.MeterType
import org.junit.Test
import org.junit.Assert.*

class AddressCostAdapterTest {

    @Test
    fun `AddressCostItem should create correctly`() {
        // Given
        val address = "ул. Ленина, д. 10, кв. 5"
        val totalCost = 1250.50
        val metersCount = 2
        val meters = createTestCostItems()

        // When
        val addressCostItem = AddressCostItem(address, totalCost, metersCount, meters)

        // Then
        assertEquals(address, addressCostItem.address)
        assertEquals(totalCost, addressCostItem.totalCost, 0.001)
        assertEquals(metersCount, addressCostItem.metersCount)
        assertEquals(meters, addressCostItem.meters)
    }

    @Test
    fun `AddressCostItem should handle empty meters list`() {
        // Given
        val address = "ул. Пустая, д. 1"
        val totalCost = 0.0
        val metersCount = 0
        val meters = emptyList<CostItem>()

        // When
        val addressCostItem = AddressCostItem(address, totalCost, metersCount, meters)

        // Then
        assertEquals(address, addressCostItem.address)
        assertEquals(totalCost, addressCostItem.totalCost, 0.001)
        assertEquals(metersCount, addressCostItem.metersCount)
        assertTrue(addressCostItem.meters.isEmpty())
    }

    private fun createTestCostItems(): List<CostItem> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            CostItem(
                meterId = 1L,
                meterNumber = "EL-001-2024",
                meterAddress = "ул. Ленина, д. 10, кв. 5",
                meterType = MeterType.ELECTRICITY,
                currentReading = 1250.5,
                previousReading = 1200.0,
                consumption = 50.5,
                tariff = 4.5,
                cost = 227.25,
                readingDate = currentTime
            ),
            CostItem(
                meterId = 2L,
                meterNumber = "GAS-002-2024",
                meterAddress = "ул. Ленина, д. 10, кв. 5",
                meterType = MeterType.GAS,
                currentReading = 850.2,
                previousReading = 800.0,
                consumption = 50.2,
                tariff = 6.2,
                cost = 311.24,
                readingDate = currentTime
            )
        )
    }
}