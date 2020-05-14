package com.benlefevre.monendo.doctor.viewmodel

import androidx.lifecycle.*
import com.benlefevre.monendo.doctor.api.DoctorRepository
import com.benlefevre.monendo.doctor.createDoctorsFromCpamApi
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.repository.CommentaryRepository
import com.benlefevre.monendo.login.User
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
    private val handle: SavedStateHandle,
    private val doctorRepository: DoctorRepository,
    private val commentaryRepository: CommentaryRepository
) : ViewModel() {

    private val _doctor = MutableLiveData<DoctorUiState>()
    private val _commentaries = MutableLiveData<List<Commentary>>()

    val commentaries: LiveData<List<Commentary>>
        get() = _commentaries

    val doctor: LiveData<DoctorUiState>
        get() = _doctor

    fun isReady(map: Map<String, String>): DoctorUiState {
        val mapQ = handle.get<String>("mapQ") ?: ""
        val location = handle.get<String>("location") ?: ""
        return if (handle.contains("doctor") && mapQ == map["q"] && (map.containsKey("geofilter.distance") || location == map["refine.nom_com"])) {
            DoctorUiState.DoctorReady(handle.get<List<Doctor>>("doctor")!!)
        } else {
            DoctorUiState.Loading
        }
    }

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        _doctor.value = isReady(map)
        val result = doctorRepository.getDoctors(map)
        if (result.records.isEmpty()) {
            _doctor.value =
                DoctorUiState.Error("There no doctor for this search. Please modify it")
            return@launch
        }
        val doctors = createDoctorsFromCpamApi(result)
        var isComment = false

        doctors.forEach { doctor ->
            val commentaries = getCommentaries(doctor.id)
            if (commentaries.isNotEmpty()) {
                doctor.nbComment = commentaries.size
                doctor.rating = (commentaries.sumByDouble { it.rating } / doctor.nbComment)
                isComment = true
            }
        }

        val sortedList = if (isComment) {
            doctors.sortedByDescending { it.nbComment }
        } else {
            doctors
        }

        handle.set("doctor", sortedList)
        handle.set("mapQ", map["q"])
        handle.set("location", map["refine.nom_com"])
        _doctor.value =
            DoctorUiState.DoctorReady(sortedList)
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

    fun createCommentaryInFirestore(commentary: Commentary,user: User) {
        commentaryRepository.createDoctorCommentary(commentary,user)
    }

    fun getCommentaryWithId(doctorId: String) = viewModelScope.launch {
        val commentaries = getCommentaries(doctorId)
        commentaries.sortByDescending { it.date }
        _commentaries.value = commentaries
    }
}

