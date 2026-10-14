package com.taskflow.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode(val key: String, val nightMode: Int) {
    LIGHT("light", AppCompatDelegate.MODE_NIGHT_NO),
    DARK("dark", AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

object ThemeManager {

    private const val PREFS = "taskflow_settings"
    private const val KEY_THEME = "theme_mode"

    fun getSaved(context: Context): ThemeMode {
        val key = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, null)
        return ThemeMode.entries.firstOrNull { it.key == key } ?: ThemeMode.SYSTEM
    }

    /** App shuru hote hi bulao, taake pehli screen bhi sahi theme ma khule. */
    fun applySaved(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getSaved(context).nightMode)
    }

    /** Choice save karo aur foran lagao (screen khud dobara bante hi naya theme dikhega). */
    fun setTheme(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.key)
            .apply()
        AppCompatDelegate.setDefaultNightMode(mode.nightMode)
    }
}