package com.example.viatrack.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatTripDateRange(startTime: Long, endTime: Long?): String {
        val startDate = dateFormat.format(Date(startTime))
        val endDate = if (endTime != null) {
            dateFormat.format(Date(endTime))
        } else {
            "Настоящее время"
        }
        return "$startDate - $endDate"
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}