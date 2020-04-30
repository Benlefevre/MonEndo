package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.api.DoctorRepository
import com.benlefevre.monendo.data.models.Doctor
import com.benlefevre.monendo.data.repositories.FirestoreRepo
import com.benlefevre.monendo.mappers.createDoctorsFromCpamApi
import kotlinx.coroutines.launch

class DoctorViewModel(private val doctorRepository: DoctorRepository, private val firestoreRepo: FirestoreRepo) : ViewModel() {

    private val _doctor = MutableLiveData<List<Doctor>>()

    val doctor: LiveData<List<Doctor>>
        get() = _doctor

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        val result = doctorRepository.getDoctors(map)
        _doctor.value = createDoctorsFromCpamApi(result)
    }
}

