package com.benlefevre.monendo.ui.controllers.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.PainWithRelations
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDate
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_pain_detail.*

class PainDetailFragment : Fragment(R.layout.fragment_pain_detail) {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            Injection.providerViewModelFactory(requireActivity().applicationContext)
        ).get(DashboardViewModel::class.java)
    }

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
            dates.add(formatDate(it.pain.date))
            index++
        }

        val dataSet = LineDataSet(entries, getString(R.string.my_pain)).apply {
            lineWidth = 2f
            color = getColor(requireContext(), R.color.colorSecondary)
            setCircleColor(getColor(requireContext(), R.color.colorSecondary))
            setDrawValues(false)
        }

        painChart.apply {
            description = null
            setDrawBorders(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
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
            invalidate()
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

        pain_details_date_txt.text = formatDate(pain.pain.date)
        pain_details_value_txt.text = pain.pain.intensity.toString()
        pain_details_location_txt.text = pain.pain.location
        pain_details_mood_txt.text = pain.moods[0].value
        pain_details_symptom_txt.text = symptomText
        pain_details_activities_txt.text = activitiesText
    }
}
