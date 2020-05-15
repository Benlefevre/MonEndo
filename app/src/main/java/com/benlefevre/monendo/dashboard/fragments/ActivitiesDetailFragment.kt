package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDateWithoutYear
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_activities_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActivitiesDetailFragment : Fragment(R.layout.fragment_activities_detail) {

    private val viewModel: DashboardViewModel by viewModel()
    private val painRelations = mutableListOf<PainWithRelations>()
    private val activities = mutableListOf<UserActivities>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray
    private lateinit var selectedActivity: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        setupChipListener()
        activities_detail_chart.apply {
            setNoDataText(getString(R.string.click_on_a_pie_chart_value_to_see_the_evolution_of_pain_with_activities))
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
                    if (!activities_detail_chart.isEmpty) {
                        setupDetailsChart(selectedActivity)
                    }
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!activities_detail_chart.isEmpty) {
                        setupDetailsChart(selectedActivity)
                    }
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!activities_detail_chart.isEmpty) {
                        setupDetailsChart(selectedActivity)
                    }
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!activities_detail_chart.isEmpty) {
                        setupDetailsChart(selectedActivity)
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
            activities.addAll(it.userActivities)
            dates.add(formatDateWithoutYear(it.pain.date))
        }
    }

    private fun clearList() {
        painRelations.clear()
        activities.clear()
        dates.clear()
    }

    /**
     * Configures the pie chart with the right value and defines the on value clickListener
     * behavior.
     */
    private fun setupRepartitionChart() {
        val activitiesRep = calculateActivitiesRepartition()
        val entries = mutableListOf<PieEntry>()
        val pieColors = mutableListOf<Int>()
        var colorCounter = 0

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
                }

                override fun onValueSelected(e: Entry, h: Highlight) {
                    selectedActivity = e.data.toString()
                    setupDetailsChart(e.data.toString())
                }

            })
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
     * Configures a CombinedChart to see the selected activity vs pain evolution in time
     */
    private fun setupDetailsChart(name: String) {
        val painEntries = mutableListOf<Entry>()
        val activitiesEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index, it.pain.intensity.toFloat()))
            it.userActivities.forEach { activity ->
                when {
                    activity.name == name ->
                        activitiesEntries.add(
                            BarEntry(
                                index,
                                activity.intensity.toFloat(),
                                activity
                            )
                        )
                    name == getString(R.string.sport) && resources.getStringArray(R.array.sport)
                        .contains(activity.name) ->
                        activitiesEntries.add(
                            BarEntry(
                                index,
                                activity.intensity.toFloat(),
                                activity
                            )
                        )
                    name == getString(R.string.sleep) ->
                        return
                    activity.name.contains(name) ->
                        activitiesEntries.add(
                            BarEntry(
                                index,
                                activity.intensity.toFloat(),
                                activity
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

        val activitiesDataSet = BarDataSet(activitiesEntries, name).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(false)
            color = when (name) {
                getString(R.string.sport) -> colorsChart[0]
                getString(R.string.stress) -> colorsChart[1]
                getString(R.string.sex) -> colorsChart[2]
                getString(R.string.relaxation) -> colorsChart[3]
                getString(R.string.other) -> colorsChart[4]
                else -> getColor(requireContext(), R.color.colorSecondary)
            }
        }

        activities_detail_chart.apply {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val activity: UserActivities = e?.data as UserActivities
                    Toast.makeText(
                        context,
                        getString(
                            R.string.other_detail_activity,
                            activity.name,
                            activity.duration,
                            activity.intensity
                        ),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
            highlightValues(null)
            description = null
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            axisLeft.granularity = 1f
            axisLeft.setDrawZeroLine(true)
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 10f
            axisRight.isEnabled = false
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(activitiesDataSet))
            }
            animateX(2000, Easing.EaseOutBack)
            invalidate()
        }
    }

    /**
     * Computes the division in percents of each activity vs the number of practiced activities
     */
    private fun calculateActivitiesRepartition(): Array<Pair<Float, String>> {
        val size = activities.size.toFloat()
        val sport = Pair(activities.count {
            resources.getStringArray(R.array.sport)
                .contains(it.name)
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
