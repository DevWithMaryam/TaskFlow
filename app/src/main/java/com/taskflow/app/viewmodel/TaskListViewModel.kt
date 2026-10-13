package com.taskflow.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.local.model.TaskWithCategory
import com.taskflow.app.data.repository.TaskRepository
import com.taskflow.app.ui.tasklist.TaskListArgs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listType: String =
        savedStateHandle.get<String>(TaskListArgs.TYPE) ?: TaskListArgs.TYPE_CATEGORY
    private val categoryId: Long =
        savedStateHandle.get<Long>(TaskListArgs.CATEGORY_ID) ?: -1L

    /** null = abhi load ho raha hai (tab khali screen na dikhao). */
    val tasks: StateFlow<List<TaskWithCategory>?> = when (listType) {
        TaskListArgs.TYPE_TODAY -> repository.observeToday()
        TaskListArgs.TYPE_PLANNED -> repository.observePlanned()
        else -> repository.observeByCategory(categoryId)
    }.stateIn<List<TaskWithCategory>?>(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    fun setCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(taskId, completed) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    /** UNDO: wohi task (wohi id ke saath) wapas daal do. */
    fun restoreTask(task: Task) {
        viewModelScope.launch { repository.addTask(task) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                TaskListViewModel(app.taskRepository, createSavedStateHandle())
            }
        }
    }
}