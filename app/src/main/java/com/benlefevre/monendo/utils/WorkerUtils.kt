package com.benlefevre.monendo.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.benlefevre.monendo.treatment.models.Treatment
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

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
