package com.taskflow.app.ui.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskflow.app.R
import com.taskflow.app.data.local.model.CategoryWithCount
import com.taskflow.app.databinding.ItemHomeRowBinding

class CategoriesAdapter(
    private val onClick: (CategoryWithCount) -> Unit,
    private val onMoreClick: (View, CategoryWithCount) -> Unit
) : ListAdapter<CategoryWithCount, CategoriesAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemHomeRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryWithCount) = with(binding) {
            ivIcon.setImageResource(R.drawable.ic_folder)
            tvTitle.text = item.category.name
            tvCount.text = root.resources.getQuantityString(
                R.plurals.tasks_count, item.taskCount, item.taskCount
            )
            btnMore.isVisible = true
            root.setOnClickListener { onClick(item) }
            btnMore.setOnClickListener { onMoreClick(it, item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<CategoryWithCount>() {
        override fun areItemsTheSame(old: CategoryWithCount, new: CategoryWithCount) =
            old.category.id == new.category.id

        override fun areContentsTheSame(old: CategoryWithCount, new: CategoryWithCount) =
            old == new
    }
}