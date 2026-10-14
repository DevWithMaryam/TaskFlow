package com.taskflow.app.ui.tasklist

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentTaskListBinding
import com.taskflow.app.util.SwipeToDeleteCallback
import com.taskflow.app.util.VerticalSpaceDecoration
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding
import com.taskflow.app.viewmodel.TaskListViewModel
import kotlinx.coroutines.launch

/** Yeh ek hi screen Today, Planned, Category aur Search, sab ke liye use hoti hai. */
class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels { TaskListViewModel.Factory }

    private val isSearch: Boolean
        get() = arguments?.getString(TaskListArgs.TYPE) == TaskListArgs.TYPE_SEARCH

    private val adapter = TaskAdapter(
        onToggle = { task, done -> viewModel.setCompleted(task.id, done) },
        onClick = { task ->
            hideKeyboard()
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
        binding.header.btnBack.setOnClickListener {
            hideKeyboard()
            findNavController().navigateUp()
        }

        binding.emptyState.apply {
            tvEmptyTitle.setText(R.string.empty_tasks_title)
            tvEmptyMessage.setText(R.string.empty_tasks_message)
        }

        setupList()
        if (isSearch) setupSearch(showKeyboard = savedInstanceState == null)
        observeTasks()
    }

    // ---------- Search ----------

    private fun setupSearch(showKeyboard: Boolean) {
        binding.tilSearch.isVisible = true

        // Phone ghumane ya wapas aane par pehle wala text rahe
        binding.etSearch.setText(viewModel.searchQuery.value)
        binding.etSearch.setSelection(binding.etSearch.text?.length ?: 0)
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        // List scroll karne par keyboard hat jaye
        binding.rvTasks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyboard()
            }
        })

        if (showKeyboard) {
            binding.etSearch.requestFocus()
            binding.etSearch.post {
                _binding?.let {
                    WindowCompat.getInsetsController(requireActivity().window, it.etSearch)
                        .show(WindowInsetsCompat.Type.ime())
                }
            }
        }
    }

    private fun hideKeyboard() {
        val b = _binding ?: return
        if (!isSearch) return
        WindowCompat.getInsetsController(requireActivity().window, b.etSearch)
            .hide(WindowInsetsCompat.Type.ime())
    }

    /** Khali list par kya likha aaye: search ma do surtein hain. */
    private fun updateEmptyText() {
        if (isSearch && viewModel.searchQuery.value.isBlank()) {
            binding.emptyState.tvEmptyTitle.setText(R.string.search_prompt_title)
            binding.emptyState.tvEmptyMessage.setText(R.string.search_prompt_message)
        } else if (isSearch) {
            binding.emptyState.tvEmptyTitle.setText(R.string.search_no_results_title)
            binding.emptyState.tvEmptyMessage.setText(R.string.search_no_results_message)
        } else {
            binding.emptyState.tvEmptyTitle.setText(R.string.empty_tasks_title)
            binding.emptyState.tvEmptyMessage.setText(R.string.empty_tasks_message)
        }
    }

    // ---------- List ----------

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
                    if (!hasItems) updateEmptyText()
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