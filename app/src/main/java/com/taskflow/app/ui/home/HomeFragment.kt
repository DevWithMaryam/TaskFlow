package com.taskflow.app.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.R
import com.taskflow.app.data.local.model.CategoryWithCount
import com.taskflow.app.databinding.FragmentHomeBinding
import com.taskflow.app.ui.categories.CategoriesAdapter
import com.taskflow.app.ui.tasklist.TaskListArgs
import com.taskflow.app.util.VerticalSpaceDecoration
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding
import com.taskflow.app.util.greetingResForHour
import com.taskflow.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    // Home par categories ke saath 3-dot menu nahi: edit/delete sirf Categories screen se
    private val categoriesAdapter = CategoriesAdapter(onClick = { openCategory(it) })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.header.applyStatusBarPadding()
        binding.bottomNav.navBar.applyNavigationBarPadding()

        setupRows()
        setupCategoryList()
        setupBottomNav()
        setupExitDialog()
        observeData()
    }

    /** Jab bhi Home samne aaye: greeting aur din dobara check karo. */
    override fun onResume() {
        super.onResume()
        binding.tvGreeting.setText(
            greetingResForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        )
        viewModel.refreshDay()
    }

    private fun setupRows() {
        binding.rowToday.apply {
            ivIcon.setImageResource(R.drawable.ic_today)
            ivIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.tf_accent_sun)
            )
            tvTitle.setText(R.string.today)
            root.setOnClickListener {
                openTaskList(getString(R.string.today), TaskListArgs.TYPE_TODAY)
            }
        }

        binding.rowPlanned.apply {
            ivIcon.setImageResource(R.drawable.ic_planned)
            tvTitle.setText(R.string.planned)
            root.setOnClickListener {
                openTaskList(getString(R.string.planned), TaskListArgs.TYPE_PLANNED)
            }
        }
    }

    private fun setupCategoryList() {
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoriesAdapter
            addItemDecoration(
                VerticalSpaceDecoration(resources.getDimensionPixelSize(R.dimen.list_item_gap))
            )
        }
    }

    // ---------- Database se data ----------

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.todayCount.collect { count ->
                        if (count == null) return@collect   // abhi load ho raha hai
                        binding.tvSubtitle.text =
                            resources.getQuantityString(R.plurals.today_subtitle, count, count)
                        binding.rowToday.tvCount.text =
                            resources.getQuantityString(R.plurals.tasks_count, count, count)
                    }
                }

                launch {
                    viewModel.plannedCount.collect { count ->
                        if (count == null) return@collect
                        binding.rowPlanned.tvCount.text =
                            resources.getQuantityString(R.plurals.tasks_count, count, count)
                    }
                }

                launch {
                    viewModel.categories.collect { list ->
                        if (list == null) return@collect
                        categoriesAdapter.submitList(list)
                        binding.rvCategories.isVisible = list.isNotEmpty()
                        binding.tvNoCategories.isVisible = list.isEmpty()
                    }
                }
            }
        }
    }

    // ---------- Navigation ----------

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

    private fun openCategory(item: CategoryWithCount) {
        findNavController().navigate(
            R.id.action_home_to_taskList,
            bundleOf(
                TaskListArgs.TITLE to item.category.name,
                TaskListArgs.TYPE to TaskListArgs.TYPE_CATEGORY,
                TaskListArgs.CATEGORY_ID to item.category.id
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
        binding.rvCategories.adapter = null
        _binding = null
    }
}