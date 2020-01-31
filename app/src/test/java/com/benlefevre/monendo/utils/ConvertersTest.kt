package com.benlefevre.monendo.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.*


class ConvertersTest{

    lateinit var SUT : Converters

    @Before
    fun setUp() {
        SUT = Converters()
    }

    @Test
    fun fromTimestamp_success_correctDateReturned(){
        val returnedDate = SUT.fromTimestamp(LONG_DATE)
        val expectedDate = Date(LONG_DATE)
        assertEquals(expectedDate,returnedDate)
    }

    @Test
    fun fromTimestamp_failure_nullReturned(){
        val returnedDate = SUT.fromTimestamp(null)
        assertNull(returnedDate)
    }

    @Test
    fun dateToTimestamp_success_correctLongReturned(){
        val expectedDate = Date(LONG_DATE)
        val returnedLong = SUT.dateToTimestamp(expectedDate)
        assertEquals(LONG_DATE,returnedLong)
    }

    @Test
    fun dateToTimestamp_failure_nullReturned(){
        val returnedLong = SUT.dateToTimestamp(null)
        assertNull(returnedLong)
    }
}