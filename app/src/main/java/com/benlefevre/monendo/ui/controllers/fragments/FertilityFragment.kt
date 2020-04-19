package com.benlefevre.monendo.ui.controllers.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Temperature
import com.benlefevre.monendo.ui.viewmodels.FertilityViewModel
import com.benlefevre.monendo.utils.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_fertility.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class FertilityFragment : Fragment(R.layout.fragment_fertility) {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mensDay: TextInputEditText
    private lateinit var durationMens: TextInputEditText
    private val mensDates: MutableList<String> by lazy { mutableListOf<String>() }
    private val ovulDays: MutableList<String> by lazy { mutableListOf<String>() }
    private val fertiPeriods: MutableList<String> by lazy { mutableListOf<String>() }
    private val temperatures: MutableList<Temperature> by lazy { mutableListOf<Temperature>() }
    var calendar: Calendar = Calendar.getInstance()
    private var monthLabel: Int = -1

    private val viewModel: FertilityViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        initViews()
        getUserInput()
        getUserTemperature()
        initCalendar()
        setListener()
    }

    private fun initViews() {
        mensDay = fertility_day_mens_txt
        durationMens = fertility_duration_mens_txt
    }

    /**
     * Fetches the user's input in SharedPreferences and binds them into the right field if the
     * returned value is not null
     */
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

    /**
     * Pass the correct data to the FertilityCalendar to draw each defined period
     */
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
        if (day != Date(-1L)) {
            mensDates.add(formatDateWithYear(day))
            val calendar = Calendar.getInstance().apply {
                time = day
                add(Calendar.DAY_OF_YEAR, durationMens.text.toString().toInt())
            }
            day = calendar.time
            mensDates.add(formatDateWithYear(day))
            sharedPreferences.edit()
                .putString((if (monthLabelTemp + 1 == 12) 0 else monthLabelTemp + 1).toString(), formatDateWithYear(day))
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
                day = with(calendar) {
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

    /**
     * Opens a custom dialog to show all the right dates according to the user's item click
     */
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
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    /**
     * Fetches all the temperatures saved in Database to display them into a chart.
     */
    private fun getUserTemperature() {
        viewModel.getAllTemperatures().observe(viewLifecycleOwner, Observer {
            temperatures.clear()
            temperatures.addAll(it)
            initTempChart()
        })
    }

    /**
     * Sets the temperatures chart with the correct values
     */
    private fun initTempChart() {
        Timber.i("$temperatures")
        val entries = mutableListOf<Entry>()
        val dates = mutableListOf<String>()
        var index = 0f

        temperatures.forEach {
            entries.add(Entry(index, it.value))
            dates.add(formatDateWithoutYear(it.date))
            index++
        }

        val lineDataSet = LineDataSet(entries, getString(R.string.my_body_temp)).apply {
            lineWidth = 2f
            setCircleColor(getColor(requireContext(), R.color.colorSecondary))
            color = getColor(requireContext(), R.color.colorSecondary)
            setDrawValues(false)
        }

        fertility_temp_chart.apply {
            description = null
            setDrawBorders(false)
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 0.2f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMaximum = 40f
            axisLeft.axisMinimum = 36f
            axisRight.isEnabled = false
            data = LineData(lineDataSet)
            animateX(900, Easing.EaseOutBack)
        }
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

        fertility_temp_save_btn.setOnClickListener {
            viewModel.createTemp(Temperature(fertility_temp_slider.value, Date()))
        }
    }

}
