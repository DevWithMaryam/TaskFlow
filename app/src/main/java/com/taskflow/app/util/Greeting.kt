package com.taskflow.app.util

import androidx.annotation.StringRes
import com.taskflow.app.R

/** Din ke ghante (0 se 23) ke hisab se greeting ka text. */
@StringRes
fun greetingResForHour(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    in 17..20 -> R.string.greeting_evening
    else -> R.string.greeting_night
}