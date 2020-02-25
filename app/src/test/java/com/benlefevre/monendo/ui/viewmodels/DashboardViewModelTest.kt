package com.benlefevre.monendo.ui.viewmodels

import com.benlefevre.monendo.data.repositories.PainWithRelationsRepo
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
class DashboardViewModelTest {

    private lateinit var SUT : DashboardViewModel

    @Mock
    lateinit var painRelationsRepo : PainWithRelationsRepo

    @Before
    fun setUp() {
        SUT = DashboardViewModel(painRelationsRepo)
    }

    @Test
    fun getPainRelationsBy7LastDays_success_correctDataPassed() {
        SUT.getPainRelationsBy7LastDays()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(),capture())
            assertEquals(SUT.date7,firstValue)
            assertEquals(SUT.today,secondValue)
        }
    }

    @Test
    fun getPainRelationsByLastMonth_success_correctDataPassed() {
        SUT.getPainRelationsByLastMonth()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(),capture())
            assertEquals(SUT.dateMonth,firstValue)
            assertEquals(SUT.today,secondValue)
        }
    }

    @Test
    fun getPainRelationsByLast6Months_success_correctDataPassed() {
        SUT.getPainRelationsByLast6Months()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(),capture())
            assertEquals(SUT.date6Months,firstValue)
            assertEquals(SUT.today,secondValue)
        }
    }

    @Test
    fun getPainRelationsByLastYear_success_correctDataPassed() {
        SUT.getPainRelationsByLastYear()
        argumentCaptor<Date> {
            verify(painRelationsRepo).getPainWithRelationByPeriod(capture(),capture())
            assertEquals(SUT.dateYear,firstValue)
            assertEquals(SUT.today,secondValue)
        }
    }
}