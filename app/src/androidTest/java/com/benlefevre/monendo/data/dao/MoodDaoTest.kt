package com.benlefevre.monendo.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.benlefevre.monendo.data.database.EndoDatabase
import com.benlefevre.monendo.data.models.Mood
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
class MoodDaoTest {

    lateinit var endoDatabase: EndoDatabase
    lateinit var moodDao: MoodDao
    var rowId: Long = 0
    var rowId2: Long = 0

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
            rowId = endoDatabase.painDao().insertPain(Pain(Date(), 5, "head"))
            rowId2 = endoDatabase.painDao().insertPain(Pain(Date(), 10, "bladder"))
        }

        moodDao = endoDatabase.moodDao()
    }

    @After
    fun tearDown() {
        endoDatabase.close()
    }

    @Test
    fun insertAndGetMood_success_correctDataReturned() = runBlocking{
        val mood = Mood(rowId, "happy")
        val mood2 = Mood(rowId2, "happy")

        moodDao.insert(mood)
        moodDao.insert(mood2)
        val moodReturned = endoDatabase.moodDao().getAllMoods().getOrAwaitValue()
        assertEquals(2, moodReturned.size)
        assertEquals(mood, moodReturned[0])
        assertEquals(mood2, moodReturned[1])
    }

    @Test
    fun deleteAllMoods_success_noDataReturned() = runBlocking {
        insertAndGetMood_success_correctDataReturned()
        var moodReturned = endoDatabase.moodDao().getAllMoods().getOrAwaitValue()
        assertEquals(2, moodReturned.size)

        moodDao.deleteAllMoods()

        moodReturned = endoDatabase.moodDao().getAllMoods().getOrAwaitValue()
        assertEquals(0, moodReturned.size)
    }

    @Test
    fun getPainMood_success_correctDataReturned() = runBlocking {
        val mood1 = Mood(rowId, "happy")
        val mood2 = Mood(rowId2, "sad")
        moodDao.insert(mood2)
        moodDao.insert(mood1)

        var moodReturned = endoDatabase.moodDao().getPainMood(rowId).getOrAwaitValue()
        assertEquals(mood1, moodReturned)

        moodReturned = endoDatabase.moodDao().getPainMood(rowId2).getOrAwaitValue()
        assertEquals(mood2, moodReturned)
    }
}