package com.example.viatrack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.services.TrackingService
import com.example.viatrack.utils.Constants
import com.example.viatrack.utils.DateUtils
import com.example.viatrack.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import com.example.viatrack.database.Trip
import com.example.viatrack.repository.TripStats
import com.example.viatrack.ui.TripDetailDialogFragment

class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by activityViewModels()
    private lateinit var tripHistoryAdapter: TripHistoryAdapter

    private lateinit var recyclerViewTrips: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPastTripsTitle: TextView
    private var sectionActiveTrip: View? = null

    private var tvActiveTripTitle: TextView? = null
    private var tvTripTitle: TextView? = null
    private var tvActiveTripDate: TextView? = null
    private var tvActivePhotos: TextView? = null
    private var tvActiveVideos: TextView? = null
    private var tvActiveDistance: TextView? = null
    private var btnEndTrip: Button? = null
    private var btnContinueTrip: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupViews(view: View) {
        recyclerViewTrips = view.findViewById(R.id.recyclerViewTrips)
        progressBar = view.findViewById(R.id.progressBar)
        tvPastTripsTitle = view.findViewById(R.id.tvPastTripsTitle)
        sectionActiveTrip = view.findViewById(R.id.section_active_trip)

        sectionActiveTrip?.let { activeTripRoot ->
            tvActiveTripTitle = activeTripRoot.findViewById(R.id.tvActiveTripTitle)
            tvTripTitle = activeTripRoot.findViewById(R.id.tvTripTitle)
            tvActiveTripDate = activeTripRoot.findViewById(R.id.tvActiveTripDate)
            tvActivePhotos = activeTripRoot.findViewById(R.id.tvActivePhotos)
            tvActiveVideos = activeTripRoot.findViewById(R.id.tvActiveVideos)
            tvActiveDistance = activeTripRoot.findViewById(R.id.tvActiveDistance)
            btnEndTrip = activeTripRoot.findViewById(R.id.btnEndTrip)
            btnContinueTrip = activeTripRoot.findViewById(R.id.btnContinueTrip)
        }
    }

    private fun setupRecyclerView() {
        tripHistoryAdapter = TripHistoryAdapter { trip ->
            TripDetailDialogFragment.newInstance(trip.id, trip.title).show(parentFragmentManager, "TripDetail")
        }
        recyclerViewTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripHistoryAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.activeTrip.observe(viewLifecycleOwner) { activeTrip ->
            if (!isAdded) return@observe
            if (activeTrip != null) {
                sectionActiveTrip?.visibility = View.VISIBLE
                showActiveTrip(activeTrip)
            } else {
                sectionActiveTrip?.visibility = View.GONE
                viewModel.loadHistoryData()
            }
        }

        viewModel.completedTrips.observe(viewLifecycleOwner) { tripsWithStats ->
            if (!isAdded) return@observe
            tripHistoryAdapter.submitList(tripsWithStats.toList())
            tvPastTripsTitle.visibility = if (tripsWithStats.isNotEmpty()) View.VISIBLE else View.GONE
            if (tripsWithStats.isNotEmpty()) {
                recyclerViewTrips.post {
                    recyclerViewTrips.scrollToPosition(0)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isAdded) return@observe
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showActiveTrip(trip: Trip) {
        lifecycleScope.launch {
            try {
                val stats = viewModel.getTripStats(trip.id)
                if (!isAdded) return@launch
                updateActiveTripViews(trip, stats)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateActiveTripViews(trip: Trip, stats: TripStats) {
        tvActiveTripTitle?.text = "Активная поездка"
        tvTripTitle?.text = trip.title
        tvActiveTripDate?.text = DateUtils.formatTripDateRange(trip.startTime, trip.endTime)
        tvActivePhotos?.text = "${stats.photoCount} фото"
        tvActiveVideos?.text = "${stats.videoCount} видео"
        tvActiveDistance?.text = "%.2f км".format(stats.totalDistance)

        sectionActiveTrip?.setOnClickListener {
            TripDetailDialogFragment.newInstance(trip.id, trip.title).show(parentFragmentManager, "TripDetail")
        }

        btnEndTrip?.setOnClickListener {
            val stopIntent = Intent(requireContext(), TrackingService::class.java).apply {
                action = Constants.ACTION_STOP_TRACKING
            }
            ContextCompat.startForegroundService(requireContext(), stopIntent)
            viewModel.endActiveTrip()
        }

        btnContinueTrip?.setOnClickListener {
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistoryData()
    }
}