package com.benlefevre.monendo.treatment

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.benlefevre.monendo.treatment.models.Treatment
import com.benlefevre.monendo.utils.CURRENT_CHECKED
import com.benlefevre.monendo.utils.PREFERENCES
import com.benlefevre.monendo.utils.TREATMENT
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

class ResetPillWorker(private val context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {

    override fun doWork(): Result {
        Timber.i("resetCurrentChecked")
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        resetCurrentCheck(sharedPreferences)
        resetTreatment(sharedPreferences)
        return Result.success()
    }

    private fun resetCurrentCheck(preferences: SharedPreferences){
        preferences.edit().putBoolean(CURRENT_CHECKED,false).apply()
    }

    private fun resetTreatment(preferences: SharedPreferences) {
        val gson = Gson()
        val treatments = mutableListOf<Treatment>()
        gson.fromJson<List<Treatment>>(
            preferences.getString(TREATMENT, null),
            object : TypeToken<List<Treatment>>() {}.type
        )
            ?.let { list ->
                treatments.addAll(list)
            }
        if(treatments.isNotEmpty()){
            treatments.forEach { treatment ->
                treatment.isTakenMorning = false
                treatment.isTakenNoon = false
                treatment.isTakenAfternoon = false
                treatment.isTakenEvening = false
            }
            preferences.edit().putString(TREATMENT, gson.toJson(treatments)).apply()
        }
    }
}