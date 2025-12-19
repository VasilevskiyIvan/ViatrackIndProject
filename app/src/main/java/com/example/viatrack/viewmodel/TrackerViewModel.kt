package com.example.viatrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.viatrack.database.Event
import com.example.viatrack.database.Media
import com.example.viatrack.database.TrackPoint
import com.example.viatrack.database.Trip
import com.example.viatrack.repository.TrackerRepository
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TrackerRepository(application)

    val activeTrip: LiveData<Trip?> = repository.activeTrip

    val activeEvent: LiveData<Event?> = activeTrip.switchMap { trip ->
        if (trip == null) MutableLiveData(null)
        else repository.getActiveEvent(trip.id)
    }

    val routePoints: LiveData<List<TrackPoint>> = activeTrip.switchMap { trip ->
        if (trip == null) MutableLiveData(emptyList())
        else repository.getPointsForTrip(trip.id)
    }

    private val _timelineEvents = MutableLiveData<List<EventUiModel>>()
    val timelineEvents: LiveData<List<EventUiModel>> = _timelineEvents

    private val _eventDetails = MutableLiveData<List<Media>>()
    val eventDetails: LiveData<List<Media>> = _eventDetails

    fun startNewTrip(title: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.startNewTrip(title)
            onCreated(id)
        }
    }

    fun startNewEvent(title: String) = viewModelScope.launch {
        activeTrip.value?.let {
            repository.startNewEvent(title, it.id)
            loadTimeline(it.id)
        }
    }

    fun endActiveEvent() = viewModelScope.launch {
        activeTrip.value?.let { trip ->
            repository.endActiveEvent(trip.id)
            loadTimeline(trip.id)
        }
    }

    fun saveMedia(type: String, filePath: String?, content: String? = null, tripId: Long?, eventId: Long?) {
        if (tripId == null) return
        viewModelScope.launch {
            val media = Media(
                tripId = tripId,
                eventId = eventId,
                type = type,
                filePath = filePath,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            repository.saveMedia(media)
            loadTimeline(tripId)
        }
    }

    fun loadTimeline(tripId: Long) {
        viewModelScope.launch {
            _timelineEvents.value = repository.getTimelineEvents(tripId)
        }
    }

    fun loadEventDetails(eventId: Long) {
        viewModelScope.launch {
            _eventDetails.value = repository.getEventDetails(eventId)
        }
    }
}