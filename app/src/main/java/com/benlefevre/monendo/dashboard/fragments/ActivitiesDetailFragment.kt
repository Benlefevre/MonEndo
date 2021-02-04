package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentActivitiesDetailBinding
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.benlefevre.monendo.utils.setupChipDurationListener
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class ActivitiesDetailFragment : Fragment(R.layout.fragment_activities_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private var _binding : FragmentActivitiesDetailBinding? = null
    @VisibleForTesting
    val binding get() = _binding!!
    private val painRelations = mutableListOf<PainWithRelations>()
    private val activities = mutableListOf<UserActivities>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray
    private var colorSecondary: Int = 0
    private var colorPrimary: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentActivitiesDetailBinding.bind(view)
        colorsChart = resources.getIntArray(R.array.chartColors)
        colorSecondary = getColor(requireContext(), R.color.colorSecondary)
        colorPrimary = getColor(requireContext(), R.color.colorPrimary)
        viewModel.pains.observe(viewLifecycleOwner, configuresObserver())
        setupEmptyChartsMessages()
        setupCharts()
        setupChipListener()
    }

    private fun setupCharts() {
        binding.activitiesDetailsRepChart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    clearDetailChart()
                    displayAllActivities()
                }

                override fun onValueSelected(e: Entry, h: Highlight?) {
                    displaySelectedUserActivityDetails(e.data.toString())
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
        }

        binding.activitiesDetailChart.apply {
            setVisibleXRange(7f, 15f)
            legend.apply {
                textColor = colorPrimary
                isWordWrapEnabled = true
            }
            highlightValues(null)
            description = null
            isScaleYEnabled = false
            xAxis.apply {
                granularity = 1f
                isGranularityEnabled = true
                textColor = colorPrimary
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                axisMinimum = 0.0f
                axisMaximum = 11.0f
                textColor = colorPrimary
            }
            axisRight.isEnabled = false
        }
    }

    private fun setupEmptyChartsMessages() {
        binding.activitiesDetailsRepChart.apply {
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        binding.activitiesDetailChart.apply {
            data = null
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
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
            clearDetailChart()
            if (it.isNotEmpty()) {
                setupList(it)
                setupRepartitionChart()
                displayAllActivities()
                binding.activitiesDetailChartDetailsText.text =
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
        binding.activitiesDetailChart.apply {
            data = null
        }
        binding.activitiesDetailsRepChart.apply {
            highlightValues(null)
        }
        binding.activitiesDetailChartDetailsText.text =
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

        binding.activitiesDetailsRepChart.apply {
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

        binding.activitiesDetailChart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
//                    There is nothing to do...
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    displayActivitiesInformations(h, "")
                }
            })
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(ScatterData(dataSetList))
            }
            fitScreen()
            setVisibleXRangeMaximum(10.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }

    }

    private fun displaySelectedUserActivityDetails(activityName: String) {
        Timber.i(activityName)
        val painEntries = mutableListOf<Entry>()
        val selectedActivityEntries = mutableListOf<Entry>()
        var index = 0f

        painRelations.forEach { pain ->
            val filterActivities = pain.userActivities.filter {
                it.name == activityName || it.name.contains(activityName)
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
        val dataSet = ScatterDataSet(selectedActivityEntries, activityName).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(false)
            scatterShapeSize = 25f
            color = setScatterColor(activityName)
        }

        binding.activitiesDetailChart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
//                    There is nothing to do...

                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    displayActivitiesInformations(h, activityName)
                }
            })
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(ScatterData(dataSet))
            }
            fitScreen()
            setVisibleXRangeMaximum(10.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }
    }

    /**
     * Sets the chart's entries' color according to the name of the practiced activity
     */
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


    private fun displayActivitiesInformations(highLight: Highlight?, activityName: String) {
        Timber.i(" x : ${highLight?.x} / dataSet : ${highLight?.dataSetIndex} / dataIndex = ${highLight?.dataIndex} / x.toInt = $${highLight?.x?.toInt()} / y = ${highLight?.y}")
        var activitiesText =
            "${formatDateWithYear(painRelations[highLight?.x?.toInt()!!].pain.date)} \n"
        painRelations[highLight.x.toInt()].userActivities.filter {
            when {
                activityName.isBlank() -> it.name != getString(R.string.sleep)
                else -> it.name == activityName || it.name.contains(activityName)
            }
        }
            .forEach {
                activitiesText += when (it.name) {
                    getString(R.string.stress) ->
                        getString(
                            R.string.stress_detail_activity,
                            it.name.toUpperCase(Locale.ROOT),
                            it.intensity
                        ) + "\n"
                    else ->
                        getString(
                            R.string.other_detail_activity,
                            it.name.toUpperCase(Locale.ROOT),
                            it.duration,
                            it.intensity
                        ) + "\n"
                }
            }
        binding.activitiesDetailChartDetailsText.text = activitiesText
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
                    it.name != getString(R.string.sex) && it.name != getString(R.string.sleep) && !it.name.contains(
                getString(R.string.sport)
            )
        }.toFloat() / size, getString(R.string.other))

        return arrayOf(sport, stress, sex, relaxation, other)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

