package com.benlefevre.monendo.ui.controllers.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.PainWithRelations
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_sleep_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SleepDetailFragment : Fragment(R.layout.fragment_sleep_detail) {

    private val viewModel : DashboardViewModel by viewModel()

    private val painRelations = mutableListOf<PainWithRelations>()
    private val dates = mutableListOf<String>()

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
                    setupList(it)
                    setupSleepChart()
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupSleepChart()
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupSleepChart()
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupSleepChart()
                })
            }
        }
        chip_week.isChecked = true
    }

    private fun setupList(pains: List<PainWithRelations>) {
        clearList()
        painRelations.addAll(pains)
        pains.forEach { pain ->
            dates.add(formatDateWithoutYear(pain.pain.date))
        }
    }

    private fun clearList() {
        painRelations.clear()
        dates.clear()
    }

    /**
     * Configures a CombinedChart to see the sleep quality vs pain evolution in time
     */
    private fun setupSleepChart(){
        val painEntries = mutableListOf<Entry>()
        val sleepEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index,it.pain.intensity.toFloat()))
            it.userActivities.forEach {activity ->
                if (activity.name == getString(R.string.sleep))
                    sleepEntries.add(BarEntry(index,activity.intensity.toFloat()))
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries,getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.graph1))
            setDrawValues(false)
        }

        val sleepDataSet = BarDataSet(sleepEntries,getString(R.string.sleep_quality)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(false)
            color = getColor(requireContext(),R.color.graph2)
        }

        sleep_details_chart.apply {
            description = null
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet))
                setData(BarData(sleepDataSet))
            }
            invalidate()
        }
    }
}
