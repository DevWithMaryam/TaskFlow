package com.taskflow.app.ui.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskflow.app.R
import com.taskflow.app.databinding.FragmentCategoriesBinding
import com.taskflow.app.util.applyNavigationBarPadding
import com.taskflow.app.util.applyStatusBarPadding

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)

        binding.header.root.applyStatusBarPadding()
        binding.root.applyNavigationBarPadding()

        binding.header.tvTitle.setText(R.string.categories_title)
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.emptyState.apply {
            tvEmptyTitle.setText(R.string.empty_categories_title)
            tvEmptyMessage.setText(R.string.empty_categories_message)
        }

        // fabAddCategory ka kaam (dialog) Phase 4 ma
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}