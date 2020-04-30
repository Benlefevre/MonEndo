package com.benlefevre.monendo.database.repositories

import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.fertility.temperature.TemperatureDao
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
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
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class TemperatureRepoTest{

    lateinit var SUT : TemperatureRepo

    @Mock
    lateinit var temperatureDao: TemperatureDao

    @Before
    fun setUp() {
        SUT = TemperatureRepo(
            temperatureDao
        )
    }

    @Test
    fun getAllTemperature_success_correctDataReturned() {
        SUT.temperatures
        verify(temperatureDao).getAllTemperatures()
    }

    @Test
    fun getTemperaturesByPeriod_success_correctDataPassed() {
        val dateBegin = Date()
        val dateEnd = Date()
        SUT.getTemperaturesByPeriod(dateBegin, dateEnd)
        argumentCaptor<Date>().apply {
            verify(temperatureDao).getTemperaturesByPeriod(capture(),capture())
            assertEquals(dateBegin,firstValue)
            assertEquals(dateEnd,secondValue)
        }
    }

    @Test
    fun insertTemperature_success_correctDataPassed() = runBlockingTest {
        val temp = Temperature(
            12.5f,
            Date()
        )
        SUT.insertTemperature(temp)
        argumentCaptor<Temperature>().apply {
            verify(temperatureDao).insert(capture())
            assertEquals(temp,firstValue)
        }
    }

    @Test
    fun deleteTemperature_success_temperatureDaoInteraction() = runBlockingTest {
        SUT.deleteAllTemperatures()
        verify(temperatureDao).deleteAllTemperatures()
    }
}