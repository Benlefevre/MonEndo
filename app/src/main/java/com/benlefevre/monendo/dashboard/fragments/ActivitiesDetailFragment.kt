package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_activities_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ActivitiesDetailFragment : Fragment(R.layout.fragment_activities_detail) {

    private val viewModel: DashboardViewModel by viewModel()
    private val painRelations = mutableListOf<PainWithRelations>()
    private val activities = mutableListOf<UserActivities>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray
    private var colorSecondary: Int = 0
    private var colorPrimary: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        colorSecondary = getColor(requireContext(), R.color.colorSecondary)
        colorPrimary = getColor(requireContext(), R.color.colorPrimary)
        setupEmptyChartsMessages()
        setupChipListener()
    }

    private fun setupEmptyChartsMessages() {
        activities_details_rep_chart.apply {
            setNoDataText(getString(R.string.click_on_a_pie_chart_value_to_see_the_evolution_of_pain_with_activities))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        activities_detail_chart.apply {
            data = null
            setNoDataText("No data for this period")
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
    }

    /**
     * Sets a checked listener on each chip to call the correct ViewModel's function
     * to fetch user's input in locale DB.
     */
    private fun setupChipListener() {
        chip_week.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsBy7LastDays()
                    .observe(viewLifecycleOwner, configuresObserver())
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth()
                    .observe(viewLifecycleOwner, configuresObserver())
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months()
                    .observe(viewLifecycleOwner, configuresObserver())
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear()
                    .observe(viewLifecycleOwner, configuresObserver())
            }
        }
        chip_week.isChecked = true
    }

    /**
     * Defines the called functions when the ViewModel return a new value
     */
    private fun configuresObserver(): Observer<List<PainWithRelations>> {
        return Observer<List<PainWithRelations>> {
            clearDetailChart()
            if (it.isNotEmpty()) {
                setupList(it)
                setupRepartitionChart()
                displayAllActivities()
                activities_detail_chart_details_text.text =
                    getString(R.string.click_on_a_value_to_see_the_detail_of_the_chosen_practiced_activity)
            }
        }
    }

    private fun setupList(pains: List<PainWithRelations>) {
        clearList()
        painRelations.addAll(pains)
        pains.forEach {
            activities.addAll(it.userActivities.filter { activity -> activity.name != getString(R.string.sleep) })
            dates.add(formatDateWithoutYear(it.pain.date))
        }
    }

    private fun clearList() {
        painRelations.clear()
        activities.clear()
        dates.clear()
    }

    /**
     * Clears the highlight and data values for each chart
     */
    private fun clearDetailChart() {
        activities_detail_chart.apply {
            data = null
            fitScreen()
        }
        activities_details_rep_chart.apply {
            highlightValues(null)
        }
        activities_detail_chart_details_text.text =
            getString(R.string.click_on_a_value_to_see_the_detail_of_the_chosen_practiced_activity)
    }

    /**
     * Configures the pie chart with the right value and defines the on value clickListener
     * behavior.
     */
    private fun setupRepartitionChart() {
        val activitiesRep = calculateActivitiesRepartition()
        val entries = mutableListOf<PieEntry>()
        val pieColors = mutableListOf<Int>()
        var colorCounter = 4

        activitiesRep.forEach {
            if (it.first != 0f) {
                entries.add(PieEntry(it.first, it.second, it.second))
                pieColors.add(colorsChart[colorCounter])
            }
            colorCounter++
        }

        val pieDataSet = PieDataSet(entries, "").apply {
            colors = pieColors
            valueFormatter = PercentFormatter()
            valueTextColor = getColor(requireContext(), R.color.colorOnPrimary)
            valueTextSize = 10f
        }

        activities_details_rep_chart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    Timber.i("nothing")
                    clearDetailChart()
                    displayAllActivities()
                }

                override fun onValueSelected(e: Entry, h: Highlight?) {
                    Timber.i("${e.data}")
                    displaySelectedUserActivityDetails(e.data.toString())
                    activities_detail_chart.fitScreen()
                }
            })
            legend.apply {
                textColor = colorPrimary
                isWordWrapEnabled = true
            }
            description = null
            isDrawHoleEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.isWordWrapEnabled = true
            data = PieData(pieDataSet)
            animateX(500, Easing.EaseOutCirc)
        }
    }

    /**
     * Configures a CombinedChart to see all activities vs pain evolution in time
     */
    private fun displayAllActivities() {
        val dataSetList: MutableList<IScatterDataSet> = mutableListOf()
        val painEntries = mutableListOf<Entry>()
        val sportEntries = Pair(mutableListOf<Entry>(), getString(R.string.sport))
        val sexEntries = Pair(mutableListOf<Entry>(), getString(R.string.sex))
        val relaxationEntries = Pair(mutableListOf<Entry>(), getString(R.string.relaxation))
        val stressEntries = Pair(mutableListOf<Entry>(), getString(R.string.stress))
        val otherEntries = Pair(mutableListOf<Entry>(), getString(R.string.other))
        var index = 0f

        painRelations.forEach { pain ->
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            val userActivities = pain.userActivities.filter { it.name != getString(R.string.sleep) }

            var yValue = when {
                pain.pain.intensity == 0 -> 0.4f
                pain.userActivities.size <= 2 && pain.pain.intensity == 1 -> pain.pain.intensity - (0.4f * userActivities.size)
                pain.userActivities.size > 2 && pain.pain.intensity == 1 -> pain.pain.intensity + 0.4f
                pain.userActivities.size > 3 && pain.pain.intensity in 2..3 -> pain.pain.intensity + 0.4f
                else -> pain.pain.intensity - (0.4f * userActivities.size)
            }

            userActivities.forEach { activity ->
                when {
                    resources.getStringArray(R.array.sport)
                        .contains(activity.name) || activity.name.contains(getString(R.string.sport)) -> sportEntries.first.add(
                        Entry(index, yValue)
                    )
                    activity.name == getString(R.string.sex) ->
                        sexEntries.first.add(Entry(index, yValue))
                    activity.name == getString(R.string.stress) ->
                        stressEntries.first.add(Entry(index, yValue))
                    activity.name == getString(R.string.relaxation) ->
                        relaxationEntries.first.add(Entry(index, yValue))
                    activity.name.contains(getString(R.string.other)) ->
                        otherEntries.first.add(Entry(index, yValue))
                }
                yValue += 0.5f
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.design_default_color_secondary)
        }

        val listLineSet =
            listOf(
                sportEntries,
                sexEntries,
                relaxationEntries,
                stressEntries,
                otherEntries
            )

        listLineSet.forEach {
            val dataSet = ScatterDataSet(it.first, it.second).apply {
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawValues(false)
                scatterShapeSize = 25f
                color = setScatterColor(it.second)
            }
            dataSetList.add(dataSet)
        }

        activities_detail_chart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    displayActivitiesInformations(h)
                }
            })
            legend.apply {
                textColor = colorPrimary
                isWordWrapEnabled = true
            }
            highlightValues(null)
            description = null
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                isGranularityEnabled = true
                textColor = colorPrimary
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                textColor = colorPrimary
            }
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(ScatterData(dataSetList))
            }
            xAxis.axisMaximum = dates.size.toFloat()
            animateX(2000, Easing.EaseOutBack)
        }

    }

    private fun displaySelectedUserActivityDetails(name: String) {
        Timber.i(name)
        val painEntries = mutableListOf<Entry>()
        val selectedActivityEntries = mutableListOf<Entry>()
        var index = 0f

        painRelations.forEach { pain ->
            val filterActivities = pain.userActivities.filter {
                it.name == name || it.name.contains(name) || it.name.contains(name)
            }
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            if (filterActivities.isNotEmpty()) {
                selectedActivityEntries.add(Entry(index, pain.pain.intensity + 0.4f))
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.design_default_color_secondary)
        }
        val dataSet = ScatterDataSet(selectedActivityEntries, name).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(false)
            scatterShapeSize = 25f
            color = setScatterColor(name)
        }

        activities_detail_chart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    displayActivitiesInformations(h)
                }
            })
            legend.apply {
                textColor = colorPrimary
                isWordWrapEnabled = true
            }
            highlightValues(null)
            description = null
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                isGranularityEnabled = true
                textColor = colorPrimary
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                textColor = colorPrimary
            }
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(ScatterData(dataSet))
            }
            xAxis.axisMaximum = dates.size.toFloat()
            animateX(2000, Easing.EaseOutBack)
        }
    }

    private fun setScatterColor(activityName: String): Int {
        return when (activityName) {
            getString(R.string.sport) -> colorsChart[4]
            getString(R.string.sex) -> colorsChart[6]
            getString(R.string.relaxation) -> colorsChart[7]
            getString(R.string.stress) -> colorsChart[5]
            getString(R.string.other) -> colorsChart[8]
            else -> getColor(requireContext(), R.color.colorSecondary)
        }
    }

    private fun displayActivitiesInformations(highLight: Highlight?) {
        Timber.i(" x : ${highLight?.x} / dataSet : ${highLight?.dataSetIndex} / dataIndex = ${highLight?.dataIndex} / x.toInt = $${highLight?.x?.toInt()} / y = ${highLight?.y}")
        var activitiesText =
            "${formatDateWithYear(painRelations[highLight?.x?.toInt()!!].pain.date)} \n"
        painRelations[highLight.x.toInt()].userActivities.filter { it.name != getString(R.string.sleep) }
            .forEach {
                activitiesText += when (it.name) {
                    getString(R.string.stress) ->
                        getString(
                            R.string.stress_detail_activity,
                            it.name.toUpperCase(),
                            it.intensity
                        ) + "\n"
                    else ->
                        getString(
                            R.string.other_detail_activity,
                            it.name.toUpperCase(),
                            it.duration,
                            it.intensity
                        ) + "\n"
                }
            }
        activities_detail_chart_details_text.text = activitiesText
    }

    /**
     * Computes the division in percents of each activity vs the number of practiced activities
     */
    private fun calculateActivitiesRepartition(): Array<Pair<Float, String>> {
        val size = activities.size.toFloat()
        val sport = Pair(activities.count {
            resources.getStringArray(R.array.sport)
                .contains(it.name) || it.name.contains(getString(R.string.sport))
        }.toFloat() / size, getString(R.string.sport))
        val stress =
            Pair(activities.count { it.name == getString(R.string.stress) }
                .toFloat() / size, getString(R.string.stress))
        val sex =
            Pair(activities.count { it.name == getString(R.string.sex) }
                .toFloat() / size, getString(R.string.sex))
        val relaxation =
            Pair(activities.count { it.name == getString(R.string.relaxation) }
                .toFloat() / size, getString(R.string.relaxation))
        val other = Pair(activities.count {
            it.name != getString(R.string.stress) && it.name != getString(R.string.relaxation) &&
                    it.name != getString(R.string.sex) && it.name != getString(R.string.sleep) &&
                    !resources.getStringArray(R.array.sport).contains(it.name)
        }.toFloat() / size, getString(R.string.other))

        return arrayOf(sport, stress, sex, relaxation, other)
    }
}

