package com.taskflow.app.ui.categories

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.model.CategoryWithCount
import com.taskflow.app.databinding.DialogCategoryBinding
import com.taskflow.app.databinding.FragmentCategoriesBinding
import com.taskflow.app.util.VerticalSpaceDecoration
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding
import com.taskflow.app.viewmodel.CategoriesViewModel
import com.taskflow.app.viewmodel.SaveResult
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoriesViewModel by viewModels { CategoriesViewModel.Factory }

    private val adapter = CategoriesAdapter(
        onClick = { openCategory(it) },
        onMoreClick = { anchor, item -> showItemMenu(anchor, item) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.setText(R.string.categories_title)
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        setupList()
        setupEmptyState()
        binding.fabAddCategory.setOnClickListener { showCategoryDialog(null) }

        observeCategories()
    }

    private fun setupList() {
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CategoriesFragment.adapter
            addItemDecoration(
                VerticalSpaceDecoration(resources.getDimensionPixelSize(R.dimen.list_item_gap))
            )
        }
    }

    private fun setupEmptyState() {
        binding.emptyState.apply {
            tvEmptyTitle.setText(R.string.empty_categories_title)
            tvEmptyMessage.setText(R.string.empty_categories_message)
            btnEmptyAction.setText(R.string.add_category)
            btnEmptyAction.isVisible = true
            btnEmptyAction.setOnClickListener { showCategoryDialog(null) }
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { list ->
                    adapter.submitList(list)
                    val hasItems = list.isNotEmpty()
                    binding.rvCategories.isVisible = hasItems
                    binding.fabAddCategory.isVisible = hasItems
                    binding.emptyState.root.isVisible = !hasItems
                }
            }
        }
    }

    private fun openCategory(item: CategoryWithCount) {
        findNavController().navigate(
            R.id.action_categories_to_taskList,
            bundleOf("title" to item.category.name, "categoryId" to item.category.id)
        )
    }

    private fun showItemMenu(anchor: View, item: CategoryWithCount) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_category_item, menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> showCategoryDialog(item.category)
                    R.id.action_delete -> confirmDelete(item.category)
                }
                true
            }
            show()
        }
    }

    /** existing == null ho to naya category, warna naam badalna. */
    private fun showCategoryDialog(existing: Category?) {
        val dialogBinding = DialogCategoryBinding.inflate(layoutInflater)
        existing?.let {
            dialogBinding.etName.setText(it.name)
            dialogBinding.etName.setSelection(it.name.length)
        }
        dialogBinding.etName.doOnTextChanged { _, _, _, _ -> dialogBinding.tilName.error = null }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) R.string.add_category else R.string.edit_category)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null) // neeche khud handle karenge
            .create()

        dialog.setOnShowListener {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialogBinding.etName.requestFocus()

            // Is tarah galat naam par dialog band nahi hota, error dikhta hai
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.saveCategory(existing, dialogBinding.etName.text.toString()) { result ->
                    when (result) {
                        SaveResult.SUCCESS -> dialog.dismiss()
                        SaveResult.EMPTY ->
                            dialogBinding.tilName.error = getString(R.string.error_name_empty)
                        SaveResult.DUPLICATE ->
                            dialogBinding.tilName.error = getString(R.string.error_name_exists)
                    }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(category: Category) {
        viewModel.requestTaskCount(category) { count ->
            if (!isAdded) return@requestTaskCount
            val message = if (count == 0) {
                getString(R.string.delete_category_message_empty)
            } else {
                resources.getQuantityString(R.plurals.delete_category_message, count, count)
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_category_title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteCategory(category) }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCategories.adapter = null
        _binding = null
    }
}