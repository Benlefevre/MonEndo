package com.benlefevre.monendo.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.dashboard.dao.SymptomDao
import com.benlefevre.monendo.dashboard.models.Pain
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.database.EndoDatabase
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
class SymptomDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var symptomDao: SymptomDao
    var rowId: Long = 0
    var rowId2: Long = 0
    lateinit var date: Date

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        endoDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            EndoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        runBlocking {
            rowId = endoDatabase.painDao().insertPain(
                Pain(
                    Date(),
                    5,
                    "head"
                )
            )
            rowId2 = endoDatabase.painDao().insertPain(
                Pain(
                    Date(),
                    10,
                    "bladder"
                )
            )
        }

        symptomDao = endoDatabase.symptomDao()
        date = Date()
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun insertAndGetAllSymptoms_success_correctDataReturned() = runBlocking {
        val symptom =
            Symptom(rowId, "burns", date)
        val symptom2 = Symptom(
            rowId2,
            "fever",
            date
        )
        val symptomList = listOf(symptom, symptom2)

        symptomDao.insertAll(symptomList)

        val symptomReturned = endoDatabase.symptomDao().getAllSymptoms().getOrAwaitValue()
        assertEquals(2, symptomReturned.size)
        assertEquals(symptom, symptomReturned[0])
        assertEquals(symptom2, symptomReturned[1])
    }

    @Test
    fun deleteAllSymptoms_success_noDataReturned() = runBlocking{
        insertAndGetAllSymptoms_success_correctDataReturned()
        var symptomReturned = endoDatabase.symptomDao().getAllSymptoms().getOrAwaitValue()
        assertEquals(2, symptomReturned.size)

        symptomDao.deleteAllSymptoms()

        symptomReturned = endoDatabase.symptomDao().getAllSymptoms().getOrAwaitValue()
        assertEquals(0, symptomReturned.size)
    }

    @Test
    fun getPainSymptom_success_correctDataReturned() = runBlocking{
        val symptom =
            Symptom(rowId, "burns", date)
        val symptom2 = Symptom(
            rowId2,
            "fever",
            date
        )
        val symptomList = listOf(symptom, symptom2)

        symptomDao.insertAll(symptomList)

        var symptomReturned = endoDatabase.symptomDao().getAllSymptoms().getOrAwaitValue()
        assertEquals(2, symptomReturned.size)

        symptomReturned = endoDatabase.symptomDao().getPainSymptoms(rowId).getOrAwaitValue()
        assertEquals(1, symptomReturned.size)
        assertEquals(symptom, symptomReturned[0])

        symptomReturned = endoDatabase.symptomDao().getPainSymptoms(rowId2).getOrAwaitValue()
        assertEquals(1, symptomReturned.size)
        assertEquals(symptom2, symptomReturned[0])
    }

    @Test
    fun getSymptomByPeriod_success_correctDataReturned() = runBlocking {
        val date1 = with(Calendar.getInstance()) {
            set(Calendar.MONTH, 2)
            set(Calendar.YEAR, 2018)
            time
        }
        val date2 = with(Calendar.getInstance()) {
            set(Calendar.MONTH, 12)
            set(Calendar.YEAR, 2018)
            time
        }
        val date3 = with(Calendar.getInstance()) {
            set(Calendar.MONTH, 12)
            set(Calendar.YEAR, 2019)
            time
        }
        val date4 = with(Calendar.getInstance()) {
            set(Calendar.MONTH, 1)
            set(Calendar.YEAR, 2018)
            time
        }

        val symptom = Symptom(
            rowId,
            "bloating",
            date1
        )
        val symptom2 = Symptom(
            rowId2,
            "cramps",
            date2
        )

        val symptomList = listOf(symptom, symptom2)

        symptomDao.insertAll(symptomList)
        var symptomReturned = endoDatabase.symptomDao().getAllSymptoms().getOrAwaitValue()
        assertEquals(2, symptomReturned.size)

        symptomReturned =
            endoDatabase.symptomDao().getSymptomsByPeriod(date3, date).getOrAwaitValue()
        assertEquals(0, symptomReturned.size)

        symptomReturned =
            endoDatabase.symptomDao().getSymptomsByPeriod(date4, date1).getOrAwaitValue()
        assertEquals(1, symptomReturned.size)
        assertEquals(symptom, symptomReturned[0])

        symptomReturned =
            endoDatabase.symptomDao().getSymptomsByPeriod(date1, date3).getOrAwaitValue()
        assertEquals(2, symptomReturned.size)
        assertEquals(symptom, symptomReturned[0])
        assertEquals(symptom2, symptomReturned[1])


    }
}