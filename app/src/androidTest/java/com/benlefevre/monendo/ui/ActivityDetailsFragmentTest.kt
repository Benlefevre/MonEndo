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
import com.benlefevre.monendo.dashboard.fragments.ActivitiesDetailFragment
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentActivitiesDetailBinding
import com.benlefevre.monendo.injection.appModule
import com.benlefevre.monendo.injection.networkModule
import com.google.common.truth.Truth.assertThat
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
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
class ActivityDetailsFragmentTest {

    private lateinit var context: Context
    lateinit var viewModel: DashboardViewModel
    lateinit var module: Module
    private val mutableLiveData = MutableLiveData<List<PainWithRelations>>()
    private val liveData: LiveData<List<PainWithRelations>>
        get() = mutableLiveData
    private lateinit var binding: FragmentActivitiesDetailBinding

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
    fun activitiesDetailsFragmentTest() {
        every { viewModel.pains } returns liveData
        launchActivitiesFragment()
        assertDisplayed(R.id.activities_detail_rep_card)
        assertDisplayed(R.id.activities_details_rep_chart)
        assertDisplayed(R.id.activities_detail_card)
        assertDisplayed(R.id.activities_detail_chart)
        assertDisplayed(R.id.chip_week)
        assertDisplayed(R.id.chip_month)
        assertDisplayed(R.id.chip_6months)
        assertDisplayed(R.id.chip_year)
    }

    @Test
    fun activitiesDetailsChipTest() {
        every { viewModel.pains } returns liveData
        launchActivitiesFragment()
        clickOn(R.id.chip_month)
        clickOn(R.id.chip_6months)
        clickOn(R.id.chip_year)
        clickOn(R.id.chip_week)
        verifyOrder {
            viewModel.getPainsRelations7days()
            viewModel.getPainsRelations30days()
            viewModel.getPainsRelations180days()
            viewModel.getPainsRelations360days()
            viewModel.getPainsRelations7days()
        }
        assertThat(binding.activitiesDetailsRepChart.data.dataSet.entryCount).isEqualTo(5)
    }

    @Test
    fun activitiesDetailsRepChartTest() {
        every { viewModel.pains } returns liveData
        launchActivitiesFragment()
        clickOn(R.id.activities_details_rep_chart)
        assertThat(binding.activitiesDetailChart.data.scatterData.dataSetCount).isEqualTo(1)
        clickOn(R.id.activities_details_rep_chart)
        assertThat(binding.activitiesDetailChart.data.scatterData.dataSetCount).isEqualTo(5)
    }

    @Test
    fun activitiesDetailsEvoChartTest(){
        every { viewModel.pains } returns liveData
        launchActivitiesFragment()
        clickOn(R.id.activities_detail_chart)
        assertThat(binding.activitiesDetailChartDetailsText.text.toString()).contains("BASKETBALL")
        assertThat(binding.activitiesDetailChartDetailsText.text.toString()).contains(context.getString(R.string.relaxation).toUpperCase())
        assertThat(binding.activitiesDetailChartDetailsText.text.toString()).contains(context.getString(R.string.stress).toUpperCase())
        assertThat(binding.activitiesDetailChartDetailsText.text.toString()).contains(context.getString(R.string.sex).toUpperCase())
        assertThat(binding.activitiesDetailChartDetailsText.text.toString()).contains(context.getString(R.string.other).toUpperCase())
    }

    private fun setValue() {
        mutableLiveData.value = getPainsWithRelations(context)
    }

    private fun launchActivitiesFragment(): FragmentScenario<ActivitiesDetailFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            setValue()
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.activitiesDetailFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            ActivitiesDetailFragment::class.java,
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