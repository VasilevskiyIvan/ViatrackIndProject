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

class AddNoteDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: TrackerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etContent = view.findViewById<EditText>(R.id.et_note_content)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isNotEmpty()) {
                val tripId = viewModel.activeTrip.value?.id

                val eventId = viewModel.activeEvent.value?.id

                if(tripId != null) {
                    viewModel.saveMedia(
                        type = "NOTE",
                        filePath = null,
                        content = content,
                        tripId = tripId,
                        eventId = eventId
                    )
                    Toast.makeText(requireContext(), "Заметка успешно сохранена", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Ошибка: Поездка не найдена", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Заметка не может быть пустой", Toast.LENGTH_SHORT).show()
            }
        }
    }
}