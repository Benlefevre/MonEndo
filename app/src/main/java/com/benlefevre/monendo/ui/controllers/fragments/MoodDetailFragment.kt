package com.benlefevre.monendo.ui.controllers.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.PainWithRelations
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_mood_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MoodDetailFragment : Fragment(R.layout.fragment_mood_detail) {

    private val viewModel : DashboardViewModel by viewModel()

    private val painRelations = mutableListOf<PainWithRelations>()
    private val moods = mutableListOf<Mood>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray
    private lateinit var selectedMood: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
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
                    setupRepartitionChart()
                    if (!mood_details_evo_chart.isEmpty) {
                        setupDetailChart(selectedMood)
                    }
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!mood_details_evo_chart.isEmpty) {
                        setupDetailChart(selectedMood)
                    }
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!mood_details_evo_chart.isEmpty) {
                        setupDetailChart(selectedMood)
                    }
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!mood_details_evo_chart.isEmpty) {
                        setupDetailChart(selectedMood)
                    }
                })
            }
        }
        chip_week.isChecked = true
    }

    private fun setupList(pains: List<PainWithRelations>) {
        clearList()
        painRelations.addAll(pains)
        pains.forEach {
            dates.add(formatDateWithoutYear(it.pain.date))
            moods.addAll(it.moods)
        }
    }

    private fun clearList() {
        painRelations.clear()
        dates.clear()
        moods.clear()
    }

    /**
     * Configures the pie chart with the right value and defines the on value clickListener
     * behavior.
     */
    private fun setupRepartitionChart() {
        val moodRep = calculateMoodRepartition()
        val moodEntries = mutableListOf<PieEntry>()
        val pieColors = mutableListOf<Int>()

        moodRep.forEach {
            if (it.first != 0f) {
                moodEntries.add(PieEntry(it.first, it.second, it.second))
                when (it.second) {
                    getString(R.string.sad) -> pieColors.add(colorsChart[2])
                    getString(R.string.sick) -> pieColors.add(colorsChart[6])
                    getString(R.string.irritated) -> pieColors.add(colorsChart[8])
                    getString(R.string.happy) -> pieColors.add(colorsChart[5])
                    getString(R.string.very_happy) -> pieColors.add(colorsChart[4])
                }
            }
        }

        val pieDataSet = PieDataSet(moodEntries, "")
        pieDataSet.apply {
            valueFormatter = PercentFormatter()
            valueTextColor = getColor(requireContext(), R.color.colorOnPrimary)
            valueTextSize = 10f
            colors = pieColors
        }

        mood_details_rep_chart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    Timber.i(e?.data.toString())
                    selectedMood = e?.data.toString()
                    setupDetailChart(selectedMood)
                }

            })
            description = null
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = false
            legend.isWordWrapEnabled = true
            data = PieData(pieDataSet)
            animateX(500, Easing.EaseOutCirc)
        }
    }

    /**
     * Configures a CombinedChart to see the selected mood vs pain evolution in time
     */
    private fun setupDetailChart(name: String) {
        val painEntries = mutableListOf<Entry>()
        val moodEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index, it.pain.intensity.toFloat()))
            it.moods.forEach { mood ->
                if (mood.value == name)
                    moodEntries.add(BarEntry(index, 1f))
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.graph2))
            setDrawValues(false)
        }

        val moodDataSet = BarDataSet(moodEntries, name).apply {
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)
            color = when(name){
                getString(R.string.sad) -> colorsChart[2]
                getString(R.string.sick) -> colorsChart[6]
                getString(R.string.irritated) -> colorsChart[8]
                getString(R.string.happy) -> colorsChart[5]
                getString(R.string.very_happy) -> colorsChart[4]
                else -> getColor(requireContext(), R.color.graph2)
            }
        }

        mood_details_evo_chart.apply {
            description = null
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.granularity = 1f
            axisRight.axisMinimum = 0f
            axisRight.axisMaximum = 1.25f
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(moodDataSet))
            }
            animateX(2000,Easing.EaseOutBack)
        }
    }

    /**
     * Calculates the division in percents of each mood vs the number of moods
     */
    private fun calculateMoodRepartition(): Array<Pair<Float, String>> {
        val size = moods.size.toFloat()
        val sad = Pair(moods.count { it.value == getString(R.string.sad) }
            .toFloat() / size, getString(R.string.sad))
        val sick = Pair(moods.count { it.value == getString(R.string.sick) }
            .toFloat() / size, getString(R.string.sick))
        val irritated = Pair(moods.count { it.value == getString(R.string.irritated) }
            .toFloat() / size, getString(R.string.irritated))
        val happy = Pair(moods.count { it.value == getString(R.string.happy) }
            .toFloat() / size, getString(R.string.happy))
        val veryHappy = Pair(moods.count { it.value == getString(R.string.very_happy) }
            .toFloat() / size, getString(R.string.very_happy))

        return arrayOf(sad, sick, irritated, happy, veryHappy)
    }

}
