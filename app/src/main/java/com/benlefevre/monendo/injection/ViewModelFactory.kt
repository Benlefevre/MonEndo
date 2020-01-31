package com.benlefevre.monendo.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benlefevre.monendo.data.repositories.FirestoreRepo
import com.benlefevre.monendo.ui.viewmodels.LoginActivityViewModel

class ViewModelFactory(private val firestoreRepo: FirestoreRepo) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginActivityViewModel::class.java))
            return LoginActivityViewModel(
                firestoreRepo
            ) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}