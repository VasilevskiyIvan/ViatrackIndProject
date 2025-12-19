package com.example.viatrack.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "events",
    foreignKeys = [ForeignKey(entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE)]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null,
    var isActive: Boolean = true
)