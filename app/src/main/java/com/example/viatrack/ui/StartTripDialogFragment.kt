package com.example.viatrack.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.viatrack.R
import com.example.viatrack.services.TrackingService
import com.example.viatrack.utils.Constants
import com.example.viatrack.viewmodel.TrackerViewModel

class StartTripDialogFragment : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    private val viewModel: TrackerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_start_trip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val etTitle = view.findViewById<EditText>(R.id.et_trip_title)
        val btnStart = view.findViewById<Button>(R.id.btn_start_trip)

        btnStart.setOnClickListener {
            val title = etTitle.text.toString().trim()

            if (title.isNotEmpty()) {

                viewModel.startNewTrip(title) { tripId ->

                    val intent = Intent(requireContext(), TrackingService::class.java).apply {
                        action = Constants.ACTION_START_TRACKING
                        putExtra(Constants.EXTRA_TRIP_ID, tripId)
                        putExtra(Constants.EXTRA_EVENT_ID, -1L)
                    }

                    ContextCompat.startForegroundService(requireContext(), intent)
                    dismiss()
                }

            } else {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
