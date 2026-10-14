package com.taskflow.app

import com.taskflow.app.util.greetingResForHour
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun morning_is5To11() {
        assertEquals(R.string.greeting_morning, greetingResForHour(5))
        assertEquals(R.string.greeting_morning, greetingResForHour(11))
    }

    @Test
    fun afternoon_is12To16() {
        assertEquals(R.string.greeting_afternoon, greetingResForHour(12))
        assertEquals(R.string.greeting_afternoon, greetingResForHour(16))
    }

    @Test
    fun evening_is17To20() {
        assertEquals(R.string.greeting_evening, greetingResForHour(17))
        assertEquals(R.string.greeting_evening, greetingResForHour(20))
    }

    @Test
    fun night_isFrom21ToBefore5() {
        assertEquals(R.string.greeting_night, greetingResForHour(21))
        assertEquals(R.string.greeting_night, greetingResForHour(0))
        assertEquals(R.string.greeting_night, greetingResForHour(4))
    }
}