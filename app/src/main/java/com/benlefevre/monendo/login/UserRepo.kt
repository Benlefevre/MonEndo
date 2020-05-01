package com.benlefevre.monendo.login

import com.benlefevre.monendo.utils.USER_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore

class UserRepo(val firestore: FirebaseFirestore) {

    fun createUserInFirestore(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user)
    }
}
