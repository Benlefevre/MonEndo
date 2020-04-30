package com.benlefevre.monendo.login

import com.benlefevre.monendo.utils.USER_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore

class UserRepo {

    private val firestore = FirebaseFirestore.getInstance()

    companion object{
        private var INSTANCE : UserRepo? = null

        fun getInstance(): UserRepo {
            var tempInstance =
                INSTANCE
            if (tempInstance != null)
                return tempInstance
            tempInstance = UserRepo()
            INSTANCE = tempInstance
            return tempInstance
        }
    }

    fun createUserInFirestore(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user)
    }
}
