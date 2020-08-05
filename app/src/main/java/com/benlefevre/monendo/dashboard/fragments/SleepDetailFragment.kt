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
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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
        val sleepEntries = mutableListOf<Entry>()
        var index = 0f
        val listEntries = mutableListOf<ILineDataSet>()

        painRelations.forEach {
            painEntries.add(Entry(index,it.pain.intensity.toFloat()))
            it.userActivities.forEach {activity ->
                if (activity.name == getString(R.string.sleep))
                    sleepEntries.add(Entry(index,activity.intensity.toFloat()))
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

        val sleepDataSet = LineDataSet(sleepEntries,getString(R.string.sleep_quality)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            setDrawValues(false)
            color = getColor(requireContext(),R.color.graph2)
            setCircleColor(getColor(requireContext(),R.color.graph2))
        }

        listEntries.add(painDataSet)
        listEntries.add(sleepDataSet)

        sleep_details_chart.apply {
            legend.apply {
                textColor = getColor(context,R.color.colorPrimary)
            }
            isHighlightPerTapEnabled = false
            description = null
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                axisMinimum = 0f
                axisMaximum = 11f
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisRight.isEnabled = false
            data = LineData(listEntries)
            animateX(2000, Easing.EaseOutBack)
            invalidate()
        }
    }
}
