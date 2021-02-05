package com.benlefevre.monendo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.benlefevre.monendo.treatment.models.Treatment
import com.benlefevre.monendo.utils.*
import timber.log.Timber
import java.util.*

/**
 * Sets a repeating alarm with the AlarmManager after define the needed PendingIntent
 */
fun createAlarmAtTheUserTime(context: Context, intent: Intent, hour: String, tag: Int) {
    Timber.i("$tag")
    val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent =
        PendingIntent.getBroadcast(context, tag, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    val triggeredTime =
        if (isNeededStop7Days(preferences) && tag == PILL_ID) {
            setTriggeredTimeToNextPill(hour, preferences.getString(NEXT_PILL_DATE, "")!!)
        } else {
            setTriggeredTime(hour)
        }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggeredTime,
            pendingIntent
        )
    } else {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggeredTime,
            pendingIntent)
    }
}

private fun isNeededStop7Days(preferences: SharedPreferences): Boolean {
    val nextPillDate = preferences.getString(NEXT_PILL_DATE, "")!!
    if (nextPillDate.isBlank()){
        return false
    }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val newPill = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, nextPillDate.substring(0, 2).toInt())
        set(Calendar.MONTH, nextPillDate.substring(3, 5).toInt() - 1)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    return preferences.getBoolean(NEED_CLEAR, false)
            && preferences.getString(NUMBER_OF_PILLS, "28")?.toInt() == 21
            && today.time != newPill.time
}

fun setTriggeredTimeToNextPill(hour: String, nextPillDate: String): Long {
    Timber.i("SetTriggeredTimeNextPill")
    val selectedHour = Calendar.getInstance()
    if (hour.isNotBlank()) {
        selectedHour.apply {
            set(Calendar.DAY_OF_MONTH, nextPillDate.substring(0, 2).toInt())
            set(Calendar.MONTH, nextPillDate.substring(3, 5).toInt() - 1)
            set(Calendar.HOUR_OF_DAY, hour.substring(0, 2).toInt())
            set(Calendar.MINUTE, hour.substring(3).toInt())
            set(Calendar.SECOND, 0)
        }
    }
    Timber.i("now = ${System.currentTimeMillis()} et selectedHour = ${selectedHour.timeInMillis}, différence = ${(selectedHour.timeInMillis - System.currentTimeMillis()) / 1000}sec")
    return selectedHour.timeInMillis
}

/**
 * Checks if the passed hour is after now and returns a long that corresponding to time in millis
 */
fun setTriggeredTime(hour: String): Long {
    Timber.i("SetTriggeredTime")
    val now = Calendar.getInstance()
    val selectedHour = Calendar.getInstance()
    if (!hour.isBlank()) {
        selectedHour.apply {
            set(Calendar.HOUR_OF_DAY, hour.substring(0, 2).toInt())
            set(Calendar.MINUTE, hour.substring(3).toInt())
            set(Calendar.SECOND, 0)
        }
    }
    return if (now.after(selectedHour)) {
        selectedHour.add(Calendar.DAY_OF_YEAR, 1)
        Timber.i("now after = ${System.currentTimeMillis()} et selectedHour = ${selectedHour.timeInMillis}, différence = ${(selectedHour.timeInMillis - System.currentTimeMillis()) / 1000}sec")
        selectedHour.timeInMillis
    } else {
        Timber.i("now = ${System.currentTimeMillis()} et selectedHour = ${selectedHour.timeInMillis}, différence = ${(selectedHour.timeInMillis - System.currentTimeMillis()) / 1000}sec")
        selectedHour.timeInMillis
    }
}

/**
 * Cancels all saved pill alarms with the AlarmManager
 */
fun cancelPillAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(TREATMENT, PILL_TAG)
    }
    val repeatIntent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(TREATMENT, PILL_REPEAT)
    }
    alarmManager.cancel(
        PendingIntent.getBroadcast(
            context,
            PILL_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    )
    alarmManager.cancel(
        PendingIntent.getBroadcast(
            context,
            PILL_REPEAT_ID,
            repeatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    )
}

/**
 * Cancels all saved treatment alarms with the AlarmManager
 */
fun cancelTreatmentAlarm(context: Context, treatment: Treatment, position: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra(TREATMENT, TREATMENT_TAG)
        putExtra(TREATMENT_NAME, treatment.name)
        putExtra(TREATMENT_DOSAGE, treatment.dosage)
        putExtra(TREATMENT_FORMAT, treatment.format)
    }
    for (index in 1..4) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                (position + (index * 10)),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }
}