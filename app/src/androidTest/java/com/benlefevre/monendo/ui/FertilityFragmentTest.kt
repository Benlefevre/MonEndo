package com.benlefevre.monendo.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.R
import com.benlefevre.monendo.fertility.ui.FertilityFragment
import com.benlefevre.monendo.utils.PREFERENCES
import com.schibsted.spain.barista.assertion.BaristaErrorAssertions.assertError
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.clearText
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaPickerInteractions.setDateOnPicker
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class FertilityFragmentTest {

    lateinit var context: Context
    lateinit var preferences: SharedPreferences

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        removeDataInSharedPreferences(preferences)
    }

    @Test
    fun fertilityFragmentUiTest() {
        launchFertilityFragment()
        assertDisplayed(R.id.fertility_calendar)
        assertDisplayed(R.id.fertility_chip_mens)
        assertDisplayed(R.id.fertility_chip_ovul)
        assertDisplayed(R.id.fertility_chip_ferti)
        assertDisplayed(R.id.fertility_day_mens_label)
        assertDisplayed(R.id.fertility_day_mens_txt)
        assertDisplayed(R.id.fertility_duration_mens_label)
        assertDisplayed(R.id.fertility_duration_mens_txt)
        assertDisplayed(R.string.body_temperature_monitoring)
        scrollTo(R.id.fertility_temp_chart)
        assertDisplayed(R.string.for_more_precision_you_can_save_your_daily_body_s_temperature_to_see_precisely_your_ovulation_s_period)
        assertDisplayed(R.id.fertility_temp_slider)
        assertDisplayed(R.id.fertility_temp_chart)
        assertDisplayed(R.id.fertility_temp_save_btn)
    }

    @Test
    fun fertilityMensTxtTest() {
        val lastDate = getLastDate(preferences)
        launchFertilityFragment()
        assertDisplayed(R.id.fertility_day_mens_txt, lastDate)
        clickOn(R.id.fertility_day_mens_txt)
        setDateOnPicker(2021, 1, 1)
        assertDisplayed(R.id.fertility_day_mens_txt, "01/01/21")
    }

    @Test
    fun fertilityMensTxtUpdateWithNextMensTest() {
        val nextDate = getVeryLastDate(preferences)
        launchFertilityFragment()
        assertDisplayed(R.id.fertility_day_mens_txt, nextDate)
    }

    @Test
    fun fertilityDurationTxtTest() {
        insertDataInSharedPreferences(preferences)
        launchFertilityFragment()
        assertDisplayed(R.id.fertility_duration_mens_txt, "28")
        writeTo(R.id.fertility_duration_mens_txt,"20")
        clearText(R.id.fertility_duration_mens_txt)
    }

    @Test
    fun fertilityMensTxtErrorTest(){
        launchFertilityFragment()
        clickOn(R.id.fertility_duration_mens_txt)
        assertError(R.id.fertility_day_mens_label,R.string.error_mens_txt)
    }

    @Test
    fun mensChipOpenDialogTest() {
        val nextDate = getVeryLastDate(preferences)
        launchFertilityFragment()
        clickOn(R.id.fertility_chip_mens)
        assertDisplayed(R.string.mens_dates)
        assertContains(nextDate)
    }

    @Test
    fun ovulChipOpenDialogTest() {
        getVeryLastDate(preferences)
        launchFertilityFragment()
        clickOn(R.id.fertility_chip_ovul)
        assertDisplayed(R.string.ovul_days)
    }

    @Test
    fun fertiChipOpenDialogTest() {
        getVeryLastDate(preferences)
        launchFertilityFragment()
        clickOn(R.id.fertility_chip_ferti)
        assertDisplayed(R.string.ferti_periods)
    }

    @Test
    fun sliderTempTest(){
        launchFertilityFragment()
        scrollTo(R.id.fertility_temp_save_btn)
        onView(withId(R.id.fertility_temp_slider)).perform(setValue(39.0F))
        clickOn(R.id.fertility_temp_save_btn)
    }

    private fun launchFertilityFragment(): FragmentScenario<FertilityFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.fertilityFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            FertilityFragment::class.java,
            null,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever {
                if (it != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
        return scenario
    }
}