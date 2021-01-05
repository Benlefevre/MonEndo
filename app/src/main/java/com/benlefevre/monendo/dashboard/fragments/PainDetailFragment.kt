package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentPainDetailBinding
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class PainDetailFragment : Fragment(R.layout.fragment_pain_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private var _binding : FragmentPainDetailBinding? = null
    private val binding get() = _binding!!
    private val painRelations: MutableList<PainWithRelations> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPainDetailBinding.bind(view)
        setupCharts()
        viewModel.pains.observe(viewLifecycleOwner, configuresObserver())
        setupChipListener()
    }

    private fun setupCharts() {
        binding.painDetailsChart.apply {
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            description = null
            setDrawBorders(false)
             xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
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
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null)
                        displaySelectedPain(e.x.toInt())
                }
            })
        }
    }

    private fun configuresObserver(): Observer<List<PainWithRelations>> {
        return Observer {
            clearCharts()
            if (it.isNotEmpty()) {
                displayAllPainsIntoChart(it)
            }
        }
    }

    /**
     * Clears the highlight and data values for each chart
     */
    private fun clearCharts() {
        binding.painDetailsChart.apply {
            data = null
        }
    }

    /**
     * Sets a checked listener on each chip to call the correct ViewModel's function
     * to fetch user's input in locale DB.
     */
    private fun setupChipListener() {
        binding.chipGroup.chipWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations7days()
            }
        }
        binding.chipGroup.chipMonth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations30days()
            }
        }
        binding.chipGroup.chip6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations180days()
            }
        }
        binding.chipGroup.chipYear.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations360days()
            }
        }
        binding.chipGroup.chipWeek.isChecked = true
    }

    /**
     * Sets the line chart with the user pain and defines the user's value click behavior
     */
    private fun displayAllPainsIntoChart(pains: List<PainWithRelations>) {
        val painChart = binding.painDetailsChart
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
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.colorSecondary)
        }

        painChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = LineData(dataSet)
            fitScreen()
            setVisibleXRangeMaximum(29.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
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
                    getString(
                        R.string.other_detail_activity,
                        it.name,
                        it.duration,
                        it.intensity
                    ) + "\n"
            }
        }

        binding.painDetailsDateTxt.text = formatDateWithoutYear(pain.pain.date)
        binding.painDetailsValueTxt.text = pain.pain.intensity.toString()
        binding.painDetailsLocationTxt.text =
            if (!pain.pain.location.isBlank()) pain.pain.location else getString(R.string.not_registered)
        binding.painDetailsMoodTxt.text =
            if (!pain.moods.isNullOrEmpty()) pain.moods[0].value else getString(R.string.not_registered)
        binding.painDetailsSymptomTxt.text = symptomText
        binding.painDetailsActivitiesTxt.text = activitiesText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
