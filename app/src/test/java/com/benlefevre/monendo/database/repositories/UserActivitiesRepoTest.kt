package com.benlefevre.monendo.database.repositories

import androidx.lifecycle.MutableLiveData
import com.benlefevre.monendo.dashboard.dao.UserActivitiesDao
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.dashboard.repository.UserActivitiesRepo
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
class UserActivitiesRepoTest {

    lateinit var SUT: UserActivitiesRepo

    @Mock
    lateinit var userActivitiesDao: UserActivitiesDao

    @Before
    fun setUp() {
        SUT = UserActivitiesRepo(
            userActivitiesDao
        )
    }

    @Test
    fun getAllUserActivities_success_correctDataReturned() {
        SUT.userActivities
        verify(userActivitiesDao).getAllUserActivities()
    }

    @Test
    fun getPainUserActivities_success_correctDataReturned() {
        val userActivities = MutableLiveData<List<UserActivities>>(
            listOf(
                UserActivities(
                    0, "sport", 60, 5, 5,
                    Date()
                )
            )
        )
        whenever(userActivitiesDao.getPainUserActivities(any())).thenReturn(userActivities)
        val userActivitiesReturned = SUT.getPainUserActivities(0)
        argumentCaptor<Long>().apply {
            verify(userActivitiesDao).getPainUserActivities(capture())
            assertEquals(0, firstValue)
        }
        assertEquals(userActivities.value!!.size, userActivitiesReturned.value!!.size)
        assertEquals(userActivities.value!![0].name, userActivitiesReturned.value!![0].name)
    }

    @Test
    fun getUserActivitiesByPeriod_success_correctDataPassed() {
        val beginDate = Date()
        val dateEnd = Date()
        SUT.getUserActivitiesByPeriod(beginDate, dateEnd)
        argumentCaptor<Date>().apply {
            verify(userActivitiesDao).getUserActivitiesByPeriod(capture(), capture())
            assertEquals(beginDate, firstValue)
            assertEquals(dateEnd, secondValue)
        }
    }

    @Test
    fun insertAllUserActivities_success_correctDataPassed() = runBlockingTest {
        val userActivities = listOf(
            UserActivities(
                0,
                "sport",
                15,
                5,
                5,
                Date()
            )
        )
        SUT.insertAllUserActivities(userActivities)
        argumentCaptor<List<UserActivities>>().apply {
            verify(userActivitiesDao).insertAll(capture())
            assertEquals(userActivities,firstValue)
        }
    }

    @Test
    fun deleteAllUserActivities_success_correctDataPassed() = runBlockingTest{
        SUT.deleteAllUserActivities("sport")
        argumentCaptor<String>().apply {
            verify(userActivitiesDao).deleteAllUserActivities(capture())
            assertEquals("sport",firstValue)
        }
    }

    @Test
    fun deleteAllSleepDatas_success_correctDataPassed() = runBlockingTest{
        SUT.deleteAllSleepDatas("sleep")
        argumentCaptor<String>().apply {
            verify(userActivitiesDao).deleteAllSleepDatas(capture())
            assertEquals("sleep",firstValue)
        }
    }

}