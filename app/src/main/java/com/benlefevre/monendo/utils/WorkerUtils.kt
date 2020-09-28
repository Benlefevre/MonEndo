package com.benlefevre.monendo.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.benlefevre.monendo.notification.NotificationWorker
import com.benlefevre.monendo.treatment.ResetPillWorker
import com.benlefevre.monendo.treatment.models.Treatment
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

const val RESET_CURRENT_CHECKED = "resetCurrentChecked"

fun configureTreatmentNotification(context: Context, data: Data, hour: String, tag: String = PILL_TAG) {
    Timber.i(tag)
    val treatmentWorker =
        PeriodicWorkRequest.Builder(NotificationWorker::class.java, 1, TimeUnit.DAYS)
            .setInputData(data)
            .setInitialDelay(setDelayDuration(hour), TimeUnit.MILLISECONDS)
            .addTag(tag)
            .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(tag,ExistingPeriodicWorkPolicy.REPLACE,treatmentWorker)
}

fun configureResetCurrentChecked(context: Context){
    val resetWorker =
        PeriodicWorkRequest.Builder(ResetPillWorker::class.java,1,TimeUnit.DAYS)
            .setInitialDelay(setResetDelayDuration(),TimeUnit.MILLISECONDS)
            .addTag(RESET_CURRENT_CHECKED)
            .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(RESET_CURRENT_CHECKED,ExistingPeriodicWorkPolicy.REPLACE,resetWorker)
}

fun setResetDelayDuration() : Long{
    val now = Calendar.getInstance()
    now.apply {
        add(Calendar.DAY_OF_YEAR,1)
        set(Calendar.HOUR_OF_DAY,0)
        set(Calendar.MINUTE,0)
        set(Calendar.SECOND,0)
    }
    Timber.i("now : ${now.timeInMillis} et system : ${System.currentTimeMillis()} / différence : ${now.timeInMillis - System.currentTimeMillis()}")
    return now.timeInMillis - System.currentTimeMillis()
}

fun setDelayDuration(hour: String): Long {
    val now = Calendar.getInstance()
    val selectedHour = Calendar.getInstance()
    if (!hour.isBlank()) {
        selectedHour.apply {
            set(Calendar.HOUR_OF_DAY, hour.substring(0, 2).toInt())
            set(Calendar.MINUTE, hour.substring(3).toInt())
        }
    }
    return if (now.after(selectedHour)) {
        selectedHour.add(Calendar.DAY_OF_YEAR, 1)
        selectedHour.timeInMillis - System.currentTimeMillis()
    } else {
        selectedHour.timeInMillis - System.currentTimeMillis()
    }
}

fun cancelResetCurrentWorker(context: Context){
    WorkManager.getInstance(context).cancelUniqueWork(RESET_CURRENT_CHECKED)
}

fun cancelPillWorkWithWorker(context: Context){
            WorkManager.getInstance(context).apply {
            cancelUniqueWork(PILL_TAG)
            cancelUniqueWork(PILL_REPEAT)
        }
}

fun cancelTreatmentWorkWithWorker(context: Context, treatment: Treatment){
            WorkManager.getInstance(context).apply {
            cancelUniqueWork("${treatment.name} $MORNING")
            cancelUniqueWork("${treatment.name} $NOON")
            cancelUniqueWork("${treatment.name} $AFTERNOON")
            cancelUniqueWork("${treatment.name} $EVENING")
        }
}
