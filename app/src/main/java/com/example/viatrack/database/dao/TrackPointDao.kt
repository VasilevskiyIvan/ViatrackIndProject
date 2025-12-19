package com.example.viatrack.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insertTrackPoint(point: TrackPoint)

    @Query("SELECT COUNT(*) FROM track_points WHERE tripId = :tripId")
    suspend fun getTrackPointsCount(tripId: Long): Int

    @Query("SELECT * FROM track_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getAllPointsForTripSync(tripId: Long): List<TrackPoint>

    @Query("SELECT * FROM track_points")
    suspend fun getAllTrackPoints(): List<TrackPoint>

    @Query("SELECT * FROM track_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getPointsForTrip(tripId: Long): androidx.lifecycle.LiveData<List<TrackPoint>>
}