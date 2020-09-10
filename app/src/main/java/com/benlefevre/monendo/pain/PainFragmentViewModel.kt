package com.benlefevre.monendo.pain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.models.Pain
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.dashboard.repository.MoodRepo
import com.benlefevre.monendo.dashboard.repository.SymptomRepo
import com.benlefevre.monendo.dashboard.repository.UserActivitiesRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainFragmentViewModel(
    private val painRepo: PainRepo,
    private val symptomRepo: SymptomRepo,
    private val moodRepo: MoodRepo,
    private val userActivitiesRepo: UserActivitiesRepo
) : ViewModel() {

    private val activities = mutableListOf<UserActivities>()
    private val _activitiesLiveData = MutableLiveData<List<UserActivities>>()

    private val _insertDone = MutableLiveData<Boolean>()
    val insertDone: LiveData<Boolean>
        get() = _insertDone

    val activitiesLiveData: LiveData<List<UserActivities>>
        get() = _activitiesLiveData

    init {
        _insertDone.value = false
    }

    fun addActivities(userActivities: UserActivities) {
        activities.add(userActivities)
        _activitiesLiveData.value = activities
    }

    fun removeActivities(userActivities: UserActivities) {
        activities.remove(userActivities)
        _activitiesLiveData.value = activities
    }

    suspend fun insertPain(pain: Pain): Long =
        withContext(viewModelScope.coroutineContext) { painRepo.insertPain(pain) }

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

    suspend fun insertUserActivities(activities: List<UserActivities>, rowId: Long) {
        for (activity in activities)
            activity.painId = rowId
        userActivitiesRepo.insertAllUserActivities(activities)
    }

    fun insertUserInput(pain: Pain, mood: Mood?, symptoms: List<Symptom>) = viewModelScope.launch {
        val row = insertPain(pain)
        mood?.let { insertMood(mood, row) }
        if (!symptoms.isNullOrEmpty()) insertSymptoms(symptoms, row)
        if (!activities.isNullOrEmpty()) insertUserActivities(activities, row)
        _insertDone.value = true
    }
}
