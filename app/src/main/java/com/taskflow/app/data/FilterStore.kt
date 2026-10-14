package com.taskflow.app.data

import com.taskflow.app.data.local.model.TaskFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FilterStore {

    private val _filter = MutableStateFlow(TaskFilter())
    val filter: StateFlow<TaskFilter> = _filter

    fun apply(filter: TaskFilter) {
        _filter.value = filter
    }

    /** Agar chuni hui category delete ho gayi ho to filter ka woh hissa hata do. */
    fun dropCategoryIfMissing(existingIds: Collection<Long>) {
        val selected = _filter.value.categoryId ?: return
        if (selected !in existingIds) {
            _filter.update { it.copy(categoryId = null) }
        }
    }
}