package com.example.viatrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.services.TrackingService
import com.example.viatrack.ui.AddNoteDialogFragment
import com.example.viatrack.ui.EventDetailDialogFragment
import com.example.viatrack.ui.StartEventDialogFragment
import com.example.viatrack.ui.StartTripDialogFragment
import com.example.viatrack.ui.adapters.TimelineAdapter
import com.example.viatrack.utils.Constants
import com.example.viatrack.viewmodel.TrackerViewModel
import com.example.viatrack.viewmodel.TrackerViewModelFactory
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackerFragment : Fragment(R.layout.fragment_tracker) {

    private val viewModel: TrackerViewModel by activityViewModels {
        TrackerViewModelFactory(requireActivity().application)
    }

    private lateinit var motionLayout: MotionLayout
    private lateinit var trackerPanel: View
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnStartEvent: MaterialButton
    private lateinit var tvCity: TextView
    private lateinit var tvDates: TextView
    private lateinit var tvKm: TextView
    private lateinit var tvSteps: TextView
    private lateinit var tvKmPerHour: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var btnExpandPanel: View

    private lateinit var mapView: MapView
    private var roadOverlay = Polyline()
    private var locationMarker: Marker? = null

    private lateinit var rvTimeline: RecyclerView
    private lateinit var timelineAdapter: TimelineAdapter

    private var isTracking = false
    private var currentTripId: Long? = null
    private var currentEventId: Long? = null
    private var latestPhotoUri: Uri? = null
    private var latestVideoUri: Uri? = null

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) Toast.makeText(requireContext(), "Необходимо право на камеру", Toast.LENGTH_SHORT).show()
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                latestPhotoUri?.let { uri ->
                    viewModel.saveMedia("PHOTO", uri.toString(), null, currentTripId, currentEventId)
                }
            }
        }

        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            if (success) {
                latestVideoUri?.let { uri ->
                    viewModel.saveMedia("VIDEO", uri.toString(), null, currentTripId, currentEventId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupMap(view)
        setupMotionLayout()
        setupTimelineRecycler(view)
        observeViewModel()
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        motionLayout = view.findViewById(R.id.motion_tracker_root)
        trackerPanel = view.findViewById(R.id.tracker_panel)
        btnExpandPanel = view.findViewById(R.id.btn_expand_panel)
        btnPlayPause = trackerPanel.findViewById(R.id.btn_play_pause)
        btnStartEvent = view.findViewById(R.id.btn_start_event)
        tvCity = trackerPanel.findViewById(R.id.tv_city)
        tvDates = trackerPanel.findViewById(R.id.tv_dates)
        tvKm = trackerPanel.findViewById(R.id.tv_km)
        tvSteps = trackerPanel.findViewById(R.id.tv_steps)
        tvKmPerHour = trackerPanel.findViewById(R.id.tv_km_per_hour)
        tvAltitude = trackerPanel.findViewById(R.id.tv_altitude)
    }

    private fun setupMap(view: View) {
        mapView = view.findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        roadOverlay.outlinePaint.color = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
        roadOverlay.outlinePaint.strokeWidth = 12f
        mapView.overlays.add(roadOverlay)

        locationMarker = Marker(mapView).apply {
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            infoWindow = null
        }
        locationMarker?.let { mapView.overlays.add(it) }
    }

    private fun setupTimelineRecycler(view: View) {
        rvTimeline = trackerPanel.findViewById(R.id.rv_timeline)
        timelineAdapter = TimelineAdapter(emptyList()) { model ->
            EventDetailDialogFragment.newInstance(model.event.id, model.event.title)
                .show(parentFragmentManager, "EventDetail")
        }
        rvTimeline.layoutManager = LinearLayoutManager(requireContext())
        rvTimeline.adapter = timelineAdapter
    }

    private fun observeViewModel() {
        viewModel.activeTrip.observe(viewLifecycleOwner) { trip ->
            if (trip == null) {
                if (parentFragmentManager.findFragmentByTag("StartTripDialog") == null) {
                    StartTripDialogFragment().show(parentFragmentManager, "StartTripDialog")
                }
                trackerPanel.visibility = View.GONE
                btnStartEvent.visibility = View.GONE
            } else {
                trackerPanel.visibility = View.VISIBLE
                btnStartEvent.visibility = View.VISIBLE
                if (currentTripId != trip.id) {
                    currentTripId = trip.id
                    viewModel.loadTimeline(trip.id)
                }
                tvCity.text = trip.title
                val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(trip.startTime))
                tvDates.text = "$startDate - Настоящее время"
            }
        }

        viewModel.routePoints.observe(viewLifecycleOwner) { points ->
            if (!points.isNullOrEmpty()) {
                val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }
                roadOverlay.setPoints(geoPoints)
                val lastPoint = geoPoints.last()
                locationMarker?.position = lastPoint
                mapView.controller.animateTo(lastPoint)
                mapView.invalidate()
            }
        }

        viewModel.timelineEvents.observe(viewLifecycleOwner) { timelineAdapter.updateList(it) }

        viewModel.activeEvent.observe(viewLifecycleOwner) { event ->
            currentEventId = event?.id
            btnStartEvent.text = if (event == null) "Начать событие" else "Завершить событие"
            btnStartEvent.setIconResource(if (event == null) R.drawable.play else R.drawable.stop)
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) { tracking ->
            isTracking = tracking
            btnPlayPause.setImageResource(if (isTracking) R.drawable.pause else R.drawable.play)
        }

        TrackingService.apply {
            liveDistance.observe(viewLifecycleOwner) { tvKm.text = String.format(Locale.US, "%.1f км", it / 1000f) }
            liveSteps.observe(viewLifecycleOwner) { tvSteps.text = "$it шагов" }
            livePace.observe(viewLifecycleOwner) { tvKmPerHour.text = String.format(Locale.US, "%.1f км/ч", it) }
            liveAltitude.observe(viewLifecycleOwner) { tvAltitude.text = String.format(Locale.US, "%.0f м", it) }
        }
    }

    private fun setupClickListeners() {
        trackerPanel.findViewById<LinearLayout>(R.id.button_one).apply {
            findViewById<ImageView>(R.id.icon_image_view).setImageResource(R.drawable.photo)
            findViewById<TextView>(R.id.icon_text_view).text = "Фото"
            setOnClickListener { if (currentTripId != null) openCameraImproved(true) }
        }

        trackerPanel.findViewById<LinearLayout>(R.id.button_two).apply {
            findViewById<ImageView>(R.id.icon_image_view).setImageResource(R.drawable.video)
            findViewById<TextView>(R.id.icon_text_view).text = "Видео"
            setOnClickListener { if (currentTripId != null) openCameraImproved(false) }
        }

        trackerPanel.findViewById<LinearLayout>(R.id.button_three).apply {
            findViewById<ImageView>(R.id.icon_image_view).setImageResource(R.drawable.text)
            findViewById<TextView>(R.id.icon_text_view).text = "Заметка"
            setOnClickListener { if (currentTripId != null) AddNoteDialogFragment().show(parentFragmentManager, "AddNoteDialog") }
        }

        btnExpandPanel.setOnClickListener {
            if (motionLayout.progress > 0.5f) motionLayout.transitionToStart() else motionLayout.transitionToEnd()
        }

        btnPlayPause.setOnClickListener {
            if (currentTripId != null) {
                sendCommandToService(if (isTracking) Constants.ACTION_PAUSE_TRACKING else Constants.ACTION_START_TRACKING)
            }
        }

        btnStartEvent.setOnClickListener {
            if (currentEventId == null) {
                if (parentFragmentManager.findFragmentByTag("StartEventDialog") == null) {
                    StartEventDialogFragment().show(parentFragmentManager, "StartEventDialog")
                }
            } else {
                viewModel.endActiveEvent()
            }
        }
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(requireContext(), TrackingService::class.java).apply {
            this.action = action
            putExtra(Constants.EXTRA_TRIP_ID, currentTripId)
            putExtra(Constants.EXTRA_EVENT_ID, currentEventId ?: -1L)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun openCameraImproved(isPhoto: Boolean) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        val file = createImageFile(isPhoto)
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        if (isPhoto) {
            latestPhotoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            latestVideoUri = uri
            takeVideoLauncher.launch(uri)
        }
    }

    private fun createImageFile(isPhoto: Boolean): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(if (isPhoto) "photos" else "videos")
        return File.createTempFile("VIA_${timeStamp}_", if (isPhoto) ".jpg" else ".mp4", storageDir)
    }

    private fun setupMotionLayout() {
        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) { mapView.invalidate() }
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) { mapView.invalidate() }
            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        })
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
}