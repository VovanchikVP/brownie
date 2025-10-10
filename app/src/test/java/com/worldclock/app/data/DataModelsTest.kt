package com.worldclock.app.data

import org.junit.Test
import org.junit.Assert.*

class DataModelsTest {

    @Test
    fun `Meter should create correctly`() {
        // Given
        val id = 1L
        val number = "EL-001-2024"
        val address = "ул. Ленина, д. 10, кв. 5"
        val type = MeterType.ELECTRICITY

        // When
        val meter = Meter(id, number, address, type)

        // Then
        assertEquals(id, meter.id)
        assertEquals(number, meter.number)
        assertEquals(address, meter.address)
        assertEquals(type, meter.type)
    }

    @Test
    fun `Reading should create correctly`() {
        // Given
        val id = 1L
        val meterId = 1L
        val value = 1250.5
        val date = System.currentTimeMillis()

        // When
        val reading = Reading(id, meterId, value, date)

        // Then
        assertEquals(id, reading.id)
        assertEquals(meterId, reading.meterId)
        assertEquals(value, reading.value, 0.001)
        assertEquals(date, reading.date)
    }

    @Test
    fun `Tariff should create correctly`() {
        // Given
        val id = 1L
        val meterId = 1L
        val rate = 4.5
        val startDate = System.currentTimeMillis()
        val endDate = System.currentTimeMillis() + 86400000L // +1 day

        // When
        val tariff = Tariff(id, meterId, rate, startDate, endDate)

        // Then
        assertEquals(id, tariff.id)
        assertEquals(meterId, tariff.meterId)
        assertEquals(rate, tariff.rate, 0.001)
        assertEquals(startDate, tariff.startDate)
        assertEquals(endDate, tariff.endDate)
    }

    @Test
    fun `Tariff without end date should create correctly`() {
        // Given
        val id = 1L
        val meterId = 1L
        val rate = 4.5
        val startDate = System.currentTimeMillis()

        // When
        val tariff = Tariff(id, meterId, rate, startDate, null)

        // Then
        assertEquals(id, tariff.id)
        assertEquals(meterId, tariff.meterId)
        assertEquals(rate, tariff.rate, 0.001)
        assertEquals(startDate, tariff.startDate)
        assertNull(tariff.endDate)
    }

    @Test
    fun `MeterType should have correct display names`() {
        // Then
        assertEquals("Электричество", MeterType.ELECTRICITY.displayName)
        assertEquals("Газ", MeterType.GAS.displayName)
        assertEquals("Горячая вода", MeterType.HOT_WATER.displayName)
        assertEquals("Холодная вода", MeterType.COLD_WATER.displayName)
    }

    @Test
    fun `MeterType should have correct values`() {
        // Then
        assertEquals(0, MeterType.ELECTRICITY.ordinal)
        assertEquals(1, MeterType.GAS.ordinal)
        assertEquals(2, MeterType.HOT_WATER.ordinal)
        assertEquals(3, MeterType.COLD_WATER.ordinal)
    }

    @Test
    fun `Meter copy should work correctly`() {
        // Given
        val originalMeter = Meter(1L, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)

        // When
        val copiedMeter = originalMeter.copy(
            number = "EL-002",
            address = "ул. Пушкина, д. 5"
        )

        // Then
        assertEquals(1L, copiedMeter.id)
        assertEquals("EL-002", copiedMeter.number)
        assertEquals("ул. Пушкина, д. 5", copiedMeter.address)
        assertEquals(MeterType.ELECTRICITY, copiedMeter.type)
    }

    @Test
    fun `Reading copy should work correctly`() {
        // Given
        val originalReading = Reading(1L, 1L, 1000.0, System.currentTimeMillis())

        // When
        val copiedReading = originalReading.copy(value = 1200.0)

        // Then
        assertEquals(1L, copiedReading.id)
        assertEquals(1L, copiedReading.meterId)
        assertEquals(1200.0, copiedReading.value, 0.001)
        assertEquals(originalReading.date, copiedReading.date)
    }

    @Test
    fun `Tariff copy should work correctly`() {
        // Given
        val originalTariff = Tariff(1L, 1L, 4.5, System.currentTimeMillis(), null)

        // When
        val copiedTariff = originalTariff.copy(rate = 5.0)

        // Then
        assertEquals(1L, copiedTariff.id)
        assertEquals(1L, copiedTariff.meterId)
        assertEquals(5.0, copiedTariff.rate, 0.001)
        assertEquals(originalTariff.startDate, copiedTariff.startDate)
        assertNull(copiedTariff.endDate)
    }
}