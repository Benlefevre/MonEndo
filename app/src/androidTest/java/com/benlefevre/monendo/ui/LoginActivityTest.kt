package com.benlefevre.monendo.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.login.LoginActivity
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import com.schibsted.spain.barista.interaction.BaristaEditTextInteractions.writeTo
import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Test
    fun loginActivityUiTest() {
        ActivityScenario.launch(LoginActivity::class.java)
        assertDisplayed(R.id.loginLogo)
        assertDisplayed(R.id.welcomeText, R.string.welcome)
        assertDisplayed(R.id.loginText, R.string.please_login)
        assertDisplayed(R.id.login_mail_btn, R.string.fui_sign_in_with_email)
        assertDisplayed(R.id.login_google_btn, R.string.fui_sign_in_with_google)
        assertDisplayed(R.id.login_facebook_btn, R.string.fui_sign_in_with_facebook)
        assertDisplayed(R.id.login_anonymous, R.string.fui_sign_in_anonymously)
    }

    @Test
    fun loginMailTest() {
        Intents.init()
        ActivityScenario.launch(LoginActivity::class.java)
        clickOn(R.id.login_mail_btn)
        assertDisplayed(R.id.email)
        assertDisplayed(R.id.button_next)
        writeTo(R.id.email, "test@test.fr")
        clickOn(R.id.button_next)
        sleep(2000)
        assertDisplayed(R.id.password)
        assertDisplayed(R.id.button_done)
        writeTo(R.id.password, "password")
        clickOn(R.id.button_done)
        sleep(4000)
        intended(hasComponent(MainActivity::class.java.name))
        Intents.release()
    }

}