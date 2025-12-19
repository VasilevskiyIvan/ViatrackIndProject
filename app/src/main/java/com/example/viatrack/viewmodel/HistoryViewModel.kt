package com.example.viatrack.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.viatrack.database.Media
import com.example.viatrack.database.Trip
import com.example.viatrack.repository.TrackerRepository
import com.example.viatrack.repository.TripStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrackerRepository(application)
    val activeTrip: LiveData<Trip?> = repository.activeTrip

    private val _completedTrips = MutableLiveData<List<Pair<Trip, TripStats>>>()
    val completedTrips: LiveData<List<Pair<Trip, TripStats>>> = _completedTrips

    private val _tripDetails = MutableLiveData<List<Media>>()
    val tripDetails: LiveData<List<Media>> = _tripDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadHistoryData()
    }

    fun loadHistoryData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val completedTripsList = withContext(Dispatchers.IO) {
                    repository.getCompletedTrips()
                }
                val tripsWithStats = mutableListOf<Pair<Trip, TripStats>>()
                for (trip in completedTripsList) {
                    val stats = withContext(Dispatchers.IO) {
                        repository.getTripStats(trip.id)
                    }
                    tripsWithStats.add(Pair(trip, stats))
                }
                _completedTrips.value = tripsWithStats
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTripDetails(tripId: Long) {
        viewModelScope.launch {
            val media = withContext(Dispatchers.IO) {
                repository.getTripDetails(tripId)
            }
            _tripDetails.value = media
        }
    }

    fun endActiveTrip() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.endActiveTrip()
                }
                loadHistoryData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getTripStats(tripId: Long): TripStats {
        return repository.getTripStats(tripId)
    }
}