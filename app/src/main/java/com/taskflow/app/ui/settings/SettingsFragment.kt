package com.taskflow.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentSettingsBinding
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.setText(R.string.settings_title)
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Sirf yehi 4 items hain. Un ke kaam Phase 10 ma.
        binding.rowTheme.apply {
            ivIcon.setImageResource(R.drawable.ic_theme)
            tvTitle.setText(R.string.settings_theme)
        }
        binding.rowLanguage.apply {
            ivIcon.setImageResource(R.drawable.ic_language)
            tvTitle.setText(R.string.settings_language)
        }
        binding.rowFeedback.apply {
            ivIcon.setImageResource(R.drawable.ic_feedback)
            tvTitle.setText(R.string.settings_feedback)
        }
        binding.rowShare.apply {
            ivIcon.setImageResource(R.drawable.ic_share)
            tvTitle.setText(R.string.settings_share)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}