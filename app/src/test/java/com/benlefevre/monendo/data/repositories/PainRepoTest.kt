package com.benlefevre.monendo.data.repositories

import com.benlefevre.monendo.data.dao.PainDao
import com.benlefevre.monendo.data.models.Pain
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class PainRepoTest{

    lateinit var SUT : PainRepo


    @Mock
    lateinit var painDao: PainDao

    @Before
    fun setUp() {
        SUT = PainRepo(painDao)
    }

    @Test
    fun getAllPains_success_painDaoInteraction() {
        SUT.pains
        verify(painDao).getAllPains()
    }

    @Test
    fun getPainByPeriod_success_correctDataPassed() {
        val beginDate = Date()
        val dateEnd = Date()
        SUT.getPainsByPeriod(beginDate, dateEnd)
        argumentCaptor<Date>().apply {
            verify(painDao).getPainByPeriod(capture(),capture())
            assertEquals(beginDate,firstValue)
            assertEquals(dateEnd,secondValue)
        }
    }

    @Test
    fun insertPain_success_painDaoInteraction() {
        val pain = Pain(Date(),5,"head")
        SUT.insertPain(pain)
        argumentCaptor<Pain>().apply {
            verify(painDao).insertPain(capture())
            assertEquals(pain,firstValue)
        }
    }

    @Test
    fun deleteAllPains_success_painDaoInteraction() {
        SUT.deleteAllPains()
        verify(painDao).deleteAllPain()
    }
}