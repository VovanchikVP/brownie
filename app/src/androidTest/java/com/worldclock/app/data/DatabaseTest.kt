package com.worldclock.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var meterDao: MeterDao
    private lateinit var readingDao: ReadingDao
    private lateinit var tariffDao: TariffDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        meterDao = database.meterDao()
        readingDao = database.readingDao()
        tariffDao = database.tariffDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and get meter should work correctly`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)

        // When
        val insertedId = meterDao.insertMeter(meter)
        val retrievedMeter = meterDao.getMeterById(insertedId)

        // Then
        assertNotNull(retrievedMeter)
        assertEquals("EL-001", retrievedMeter?.number)
        assertEquals("ул. Ленина, д. 10", retrievedMeter?.address)
        assertEquals(MeterType.ELECTRICITY, retrievedMeter?.type)
    }

    @Test
    fun `get all meters should return inserted meters`() = runTest {
        // Given
        val meter1 = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meter2 = Meter(0, "GAS-002", "ул. Пушкина, д. 5", MeterType.GAS)

        // When
        meterDao.insertMeter(meter1)
        meterDao.insertMeter(meter2)
        val allMeters = meterDao.getAllMeters().first()

        // Then
        assertEquals(2, allMeters.size)
        assertTrue(allMeters.any { it.number == "EL-001" })
        assertTrue(allMeters.any { it.number == "GAS-002" })
    }

    @Test
    fun `insert and get reading should work correctly`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 1000.0, System.currentTimeMillis())

        // When
        val insertedId = readingDao.insertReading(reading)
        val retrievedReading = readingDao.getLatestReadingByMeterId(meterId)

        // Then
        assertNotNull(retrievedReading)
        assertEquals(meterId, retrievedReading?.meterId)
        assertEquals(1000.0, retrievedReading?.value, 0.001)
    }

    @Test
    fun `get readings by meter id should return correct readings`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading1 = Reading(0, meterId, 1000.0, System.currentTimeMillis() - 86400000L)
        val reading2 = Reading(0, meterId, 1100.0, System.currentTimeMillis())

        // When
        readingDao.insertReading(reading1)
        readingDao.insertReading(reading2)
        val readings = readingDao.getReadingsByMeterId(meterId).first()

        // Then
        assertEquals(2, readings.size)
        assertTrue(readings.any { it.value == 1000.0 })
        assertTrue(readings.any { it.value == 1100.0 })
    }

    @Test
    fun `insert and get tariff should work correctly`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)

        // When
        val insertedId = tariffDao.insertTariff(tariff)
        val retrievedTariff = tariffDao.getCurrentTariffByMeterId(meterId)

        // Then
        assertNotNull(retrievedTariff)
        assertEquals(meterId, retrievedTariff?.meterId)
        assertEquals(4.5, retrievedTariff?.rate, 0.001)
    }

    @Test
    fun `get tariffs by meter id should return correct tariffs`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff1 = Tariff(0, meterId, 4.5, System.currentTimeMillis() - 86400000L, null)
        val tariff2 = Tariff(0, meterId, 5.0, System.currentTimeMillis(), null)

        // When
        tariffDao.insertTariff(tariff1)
        tariffDao.insertTariff(tariff2)
        val tariffs = tariffDao.getTariffsByMeterId(meterId).first()

        // Then
        assertEquals(2, tariffs.size)
        assertTrue(tariffs.any { it.rate == 4.5 })
        assertTrue(tariffs.any { it.rate == 5.0 })
    }

    @Test
    fun `delete meter should remove meter and related data`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 1000.0, System.currentTimeMillis())
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)
        
        readingDao.insertReading(reading)
        tariffDao.insertTariff(tariff)

        // When
        meterDao.deleteMeter(Meter(meterId, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY))

        // Then
        val retrievedMeter = meterDao.getMeterById(meterId)
        assertNull(retrievedMeter)
    }

    @Test
    fun `update meter should modify existing meter`() = runTest {
        // Given
        val meter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val updatedMeter = Meter(meterId, "EL-002", "ул. Пушкина, д. 5", MeterType.ELECTRICITY)

        // When
        meterDao.updateMeter(updatedMeter)
        val retrievedMeter = meterDao.getMeterById(meterId)

        // Then
        assertNotNull(retrievedMeter)
        assertEquals("EL-002", retrievedMeter?.number)
        assertEquals("ул. Пушкина, д. 5", retrievedMeter?.address)
    }

    @Test
    fun `get meters by type should return correct meters`() = runTest {
        // Given
        val electricityMeter = Meter(0, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY)
        val gasMeter = Meter(0, "GAS-002", "ул. Пушкина, д. 5", MeterType.GAS)
        val anotherElectricityMeter = Meter(0, "EL-003", "ул. Мира, д. 1", MeterType.ELECTRICITY)

        // When
        meterDao.insertMeter(electricityMeter)
        meterDao.insertMeter(gasMeter)
        meterDao.insertMeter(anotherElectricityMeter)
        val electricityMeters = meterDao.getMetersByType(MeterType.ELECTRICITY).first()

        // Then
        assertEquals(2, electricityMeters.size)
        assertTrue(electricityMeters.all { it.type == MeterType.ELECTRICITY })
    }
}