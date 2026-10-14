package com.taskflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.FilterStore
import com.taskflow.app.data.local.model.CategoryWithCount
import com.taskflow.app.data.repository.CategoryRepository
import com.taskflow.app.data.repository.TaskRepository
import com.taskflow.app.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    taskRepository: TaskRepository,
    categoryRepository: CategoryRepository,
    private val filterStore: FilterStore
) : ViewModel() {

    /** Aaj ki date. Din badalne par yeh badalti hai aur counts dobara nikalte hain. */
    private val currentDay = MutableStateFlow(DateUtils.startOfToday())

    /** null = abhi load ho raha hai. */
    val todayCount: StateFlow<Int?> = currentDay
        .flatMapLatest { taskRepository.observeTodayCount() }
        .stateIn<Int?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val plannedCount: StateFlow<Int?> = currentDay
        .flatMapLatest { taskRepository.observePlannedCount() }
        .stateIn<Int?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val categories: StateFlow<List<CategoryWithCount>?> =
        categoryRepository.observeCategoriesWithCount()
            .stateIn<List<CategoryWithCount>?>(
                viewModelScope, SharingStarted.WhileSubscribed(5_000), null
            )

    /** Filter button ke nishan ke liye: filter lagi hai ya nahi. */
    val isFilterActive: StateFlow<Boolean> = filterStore.filter
        .map { it.isActive }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            filterStore.filter.value.isActive
        )

    init {
        // Agar filter ki category delete ho jaye to filter ka woh hissa hata do
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { list ->
                filterStore.dropCategoryIfMissing(list.map { it.id })
            }
        }
    }

    fun isFilterActiveNow(): Boolean = filterStore.filter.value.isActive

    /** Home dikhte hi bulao: agar raat ke 12 baj chuke hon to counts naye din ke hon. */
    fun refreshDay() {
        currentDay.value = DateUtils.startOfToday()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                HomeViewModel(app.taskRepository, app.categoryRepository, app.filterStore)
            }
        }
    }
}