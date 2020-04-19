package com.benlefevre.monendo.utils

import android.content.Context
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

fun configureTreatmentNotification(context: Context, data: Data, hour: String, tag: String = PILL_TAG) {
    val treatmentWorker =
        PeriodicWorkRequest.Builder(NotificationWorker::class.java, 1, TimeUnit.DAYS)
            .setInputData(data)
            .setInitialDelay(setDelayDuration(hour), TimeUnit.MILLISECONDS)
            .addTag(tag)
            .build()
    WorkManager.getInstance(context).enqueue(treatmentWorker)
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
