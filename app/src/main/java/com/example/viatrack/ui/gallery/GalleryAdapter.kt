package com.example.viatrack.ui.gallery

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.viatrack.R
import com.example.viatrack.database.Event
import com.example.viatrack.database.Trip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryAdapter(
    private var items: List<GalleryItemData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TRIP_HEADER = 0
        const val VIEW_TYPE_EVENT_HEADER = 1
        const val VIEW_TYPE_MEDIA_GRID = 2
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GalleryItemData.TripHeader -> VIEW_TYPE_TRIP_HEADER
            is GalleryItemData.EventHeader -> VIEW_TYPE_EVENT_HEADER
            is GalleryItemData.MediaGridItem -> VIEW_TYPE_MEDIA_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TRIP_HEADER -> TripHeaderViewHolder(inflater.inflate(R.layout.item_gallery_trip_header, parent, false))
            VIEW_TYPE_EVENT_HEADER -> EventHeaderViewHolder(inflater.inflate(R.layout.item_gallery_event_header, parent, false))
            VIEW_TYPE_MEDIA_GRID -> MediaGridViewHolder(inflater.inflate(R.layout.item_gallery_grid_media, parent, false))
            else -> throw IllegalArgumentException("Неизвестный ViewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GalleryItemData.TripHeader -> (holder as TripHeaderViewHolder).bind(item.trip, item.totalPhotoCount, item.totalVideoCount)
            is GalleryItemData.EventHeader -> (holder as EventHeaderViewHolder).bind(item.event)
            is GalleryItemData.MediaGridItem -> (holder as MediaGridViewHolder).bind(item)
        }
    }

    fun updateData(newItems: List<GalleryItemData>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TripHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_trip_title)
        private val tvStats: TextView = view.findViewById(R.id.tv_trip_stats)

        fun bind(trip: Trip, photoCount: Int, videoCount: Int) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDateStr = sdf.format(Date(trip.startTime))
            val endDateStr = trip.endTime?.let { sdf.format(Date(it)) } ?: "н.в."

            tvTitle.text = trip.title
            tvStats.text = "$startDateStr – $endDateStr · $photoCount фото · $videoCount видео"
        }
    }

    class EventHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tv_event_title)

        fun bind(event: Event) {
            tvTitle.text = "Событие: ${event.title}"
        }
    }

    class MediaGridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.image_item)
        private val iconVideo: ImageView = view.findViewById(R.id.icon_video)

        fun bind(item: GalleryItemData.MediaGridItem) {
            val media = item.media
            val context = itemView.context

            Glide.with(context)
                .load(media.filePath)
                .placeholder(R.drawable.default_city)
                .error(R.drawable.default_city)
                .centerCrop()
                .into(imageView)

            iconVideo.visibility = if (media.type == "VIDEO") View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (!media.filePath.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(media.filePath)
                        val mimeType = if (media.type == "VIDEO") "video/*" else "image/*"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}