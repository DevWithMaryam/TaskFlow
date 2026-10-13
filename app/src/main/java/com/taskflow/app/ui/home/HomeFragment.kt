package com.taskflow.app.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentHomeBinding
import com.taskflow.app.ui.tasklist.TaskListArgs
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.header.applyStatusBarPadding()
        binding.bottomNav.navBar.applyNavigationBarPadding()

        setupRows()
        setupBottomNav()
        setupExitDialog()
    }

    private fun setupRows() {
        binding.rowToday.apply {
            ivIcon.setImageResource(R.drawable.ic_today)
            ivIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.tf_accent_sun)
            )
            tvTitle.setText(R.string.today)
            tvCount.setText(R.string.tasks_placeholder)
            root.setOnClickListener {
                openTaskList(getString(R.string.today), TaskListArgs.TYPE_TODAY)
            }
        }

        binding.rowPlanned.apply {
            ivIcon.setImageResource(R.drawable.ic_planned)
            tvTitle.setText(R.string.planned)
            tvCount.setText(R.string.tasks_placeholder)
            root.setOnClickListener {
                openTaskList(getString(R.string.planned), TaskListArgs.TYPE_PLANNED)
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.apply {
            navCategories.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_categories)
            }
            navSettings.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_settings)
            }
            fabAdd.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_addTask)
            }
            // navHome: hum pehle se Home par hain, isliye kuch nahi
            // navFilter: Phase 9 ma banega
        }
    }

    private fun openTaskList(title: String, type: String) {
        findNavController().navigate(
            R.id.action_home_to_taskList,
            bundleOf(
                TaskListArgs.TITLE to title,
                TaskListArgs.TYPE to type
            )
        )
    }

    /** Home par back dabane se app seedha band nahi hoti, pehle puchti hai. */
    private fun setupExitDialog() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.exit) { _, _ -> requireActivity().finish() }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}