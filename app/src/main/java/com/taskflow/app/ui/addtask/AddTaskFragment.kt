package com.taskflow.app.ui.addtask

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentAddTaskBinding
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding

class AddTaskFragment : Fragment(R.layout.fragment_add_task) {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddTaskBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.setText(R.string.add_task_title)
        binding.header.tvAction.isVisible = true   // "Save", uska kaam Phase 5 ma
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_addTask_to_categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}