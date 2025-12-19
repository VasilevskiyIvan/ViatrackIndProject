package com.example.viatrack.ui.gallery

import com.example.viatrack.database.Event
import com.example.viatrack.database.Media
import com.example.viatrack.database.Trip

sealed class GalleryItemData {
    data class TripHeader(
        val trip: Trip,
        val totalPhotoCount: Int,
        val totalVideoCount: Int
    ) : GalleryItemData()

    data class EventHeader(
        val event: Event,
        val tripId: Long
    ) : GalleryItemData()

    data class MediaGridItem(val media: Media) : GalleryItemData()
}