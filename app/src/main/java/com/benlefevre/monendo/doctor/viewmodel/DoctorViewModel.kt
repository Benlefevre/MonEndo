package com.benlefevre.monendo.doctor.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.benlefevre.monendo.doctor.createDoctorsFromCpamApi
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.doctor.models.Doctor
import com.benlefevre.monendo.doctor.repository.AdresseRepository
import com.benlefevre.monendo.doctor.repository.CommentaryRepository
import com.benlefevre.monendo.doctor.repository.DoctorRepository
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.DOC
import com.benlefevre.monendo.utils.GEOFILTER_DIST
import com.benlefevre.monendo.utils.GYNE
import com.benlefevre.monendo.utils.REFINE_CODE
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

sealed class DoctorUiState {
    object Loading : DoctorUiState()
    data class DoctorReady(val doctors: List<Doctor>) : DoctorUiState()
    data class Error(val errorMessage: String) : DoctorUiState()
}

class DoctorViewModel(
    private val handle: SavedStateHandle,
    private val doctorRepository: DoctorRepository,
    private val commentaryRepository: CommentaryRepository,
    private val adresseRepository: AdresseRepository
) : ViewModel() {

    private val _doctor = MutableLiveData<DoctorUiState>()
    private val _commentaries = MutableLiveData<List<Commentary>>()
    private val _cities = MutableLiveData<List<Pair<String, String>>>()
    private val queryMap = mutableMapOf<String, String>()

    val cities: LiveData<List<Pair<String, String>>>
        get() = _cities

    val commentaries: LiveData<List<Commentary>>
        get() = _commentaries

    val doctor: LiveData<DoctorUiState>
        get() = _doctor

    init {
        queryMap["dataset"] = "annuaire-des-professionnels-de-sante"
        queryMap["q"] =
            "Gynécologue obstétricien OR Gynécologue médical OR Gynécologue médical et obstétricien"
        handle.set("mapQ", queryMap["q"])
        queryMap["rows"] = "50"
    }

    fun isReady(map: Map<String, String>): Boolean {
        Timber.i("${handle.get<String>("mapQ")} && ${handle.get<String>("location")} ")
        val mapQ = handle.get<String>("mapQ") ?: ""
        val location = handle.get<String>("location") ?: ""
        val geolocation = handle.get<String>("geolocation") ?: ""
        return (handle.contains("doctor") && mapQ == map["q"]
                && ((map.containsKey(GEOFILTER_DIST) && geolocation == map[GEOFILTER_DIST])
                || (map.containsKey(REFINE_CODE) && location == map[REFINE_CODE])))
    }

    fun getDoctors(map: Map<String, String>) = viewModelScope.launch {
        val doctors = mutableListOf<Doctor>()
        if (isReady(map)) {
            doctors.addAll(handle.get<List<Doctor>>("doctor")!!)
            _doctor.value = DoctorUiState.DoctorReady(doctors)
        } else {
            _doctor.value = DoctorUiState.Loading
            val result = doctorRepository.getDoctors(map)
            if (result.records.isEmpty()) {
                _doctor.value =
                    DoctorUiState.Error("There no doctor for this search. Please modify it")
                return@launch
            }
            doctors.addAll(createDoctorsFromCpamApi(result))
        }
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
        handle.set("location", map[REFINE_CODE])
        handle.set("geolocation", map[GEOFILTER_DIST])
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

    fun createCommentaryInFirestore(commentary: Commentary, user: User) {
        commentaryRepository.createDoctorCommentary(commentary, user)
    }

    fun getCommentaryWithId(doctorId: String) = viewModelScope.launch {
        val commentaries = getCommentaries(doctorId)
        commentaries.sortByDescending { it.date }
        _commentaries.value = commentaries
    }

    fun getAdresse(input: String) = viewModelScope.launch {
        val map = mutableMapOf<String, String>()
        map["type"] = "municipality"
        map["limit"] = "15"
        map["q"] = input
        val adresses = adresseRepository.getAdresses(map)
        val citiesName = mutableListOf<Pair<String, String>>()
        if (adresses.features.isNotEmpty()) {
            for (feature in adresses.features) {
                citiesName.add(Pair(feature.properties.name, feature.properties.postcode))
            }
            _cities.value = citiesName
        }
    }

    fun getDoctorsWithUserInput() {
        getDoctors(queryMap)
    }

    fun setSpecialitySearchCriteria(specName: String) {
        when (specName) {
            GYNE -> queryMap["q"] =
                "Gynécologue obstétricien OR Gynécologue médical OR Gynécologue médical et obstétricien"
            DOC -> queryMap["q"] = "Médecin généraliste"
        }
    }

    fun setPostalCodeQuery(postalCode: String) {
        queryMap.remove(GEOFILTER_DIST)
        queryMap["facet"] = "code_postal"
        queryMap[REFINE_CODE] = postalCode
    }

    fun setGeoLocationQuery(location: Location) {
        queryMap.remove("facet")
        queryMap.remove(REFINE_CODE)
        queryMap[GEOFILTER_DIST] = "${location.latitude},${location.longitude},5000"
    }
}

