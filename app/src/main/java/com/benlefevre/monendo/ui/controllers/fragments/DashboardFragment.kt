package com.benlefevre.monendo.ui.controllers.fragments


import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.Symptom
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            Injection.providerViewModelFactory(requireActivity().applicationContext)
        ).get(DashboardViewModel::class.java)
    }

    private val navController by lazy { findNavController() }

    private val pains: MutableList<Pain> = mutableListOf()
    private val symptoms: MutableList<Symptom> = mutableListOf()
    private val activities: MutableList<UserActivities> = mutableListOf()
    private val moods: MutableList<Mood> = mutableListOf()
    private val dates: MutableList<String> = mutableListOf()
    private lateinit var painChart: LineChart
    private lateinit var symptomsChart: BarChart
    private lateinit var activitiesChart: BarChart
    private lateinit var sleepChart: LineChart
    private lateinit var moodChart: PieChart

    private lateinit var colorsChart : IntArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        setupOnClickListener()
        initAllCharts()
        getLast7DaysUserInputs()
    }

    /**
     * Initializes the empty data text for each chart
     */
    private fun initAllCharts() {
        painChart = dashboard_chart_pain.apply {
            setNoDataText(getString(R.string.pain_history_appear))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        symptomsChart = dashboard_chart_symptom.apply {
            setNoDataText(getString(R.string.symptom_history_appear))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        activitiesChart = dashboard_chart_activities.apply {
            setNoDataText(getString(R.string.activities_history_appear))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        sleepChart = dashboard_chart_sleep.apply {
            setNoDataText(getString(R.string.sleep_quality_history_appear))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        moodChart = dashboard_chart_mood.apply {
            setNoDataText(getString(R.string.mood_history_appear))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
    }

    /**
     * Fetches the user's inputs from locale room db and add them into MutableList
     */
    private fun getLast7DaysUserInputs() {
        viewModel.getPainRelationsBy7LastDays().observe(viewLifecycleOwner, Observer { list ->
            clearLists()
            list.forEach {
                pains.add(it.pain)
                dates.add(formatDateWithoutYear(it.pain.date))
                symptoms.addAll(it.symptoms)
                activities.addAll(it.userActivities)
                moods.addAll(it.moods)
            }
            setupAllCharts()
        })
    }

    /**
     * Initializes charts if the fetched user's inputs in Db or not empty or null
     */
    private fun setupAllCharts() {
        if (!pains.isNullOrEmpty()) setupPainChart()
        if (!symptoms.isNullOrEmpty()) setupSymptomsChart()
        if (!activities.isNullOrEmpty()) setupActivitiesChart()
        if (!activities.filter { it.name == getString(R.string.sleep) }.isNullOrEmpty()) setupSleepChart()
        if (!moods.isNullOrEmpty()) setupMoodChart()
    }

    private fun clearLists() {
        pains.clear()
        dates.clear()
        symptoms.clear()
        activities.clear()
        moods.clear()
    }

    /**
     * Setup the line chart that represented the pain over the last 7 days
     */
    private fun setupPainChart() {
        var counter = 0
        val entries: MutableList<Entry> = mutableListOf()
        pains.forEach {
            entries.add(Entry(counter.toFloat(), it.intensity.toFloat()))
            counter++
        }

        val dataSet = LineDataSet(entries, getString(R.string.my_pain))
        with(dataSet) {
            lineWidth = 2.0f
            color = getColor(requireContext(), R.color.colorSecondary)
            setCircleColor(getColor(requireContext(), R.color.colorSecondary))
            setDrawValues(false)
        }

        painChart.apply {
            description = null
            setDrawBorders(false)
            setTouchEnabled(false)
            xAxis.granularity = 1.0f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1.0f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0.0f
            axisLeft.axisMaximum = 10.0f
            axisRight.isEnabled = false
            data = LineData(dataSet)
            invalidate()
        }
    }

    /**
     * Setup the bar chart that represented the recurrence of symptoms over the last 7 days
     */
    private fun setupSymptomsChart() {
        val dataSet: MutableList<IBarDataSet> = mutableListOf()
        var indexDataSet = 0.0f
        var colorCounter = 0

        val burns =
            Pair(symptoms.count { it.name == getString(R.string.burns) }.toFloat(), getString(R.string.burns))
        val cramps =
            Pair(symptoms.count { it.name == getString(R.string.cramps) }.toFloat(), getString(R.string.cramps))
        val bleeding =
            Pair(symptoms.count { it.name == getString(R.string.bleeding) }.toFloat(), getString(R.string.bleeding))
        val chills =
            Pair(symptoms.count { it.name == getString(R.string.chills) }.toFloat(), getString(R.string.chills))
        val fever =
            Pair(symptoms.count { it.name == getString(R.string.fever) }.toFloat(), getString(R.string.fever))
        val bloating =
            Pair(symptoms.count { it.name == getString(R.string.bloating) }.toFloat(), getString(R.string.bloating))
        val hotFlush =
            Pair(symptoms.count { it.name == getString(R.string.hot_flush) }.toFloat(), getString(R.string.hot_flush))
        val diarrhea =
            Pair(symptoms.count { it.name == getString(R.string.diarrhea) }.toFloat(), getString(R.string.diarrhea))
        val constipation =
            Pair(symptoms.count { it.name == getString(R.string.constipation) }.toFloat(), getString(R.string.constipation))
        val nausea =
            Pair(symptoms.count { it.name == getString(R.string.nausea) }.toFloat(), getString(R.string.nausea))
        val tired =
            Pair(symptoms.count { it.name == getString(R.string.tired) }.toFloat(), getString(R.string.tired))

        val symptomsCount =
            arrayOf(
                burns, cramps, bleeding, chills, fever, bloating, hotFlush, diarrhea,
                constipation, nausea, tired
            )

        symptomsCount.forEach {
            if (it.first != 0f) {
                val barSet = BarDataSet(listOf(BarEntry(indexDataSet, it.first)), it.second)
                barSet.apply {
                    color = colorsChart[colorCounter]
                    setDrawValues(false)
                }
                indexDataSet++
                dataSet.add(barSet)
            }
            colorCounter++
        }

        symptomsChart.apply {
            description = null
            setTouchEnabled(false)
            setFitBars(true)
            legend.isWordWrapEnabled = true
            xAxis.isEnabled = false
            axisLeft.granularity = 1.0f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0.0f
            axisRight.isEnabled = false
            data = BarData(dataSet)
            invalidate()
        }

    }

    /**
     * Counts the number of session for each practiced activity and calculates average data
     */
    private fun calculateActivitiesData(): Array<Pair<Triple<Float, Float, Float>, String>> {
        var stressNb = 0f
        var stressIntensity = 0f
        var sportDuration = 0f
        var sportIntensity = 0f
        var sportNb = 0f
        var relaxationNb = 0f
        var relaxationDuration = 0f
        var relaxationIntensity = 0f
        var sexNb = 0f
        var sexDuration = 0f
        var sexIntensity = 0f

        activities.forEach {
            when {
                resources.getStringArray(R.array.sport).contains(it.name) -> {
                    sportNb++
                    sportIntensity += it.intensity
                    sportDuration += it.duration
                }
                it.name == getString(R.string.stress) -> {
                    stressNb++
                    stressIntensity += it.intensity
                }
                it.name == getString(R.string.relaxation) -> {
                    relaxationNb++
                    relaxationDuration += it.duration
                    relaxationIntensity += it.intensity
                }
                it.name == getString(R.string.sex) -> {
                    sexNb++
                    sexDuration += it.duration
                    sexIntensity += it.intensity
                }
            }
        }
        val sportData =
            Pair(
                Triple(sportNb, sportDuration / sportNb, sportIntensity / sportNb),
                getString(R.string.sport)
            )
        val stressData =
            Pair(Triple(stressNb, 0f, stressIntensity / stressNb), getString(R.string.stress))
        val relaxationData =
            Pair(
                Triple(relaxationNb, relaxationDuration / relaxationNb, relaxationIntensity / relaxationNb),
                getString(R.string.relaxation)
            )
        val sexData =
            Pair(Triple(sexNb, sexDuration / sexNb, sexIntensity / sexNb), getString(R.string.sex))

        return arrayOf(sportData, stressData, relaxationData, sexData)
    }

    /**
     * Setup the multiBar chart that represented all practiced activities by user for the 7 last days
     */
    private fun setupActivitiesChart() {
        val dataSet: MutableList<IBarDataSet> = mutableListOf()
        var indexDataSet = 0.0f
        var colorCounter = 0
        val activitiesCount = calculateActivitiesData()

        activitiesCount.forEach {
            if (it.first.first != 0f) {
                val barSet =
                    BarDataSet(
                        listOf(BarEntry(indexDataSet, it.first.first)),
                        getString(R.string.activity_session, it.second)
                    )
                barSet.apply {
                    color = colorsChart[colorCounter]
                    axisDependency = YAxis.AxisDependency.RIGHT
                    valueTextSize = 10f
                }
                indexDataSet++
                colorCounter++
                dataSet.add(barSet)
                if (it.first.second != 0f) {
                    val barSetDuration =
                        BarDataSet(
                            listOf(BarEntry(indexDataSet, it.first.second)),
                            getString(R.string.activity_duration_chart, it.second)
                        )
                    barSetDuration.apply {
                        color = colorsChart[colorCounter]
                        axisDependency = YAxis.AxisDependency.LEFT
                        valueTextSize = 10f
                    }
                    indexDataSet++
                    colorCounter++
                    dataSet.add(barSetDuration)
                }
                if (it.first.third != 0f) {
                    val barSetIntensity =
                        BarDataSet(
                            listOf(BarEntry(indexDataSet, it.first.third)),
                            getString(R.string.activity_intensity_chart, it.second)
                        )
                    barSetIntensity.apply {
                        color = colorsChart[colorCounter]
                        axisDependency = YAxis.AxisDependency.RIGHT
                        valueTextSize = 10f
                    }
                    indexDataSet++
                    colorCounter++
                    dataSet.add(barSetIntensity)
                }
            }
        }

        activitiesChart.apply {
            description = null
            setTouchEnabled(false)
            setFitBars(true)
            legend.isWordWrapEnabled = true
            legend.textSize = 8f
            xAxis.isEnabled = false
            axisLeft.axisMinimum = 0f
            axisLeft.setDrawGridLines(false)
            axisLeft.granularity = 20f
            axisRight.setDrawGridLines(false)
            axisRight.axisMinimum = 0f
            axisRight.granularity = 1f
            data = BarData(dataSet)
            invalidate()
        }
    }

    /**
     * Setup the line chart that represented the sleep quality over last 7 days
     */
    private fun setupSleepChart() {
        var counter = 0f
        val entries: MutableList<Entry> = mutableListOf()
        dates.clear()

        for (userActivities in activities.filter { it.name == getString(R.string.sleep) }) {
            entries.add(Entry(counter, userActivities.intensity.toFloat()))
            dates.add(formatDateWithoutYear(userActivities.date))
            counter++
        }

        val dataSet = LineDataSet(entries, getString(R.string.sleep_quality))
        with(dataSet) {
            lineWidth = 2.0f
            color = getColor(requireContext(), R.color.graph2)
            setCircleColor(getColor(requireContext(), R.color.graph2))
            setDrawValues(false)
        }

        sleepChart.apply {
            description = null
            setTouchEnabled(false)
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false
            data = LineData(dataSet)
            invalidate()
        }
    }

    /**
     * Calculates the repartition of user's mood of 7 days
     */
    private fun calculateMoodData(): Array<Pair<Float, String>> {
        val sad =
            Pair(
                moods.count { it.value == getString(R.string.sad) }.toFloat() / moods.size.toFloat(),
                getString(R.string.sad)
            )
        val sick =
            Pair(
                moods.count { it.value == getString(R.string.sick) }.toFloat() / moods.size.toFloat(),
                getString(R.string.sick)
            )
        val irritated =
            Pair(
                moods.count { it.value == getString(R.string.irritated) }.toFloat() / moods.size.toFloat(),
                getString(R.string.irritated)
            )
        val happy =
            Pair(
                moods.count { it.value == getString(R.string.happy) }.toFloat() / moods.size.toFloat(),
                getString(R.string.happy)
            )
        val veryHappy =
            Pair(
                moods.count { it.value == getString(R.string.very_happy) }.toFloat() / moods.size.toFloat(),
                getString(R.string.very_happy)
            )

        return arrayOf(sad, sick, irritated, happy, veryHappy)
    }


    /**
     * Setup the pie chart that represented the mood repartition for over last 7 days
     */
    private fun setupMoodChart() {
        val entries: MutableList<PieEntry> = mutableListOf()
        val moodsPercent = calculateMoodData()
        val moodColor: MutableList<Int> = mutableListOf()

        moodsPercent.forEach {
            if (it.first != 0f) {
                entries.add(PieEntry(it.first, it.second))
                when (it.second) {
                    getString(R.string.sad) -> moodColor.add(colorsChart[2])
                    getString(R.string.sick) -> moodColor.add(colorsChart[6])
                    getString(R.string.irritated) -> moodColor.add(colorsChart[8])
                    getString(R.string.happy) -> moodColor.add(colorsChart[5])
                    getString(R.string.very_happy) -> moodColor.add(colorsChart[4])
                }
            }
        }

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            valueFormatter = PercentFormatter()
            valueTextColor = getColor(requireContext(), R.color.colorOnPrimary)
            valueTextSize = 10f
            colors = moodColor
        }

        moodChart.apply {
            description = null
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = false
            setTouchEnabled(false)
            legend.isWordWrapEnabled = true
            data = PieData(pieDataSet)
            invalidate()
        }
    }

    /**
     * Configures the NavController's destination when user clicks on a card.
     */
    private fun setupOnClickListener() {
        dashboard_fab.setOnClickListener {
            navController.navigate(R.id.painFragment)
        }
        dashboard_card_pain.setOnClickListener {
            navController.navigate(R.id.painDetailFragment)
        }
        dashboard_card_symptom.setOnClickListener {
            navController.navigate(R.id.symptomDetailFragment)
        }
        dashboard_card_activities.setOnClickListener {
            navController.navigate(R.id.activitiesDetailFragment)
        }
        dashboard_card_sleep.setOnClickListener {
            navController.navigate(R.id.sleepDetailFragment)
        }
        dashboard_card_mood.setOnClickListener {
            navController.navigate(R.id.moodDetailFragment)
        }
    }
}
