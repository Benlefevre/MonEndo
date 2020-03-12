package com.benlefevre.monendo.ui.controllers.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.WorkManager
import com.benlefevre.monendo.R
import com.benlefevre.monendo.ui.customview.ContraceptiveTablet
import com.benlefevre.monendo.ui.customview.Pill
import com.benlefevre.monendo.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_treatment.*
import java.util.*

class TreatmentFragment : Fragment(R.layout.fragment_treatment) {

    private lateinit var notifHour: TextInputEditText
    private lateinit var dayMens: TextInputEditText
    private lateinit var pillTablet: ContraceptiveTablet

    private lateinit var sharedPreferences: SharedPreferences
    private val gson by lazy {
        Gson()
    }
    private val checkedPills: MutableList<Pill> by lazy {
        mutableListOf<Pill>()
    }
    private lateinit var calendar: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupClickListener()
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        calendar = Calendar.getInstance()
        getCheckedPills()
        getUserInput()
        calculateElapsedTime()
    }

    private fun initViews() {
        notifHour = treatment_mens_notif_txt
        dayMens = treatment_mens_txt
        pillTablet = pill_tablet
    }

    /**
     * Fetches a Json in SharedPreferences and sets the list of checked pills in the tablet
     */
    private fun getCheckedPills() {
        gson.fromJson<List<Pill>>(sharedPreferences.getString(CHECKED_PILLS, null),
                object : TypeToken<List<Pill>>() {}.type)?.let {
                checkedPills.addAll(it)
            }
        if (!checkedPills.isNullOrEmpty())
            pillTablet.pills = checkedPills
    }

    /**
     * Bind user's input saved in SharedPreferences into fields
     */
    private fun getUserInput() {
        val monthLabel = Calendar.getInstance().get(Calendar.MONTH)
        sharedPreferences.getString(monthLabel.toString(), null)?.let {
            dayMens.setText(it)
        }
        sharedPreferences.getString(PILL_HOUR_NOTIF, null)?.let {
            notifHour.setText(it)
        }
    }

    /**
     * Calculates the elapsed time between the first day of the menstruation and the current day
     */
    private fun calculateElapsedTime() {
        var today = Date(-1L)
        if (!treatment_mens_txt.text.isNullOrBlank())
            today = parseStringInDate(treatment_mens_txt.text.toString())
        if (today != Date(-1L)) {
            var elapsedTime = System.currentTimeMillis() - today.time
            elapsedTime /= (24 * 60 * 60 * 1000)
            pillTablet.setupTablet(elapsedTime.toInt())
        }
    }

    /**
     * Sets a OnDateSetListener with updateMenstruationDate() and shows a DatePickerDialog
     */
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

    /**
     * Fetches the date set in the DatePicker to bind it into the corresponding field and saves the
     * value into SharedPreferences.
     */
    private fun updateMenstruationDate() {
        dayMens.setText(formatDateWithYear(calendar.time))
        if (!dayMens.text.isNullOrBlank()) {
            val monthLabel = with(Calendar.getInstance()) {
                get(Calendar.MONTH)
            }
            sharedPreferences.edit()
                .putString(monthLabel.toString(), dayMens.text.toString())
                .apply()
        }
        calculateElapsedTime()
    }

    /**
     * Sets a OnTimeSetListener with updateNotifHour() ans shows a TimePickerDialog
     */
    private fun openTimePicker() {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            updateNotifHour()
        }
        context?.let { it ->
            TimePickerDialog(
                it, timeListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            ).apply {
                setButton(DialogInterface.BUTTON_NEUTRAL, "Delete the time") { _, _ ->
                    notifHour.setText("")
                    sharedPreferences.edit().remove(PILL_HOUR_NOTIF).apply()
                    WorkManager.getInstance(context).cancelAllWorkByTag(PILL_TAG)
                }
            }.show()
        }
    }

    /**
     * Fetches the chosen time by user, updates the corresponding field and saves the value in
     * SharedPreferences.
     * Calls configurePillNotification() to enqueue a request with WorkManager
     */
    private fun updateNotifHour() {
        notifHour.setText(formatTime(calendar.time))
        if (!notifHour.text.isNullOrBlank()) {
            sharedPreferences.edit().putString(PILL_HOUR_NOTIF, notifHour.text.toString())
                .apply()
            val data = Data.Builder().putString(TREATMENT, PILL_TAG).build()
            context?.let { configurePillNotification(it, data, notifHour.text.toString()) }
        }
    }

    override fun onPause() {
        super.onPause()
//        Save in SharedPreferences the checked pills
        sharedPreferences.edit().putString(CHECKED_PILLS, gson.toJson(pillTablet.pills)).apply()
    }

    private fun setupClickListener() {
        dayMens.setOnClickListener {
            openDatePicker()
        }
        notifHour.setOnClickListener {
            openTimePicker()
        }
    }

}
