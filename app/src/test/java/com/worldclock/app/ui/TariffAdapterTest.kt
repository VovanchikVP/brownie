package com.worldclock.app.ui

import com.worldclock.app.data.MeterType
import com.worldclock.app.data.Tariff
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TariffAdapterTest {

    @Mock
    private lateinit var mockOnDeleteClick: (Tariff) -> Unit

    @Mock
    private lateinit var mockOnItemClick: (Tariff) -> Unit

    private lateinit var adapter: TariffAdapter
    private lateinit var testTariffs: List<TariffAdapter.TariffWithMeter>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testTariffs = listOf(
            TariffAdapter.TariffWithMeter(
                Tariff(1, 1, 4.5, System.currentTimeMillis(), null),
                "12345",
                MeterType.ELECTRICITY.displayName
            ),
            TariffAdapter.TariffWithMeter(
                Tariff(2, 2, 6.0, System.currentTimeMillis(), System.currentTimeMillis() + 86400000),
                "67890",
                MeterType.GAS.displayName
            ),
            TariffAdapter.TariffWithMeter(
                Tariff(3, 3, 8.0, System.currentTimeMillis(), null),
                "11111",
                MeterType.HOT_WATER.displayName
            )
        )
        adapter = TariffAdapter(testTariffs, mockOnDeleteClick, mockOnItemClick)
    }

    @Test
    fun `getItemCount should return correct count`() {
        // When
        val count = adapter.itemCount

        // Then
        assert(count == testTariffs.size)
    }

    @Test
    fun `TariffWithMeter data class should work correctly`() {
        // Given
        val tariff = Tariff(1, 1, 4.5, System.currentTimeMillis(), null)
        val meterNumber = "12345"
        val meterType = MeterType.ELECTRICITY.displayName

        // When
        val tariffWithMeter = TariffAdapter.TariffWithMeter(tariff, meterNumber, meterType)

        // Then
        assert(tariffWithMeter.tariff == tariff)
        assert(tariffWithMeter.meterNumber == meterNumber)
        assert(tariffWithMeter.meterType == meterType)
    }
}
