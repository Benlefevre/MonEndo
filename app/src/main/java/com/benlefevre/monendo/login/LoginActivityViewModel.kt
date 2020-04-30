package com.benlefevre.monendo.login

import androidx.lifecycle.ViewModel

class LoginActivityViewModel(private val userRepo : UserRepo) : ViewModel() {

    fun createUserInFirestore(user: User) =
        userRepo.createUserInFirestore(user)

}