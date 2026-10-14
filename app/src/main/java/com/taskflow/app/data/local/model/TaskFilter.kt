package com.taskflow.app.data.local.model

import com.taskflow.app.data.local.entity.Priority

enum class StatusFilter(val code: Int) { ALL(0), PENDING(1), COMPLETED(2) }

enum class DueFilter(val code: Int) { ALL(0), TODAY(1), UPCOMING(2) }

/** Filter ki selection. Default (kuch nahi chuna) = koi filter nahi. */
data class TaskFilter(
    val status: StatusFilter = StatusFilter.ALL,
    /** Khali = saari priorities. */
    val priorities: Set<Priority> = emptySet(),
    /** null = saari categories. */
    val categoryId: Long? = null,
    val due: DueFilter = DueFilter.ALL
) {
    val isActive: Boolean
        get() = status != StatusFilter.ALL ||
                priorities.isNotEmpty() ||
                categoryId != null ||
                due != DueFilter.ALL
}