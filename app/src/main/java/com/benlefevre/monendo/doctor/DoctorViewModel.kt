package com.benlefevre.monendo.doctor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.doctor.api.DoctorRepository
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.models.Doctor
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class DoctorUiState {
    object Loading : DoctorUiState()
    data class DoctorReady(val doctors: List<Doctor>) : DoctorUiState()
    data class Error(val errorMessage: String) : DoctorUiState()
}

class DoctorViewModel(
    private val doctorRepository: DoctorRepository,
    private val commentaryRepository: CommentaryRepository
) : ViewModel() {

    private val _doctor = MutableLiveData<DoctorUiState>()

    val doctor: LiveData<DoctorUiState>
        get() = _doctor

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        _doctor.value = DoctorUiState.Loading
        val result = doctorRepository.getDoctors(map)
        if (result.records.isEmpty()) {
            _doctor.value = DoctorUiState.Error("There no doctor for this search. Please modify it")
            return@launch
        }
        val doctors = createDoctorsFromCpamApi(result)
        doctors.forEach { doctor ->
            val commentaries = getCommentaries(doctor.id)
            doctor.nbComment = commentaries.size
            doctor.rating = (commentaries.sumByDouble { it.rating } / doctor.nbComment)
        }
        doctors.sortedBy { it.nbComment }
        _doctor.value = DoctorUiState.DoctorReady(doctors)
    }

    private suspend fun getCommentaries(doctorId: String): MutableList<Commentary> {
        val commentaries = mutableListOf<Commentary>()

        withContext(Dispatchers.IO) {
            val data = commentaryRepository.getDoctorCommentary(doctorId).await()
            data.forEach {
                val comment: Commentary = it.toObject()
                commentaries.add(comment)
            }
        }
        return commentaries
    }
}

