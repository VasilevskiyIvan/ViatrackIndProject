package com.example.viatrack.viewmodel

import com.example.viatrack.database.Event

data class EventUiModel(
    val event: Event,
    val photoCount: Int,
    val coverPath: String?
)