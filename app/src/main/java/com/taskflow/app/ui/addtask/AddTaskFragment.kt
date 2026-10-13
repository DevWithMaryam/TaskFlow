package com.taskflow.app.ui.addtask

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.databinding.FragmentAddTaskBinding
import com.taskflow.app.util.DateUtils
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding
import com.taskflow.app.viewmodel.AddTaskViewModel
import kotlinx.coroutines.launch

class AddTaskFragment : Fragment(R.layout.fragment_add_task) {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTaskViewModel by viewModels { AddTaskViewModel.Factory }

    private var categories: List<Category> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddTaskBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.setText(R.string.add_task_title)
        binding.header.tvAction.isVisible = true
        binding.header.tvAction.setOnClickListener { save() }
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.etTitle.doOnTextChanged { _, _, _, _ -> binding.tilTitle.error = null }

        binding.btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_addTask_to_categories)
        }

        setupCategoryDropdown()
        setupDueDate()
        observeCategories()
    }

    // ---------- Category dropdown ----------

    private fun setupCategoryDropdown() {
        binding.actCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedCategoryId = categories[position].id
            binding.tilCategory.error = null
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { list ->
                    categories = list
                    val hasCategories = list.isNotEmpty()

                    // Category na hon to dropdown ki jagah "Add a category first" dikhao
                    binding.tilCategory.isVisible = hasCategories
                    binding.layoutNoCategory.isVisible = !hasCategories

                    binding.actCategory.setAdapter(
                        NoFilterArrayAdapter(requireContext(), list.map { it.name })
                    )

                    // Pehle chuni hui category ab bhi maujood hai? (delete na hui ho)
                    val selected = list.firstOrNull { it.id == viewModel.selectedCategoryId }
                    viewModel.selectedCategoryId = selected?.id
                    binding.actCategory.setText(selected?.name.orEmpty(), false)
                }
            }
        }
    }

    // ---------- Due date ----------

    private fun setupDueDate() {
        binding.etDueDate.setOnClickListener { showDatePicker() }
        binding.tilDueDate.setEndIconOnClickListener {
            if (viewModel.dueDate != null) {
                viewModel.dueDate = null   // X dabane se date hat jati hai
                updateDueDateUi()
            } else {
                showDatePicker()
            }
        }

        // Phone ghumane ke baad khula hua picker apna listener kho deta hai, wapas lagao
        @Suppress("UNCHECKED_CAST")
        (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>)
            ?.addOnPositiveButtonClickListener { onDatePicked(it) }

        updateDueDateUi()
    }

    private fun showDatePicker() {
        val today = DateUtils.startOfToday()
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(DateUtils.toPickerSelection(today)))
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.pick_date)
            .setSelection(DateUtils.toPickerSelection(viewModel.dueDate ?: today))
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { onDatePicked(it) }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun onDatePicked(utcMillis: Long) {
        viewModel.dueDate = DateUtils.fromPickerSelection(utcMillis)
        updateDueDateUi()
    }

    /** Date ho to X icon, warna calendar icon. */
    private fun updateDueDateUi() {
        val date = viewModel.dueDate
        binding.etDueDate.setText(date?.let { DateUtils.format(it) }.orEmpty())
        if (date != null) {
            binding.tilDueDate.setEndIconDrawable(R.drawable.ic_close)
            binding.tilDueDate.setEndIconContentDescription(R.string.clear_date)
        } else {
            binding.tilDueDate.setEndIconDrawable(R.drawable.ic_planned)
            binding.tilDueDate.setEndIconContentDescription(R.string.pick_date)
        }
    }

    // ---------- Save ----------

    private fun selectedPriority(): Priority = when (binding.chipGroupPriority.checkedChipId) {
        R.id.chipLow -> Priority.LOW
        R.id.chipHigh -> Priority.HIGH
        else -> Priority.MEDIUM
    }

    private fun save() {
        viewModel.saveTask(
            title = binding.etTitle.text?.toString().orEmpty(),
            description = binding.etDescription.text?.toString().orEmpty(),
            priority = selectedPriority()
        ) { errors ->
            val b = _binding ?: return@saveTask

            if (errors.titleMissing) {
                b.tilTitle.error = getString(R.string.error_title_required)
            }
            if (errors.categoryMissing) {
                if (categories.isEmpty()) {
                    Snackbar.make(b.root, R.string.empty_categories_message, Snackbar.LENGTH_LONG).show()
                } else {
                    b.tilCategory.error = getString(R.string.error_category_required)
                }
            }

            if (!errors.hasErrors) {
                ViewCompat.getWindowInsetsController(b.root)?.hide(WindowInsetsCompat.Type.ime())
                Toast.makeText(requireContext(), R.string.task_added, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DATE_PICKER_TAG = "due_date_picker"
    }
}