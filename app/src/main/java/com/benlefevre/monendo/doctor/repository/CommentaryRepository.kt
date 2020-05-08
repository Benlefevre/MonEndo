package com.benlefevre.monendo.doctor.repository

import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.utils.COMMENT_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommentaryRepository (private val firestore: FirebaseFirestore) {

    fun createDoctorCommentary(commentary: Commentary) {
        firestore.collection(COMMENT_COLLECTION).document().set(commentary)
    }

    fun getDoctorCommentary(doctorId: String): Task<QuerySnapshot> {
        return firestore.collection(COMMENT_COLLECTION).whereEqualTo("doctorId", doctorId).get()
    }
}