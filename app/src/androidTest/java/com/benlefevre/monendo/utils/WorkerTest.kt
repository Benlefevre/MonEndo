package com.benlefevre.monendo.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import com.benlefevre.monendo.notification.NotificationWorker
import com.benlefevre.monendo.treatment.ResetPillWorker
import com.benlefevre.monendo.treatment.models.Treatment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4ClassRunner::class)
class WorkerTest {

    private lateinit var context: Context
    private lateinit var executor: Executor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @After
    fun tearDown() {
        WorkManager.getInstance(context).cancelUniqueWork("Dolipranne")
    }

    @Test
    fun doWorkPill_success_correctResultReturned() {
        val notificationWorker = spyk(
            TestWorkerBuilder<NotificationWorker>(
                context = context,
                executor = executor,
                inputData = workDataOf(TREATMENT to PILL_TAG)
            ).build()
        )
        val result = notificationWorker.doWork()
        verify { notificationWorker.sendPillNotification(any()) }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWorkPillRepeat_success_correctResultReturned() {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putBoolean(CURRENT_CHECKED, false).apply()
        val notificationWorker = spyk(
            TestWorkerBuilder<NotificationWorker>(
                context = context,
                executor = executor,
                inputData = workDataOf(TREATMENT to PILL_REPEAT)
            ).build()
        )
        val result = notificationWorker.doWork()
        verify { notificationWorker.sendPillNotification(any()) }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWorkPillRepeat_successButChecked_correctResultReturned() {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putBoolean(CURRENT_CHECKED, true).apply()
        val notificationWorker = spyk(
            TestWorkerBuilder<NotificationWorker>(
                context = context,
                executor = executor,
                inputData = workDataOf(TREATMENT to PILL_REPEAT)
            ).build()
        )
        val result = notificationWorker.doWork()
        verify(exactly = 0) { notificationWorker.sendPillNotification(any()) }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWorkTreatment_success_correctResultReturned() {
        val notificationWorker = spyk(
            TestWorkerBuilder<NotificationWorker>(
                context = context,
                executor = executor,
                inputData = workDataOf(
                    TREATMENT to TREATMENT_TAG, TREATMENT_NAME to "Dolipranne",
                    TREATMENT_DOSAGE to "2", TREATMENT_FORMAT to "pills"
                )
            ).build()
        )
        val result = notificationWorker.doWork()
        verify { notificationWorker.sendTreatmentNotification(any()) }
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_failure_correctResultReturned() {
        val notificationWorker = spyk(
            TestWorkerBuilder<NotificationWorker>(
                context = context,
                executor = executor,
                inputData = workDataOf(TREATMENT to "")
            ).build()
        )
        val result = notificationWorker.doWork()
        verify(exactly = 0) { notificationWorker.sendPillNotification(any()) }
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun doWork_success_correctDataInsertedInSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(CURRENT_CHECKED, true).apply()
        val resetPillWorker = spyk(
            TestWorkerBuilder<ResetPillWorker>(
                context = context,
                executor = executor
            ).build()
        )
        val result = resetPillWorker.doWork()
        val isCurrentChecked = sharedPreferences.getBoolean(CURRENT_CHECKED, true)

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(false, isCurrentChecked)
    }

    @Test
    fun doWork_success_correctTreatmentUpdatedInSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit().putString(
            TREATMENT, gson.toJson(
                listOf(
                    Treatment(
                        name = "Doli",
                        morning = "08:00",
                        noon = "12:15",
                        isTakenMorning = true,
                        isTakenNoon = true,
                        isTakenAfternoon = false,
                        isTakenEvening = false
                    )
                )
            )
        )
            .apply()
        val resetPillWorker = spyk(
            TestWorkerBuilder<ResetPillWorker>(
                context = context,
                executor = executor
            ).build()
        )
        val result = resetPillWorker.doWork()

        val treatments = mutableListOf<Treatment>()

        gson.fromJson<List<Treatment>>(
            sharedPreferences.getString(TREATMENT, null),
            object : TypeToken<List<Treatment>>() {}.type
        )?.let {
            treatments.addAll(it)
        }

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals("Doli", treatments[0].name)
        assertEquals("08:00", treatments[0].morning)
        assertEquals("12:15", treatments[0].noon)
        assertEquals(false, treatments[0].isTakenMorning)
        assertEquals(false, treatments[0].isTakenNoon)
        assertEquals(false, treatments[0].isTakenAfternoon)
        assertEquals(false, treatments[0].isTakenEvening)
    }
}