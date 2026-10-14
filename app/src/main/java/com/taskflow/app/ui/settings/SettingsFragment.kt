package com.taskflow.app.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentSettingsBinding
import com.taskflow.app.util.AppConfig
import com.taskflow.app.util.SupportedLanguages
import com.taskflow.app.util.ThemeManager
import com.taskflow.app.util.ThemeMode
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

        // Sirf yehi 4 items hain.
        binding.rowTheme.apply {
            ivIcon.setImageResource(R.drawable.ic_theme)
            tvTitle.setText(R.string.settings_theme)
            root.setOnClickListener { showThemeDialog() }
        }
        binding.rowLanguage.apply {
            ivIcon.setImageResource(R.drawable.ic_language)
            tvTitle.setText(R.string.settings_language)
            root.setOnClickListener { showLanguageDialog() }
        }
        binding.rowFeedback.apply {
            ivIcon.setImageResource(R.drawable.ic_feedback)
            tvTitle.setText(R.string.settings_feedback)
            root.setOnClickListener { sendFeedback() }
        }
        binding.rowShare.apply {
            ivIcon.setImageResource(R.drawable.ic_share)
            tvTitle.setText(R.string.settings_share)
            root.setOnClickListener { shareApp() }
        }
    }

    // ---------- Theme ----------

    private fun showThemeDialog() {
        val modes = ThemeMode.entries
        val labels = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        val checked = modes.indexOf(ThemeManager.getSaved(requireContext()))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.theme_dialog_title)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                dialog.dismiss()
                // Theme lagte hi screen khud dobara banti hai
                ThemeManager.setTheme(requireContext().applicationContext, modes[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---------- Language ----------

    private fun showLanguageDialog() {
        val languages = SupportedLanguages.all
        val names = languages.map { it.displayName }.toTypedArray()

        // Abhi kaun si zubaan lagi hai (kuch na ho to pehli zubaan)
        val currentTag = AppCompatDelegate.getApplicationLocales()[0]?.language
        val checked = languages.indexOfFirst { it.tag == currentTag }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(names, checked) { dialog, which ->
                dialog.dismiss()
                val tag = languages[which].tag
                if (tag != currentTag) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(tag)
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ---------- Feedback ----------

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.FEEDBACK_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- Share ----------

    private fun shareApp() {
        val url = AppConfig.shareUrl(requireContext().packageName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, url))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}