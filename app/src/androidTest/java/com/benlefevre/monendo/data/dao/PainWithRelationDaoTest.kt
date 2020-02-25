package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.*
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
class PainWithRelationDaoTest {

    private lateinit var endoDatabase: EndoDatabase
    private lateinit var painDao: PainDao
    private lateinit var moodDao: MoodDao
    private lateinit var symptomDao: SymptomDao
    private lateinit var userActivitiesDao: UserActivitiesDao
    private lateinit var painWithRelationsDao: PainWithRelationsDao
    lateinit var date: Date
    private lateinit var dateAnte : Date
    private var rowId : Long = 0
    private var rowId2 : Long = 0

    @get:Rule
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
        dateAnte = with(Calendar.getInstance()){
            add(Calendar.DAY_OF_YEAR,-60)
            time
        }
        runBlocking {
            rowId2 = painDao.insertPain(Pain(dateAnte,10,"back"))
            moodDao.insert(Mood(rowId2,"sad"))
            symptomDao.insertAll(listOf(Symptom(rowId2,"bleeding",dateAnte)))
            userActivitiesDao.insertAll(listOf(UserActivities(rowId2,"stress",0,10,10,dateAnte)))

            rowId = painDao.insertPain(Pain(date,5,"head"))
            moodDao.insert(Mood(rowId,"happy"))
            symptomDao.insertAll(listOf(Symptom(rowId,"fever",date)))
            userActivitiesDao.insertAll(listOf(UserActivities(rowId,"sleep",60,5,5,date)))

        }
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

    @Test
    fun getPainWithRelationByPeriod_successMultiDate_correctDataOrderReturned() {
        val painRelationReturned : List<PainWithRelations> = painWithRelationsDao.getAllPainsWithRelations(dateAnte,Date()).getOrAwaitValue()
        assertEquals(2,painRelationReturned.size)
        assertEquals(10,painRelationReturned[0].pain.intensity)
        assertEquals("sad",painRelationReturned[0].moods[0].value)
        assertEquals(rowId2,painRelationReturned[0].moods[0].painId)
        assertEquals("bleeding",painRelationReturned[0].symptoms[0].name)
        assertEquals(rowId2,painRelationReturned[0].symptoms[0].painId)

        assertEquals(5,painRelationReturned[1].pain.intensity)
        assertEquals("happy",painRelationReturned[1].moods[0].value)
        assertEquals(rowId,painRelationReturned[1].moods[0].painId)
        assertEquals("fever",painRelationReturned[1].symptoms[0].name)
        assertEquals(rowId,painRelationReturned[1].symptoms[0].painId)
    }
}