package com.worldclock.app.data

import org.junit.Test
import org.junit.Assert.*

class MeterTypeConverterTest {

    private val converter = MeterTypeConverter()

    @Test
    fun `fromMeterType should convert ELECTRICITY to string`() {
        // When
        val result = converter.fromMeterType(MeterType.ELECTRICITY)

        // Then
        assertEquals("ELECTRICITY", result)
    }

    @Test
    fun `fromMeterType should convert GAS to string`() {
        // When
        val result = converter.fromMeterType(MeterType.GAS)

        // Then
        assertEquals("GAS", result)
    }

    @Test
    fun `fromMeterType should convert HOT_WATER to string`() {
        // When
        val result = converter.fromMeterType(MeterType.HOT_WATER)

        // Then
        assertEquals("HOT_WATER", result)
    }

    @Test
    fun `fromMeterType should convert COLD_WATER to string`() {
        // When
        val result = converter.fromMeterType(MeterType.COLD_WATER)

        // Then
        assertEquals("COLD_WATER", result)
    }

    @Test
    fun `toMeterType should convert ELECTRICITY string to enum`() {
        // When
        val result = converter.toMeterType("ELECTRICITY")

        // Then
        assertEquals(MeterType.ELECTRICITY, result)
    }

    @Test
    fun `toMeterType should convert GAS string to enum`() {
        // When
        val result = converter.toMeterType("GAS")

        // Then
        assertEquals(MeterType.GAS, result)
    }

    @Test
    fun `toMeterType should convert HOT_WATER string to enum`() {
        // When
        val result = converter.toMeterType("HOT_WATER")

        // Then
        assertEquals(MeterType.HOT_WATER, result)
    }

    @Test
    fun `toMeterType should convert COLD_WATER string to enum`() {
        // When
        val result = converter.toMeterType("COLD_WATER")

        // Then
        assertEquals(MeterType.COLD_WATER, result)
    }

    @Test
    fun `conversion should be bidirectional for ELECTRICITY`() {
        // Given
        val originalType = MeterType.ELECTRICITY

        // When
        val stringValue = converter.fromMeterType(originalType)
        val convertedType = converter.toMeterType(stringValue)

        // Then
        assertEquals(originalType, convertedType)
    }

    @Test
    fun `conversion should be bidirectional for GAS`() {
        // Given
        val originalType = MeterType.GAS

        // When
        val stringValue = converter.fromMeterType(originalType)
        val convertedType = converter.toMeterType(stringValue)

        // Then
        assertEquals(originalType, convertedType)
    }

    @Test
    fun `conversion should be bidirectional for HOT_WATER`() {
        // Given
        val originalType = MeterType.HOT_WATER

        // When
        val stringValue = converter.fromMeterType(originalType)
        val convertedType = converter.toMeterType(stringValue)

        // Then
        assertEquals(originalType, convertedType)
    }

    @Test
    fun `conversion should be bidirectional for COLD_WATER`() {
        // Given
        val originalType = MeterType.COLD_WATER

        // When
        val stringValue = converter.fromMeterType(originalType)
        val convertedType = converter.toMeterType(stringValue)

        // Then
        assertEquals(originalType, convertedType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toMeterType should throw exception for invalid string`() {
        // When
        converter.toMeterType("INVALID_TYPE")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toMeterType should throw exception for empty string`() {
        // When
        converter.toMeterType("")
    }

}
