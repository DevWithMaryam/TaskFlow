package com.taskflow.app.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.taskflow.app.R
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.data.local.model.DueFilter
import com.taskflow.app.data.local.model.StatusFilter
import com.taskflow.app.data.local.model.TaskFilter
import com.taskflow.app.databinding.SheetFilterBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetFilterBinding? = null
    private val binding get() = _binding!!

    private val app get() = requireActivity().application as TaskFlowApp

    /** Category ki id -> uske chip ki id. -1 = "All Categories". */
    private val categoryChipIds = mutableMapOf<Long, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSelection(app.filterStore.filter.value)
        loadCategoryChips()

        // Reset sirf sheet ki selection saaf karta hai. Filter Apply par hi badalta hai.
        binding.btnReset.setOnClickListener { showSelection(TaskFilter()) }
        binding.btnApply.setOnClickListener { applyFilter() }
    }

    /** Sheet poori khuli dikhe, adhi nahi. */
    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    // ---------- Chips banana ----------

    private fun loadCategoryChips() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categories = app.categoryRepository.observeCategories().first()
            if (_binding == null) return@launch

            categoryChipIds.clear()
            binding.chipGroupCategory.removeAllViews()
            addCategoryChip(null, getString(R.string.all_categories))
            categories.forEach { addCategoryChip(it.id, it.name) }

            selectCategory(app.filterStore.filter.value.categoryId)
        }
    }

    private fun addCategoryChip(categoryId: Long?, name: String) {
        val chip = layoutInflater.inflate(
            R.layout.item_filter_chip, binding.chipGroupCategory, false
        ) as Chip
        chip.id = View.generateViewId()
        chip.text = name
        chip.tag = categoryId ?: ALL_CATEGORIES
        binding.chipGroupCategory.addView(chip)
        categoryChipIds[categoryId ?: ALL_CATEGORIES] = chip.id
    }

    private fun selectCategory(categoryId: Long?) {
        val chipId = categoryChipIds[categoryId ?: ALL_CATEGORIES]
            ?: categoryChipIds[ALL_CATEGORIES]
        chipId?.let { binding.chipGroupCategory.check(it) }
    }

    // ---------- Filter ko screen par dikhana ----------

    private fun showSelection(filter: TaskFilter) {
        binding.chipGroupStatus.check(
            when (filter.status) {
                StatusFilter.ALL -> R.id.chipStatusAll
                StatusFilter.PENDING -> R.id.chipStatusPending
                StatusFilter.COMPLETED -> R.id.chipStatusCompleted
            }
        )

        binding.chipPrioHigh.isChecked = Priority.HIGH in filter.priorities
        binding.chipPrioMedium.isChecked = Priority.MEDIUM in filter.priorities
        binding.chipPrioLow.isChecked = Priority.LOW in filter.priorities

        binding.chipGroupDue.check(
            when (filter.due) {
                DueFilter.ALL -> R.id.chipDueAll
                DueFilter.TODAY -> R.id.chipDueToday
                DueFilter.UPCOMING -> R.id.chipDueUpcoming
            }
        )

        selectCategory(filter.categoryId)
    }

    // ---------- Screen se filter parhna aur lagana ----------

    private fun readSelection(): TaskFilter {
        val status = when (binding.chipGroupStatus.checkedChipId) {
            R.id.chipStatusPending -> StatusFilter.PENDING
            R.id.chipStatusCompleted -> StatusFilter.COMPLETED
            else -> StatusFilter.ALL
        }

        val priorities = buildSet {
            if (binding.chipPrioHigh.isChecked) add(Priority.HIGH)
            if (binding.chipPrioMedium.isChecked) add(Priority.MEDIUM)
            if (binding.chipPrioLow.isChecked) add(Priority.LOW)
        }

        val due = when (binding.chipGroupDue.checkedChipId) {
            R.id.chipDueToday -> DueFilter.TODAY
            R.id.chipDueUpcoming -> DueFilter.UPCOMING
            else -> DueFilter.ALL
        }

        val checkedCategoryChip = binding.chipGroupCategory
            .findViewById<Chip>(binding.chipGroupCategory.checkedChipId)
        val categoryId = (checkedCategoryChip?.tag as? Long)?.takeIf { it != ALL_CATEGORIES }

        return TaskFilter(status, priorities, categoryId, due)
    }

    private fun applyFilter() {
        app.filterStore.apply(readSelection())
        // Home ko batao ke filter lag gayi, woh natije kholega
        parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle.EMPTY)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "filter_sheet"
        const val RESULT_KEY = "filter_applied"
        private const val ALL_CATEGORIES = -1L
    }
}