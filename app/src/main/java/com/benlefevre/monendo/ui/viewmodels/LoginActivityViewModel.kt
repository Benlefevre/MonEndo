package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benlefevre.monendo.data.repositories.FirestoreRepo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginActivityViewModel : ViewModel() {

    private val mFirestoreRepo : FirestoreRepo = FirestoreRepo

    fun createUserInFirestore(firestoreUser: FirebaseUser) = viewModelScope.launch{
        mFirestoreRepo.createUserInFirestore(firestoreUser)
    }

}