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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = org.robolectric.annotation.Config.NONE)
class MeterDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var meterDao: MeterDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        meterDao = database.meterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertMeter should insert meter and return id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)

        // When
        val id = meterDao.insertMeter(meter)

        // Then
        assert(id > 0)
    }

    @Test
    fun `getMeterById should return inserted meter`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val id = meterDao.insertMeter(meter)

        // When
        val retrievedMeter = meterDao.getMeterById(id)

        // Then
        assertNotNull(retrievedMeter)
        assertEquals(meter.number, retrievedMeter!!.number)
        assertEquals(meter.address, retrievedMeter.address)
        assertEquals(meter.type, retrievedMeter.type)
    }

    @Test
    fun `getMeterById should return null for non-existent meter`() = runBlocking {
        // When
        val retrievedMeter = meterDao.getMeterById(999L)

        // Then
        assertNull(retrievedMeter)
    }

    @Test
    fun `getAllMetersSync should return all inserted meters`() = runBlocking {
        // Given
        val meters = listOf(
            Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY),
            Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS),
            Meter(0, "11111", "ул. Тестовая, 3", MeterType.HOT_WATER)
        )
        meters.forEach { meterDao.insertMeter(it) }

        // When
        val retrievedMeters = meterDao.getAllMetersSync()

        // Then
        assertEquals(3, retrievedMeters.size)
        assertTrue(retrievedMeters.any { it.number == "12345" })
        assertTrue(retrievedMeters.any { it.number == "67890" })
        assertTrue(retrievedMeters.any { it.number == "11111" })
    }

    @Test
    fun `getAllMeters should return flow of all meters`() = runBlocking {
        // Given
        val meters = listOf(
            Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY),
            Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        )
        meters.forEach { meterDao.insertMeter(it) }

        // When
        val flow = meterDao.getAllMeters()
        val retrievedMeters = flow.first()

        // Then
        assertEquals(2, retrievedMeters.size)
    }

    @Test
    fun `getMetersByType should return only meters of specified type`() = runBlocking {
        // Given
        val electricityMeter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val gasMeter = Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        val hotWaterMeter = Meter(0, "11111", "ул. Тестовая, 3", MeterType.HOT_WATER)
        
        meterDao.insertMeter(electricityMeter)
        meterDao.insertMeter(gasMeter)
        meterDao.insertMeter(hotWaterMeter)

        // When
        val flow = meterDao.getMetersByType(MeterType.ELECTRICITY)
        val electricityMeters = flow.first()

        // Then
        assertEquals(1, electricityMeters.size)
        assertEquals(MeterType.ELECTRICITY, electricityMeters[0].type)
        assertEquals("12345", electricityMeters[0].number)
    }

    @Test
    fun `updateMeter should update existing meter`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val id = meterDao.insertMeter(meter)
        val updatedMeter = meter.copy(id = id, address = "ул. Обновленная, 1")

        // When
        meterDao.updateMeter(updatedMeter)
        val retrievedMeter = meterDao.getMeterById(id)

        // Then
        assertNotNull(retrievedMeter)
        assertEquals("ул. Обновленная, 1", retrievedMeter!!.address)
    }

    @Test
    fun `deleteMeter should remove meter from database`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val id = meterDao.insertMeter(meter)

        // When
        val meterToDelete = meter.copy(id = id)
        meterDao.deleteMeter(meterToDelete)
        val retrievedMeter = meterDao.getMeterById(id)

        // Then
        assertNull(retrievedMeter)
    }

    @Test
    fun `deleteMeterById should remove meter by id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val id = meterDao.insertMeter(meter)

        // When
        meterDao.deleteMeterById(id)
        val retrievedMeter = meterDao.getMeterById(id)

        // Then
        assertNull(retrievedMeter)
    }

    @Test
    fun `deleteAllMeters should remove all meters`() = runBlocking {
        // Given
        val meters = listOf(
            Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY),
            Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        )
        meters.forEach { meterDao.insertMeter(it) }

        // When
        meterDao.deleteAllMeters()
        val retrievedMeters = meterDao.getAllMetersSync()

        // Then
        assertEquals(0, retrievedMeters.size)
    }

    @Test
    fun `insertMeters should insert multiple meters`() = runBlocking {
        // Given
        val meters = listOf(
            Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY),
            Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS),
            Meter(0, "11111", "ул. Тестовая, 3", MeterType.HOT_WATER)
        )

        // When
        meterDao.insertMeters(meters)
        val retrievedMeters = meterDao.getAllMetersSync()

        // Then
        assertEquals(3, retrievedMeters.size)
    }

    @Test
    fun `getAllMetersSync should return meters ordered by type and number`() = runBlocking {
        // Given
        val meters = listOf(
            Meter(0, "33333", "ул. Тестовая, 3", MeterType.ELECTRICITY),
            Meter(0, "11111", "ул. Тестовая, 1", MeterType.ELECTRICITY),
            Meter(0, "22222", "ул. Тестовая, 2", MeterType.GAS)
        )
        meterDao.insertMeters(meters)

        // When
        val retrievedMeters = meterDao.getAllMetersSync()

        // Then
        assertEquals(3, retrievedMeters.size)
        // Проверяем, что электричество идет первым (по алфавиту)
        assertEquals(MeterType.ELECTRICITY, retrievedMeters[0].type)
        assertEquals(MeterType.ELECTRICITY, retrievedMeters[1].type)
        assertEquals(MeterType.GAS, retrievedMeters[2].type)
        // Проверяем сортировку по номеру внутри типа
        assertEquals("11111", retrievedMeters[0].number)
        assertEquals("33333", retrievedMeters[1].number)
    }
}
