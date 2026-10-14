package com.taskflow.app

import com.taskflow.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    private val hour = 60 * 60 * 1000L

    @Test
    fun tomorrowStart_isAboutOneDayAfterTodayStart() {
        val diff = DateUtils.startOfTomorrow() - DateUtils.startOfToday()
        // 23 se 25 ghante: daylight saving wale dinon ka khayal
        assertTrue(diff in (23 * hour)..(25 * hour))
    }

    @Test
    fun startOfDay_isNotAfterGivenTime() {
        val now = System.currentTimeMillis()
        val start = DateUtils.startOfDay(now)
        assertTrue(start <= now)
        assertTrue(now - start < 25 * hour)
    }

    @Test
    fun datePickerConversion_roundTrips() {
        val localMidnight = DateUtils.startOfToday()
        val roundTrip = DateUtils.fromPickerSelection(
            DateUtils.toPickerSelection(localMidnight)
        )
        assertEquals(localMidnight, roundTrip)
    }
}