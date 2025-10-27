package com.worldclock.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ReadingDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var readingDao: ReadingDao
    private lateinit var meterDao: MeterDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        readingDao = database.readingDao()
        meterDao = database.meterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertReading should insert reading and return id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())

        // When
        val id = readingDao.insertReading(reading)

        // Then
        assert(id > 0)
    }

    @Test
    fun `getReadingById should return inserted reading`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())
        val id = readingDao.insertReading(reading)

        // When
        val retrievedReading = readingDao.getReadingById(id)

        // Then
        assertNotNull(retrievedReading)
        assert(reading.value == retrievedReading!!.value)
        assert(reading.meterId == retrievedReading.meterId)
        assertEquals(reading.date, retrievedReading.date)
    }

    @Test
    fun `getReadingById should return null for non-existent reading`() = runBlocking {
        // When
        val retrievedReading = readingDao.getReadingById(999L)

        // Then
        assertNull(retrievedReading)
    }

    @Test
    fun `getAllReadings should return all readings ordered by date desc`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val readings = listOf(
            Reading(0, meterId, 100.0, now - 2000),
            Reading(0, meterId, 200.0, now - 1000),
            Reading(0, meterId, 300.0, now)
        )
        readings.forEach { readingDao.insertReading(it) }

        // When
        val flow = readingDao.getAllReadings()
        val retrievedReadings = flow.first()

        // Then
        assertEquals(3, retrievedReadings.size)
        // Проверяем сортировку по дате (новые первыми)
        assertEquals(300.0, retrievedReadings[0].value, 0.01)
        assertEquals(200.0, retrievedReadings[1].value, 0.01)
        assertEquals(100.0, retrievedReadings[2].value, 0.01)
    }

    @Test
    fun `getReadingsByMeterId should return only readings for specific meter`() = runBlocking {
        // Given
        val meter1 = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meter2 = Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        val meterId1 = meterDao.insertMeter(meter1)
        val meterId2 = meterDao.insertMeter(meter2)
        
        val now = System.currentTimeMillis()
        val reading1 = Reading(0, meterId1, 100.0, now)
        val reading2 = Reading(0, meterId2, 200.0, now)
        
        readingDao.insertReading(reading1)
        readingDao.insertReading(reading2)

        // When
        val flow = readingDao.getReadingsByMeterId(meterId1)
        val meter1Readings = flow.first()

        // Then
        assertEquals(1, meter1Readings.size)
        assertEquals(100.0, meter1Readings[0].value, 0.01)
        assertEquals(meterId1, meter1Readings[0].meterId)
    }

    @Test
    fun `getLatestReadingByMeterId should return most recent reading`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val readings = listOf(
            Reading(0, meterId, 100.0, now - 2000),
            Reading(0, meterId, 200.0, now - 1000),
            Reading(0, meterId, 300.0, now)
        )
        readings.forEach { readingDao.insertReading(it) }

        // When
        val latestReading = readingDao.getLatestReadingByMeterId(meterId)

        // Then
        assertNotNull(latestReading)
        assertEquals(300.0, latestReading!!.value, 0.01)
        assertEquals(now, latestReading.date)
    }

    @Test
    fun `getPreviousReadingByMeterId should return second most recent reading`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val readings = listOf(
            Reading(0, meterId, 100.0, now - 2000),
            Reading(0, meterId, 200.0, now - 1000),
            Reading(0, meterId, 300.0, now)
        )
        readings.forEach { readingDao.insertReading(it) }

        // When
        val previousReading = readingDao.getPreviousReadingByMeterId(meterId)

        // Then
        assertNotNull(previousReading)
        assertEquals(200.0, previousReading!!.value, 0.01)
        assertEquals(now - 1000, previousReading.date)
    }

    @Test
    fun `getLatestReadingByMeterId should return null when no readings exist`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)

        // When
        val latestReading = readingDao.getLatestReadingByMeterId(meterId)

        // Then
        assertNull(latestReading)
    }

    @Test
    fun `getPreviousReadingByMeterId should return null when only one reading exists`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())
        readingDao.insertReading(reading)

        // When
        val previousReading = readingDao.getPreviousReadingByMeterId(meterId)

        // Then
        assertNull(previousReading)
    }

    @Test
    fun `updateReading should update existing reading`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())
        val id = readingDao.insertReading(reading)
        val updatedReading = reading.copy(id = id, value = 150.0)

        // When
        readingDao.updateReading(updatedReading)
        val retrievedReading = readingDao.getReadingById(id)

        // Then
        assertNotNull(retrievedReading)
        assertEquals(150.0, retrievedReading!!.value, 0.01)
    }

    @Test
    fun `deleteReading should remove reading from database`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())
        val id = readingDao.insertReading(reading)

        // When
        val readingToDelete = reading.copy(id = id)
        readingDao.deleteReading(readingToDelete)
        val retrievedReading = readingDao.getReadingById(id)

        // Then
        assertNull(retrievedReading)
    }

    @Test
    fun `deleteReadingById should remove reading by id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val reading = Reading(0, meterId, 100.0, System.currentTimeMillis())
        val id = readingDao.insertReading(reading)

        // When
        readingDao.deleteReadingById(id)
        val retrievedReading = readingDao.getReadingById(id)

        // Then
        assertNull(retrievedReading)
    }

    @Test
    fun `deleteReadingsByMeterId should remove all readings for specific meter`() = runBlocking {
        // Given
        val meter1 = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meter2 = Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        val meterId1 = meterDao.insertMeter(meter1)
        val meterId2 = meterDao.insertMeter(meter2)
        
        val now = System.currentTimeMillis()
        val reading1 = Reading(0, meterId1, 100.0, now)
        val reading2 = Reading(0, meterId2, 200.0, now)
        
        readingDao.insertReading(reading1)
        readingDao.insertReading(reading2)

        // When
        readingDao.deleteReadingsByMeterId(meterId1)
        val flow = readingDao.getAllReadings()
        val remainingReadings = flow.first()

        // Then
        assertEquals(1, remainingReadings.size)
        assertEquals(200.0, remainingReadings[0].value, 0.01)
    }

    @Test
    fun `deleteAllReadings should remove all readings`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val readings = listOf(
            Reading(0, meterId, 100.0, System.currentTimeMillis()),
            Reading(0, meterId, 200.0, System.currentTimeMillis())
        )
        readings.forEach { readingDao.insertReading(it) }

        // When
        readingDao.deleteAllReadings()
        val flow = readingDao.getAllReadings()
        val remainingReadings = flow.first()

        // Then
        assertEquals(0, remainingReadings.size)
    }

    @Test
    fun `insertReadings should insert multiple readings`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val readings = listOf(
            Reading(0, meterId, 100.0, now - 2000),
            Reading(0, meterId, 200.0, now - 1000),
            Reading(0, meterId, 300.0, now)
        )

        // When
        readingDao.insertReadings(readings)
        val flow = readingDao.getAllReadings()
        val retrievedReadings = flow.first()

        // Then
        assertEquals(3, retrievedReadings.size)
    }
}
