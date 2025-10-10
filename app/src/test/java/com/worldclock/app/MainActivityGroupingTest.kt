package com.worldclock.app

import com.worldclock.app.data.MeterType
import com.worldclock.app.ui.AddressCostItem
import com.worldclock.app.ui.CostItem
import org.junit.Test
import org.junit.Assert.*

class MainActivityGroupingTest {

    @Test
    fun `groupCostsByAddress should group costs by address`() {
        // Given
        val costItems = listOf(
            CostItem(1L, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY, 1250.0, 1200.0, 50.0, 4.5, 225.0, System.currentTimeMillis()),
            CostItem(2L, "GAS-002", "ул. Ленина, д. 10", MeterType.GAS, 850.0, 800.0, 50.0, 6.2, 310.0, System.currentTimeMillis()),
            CostItem(3L, "EL-003", "ул. Пушкина, д. 5", MeterType.ELECTRICITY, 2000.0, 1900.0, 100.0, 4.5, 450.0, System.currentTimeMillis())
        )

        // When
        val groupedCosts = groupCostsByAddress(costItems)

        // Then
        assertEquals(2, groupedCosts.size)
        
        val leningaAddress = groupedCosts.find { it.address == "ул. Ленина, д. 10" }
        assertNotNull(leningaAddress)
        assertEquals(2, leningaAddress?.metersCount)
        assertEquals(535.0, leningaAddress?.totalCost ?: 0.0, 0.001)
        
        val pushkinaAddress = groupedCosts.find { it.address == "ул. Пушкина, д. 5" }
        assertNotNull(pushkinaAddress)
        assertEquals(1, pushkinaAddress?.metersCount)
        assertEquals(450.0, pushkinaAddress?.totalCost ?: 0.0, 0.001)
    }

    @Test
    fun `groupCostsByAddress should sort by total cost descending`() {
        // Given
        val costItems = listOf(
            CostItem(1L, "EL-001", "ул. Дешевая, д. 1", MeterType.ELECTRICITY, 1000.0, 900.0, 100.0, 2.0, 200.0, System.currentTimeMillis()),
            CostItem(2L, "GAS-002", "ул. Дорогая, д. 2", MeterType.GAS, 1000.0, 900.0, 100.0, 5.0, 500.0, System.currentTimeMillis())
        )

        // When
        val groupedCosts = groupCostsByAddress(costItems)

        // Then
        assertEquals(2, groupedCosts.size)
        assertEquals("ул. Дорогая, д. 2", groupedCosts[0].address) // Highest cost first
        assertEquals("ул. Дешевая, д. 1", groupedCosts[1].address)
    }

    @Test
    fun `groupCostsByAddress should handle empty list`() {
        // Given
        val emptyList = emptyList<CostItem>()

        // When
        val groupedCosts = groupCostsByAddress(emptyList)

        // Then
        assertTrue(groupedCosts.isEmpty())
    }

    @Test
    fun `groupCostsByAddress should handle single address`() {
        // Given
        val costItems = listOf(
            CostItem(1L, "EL-001", "ул. Одна, д. 1", MeterType.ELECTRICITY, 1000.0, 900.0, 100.0, 4.5, 450.0, System.currentTimeMillis())
        )

        // When
        val groupedCosts = groupCostsByAddress(costItems)

        // Then
        assertEquals(1, groupedCosts.size)
        assertEquals("ул. Одна, д. 1", groupedCosts[0].address)
        assertEquals(1, groupedCosts[0].metersCount)
        assertEquals(450.0, groupedCosts[0].totalCost, 0.001)
    }

    @Test
    fun `groupCostsByAddress should preserve individual cost items`() {
        // Given
        val costItems = listOf(
            CostItem(1L, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY, 1250.0, 1200.0, 50.0, 4.5, 225.0, System.currentTimeMillis()),
            CostItem(2L, "GAS-002", "ул. Ленина, д. 10", MeterType.GAS, 850.0, 800.0, 50.0, 6.2, 310.0, System.currentTimeMillis())
        )

        // When
        val groupedCosts = groupCostsByAddress(costItems)

        // Then
        val leningaAddress = groupedCosts.find { it.address == "ул. Ленина, д. 10" }
        assertNotNull(leningaAddress)
        assertEquals(2, leningaAddress?.meters?.size)
        assertTrue(leningaAddress?.meters?.any { it.meterNumber == "EL-001" } == true)
        assertTrue(leningaAddress?.meters?.any { it.meterNumber == "GAS-002" } == true)
    }

    // Helper function that mimics the one in MainActivity
    private fun groupCostsByAddress(costItems: List<CostItem>): List<AddressCostItem> {
        val groupedByAddress = costItems.groupBy { it.meterAddress }
        
        return groupedByAddress.map { (address, costs) ->
            val totalCost = costs.sumOf { it.cost }
            AddressCostItem(
                address = address,
                totalCost = totalCost,
                metersCount = costs.size,
                meters = costs
            )
        }.sortedByDescending { it.totalCost }
    }
}