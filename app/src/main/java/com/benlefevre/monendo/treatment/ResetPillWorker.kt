package com.benlefevre.monendo.treatment

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.benlefevre.monendo.utils.CURRENT_CHECKED
import com.benlefevre.monendo.utils.PREFERENCES
import timber.log.Timber

class ResetPillWorker(private val context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {

    override fun doWork(): Result {
        Timber.i("resetCurrentChecked")
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(CURRENT_CHECKED,false).apply()
        return Result.success()
    }
}