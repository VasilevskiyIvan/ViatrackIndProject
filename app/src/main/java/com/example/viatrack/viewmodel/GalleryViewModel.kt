package com.example.viatrack.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.viatrack.database.AppDatabase
import com.example.viatrack.database.Media
import com.example.viatrack.ui.gallery.GalleryItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val tripDao = AppDatabase.getInstance(application).tripDao()
    private val eventDao = AppDatabase.getInstance(application).eventDao()
    private val mediaDao = AppDatabase.getInstance(application).mediaDao()

    private val _galleryItems = MutableLiveData<List<GalleryItemData>>()
    val galleryItems: LiveData<List<GalleryItemData>> = _galleryItems

    private val _totalPhotoCount = MutableLiveData<Int>()
    val totalPhotoCount: LiveData<Int> = _totalPhotoCount

    private val _totalVideoCount = MutableLiveData<Int>()
    val totalVideoCount: LiveData<Int> = _totalVideoCount

    init {
        loadGalleryData()
    }

    fun loadGalleryData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                val allTrips = tripDao.getAllTripsForGallery()
                val allMedia = mediaDao.getAllMediaForGallery()

                val resultList = mutableListOf<GalleryItemData>()

                val totalPhotos = allMedia.count { it.type == "PHOTO" }
                val totalVideos = allMedia.count { it.type == "VIDEO" }
                _totalPhotoCount.postValue(totalPhotos)
                _totalVideoCount.postValue(totalVideos)

                val mediaMap: Map<Long, List<Media>> = allMedia.groupBy { it.tripId }

                for (trip in allTrips) {
                    val tripMedia = mediaMap[trip.id] ?: continue

                    val photoCount = tripMedia.count { it.type == "PHOTO" }
                    val videoCount = tripMedia.count { it.type == "VIDEO" }
                    resultList.add(GalleryItemData.TripHeader(trip, photoCount, videoCount))

                    val mediaByEvent: Map<Long?, List<Media>> = tripMedia.groupBy { it.eventId }

                    val eventIdsWithMedia = mediaByEvent.keys.filterNotNull()
                    val mediaWithoutEvent = mediaByEvent[null]

                    val events = eventDao.getEventsByTripId(trip.id)
                        .filter { it.id in eventIdsWithMedia }

                    for (event in events) {
                        resultList.add(GalleryItemData.EventHeader(event, trip.id))
                        mediaByEvent[event.id]?.forEach { media ->
                            resultList.add(GalleryItemData.MediaGridItem(media))
                        }
                    }

                    mediaWithoutEvent?.forEach { media ->
                        resultList.add(GalleryItemData.MediaGridItem(media))
                    }
                }

                _galleryItems.postValue(resultList)
            }
        }
    }
}
