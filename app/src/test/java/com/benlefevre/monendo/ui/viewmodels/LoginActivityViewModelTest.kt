package com.benlefevre.monendo.ui.viewmodels

import com.benlefevre.monendo.data.models.User
import com.benlefevre.monendo.data.repositories.FirestoreRepo
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginActivityViewModelTest{

    lateinit var SUT : LoginActivityViewModel

    @Mock
    lateinit var firestoreRepo : FirestoreRepo

    lateinit var user : User

    @Before
    fun setUp() {
        SUT = LoginActivityViewModel(firestoreRepo)
        user = User("","","","")
    }

    @Test
    fun createUserInFirestore_success_correctDataPassed() {
        SUT.createUserInFirestore(user)
        argumentCaptor<User>().apply {
            verify(firestoreRepo).createUserInFirestore(capture())
            assertEquals(user,firstValue)
        }
    }

}