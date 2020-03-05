package com.benlefevre.monendo.ui.controllers.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.PainWithRelations
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.utils.formatDate
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.chipgroup_duration.*
import kotlinx.android.synthetic.main.fragment_activities_detail.*

class ActivitiesDetailFragment : Fragment(R.layout.fragment_activities_detail) {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            Injection.providerViewModelFactory(requireActivity().applicationContext)
        ).get(DashboardViewModel::class.java)
    }
    private val painRelations = mutableListOf<PainWithRelations>()
    private val activities = mutableListOf<UserActivities>()
    private val dates = mutableListOf<String>()
    private lateinit var colorsChart: IntArray
    private lateinit var selectedActivity: String

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
            dates.add(formatDate(it.pain.date))
        }
    }

    private fun clearList() {
        painRelations.clear()
        activities.clear()
        dates.clear()
    }

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
//                    Log.i("benoit","${e.x} + ${e.y} + ${h.x}")
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
            invalidate()
        }
    }

    private fun setupDetailsChart(name: String) {
        val painEntries = mutableListOf<Entry>()
        val activitiesEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach {
            painEntries.add(Entry(index, it.pain.intensity.toFloat()))
            it.userActivities.forEach { activity ->
                when {
                    activity.name == name ->
                        activitiesEntries.add(BarEntry(index, activity.intensity.toFloat(), activity))
                    name == getString(R.string.sport) && resources.getStringArray(R.array.sport)
                        .contains(activity.name) ->
                        activitiesEntries.add(BarEntry(index, activity.intensity.toFloat(), activity))
                    name == getString(R.string.sleep) ->
                        return
                    activity.name.contains(name) ->
                        activitiesEntries.add(BarEntry(index, activity.intensity.toFloat(), activity))
                }
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
                    Toast.makeText(context, "${activity.name} during ${activity.duration} with intensity of ${activity.intensity}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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
            invalidate()
        }
    }

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
            it.name != getString(R.string.stress) || it.name != getString(R.string.relaxation) ||
                    it.name != getString(R.string.sex) || it.name != getString(R.string.sleep) ||
                    !resources.getStringArray(R.array.sport).contains(it.name)
        }.toFloat() / size, getString(R.string.other))

        return arrayOf(sport, stress, sex, relaxation, other)
    }
}
