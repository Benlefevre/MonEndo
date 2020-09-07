package com.benlefevre.monendo.database.repositories

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.dashboard.dao.PainWithRelationsDao
import com.benlefevre.monendo.dashboard.models.*
import com.benlefevre.monendo.dashboard.repository.PainWithRelationsRepo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class PainWithRelationsRepoTest {

    lateinit var SUT: PainWithRelationsRepo

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var painWithRelationsDao: PainWithRelationsDao

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT = PainWithRelationsRepo(
            painWithRelationsDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
    fun getPainRelationByPeriod_success_correctDataPassed() = testDispatcher.runBlockingTest {
        val date1 = Date()
        val date2 = Date()
        SUT.getPainRelationByPeriod(date1, date2)
        argumentCaptor<Date>().apply {
            verify(painWithRelationsDao).getAllPainsRelations(capture(), capture())
            assertEquals(date1, firstValue)
            assertEquals(date2, secondValue)
        }
    }

    @Test
    fun getPainRelationsByPeriod_success_correctDataReturned() = testDispatcher.runBlockingTest {
        val painRelation =
            listOf(
                PainWithRelations(
                    Pain(
                        Date(),
                        5,
                        "head"
                    ),
                    listOf(
                        Symptom(
                            1,
                            "fever",
                            Date()
                        )
                    ),
                    listOf(
                        UserActivities(
                            1,
                            "sleep",
                            60,
                            5,
                            5,
                            Date()
                        )
                    ),
                    listOf(
                        Mood(
                            1,
                            "happy"
                        )
                    )
                )
            )


        whenever(painWithRelationsDao.getAllPainsRelations(any(), any())).thenReturn(
            painRelation
        )
        val painReturned = SUT.getPainRelationByPeriod(Date(), Date())
        assertEquals(1, painReturned.size)
        assertEquals(5, painReturned[0].pain.intensity)
        assertEquals("fever", painReturned[0].symptoms[0].name)
        assertEquals("sleep", painReturned[0].userActivities[0].name)
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
                    listOf(
                        Symptom(
                            1,
                            "fever",
                            Date()
                        )
                    ),
                    listOf(
                        UserActivities(
                            1,
                            "sleep",
                            60,
                            5,
                            5,
                            Date()
                        )
                    ),
                    listOf(
                        Mood(
                            1,
                            "happy"
                        )
                    )
                )
            )
        )

        whenever(painWithRelationsDao.getAllPainsWithRelations(any(), any())).thenReturn(
            painRelation
        )
        val painReturned = SUT.getPainWithRelationByPeriod(Date(), Date())
        assertEquals(1, painReturned.value!!.size)
        assertEquals(5, painReturned.value!![0].pain.intensity)
        assertEquals("fever", painReturned.value!![0].symptoms[0].name)
        assertEquals("sleep", painReturned.value!![0].userActivities[0].name)
    }
}