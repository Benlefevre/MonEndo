package com.benlefevre.monendo.ui

import android.content.Context
import android.content.SharedPreferences
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
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.treatment.ui.TreatmentFragment
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.benlefevre.monendo.utils.NUMBER_OF_PILLS
import com.benlefevre.monendo.utils.PREFERENCES
import com.google.android.material.chip.Chip
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.schibsted.spain.barista.assertion.BaristaErrorAssertions.assertError
import com.schibsted.spain.barista.assertion.BaristaHintAssertions.assertHint
import com.schibsted.spain.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaDialogInteractions.*
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaPickerInteractions.setDateOnPicker
import com.schibsted.spain.barista.interaction.BaristaPickerInteractions.setTimeOnPicker
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@LargeTest
@RunWith(AndroidJUnit4::class)
class TreatmentFragmentTest {

    lateinit var context: Context
    lateinit var preferences: SharedPreferences
    private val idlingResource = CountingIdlingResource("treatmentFragmentTest")
    private lateinit var formatArray: Array<String>
    private lateinit var durationArray: Array<String>
    private lateinit var pillArray: Array<String>

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        formatArray = context.resources.getStringArray(R.array.format)
        durationArray = context.resources.getStringArray(R.array.duration)
        pillArray = context.resources.getStringArray(R.array.pill_format)
        signInWithCorrectCredentials()
    }

    @After
    fun tearDown() {
        removeDataInSharedPreferences(preferences)
        FirebaseAuth.getInstance().signOut()
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun treatmentFragmentUiTest() {
        launchTreatmentFragment()
        assertDisplayed(R.id.pill_title)
        assertDisplayed(R.id.pill_tablet)
        assertDisplayed(R.id.mens_txt_label)
        assertDisplayed(R.id.mens_txt)
        assertDisplayed(R.id.mens_notif_label)
        assertDisplayed(R.id.mens_notif_txt)
        assertDisplayed(R.id.other_treatment_title)
        assertDisplayed(R.id.add_button)
    }

    @Test
    fun treatmentFragmentMensDataBindingTest() {
        insertDataInSharedPreferences(preferences)
        launchTreatmentFragment()
        assertDisplayed(R.id.mens_txt, lastDate)
        assertDisplayed(R.id.mens_notif_txt, notifHour)
    }

    @Test
    fun mensTxtTest() {
        removeDataInSharedPreferences(preferences)
        launchTreatmentFragment()
        clickOn(R.id.mens_txt)
        setDateOnPicker(2021, 1, 15)
        sleep(800)
        assertDisplayed(R.id.mens_txt, "15/01/21")
    }

    @Test
    fun notifHourTxtTest() {
        launchTreatmentFragment()
        clickOn(R.id.mens_notif_txt)
        assertError(R.id.mens_txt_label, R.string.start_date_pill)
        clickOn(R.id.mens_txt)
        setDateOnPicker(2021, 1, 15)
        clickOn(R.id.mens_notif_txt)
        setTimeOnPicker(12, 0)
        assertDisplayed(R.id.mens_notif_txt, "12:00")
        clickOn(R.id.mens_notif_txt)
        clickDialogNeutralButton()
        assertHint(
            R.id.mens_notif_label,
            R.string.the_time_you_want_to_be_notified_of_taking_your_pill
        )
    }

    @Test
    fun addButtonDialogUiTest() {
        launchTreatmentFragment()
        clickOn(R.id.add_button)
        assertDisplayed(R.id.treatment_title)
        assertDisplayed(R.id.treatment_name_label)
        assertDisplayed(R.id.treatment_name)
        assertDisplayed(R.id.treatment_duration_label)
        assertDisplayed(R.id.treatment_duration)
        assertDisplayed(R.id.treatment_dosage_label)
        assertDisplayed(R.id.treatment_dosage)
        assertDisplayed(R.id.treatment_format_label)
        assertDisplayed(R.id.treatment_format)
        assertDisplayed(R.id.treatment_format)
        assertDisplayed(R.string.click_on_the_day_s_period_to_define_the_notification_s_hour_of_taking_treatment)
        assertDisplayed(R.id.period_chip_group)
    }

    @Test
    fun durationDropDownTest() {
        launchTreatmentFragment()
        clickOn(R.id.add_button)
        clickOn(R.id.treatment_duration)
        onView(withText(durationArray[0])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(durationArray[1])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(durationArray[2])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun formatDropDownTest() {
        launchTreatmentFragment()
        clickOn(R.id.add_button)
        clickOn(R.id.treatment_format)
        onView(withText(formatArray[0])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(formatArray[1])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(formatArray[2])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(formatArray[3])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun chipNotifHourTest() {
        launchTreatmentFragment()
        clickOn(R.id.add_button)
        clickOn(R.id.morning_chip)
        setTimeOnPicker(6, 0)
        assertDisplayed(R.id.morning_chip, "06:00")
        clickOn(R.id.noon_chip)
        setTimeOnPicker(12, 0)
        assertDisplayed(R.id.noon_chip, "12:00")
        clickOn(R.id.afternoon_chip)
        setTimeOnPicker(16, 0)
        assertDisplayed(R.id.afternoon_chip, "16:00")
        clickOn(R.id.evening_chip)
        setTimeOnPicker(20, 0)
        assertDisplayed(R.id.evening_chip, "20:00")
        clickOn(R.id.morning_chip)
        assertDisplayed(R.id.morning_chip, R.string.morning)
        clickOn(R.id.noon_chip)
        assertDisplayed(R.id.noon_chip, R.string.noon)
        clickOn(R.id.afternoon_chip)
        assertDisplayed(R.id.afternoon_chip, R.string.afternoon)
        clickOn(R.id.evening_chip)
        assertDisplayed(R.id.evening_chip, R.string.evening)
    }

    @Test
    fun createTreatmentTest() {
        removeDataInSharedPreferences(preferences)
        launchTreatmentFragment()
        assertNotDisplayed(R.id.recycler_view)
        clickOn(R.id.add_button)
        writeTo(R.id.treatment_name, "Doliprane")
        clickOn(R.id.treatment_duration)
        onView(withText(durationArray[0])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickOn(R.id.treatment_format)
        onView(withText(formatArray[0])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        writeTo(R.id.treatment_dosage, "3")
        clickOn(R.id.morning_chip)
        setTimeOnPicker(6, 0)
        clickOn(R.id.noon_chip)
        setTimeOnPicker(12, 0)
        clickOn(R.id.afternoon_chip)
        setTimeOnPicker(16, 0)
        clickOn(R.id.evening_chip)
        setTimeOnPicker(21, 0)
        clickOn(R.id.save_btn)
        assertRecyclerViewItemCount(R.id.recycler_view, 1)
    }

    @Test
    fun removeTreatmentTest() {
        insertDataInSharedPreferences(preferences)
        launchTreatmentFragment()
        assertRecyclerViewItemCount(R.id.recycler_view, 1)
        clickOn(R.id.item_delete_btn)
        assertNotDisplayed(R.id.recycler_view)
    }

    @Test
    fun treatmentDialogErrorLabelTest() {
        launchTreatmentFragment()
        clickOn(R.id.add_button)
        assertHint(R.id.treatment_name_label, R.string.treatment_s_name)
        assertHint(R.id.treatment_duration_label, R.string.treatment_s_duration)
        assertHint(R.id.treatment_dosage_label, R.string.dosage)
        assertHint(R.id.treatment_format_label, R.string.format)
        clickOn(R.id.save_btn)
        assertError(R.id.treatment_name_label, R.string.enter_treatment_name)
        assertError(R.id.treatment_duration_label, R.string.enter_treatment_duration)
        assertError(R.id.treatment_dosage_label, R.string.enter_treatment_dosage)
        assertError(R.id.treatment_format_label, R.string.enter_treatment_format)
    }

    @Test
    fun openDialogWhenUserPressSetting() {
        ActivityScenario.launch(MainActivity::class.java)
        clickOn(R.id.treatmentFragment)
        clickOn(R.id.pill_settings)
        assertDisplayed(R.id.pill_title)
        assertDisplayed(R.id.pill_label)
        assertDisplayed(R.id.pill_format)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[0])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[1])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[2])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[3])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[4])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[5])).inRoot(RootMatchers.isPlatformPopup()).check(
            matches(
                isDisplayed()
            )
        )
        onView(withText(pillArray[0])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogNegativeButton()

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[0])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("28")

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[1])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("29")

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[2])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("21")

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[3])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("14")

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[4])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("12")

        clickOn(R.id.pill_settings)
        clickOn(R.id.pill_format)
        onView(withText(pillArray[5])).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        clickDialogPositiveButton()
        assertThat(preferences.getString(NUMBER_OF_PILLS,"28")).isEqualTo("10")
    }

    @Test
    fun treatmentFragmentTreatmentDataBindingTest() {
        insertDataInSharedPreferences(preferences)
        launchTreatmentFragment()
        assertDisplayed(R.id.item_name, treatmentName)
        onView(
            allOf(
                withText(containsString(treatmentMorning)),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(containsString(treatmentNoon)),
                isAssignableFrom(Chip::class.java)
            )
        ).check(matches(isDisplayed()))
        assertDisplayed(R.id.duration_time, treatmentDuration)
    }

    private fun launchTreatmentFragment(): FragmentScenario<TreatmentFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.treatmentFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            TreatmentFragment::class.java,
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