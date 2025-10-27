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
class TariffDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var tariffDao: TariffDao
    private lateinit var meterDao: MeterDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        tariffDao = database.tariffDao()
        meterDao = database.meterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertTariff should insert tariff and return id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)

        // When
        val id = tariffDao.insertTariff(tariff)

        // Then
        assert(id > 0)
    }

    @Test
    fun `getTariffById should return inserted tariff`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)
        val id = tariffDao.insertTariff(tariff)

        // When
        val retrievedTariff = tariffDao.getTariffById(id)

        // Then
        assertNotNull(retrievedTariff)
        assertEquals(tariff.rate, retrievedTariff!!.rate, 0.01)
        assertEquals(tariff.meterId, retrievedTariff.meterId)
        assertEquals(tariff.startDate, retrievedTariff.startDate)
        assertEquals(tariff.endDate, retrievedTariff.endDate)
    }

    @Test
    fun `getTariffById should return null for non-existent tariff`() = runBlocking {
        // When
        val retrievedTariff = tariffDao.getTariffById(999L)

        // Then
        assertNull(retrievedTariff)
    }

    @Test
    fun `getAllTariffs should return all tariffs ordered by startDate desc`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val tariffs = listOf(
            Tariff(0, meterId, 4.0, now - 2000, null),
            Tariff(0, meterId, 4.5, now - 1000, null),
            Tariff(0, meterId, 5.0, now, null)
        )
        tariffs.forEach { tariffDao.insertTariff(it) }

        // When
        val flow = tariffDao.getAllTariffs()
        val retrievedTariffs = flow.first()

        // Then
        assertEquals(3, retrievedTariffs.size)
        // Проверяем сортировку по дате начала (новые первыми)
        assertEquals(5.0, retrievedTariffs[0].rate, 0.01)
        assertEquals(4.5, retrievedTariffs[1].rate, 0.01)
        assertEquals(4.0, retrievedTariffs[2].rate, 0.01)
    }

    @Test
    fun `getTariffsByMeterId should return only tariffs for specific meter`() = runBlocking {
        // Given
        val meter1 = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meter2 = Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        val meterId1 = meterDao.insertMeter(meter1)
        val meterId2 = meterDao.insertMeter(meter2)
        
        val now = System.currentTimeMillis()
        val tariff1 = Tariff(0, meterId1, 4.5, now, null)
        val tariff2 = Tariff(0, meterId2, 6.0, now, null)
        
        tariffDao.insertTariff(tariff1)
        tariffDao.insertTariff(tariff2)

        // When
        val flow = tariffDao.getTariffsByMeterId(meterId1)
        val meter1Tariffs = flow.first()

        // Then
        assertEquals(1, meter1Tariffs.size)
        assertEquals(4.5, meter1Tariffs[0].rate, 0.01)
        assertEquals(meterId1, meter1Tariffs[0].meterId)
    }

    @Test
    fun `getCurrentTariffByMeterId should return tariff valid for given date`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val oldTariff = Tariff(0, meterId, 4.0, now - 2000, now - 1000)
        val currentTariff = Tariff(0, meterId, 4.5, now - 1000, now + 1000)
        val futureTariff = Tariff(0, meterId, 5.0, now + 1000, null)
        
        tariffDao.insertTariff(oldTariff)
        tariffDao.insertTariff(currentTariff)
        tariffDao.insertTariff(futureTariff)

        // When
        val currentTariffResult = tariffDao.getCurrentTariffByMeterId(meterId, now)

        // Then
        assertNotNull(currentTariffResult)
        assertEquals(4.5, currentTariffResult!!.rate, 0.01)
    }

    @Test
    fun `getCurrentTariffByMeterId should return null when no tariff valid for date`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val oldTariff = Tariff(0, meterId, 4.0, now - 2000, now - 1000)
        val futureTariff = Tariff(0, meterId, 5.0, now + 1000, null)
        
        tariffDao.insertTariff(oldTariff)
        tariffDao.insertTariff(futureTariff)

        // When
        val currentTariffResult = tariffDao.getCurrentTariffByMeterId(meterId, now)

        // Then
        assertNull(currentTariffResult)
    }

    @Test
    fun `getActiveTariffByMeterId should return tariff with null endDate`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val expiredTariff = Tariff(0, meterId, 4.0, now - 2000, now - 1000)
        val activeTariff = Tariff(0, meterId, 4.5, now - 1000, null)
        
        tariffDao.insertTariff(expiredTariff)
        tariffDao.insertTariff(activeTariff)

        // When
        val activeTariffResult = tariffDao.getActiveTariffByMeterId(meterId)

        // Then
        assertNotNull(activeTariffResult)
        assertEquals(4.5, activeTariffResult!!.rate, 0.01)
        assertNull(activeTariffResult.endDate)
    }

    @Test
    fun `getActiveTariffByMeterId should return null when no active tariff exists`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val expiredTariff = Tariff(0, meterId, 4.0, now - 2000, now - 1000)
        
        tariffDao.insertTariff(expiredTariff)

        // When
        val activeTariffResult = tariffDao.getActiveTariffByMeterId(meterId)

        // Then
        assertNull(activeTariffResult)
    }

    @Test
    fun `updateTariff should update existing tariff`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)
        val id = tariffDao.insertTariff(tariff)
        val updatedTariff = tariff.copy(id = id, rate = 5.0)

        // When
        tariffDao.updateTariff(updatedTariff)
        val retrievedTariff = tariffDao.getTariffById(id)

        // Then
        assertNotNull(retrievedTariff)
        assertEquals(5.0, retrievedTariff!!.rate, 0.01)
    }

    @Test
    fun `deleteTariff should remove tariff from database`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)
        val id = tariffDao.insertTariff(tariff)

        // When
        val tariffToDelete = tariff.copy(id = id)
        tariffDao.deleteTariff(tariffToDelete)
        val retrievedTariff = tariffDao.getTariffById(id)

        // Then
        assertNull(retrievedTariff)
    }

    @Test
    fun `deleteTariffById should remove tariff by id`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        val tariff = Tariff(0, meterId, 4.5, System.currentTimeMillis(), null)
        val id = tariffDao.insertTariff(tariff)

        // When
        tariffDao.deleteTariffById(id)
        val retrievedTariff = tariffDao.getTariffById(id)

        // Then
        assertNull(retrievedTariff)
    }

    @Test
    fun `deleteTariffsByMeterId should remove all tariffs for specific meter`() = runBlocking {
        // Given
        val meter1 = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meter2 = Meter(0, "67890", "ул. Тестовая, 2", MeterType.GAS)
        val meterId1 = meterDao.insertMeter(meter1)
        val meterId2 = meterDao.insertMeter(meter2)
        
        val now = System.currentTimeMillis()
        val tariff1 = Tariff(0, meterId1, 4.5, now, null)
        val tariff2 = Tariff(0, meterId2, 6.0, now, null)
        
        tariffDao.insertTariff(tariff1)
        tariffDao.insertTariff(tariff2)

        // When
        tariffDao.deleteTariffsByMeterId(meterId1)
        val flow = tariffDao.getAllTariffs()
        val remainingTariffs = flow.first()

        // Then
        assertEquals(1, remainingTariffs.size)
        assertEquals(6.0, remainingTariffs[0].rate, 0.01)
    }

    @Test
    fun `deleteAllTariffs should remove all tariffs`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val tariffs = listOf(
            Tariff(0, meterId, 4.5, System.currentTimeMillis(), null),
            Tariff(0, meterId, 5.0, System.currentTimeMillis(), null)
        )
        tariffs.forEach { tariffDao.insertTariff(it) }

        // When
        tariffDao.deleteAllTariffs()
        val flow = tariffDao.getAllTariffs()
        val remainingTariffs = flow.first()

        // Then
        assertEquals(0, remainingTariffs.size)
    }

    @Test
    fun `insertTariffs should insert multiple tariffs`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val tariffs = listOf(
            Tariff(0, meterId, 4.0, now - 2000, now - 1000),
            Tariff(0, meterId, 4.5, now - 1000, now + 1000),
            Tariff(0, meterId, 5.0, now + 1000, null)
        )

        // When
        tariffDao.insertTariffs(tariffs)
        val flow = tariffDao.getAllTariffs()
        val retrievedTariffs = flow.first()

        // Then
        assertEquals(3, retrievedTariffs.size)
    }

    @Test
    fun `getCurrentTariffByMeterId should handle tariff with null endDate`() = runBlocking {
        // Given
        val meter = Meter(0, "12345", "ул. Тестовая, 1", MeterType.ELECTRICITY)
        val meterId = meterDao.insertMeter(meter)
        
        val now = System.currentTimeMillis()
        val tariffWithNullEndDate = Tariff(0, meterId, 4.5, now - 1000, null)
        
        tariffDao.insertTariff(tariffWithNullEndDate)

        // When
        val currentTariffResult = tariffDao.getCurrentTariffByMeterId(meterId, now)

        // Then
        assertNotNull(currentTariffResult)
        assertEquals(4.5, currentTariffResult!!.rate, 0.01)
        assertNull(currentTariffResult.endDate)
    }
}
