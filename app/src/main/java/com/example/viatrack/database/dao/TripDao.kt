package com.example.viatrack.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    fun getActiveTrip(): androidx.lifecycle.LiveData<Trip?>

    @Query("SELECT * FROM trips WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTripOnce(): Trip?

    @Transaction
    @Query("UPDATE trips SET isActive = 0, endTime = :endTime WHERE isActive = 1")
    suspend fun deactivateActiveTrip(endTime: Long)

    @Query("SELECT * FROM trips WHERE isActive = 0 ORDER BY startTime DESC")
    suspend fun getCompletedTrips(): List<Trip>

    @Query("SELECT * FROM trips")
    suspend fun getAllTripsDebug(): List<Trip>

    @Query("SELECT * FROM trips ORDER BY isActive DESC, startTime DESC")
    suspend fun getAllTripsForGallery(): List<Trip>

}
