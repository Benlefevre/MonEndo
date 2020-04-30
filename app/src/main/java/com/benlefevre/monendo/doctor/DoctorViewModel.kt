package com.benlefevre.monendo.doctor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.doctor.api.DoctorRepository
import com.benlefevre.monendo.doctor.models.Doctor
import kotlinx.coroutines.launch

class DoctorViewModel(private val doctorRepository: DoctorRepository, private val commentaryRepository: CommentaryRepository) : ViewModel() {

    private val _doctor = MutableLiveData<List<Doctor>>()

    val doctor: LiveData<List<Doctor>>
        get() = _doctor

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        val result = doctorRepository.getDoctors(map)
        _doctor.value =
            createDoctorsFromCpamApi(result)
    }
}

