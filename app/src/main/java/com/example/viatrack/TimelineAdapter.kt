package com.example.viatrack.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.R
import com.example.viatrack.viewmodel.EventUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimelineAdapter(
    private var events: List<EventUiModel>,
    private val onClick: (EventUiModel) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    fun updateList(newList: List<EventUiModel>) {
        events = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chronology_event, parent, false)
        return TimelineViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    class TimelineViewHolder(itemView: View, val onClick: (EventUiModel) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val ivImage: ImageView = itemView.findViewById(R.id.image_event)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_event_description)
        private val tvDetails: TextView = itemView.findViewById(R.id.tv_event_details)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_event_time)

        fun bind(item: EventUiModel) {
            tvDescription.text = item.event.title
            val photoText = if (item.photoCount > 0) "${item.photoCount} фото" else "Нет фото"
            tvDetails.text = photoText

            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(item.event.startTime))

            if (!item.coverPath.isNullOrEmpty()) {
                try {
                    ivImage.setImageURI(Uri.parse(item.coverPath))
                } catch (e: Exception) {
                    ivImage.setImageResource(R.drawable.default_city)
                }
            } else {
                ivImage.setImageResource(R.drawable.default_city)
            }

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}