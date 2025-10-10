package com.worldclock.app.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@RunWith(MockitoJUnitRunner::class)
class MeterRepositoryTest {

    @Mock
    private lateinit var meterDao: MeterDao

    @Mock
    private lateinit var readingDao: ReadingDao

    @Mock
    private lateinit var tariffDao: TariffDao

    private lateinit var repository: MeterRepository

    @Before
    fun setUp() {
        repository = MeterRepository(meterDao, readingDao, tariffDao)
    }

    @Test
    fun `getAllMeters should return flow from meterDao`() = runTest {
        // Given
        val expectedMeters = listOf(
            Meter(1, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY),
            Meter(2, "GAS-002", "ул. Пушкина, д. 5", MeterType.GAS)
        )
        whenever(meterDao.getAllMeters()).thenReturn(flowOf(expectedMeters))

        // When
        val result = repository.getAllMeters().first()

        // Then
        assert(result == expectedMeters)
        verify(meterDao).getAllMeters()
    }

    @Test
    fun `insertMeter should call meterDao insertMeter`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        whenever(meterDao.insertMeter(meter)).thenReturn(1L)

        // When
        val result = repository.insertMeter(meter)

        // Then
        assert(result == 1L)
        verify(meterDao).insertMeter(meter)
    }

    @Test
    fun `getLatestReadingByMeterId should return latest reading`() = runTest {
        // Given
        val meterId = 1L
        val expectedReading = Reading(1, meterId, 1000.0, System.currentTimeMillis())
        whenever(readingDao.getLatestReadingByMeterId(meterId)).thenReturn(expectedReading)

        // When
        val result = repository.getLatestReadingByMeterId(meterId)

        // Then
        assert(result == expectedReading)
        verify(readingDao).getLatestReadingByMeterId(meterId)
    }

    @Test
    fun `getCurrentTariffByMeterId should return current tariff`() = runTest {
        // Given
        val meterId = 1L
        val date = System.currentTimeMillis()
        val expectedTariff = Tariff(1, meterId, 4.5, date, null)
        whenever(tariffDao.getCurrentTariffByMeterId(meterId, date)).thenReturn(expectedTariff)

        // When
        val result = repository.getCurrentTariffByMeterId(meterId, date)

        // Then
        assert(result == expectedTariff)
        verify(tariffDao).getCurrentTariffByMeterId(meterId, date)
    }

    @Test
    fun `deleteMeter should call meterDao deleteMeter`() = runTest {
        // Given
        val meter = Meter(1, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)

        // When
        repository.deleteMeter(meter)

        // Then
        verify(meterDao).deleteMeter(meter)
    }

    @Test
    fun `insertReading should call readingDao insertReading`() = runTest {
        // Given
        val reading = Reading(0, 1L, 1000.0, System.currentTimeMillis())
        whenever(readingDao.insertReading(reading)).thenReturn(1L)

        // When
        val result = repository.insertReading(reading)

        // Then
        assert(result == 1L)
        verify(readingDao).insertReading(reading)
    }

    @Test
    fun `insertTariff should call tariffDao insertTariff`() = runTest {
        // Given
        val tariff = Tariff(0, 1L, 4.5, System.currentTimeMillis(), null)
        whenever(tariffDao.insertTariff(tariff)).thenReturn(1L)

        // When
        val result = repository.insertTariff(tariff)

        // Then
        assert(result == 1L)
        verify(tariffDao).insertTariff(tariff)
    }
}