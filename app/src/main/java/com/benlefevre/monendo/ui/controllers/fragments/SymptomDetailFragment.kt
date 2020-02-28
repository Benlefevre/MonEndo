package com.benlefevre.monendo.ui.controllers.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.PainWithRelations
import com.benlefevre.monendo.data.models.Symptom
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
import kotlinx.android.synthetic.main.fragment_symptom_detail.*

class SymptomDetailFragment : Fragment(R.layout.fragment_symptom_detail) {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            Injection.providerViewModelFactory(requireActivity().applicationContext)
        ).get(DashboardViewModel::class.java)
    }

    private val colors = arrayOf(
        R.color.colorSecondary, R.color.colorPrimary, R.color.colorBackground,
        R.color.colorPrimaryVariant, R.color.graph1, R.color.graph2, R.color.graph3, R.color.graph4,
        R.color.graph5, R.color.graph6, R.color.graph7
    )

    private val painRelations = mutableListOf<PainWithRelations>()
    private val symptoms = mutableListOf<Symptom>()
    private val painList = mutableListOf<Float>()
    private val dates = mutableListOf<String>()
    private var selectedSymptom = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    if (!symptom_details_evo_chart.isEmpty)
                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_month.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastMonth().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!symptom_details_evo_chart.isEmpty)
                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_6months.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLast6Months().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!symptom_details_evo_chart.isEmpty)
                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_year.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.getPainRelationsByLastYear().observe(viewLifecycleOwner, Observer {
                    setupList(it)
                    setupRepartitionChart()
                    if (!symptom_details_evo_chart.isEmpty)
                        displaySymptomDetails(selectedSymptom)
                })
            }
        }
        chip_week.isChecked = true
    }

    private fun setupList(pains: List<PainWithRelations>) {
        clearList()
        painRelations.addAll(pains)
        pains.forEach {
            painList.add(it.pain.intensity.toFloat())
            dates.add(formatDate(it.pain.date))
            symptoms.addAll(it.symptoms)
        }
    }

    private fun clearList() {
        painRelations.clear()
        painList.clear()
        symptoms.clear()
        dates.clear()
    }

    private fun setupRepartitionChart() {
        val symptomsRep = calculateSymptomRepartition()
        val entries = mutableListOf<PieEntry>()
        val pieColors = mutableListOf<Int>()
        var colorCounter = 0

        symptomsRep.forEach {
            if (it.first != 0f) {
                val pieEntry = PieEntry(it.first, it.second, it.second).apply {
                    pieColors.add(getColor(requireContext(), colors[colorCounter]))
                }
                entries.add(pieEntry)
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
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry, h: Highlight?) {
                    Log.i("benoit", "${e.data}")
                    selectedSymptom = e.data.toString()
                    displaySymptomDetails(e.data.toString())
                }
            })
            description = null
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            isDrawHoleEnabled = false
            legend.isWordWrapEnabled = true
            data = PieData(pieDataSet)
            invalidate()
        }
    }

    private fun calculateSymptomRepartition(): Array<Pair<Float, String>> {
        val size = symptoms.size.toFloat()
        val burns =
            Pair(symptoms.count { it.name == getString(R.string.burns) }.toFloat() / size, getString(R.string.burns))
        val cramps =
            Pair(symptoms.count { it.name == getString(R.string.cramps) }.toFloat() / size, getString(R.string.cramps))
        val bleeding =
            Pair(symptoms.count { it.name == getString(R.string.bleeding) }.toFloat() / size, getString(R.string.bleeding))
        val chills =
            Pair(symptoms.count { it.name == getString(R.string.chills) }.toFloat() / size, getString(R.string.chills))
        val fever =
            Pair(symptoms.count { it.name == getString(R.string.fever) }.toFloat() / size, getString(R.string.fever))
        val bloating =
            Pair(symptoms.count { it.name == getString(R.string.bloating) }.toFloat() / size, getString(R.string.bloating))
        val hotFlush =
            Pair(symptoms.count { it.name == getString(R.string.hot_flush) }.toFloat() / size, getString(R.string.hot_flush))
        val diarrhea =
            Pair(symptoms.count { it.name == getString(R.string.diarrhea) }.toFloat() / size, getString(R.string.diarrhea))
        val constipation =
            Pair(symptoms.count { it.name == getString(R.string.constipation) }.toFloat() / size, getString(R.string.constipation))
        val nausea =
            Pair(symptoms.count { it.name == getString(R.string.nausea) }.toFloat() / size, getString(R.string.nausea))
        val tired =
            Pair(symptoms.count { it.name == getString(R.string.tired) }.toFloat() / size, getString(R.string.tired))

        return arrayOf(
            burns, cramps, bleeding, chills, fever, bloating, hotFlush, diarrhea,
            constipation, nausea, tired
        )
    }

    private fun displaySymptomDetails(name: String) {
        val painEntries = mutableListOf<Entry>().apply {
            var index = 0f
            painRelations.forEach {
                this.add(Entry(index, it.pain.intensity.toFloat()))
                index++
            }
        }
        val symptomEntries = mutableListOf<BarEntry>().apply {
            var index = 0f
            painRelations.forEach { pain ->
                val filterSymptom = pain.symptoms.filter { it.name == name }
                if (!filterSymptom.isNullOrEmpty()) {
                    this.add(BarEntry(index, 1f))
                } else {
                    this.add(BarEntry(index, 0f))
                }
                index++
            }
        }

        val painDataSet = LineDataSet(painEntries, getString(R.string.my_pain)).apply {
            axisDependency = YAxis.AxisDependency.LEFT
            lineWidth = 2f
            color = getColor(requireContext(), R.color.design_default_color_secondary)
            setCircleColor(getColor(requireContext(), R.color.graph2))
            setDrawValues(false)
        }
        val symptomDataSet = BarDataSet(symptomEntries, name).apply {
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)
            color = when (name) {
                getString(R.string.burns) -> getColor(requireContext(), this@SymptomDetailFragment.colors[0])
                getString(R.string.cramps) -> getColor(requireContext(), this@SymptomDetailFragment.colors[1])
                getString(R.string.bleeding) -> getColor(requireContext(), this@SymptomDetailFragment.colors[2])
                getString(R.string.chills) -> getColor(requireContext(), this@SymptomDetailFragment.colors[3])
                getString(R.string.fever) -> getColor(requireContext(), this@SymptomDetailFragment.colors[4])
                getString(R.string.bloating) -> getColor(requireContext(), this@SymptomDetailFragment.colors[5])
                getString(R.string.hot_flush) -> getColor(requireContext(), this@SymptomDetailFragment.colors[6])
                getString(R.string.diarrhea) -> getColor(requireContext(), this@SymptomDetailFragment.colors[7])
                getString(R.string.constipation) -> getColor(requireContext(), this@SymptomDetailFragment.colors[8])
                getString(R.string.nausea) -> getColor(requireContext(), this@SymptomDetailFragment.colors[9])
                getString(R.string.tired) -> getColor(requireContext(), this@SymptomDetailFragment.colors[10])
                else -> getColor(requireContext(), R.color.colorSecondary)
            }
        }

        symptom_details_evo_chart.apply {
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
                setData(LineData(painDataSet))
                setData(BarData(symptomDataSet))
            }
            invalidate()
        }
    }
}
