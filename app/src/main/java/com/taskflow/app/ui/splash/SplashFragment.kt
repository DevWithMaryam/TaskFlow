package com.taskflow.app.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.taskflow.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Screen band hone par yeh coroutine khud cancel ho jata hai
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            findNavController().navigate(R.id.action_splash_to_home)
        }
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1500L
    }
}