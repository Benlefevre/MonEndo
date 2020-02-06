package com.benlefevre.monendo.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.data.repositories.*
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import com.benlefevre.monendo.ui.viewmodels.LoginActivityViewModel
import com.benlefevre.monendo.ui.viewmodels.PainFragmentViewModel

class ViewModelFactory(
    private val firestoreRepo: FirestoreRepo,
    private val painRepo: PainRepo,
    private val symptomRepo: SymptomRepo,
    private val moodRepo: MoodRepo,
    private val userActivitiesRepo: UserActivitiesRepo,
    private val temperatureRepo: TemperatureRepo,
    private val painRelationRepo: PainWithRelationsRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(LoginActivityViewModel::class.java) ->
                return LoginActivityViewModel(firestoreRepo) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java)->
                return DashboardViewModel(painRelationRepo) as T
            modelClass.isAssignableFrom(PainFragmentViewModel::class.java) ->
                return PainFragmentViewModel(painRepo,symptomRepo,moodRepo,userActivitiesRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}