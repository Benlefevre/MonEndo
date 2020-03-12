package com.benlefevre.monendo.utils

import android.content.Context
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

fun configurePillNotification(context: Context, data: Data, hour: String) {
    val pillWorker =
        PeriodicWorkRequest.Builder(NotificationWorker::class.java, 1, TimeUnit.DAYS).apply {
            setInputData(data)
            setInitialDelay(setDelayDuration(hour), TimeUnit.MILLISECONDS)
            addTag(PILL_TAG)
        }.build()
    WorkManager.getInstance(context).enqueue(pillWorker)
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