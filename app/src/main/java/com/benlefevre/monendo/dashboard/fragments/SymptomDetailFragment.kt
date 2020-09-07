package com.benlefevre.monendo.dashboard.fragments

import android.os.Bundle
import android.view.View
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
import timber.log.Timber

class SymptomDetailFragment : Fragment(R.layout.fragment_symptom_detail) {

    private val viewModel: DashboardViewModel by viewModel()

    private lateinit var colorsChart: IntArray
    private val painRelations = mutableListOf<PainWithRelations>()
    private val symptoms = mutableListOf<Symptom>()
    private val dates = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        colorsChart = resources.getIntArray(R.array.chartColors)
        viewModel.pains.observe(viewLifecycleOwner, configuresObserver())
        setupEmptyChartsMessages()
        setupCharts()
        setupChipListener()
    }

    private fun setupEmptyChartsMessages() {
        symptom_details_evo_chart.apply {
            setNoDataText(getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        symptom_details_rep_chart.apply {
            data = null
            setNoDataText(context.getString(R.string.no_data_period))
            setNoDataTextColor(getColor(context, R.color.colorSecondary))
        }
        symptom_filter_legend.visibility = View.GONE
    }

    /**
     * Configures charts' style and defines their onValueClickListener's
     * behavior.
     */
    private fun setupCharts() {
        symptom_details_rep_chart.apply {
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    Timber.i("nothing")
                    clearDetailChart()
                    displayAllSymptomsDetails()
                }

                override fun onValueSelected(e: Entry, h: Highlight?) {
                    Timber.i("${e.data}")
                    displaySelectedSymptomDetails(e.data.toString())
                }
            })
            description = null
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            legend.isWordWrapEnabled = true
        }

        symptom_details_evo_chart.apply {
            legend.apply {
                textColor = getColor(context, R.color.colorPrimary)
                isWordWrapEnabled = true
            }
            isScaleYEnabled = false
            highlightValues(null)
            description = null
            xAxis.apply {
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisLeft.apply {
                granularity = 1f
                setDrawZeroLine(true)
                textColor = getColor(context, R.color.colorPrimary)
            }
            axisRight.apply {
                isEnabled = false
            }
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

    private fun configuresObserver(): Observer<List<PainWithRelations>> {
        return Observer<List<PainWithRelations>> {
            clearDetailChart()
            if (it.isNotEmpty()) {
                setupList(it)
                setupRepartitionChart()
                displayAllSymptomsDetails()
                symptom_filter_legend.visibility = View.VISIBLE
            } else {
                setupEmptyChartsMessages()
            }
        }
    }

    private fun setupList(pains: List<PainWithRelations>) {
        clearList()
        painRelations.addAll(pains)
        pains.forEach {
            dates.add(formatDateWithoutYear(it.pain.date))
            symptoms.addAll(it.symptoms)
        }
    }

    /**
     * Clears all the lists that contains charts' data
     */
    private fun clearList() {
        painRelations.clear()
        symptoms.clear()
        dates.clear()
    }

    /**
     * Clears the highlight and data values for each chart
     */
    private fun clearDetailChart() {
        symptom_details_evo_chart.apply {
            data = null
        }
        symptom_details_rep_chart.apply {
            highlightValues(null)
        }
    }

    /**
     * Configures the pie chart with the right value
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

    /**
     * Configures a CombinedChart to see all symptoms' evolution in time
     */
    private fun displayAllSymptomsDetails() {
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
                var yValue = when {
                    pain.pain.intensity == 0 -> 0.5f
                    pain.symptoms.size <= 2 && pain.pain.intensity == 1 -> pain.pain.intensity - (0.5f * pain.symptoms.size)
                    pain.symptoms.size > 2 && pain.pain.intensity == 1 -> pain.pain.intensity + 0.5f
                    pain.symptoms.size <= 3 && pain.pain.intensity in 2..3 -> pain.pain.intensity - (0.5f * pain.symptoms.size)
                    pain.symptoms.size > 3 && pain.pain.intensity in 2..3 -> pain.pain.intensity + 0.5f
                    pain.symptoms.size <= 6 && pain.pain.intensity in 4..7 -> pain.pain.intensity - (0.5f * pain.symptoms.size)
                    pain.symptoms.size > 6 && pain.pain.intensity in 4..7 -> pain.pain.intensity - (0.5f * pain.symptoms.size / 2)
                    pain.pain.intensity in 8..10 -> pain.pain.intensity - (0.5f * pain.symptoms.size)
                    else -> 0.5f
                }
                pain.symptoms.forEach {
                    when (it.name) {
                        getString(R.string.burns) -> burnsEntries.first.add(Entry(index, yValue))
                        getString(R.string.cramps) -> crampsEntries.first.add(Entry(index, yValue))
                        getString(R.string.bleeding) -> bleedingEntries.first.add(
                            Entry(
                                index,
                                yValue
                            )
                        )
                        getString(R.string.chills) -> chillsEntries.first.add(Entry(index, yValue))
                        getString(R.string.fever) -> feverEntries.first.add(Entry(index, yValue))
                        getString(R.string.bloating) -> bloatingEntries.first.add(
                            Entry(
                                index,
                                yValue
                            )
                        )
                        getString(R.string.hot_flush) -> hotFlushEntries.first.add(
                            Entry(
                                index,
                                yValue
                            )
                        )
                        getString(R.string.diarrhea) -> diarrheaEntries.first.add(
                            Entry(
                                index,
                                yValue
                            )
                        )
                        getString(R.string.constipation) -> constipationEntries.first.add(
                            Entry(
                                index,
                                yValue
                            )
                        )
                        getString(R.string.nausea) -> nauseaEntries.first.add(Entry(index, yValue))
                        getString(R.string.tired) -> tiredEntries.first.add(Entry(index, yValue))
                        else -> getColor(requireContext(), R.color.colorSecondary)
                    }
                    yValue += 0.5f
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
            setDrawFilled(true)
            fillColor = getColor(requireContext(), R.color.design_default_color_secondary)
        }

        val listLineSet =
            listOf(
                burnsEntries,
                crampsEntries,
                bleedingEntries,
                chillsEntries,
                feverEntries,
                bloatingEntries,
                hotFlushEntries,
                diarrheaEntries,
                constipationEntries,
                nauseaEntries,
                tiredEntries
            )
        listLineSet.forEach {
            val dataSet = ScatterDataSet(it.first, it.second).apply {
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawValues(false)
                scatterShapeSize = 25f
                color = setChartEntriesColors(it.second)
            }
            dataSetList.add(dataSet)
        }

        symptom_details_evo_chart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            data = CombinedData().apply {
                setData(ScatterData(dataSetList))
                setData(LineData(painDataSet)).apply {
                    isHighlightEnabled = false
                }
            }
            fitScreen()
            setVisibleXRangeMaximum(10.0f)
            moveViewToAnimated(data.xMax, 5F, YAxis.AxisDependency.RIGHT, 2000)
            animateX(1000, Easing.EaseOutBack)
        }
    }

    /**
     * Configures a CombinedChart to see the selected symptom's evolution in time
     */
    private fun displaySelectedSymptomDetails(name: String) {
        val painEntries = mutableListOf<Entry>()
        val selectedSymptomEntries = mutableListOf<Entry>()
        var index = 0f

        painRelations.forEach { pain ->
            val filterSymptom = pain.symptoms.filter { it.name == name }
            painEntries.add(Entry(index, pain.pain.intensity.toFloat()))
            if (!filterSymptom.isNullOrEmpty()) {
                selectedSymptomEntries.add(Entry(index, pain.pain.intensity + 0.5f))
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
        val dataSet = ScatterDataSet(selectedSymptomEntries, name).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawValues(false)
            scatterShapeSize = 25f
            color = setChartEntriesColors(name)
        }

        symptom_details_evo_chart.apply {
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
     * Sets the right Entry's color according to the symptom's name
     */
    private fun setChartEntriesColors(symptomName: String): Int {
        return when (symptomName) {
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
}
