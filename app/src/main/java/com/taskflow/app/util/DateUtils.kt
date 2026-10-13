package com.taskflow.app.util

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

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

    /** Date picker ki (UTC) selection ko local 12:00am ma badalta hai. */
    fun fromPickerSelection(utcMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = utcMillis
        val local = Calendar.getInstance()
        local.clear()
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        return local.timeInMillis
    }

    /** Local 12:00am ko date picker ke (UTC) format ma badalta hai. */
    fun toPickerSelection(localMillis: Long): Long {
        val local = Calendar.getInstance()
        local.timeInMillis = localMillis
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        return utc.timeInMillis
    }

    /** Phone ki language/region ke mutabiq date, jaise "Oct 15, 2026". */
    fun format(localMillis: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(localMillis))
}