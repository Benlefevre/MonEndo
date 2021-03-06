package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentSleepDetailBinding
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.benlefevre.monendo.utils.setupChipDurationListener
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.koin.androidx.viewmodel.ext.android.viewModel

class SleepDetailFragment : Fragment(R.layout.fragment_sleep_detail) {

    private val viewModel : DashboardViewModel by viewModel()

    private var _binding : FragmentSleepDetailBinding? = null
    private val binding get() = _binding!!
    private val painRelations = mutableListOf<PainWithRelations>()
    private val dates = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSleepDetailBinding.bind(view)
        setupCharts()
        viewModel.pains.observe(viewLifecycleOwner, configuresObserver())
        setupChipListener()
    }

    private fun setupCharts() {
        binding.sleepDetailsChart.apply {
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
            legend.apply {
                textColor = getColor(context,R.color.colorPrimary)
            }
            isScaleYEnabled = false
            isHighlightPerTapEnabled = false
            description = null
            xAxis.apply {
                granularity = 1f
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
        }
    }

    /**
     * Sets a checked listener on each chip to call the correct ViewModel's function
     * to fetch user's input in locale DB.
     */
    private fun setupChipListener() {
        setupChipDurationListener(binding.chipGroup.durationChipgroup, viewModel)
        binding.chipGroup.chipWeek.isChecked = true
    }

    /**
     * Defines the called functions when the ViewModel return a new value
     */
    private fun configuresObserver(): Observer<List<PainWithRelations>> {
        return Observer<List<PainWithRelations>> {
            clearChart()
            if (it.isNotEmpty()) {
                setupList(it)
                displaySleepIntoChart()
            }
        }
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

    private fun clearChart(){
        binding.sleepDetailsChart.apply {
            data = null
        }
    }

    /**
     * Configures a CombinedChart to see the sleep quality vs pain evolution in time
     */
    private fun displaySleepIntoChart(){
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
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.design_default_color_secondary)
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

        binding.sleepDetailsChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = LineData(listEntries)
            fitScreen()
            setVisibleXRangeMaximum(29.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
