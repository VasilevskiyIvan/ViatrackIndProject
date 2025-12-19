package com.example.viatrack.repository

import android.app.Application
import android.location.Location
import androidx.lifecycle.LiveData
import com.example.viatrack.database.*
import com.example.viatrack.viewmodel.EventUiModel

class TrackerRepository(application: Application) {
    private val db = AppDatabase.getInstance(application)
    private val tripDao = db.tripDao()
    private val eventDao = db.eventDao()
    private val trackPointDao = db.trackPointDao()
    private val mediaDao = db.mediaDao()

    val activeTrip: LiveData<Trip?> = tripDao.getActiveTrip()

    suspend fun startNewTrip(title: String): Long {
        tripDao.deactivateActiveTrip(System.currentTimeMillis())
        val newTrip = Trip(title = title)
        return tripDao.insertTrip(newTrip)
    }

    suspend fun getActiveTripOnce(): Trip? {
        return tripDao.getActiveTripOnce()
    }

    suspend fun endActiveTrip() {
        tripDao.deactivateActiveTrip(System.currentTimeMillis())
    }

    suspend fun startNewEvent(title: String, tripId: Long): Long {
        eventDao.deactivateActiveEvent(tripId, System.currentTimeMillis())
        val newEvent = Event(tripId = tripId, title = title)
        return eventDao.insertEvent(newEvent)
    }

    suspend fun endActiveEvent(tripId: Long) {
        eventDao.deactivateActiveEvent(tripId, System.currentTimeMillis())
    }

    fun getActiveEvent(tripId: Long): LiveData<Event?> {
        return eventDao.getActiveEvent(tripId)
    }

    suspend fun getAllTrips(): List<Trip> {
        return tripDao.getAllTripsDebug()
    }

    suspend fun getCompletedTrips(): List<Trip> {
        return tripDao.getCompletedTrips()
    }

    suspend fun getTripStats(tripId: Long): TripStats {
        val photoCount = mediaDao.getPhotoCount(tripId)
        val videoCount = mediaDao.getVideoCount(tripId)
        val points = trackPointDao.getAllPointsForTripSync(tripId)
        val distanceMeters = calculateDistance(points)
        val firstPhoto = mediaDao.getFirstPhoto(tripId)

        return TripStats(
            photoCount = photoCount,
            videoCount = videoCount,
            totalDistance = distanceMeters / 1000f,
            coverImagePath = firstPhoto?.filePath
        )
    }

    suspend fun getTimelineEvents(tripId: Long): List<EventUiModel> {
        val events = eventDao.getEventsByTripId(tripId)
        return events.map { event ->
            val count = mediaDao.getPhotoCountForEvent(event.id)
            val cover = mediaDao.getLastPhotoForEvent(event.id)
            EventUiModel(event, count, cover?.filePath)
        }
    }

    suspend fun getEventDetails(eventId: Long): List<Media> {
        return mediaDao.getMediaForEvent(eventId)
    }

    suspend fun getTripDetails(tripId: Long): List<Media> {
        return mediaDao.getMediaForTrip(tripId)
    }

    fun getPointsForTrip(tripId: Long) = trackPointDao.getPointsForTrip(tripId)

    suspend fun getAllNotes(): List<Media> {
        return mediaDao.getAllNotes()
    }

    suspend fun getAllTrackPoints(): List<TrackPoint> {
        return trackPointDao.getAllTrackPoints()
    }

    suspend fun saveMedia(media: Media) {
        mediaDao.insertMedia(media)
    }

    suspend fun saveTrackPoint(point: TrackPoint) {
        trackPointDao.insertTrackPoint(point)
    }

    private fun calculateDistance(points: List<TrackPoint>): Float {
        var total = 0f
        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val results = FloatArray(1)
            Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
            total += results[0]
        }
        return total
    }
}

data class TripStats(
    val photoCount: Int,
    val videoCount: Int,
    val totalDistance: Float,
    val coverImagePath: String?
)