package com.taskflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.model.CategoryWithCount
import com.taskflow.app.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SaveResult { SUCCESS, EMPTY, DUPLICATE }

class CategoriesViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    /** Categories aur un ke pending task counts. Data badalte hi khud update. */
    val categories: StateFlow<List<CategoryWithCount>> =
        repository.observeCategoriesWithCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Naya category banata hai (existing == null) ya purani ka naam badalta hai.
     * Natija onResult ma milta hai.
     */
    fun saveCategory(existing: Category?, name: String, onResult: (SaveResult) -> Unit) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                onResult(SaveResult.EMPTY)
                return@launch
            }
            if (repository.nameExists(trimmed, existing?.id ?: 0)) {
                onResult(SaveResult.DUPLICATE)
                return@launch
            }
            if (existing == null) {
                repository.addCategory(Category(name = trimmed))
            } else {
                repository.updateCategory(existing.copy(name = trimmed))
            }
            onResult(SaveResult.SUCCESS)
        }
    }

    /** Delete dialog ke liye: is category ma kul kitne tasks hain. */
    fun requestTaskCount(category: Category, onCount: (Int) -> Unit) {
        viewModelScope.launch { onCount(repository.getTaskCount(category.id)) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                CategoriesViewModel(app.categoryRepository)
            }
        }
    }
}