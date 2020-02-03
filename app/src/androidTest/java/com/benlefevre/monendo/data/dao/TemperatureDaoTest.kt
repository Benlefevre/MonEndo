package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.Temperature
import com.benlefevre.monendo.utils.getOrAwaitValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class TemperatureDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var temperatureDao: TemperatureDao
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

        temperatureDao = endoDatabase.temperatureDao()
        date = Date()
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun insertAndGetAllTemperatures_success_correctDataReturned() {
        val temp1 = Temperature(37.5,date)
        val temp2 = Temperature(39.5,date)

        temperatureDao.insert(temp1)
        temperatureDao.insert(temp2)

        val tempReturned = endoDatabase.temperatureDao().getAllTemperatures().getOrAwaitValue()
        assertEquals(2,tempReturned.size)
        assertEquals(temp1,tempReturned[0])
        assertEquals(temp2,tempReturned[1])
    }

    @Test
    fun deleteAllTemperatures_success_noDataReturned() {
        insertAndGetAllTemperatures_success_correctDataReturned()
        var tempReturned = endoDatabase.temperatureDao().getAllTemperatures().getOrAwaitValue()
        assertEquals(2,tempReturned.size)

        temperatureDao.deleteAllTemperatures()

        tempReturned = endoDatabase.temperatureDao().getAllTemperatures().getOrAwaitValue()
        assertEquals(0,tempReturned.size)
    }

    @Test
    fun getTemperatureByPeriod_success_correctDataReturned() {
        val date1 = with(Calendar.getInstance()){
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

        val temp1 = Temperature(36.5,date1)
        val temp2 = Temperature(38.0,date2)

        temperatureDao.insert(temp1)
        temperatureDao.insert(temp2)

        var tempReturned = endoDatabase.temperatureDao().getAllTemperatures().getOrAwaitValue()
        assertEquals(2,tempReturned.size)

        tempReturned = endoDatabase.temperatureDao().getTemperaturesByPeriod(date3,date).getOrAwaitValue()
        assertEquals(0,tempReturned.size)

        tempReturned = endoDatabase.temperatureDao().getTemperaturesByPeriod(date4,date1).getOrAwaitValue()
        assertEquals(1,tempReturned.size)
        assertEquals(temp1,tempReturned[0])

        tempReturned = endoDatabase.temperatureDao().getTemperaturesByPeriod(date1,date3).getOrAwaitValue()
        assertEquals(2,tempReturned.size)
        assertEquals(temp1,tempReturned[0])
        assertEquals(temp2,tempReturned[1])
    }
}