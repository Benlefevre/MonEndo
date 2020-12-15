package com.benlefevre.monendo.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.repository.CommentaryRepository
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import com.benlefevre.monendo.pain.PainRepo
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingViewModel(
    private val painRepo: PainRepo,
    private val tempRepo: TemperatureRepo,
    private val commentRepo: CommentaryRepository
) : ViewModel() {

    val commentLiveData = MutableLiveData<List<Commentary>>()

    fun deleteAllPains() = viewModelScope.launch {
        painRepo.deleteAllPains()
    }

    fun deleteAllTemperatures() = viewModelScope.launch {
        tempRepo.deleteAllTemperatures()
    }

    fun getUserCommentaries(userId : String) = viewModelScope.launch {
        val commentaries = mutableListOf<Commentary>()
        commentaries.addAll(commentRepo.getCommentariesByUser(userId).await().toObjects(Commentary::class.java))
        commentLiveData.value = commentaries
    }

    fun deleteCommentary(commentaryId : String) = commentRepo.deleteCommentary(commentaryId)
}