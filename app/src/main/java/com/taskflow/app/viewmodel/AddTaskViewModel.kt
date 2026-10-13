package com.taskflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.repository.CategoryRepository
import com.taskflow.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Form ma kya kami hai. Dono false ho to sab theek hai. */
data class TaskFormErrors(
    val titleMissing: Boolean = false,
    val categoryMissing: Boolean = false
) {
    val hasErrors: Boolean get() = titleMissing || categoryMissing
}

class AddTaskViewModel(
    private val taskRepository: TaskRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    /** Dropdown ke liye maujooda categories. */
    val categories: StateFlow<List<Category>> =
        categoryRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var selectedCategoryId: Long? = null
    /** Local 12:00am ke millis, ya null (date nahi). */
    var dueDate: Long? = null

    private var isSaving = false

    fun saveTask(
        title: String,
        description: String,
        priority: Priority,
        onResult: (TaskFormErrors) -> Unit
    ) {
        if (isSaving) return

        val cleanTitle = title.trim()
        val errors = TaskFormErrors(
            titleMissing = cleanTitle.isEmpty(),
            categoryMissing = selectedCategoryId == null
        )
        if (errors.hasErrors) {
            onResult(errors)
            return
        }

        isSaving = true
        viewModelScope.launch {
            taskRepository.addTask(
                Task(
                    title = cleanTitle,
                    description = description.trim().ifEmpty { null },
                    categoryId = selectedCategoryId,
                    priority = priority,
                    dueDate = dueDate
                )
            )
            isSaving = false
            onResult(errors)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                AddTaskViewModel(app.taskRepository, app.categoryRepository)
            }
        }
    }
}