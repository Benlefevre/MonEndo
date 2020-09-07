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
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_mood_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoodDetailFragment : Fragment(R.layout.fragment_mood_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private val painRelations = mutableListOf<PainWithRelations>()
    private val moods = mutableListOf<Mood>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        viewModel.pains.observe(viewLifecycleOwner, configuresObserver())
        setupChipListener()
        setupCharts()
    }

    /**
     * Configures charts and the ClickListener's behavior
     */
    private fun setupCharts() {
        mood_details_evo_chart.apply {
            setNoDataText(getString(R.string.click_on_a_pie_chart_s_value_to_see_the_mood_s_evolution_in_time))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
            setVisibleXRange(7f, 15f)
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            highlightValues(null)
            isHighlightPerTapEnabled = false
            isHighlightFullBarEnabled = false
            description = null
            isScaleYEnabled = false
            xAxis.apply {
                granularity = 1f
                textColor = getColor(requireContext(), R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                axisMinimum = 0f
                axisMaximum = 11f
                isEnabled = true
                textColor = getColor(requireContext(), R.color.colorPrimary)
            }
            axisRight.apply {
                granularity = 1f
                axisMinimum = 0f
                axisMaximum = 1.45f
                isEnabled = false
            }
        }

        mood_details_rep_chart.apply {
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    clearCharts()
                    setupDetailChart()
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    displaySelectedMood(e?.data.toString())
                }
            })
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            description = null
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = false
            legend.isWordWrapEnabled = true
        }
    }

    /**
     * Sets a checked listener on each chip to call the correct ViewModel's function
     * to fetch user's input in locale DB.
     */
    private fun setupChipListener() {
        chip_week.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations7days()

            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations30days()

            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations180days()

            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainsRelations360days()

            }
        }
        chip_week.isChecked = true
    }

    /**
     * Defines the called functions when the ViewModel return a new value
     */
    private fun configuresObserver(): Observer<List<PainWithRelations>> {
        return Observer<List<PainWithRelations>> {
            clearCharts()
            if (it.isNotEmpty()) {
                setupList(it)
                setupRepartitionChart()
                setupDetailChart()
            }
        }
    }

    /**
     * Clears the highlight and data values for each chart
     */
    private fun clearCharts() {
        mood_details_rep_chart.highlightValue(null)
        mood_details_evo_chart.apply {
            data = null
        }
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
     * Configures the pie chart with the right value to display the repartition of the user's moods
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

        val pieDataSet = PieDataSet(moodEntries, "").apply {
            valueFormatter = PercentFormatter()
            valueTextColor = getColor(requireContext(), R.color.colorOnPrimary)
            valueTextSize = 10f
            colors = pieColors
        }

        mood_details_rep_chart.apply {
            data = PieData(pieDataSet)
            animateX(500, Easing.EaseOutCirc)
        }
    }

    /**
     * Configures the Combined chart with the right value to display the history of the selected mood
     */
    private fun displaySelectedMood(moodName: String) {
        val painEntries = mutableListOf<Entry>()
        val moodEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach { pain ->
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            pain.moods.filter { it.value == moodName }.forEach { _ ->
                when (moodName) {
                    getString(R.string.sad) -> moodEntries.add(BarEntry(index, 0.25f))
                    getString(R.string.sick) -> moodEntries.add(BarEntry(index, 0.5f))
                    getString(R.string.irritated) -> moodEntries.add(BarEntry(index, 0.75f))
                    getString(R.string.happy) -> moodEntries.add(BarEntry(index, 1f))
                    getString(R.string.very_happy) -> moodEntries.add(BarEntry(index, 1.25f))
                }
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(false)
        }

        val dataSet = BarDataSet(moodEntries, moodName).apply {
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)
            isHighlightEnabled = false
            color = setEntriesColors(moodName)
        }

        mood_details_evo_chart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(dataSet))
            }
            fitScreen()
            setVisibleXRangeMaximum(10.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }
    }

    /**
     * Configures a CombinedChart to see all moods vs pain evolution in time
     */
    private fun setupDetailChart() {
        val dataSetList: MutableList<IBarDataSet> = mutableListOf()
        val painEntries = mutableListOf<Entry>()
        val sadEntries = Pair(mutableListOf<BarEntry>(), getString(R.string.sad))
        val sickEntries = Pair(mutableListOf<BarEntry>(), getString(R.string.sick))
        val irritatedEntries = Pair(mutableListOf<BarEntry>(), getString(R.string.irritated))
        val happyEntries = Pair(mutableListOf<BarEntry>(), getString(R.string.happy))
        val veryHappyEntries = Pair(mutableListOf<BarEntry>(), getString(R.string.very_happy))
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index, it.pain.intensity.toFloat()))
            it.moods.forEach { mood ->
                when (mood.value) {
                    getString(R.string.sad) -> sadEntries.first.add(BarEntry(index, 0.25f))
                    getString(R.string.sick) -> sickEntries.first.add(BarEntry(index, 0.5f))
                    getString(R.string.irritated) -> irritatedEntries.first.add(
                        BarEntry(
                            index,
                            0.75f
                        )
                    )
                    getString(R.string.happy) -> happyEntries.first.add(BarEntry(index, 1f))
                    getString(R.string.very_happy) -> veryHappyEntries.first.add(
                        BarEntry(
                            index,
                            1.25f
                        )
                    )
                }
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(false)
        }

        val listEntries =
            listOf(sadEntries, sickEntries, irritatedEntries, happyEntries, veryHappyEntries)
        listEntries.forEach {
            val dataSet = BarDataSet(it.first, it.second).apply {
                axisDependency = YAxis.AxisDependency.RIGHT
                setDrawValues(false)
                color = setEntriesColors(it.second)
            }
            dataSetList.add(dataSet)
        }

        mood_details_evo_chart.apply {
            data = CombinedData().apply {
                xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(dataSetList))
            }
            fitScreen()
            setVisibleXRangeMaximum(10.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }
    }

    /**
     * Defines the colors of the chart's entries according to the mood's value
     */
    private fun setEntriesColors(moodName: String): Int {
        return when (moodName) {
            getString(R.string.sad) -> colorsChart[2]
            getString(R.string.sick) -> colorsChart[6]
            getString(R.string.irritated) -> colorsChart[8]
            getString(R.string.happy) -> colorsChart[5]
            getString(R.string.very_happy) -> colorsChart[4]
            else -> getColor(requireContext(), R.color.graph2)
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
