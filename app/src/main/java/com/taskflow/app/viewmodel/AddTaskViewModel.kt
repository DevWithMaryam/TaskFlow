package com.taskflow.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.repository.CategoryRepository
import com.taskflow.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
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
    categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: -1L

    /** true = purana task edit ho raha hai, false = naya task. */
    val isEditing: Boolean = taskId != -1L

    /** null = abhi load ho raha hai. */
    val categories: StateFlow<List<Category>?> =
        categoryRepository.observeCategories()
            .stateIn<List<Category>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var selectedCategoryId: Long? = null
    /** Local 12:00am ke millis, ya null (date nahi). */
    var dueDate: Long? = null

    /** Edit ma form sirf ek baar bhara jaye (rotate par user ki likhai na mitay). */
    var prefillDone = false

    private var originalTask: Task? = null
    private val _loadedTask = MutableStateFlow<Task?>(null)
    val loadedTask: StateFlow<Task?> = _loadedTask

    private var isSaving = false

    init {
        if (isEditing) {
            viewModelScope.launch {
                taskRepository.getTask(taskId)?.let { task ->
                    originalTask = task
                    selectedCategoryId = task.categoryId
                    dueDate = task.dueDate
                    _loadedTask.value = task
                }
            }
        }
    }

    fun saveTask(
        title: String,
        description: String,
        priority: Priority,
        onResult: (TaskFormErrors) -> Unit
    ) {
        if (isSaving) return
        if (isEditing && originalTask == null) return   // task abhi load nahi hua

        // Jis purane task ki category delete ho chuki ho, uske liye category zaroori nahi
        val categoryRequired = !(isEditing && originalTask?.categoryId == null)

        val cleanTitle = title.trim()
        val errors = TaskFormErrors(
            titleMissing = cleanTitle.isEmpty(),
            categoryMissing = categoryRequired && selectedCategoryId == null
        )
        if (errors.hasErrors) {
            onResult(errors)
            return
        }

        isSaving = true
        viewModelScope.launch {
            val cleanDescription = description.trim().ifEmpty { null }
            val original = originalTask
            if (original != null) {
                taskRepository.updateTask(
                    original.copy(
                        title = cleanTitle,
                        description = cleanDescription,
                        categoryId = selectedCategoryId,
                        priority = priority,
                        dueDate = dueDate
                    )
                )
            } else {
                taskRepository.addTask(
                    Task(
                        title = cleanTitle,
                        description = cleanDescription,
                        categoryId = selectedCategoryId,
                        priority = priority,
                        dueDate = dueDate
                    )
                )
            }
            isSaving = false
            onResult(errors)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                AddTaskViewModel(
                    app.taskRepository,
                    app.categoryRepository,
                    createSavedStateHandle()
                )
            }
        }
    }
}