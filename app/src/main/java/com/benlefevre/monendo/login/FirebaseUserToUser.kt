package com.benlefevre.monendo.login

import com.benlefevre.monendo.utils.NO_MAIL
import com.benlefevre.monendo.utils.NO_NAME
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.google.firebase.auth.FirebaseUser

fun convertFirebaseUserIntoUser(firebaseUser: FirebaseUser): User {
        val id = firebaseUser.uid
        val name  = if (!firebaseUser.displayName.isNullOrEmpty()) firebaseUser.displayName!! else NO_NAME
        val mail  = if (!firebaseUser.email.isNullOrEmpty()) firebaseUser.email!! else NO_MAIL
        val url = if (firebaseUser.photoUrl != null) firebaseUser.photoUrl.toString() else NO_PHOTO_URL
        return User(id, name, mail, url)
}