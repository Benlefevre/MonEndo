package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.data.dao.SymptomDao
import com.benlefevre.monendo.data.models.Symptom
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SymptomRepoTest{

    lateinit var SUT : SymptomRepo

    @Mock
    lateinit var symptomDao: SymptomDao

    @Before
    fun setUp() {
        SUT = SymptomRepo(symptomDao)
    }

    @Test
    fun getAllSymptoms_success_correctDataReturned() {
        SUT.symptoms
        verify(symptomDao).getAllSymptoms()
    }

    @Test
    fun getPainSymptoms_success_correctDataReturned() {
        val symptoms = MutableLiveData<List<Symptom>>(listOf(Symptom(0,"burns", Date()),Symptom(0,"fever",
            Date()
        )))
        whenever(symptomDao.getPainSymptoms(any())).thenReturn(symptoms)
        val symptomsReturned = SUT.getPainSymptoms(0)
        assertEquals(symptoms.value!!.size, symptomsReturned.value!!.size)
        assertEquals(symptoms.value!![0], symptomsReturned.value!![0])
        assertEquals(0, symptomsReturned.value!![0].painId)
    }

    @Test
    fun getSymptomsByPeriod_success_correctDataPassed(){
        val beginDate = Date()
        val dateEnd = Date()
        SUT.getSymptomsByPeriod(beginDate, dateEnd)
        argumentCaptor<Date>().apply {
            verify(symptomDao).getSymptomsByPeriod(capture(),capture())
            assertEquals(beginDate,firstValue)
            assertEquals(dateEnd,secondValue)
        }
    }

    @Test
    fun insertSymptoms_success_correctDataPassed() = runBlockingTest {
        val symptoms = listOf(Symptom(0,"fever",Date()), Symptom(1,"bloating",Date()))
        SUT.insertAllSymptoms(symptoms)
        argumentCaptor<List<Symptom>>().apply {
            verify(symptomDao).insertAll(capture())
            assertEquals(symptoms,firstValue)
            assertEquals(symptoms[0].name,firstValue[0].name)
        }
    }

    @Test
    fun deleteAllSymptoms_success_symptomDaoInteraction() = runBlockingTest {
        SUT.deleteAllSymptoms()
        verify(symptomDao).deleteAllSymptoms()
    }
}