package com.example.viatrack

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.database.Trip
import com.example.viatrack.repository.TripStats
import com.example.viatrack.utils.DateUtils

class TripHistoryAdapter(private val onTripClick: (Trip) -> Unit) : ListAdapter<Pair<Trip, TripStats>, TripHistoryAdapter.TripViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_history, parent, false)
        return TripViewHolder(view, onTripClick)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val (trip, stats) = getItem(position)
        holder.bind(trip, stats)
    }

    class TripViewHolder(itemView: View, private val onTripClick: (Trip) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageTrip: ImageView = itemView.findViewById(R.id.image_trip)
        private val tvTripName: TextView = itemView.findViewById(R.id.tv_trip_name)
        private val tvStatsPhotos: TextView = itemView.findViewById(R.id.tv_stats_photos)
        private val tvStatsVideos: TextView = itemView.findViewById(R.id.tv_stats_videos)
        private val tvStatsDistance: TextView = itemView.findViewById(R.id.tv_stats_distance)
        private val tvTripDate: TextView = itemView.findViewById(R.id.tv_trip_date)

        fun bind(trip: Trip, stats: TripStats) {
            if (!stats.coverImagePath.isNullOrEmpty()) {
                imageTrip.setImageURI(Uri.parse(stats.coverImagePath))
            } else {
                imageTrip.setImageResource(R.drawable.default_city)
            }

            tvTripName.text = trip.title
            tvStatsPhotos.text = "${stats.photoCount} фото"
            tvStatsVideos.text = "${stats.videoCount} видео"
            tvStatsDistance.text = "%.1f км".format(stats.totalDistance)
            tvTripDate.text = DateUtils.formatTripDateRange(trip.startTime, trip.endTime)

            itemView.setOnClickListener { onTripClick(trip) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Pair<Trip, TripStats>>() {
        override fun areItemsTheSame(oldItem: Pair<Trip, TripStats>, newItem: Pair<Trip, TripStats>) = oldItem.first.id == newItem.first.id
        override fun areContentsTheSame(oldItem: Pair<Trip, TripStats>, newItem: Pair<Trip, TripStats>) = oldItem == newItem
    }
}