package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.*
import com.benlefevre.monendo.utils.getOrAwaitValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class PainWithRelationDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var painDao: PainDao
    lateinit var moodDao: MoodDao
    lateinit var symptomDao: SymptomDao
    lateinit var userActivitiesDao: UserActivitiesDao
    lateinit var painWithRelationsDao: PainWithRelationsDao
    lateinit var date: Date
    var rowId : Long = 0

    @Rule
    @JvmField
    var instantTaskExecutorRule  = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        endoDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().context,
            EndoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        painDao = endoDatabase.painDao()
        moodDao = endoDatabase.moodDao()
        symptomDao = endoDatabase.symptomDao()
        userActivitiesDao = endoDatabase.userActivitiesDao()
        painWithRelationsDao = endoDatabase.painRelationDao()

        date = Date()
        rowId = painDao.insertPain(Pain(date,5,"head"))
        moodDao.insert(Mood(rowId,"happy"))
        symptomDao.insertAll(listOf(Symptom(rowId,"fever",date)))
        userActivitiesDao.insertAll(listOf(UserActivities(rowId,"sleep",60,5,5,date)))
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun getPainWithRelationByPeriod_success_correctDataReturned() {
        val painRelationReturned : List<PainWithRelations> = painWithRelationsDao.getAllPainsWithRelations(date,Date()).getOrAwaitValue()
        assertEquals(1,painRelationReturned.size)
        assertEquals(5,painRelationReturned[0].pain.intensity)
        assertEquals("happy",painRelationReturned[0].moods[0].value)
        assertEquals(rowId,painRelationReturned[0].moods[0].painId)
        assertEquals("fever",painRelationReturned[0].symptoms[0].name)
        assertEquals(rowId,painRelationReturned[0].symptoms[0].painId)
    }
}