package com.benlefevre.monendo.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.data.models.Temperature
import com.benlefevre.monendo.data.repositories.TemperatureRepo
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
class FertilityViewModelTest {

    private lateinit var SUT : FertilityViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var temperatureRepo: TemperatureRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT = FertilityViewModel(temperatureRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTemp() = testDispatcher.runBlockingTest {
        val date = Date()
        val temp = Temperature(36.5f,date)
        SUT.createTemp(temp)
        argumentCaptor<Temperature>().apply {
            verify(temperatureRepo).insertTemperature(capture())
            assertEquals(temp.value,firstValue.value)
            assertEquals(temp.date,firstValue.date)
        }
    }

    @Test
    fun getAllTemperatures() {
        val temps = SUT.getAllTemperatures()
        assertEquals(temps,temperatureRepo.temperatures)
    }

    @Test
    fun getTemperaturesByPeriod() {
        val temperatures = MutableLiveData(listOf(Temperature(36.5f,Date())))
        whenever(temperatureRepo.getTemperaturesByPeriod(any(), any())).thenReturn(temperatures)
        val end = Date()
        val begin = with(Calendar.getInstance()){
            set(Calendar.MONTH,1)
            time
        }
        val temps = SUT.getTemperaturesByPeriod(begin,end)
        argumentCaptor<Date>().apply {
            verify(temperatureRepo).getTemperaturesByPeriod(capture(),capture())
            assertEquals(begin,firstValue)
            assertEquals(end,secondValue)
            assertEquals(temps,temperatures)
        }
    }

    @Test
    fun deleteAllTemperatures() = testDispatcher.runBlockingTest {
        SUT.deleteAllTemperatures()
        verify(temperatureRepo).deleteAllTemperatures()
    }
}