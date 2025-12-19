package com.example.viatrack.utils

import android.location.Location

object TrackingUtils {
    fun calculateDistance(loc1: Location, loc2: Location): Float {
        return loc1.distanceTo(loc2)
    }

    fun calculatePolylineDistance(polyline: List<Location>): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            distance += calculateDistance(polyline[i], polyline[i + 1])
        }
        return distance
    }
}