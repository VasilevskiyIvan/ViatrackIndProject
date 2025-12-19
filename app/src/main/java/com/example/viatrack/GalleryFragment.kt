package com.example.viatrack.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.R
import com.example.viatrack.viewmodel.GalleryViewModel

class GalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var rvGallery: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var llStats: LinearLayout
    private lateinit var tvStatsPhotos: TextView
    private lateinit var tvStatsVideos: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        rvGallery = view.findViewById(R.id.rv_gallery)
        llStats = view.findViewById(R.id.ll_stats)
        tvStatsPhotos = view.findViewById(R.id.tv_stats_photos)
        tvStatsVideos = view.findViewById(R.id.tv_stats_videos)

        galleryAdapter = GalleryAdapter(emptyList())
        rvGallery.adapter = galleryAdapter

        val spanCount = 3
        val layoutManager = GridLayoutManager(context, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (galleryAdapter.getItemViewType(position)) {
                    GalleryAdapter.VIEW_TYPE_TRIP_HEADER,
                    GalleryAdapter.VIEW_TYPE_EVENT_HEADER -> spanCount
                    GalleryAdapter.VIEW_TYPE_MEDIA_GRID -> 1
                    else -> 1
                }
            }
        }
        rvGallery.layoutManager = layoutManager

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.galleryItems.observe(viewLifecycleOwner) { items ->
            galleryAdapter.updateData(items)
        }

        viewModel.totalPhotoCount.observe(viewLifecycleOwner) { count ->
            tvStatsPhotos.text = "$count фото"
        }
        viewModel.totalVideoCount.observe(viewLifecycleOwner) { count ->
            tvStatsVideos.text = "$count видео"
        }
    }
}
