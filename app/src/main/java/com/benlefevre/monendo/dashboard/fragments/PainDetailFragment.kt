package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_pain_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PainDetailFragment : Fragment(R.layout.fragment_pain_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private val painRelations: MutableList<PainWithRelations> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipListener()
    }

    /**
     * Sets a checked listener on each chip to call the correct ViewModel's function
     * to fetch user's input in locale DB.
     */
    private fun setupChipListener() {
        chip_week.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsBy7LastDays().observe(viewLifecycleOwner, Observer {
                    setupPainChart(it)
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupPainChart(it)
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupPainChart(it)
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupPainChart(it)
                })
            }
        }
        chip_week.isChecked = true
    }

    /**
     * Sets the line chart with the user pain and defines the user's value click behavior
     */
    private fun setupPainChart(pains: List<PainWithRelations>) {
        val painChart = pain_details_chart
        painRelations.clear()
        painRelations.addAll(pains)
        val entries: MutableList<Entry> = mutableListOf()
        val dates: MutableList<String> = mutableListOf()
        var index = 0f

        painRelations.forEach {
            entries.add(Entry(index, it.pain.intensity.toFloat()))
            dates.add(formatDateWithoutYear(it.pain.date))
            index++
        }

        val dataSet = LineDataSet(entries, getString(R.string.my_pain)).apply {
            lineWidth = 2f
            color = getColor(requireContext(), R.color.colorSecondary)
            setCircleColor(getColor(requireContext(), R.color.colorSecondary))
            setDrawValues(false)
        }

        painChart.apply {
            legend.apply {
                textColor = getColor(context,R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            description = null
            setDrawBorders(false)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dates)
                textColor = getColor(context,R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                axisMinimum = 0f
                axisMaximum = 11f
                textColor = getColor(context,R.color.colorPrimary)
            }
            axisRight.isEnabled = false
            data = LineData(dataSet)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null)
                        displaySelectedPain(e.x.toInt())
                }
            })
            animateX(500, Easing.EaseOutBack)
        }
    }

    /**
     * Binds the user's input into corresponding fields when user clicks on a value
     */
    private fun displaySelectedPain(x: Int) {
        val pain = painRelations[x]
        var symptomText = ""
        var activitiesText = ""

        pain.symptoms.forEach {
            symptomText += "${it.name}, "
        }

        pain.userActivities.forEach {
            activitiesText += when (it.name) {
                getString(R.string.sleep) ->
                    getString(R.string.sleep_detail_activity, it.name, it.intensity) + "\n"
                getString(R.string.stress) ->
                    getString(R.string.stress_detail_activity, it.name, it.intensity) + "\n"
                else ->
                    getString(R.string.other_detail_activity, it.name, it.duration, it.intensity) + "\n"
            }
        }

        pain_details_date_txt.text = formatDateWithoutYear(pain.pain.date)
        pain_details_value_txt.text = pain.pain.intensity.toString()
        pain_details_location_txt.text =
            if(!pain.pain.location.isBlank()) pain.pain.location else getString(R.string.not_registered)
        pain_details_mood_txt.text =
            if (!pain.moods.isNullOrEmpty()) pain.moods[0].value else getString(R.string.not_registered)
        pain_details_symptom_txt.text = symptomText
        pain_details_activities_txt.text = activitiesText
    }
}
