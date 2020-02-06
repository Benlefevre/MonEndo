package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.utils.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class PainDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var painDao: PainDao
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

        painDao = endoDatabase.painDao()
        date = Date()
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun insertAndGetPain_success_correctDataReturned() = runBlocking {
        val pain1 = Pain(date,5,"head")
        val pain2 = Pain(date,10,"bladder")

        painDao.insertPain(pain1)
        painDao.insertPain(pain2)

        val painReturned = endoDatabase.painDao().getAllPains().getOrAwaitValue()
        assertEquals(pain1,painReturned[0])
        assertEquals(pain2,painReturned[1])
    }

    @Test
    fun deleteAllPains_success_noPainsInDb() = runBlocking {
        insertAndGetPain_success_correctDataReturned()

        var painReturned = endoDatabase.painDao().getAllPains().getOrAwaitValue()
        assertEquals(2,painReturned.size)

        painDao.deleteAllPain()

        painReturned = endoDatabase.painDao().getAllPains().getOrAwaitValue()
        assertEquals(0,painReturned.size)
    }

    @Test
    fun getPainByPeriod_success_returnedCorrectData() = runBlocking {
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

        val pain1 = Pain(date1,5,"head")
        val pain2 = Pain(date2,10,"bladder")

        painDao.insertPain(pain1)
        painDao.insertPain(pain2)

        var painReturned = endoDatabase.painDao().getPainByPeriod(date3,date).getOrAwaitValue()
        assertEquals(0,painReturned.size)

        painReturned = endoDatabase.painDao().getPainByPeriod(date4,date1).getOrAwaitValue()
        assertEquals(1,painReturned.size)

        painReturned = endoDatabase.painDao().getPainByPeriod(date1,date3).getOrAwaitValue()
        assertEquals(2,painReturned.size)

    }
}