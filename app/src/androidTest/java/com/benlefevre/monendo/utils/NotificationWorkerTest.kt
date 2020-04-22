package com.benlefevre.monendo.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class NotificationWorkerTest {

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
        context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit().putBoolean(CURRENT_CHECKED,false).apply()
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
        context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit().putBoolean(CURRENT_CHECKED,true).apply()
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
}