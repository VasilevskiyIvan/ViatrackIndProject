package com.example.viatrack.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MediaDao {
    @Insert
    suspend fun insertMedia(media: Media)

    @Query("SELECT COUNT(*) FROM media WHERE tripId = :tripId AND type = 'PHOTO'")
    suspend fun getPhotoCount(tripId: Long): Int

    @Query("SELECT COUNT(*) FROM media WHERE tripId = :tripId AND type = 'VIDEO'")
    suspend fun getVideoCount(tripId: Long): Int

    @Query("SELECT COUNT(*) FROM media WHERE eventId = :eventId AND type = 'PHOTO'")
    suspend fun getPhotoCountForEvent(eventId: Long): Int

    @Query("SELECT * FROM media WHERE tripId = :tripId AND type = 'PHOTO' ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstPhoto(tripId: Long): Media?

    @Query("SELECT * FROM media WHERE eventId = :eventId AND type = 'PHOTO' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPhotoForEvent(eventId: Long): Media?

    @Query("SELECT * FROM media WHERE type = 'TEXT'")
    suspend fun getAllNotes(): List<Media>

    @Query("SELECT * FROM media WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getMediaForTrip(tripId: Long): List<Media>

    @Query("SELECT * FROM media WHERE eventId = :eventId ORDER BY timestamp ASC")
    suspend fun getMediaForEvent(eventId: Long): List<Media>

    @Query("""
        SELECT * FROM media
        WHERE type IN ('PHOTO', 'VIDEO')
        ORDER BY tripId DESC, eventId DESC, timestamp DESC
    """)
    suspend fun getAllMediaForGallery(): List<Media>
}