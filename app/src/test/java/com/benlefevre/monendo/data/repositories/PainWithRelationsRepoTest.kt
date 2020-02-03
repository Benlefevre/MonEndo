package com.benlefevre.monendo.data.repositories

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.data.dao.PainWithRelationsDao
import com.benlefevre.monendo.data.models.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class PainWithRelationsRepoTest {

    lateinit var SUT: PainWithRelationsRepo

    @Mock
    lateinit var painWithRelationsDao: PainWithRelationsDao

    @Before
    fun setUp() {
        SUT = PainWithRelationsRepo(painWithRelationsDao)
    }

    @Test
    fun getPainWithRelationByPeriod_success_correctDataPassed() {
        val date1 = Date()
        val date2 = Date()
        SUT.getPainWithRelationByPeriod(date1, date2)
        argumentCaptor<Date>().apply {
            verify(painWithRelationsDao).getAllPainsWithRelations(capture(), capture())
            assertEquals(date1, firstValue)
            assertEquals(date2, secondValue)
        }
    }

    @Test
    fun getPainWithRelationByPeriod_success_correctDataReturned() {
        val painRelation = MutableLiveData<List<PainWithRelations>>(
            listOf(
                PainWithRelations(
                    Pain(
                        Date(),
                        5,
                        "head"
                    ),
                    listOf(Symptom(1,"fever",Date())),
                    listOf(UserActivities(1,"sleep",60,5,5,Date())),
                    listOf(Mood(1,"happy"))
                    )
            )
        )

        whenever(painWithRelationsDao.getAllPainsWithRelations(any(), any())).thenReturn(painRelation)
        val painReturned = SUT.getPainWithRelationByPeriod(Date(),Date())
        assertEquals(1, painReturned.value!!.size)
        assertEquals(5, painReturned.value!![0].pain.intensity)
        assertEquals("fever", painReturned.value!![0].symptoms[0].name)
        assertEquals("sleep", painReturned.value!![0].userActivities[0].name)
    }
}