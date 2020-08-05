package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
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
import kotlinx.android.synthetic.main.fragment_symptom_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SymptomDetailFragment : Fragment(R.layout.fragment_symptom_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private lateinit var colorsChart: IntArray
    private val painRelations = mutableListOf<PainWithRelations>()
    private val symptoms = mutableListOf<Symptom>()
    private val dates = mutableListOf<String>()
    private lateinit var selectedSymptom: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        setupChipListener()
        symptom_details_evo_chart.apply {
            setNoDataText(getString(R.string.click_on_a_pie_chart_s_value_to_see_the_symptom_evolution_in_time))
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
                viewModel.getPainRelationsBy7LastDays().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    displaySymptomDetails2()
//                    if (!symptom_details_evo_chart.isEmpty)
//                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    displaySymptomDetails2()
//                    if (!symptom_details_evo_chart.isEmpty)
//                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    displaySymptomDetails2()
//                    if (!symptom_details_evo_chart.isEmpty)
//                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    displaySymptomDetails2()
//                    if (!symptom_details_evo_chart.isEmpty)
//                        displaySymptomDetails(selectedSymptom)
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
            symptoms.addAll(it.symptoms)
        }
    }

    private fun clearList() {
        painRelations.clear()
        symptoms.clear()
        dates.clear()
    }

    /**
     * Configures the pie chart with the right value and defines the on value clickListener
     * behavior.
     */
    private fun setupRepartitionChart() {
        val symptomsRep = calculateSymptomRepartition()
        val entries = mutableListOf<PieEntry>()
        val pieColors = mutableListOf<Int>()
        var colorCounter = 0

        symptomsRep.forEach {
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

        symptom_details_rep_chart.apply {
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
//            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                override fun onNothingSelected() {
//                }
//
//                override fun onValueSelected(e: Entry, h: Highlight?) {
//                    selectedSymptom = e.data.toString()
//                    displaySymptomDetails(e.data.toString())
//                }
//            })
            description = null
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            legend.isWordWrapEnabled = true
            data = PieData(pieDataSet)
            animateX(500, Easing.EaseOutCirc)
        }
    }

    /**
     * Computes the division in percents of each symptom vs the number of symptoms
     */
    private fun calculateSymptomRepartition(): Array<Pair<Float, String>> {
        val size = symptoms.size.toFloat()
        val burns =
            Pair(symptoms.count { it.name == getString(R.string.burns) }
                .toFloat() / size, getString(R.string.burns))
        val cramps =
            Pair(symptoms.count { it.name == getString(R.string.cramps) }
                .toFloat() / size, getString(R.string.cramps))
        val bleeding =
            Pair(symptoms.count { it.name == getString(R.string.bleeding) }
                .toFloat() / size, getString(R.string.bleeding))
        val chills =
            Pair(symptoms.count { it.name == getString(R.string.chills) }
                .toFloat() / size, getString(R.string.chills))
        val fever =
            Pair(symptoms.count { it.name == getString(R.string.fever) }
                .toFloat() / size, getString(R.string.fever))
        val bloating =
            Pair(symptoms.count { it.name == getString(R.string.bloating) }
                .toFloat() / size, getString(R.string.bloating))
        val hotFlush =
            Pair(symptoms.count { it.name == getString(R.string.hot_flush) }
                .toFloat() / size, getString(R.string.hot_flush))
        val diarrhea =
            Pair(symptoms.count { it.name == getString(R.string.diarrhea) }
                .toFloat() / size, getString(R.string.diarrhea))
        val constipation =
            Pair(symptoms.count { it.name == getString(R.string.constipation) }
                .toFloat() / size, getString(R.string.constipation))
        val nausea =
            Pair(symptoms.count { it.name == getString(R.string.nausea) }
                .toFloat() / size, getString(R.string.nausea))
        val tired =
            Pair(symptoms.count { it.name == getString(R.string.tired) }
                .toFloat() / size, getString(R.string.tired))

        return arrayOf(
            burns, cramps, bleeding, chills, fever, bloating, hotFlush, diarrhea,
            constipation, nausea, tired
        )
    }

    private fun displaySymptomDetails2() {
        val dataSetList: MutableList<IScatterDataSet> = mutableListOf()
        val painEntries = mutableListOf<Entry>()
        val burnsEntries = Pair(mutableListOf<Entry>(), getString(R.string.burns))
        val crampsEntries = Pair(mutableListOf<Entry>(), getString(R.string.cramps))
        val bleedingEntries = Pair(mutableListOf<Entry>(), getString(R.string.bleeding))
        val chillsEntries = Pair(mutableListOf<Entry>(), getString(R.string.chills))
        val feverEntries = Pair(mutableListOf<Entry>(), getString(R.string.fever))
        val bloatingEntries = Pair(mutableListOf<Entry>(), getString(R.string.bloating))
        val hotFlushEntries = Pair(mutableListOf<Entry>(), getString(R.string.hot_flush))
        val diarrheaEntries = Pair(mutableListOf<Entry>(), getString(R.string.diarrhea))
        val constipationEntries = Pair(mutableListOf<Entry>(), getString(R.string.constipation))
        val nauseaEntries = Pair(mutableListOf<Entry>(), getString(R.string.nausea))
        val tiredEntries = Pair(mutableListOf<Entry>(), getString(R.string.tired))
        var index = 0f

        painRelations.forEach { pain ->
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            if (!pain.symptoms.isNullOrEmpty()) {
                var yValue = when(pain.pain.intensity){
//                    0 -> 0.5f
                    in 0..10 -> pain.pain.intensity - (0.5f * pain.symptoms.size)
//                    10 -> 7.5f
                    else -> 0.0f
                }
                pain.symptoms.forEach {
                    when (it.name) {
                        getString(R.string.burns) -> burnsEntries.first.add(Entry(index, yValue))
                        getString(R.string.cramps) -> crampsEntries.first.add(Entry(index, yValue))
                        getString(R.string.bleeding) -> bleedingEntries.first.add(Entry(index, yValue))
                        getString(R.string.chills) -> chillsEntries.first.add(Entry(index, yValue))
                        getString(R.string.fever) -> feverEntries.first.add(Entry(index, yValue))
                        getString(R.string.bloating) -> bloatingEntries.first.add(Entry(index, yValue))
                        getString(R.string.hot_flush) -> hotFlushEntries.first.add(Entry(index, yValue))
                        getString(R.string.diarrhea) -> diarrheaEntries.first.add(Entry(index, yValue))
                        getString(R.string.constipation) -> constipationEntries.first.add(Entry(index, yValue))
                        getString(R.string.nausea) -> nauseaEntries.first.add(Entry(index, yValue))
                        getString(R.string.tired) -> tiredEntries.first.add(Entry(index, yValue))
                        else -> getColor(requireContext(), R.color.colorSecondary)
                    }
                    yValue += 0.7f
                }
            }
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
//            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.design_default_color_secondary)
//            valueTextSize = 10f
//            valueFormatter = object : ValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return (value.toInt()).toString()
//                }
//            }
        }

        val listLineSet =
            listOf(burnsEntries, crampsEntries, bleedingEntries, chillsEntries, feverEntries, bloatingEntries, hotFlushEntries, diarrheaEntries, constipationEntries, nauseaEntries, tiredEntries)
        listLineSet.forEach {
            val dataSet = ScatterDataSet(it.first,it.second).apply {
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawValues(false)
                scatterShapeSize = 30f
//                scatterShapeHoleRadius = 2f
//                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                color = when(it.second){
                    getString(R.string.burns) -> colorsChart[0]
                    getString(R.string.cramps) -> colorsChart[1]
                    getString(R.string.bleeding) -> colorsChart[2]
                    getString(R.string.chills) -> colorsChart[3]
                    getString(R.string.fever) -> colorsChart[4]
                    getString(R.string.bloating) -> colorsChart[5]
                    getString(R.string.hot_flush) -> colorsChart[6]
                    getString(R.string.diarrhea) -> colorsChart[7]
                    getString(R.string.constipation) -> colorsChart[8]
                    getString(R.string.nausea) -> colorsChart[9]
                    getString(R.string.tired) -> colorsChart[10]
                    else -> getColor(requireContext(), R.color.colorSecondary)
                }
            }
            dataSetList.add(dataSet)
        }

        symptom_details_evo_chart.apply {
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
//            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                override fun onNothingSelected() {
//                }
//
//                override fun onValueSelected(e: Entry?, h: Highlight?) {
//                    val pain: PainWithRelations = e?.data as PainWithRelations
//                    Toast.makeText(context, getString(R.string.symptom_detailed_click,name,pain.pain.intensity,pain.pain.location), Toast.LENGTH_LONG)
//                        .show()
//                }
//            })
            highlightValues(null)
            description = null
            xAxis.apply {
                axisMinimum = -0.5f
                spaceMax = 0.5f
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
//                axisMinimum = -0.5f
//                axisMaximum = 11f
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisRight.apply {
                granularity = 1f
                axisMinimum = 0f
                axisMaximum = 1.25f
                isEnabled = false
            }
            data = CombinedData().apply {
                setData(ScatterData(dataSetList))
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
            }
            animateX(2000, Easing.EaseOutBack)
        }
    }

    /**
     * Configures a LineChart to see the selected symptom evolution in time
     */
    private fun displaySymptomDetails(name: String) {
        val painEntries = mutableListOf<Entry>()
        val symptomEntries = mutableListOf<BarEntry>()
        var index = 0f

        painRelations.forEach { pain ->
            val filterSymptom = pain.symptoms.filter { it.name == name }
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            if (!filterSymptom.isNullOrEmpty())
                symptomEntries.add(BarEntry(index, 1f, pain))
            index++
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.design_default_color_secondary))
            setDrawValues(false)
        }
        val symptomDataSet = BarDataSet(symptomEntries, name).apply {
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)
            color = when (name) {
                getString(R.string.burns) -> colorsChart[0]
                getString(R.string.cramps) -> colorsChart[1]
                getString(R.string.bleeding) -> colorsChart[2]
                getString(R.string.chills) -> colorsChart[3]
                getString(R.string.fever) -> colorsChart[4]
                getString(R.string.bloating) -> colorsChart[5]
                getString(R.string.hot_flush) -> colorsChart[6]
                getString(R.string.diarrhea) -> colorsChart[7]
                getString(R.string.constipation) -> colorsChart[8]
                getString(R.string.nausea) -> colorsChart[9]
                getString(R.string.tired) -> colorsChart[10]
                else -> getColor(requireContext(), R.color.colorSecondary)
            }
        }

        symptom_details_evo_chart.apply {
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val pain: PainWithRelations = e?.data as PainWithRelations
                    Toast.makeText(context, getString(R.string.symptom_detailed_click, name, pain.pain.intensity, pain.pain.location), Toast.LENGTH_LONG)
                        .show()
                }
            })
            highlightValues(null)
            description = null
            xAxis.apply {
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dates)
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                axisMinimum = 0f
                axisMaximum = 11f
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisRight.apply {
                granularity = 1f
                axisMinimum = 0f
                axisMaximum = 1.25f
                isEnabled = false
            }
            data = CombinedData().apply {
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
                setData(BarData(symptomDataSet))
            }
            animateX(2000, Easing.EaseOutBack)
        }
    }
}
