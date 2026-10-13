package com.taskflow.app.ui.tasklist

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentTaskListBinding
import com.taskflow.app.util.SwipeToDeleteCallback
import com.taskflow.app.util.VerticalSpaceDecoration
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding
import com.taskflow.app.viewmodel.TaskListViewModel
import kotlinx.coroutines.launch

/** Yeh ek hi screen Today, Planned aur Category, sab ke liye use hoti hai. */
class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels { TaskListViewModel.Factory }

    private val adapter = TaskAdapter(
        onToggle = { task, done -> viewModel.setCompleted(task.id, done) },
        onClick = { task ->
            findNavController().navigate(
                R.id.action_taskList_to_addTask,
                bundleOf("taskId" to task.id)
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskListBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.text =
            arguments?.getString(TaskListArgs.TITLE) ?: getString(R.string.default_list_title)
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.emptyState.apply {
            tvEmptyTitle.setText(R.string.empty_tasks_title)
            tvEmptyMessage.setText(R.string.empty_tasks_message)
        }

        setupList()
        observeTasks()
    }

    private fun setupList() {
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TaskListFragment.adapter
            addItemDecoration(
                VerticalSpaceDecoration(resources.getDimensionPixelSize(R.dimen.list_item_gap))
            )
        }

        ItemTouchHelper(SwipeToDeleteCallback(requireContext()) { position ->
            val task = adapter.currentList[position].task
            viewModel.deleteTask(task)
            Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { viewModel.restoreTask(task) }
                .show()
        }).attachToRecyclerView(binding.rvTasks)
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { list ->
                    if (list == null) return@collect   // abhi load ho raha hai
                    adapter.submitList(list)
                    val hasItems = list.isNotEmpty()
                    binding.rvTasks.isVisible = hasItems
                    binding.imgBackdrop.isVisible = hasItems
                    binding.emptyState.root.isVisible = !hasItems
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvTasks.adapter = null
        _binding = null
    }
}