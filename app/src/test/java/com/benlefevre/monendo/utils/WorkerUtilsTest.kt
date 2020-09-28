package com.benlefevre.monendo.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class WorkerUtilsTest {

    @Test
    fun setDelayDuration_success_correctDataReturned() {
        val hour = formatTime(with(Calendar.getInstance()){
            add(Calendar.MINUTE,1)
            time
        })
        assertTrue(setDelayDuration(hour) <= 60000)

        val passedHour = formatTime(with(Calendar.getInstance()){
            add(Calendar.MINUTE,-1)
            time
        })
        assertTrue(setDelayDuration(passedHour) >= 86328000)
    }
}