package com.taskflow.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate se PEHLE hona zaroori hai
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Status bar ke icons hamesha safed: har screen ka header teal hai
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContentView(R.layout.activity_main)
    }
}