package com.taskflow.app.util

import java.util.Calendar

object DateUtils {

    /** Diye gaye waqt ke din ka local 12:00am (millis). */
    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    fun startOfTomorrow(): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfToday()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }
}