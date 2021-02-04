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
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.fragments.SymptomDetailFragment
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.benlefevre.monendo.databinding.FragmentSymptomDetailBinding
import com.benlefevre.monendo.injection.appModule
import com.benlefevre.monendo.injection.networkModule
import com.google.common.truth.Truth.assertThat
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest

@LargeTest
@RunWith(AndroidJUnit4::class)
class SymptomDetailsFragmentTest : KoinTest {

    lateinit var context: Context

    lateinit var viewModel: DashboardViewModel
    lateinit var module: Module
    private val mutableLiveData = MutableLiveData<List<PainWithRelations>>()
    private val liveData: LiveData<List<PainWithRelations>>
        get() = mutableLiveData
    lateinit var binding: FragmentSymptomDetailBinding

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
    fun symptomFragmentUiTest() {
        every { viewModel.pains }.returns(liveData)
        launchSymptomFragment()
        assertDisplayed(R.id.symptom_details_rep_card)
        assertDisplayed(R.id.symptom_details_rep_chart)
        assertDisplayed(R.id.symptom_details_card)
        assertDisplayed(R.id.symptom_details_evo_chart)
        assertDisplayed(R.id.chip_week)
        assertDisplayed(R.id.chip_month)
        assertDisplayed(R.id.chip_6months)
        assertDisplayed(R.id.chip_year)
    }

    @Test
    fun symptomRepChartTest() {
        every { viewModel.pains }.returns(liveData)
        launchSymptomFragment()
        assertThat(binding.symptomDetailsRepChart.data.dataSet.entryCount).isEqualTo(11)
    }

    @Test
    fun symptomDetailsChartTest(){
        every { viewModel.pains }.returns(liveData)
        launchSymptomFragment()
        clickOn(R.id.symptom_details_rep_chart)
        clickOn(R.id.symptom_details_rep_chart)
        clickOn(R.id.chip_month)
        verify { viewModel.getPainsRelations30days() }
        clickOn(R.id.chip_6months)
        verify { viewModel.getPainsRelations180days() }
        clickOn(R.id.chip_year)
        verify { viewModel.getPainsRelations360days() }
        clickOn(R.id.chip_week)
        verify { viewModel.getPainsRelations7days() }
        clickOn(R.id.symptom_details_rep_chart)
        assertThat(binding.symptomDetailsEvoChart.data.scatterData.dataSetCount).isEqualTo(1)
    }

    private fun setValue() {
        mutableLiveData.value = getPainsWithRelations(context)
    }

    private fun launchSymptomFragment(): FragmentScenario<SymptomDetailFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        runOnUiThread {
            setValue()
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.symptomDetailFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            SymptomDetailFragment::class.java,
            null,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            binding = fragment.binding
            fragment.viewLifecycleOwnerLiveData.observeForever {
                if (it != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
        return scenario
    }
}