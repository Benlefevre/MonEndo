package com.benlefevre.monendo.doctor

import androidx.lifecycle.*
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
    private val handle: SavedStateHandle,
    private val doctorRepository: DoctorRepository,
    private val commentaryRepository: CommentaryRepository
) : ViewModel() {

    private val _doctor = MutableLiveData<DoctorUiState>()

    val doctor: LiveData<DoctorUiState>
        get() = _doctor

    fun isReady(map: Map<String, String>) : DoctorUiState{
        val mapQ = handle.get<String>("mapQ") ?: ""
        val location = handle.get<String>("location")
        return if (handle.contains("doctor") && mapQ == map["q"] && location == map["geofilter.distance"]){
            DoctorUiState.DoctorReady(handle.get<List<Doctor>>("doctor")!!)
        }else{
            DoctorUiState.Loading
        }
    }

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        _doctor.value = isReady(map)
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
        handle.set("doctor", doctors)
        handle.set("mapQ", map["q"])
        handle.set("location",map["geofilter.distance"])
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

