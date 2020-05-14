package com.benlefevre.monendo.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.fertility.FertilityViewModel
import com.benlefevre.monendo.fertility.models.Temperature
import com.benlefevre.monendo.fertility.temperature.TemperatureRepo
import com.benlefevre.monendo.utils.CURRENT_MENS
import com.benlefevre.monendo.utils.DURATION
import com.benlefevre.monendo.utils.NEXT_MENS
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

    @Mock
    lateinit var preferences: SharedPreferences

    @Mock
    lateinit var editor : SharedPreferences.Editor

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT =
            FertilityViewModel(temperatureRepo, preferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTemp() = testDispatcher.runBlockingTest {
        val date = Date()
        val temp =
            Temperature(36.5f, date)
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
        val temperatures = MutableLiveData(listOf(
            Temperature(
                36.5f,
                Date()
            )
        ))
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

    @Test
    fun getCorrectUserInputs_todayBeforeNextMens_correctDataReturned(){
        whenever(preferences.getString(CURRENT_MENS,null)).thenReturn("01/01/20")
        whenever(preferences.getString(NEXT_MENS,null)).thenReturn("01/01/29")
        whenever(preferences.getString(DURATION,null)).thenReturn("28")
        val correctUserInput = SUT.getCorrectUserInput()
        verify(preferences).getString(NEXT_MENS,null)
        assertEquals("01/01/20",correctUserInput[0])
        assertEquals("28",correctUserInput[1])
    }

    @Test
    fun getCorrectUserInputs_todayAfterNextMens_correctDataReturned(){
        whenever(preferences.getString(NEXT_MENS,null)).thenReturn("10/01/20")
        whenever(preferences.getString(DURATION,null)).thenReturn("28")
        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        val correctUserInput = SUT.getCorrectUserInput()
        verify(preferences).getString(NEXT_MENS,null)
        assertEquals("10/01/20",correctUserInput[0])
        assertEquals("28",correctUserInput[1])
    }
}