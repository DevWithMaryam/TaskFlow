package com.taskflow.app.ui.tasklist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentTaskListBinding
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding

/** Yeh ek hi screen Today, Planned, Category, Search sab ke liye use hogi. */
class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskListBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.text =
            arguments?.getString("title") ?: getString(R.string.default_list_title)
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.emptyState.apply {
            tvEmptyTitle.setText(R.string.empty_tasks_title)
            tvEmptyMessage.setText(R.string.empty_tasks_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}