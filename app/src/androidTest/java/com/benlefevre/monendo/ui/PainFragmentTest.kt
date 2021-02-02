package com.benlefevre.monendo.ui

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.pain.PainFragment
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.parseStringInDate
import com.google.android.material.chip.Chip
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.schibsted.spain.barista.assertion.BaristaHintAssertions.assertHint
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaDialogInteractions.clickDialogPositiveButton
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaPickerInteractions.setDateOnPicker
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class PainFragmentTest {

    lateinit var context: Context
    private val idlingResource = CountingIdlingResource("painFragmentTest")

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun painFragmentUiTest() {
        launchPainFragment()
        assertDisplayed(R.id.card_title)
        assertDisplayed(R.id.card_date)
        assertDisplayed(R.id.slider_legend)
        assertDisplayed(R.id.pain_slider)
        assertDisplayed(R.id.location_legend)
        assertDisplayed(R.id.location_chipgroup)
        assertDisplayed(R.id.symptom_legend)
        assertDisplayed(R.id.symptom_chipgroup)
        assertDisplayed(R.id.activities_card_title)
        assertDisplayed(R.id.activities_card_legend)
        scrollTo(R.id.save_btn)
        assertDisplayed(R.id.card_sport)
        assertDisplayed(R.id.card_sleep)
        assertDisplayed(R.id.card_stress)
        assertDisplayed(R.id.card_relaxation)
        assertDisplayed(R.id.card_sex)
        assertDisplayed(R.id.card_other)
        assertDisplayed(R.id.mood_card_title)
        assertDisplayed(R.id.mood_chipgroup)
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sad))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sick))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.irritated))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.happy))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.very_happy))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun datePickerTest() {
        launchPainFragment()
        clickOn(R.id.card_date)
        setDateOnPicker(2020, 11, 1)
        assertDisplayed(R.id.card_date, "01/11/20")
    }

    @Test
    fun painSliderTest() {
        launchPainFragment()
        onView(withId(R.id.pain_slider)).perform(setValue(7F))
    }

    @Test
    fun displayCorrectDialogWhenUserChooseSport() {
        launchPainFragment()
        clickOn(R.id.card_sport)
        assertNotDisplayed(R.id.other_legend)
        assertNotDisplayed(R.id.other_choice_text)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertHint(R.id.sport_legend, R.string.what_activities_did_you_practiced)
        assertDisplayed(R.id.sport_choice_text)
        assertDisplayed(R.id.duration_legend, R.string.how_long_in_minutes)
        assertDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.intensity_legend, R.string.with_what_intensity_on_a_scale_from_0_to_10)
        assertDisplayed(R.id.intensity_slider)
        clickOn(R.id.sport_choice_text)
        onView(withText("BasketBall")).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        assertDisplayed(R.id.sport_choice_text, "BasketBall")
        onView(withId(R.id.duration_slider)).perform(setValue(30F))
        onView(withId(R.id.intensity_slider)).perform(setValue(4F))
        clickDialogPositiveButton()
    }

    @Test
    fun displayCorrectDialogWhenUserChooseStress() {
        launchPainFragment()
        clickOn(R.id.card_stress)
        assertNotDisplayed(R.id.other_legend)
        assertNotDisplayed(R.id.other_choice_text)
        assertNotDisplayed(R.id.sport_legend)
        assertNotDisplayed(R.id.sport_choice_text)
        assertNotDisplayed(R.id.duration_legend)
        assertNotDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertDisplayed(R.id.intensity_legend, R.string.with_what_intensity_on_a_scale_from_0_to_10)
        assertDisplayed(R.id.intensity_slider)
        onView(withId(R.id.intensity_slider)).perform(setValue(5F))
        clickDialogPositiveButton()
    }

    @Test
    fun displayCorrectDialogWhenUserChooseSleep() {
        launchPainFragment()
        clickOn(R.id.card_sleep)
        assertNotDisplayed(R.id.other_legend)
        assertNotDisplayed(R.id.other_choice_text)
        assertNotDisplayed(R.id.sport_legend)
        assertNotDisplayed(R.id.sport_choice_text)
        assertNotDisplayed(R.id.duration_legend)
        assertNotDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertDisplayed(R.id.intensity_legend, R.string.rate_sleep_quality)
        assertDisplayed(R.id.intensity_slider)
        onView(withId(R.id.intensity_slider)).perform(setValue(5F))
        clickDialogPositiveButton()
    }

    @Test
    fun displayCorrectDialogWhenUserChooseRelaxation() {
        launchPainFragment()
        scrollTo(R.id.save_btn)
        clickOn(R.id.card_relaxation)
        assertNotDisplayed(R.id.other_legend)
        assertNotDisplayed(R.id.other_choice_text)
        assertNotDisplayed(R.id.sport_legend)
        assertNotDisplayed(R.id.sport_choice_text)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertDisplayed(R.id.duration_legend, R.string.how_long_in_minutes)
        assertDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.intensity_legend, R.string.with_what_intensity_on_a_scale_from_0_to_10)
        assertDisplayed(R.id.intensity_slider)
        onView(withId(R.id.duration_slider)).perform(setValue(100F))
        onView(withId(R.id.intensity_slider)).perform(setValue(10F))
        clickDialogPositiveButton()
    }

    @Test
    fun displayCorrectDialogWhenUserChooseSex() {
        launchPainFragment()
        scrollTo(R.id.save_btn)
        clickOn(R.id.card_sex)
        assertNotDisplayed(R.id.other_legend)
        assertNotDisplayed(R.id.other_choice_text)
        assertNotDisplayed(R.id.sport_legend)
        assertNotDisplayed(R.id.sport_choice_text)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertDisplayed(R.id.duration_legend, R.string.how_long_in_minutes)
        assertDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.intensity_legend, R.string.with_what_intensity_on_a_scale_from_0_to_10)
        assertDisplayed(R.id.intensity_slider)
        onView(withId(R.id.duration_slider)).perform(setValue(30F))
        onView(withId(R.id.intensity_slider)).perform(setValue(6F))
        clickDialogPositiveButton()
    }

    @Test
    fun displayCorrectDialogWhenUserChooseOther() {
        launchPainFragment()
        scrollTo(R.id.save_btn)
        clickOn(R.id.card_other)
        assertNotDisplayed(R.id.sport_legend)
        assertNotDisplayed(R.id.sport_choice_text)
        assertHint(R.id.other_legend, R.string.what_activities_did_you_practiced)
        assertDisplayed(R.id.other_choice_text)
        assertDisplayed(R.id.duration_legend)
        assertDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.dialog_title, R.string.some_precisions_please)
        assertDisplayed(R.id.duration_legend, R.string.how_long_in_minutes)
        assertDisplayed(R.id.duration_slider)
        assertDisplayed(R.id.intensity_legend, R.string.with_what_intensity_on_a_scale_from_0_to_10)
        assertDisplayed(R.id.intensity_slider)
        clickOn(R.id.other_choice_text)
        writeTo(R.id.other_choice_text, "Test the app")
        onView(withId(R.id.duration_slider)).perform(setValue(240F))
        onView(withId(R.id.intensity_slider)).perform(setValue(9F))
        clickDialogPositiveButton()
    }

    @Test
    fun createAChipWhenUserCreateAnActivity() {
        launchPainFragment()
        scrollTo(R.id.save_btn)
        clickOn(R.id.card_other)
        writeTo(R.id.other_choice_text, "Test the app")
        onView(withId(R.id.duration_slider)).perform(setValue(240F))
        onView(withId(R.id.intensity_slider)).perform(setValue(9F))
        clickDialogPositiveButton()
        assertDisplayed(R.id.activity_chipgroup)
        onView(
            allOf(
                withText(containsString(context.getString(R.string.other))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun displaySnackBarWhenUserClickChip() {
        createAChipWhenUserCreateAnActivity()
        onView(
            allOf(
                withText(containsString(context.getString(R.string.other))),
                isAssignableFrom(Chip::class.java)
            )
        ).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_action))
            .check(matches(withText(context.getString(R.string.delete))))
        clickOn(com.google.android.material.R.id.snackbar_action)
        assertNotExist(context.getString(R.string.other_activities, "Test the app"))
    }

    @Test
    fun whenUserSavePainThenNavigateToDashboardFragment() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.painFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            PainFragment::class.java,
            null,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        clickOn(R.id.save_btn)
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.dashboardFragment)
    }

    @Test
    fun whenUserClickOnOptionMenuThenNavigateToDashboardFragment() {
        signInWithCorrectCredentials()
        ActivityScenario.launch(MainActivity::class.java)
        clickOn(R.id.fab)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.my_pain))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.save))))
        onView(withContentDescription(R.string.save)).perform(click())
        assertDisplayed(R.id.card_pain)
        assertDisplayed(R.id.card_symptom)
        FirebaseAuth.getInstance().signOut()
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun whenUserClickSaveThenAPainIsCreated() {
        val scenario = launchPainFragment()
        scrollTo(R.id.save_btn)
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sad))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sad))),
                isAssignableFrom(Chip::class.java)
            )
        ).perform(
            click()
        )
        clickOn(R.id.chip_back)
        clickOn(R.id.save_btn)
        scenario.onFragment { fragment ->
            assertThat(fragment.pain.date).isEqualTo(parseStringInDate(formatDateWithYear(Calendar.getInstance().time)))
            assertThat(fragment.pain.location).isEqualTo(context.getString(R.string.back))
            assertThat(fragment.pain.intensity).isEqualTo(5)
        }
    }

    @Test
    fun whenUserClickSaveThenSymptomsCreated(){
        val scenario = launchPainFragment()
        clickOn(R.id.chip_bleeding)
        clickOn(R.id.chip_bloating)
        clickOn(R.id.chip_burns)
        clickOn(R.id.chip_cramps)
        clickOn(R.id.chip_fever)
        clickOn(R.id.chip_chills)
        clickOn(R.id.chip_hot_flush)
        clickOn(R.id.chip_diarrhea)
        clickOn(R.id.chip_nausea)
        clickOn(R.id.chip_tired)
        clickOn(R.id.chip_constipation)
        clickOn(R.id.save_btn)
        scenario.onFragment { fragment ->
            val date = fragment.symptoms[0].date
            val painId = fragment.symptoms[0].painId
            assertThat(fragment.symptoms).hasSize(11)
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.bleeding),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.bloating),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.cramps),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.burns),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.fever),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.chills),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.hot_flush),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.diarrhea),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.nausea),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.tired),date = date))
            assertThat(fragment.symptoms).contains(Symptom(painId = painId,name = context.getString(R.string.constipation),date = date))
        }
    }

    @Test
    fun whenUserClickSaveThenMoodIsCreated(){
        val scenario = launchPainFragment()
        scrollTo(R.id.save_btn)
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sad))),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(context.getString(R.string.sad))),
                isAssignableFrom(Chip::class.java)
            )
        ).perform(
            click()
        )
        clickOn(R.id.save_btn)
        scenario.onFragment { fragment ->
            assertThat(fragment.mood?.value).isEqualTo(context.getString(R.string.sad))
        }
    }

    private fun launchPainFragment(): FragmentScenario<PainFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.painFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            PainFragment::class.java,
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

    private fun signInWithCorrectCredentials() {
        MainActivity.user =
            User("yEIvBj0y6CbZgrIoFIjPKxH1Yob2", "Test", "test@test.fr", NO_PHOTO_URL)
        IdlingRegistry.getInstance().register(idlingResource)
        FirebaseAuth.getInstance().signInWithEmailAndPassword("test@test.fr", "password")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Timber.i("Auth sucess : ${it.isSuccessful} / User : ${FirebaseAuth.getInstance().currentUser?.email}")
                    idlingResource.decrement()
                } else {
                    Timber.i("Auth failed")
                }
            }
        idlingResource.increment()
        onIdle()
    }
}