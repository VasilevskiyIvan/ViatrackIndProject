package com.example.viatrack.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.R
import com.example.viatrack.database.Media
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Media> = emptyList()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault())

    fun setItems(newItems: List<Media>) {
        items = newItems
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1
        const val TYPE_TEXT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            "PHOTO" -> TYPE_IMAGE
            "VIDEO" -> TYPE_VIDEO
            else -> TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT -> TextViewHolder(inflater.inflate(R.layout.item_detail_text, parent, false))
            else -> MediaViewHolder(inflater.inflate(R.layout.item_detail_media, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val timeStr = timeFormat.format(Date(item.timestamp))
        val fullTimeStr = fullDateFormat.format(Date(item.timestamp))

        when (holder) {
            is MediaViewHolder -> holder.bind(item, timeStr)
            is TextViewHolder -> holder.bind(item, fullTimeStr)
        }
    }

    override fun getItemCount(): Int = items.size

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_detail_media)
        private val videoIndicator: ImageView = itemView.findViewById(R.id.iv_video_indicator)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_media_time)

        fun bind(media: Media, time: String) {
            tvTime.text = time
            videoIndicator.visibility = if (media.type == "VIDEO") View.VISIBLE else View.GONE

            if (!media.filePath.isNullOrEmpty()) {
                try {
                    imageView.setImageURI(Uri.parse(media.filePath))
                } catch (e: Exception) {
                    imageView.setImageResource(R.drawable.default_city)
                }
            }
        }
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_detail_note)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_note_time)

        fun bind(media: Media, time: String) {
            textView.text = media.content ?: ""
            tvTime.text = time
        }
    }
}