package com.benlefevre.monendo.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.benlefevre.monendo.notification.AlarmReceiver
import com.benlefevre.monendo.notification.cancelPillAlarm
import com.benlefevre.monendo.ui.removeDataInSharedPreferences
import com.benlefevre.monendo.ui.treatments
import io.mockk.spyk
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AlarmReceiverTest {

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    @After
    fun tearDown() {
        cancelPillAlarm(context)
        removeDataInSharedPreferences(preferences)
    }

    @Test
    fun intentBootCompletedTest() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val alarmReceiver = spyk(AlarmReceiver())
        alarmReceiver.onReceive(context, intent)
        verifyOrder { alarmReceiver.resetAllNotifications() }
    }

    @Test
    fun intentExtraPillTagTest() {
        val intent = Intent().apply {
            putExtra(TREATMENT, PILL_TAG)
        }
        val alarmReceiver = spyk(AlarmReceiver())
        alarmReceiver.onReceive(context, intent)
        verifyOrder { alarmReceiver.sendRightNotification(intent) }
        verifyOrder { alarmReceiver.sendPillNotification(intent.getStringExtra(TREATMENT)!!) }
        verifyOrder { alarmReceiver.resetPillNotification() }
    }

    @Test
    fun intentExtraPillRepeatTagAndCurrentCheckedFalseTest() {
        preferences.edit().apply {
            putBoolean(CURRENT_CHECKED, false)
            putString(PILL_HOUR_NOTIF, "20:00")
        }.apply()
        val intent = Intent().apply {
            putExtra(TREATMENT, PILL_REPEAT)
        }
        val alarmReceiver = spyk(AlarmReceiver())
        alarmReceiver.onReceive(context, intent)
        verifyOrder { alarmReceiver.sendRightNotification(intent) }
        verifyOrder { alarmReceiver.sendPillNotification(intent.getStringExtra(TREATMENT)!!) }
        verifyOrder { alarmReceiver.resetPillRepeatNotification() }
    }

    @Test
    fun intentExtraPillRepeatTagAndCurrentCheckedTrueTest() {
        preferences.edit().apply {
            putBoolean(CURRENT_CHECKED, true)
            putString(PILL_HOUR_NOTIF, "20:00")
        }.apply()
        val intent = Intent().apply {
            putExtra(TREATMENT, PILL_REPEAT)
        }
        val alarmReceiver = spyk(AlarmReceiver())
        alarmReceiver.onReceive(context, intent)
        verifyOrder { alarmReceiver.sendRightNotification(intent) }
        verifyOrder { alarmReceiver.resetPillRepeatNotification() }
    }

    @Test
    fun intentTreatmentTagTest() {
        preferences.edit().apply {
            putString(TREATMENT, treatments)
        }.apply()
        val intent = Intent().apply {
            putExtra(TREATMENT, TREATMENT_TAG)
            putExtra(TREATMENT_NAME, "Doliprane")
            putExtra(TREATMENT_DOSAGE, "3")
            putExtra(TREATMENT_FORMAT, "cachet")
        }
        val alarmReceiver = spyk(AlarmReceiver())
        alarmReceiver.onReceive(context, intent)
        verifyOrder { alarmReceiver.sendRightNotification(intent) }
        verifyOrder { alarmReceiver.sendTreatmentNotification(any()) }
        verifyOrder { alarmReceiver.resetTreatmentNotification() }
    }
}