package com.benlefevre.monendo.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class UtilsTest{

    @Test
    fun formatDate_success_correctDataReturned(){
        val date = with(Calendar.getInstance()){
            set(Calendar.DAY_OF_MONTH,1)
            set(Calendar.MONTH,0)
            time
        }
        assertEquals("01/01", formatDate(date))
    }
}