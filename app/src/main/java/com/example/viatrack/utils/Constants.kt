package com.example.viatrack.utils

object Constants {

    const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
    const val ACTION_PAUSE_TRACKING = "ACTION_PAUSE_TRACKING"
    const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
    const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

    const val DATABASE_NAME = "viatrack_db"
}