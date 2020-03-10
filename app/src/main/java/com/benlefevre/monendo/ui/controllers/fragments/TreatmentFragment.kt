package com.benlefevre.monendo.ui.controllers.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.benlefevre.monendo.R
import com.benlefevre.monendo.ui.customview.Pill
import com.benlefevre.monendo.utils.CHECKED_PILLS
import com.benlefevre.monendo.utils.PREFERENCES
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.parseStringInDate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_treatment.*
import java.util.*

class TreatmentFragment : Fragment(R.layout.fragment_treatment) {

    lateinit var sharedPreferences: SharedPreferences
    private val gson by lazy {
        Gson()
    }
    private val checkedPills: MutableList<Pill> by lazy {
        mutableListOf<Pill>()
    }
    lateinit var calendar: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        calendar = Calendar.getInstance()
        getCheckedPills()
        getUserInput()
        calculateElapsedTime()
    }

    private fun getCheckedPills() {
        if (gson.fromJson<List<Pill>>(sharedPreferences.getString(CHECKED_PILLS, null), object : TypeToken<List<Pill>>() {}.type) != null)
            checkedPills.addAll(gson.fromJson<List<Pill>>(sharedPreferences.getString(CHECKED_PILLS, null), object : TypeToken<List<Pill>>() {}.type))
        if (!checkedPills.isNullOrEmpty())
            pill_tablet.pills = checkedPills
    }

    private fun getUserInput() {
        val monthLabel = Calendar.getInstance().get(Calendar.MONTH)
        val firstDay = sharedPreferences.getString(monthLabel.toString(), null)
        if (!firstDay.isNullOrBlank()) {
            treatment_mens_txt.setText(firstDay)
        }
    }

    private fun calculateElapsedTime() {
        var today = Date(-1L)
        if (!treatment_mens_txt.text.isNullOrBlank())
            today = parseStringInDate(treatment_mens_txt.text.toString())
        if (today != Date(-1L)) {
            var elapsedTime = System.currentTimeMillis() - today.time
            elapsedTime /= (24 * 60 * 60 * 1000)
            pill_tablet.setupTablet(elapsedTime.toInt())
        }
    }

    private fun openDatePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            updateMenstruationDate()
        }
        context?.let {
            DatePickerDialog(
                it, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateMenstruationDate() {
        treatment_mens_txt.setText(formatDateWithYear(calendar.time))
        if (!treatment_mens_txt.text.isNullOrBlank()) {
            val monthLabel = with(Calendar.getInstance()) {
                get(Calendar.MONTH)
            }
            sharedPreferences.edit()
                .putString(monthLabel.toString(), treatment_mens_txt.text.toString())
                .apply()
        }
        calculateElapsedTime()
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.edit().putString(CHECKED_PILLS, gson.toJson(pill_tablet.pills)).apply()
    }

    private fun setupClickListener() {
        treatment_mens_txt.setOnClickListener {
            openDatePicker()
        }
    }

}
