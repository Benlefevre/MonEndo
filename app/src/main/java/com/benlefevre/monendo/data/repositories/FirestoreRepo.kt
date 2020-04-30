package com.benlefevre.monendo.data.repositories

import com.benlefevre.monendo.data.models.Commentary
import com.benlefevre.monendo.data.models.User
import com.benlefevre.monendo.utils.COMMENT_COLLECTION
import com.benlefevre.monendo.utils.USER_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepo {

    private val firestore = FirebaseFirestore.getInstance()

    companion object{
        private var INSTANCE : FirestoreRepo? = null

        fun getInstance(): FirestoreRepo {
            var tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            tempInstance = FirestoreRepo()
            INSTANCE = tempInstance
            return tempInstance
        }
    }

    fun createUserInFirestore(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user)
    }

    fun createDoctorCommentary(commentary: Commentary){
        firestore.collection(COMMENT_COLLECTION).document().set(commentary)
    }

    fun getDoctorCommentary(doctorId : String): Query {
        return firestore.collection(COMMENT_COLLECTION).whereEqualTo("doctorId",doctorId)
    }
}
