package com.taskflow.app.util

data class AppLanguage(
    /** Zubaan ka code, jaise "en", "ur". */
    val tag: String,
    /** Zubaan ka naam usi zubaan ma (translate nahi hota). */
    val displayName: String
)

/**
 * Sirf woh zubanen jin ke strings app ma maujood hain.
 *
 * Nayi zubaan add karne ke 3 qadam:
 *  1. Yahan ek line add karein, jaise AppLanguage("ur", "اردو")
 *  2. res/values-ur/strings.xml banayein (strings.xml ka tarjuma)
 *  3. res/xml/locales_config.xml ma <locale android:name="ur" /> add karein
 */
object SupportedLanguages {
    val all = listOf(
        AppLanguage("en", "English")
    )
}