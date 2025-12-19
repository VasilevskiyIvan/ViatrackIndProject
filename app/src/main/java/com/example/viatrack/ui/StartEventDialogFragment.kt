package com.example.viatrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.viatrack.R
import com.example.viatrack.viewmodel.TrackerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StartEventDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: TrackerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_start_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitle = view.findViewById<EditText>(R.id.et_event_title)

        val btnStart = view.findViewById<Button>(R.id.btn_dialog_confirm_start)

        btnStart.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                viewModel.startNewEvent(title)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
            }
        }
    }
}