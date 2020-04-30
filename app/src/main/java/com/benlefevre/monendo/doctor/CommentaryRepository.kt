package com.benlefevre.monendo.doctor

import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.utils.COMMENT_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentaryRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object{
        private var INSTANCE : CommentaryRepository? = null

        fun getInstance(): CommentaryRepository {
            var tempInstance =
                INSTANCE
            if (tempInstance != null)
                return tempInstance
            tempInstance = CommentaryRepository()
            INSTANCE = tempInstance
            return tempInstance
        }
    }

    fun createDoctorCommentary(commentary: Commentary){
        firestore.collection(COMMENT_COLLECTION).document().set(commentary)
    }

    fun getDoctorCommentary(doctorId : String): Query {
        return firestore.collection(COMMENT_COLLECTION).whereEqualTo("doctorId",doctorId)
    }
}