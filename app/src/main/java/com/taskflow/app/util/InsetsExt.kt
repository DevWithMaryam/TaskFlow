package com.taskflow.app.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** View ke upar status bar jitni jagah (padding) add karta hai. */
fun View.applyStatusBarPadding() {
    val initialTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(top = initialTop + bars.top)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

/** View ke neeche navigation/gesture bar ya keyboard (jo bara ho) jitni jagah add karta hai. */
fun View.applyNavigationBarPadding() {
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        v.updatePadding(bottom = initialBottom + maxOf(bars.bottom, ime.bottom))
        insets
    }
    ViewCompat.requestApplyInsets(this)
}