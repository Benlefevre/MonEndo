package com.benlefevre.monendo.ui.viewmodels

import com.benlefevre.monendo.dashboard.repository.PainWithRelationsRepo
import com.benlefevre.monendo.dashboard.viewmodels.DashboardViewModel
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
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
class DashboardViewModelTest {

    private lateinit var SUT: DashboardViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var painRelationsRepo: PainWithRelationsRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT =
            DashboardViewModel(
                painRelationsRepo
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getPainRelationsBy7LastDays_success_correctDataPassed() {
        SUT.getPainRelationsBy7LastDays()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(), capture())
            assertEquals(SUT.date7, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsByLastMonth_success_correctDataPassed() {
        SUT.getPainRelationsByLastMonth()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(), capture())
            assertEquals(SUT.dateMonth, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsByLast6Months_success_correctDataPassed() {
        SUT.getPainRelationsByLast6Months()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(), capture())
            assertEquals(SUT.date6Months, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsByLastYear_success_correctDataPassed() {
        SUT.getPainRelationsByLastYear()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(), capture())
            assertEquals(SUT.dateYear, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsFor7Days() = testDispatcher.runBlockingTest {
        SUT.getPainsRelations7days()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainRelationByPeriod(capture(), capture())
            assertEquals(SUT.date7, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsFor30Days() = testDispatcher.runBlockingTest {
        SUT.getPainsRelations30days()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainRelationByPeriod(capture(), capture())
            assertEquals(SUT.dateMonth, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsFor180Days() = testDispatcher.runBlockingTest {
        SUT.getPainsRelations180days()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainRelationByPeriod(capture(), capture())
            assertEquals(SUT.date6Months, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }

    @Test
    fun getPainRelationsFor360Days() = testDispatcher.runBlockingTest {
        SUT.getPainsRelations360days()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainRelationByPeriod(capture(), capture())
            assertEquals(SUT.dateYear, firstValue)
            assertEquals(SUT.today, secondValue)
        }
    }
}