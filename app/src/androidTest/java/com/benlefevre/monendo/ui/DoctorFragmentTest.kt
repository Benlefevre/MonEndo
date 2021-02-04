package com.benlefevre.monendo.ui

import android.Manifest
import android.content.Context
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.ui.DoctorDetailFragment
import com.google.firebase.auth.FirebaseAuth
import com.schibsted.spain.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaDialogInteractions.clickDialogNegativeButton
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import com.schibsted.spain.barista.interaction.BaristaListInteractions.scrollListToPosition
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import com.schibsted.spain.barista.interaction.PermissionGranter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DoctorFragmentTest {

    private val idlingResource = CountingIdlingResource("DoctorFragmentTest")
    lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        IdlingRegistry.getInstance().register(idlingResource)
        if(FirebaseAuth.getInstance().currentUser == null || !MainActivity.userIsInitialized()) {
            signInWithCorrectCredentials(idlingResource)
        }
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
        Intents.release()
    }

    @Test
    fun doctorFragmentTest() {
        ActivityScenario.launch(MainActivity::class.java)
        clickOn(R.id.doctorFragment)
        PermissionGranter.allowPermissionOneTime(Manifest.permission.ACCESS_FINE_LOCATION)
        sleep(7000)
        assertDisplayed(R.id.mapview)
        assertDisplayed(R.id.doc_chip)
        assertDisplayed(R.id.gyne_chip)
        assertDisplayed(R.id.doctor_search)
        clickOn(R.id.doctor_search)
        assertDisplayed(R.id.doc_chip2)
        clickOn(R.id.doc_chip2)
        assertDisplayed(R.id.gyne_chip2)
        clickOn(R.id.gyne_chip2)
        assertDisplayed(R.id.search_txt)
        assertDisplayed(R.id.search_btn)
        writeTo(R.id.search_txt, "Paris")
        onView(withText("Paris   75001"))
            .inRoot(isPlatformPopup()).perform(click())
        clickOn(R.id.search_btn)
        sleep(4000)
        assertRecyclerViewItemCount(R.id.doc_recycler_view, 5)
        scrollListToPosition(R.id.doc_recycler_view, 4)
        clickListItem(R.id.doc_recycler_view, 4)
        clickListItem(R.id.doc_recycler_view, 4)
        sleep(2000)
    }

    @Test
    fun doctorDetailsUiTest() {
        launchDoctorDetailsFragment()
        assertDisplayed(R.id.detail_name,"Docteur Test")
        assertDisplayed(R.id.detail_spec,"Médecin Généraliste")
        assertDisplayed(R.id.detail_address,"15 rue Lecourbe 75001 Paris")
        assertDisplayed(R.id.detail_phone,"01.90.90.90.90")
        assertDisplayed(R.id.detail_types,"Divers types d'actes chirurgicaux")
        assertDisplayed(R.id.detail_map)
        assertDisplayed(R.id.doctor_menu_call)
        assertDisplayed(R.id.doctor_menu_web)
        assertDisplayed(R.id.doctor_menu_comment)
    }

    @Test
    fun callIntentTest(){
        launchDoctorDetailsFragment()
        clickOn(R.id.doctor_menu_call)
        intended(hasAction(ACTION_DIAL))
    }

    @Test
    fun webIntentTest(){
        launchDoctorDetailsFragment()
        clickOn(R.id.doctor_menu_web)
        intended(hasAction(ACTION_VIEW))
    }

    @Test
    fun commentMenuTest(){
        launchDoctorDetailsFragment()
        clickOn(R.id.doctor_menu_comment)
        assertDisplayed(R.id.dialog_title)
        assertDisplayed(R.id.comment_slider_label)
        assertDisplayed(R.id.comment_slider)
        assertDisplayed(R.id.comment_text_label)
        assertDisplayed(R.id.comment_text)
        clickDialogNegativeButton()
    }

    private fun launchDoctorDetailsFragment(): FragmentScenario<DoctorDetailFragment> {
        val bundle = Bundle()
        bundle.putParcelable(
            "selectedDoctor", Doctor(
                "1",
                "Docteur Test",
                "Mr",
                "15 rue Lecourbe 75001 Paris",
                "Médecin Généraliste",
                null,
                "01.90.90.90.90",
                "Divers actes chirurgicaux",
                "Divers types d'actes chirurgicaux",
                listOf(48.015, 2.15),
                2.0,
                0,
                4.3
            )
        )
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        UiThreadStatement.runOnUiThread {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.doctorDetailFragment)
        }
        val scenario = FragmentScenario.launchInContainer(
            DoctorDetailFragment::class.java,
            bundle,
            R.style.AppTheme,
            null
        )
        scenario.onFragment { fragment ->
            fragment.viewLifecycleOwnerLiveData.observeForever { it ->
                if (it != null) {
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }
        }
        return scenario
    }
}