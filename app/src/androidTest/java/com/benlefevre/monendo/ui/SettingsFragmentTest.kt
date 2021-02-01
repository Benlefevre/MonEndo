package com.benlefevre.monendo.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.settings.SettingsFragment
import com.benlefevre.monendo.utils.*
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaListInteractions.scrollListToPosition
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsFragmentTest {


    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private val idlingResource = CountingIdlingResource("settingsFragmentTest")

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        signInWithCorrectCredentials()
    }

    @After
    fun tearDown() {
        FirebaseAuth.getInstance().signOut()
        IdlingRegistry.getInstance().unregister(idlingResource)
        removeDataInSharedPreferences(preferences)
    }

    @Test
    fun settingsFragmentUiTest() {
        launchSettingsFragment()
        assertDisplayed(R.string.saved_data)
        assertDisplayed(R.string.delete_pains)
        assertDisplayed(R.string.can_delete_pains)
        assertDisplayed(R.string.treatment)
        assertDisplayed(R.string.delete_treatments)
        assertDisplayed(R.string.reset_treatment_data)
        assertDisplayed(R.string.pill_tablet_settings)
        assertDisplayed(R.string.choose_nb_pill)
        assertDisplayed(R.string.fertility)
        assertDisplayed(R.string.delete_mens_data)
        assertDisplayed(R.string.can_delete_mens_data)
        assertDisplayed(R.string.delete_temp_data)
        assertDisplayed(R.string.can_delete_temp_data)
        scrollListToPosition(R.id.recycler_view, 9)
        assertDisplayed(R.string.doctor)
        assertDisplayed(R.string.delete_comment)
        assertDisplayed(R.string.can_delete_commentary)
    }

    @Test
    fun displayDialogWhenUserClickDeletePains() {
        launchSettingsFragment()
        clickOn(R.string.delete_pains)
        assertDisplayed(R.string.delete_pains)
        assertDisplayed(R.string.sure_delete_pains)
        assertDisplayed(R.string.cancel)
        assertDisplayed(R.string.yes_sure)
    }

    @Test
    fun displayDialogWhenUserClickDeleteTreatments() {
        launchSettingsFragment()
        clickOn(R.string.delete_treatments)
        assertDisplayed(R.string.delete_treatments)
        assertDisplayed(R.string.sure_delete_treatment)
        assertDisplayed(R.string.cancel)
        assertDisplayed(R.string.yes_sure)
    }

    @Test
    fun displayDialogWhenUserClickDeleteMenstruations() {
        launchSettingsFragment()
        clickOn(R.string.delete_mens_data)
        assertDisplayed(R.string.delete_mens_data)
        assertDisplayed(R.string.sure_delete_mens_data)
        assertDisplayed(R.string.cancel)
        assertDisplayed(R.string.yes_sure)
    }

    @Test
    fun displayDialogWhenUserClickDeleteTemperatures() {
        launchSettingsFragment()
        clickOn(R.string.delete_temp_data)
        assertDisplayed(R.string.delete_temp_data)
        assertDisplayed(R.string.sure_delete_temp)
        assertDisplayed(R.string.cancel)
        assertDisplayed(R.string.yes_sure)
    }

    @Test
    fun displayListWhenUserClickPillSettings() {
        launchSettingsFragment()
        clickOn(R.string.pill_tablet_settings)
        assertDisplayed(R.id.select_dialog_listview)
        assertListItemCount(R.id.select_dialog_listview, 6)
        assertDisplayedAtPosition(R.id.select_dialog_listview, 0, "28")
        assertDisplayedAtPosition(R.id.select_dialog_listview, 1, "21 + 7")
        assertDisplayedAtPosition(R.id.select_dialog_listview, 2, "21")
        assertDisplayedAtPosition(R.id.select_dialog_listview, 3, "14")
        assertDisplayedAtPosition(R.id.select_dialog_listview, 4, "12")
        assertDisplayedAtPosition(R.id.select_dialog_listview, 5, "10")
    }

    @Test
    fun displayCommentaryDialogWhenUserClickCommentaries() {
        launchSettingsFragment()
        scrollListToPosition(R.id.recycler_view, 9)
        clickOn(R.string.delete_comment)
        sleep(2000)
        assertDisplayed(R.id.pos_btn)
        assertDisplayed(R.id.neg_btn)
        assertDisplayed(R.id.recycler_view)
        assertListItemCount(R.id.recycler_view, 1)
        assertDisplayed(R.string.delete_comment)
        assertDisplayed(R.string.click_comment_to_delete)
        clickOn(R.id.item_check)
    }

    @Test
    fun removeMensDataWhenUserClickYes() {
        insertDataInSharedPreferences(preferences)
        launchSettingsFragment()

        assertThat(preferences.getString(CURRENT_MENS, null)).isEqualTo(lastDate)
        assertThat(preferences.getInt(DURATION, 0)).isEqualTo(28)
        assertThat(preferences.getString(NEXT_MENS, null)).isEqualTo(nextDate)

        clickOn(R.string.delete_mens_data)
        clickOn(R.string.yes_sure)

        assertThat(preferences.getString(CURRENT_MENS, null)).isNull()
        assertThat(preferences.getInt(DURATION, 0)).isEqualTo(0)
        assertThat(preferences.getString(NEXT_MENS, null)).isNull()
    }

    @Test
    fun removeTreatmentDataWhenUserClickYes() {
        insertDataInSharedPreferences(preferences)
        launchSettingsFragment()

        assertThat(preferences.getString(PILL_HOUR_NOTIF, null)).isEqualTo("20:00")
        assertThat(preferences.getString(CHECKED_PILLS, null)).isEqualTo(checkedPills)
        assertThat(preferences.getBoolean(CURRENT_CHECKED, false)).isTrue()
        assertThat(preferences.getBoolean(NEED_CLEAR, false)).isTrue()
        assertThat(preferences.getString(LAST_PILL_DATE, null)).isEqualTo(lastDate)
        assertThat(preferences.getString(NEXT_PILL_DATE, null)).isEqualTo(nextDate)
        assertThat(preferences.getString(TREATMENT, null)).isEqualTo(treatments)

        clickOn(R.string.delete_treatments)
        clickOn(R.string.yes_sure)

        assertThat(preferences.getString(PILL_HOUR_NOTIF, null)).isNull()
        assertThat(preferences.getString(CHECKED_PILLS, null)).isNull()
        assertThat(preferences.getBoolean(CURRENT_CHECKED, false)).isFalse()
        assertThat(preferences.getBoolean(NEED_CLEAR, false)).isFalse()
        assertThat(preferences.getString(LAST_PILL_DATE, null)).isNull()
        assertThat(preferences.getString(NEXT_PILL_DATE, null)).isNull()
        assertThat(preferences.getString(TREATMENT, null)).isNull()
    }

    private fun launchSettingsFragment(): FragmentScenario<SettingsFragment> {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.settingsFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            SettingsFragment::class.java,
            null,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
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
        Espresso.onIdle()
    }
}