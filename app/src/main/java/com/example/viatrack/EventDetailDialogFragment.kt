package com.example.viatrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.viatrack.R
import com.example.viatrack.ui.adapters.EventDetailAdapter
import com.example.viatrack.viewmodel.TrackerViewModel

class EventDetailDialogFragment : DialogFragment() {

    private val viewModel: TrackerViewModel by activityViewModels()
    private lateinit var adapter: EventDetailAdapter

    companion object {
        private const val ARG_EVENT_ID = "arg_event_id"
        private const val ARG_EVENT_TITLE = "arg_event_title"

        fun newInstance(eventId: Long, title: String) = EventDetailDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_EVENT_ID, eventId)
                putString(ARG_EVENT_TITLE, title)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dialog_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = arguments?.getLong(ARG_EVENT_ID) ?: return
        val title = arguments?.getString(ARG_EVENT_TITLE) ?: ""

        view.findViewById<TextView>(R.id.tv_detail_title).text = title
        view.findViewById<ImageButton>(R.id.btn_close_detail).setOnClickListener { dismiss() }

        setupRecyclerView(view)

        viewModel.loadEventDetails(eventId)
        viewModel.eventDetails.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }
    }

    private fun setupRecyclerView(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rv_event_details)
        adapter = EventDetailAdapter()

        val glm = GridLayoutManager(requireContext(), 3)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == EventDetailAdapter.TYPE_TEXT) 3 else 1
            }
        }

        rv.layoutManager = glm
        rv.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }
}