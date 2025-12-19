package com.example.viatrack.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var isActive: Boolean = true
)