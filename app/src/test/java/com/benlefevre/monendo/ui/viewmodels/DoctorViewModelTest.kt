package com.benlefevre.monendo.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.benlefevre.monendo.doctor.CommentaryRepository
import com.benlefevre.monendo.doctor.DoctorUiState
import com.benlefevre.monendo.doctor.DoctorViewModel
import com.benlefevre.monendo.doctor.api.*
import com.benlefevre.monendo.doctor.createDoctorsFromCpamApi
import com.google.firebase.firestore.FirebaseFirestore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DoctorViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    lateinit var SUT: DoctorViewModel

    @Mock
    lateinit var doctorRepository: DoctorRepository

    @Mock
    lateinit var commentaryRepository: CommentaryRepository

    @Mock
    lateinit var firebaseFirestore: FirebaseFirestore

    @Mock
    lateinit var doctorObserver: Observer<DoctorUiState>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        SUT = DoctorViewModel(
            doctorRepository,
            commentaryRepository
        )
        SUT.doctor.observeForever(doctorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getDoctors_success_correctDataPassed() = testDispatcher.runBlockingTest {
        val result = ResultApi(
            2,
            Parameters(
                "",
                "",
                0,
                "",
                listOf("")
            ),
            listOf(
                Records(
                    "",
                    "1",
                    Fields(
                        "", "",
                        "", 92370, "", "", "",
                        "", "Mr", "", "061789956230", "92",
                        "", 0, "", "Chaville",
                        "", 0, listOf(22.0, 23.0), ""
                    ),
                    Geometry(
                        "",
                        listOf(22.0, 23.0)
                    ),
                    ""
                )
            ),
            listOf(
                Facet_groups(
                    listOf(
                        Facets(
                            0,
                            "",
                            "",
                            ""
                        )
                    ), ""
                )
            )
        )
        whenever(doctorRepository.getDoctors(any())).thenReturn(result)

        val map = mapOf("1" to "1", "2" to "2")
        SUT.getDoctors(map)
        val doctors = createDoctorsFromCpamApi(result)

        argumentCaptor<Map<String, String>>().apply {
            verify(doctorRepository).getDoctors(capture())
            assertEquals("1", firstValue["1"])
            assertEquals("2", firstValue["2"])
        }
        argumentCaptor<String>().apply {
            verify(commentaryRepository).getDoctorCommentary(capture())
            assertEquals(result.records[0].recordid, firstValue)
        }
        SUT.doctor.observeForever(doctorObserver)

        argumentCaptor<DoctorUiState>().apply {
            verify(doctorObserver).onChanged(capture())
            assertEquals(DoctorUiState.Loading, firstValue)
        }
    }
}