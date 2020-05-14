package com.benlefevre.monendo.doctor.repository

import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.login.User
import com.benlefevre.monendo.utils.COMMENT_COLLECTION
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommentaryRepository (private val firestore: FirebaseFirestore) {

    fun createDoctorCommentary(commentary: Commentary,user : User) {
        firestore.collection(COMMENT_COLLECTION).document("${commentary.doctorName}-${user.id}").set(commentary)
    }

    fun getDoctorCommentary(doctorId: String): Task<QuerySnapshot> {
        return firestore.collection(COMMENT_COLLECTION).whereEqualTo("doctorId", doctorId).get()
    }

    fun getCommentariesByUser(commentaryId : String): Task<DocumentSnapshot> {
        return firestore.collection(COMMENT_COLLECTION).document(commentaryId).get()
    }

    fun deleteCommentary(commentaryId : String): Task<Void> {
        return firestore.collection(COMMENT_COLLECTION).document(commentaryId).delete()
    }
}