package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.benlefevre.monendo.data.models.User
import com.benlefevre.monendo.data.repositories.FirestoreRepo

class LoginActivityViewModel(private val firestoreRepo : FirestoreRepo) : ViewModel() {

    fun createUserInFirestore(user: User) =
        firestoreRepo.createUserInFirestore(user)

}