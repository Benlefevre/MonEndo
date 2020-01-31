package com.benlefevre.monendo.mappers

import com.benlefevre.monendo.utils.NO_MAIL
import com.benlefevre.monendo.utils.NO_NAME
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.google.firebase.auth.FirebaseUser
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FirebaseUserToUserKtTest{

    @Mock
    lateinit var firebaseUser : FirebaseUser

    @Test
    fun convertFirebaseUserIntoUser_success_correctUserNameReturned() {
        success()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals("name",user.name)
    }

    @Test
    fun convertFirebaseUserIntoUser_failure_correctUserNameReturned() {
        failure()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals(NO_NAME,user.name)
    }

    @Test
    fun convertFirebaseUserIntoUser_success_correctUserMailReturned() {
        success()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals("mail",user.mail)
    }

    @Test
    fun convertFirebaseUserIntoUser_failure_correctUserMailReturned() {
        failure()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals(NO_MAIL,user.mail)
    }

    @Test
    fun convertFirebaseUserIntoUser_success_correctUserIdReturned() {
        success()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals("id",user.id)
    }

    @Test
    fun convertFirebaseUserIntoUser_failure_correctUserUrlReturned() {
        failure()
        val user = convertFirebaseUserIntoUser(
            firebaseUser
        )
        assertEquals(NO_PHOTO_URL,user.photoUrl)
    }


    fun success(){
        whenever(firebaseUser.uid).thenReturn("id")
        whenever(firebaseUser.displayName).thenReturn("name")
        whenever(firebaseUser.email).thenReturn("mail")
        whenever(firebaseUser.photoUrl).thenReturn(null)
    }

    fun failure(){
        whenever(firebaseUser.uid).thenReturn("id")
        whenever(firebaseUser.displayName).thenReturn(NO_NAME)
        whenever(firebaseUser.email).thenReturn(NO_MAIL)
        whenever(firebaseUser.photoUrl).thenReturn(null)
    }
}