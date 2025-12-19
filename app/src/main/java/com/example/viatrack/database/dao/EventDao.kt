package com.example.viatrack.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event): Long

    @Query("SELECT * FROM events WHERE tripId = :tripId AND isActive = 1 LIMIT 1")
    fun getActiveEvent(tripId: Long): LiveData<Event?>

    @Query("UPDATE events SET isActive = 0, endTime = :endTime WHERE isActive = 1 AND tripId = :tripId")
    suspend fun deactivateActiveEvent(tripId: Long, endTime: Long)

    @Query("SELECT * FROM events WHERE tripId = :tripId ORDER BY startTime DESC")
    suspend fun getEventsByTripId(tripId: Long): List<Event>
}