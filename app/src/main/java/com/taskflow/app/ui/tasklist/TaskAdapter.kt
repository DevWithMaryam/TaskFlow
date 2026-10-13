package com.taskflow.app.ui.tasklist

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskflow.app.R
import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.local.model.TaskWithCategory
import com.taskflow.app.databinding.ItemTaskBinding
import com.taskflow.app.util.DateUtils

class TaskAdapter(
    private val onToggle: (Task, Boolean) -> Unit,
    private val onClick: (Task) -> Unit
) : ListAdapter<TaskWithCategory, TaskAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskWithCategory) = with(binding) {
            val task = item.task
            val context = root.context

            // Checkbox: pehle listener hatao, phir state lagao, phir listener wapas
            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = task.isCompleted
            cbDone.contentDescription = task.title
            cbDone.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }

            // Complete task par title ke upar line
            tvTitle.text = task.title
            tvTitle.paintFlags = if (task.isCompleted) {
                tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            tvDescription.isVisible = !task.description.isNullOrBlank()
            tvDescription.text = task.description

            // Category aur due date ek line ma
            val parts = listOfNotNull(item.categoryName, task.dueDate?.let { DateUtils.format(it) })
            tvMeta.isVisible = parts.isNotEmpty()
            tvMeta.text = parts.joinToString("  •  ")
            val dueDate = task.dueDate
            val overdue = dueDate != null &&
                    dueDate < DateUtils.startOfToday() && !task.isCompleted
            tvMeta.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (overdue) R.color.tf_delete else R.color.tf_on_surface_variant
                )
            )

            // Priority pill
            val (labelRes, colorRes) = when (task.priority) {
                Priority.LOW -> R.string.priority_low to R.color.tf_priority_low
                Priority.MEDIUM -> R.string.priority_medium to R.color.tf_priority_medium
                Priority.HIGH -> R.string.priority_high to R.color.tf_priority_high
            }
            tvPriority.setText(labelRes)
            tvPriority.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))

            // Complete task halka dikhe
            val alpha = if (task.isCompleted) 0.55f else 1f
            tvTitle.alpha = alpha
            tvDescription.alpha = alpha
            tvMeta.alpha = alpha
            tvPriority.alpha = alpha

            root.setOnClickListener { onClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskWithCategory>() {
        override fun areItemsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
            old.task.id == new.task.id

        override fun areContentsTheSame(old: TaskWithCategory, new: TaskWithCategory) =
            old == new
    }
}