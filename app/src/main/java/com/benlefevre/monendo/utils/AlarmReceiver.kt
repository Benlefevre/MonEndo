package com.benlefevre.monendo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.benlefevre.monendo.R
import com.benlefevre.monendo.treatment.models.Treatment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var manager: NotificationManager
    private lateinit var myContext: Context
    private lateinit var preferences : SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("onReceive + action = ${intent.action}")
        myContext = context
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        when (intent.action) {
            "android.intent.action.BOOT_COMPLETED" -> resetAllNotifications()
            else -> sendRightNotification(intent)
        }
    }

    /**
     * Checks the intent's extras to get a tag and defines which notification's function needs to
     * be called
     */
    private fun sendRightNotification(intent: Intent) {
        manager =
            myContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val extras = intent.extras
        val tag = intent.getStringExtra(TREATMENT)
        val repeat = preferences.getBoolean(CURRENT_CHECKED, false)
        tag?.let {
            when {
                it == PILL_TAG -> sendPillNotification(tag)
                it == PILL_REPEAT && !repeat -> sendPillNotification(tag)
                it == TREATMENT_TAG -> extras?.let {
                    sendTreatmentNotification(it)
                }
                else -> return
            }
        }

    }

    /**
     * Sends a notification with the rights information about the user's contraceptive pill
     */
    private fun sendPillNotification(tag: String) {
        Timber.i("Pill Notif + $tag")
        val pendingIntent = NavDeepLinkBuilder(myContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.treatmentFragment)
            .createPendingIntent()

        val channelId = myContext.getString(R.string.contra_pill_notif_id)
        val channelName = myContext.getString(R.string.contra_pill_notif_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(notificationChannel)
        }

        val title: String
        val text: String
        val id: Int
        when (tag) {
            PILL_TAG -> {
                title = myContext.getString(R.string.dont_forget_pill)
                text = myContext.getString(R.string.check_pill)
                id = 1
            }
            else -> {
                title = myContext.getString(R.string.forgotten_pill)
                text = myContext.getString(R.string.forgot_check_pill)
                id = 2
            }
        }

        val notification = NotificationCompat.Builder(myContext, channelId)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.notification_icon)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(id, notification)
    }

    /**
     * Sends a notification about the user's treatment
     */
    private fun sendTreatmentNotification(data: Bundle) {
        Timber.i("Treatment Notif")
        val pendingIntent = NavDeepLinkBuilder(myContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.treatmentFragment)
            .createPendingIntent()

        val channelId = myContext.getString(R.string.treatment_channel_id)
        val channelName = myContext.getString(R.string.treatment_channel_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(myContext, channelId)
            .setContentTitle(myContext.getString(R.string.treatment_notif_title))
            .setSmallIcon(R.drawable.notification_icon)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(myContext.getString(R.string.treatment_notif_dosage, data.getString(TREATMENT_DOSAGE), data.getString(TREATMENT_FORMAT), data.getString(TREATMENT_NAME)))
            )
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(3, notification)
    }

    /**
     * Checks if it's necessary to reset the notifications after a device's reboot
     */
    private fun resetAllNotifications() {
        val hourPill = preferences.getString(PILL_HOUR_NOTIF, "")
        val treatmentList = mutableListOf<Treatment>()
        Gson().fromJson<List<Treatment>>(
            preferences.getString(TREATMENT, ""),
            object : TypeToken<List<Treatment>>() {}.type
        )?.let {
            treatmentList.addAll(it)
        }

        if (!hourPill.isNullOrBlank()) {
            resetPillNotification(hourPill)
        }

        if (treatmentList.isNotEmpty()) {
            resetTreatmentNotification(treatmentList)
        }
    }

    /**
     * Resets an alarm with the AlarmManager to send notification about user's contraceptive pill
     */
    private fun resetPillNotification(hourPill: String) {
        Timber.i("resetPillNotification")
        val intent = Intent(myContext, AlarmReceiver::class.java).apply {
            putExtra(TREATMENT, PILL_TAG)
        }
        createAlarmAtTheUserTime(myContext, intent, hourPill, PILL_ID)

        val hour = parseStringInTime(hourPill)
        val repeatHour: String
        if (hour != Date(-1L)) {
            repeatHour = setRepeatHour(hour)
            val repeatIntent = Intent(myContext, AlarmReceiver::class.java).apply {
                putExtra(TREATMENT, PILL_REPEAT)
            }
            createAlarmAtTheUserTime(myContext, repeatIntent, repeatHour, PILL_REPEAT_ID)
        }
    }

    /**
     * Resets an alarm with the AlarmManager to send notifications about the user's treatment
     */
    private fun resetTreatmentNotification(treatmentList: MutableList<Treatment>) {
        Timber.i("resetTreatmentNotification")
        for ((index,treatment) in treatmentList.withIndex()){
            val intent = Intent(myContext, AlarmReceiver::class.java).apply {
                putExtra(TREATMENT, TREATMENT_TAG)
                putExtra(TREATMENT_NAME, treatment.name)
                putExtra(TREATMENT_DOSAGE, treatment.dosage)
                putExtra(TREATMENT_FORMAT, treatment.format)
            }
            if (treatment.morning != "") createAlarmAtTheUserTime(myContext, intent, treatment.morning, index + 10)
            if (treatment.noon != "") createAlarmAtTheUserTime(myContext, intent, treatment.noon, index +20)
            if (treatment.afternoon != "") createAlarmAtTheUserTime(myContext, intent, treatment.afternoon, index + 30)
            if (treatment.evening != "") createAlarmAtTheUserTime(myContext, intent, treatment.evening, index + 40)
        }
    }
}