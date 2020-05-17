package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_mood_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        mood_details_evo_chart.apply {
            setNoDataText(getString(R.string.click_on_a_pie_chart_s_value_to_see_the_mood_s_evolution_in_time))
            setNoDataTextColor(getColor(context,R.color.colorSecondary))
        }
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
                    setupDetailChart()
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    setupDetailChart()
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    setupDetailChart()
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    setupDetailChart()
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
            selectionShift = 0f
        }

        mood_details_rep_chart.apply {
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
     * Configures a CombinedChart to see moods vs pain evolution in time
     */
    private fun setupDetailChart(){
        val dataSetList: MutableList<IBarDataSet> = mutableListOf()
        val painEntries = mutableListOf<Entry>()
        val sadEntries = Pair(mutableListOf<BarEntry>(),getString(R.string.sad))
        val sickEntries = Pair(mutableListOf<BarEntry>(),getString(R.string.sick))
        val irritatedEntries = Pair(mutableListOf<BarEntry>(),getString(R.string.irritated))
        val happyEntries = Pair(mutableListOf<BarEntry>(),getString(R.string.happy))
        val veryHappyEntries = Pair(mutableListOf<BarEntry>(),getString(R.string.very_happy))
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index, it.pain.intensity.toFloat()))
            it.moods.forEach { mood ->
                when(mood.value){
                    getString(R.string.sad) -> sadEntries.first.add(BarEntry(index,0.25f))
                    getString(R.string.sick) -> sickEntries.first.add(BarEntry(index,0.5f))
                    getString(R.string.irritated) -> irritatedEntries.first.add(BarEntry(index,0.75f))
                    getString(R.string.happy) -> happyEntries.first.add(BarEntry(index,1f))
                    getString(R.string.very_happy) -> veryHappyEntries.first.add(BarEntry(index,1.25f))
                }
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(true)
            valueTextSize = 10f
            valueFormatter = object: ValueFormatter(){
                override fun getFormattedValue(value: Float): String {
                    return (value.toInt()).toString()
                }
            }
        }

        val listEntries = listOf(sadEntries,sickEntries,irritatedEntries,happyEntries,veryHappyEntries)
        listEntries.forEach {
            val dataSet = BarDataSet(it.first,it.second).apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                setDrawValues(false)
                color = when(it.second){
                    getString(R.string.sad) -> colorsChart[2]
                    getString(R.string.sick) -> colorsChart[6]
                    getString(R.string.irritated) -> colorsChart[8]
                    getString(R.string.happy) -> colorsChart[5]
                    getString(R.string.very_happy) -> colorsChart[4]
                    else -> getColor(requireContext(), R.color.graph2)
                }
            }
            dataSetList.add(dataSet)
        }

        mood_details_evo_chart.apply {
            legend.apply {
                textColor = getColor(context,R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            highlightValues(null)
            isHighlightPerTapEnabled = false
            isHighlightFullBarEnabled = false
            description = null
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            xAxis.textColor = getColor(requireContext(), R.color.colorPrimary)
            axisLeft.granularity = 1f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisLeft.isEnabled = false
            axisRight.granularity = 1f
            axisRight.axisMinimum = 0f
            axisRight.axisMaximum = 1.25f
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(dataSetList))
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
