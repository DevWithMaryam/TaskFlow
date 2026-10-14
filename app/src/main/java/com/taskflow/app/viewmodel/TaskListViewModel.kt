package com.taskflow.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.FilterStore
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.local.model.TaskWithCategory
import com.taskflow.app.data.repository.TaskRepository
import com.taskflow.app.ui.tasklist.TaskListArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    private val repository: TaskRepository,
    filterStore: FilterStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listType: String =
        savedStateHandle.get<String>(TaskListArgs.TYPE) ?: TaskListArgs.TYPE_CATEGORY
    private val categoryId: Long =
        savedStateHandle.get<Long>(TaskListArgs.CATEGORY_ID) ?: -1L

    private val _searchQuery = MutableStateFlow("")

    /** Search box ka text (sirf search mode ma use hota hai). */
    val searchQuery: StateFlow<String> = _searchQuery

    /** null = abhi load ho raha hai (tab khali screen na dikhao). */
    val tasks: StateFlow<List<TaskWithCategory>?> = when (listType) {
        TaskListArgs.TYPE_TODAY -> repository.observeToday()
        TaskListArgs.TYPE_PLANNED -> repository.observePlanned()
        TaskListArgs.TYPE_FILTER -> filterStore.filter
            .flatMapLatest { filter -> repository.filtered(filter) }

        TaskListArgs.TYPE_SEARCH -> _searchQuery
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                // Khali box: kuch na dikhao (hint dikhega)
                if (query.isEmpty()) flowOf(emptyList<TaskWithCategory>())
                else repository.search(query)
            }

        else -> repository.observeByCategory(categoryId)
    }.stateIn<List<TaskWithCategory>?>(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    fun setSearchQuery(text: String) {
        _searchQuery.value = text
    }

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
                TaskListViewModel(app.taskRepository, app.filterStore, createSavedStateHandle())
            }
        }
    }
}