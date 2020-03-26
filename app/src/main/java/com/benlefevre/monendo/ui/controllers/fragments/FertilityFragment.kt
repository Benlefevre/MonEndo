package com.benlefevre.monendo.ui.controllers.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.benlefevre.monendo.R
import com.benlefevre.monendo.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_fertility.*
import java.util.*

class FertilityFragment : Fragment(R.layout.fragment_fertility) {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mensDay: TextInputEditText
    private lateinit var durationMens: TextInputEditText
    private val mensDates: MutableList<String> by lazy { mutableListOf<String>() }
    private val ovulDays: MutableList<String> by lazy { mutableListOf<String>() }
    private val fertiPeriods: MutableList<String> by lazy { mutableListOf<String>() }
    var calendar: Calendar = Calendar.getInstance()
    private var monthLabel: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        initViews()
        getUserInput()
        initCalendar()
        setListener()
    }

    private fun initViews() {
        mensDay = fertility_day_mens_txt
        durationMens = fertility_duration_mens_txt
    }

    private fun getUserInput() {
        monthLabel = calendar.get(Calendar.MONTH)
        sharedPreferences.getString(monthLabel.toString(), null)?.let {
            mensDay.setText(it)
            durationMens.isFocusableInTouchMode = true
        }
        sharedPreferences.getString(DURATION, null)?.let {
            durationMens.setText(it)
        }
        sharedPreferences.edit().remove("${monthLabel - 1}").apply()
    }

    private fun initCalendar() {
        if (!mensDay.text.isNullOrBlank() && !durationMens.text.isNullOrBlank()) {
            var firstDay = mensDay.text!!.substring(0, 2).toInt()
            val duration = durationMens.text.toString().toInt()
            if ((monthLabel + 1) != mensDay.text!!.substring(3, 5).toInt()) {
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                }
                firstDay = (firstDay + duration) - calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            fertility_calendar.drawCycleInCalendar(firstDay, duration)
            calculateAndSaveNextMens()
        }
    }

    private fun calculateAndSaveNextMens() {
        mensDates.clear()
        ovulDays.clear()
        fertiPeriods.clear()
        var monthLabelTemp = monthLabel
        var day = parseStringInDate(mensDay.text.toString())
        mensDates.add(formatDateWithYear(day))
        if (day != Date(-1L)) {
            val calendar = Calendar.getInstance().apply {
                time = day
                add(Calendar.DAY_OF_YEAR, durationMens.text.toString().toInt())
            }
            day = calendar.time
            mensDates.add(formatDateWithYear(day))
            sharedPreferences.edit()
                .putString((monthLabelTemp + 1).toString(), formatDateWithYear(day))
                .apply()

            while (monthLabelTemp != 11) {
                val ovulDay = with(calendar) {
                    add(Calendar.DAY_OF_YEAR, -14)
                    time
                }
                val fertiBegin = with(calendar) {
                    add(Calendar.DAY_OF_YEAR, -3)
                    time
                }
                val fertiEnd = with(calendar) {
                    add(Calendar.DAY_OF_MONTH, 4)
                    time
                }
                day = with(calendar){
                    time = day
                    add(Calendar.DAY_OF_YEAR, durationMens.text.toString().toInt())
                    time
                }
                mensDates.add(formatDateWithYear(day))
                ovulDays.add(formatDateWithYear(ovulDay))
                fertiPeriods.add(formatDateWithYear(fertiBegin))
                fertiPeriods.add(formatDateWithYear(fertiEnd))

                monthLabelTemp++
            }
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
        mensDay.setText(formatDateWithYear(calendar.time))
        initCalendar()
        if (!mensDay.text.isNullOrBlank()) {
            sharedPreferences.edit()
                .putString(monthLabel.toString(), mensDay.text.toString())
                .apply()
            durationMens.isFocusableInTouchMode = true
            fertility_day_mens_label.isErrorEnabled = false
        }
    }

    private fun openCycleDialog(list: List<String>, tag: String) {
        var title = ""
        var message = ""

        if (tag == FERTI) {
            for ((index, string) in list.withIndex()) {
                message += if (index % 2 == 0)
                    "$string   until   "
                else
                    "$string \n"
            }
        } else {
            list.forEach {
                message += (it + "\n")
            }
        }
        when (tag) {
            MENS -> title = "Your menstruation's dates"
            OVUL -> title = "Your ovulation's days"
            FERTI -> title = "Your fertilization's periods"

        }
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun setListener() {
        mensDay.setOnClickListener {
            openDatePicker()
        }

        durationMens.apply {
            setOnClickListener {
                if (mensDay.text.isNullOrBlank()) {
                    it.clearFocus()
                    fertility_day_mens_label.error =
                        "Please enter the first day of your last menstruation"
                } else {
                    fertility_day_mens_label.isErrorEnabled = false
                }
            }
            addTextChangedListener {
                if (it.isNullOrBlank()) {
                    fertility_calendar.clearAllCalendarDays()
                } else {
                    sharedPreferences.edit()
                        .putString(DURATION, durationMens.text.toString())
                        .apply()
                    initCalendar()
                }
            }
        }

        fertility_chip_mens.setOnClickListener {
            if (!mensDates.isNullOrEmpty())
                openCycleDialog(mensDates, MENS)
        }

        fertility_chip_ovul.setOnClickListener {
            if (!ovulDays.isNullOrEmpty())
                openCycleDialog(ovulDays, OVUL)
        }

        fertility_chip_ferti.setOnClickListener {
            if (!fertiPeriods.isNullOrEmpty())
                openCycleDialog(fertiPeriods, FERTI)
        }
    }

}
