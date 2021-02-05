package com.benlefevre.monendo.ui

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.fragments.DashboardFragment
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentDashboardBinding
import com.benlefevre.monendo.injection.appModule
import com.benlefevre.monendo.injection.networkModule
import com.google.common.truth.Truth.assertThat
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

@LargeTest
@RunWith(AndroidJUnit4::class)
class DashboardFragmentTest {

    private lateinit var context: Context
    lateinit var viewModel: DashboardViewModel
    lateinit var module: Module
    private val mutableLiveData = MutableLiveData<List<PainWithRelations>>()
    private val liveData: LiveData<List<PainWithRelations>>
        get() = mutableLiveData
    private lateinit var binding: FragmentDashboardBinding

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = mockk(DashboardViewModel::class.java.name, relaxed = true)
        module = module(override = true) {
            viewModel {
                viewModel
            }
        }
        unloadKoinModules(listOf(appModule, networkModule))
        loadKoinModules(listOf(module))
    }

    @After
    fun tearDown() {
        unloadKoinModules(module)
        loadKoinModules(listOf(appModule, networkModule))
    }

    @Test
    fun dashboardFragmentUiTest() {
        launchDashboardFragment()
        assertDisplayed(R.id.card_pain)
        assertDisplayed(R.id.chart_pain)
        assertDisplayed(R.id.chart_pain_legend, R.string.evolution_of_pain_over_the_last_7_days)
        assertDisplayed(R.id.card_symptom)
        assertDisplayed(R.id.chart_symptom)
        assertDisplayed(R.id.chart_symptom_legend,R.string.recurrence_of_symptoms_over_the_last_7_days)
        assertDisplayed(R.id.card_activities)
        assertDisplayed(R.id.chart_activities)
        assertDisplayed(R.id.chart_activities_legend, R.string.practiced_activities_over_the_last_7_days)
        scrollTo(R.id.card_mood)
        assertDisplayed(R.id.card_sleep)
        assertDisplayed(R.id.chart_sleep)
        assertDisplayed(R.id.chart_sleep_legend,R.string.sleep_quality_over_the_last_7_days)
        assertDisplayed(R.id.card_mood)
        assertDisplayed(R.id.chart_mood)
        assertDisplayed(R.id.chart_mood_legend,R.string.mood_repartition_over_the_last_7_days)
        assertDisplayed(R.id.fab)
    }

    @Test
    fun dashboardPainChartTest() {
        every { viewModel.getPainRelationsBy7LastDays() } returns liveData
        launchDashboardFragment()
        assertThat(binding.chartPain.data.entryCount).isEqualTo(5)
        assertThat(binding.chartPain.legend.entries[0].label).isEqualTo(context.getString(R.string.my_pain))
    }

    @Test
    fun dashboardSymptomsChartTest() {
        val labels = listOf(
            context.getString(R.string.cramps),
            context.getString(R.string.burns),
            context.getString(R.string.bloating),
            context.getString(R.string.bleeding),
            context.getString(R.string.chills),
            context.getString(R.string.fever),
            context.getString(R.string.hot_flush),
            context.getString(R.string.constipation),
            context.getString(R.string.nausea),
            context.getString(R.string.tired),
            context.getString(R.string.diarrhea)
        )
        every { viewModel.getPainRelationsBy7LastDays() } returns liveData
        launchDashboardFragment()
        assertThat(binding.chartSymptom.data.dataSetCount).isEqualTo(11)
        assertThat(binding.chartSymptom.legend.entries.size).isEqualTo(11)

        assertThat(binding.chartSymptom.legend.entries[0].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[1].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[2].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[3].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[4].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[5].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[6].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[7].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[8].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[9].label).isIn(labels)
        assertThat(binding.chartSymptom.legend.entries[10].label).isIn(labels)


    }

    @Test
    fun dashboardActivitiesChartTest() {
        every { viewModel.getPainRelationsBy7LastDays() } returns liveData
        launchDashboardFragment()
        scrollTo(R.id.chart_activities)
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_intensity_chart,
                context.getString(R.string.stress)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_intensity_chart,
                context.getString(R.string.sport)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_duration_chart,
                context.getString(R.string.sport)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_intensity_chart,
                context.getString(R.string.sex)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_duration_chart,
                context.getString(R.string.sex)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_intensity_chart,
                context.getString(R.string.relaxation)
            )
        )
        assertThat(binding.chartActivities.data.dataSetLabels).asList().contains(
            context.getString(
                R.string.activity_duration_chart,
                context.getString(R.string.relaxation)
            )
        )
        assertThat(binding.chartActivities.data.dataSetCount).isEqualTo(7)
    }

    @Test
    fun dashboardSleepChartTest() {
        every { viewModel.getPainRelationsBy7LastDays() } returns liveData
        launchDashboardFragment()
        scrollTo(R.id.chart_sleep)
        assertThat(binding.chartSleep.legend.entries[0].label).isEqualTo(context.getString(R.string.sleep_quality))
        assertThat(binding.chartSleep.data.entryCount).isEqualTo(5)
    }

    @Test
    fun dashboardMoodChartTest() {
        val labels = listOf(
            context.getString(R.string.sad),
            context.getString(R.string.sick),
            context.getString(R.string.irritated),
            context.getString(R.string.happy),
            context.getString(R.string.very_happy)
        )
        every { viewModel.getPainRelationsBy7LastDays() } returns liveData
        launchDashboardFragment()
        scrollTo(R.id.chart_mood)
        assertThat(binding.chartMood.data.entryCount).isEqualTo(5)
        assertThat(binding.chartMood.legend.entries[0].label).isIn(labels)
        assertThat(binding.chartMood.legend.entries[1].label).isIn(labels)
        assertThat(binding.chartMood.legend.entries[2].label).isIn(labels)
        assertThat(binding.chartMood.legend.entries[3].label).isIn(labels)
        assertThat(binding.chartMood.legend.entries[4].label).isIn(labels)
    }

    private fun setValue() {
        mutableLiveData.value = getPainsWithRelations(context)
    }

    private fun launchDashboardFragment(): FragmentScenario<DashboardFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            setValue()
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.dashboardFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            DashboardFragment::class.java,
            null,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            this.binding = fragment.binding
            fragment.viewLifecycleOwnerLiveData.observeForever {
                if (it != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
        return scenario
    }
}