package com.benlefevre.monendo.mappers

import com.benlefevre.monendo.data.models.User
import com.google.firebase.auth.FirebaseUser

fun convertFirebaseUserIntoUser(firebaseUser: FirebaseUser): User {
        val id = firebaseUser.uid
        val name  = if (!firebaseUser.displayName.isNullOrEmpty()) firebaseUser.displayName!! else "No Name"
        val mail  = if (!firebaseUser.email.isNullOrEmpty()) firebaseUser.email!! else "No valid email"
        val url = if (firebaseUser.photoUrl != null) firebaseUser.photoUrl.toString() else "No valid photo's url"
        return User(id, name, mail,url)
}