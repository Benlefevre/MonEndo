package com.benlefevre.monendo.database.repositories

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.dashboard.dao.MoodDao
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.repository.MoodRepo
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

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MoodRepoTest {

    lateinit var SUT: MoodRepo

    @Mock
    lateinit var moodDao: MoodDao

    @Before
    fun setUp() {
        SUT = MoodRepo(moodDao)
    }

    @Test
    fun getPainMood_correctDataReturned() {
        val moods: MutableLiveData<Mood> = MutableLiveData((Mood(
            0,
            "happy"
        )))
        whenever(moodDao.getPainMood(0)).thenReturn(moods)
        val mood = SUT.getPainMood(0)
        assertEquals(0, mood.value!!.painId)
        assertEquals("happy", mood.value!!.value)
    }

    @Test
    fun insertMood_moodDaoInteraction() = runBlockingTest {
        val mood = Mood(0, "happy")
        SUT.insertMood(mood)
        argumentCaptor<Mood>().apply {
            verify(moodDao).insert(capture())
            assertEquals(mood, firstValue)
        }
    }

    @Test
    fun deleteMood_success_moodDaoInteraction() = runBlockingTest {
        SUT.deleteAllMoods()
        verify(moodDao).deleteAllMoods()
    }

    @Test
    fun moods(){
        SUT.moods
        verify(moodDao).getAllMoods()
    }

}