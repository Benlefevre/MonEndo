package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.utils.getOrAwaitValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class UserActivitiesDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var userActivitiesDao: UserActivitiesDao
    var rowId : Long = 0
    var rowId2 : Long = 0
    lateinit var date: Date

    @Rule
    @JvmField
    var instantTaskExecutorRule  = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        endoDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            EndoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        rowId = endoDatabase.painDao().insertPain(Pain(Date(),5,"head"))
        rowId2 = endoDatabase.painDao().insertPain(Pain(Date(),10,"bladder"))

        userActivitiesDao = endoDatabase.userActivitiesDao()
        date = Date()
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun insertAndGetAllUserActivities_success_correctDataReturned() {
        val userActivity = UserActivities(rowId,"sport",30,8,5,date)
        val userActivity2 = UserActivities(rowId2,"sleep",200,8,5,date)
        val activitiesList = listOf(userActivity,userActivity2)

        userActivitiesDao.insertAll(activitiesList)

        val userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)
        assertEquals(userActivity,userActivitiesReturned[0])
        assertEquals(userActivity2,userActivitiesReturned[1])
    }

    @Test
    fun getPainUserActivities_success_correctDataReturned() {
        val userActivity = UserActivities(rowId,"sport",30,8,5,date)
        val userActivity2 = UserActivities(rowId2,"sleep",200,8,5,date)
        val activitiesList = listOf(userActivity,userActivity2)

        userActivitiesDao.insertAll(activitiesList)

        var userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)

        userActivitiesReturned = endoDatabase.userActivitiesDao().getPainUserActivities(rowId).getOrAwaitValue()
        assertEquals(1,userActivitiesReturned.size)
        assertEquals(userActivity,userActivitiesReturned[0])

        userActivitiesReturned = endoDatabase.userActivitiesDao().getPainUserActivities(rowId2).getOrAwaitValue()
        assertEquals(1,userActivitiesReturned.size)
        assertEquals(userActivity2,userActivitiesReturned[0])
    }

    @Test
    fun deleteAllActivities_success_noDataWithNameReturned() {
        insertAndGetAllUserActivities_success_correctDataReturned()
        var userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)

        userActivitiesDao.deleteAllUserActivities("sleep")

        userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(1,userActivitiesReturned.size)
        assertNotEquals("sport",userActivitiesReturned[0].name)
        assertEquals("sleep",userActivitiesReturned[0].name)
    }

    @Test
    fun deleteAllSleepData_success_noDataWithNameReturned() {
        insertAndGetAllUserActivities_success_correctDataReturned()
        var userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)

        userActivitiesDao.deleteAllSleepDatas("sleep")

        userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(1,userActivitiesReturned.size)
        assertNotEquals("sleep",userActivitiesReturned[0].name)
        assertEquals("sport",userActivitiesReturned[0].name)
    }

    @Test
    fun getUserActivitiesByPeriod_success_correctDataReturned() {val date1 = with(Calendar.getInstance()){
        set(Calendar.MONTH,2)
        set(Calendar.YEAR,2018)
        time
    }
        val date2 = with(Calendar.getInstance()){
            set(Calendar.MONTH,12)
            set(Calendar.YEAR,2018)
            time
        }
        val date3 = with(Calendar.getInstance()){
            set(Calendar.MONTH,12)
            set(Calendar.YEAR,2019)
            time
        }
        val date4 = with(Calendar.getInstance()){
            set(Calendar.MONTH,1)
            set(Calendar.YEAR,2018)
            time
        }

        val userActivity1 = UserActivities(rowId,"sport",60,5,3,date1)
        val userActivity2 = UserActivities(rowId2,"sleep",60,5,3,date2)
        val activitiesList = listOf(userActivity1,userActivity2)

        userActivitiesDao.insertAll(activitiesList)

        var userActivitiesReturned = endoDatabase.userActivitiesDao().getAllUserActivities().getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)

        userActivitiesReturned = endoDatabase.userActivitiesDao().getUserActivitiesByPeriod(date3,date).getOrAwaitValue()
        assertEquals(0,userActivitiesReturned.size)

        userActivitiesReturned = endoDatabase.userActivitiesDao().getUserActivitiesByPeriod(date4,date1).getOrAwaitValue()
        assertEquals(1,userActivitiesReturned.size)
        assertEquals(userActivity1,userActivitiesReturned[0])

        userActivitiesReturned = endoDatabase.userActivitiesDao().getUserActivitiesByPeriod(date1,date3).getOrAwaitValue()
        assertEquals(2,userActivitiesReturned.size)
        assertEquals(userActivity1,userActivitiesReturned[0])
        assertEquals(userActivity2,userActivitiesReturned[1])


    }
}