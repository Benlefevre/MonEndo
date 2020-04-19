package com.benlefevre.monendo.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class DateUtilsTest {

    @Test
    fun formatDateWithoutYear_success_correctDataReturned() {
        val date = with(Calendar.getInstance()) {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.MONTH, 0)
            time
        }
        assertEquals("01/01", formatDateWithoutYear(date))
    }

    @Test
    fun formatDateWithYear_success_correctDataReturned() {
        val date = with(Calendar.getInstance()) {
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.MONTH, 0)
            set(Calendar.YEAR, 2020)
            time
        }
        assertEquals("01/01/20", formatDateWithYear(date))
    }

    @Test
    fun parseStringInDate_success_correctDataReturned() {
        val date = Date(1583794800000)
        if (TimeZone.getDefault() == TimeZone.getTimeZone("Europe/Paris"))
            assertEquals(date, parseStringInDate("10/03/20"))
    }

    @Test
    fun formatTime_success_correctDataReturned() {
        val date = with(Calendar.getInstance()) {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            time
        }
        assertEquals("12:00", formatTime(date))
    }

    @Test
    fun setRepeatHour_success_correctDataReturned(){
        val date = with(Calendar.getInstance()) {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            time
        }
        assertEquals("13:00", setRepeatHour(date))
    }

    @Test
    fun formatDateToDayName_success_correctDataReturned() {
        val date = with(Calendar.getInstance()){
            set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY)
            time
        }
        assertEquals("Sun", formatDateToDayName(date))
    }
}