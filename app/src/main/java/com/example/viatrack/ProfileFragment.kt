package com.example.viatrack

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface SelectionListener {
    fun onOptionSelected(key: String, selectedValue: String)
}

class OptionSelectionBottomSheet : BottomSheetDialogFragment() {

    private val ARG_TITLE = "title"
    private val ARG_OPTIONS = "options"
    private val ARG_KEY = "setting_key"

    private var selectionListener: SelectionListener? = null
    private lateinit var settingKey: String

    companion object {
        fun newInstance(title: String, options: List<String>, settingKey: String): OptionSelectionBottomSheet {
            val fragment = OptionSelectionBottomSheet()
            fragment.arguments = Bundle().apply {
                putString(fragment.ARG_TITLE, title)
                putStringArrayList(fragment.ARG_OPTIONS, ArrayList(options))
                putString(fragment.ARG_KEY, settingKey)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentFragment is SelectionListener) {
            selectionListener = parentFragment as SelectionListener
        } else if (activity is SelectionListener) {
            selectionListener = activity as SelectionListener
        }

        val title = arguments?.getString(ARG_TITLE) ?: "Выберите опцию"
        val options = arguments?.getStringArrayList(ARG_OPTIONS) ?: emptyList()
        settingKey = arguments?.getString(ARG_KEY) ?: ""

        view.findViewById<TextView>(R.id.bottom_sheet_title).text = title

        val recyclerView = view.findViewById<RecyclerView>(R.id.options_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = OptionsAdapter(options) { selectedOption ->
            selectionListener?.onOptionSelected(settingKey, selectedOption)
            dismiss()
        }
        recyclerView.adapter = adapter
    }

    private class OptionsAdapter(
        private val options: List<String>,
        private val onOptionClick: (String) -> Unit
    ) : RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {

        class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.option_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
            return OptionViewHolder(view)
        }

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
            val option = options[position]
            holder.textView.text = option
            holder.itemView.setOnClickListener {
                onOptionClick(option)
            }
        }

        override fun getItemCount() = options.size
    }
}

class ProfileFragment : Fragment(), SelectionListener {

    private val TAG = "ProfileFragment"

    private var currencySubtitleView: TextView? = null
    private var languageSubtitleView: TextView? = null
    private var themeSubtitleView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSelectionSetting(
            rootView = view,
            includeId = R.id.setting_units,
            settingKey = "setting_units",
            title = "Единицы измерения",
            initialSubtitle = "Километры",
            options = listOf("Километры", "Мили", "Футы", "Метры")
        )

        setupSelectionSetting(
            rootView = view,
            includeId = R.id.setting_language,
            settingKey = "language",
            title = "Язык",
            initialSubtitle = "Русский",
            options = listOf("Русский", "English")
        )

        setupSelectionSetting(
            rootView = view,
            includeId = R.id.setting_theme,
            settingKey = "theme",
            title = "Тема оформления",
            initialSubtitle = "Системная",
            options = listOf("Светлая", "Темная", "Системная")
        )


        setupToggleSetting(view, R.id.notif_tracking, "Уведомления о трекинге", false) { Log.d(TAG, "Уведомления о трекинге: $it") }
        setupToggleSetting(view, R.id.notif_pause, "Уведомление о паузе", true) { Log.d(TAG, "Уведомление о паузе: $it") }
        setupToggleSetting(view, R.id.notif_daily, "Ежедневный отчет", false) { Log.d(TAG, "Ежедневный отчет: $it") }

        setupArrowSetting(
            rootView = view,
            includeId = R.id.data_storage,
            title = "Хранение данных",
            subtitle = "Локально",
            onClick = { Toast.makeText(context, "Настройки хранилища", Toast.LENGTH_SHORT).show() }
        )

        setupArrowSetting(
            rootView = view,
            includeId = R.id.data_export,
            title = "Экспорт данных",
            subtitle = "",
            onClick = { Toast.makeText(context, "Экспорт", Toast.LENGTH_SHORT).show() }
        )

        view.findViewById<View>(R.id.data_delete)?.setOnClickListener {
            Log.d(TAG, "Клик: Удаление данных")
            Toast.makeText(context, "Клик по 'Удалить все данные'", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<TextView>(R.id.app_version)?.text = "1.0.1"
        view.findViewById<TextView>(R.id.app_developer)?.text = "Vialogue"
    }

    override fun onOptionSelected(key: String, selectedValue: String) {
        Log.d(TAG, "Выбрано: Ключ=$key, Значение=$selectedValue")
        Toast.makeText(context, "Выбрано: $selectedValue", Toast.LENGTH_SHORT).show()

        when (key) {
            "currency" -> currencySubtitleView?.text = selectedValue
            "language" -> languageSubtitleView?.text = selectedValue
            "theme" -> themeSubtitleView?.text = selectedValue
        }
    }

    private fun setupSelectionSetting(
        rootView: View,
        includeId: Int,
        settingKey: String,
        title: String,
        initialSubtitle: String,
        iconResId: Int? = null,
        options: List<String>
    ) {
        val settingView = rootView.findViewById<View>(includeId) ?: return
        val titleView = settingView.findViewById<TextView>(R.id.setting_title)
        val subtitleView = settingView.findViewById<TextView>(R.id.setting_subtitle)
        val imageView = settingView.findViewById<ImageView>(R.id.setting_image)

        titleView.text = title
        subtitleView?.text = initialSubtitle

        when (settingKey) {
            "currency" -> currencySubtitleView = subtitleView
            "language" -> languageSubtitleView = subtitleView
            "theme" -> themeSubtitleView = subtitleView
        }

        if (iconResId != null) {
            imageView?.setImageResource(iconResId)
            imageView?.visibility = View.VISIBLE
        } else {
            imageView?.visibility = View.GONE
        }


        settingView.setOnClickListener {
            val bottomSheet = OptionSelectionBottomSheet.newInstance(title, options, settingKey)
            Log.d("Клик на событии", "Случился клик на элементе $title.")
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }


    private fun setupToggleSetting(
        rootView: View,
        includeId: Int,
        title: String,
        initialState: Boolean,
        onChecked: (Boolean) -> Unit
    ) {
        val settingView = rootView.findViewById<View>(includeId) ?: return
        val titleView = settingView.findViewById<TextView>(R.id.setting_title)
        val switchView = settingView.findViewById<Switch>(R.id.setting_switch)

        titleView.text = title
        switchView.isChecked = initialState

        switchView.setOnCheckedChangeListener { _, isChecked ->
            onChecked(isChecked)
        }

        settingView.setOnClickListener {
            switchView.isChecked = !switchView.isChecked
        }
    }

    private fun setupArrowSetting(
        rootView: View,
        includeId: Int,
        title: String,
        subtitle: String = "",
        iconResId: Int? = null,
        onClick: () -> Unit
    ) {
        val settingView = rootView.findViewById<View>(includeId) ?: return

        val titleView = settingView.findViewById<TextView>(R.id.setting_title)
        val subtitleView = settingView.findViewById<TextView>(R.id.setting_subtitle)

        val imageView = settingView.findViewById<ImageView>(R.id.setting_image)

        titleView.text = title
        subtitleView?.text = subtitle

        if (iconResId != null) {
            imageView?.setImageResource(iconResId)
            imageView?.visibility = View.VISIBLE
        } else {
            imageView?.visibility = View.GONE
        }

        settingView.setOnClickListener {
            onClick()
        }
    }
}