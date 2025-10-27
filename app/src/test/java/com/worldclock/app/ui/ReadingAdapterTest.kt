package com.worldclock.app.ui

import com.worldclock.app.data.MeterType
import com.worldclock.app.data.Reading
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ReadingAdapterTest {

    @Mock
    private lateinit var mockOnDeleteClick: (Reading) -> Unit

    private lateinit var adapter: ReadingAdapter
    private lateinit var testReadings: List<ReadingAdapter.ReadingWithMeter>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testReadings = listOf(
            ReadingAdapter.ReadingWithMeter(
                Reading(1, 1, 100.0, System.currentTimeMillis()),
                "12345",
                MeterType.ELECTRICITY.displayName
            ),
            ReadingAdapter.ReadingWithMeter(
                Reading(2, 2, 200.0, System.currentTimeMillis()),
                "67890",
                MeterType.GAS.displayName
            ),
            ReadingAdapter.ReadingWithMeter(
                Reading(3, 3, 300.0, System.currentTimeMillis()),
                "11111",
                MeterType.HOT_WATER.displayName
            )
        )
        adapter = ReadingAdapter(testReadings, mockOnDeleteClick)
    }

    @Test
    fun `getItemCount should return correct count`() {
        // When
        val count = adapter.itemCount

        // Then
        assert(count == testReadings.size)
    }

    @Test
    fun `ReadingWithMeter data class should work correctly`() {
        // Given
        val reading = Reading(1, 1, 100.0, System.currentTimeMillis())
        val meterNumber = "12345"
        val meterType = MeterType.ELECTRICITY.displayName

        // When
        val readingWithMeter = ReadingAdapter.ReadingWithMeter(reading, meterNumber, meterType)

        // Then
        assert(readingWithMeter.reading == reading)
        assert(readingWithMeter.meterNumber == meterNumber)
        assert(readingWithMeter.meterType == meterType)
    }
}
