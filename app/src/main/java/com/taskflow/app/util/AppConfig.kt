package com.taskflow.app.util

object AppConfig {

    /** TODO: apna asli email yahan likhein, feedback isi par aayega. */
    const val FEEDBACK_EMAIL = "your-email@example.com"

    /** Play Store ka link. App publish hone par yeh khud kaam karne lagega. */
    fun shareUrl(packageName: String) =
        "https://play.google.com/store/apps/details?id=$packageName"
}