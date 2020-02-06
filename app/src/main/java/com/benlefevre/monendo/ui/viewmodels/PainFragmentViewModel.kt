package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.Symptom
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.data.repositories.MoodRepo
import com.benlefevre.monendo.data.repositories.PainRepo
import com.benlefevre.monendo.data.repositories.SymptomRepo
import com.benlefevre.monendo.data.repositories.UserActivitiesRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainFragmentViewModel(
    private val painRepo: PainRepo,
    private val symptomRepo: SymptomRepo,
    private val moodRepo: MoodRepo,
    private val userActivitiesRepo: UserActivitiesRepo
) : ViewModel() {


    suspend fun insertPain(pain: Pain): Long = withContext(viewModelScope.coroutineContext) { painRepo.insertPain(pain) }


    suspend fun insertMood(mood: Mood, rowId: Long) {
        mood.painId = rowId
        moodRepo.insertMood(mood)
    }

    suspend fun insertSymptoms(symptoms: List<Symptom>, rowId: Long) {
        for (symptom in symptoms) {
            symptom.painId = rowId
        }
        symptomRepo.insertAllSymptoms(symptoms)
    }

    suspend fun insertUserActivities(activities: List<UserActivities>,rowId : Long) {
        for (activity in activities)
            activity.painId = rowId
        userActivitiesRepo.insertAllUserActivities(activities)
    }

    fun insertUserInput(pain: Pain, mood: Mood?, symptoms: List<Symptom>) = viewModelScope.launch {
        val row = insertPain(pain)
        mood?.let { insertMood(mood, row) }
        if(!symptoms.isNullOrEmpty()) insertSymptoms(symptoms, row)
    }
}
