package com.benlefevre.monendo.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.login.LoginActivity
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.benlefevre.monendo.utils.PREFERENCES
import com.google.firebase.auth.FirebaseAuth
import com.schibsted.spain.barista.assertion.BaristaDrawerAssertions.assertDrawerIsClosed
import com.schibsted.spain.barista.assertion.BaristaDrawerAssertions.assertDrawerIsClosedWithGravity
import com.schibsted.spain.barista.assertion.BaristaDrawerAssertions.assertDrawerIsOpenWithGravity
import com.schibsted.spain.barista.assertion.BaristaImageViewAssertions.assertHasAnyDrawable
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickBack
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaDrawerInteractions.closeDrawer
import com.schibsted.spain.barista.interaction.BaristaDrawerInteractions.openDrawer
import com.schibsted.spain.barista.interaction.BaristaScrollInteractions.scrollTo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber


@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var preferences: SharedPreferences
    private lateinit var context: Context
    private val idlingResource = CountingIdlingResource("mainActivityTest")

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        signInWithCorrectCredentials()
    }

    @After
    fun reset() {
        FirebaseAuth.getInstance().signOut()
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun toolbarTest() {
        ActivityScenario.launch(MainActivity::class.java)
        assertDisplayed(R.id.main_toolbar)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.dashboard))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.nav_app_bar_open_drawer_description))))
        openDrawer(R.id.main_drawer)
        closeDrawer(R.id.main_drawer)
        clickOn(R.id.treatmentFragment)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.treatment))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.pills_settings))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.nav_app_bar_navigate_up_description))))
        clickOn(R.id.fertilityFragment)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.fertility))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.nav_app_bar_navigate_up_description))))
        clickOn(R.id.doctorFragment)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.doctor))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.search_doctors))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.nav_app_bar_navigate_up_description))))
        clickOn(R.id.dashboardFragment)
        openDrawer(R.id.main_drawer)
        clickOn(R.id.settingsFragment)
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withText(R.string.settings))))
        onView(withId(R.id.main_toolbar)).check(matches(hasDescendant(withContentDescription(R.string.nav_app_bar_navigate_up_description))))
    }

    @Test
    fun drawerTest() {
        ActivityScenario.launch(MainActivity::class.java)
        openDrawer()
        assertDrawerIsOpenWithGravity(R.id.main_drawer, GravityCompat.START)
        assertDisplayed(R.id.main_nav_view)
        closeDrawer()
        assertDrawerIsClosedWithGravity(R.id.main_drawer, GravityCompat.START)
        assertNotDisplayed(R.id.main_nav_view)
        openDrawer()
        assertContains(R.string.dashboard)
        assertContains(R.string.treatment)
        assertContains(R.string.fertility)
        assertContains(R.string.doctor)
        assertContains(R.string.settings)
        assertContains(R.string.sign_out)
        clickBack()
        assertDrawerIsClosed(R.id.main_drawer)
    }

    @Test
    fun drawerHeaderTest_goodCredentials() {
        ActivityScenario.launch(MainActivity::class.java)
        openDrawer()
        assertDisplayed(R.id.user_name, "Test")
        assertDisplayed(R.id.user_mail, "test@test.fr")
        assertHasAnyDrawable(R.id.user_photo)
    }

    @Test
    fun drawerHeaderTest_anonymousCredentials() {
        signInWithAnonymousCredentials()
        ActivityScenario.launch(MainActivity::class.java)
        openDrawer()
        assertDisplayed(R.id.user_name, R.string.anonymous)
        assertDisplayed(R.id.user_mail, "")
        assertHasAnyDrawable(R.id.user_photo)
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun bottomBarTest() {
        removeDataInSharedPreferences(preferences)
        ActivityScenario.launch(MainActivity::class.java)
        assertDisplayed(R.id.main_bottom_bar)
        assertDisplayed(R.id.dashboardFragment)
        assertDisplayed(R.id.treatmentFragment)
        assertDisplayed(R.id.fertilityFragment)
        assertDisplayed(R.id.doctorFragment)
        clickOn(R.id.fab)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        clickOn(R.id.card_pain)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        clickOn(R.id.card_symptom)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        clickOn(R.id.card_activities)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        scrollTo(R.id.card_mood)
        clickOn(R.id.card_sleep)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        clickOn(R.id.chart_mood_legend)
        assertNotDisplayed(R.id.main_bottom_bar)
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click())
        clickOn(R.id.dashboardFragment)
        onView(withId(R.id.main_bottom_bar)).check(matches(hasDescendant(withContentDescription(R.string.dashboard))))
        clickOn(R.id.treatmentFragment)
        onView(withId(R.id.main_bottom_bar)).check(matches(hasDescendant(withContentDescription(R.string.treatment))))
        clickOn(R.id.fertilityFragment)
        onView(withId(R.id.main_bottom_bar)).check(matches(hasDescendant(withContentDescription(R.string.fertility))))
        clickOn(R.id.doctorFragment)
        onView(withId(R.id.main_bottom_bar)).check(matches(hasDescendant(withContentDescription(R.string.doctor))))
    }

    @Test
    fun fabTest() {
        ActivityScenario.launch(MainActivity::class.java)
        assertDisplayed(R.id.fab)
        clickOn(R.id.fab)
    }

    @Test
    fun pressBackAfterCloseDrawerCloseApp() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openDrawer(R.id.main_drawer)
        clickBack()
        pressBackUnconditionally()
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }

    @Test
    fun launchLoginActivityWhenUserNotLogged() {
        FirebaseAuth.getInstance().signOut()
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        intended(hasComponent(LoginActivity::class.java.name))
        Intents.release()
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

    private fun signInWithAnonymousCredentials() {
    Timber.i("Auth anonymous process")
    IdlingRegistry.getInstance().register(idlingResource)
    if (FirebaseAuth.getInstance().currentUser != null) {
        FirebaseAuth.getInstance().signOut()
    }
    FirebaseAuth.getInstance().signInAnonymously()
        .addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.i("Auth success anonymous: ${it.isSuccessful} / User : ${FirebaseAuth.getInstance().currentUser?.isAnonymous}")
                idlingResource.decrement()
            } else {
                Timber.i("Auth failed")
            }
        }
    idlingResource.increment()
    onIdle()
}

}