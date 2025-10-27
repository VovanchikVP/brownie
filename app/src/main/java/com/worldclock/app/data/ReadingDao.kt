package com.worldclock.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    
    @Query("SELECT * FROM readings ORDER BY date DESC")
    fun getAllReadings(): Flow<List<Reading>>
    
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY date DESC")
    fun getReadingsByMeterId(meterId: Long): Flow<List<Reading>>
    
    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun getReadingById(id: Long): Reading?
    
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestReadingByMeterId(meterId: Long): Reading?
    
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY date DESC LIMIT 1 OFFSET 1")
    suspend fun getPreviousReadingByMeterId(meterId: Long): Reading?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: Reading): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<Reading>)
    
    @Update
    suspend fun updateReading(reading: Reading)
    
    @Delete
    suspend fun deleteReading(reading: Reading)
    
    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)
    
    @Query("DELETE FROM readings WHERE meterId = :meterId")
    suspend fun deleteReadingsByMeterId(meterId: Long)
    
    @Query("DELETE FROM readings")
    suspend fun deleteAllReadings()
}