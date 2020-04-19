package com.benlefevre.monendo.api

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DoctorRepositoryTest{

    lateinit var SUT : DoctorRepository

    @Mock
    lateinit var cpamService: CpamService

    @Before
    fun setUp() {
        SUT = DoctorRepository(cpamService)
    }

    @Test
    fun getDoctor_success_correctDataPassed() = runBlockingTest{
        val map = mapOf("1" to "1", "2" to "2")
        SUT.getDoctors(map)
        argumentCaptor<Map<String,String>>().apply {
            verify(cpamService).getDoctors(capture())
            assertEquals("1", firstValue["1"])
            assertEquals("2",firstValue["2"])
        }
    }
}