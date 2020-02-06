package com.benlefevre.monendo.ui.viewmodels

import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.Symptom
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.data.repositories.MoodRepo
import com.benlefevre.monendo.data.repositories.PainRepo
import com.benlefevre.monendo.data.repositories.SymptomRepo
import com.benlefevre.monendo.data.repositories.UserActivitiesRepo
import com.nhaarman.mockitokotlin2.*
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
class PainFragmentViewModelTest {

    private lateinit var SUT : PainFragmentViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var painRepo: PainRepo
    @Mock
    lateinit var symptomRepo: SymptomRepo
    @Mock
    lateinit var moodRepo: MoodRepo
    @Mock
    lateinit var userActivitiesRepo: UserActivitiesRepo

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT = PainFragmentViewModel(painRepo,symptomRepo, moodRepo, userActivitiesRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun insertPain_success_correctRowIdReturned() = testDispatcher.runBlockingTest{
        whenever(painRepo.insertPain(any())).thenReturn(1)
        val pain = Pain(Date(),5,"bladder")

        val rowId = SUT.insertPain(pain)

        argumentCaptor<Pain>().apply {
            verify(painRepo).insertPain(capture())
            assertEquals(pain,firstValue)
            assertEquals(1,rowId)
        }
    }

    @Test
    fun insertMood_success_correctRowIdPassed() = runBlockingTest {
        val mood = Mood(value = "happy")
        val rowId : Long = 1

        SUT.insertMood(mood,rowId)

        argumentCaptor<Mood>().apply {
            verify(moodRepo).insertMood(capture())
            assertEquals(mood,firstValue)
            assertEquals(rowId,firstValue.painId)
        }
    }

    @Test
    fun insertSymptoms_success_correctRowIdPassed() = runBlockingTest {
        val date = Date()
        val rowId : Long = 1
        val symptoms = listOf(Symptom(name = "fever",date = date), Symptom(name = "burns",date = date))

        SUT.insertSymptoms(symptoms,rowId)

        argumentCaptor<List<Symptom>>().apply {
            verify(symptomRepo).insertAllSymptoms(capture())
            assertEquals(symptoms,firstValue)
            assertEquals(rowId, firstValue[0].painId)
            assertEquals(rowId, firstValue[1].painId)
        }
    }

    @Test
    fun insertActivities_success_correctRowIdPassed() = runBlockingTest {
        val date = Date()
        val rowId : Long = 1
        val activities = listOf(UserActivities(0,"sport",30,5,6,date),
            UserActivities(0,"sleep",60,5,2,date)
        )

        SUT.insertUserActivities(activities,rowId)

        argumentCaptor<List<UserActivities>>().apply {
            verify(userActivitiesRepo).insertAllUserActivities(capture())
            assertEquals(activities,firstValue)
            assertEquals(rowId,firstValue[0].painId)
            assertEquals(rowId,firstValue[1].painId)
        }
    }

    @Test
    fun insertAllUserInput_success_correctDataPassed() = runBlockingTest {
        val date = Date()
        val pain = Pain(date,5,"head")
        val mood = Mood(value = "sad")
        val symptoms = listOf(Symptom(name = "fever",date = date), Symptom(name = "burns",date = date))
        whenever(painRepo.insertPain(any())).thenReturn(1)

        SUT.insertUserInput(pain,mood,symptoms)

        argumentCaptor<Mood>().apply {
            verify(moodRepo).insertMood(capture())
            assertEquals(mood,firstValue)
            assertEquals(1,firstValue.painId)
        }

        argumentCaptor<List<Symptom>>().apply {
            verify(symptomRepo).insertAllSymptoms(capture())
            assertEquals(symptoms,firstValue)
            assertEquals(1,firstValue[0].painId)
            assertEquals(1,firstValue[1].painId)
        }
    }

    @Test
    fun insertAllUserInput_nullMood_noMoodRepoInteraction() = runBlockingTest {
        val date = Date()
        val pain = Pain(date,5,"head")
        val mood = null
        val symptoms = listOf(Symptom(name = "fever",date = date), Symptom(name = "burns",date = date))
        whenever(painRepo.insertPain(any())).thenReturn(1)

        SUT.insertUserInput(pain,mood,symptoms)

        verifyNoMoreInteractions(moodRepo)

        argumentCaptor<List<Symptom>>().apply {
            verify(symptomRepo).insertAllSymptoms(capture())
            assertEquals(symptoms,firstValue)
            assertEquals(1,firstValue[0].painId)
            assertEquals(1,firstValue[1].painId)
        }
    }

    @Test
    fun insertAllUserInput_emptySymptomsList_noSymptomRepoInteraction() = runBlockingTest {
        val date = Date()
        val pain = Pain(date,5,"head")
        val mood = Mood(value = "sad")
        val symptoms = listOf<Symptom>()
        whenever(painRepo.insertPain(any())).thenReturn(1)

        SUT.insertUserInput(pain,mood,symptoms)

        argumentCaptor<Mood>().apply {
            verify(moodRepo).insertMood(capture())
            assertEquals(mood,firstValue)
            assertEquals(1,firstValue.painId)
        }

        verifyNoMoreInteractions(symptomRepo)
    }
}