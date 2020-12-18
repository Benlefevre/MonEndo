package com.benlefevre.monendo.fertility.ui

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.FragmentFertilityBinding
import com.benlefevre.monendo.fertility.FertilityViewModel
import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.utils.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class FertilityFragment : Fragment(R.layout.fragment_fertility) {

    private val sharedPreferences: SharedPreferences by inject()
    private var _binding : FragmentFertilityBinding? = null
    private val binding get() = _binding!!
    private lateinit var mensDay: TextInputEditText
    private lateinit var durationMens: TextInputEditText
    private val mensDates: MutableList<String> by lazy { mutableListOf() }
    private val ovulDays: MutableList<String> by lazy { mutableListOf() }
    private val fertiPeriods: MutableList<String> by lazy { mutableListOf() }
    private val temperatures: MutableList<Temperature> by lazy { mutableListOf() }
    var calendar: Calendar = Calendar.getInstance()
    private var monthLabel: Int = -1

    private val viewModel: FertilityViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFertilityBinding.bind(view)
        initViews()
        getUserInput()
        getUserTemperature()
        initCalendar()
        setListener()
    }

    private fun initViews() {
        mensDay = binding.fertilityDayMensTxt
        durationMens = binding.fertilityDurationMensTxt
    }

    /**
     * Fetches the user's input in SharedPreferences and binds them into the right field if the
     * returned value is not null
     */
    private fun getUserInput() {
        monthLabel = calendar.get(Calendar.MONTH)
        val inputs = viewModel.getCorrectUserInput()
        if (inputs[0].isNotBlank()){
            mensDay.setText(inputs[0])
            durationMens.isFocusableInTouchMode = true
        }
        if (inputs[1].isNotBlank()){
            binding.fertilityDurationMensTxt.setText(inputs[1])
        }
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
            binding.fertilityCalendar.drawCycleInCalendar(firstDay, duration)
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
                .putString(NEXT_MENS, formatDateWithYear(day))
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
                .putString(CURRENT_MENS, mensDay.text.toString())
                .apply()
            durationMens.isFocusableInTouchMode = true
            binding.fertilityDayMensLabel.isErrorEnabled = false
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
                    "$string   ${getString(R.string.until)}  "
                else
                    "$string \n"
            }
        } else {
            list.forEach {
                message += (it + "\n")
            }
        }
        when (tag) {
            MENS -> title = getString(R.string.mens_dates)
            OVUL -> title = getString(R.string.ovul_days)
            FERTI -> title = getString(R.string.ferti_periods)

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
        viewModel.getAllTemperatures().observe(viewLifecycleOwner, {
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

        binding.fertilityTempChart.apply {
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
                    binding.fertilityDayMensLabel.error =
                        "Please enter the first day of your last menstruation"
                } else {
                    binding.fertilityDayMensLabel.isErrorEnabled = false
                }
            }
            addTextChangedListener {
                if (it.isNullOrBlank()) {
                    binding.fertilityCalendar.clearAllCalendarDays()
                } else {
                    if (it.toString().toInt() >= 10) {
                        sharedPreferences.edit()
                            .putString(DURATION, durationMens.text.toString())
                            .apply()
                        initCalendar()
                    }
                }
            }
        }

        binding.fertilityChipMens.setOnClickListener {
            if (!mensDates.isNullOrEmpty())
                openCycleDialog(mensDates, MENS)
        }

        binding.fertilityChipOvul.setOnClickListener {
            if (!ovulDays.isNullOrEmpty())
                openCycleDialog(ovulDays, OVUL)
        }

        binding.fertilityChipFerti.setOnClickListener {
            if (!fertiPeriods.isNullOrEmpty())
                openCycleDialog(fertiPeriods, FERTI)
        }

        binding.fertilityTempSaveBtn.setOnClickListener {
            viewModel.createTemp(
                Temperature(
                    binding.fertilityTempSlider.value,
                    Date()
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
