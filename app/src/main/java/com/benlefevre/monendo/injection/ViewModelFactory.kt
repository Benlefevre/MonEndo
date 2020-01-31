package com.benlefevre.monendo.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.data.repositories.*
import com.benlefevre.monendo.ui.viewmodels.LoginActivityViewModel

class ViewModelFactory(private val firestoreRepo: FirestoreRepo,
                       private  val painRepo: PainRepo,
                       private val symptomRepo: SymptomRepo,
                       private val moodDao: MoodRepo,
                       private val userActivitiesRepo : UserActivitiesRepo,
                       private val temperatureRepo: TemperatureRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginActivityViewModel::class.java))
            return LoginActivityViewModel(
                firestoreRepo
            ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}