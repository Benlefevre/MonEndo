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
import com.benlefevre.monendo.dashboard.fragments.PainDetailFragment
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentPainDetailBinding
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
class PainDetailsFragmentTest {

    private lateinit var context: Context
    lateinit var viewModel: DashboardViewModel
    lateinit var module: Module
    private val mutableLiveData = MutableLiveData<List<PainWithRelations>>()
    private val liveData: LiveData<List<PainWithRelations>>
        get() = mutableLiveData
    private lateinit var binding: FragmentPainDetailBinding

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
    fun painDetailsFragmentUiTest() {
        every { viewModel.pains } returns liveData
        launchPainDetailsFragment()
        assertDisplayed(R.id.pain_details_card)
        assertDisplayed(R.id.pain_details_chart)
        assertDisplayed(R.id.pain_details_chart_legend)
        assertDisplayed(R.id.chipGroup)
        assertDisplayed(R.id.chip_week)
        assertDisplayed(R.id.chip_month)
        assertDisplayed(R.id.chip_6months)
        assertDisplayed(R.id.chip_year)
        assertDisplayed(R.id.pain_details_input_card)
        assertDisplayed(R.id.pain_details_date)
        assertDisplayed(R.id.pain_details_value)
        assertDisplayed(R.id.pain_details_location)
        assertDisplayed(R.id.pain_details_mood)
        assertDisplayed(R.id.pain_details_symptoms)
        assertDisplayed(R.id.pain_details_activities)
    }

    @Test
    fun painDetailsChipTest() {
        every { viewModel.pains } returns liveData
        launchPainDetailsFragment()
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
        assertThat(binding.painDetailsChart.data.entryCount).isEqualTo(5)
        assertThat(binding.painDetailsChart.data.dataSets[0].getEntryForIndex(0).y).isEqualTo(8F)
        assertThat(binding.painDetailsChart.data.dataSets[0].getEntryForIndex(1).y).isEqualTo(6F)
        assertThat(binding.painDetailsChart.data.dataSets[0].getEntryForIndex(2).y).isEqualTo(4F)
        assertThat(binding.painDetailsChart.data.dataSets[0].getEntryForIndex(3).y).isEqualTo(2F)
        assertThat(binding.painDetailsChart.data.dataSets[0].getEntryForIndex(4).y).isEqualTo(0F)
    }

    @Test
    fun painDetailsTextTest() {
        every { viewModel.pains } returns liveData
        launchPainDetailsFragment()
        assertThat(binding.painDetailsDateTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsActivitiesTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsLocationTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsMoodTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsLocationTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsSymptomTxt.text.toString()).isEmpty()
        assertThat(binding.painDetailsValueTxt.text.toString()).isEmpty()
        clickOn(R.id.pain_details_chart)
        assertThat(binding.painDetailsDateTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsActivitiesTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsLocationTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsMoodTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsLocationTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsSymptomTxt.text.toString()).isNotEmpty()
        assertThat(binding.painDetailsValueTxt.text.toString()).isNotEmpty()
    }

    private fun setValue() {
        mutableLiveData.value = getPainsWithRelations(context)
    }

    private fun launchPainDetailsFragment(): FragmentScenario<PainDetailFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            setValue()
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.painDetailFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            PainDetailFragment::class.java,
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