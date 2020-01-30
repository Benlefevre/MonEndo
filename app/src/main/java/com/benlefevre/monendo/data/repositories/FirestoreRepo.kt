package com.benlefevre.monendo.data.repositories

import com.benlefevre.monendo.mappers.convertFirebaseUserIntoUser
import com.benlefevre.monendo.utils.USER_COLLECTION
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRepo {

    val mFirestore = FirebaseFirestore.getInstance()

    fun createUserInFirestore(firebaseUser: FirebaseUser) {
        if (!firebaseUser.isAnonymous) {
            val user = convertFirebaseUserIntoUser(firebaseUser)
            mFirestore.collection(USER_COLLECTION).document(user.id).set(user)
        }
    }
}