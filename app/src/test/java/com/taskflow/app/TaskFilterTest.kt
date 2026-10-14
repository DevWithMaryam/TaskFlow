package com.taskflow.app

import com.taskflow.app.data.local.entity.Priority
import com.taskflow.app.data.local.model.DueFilter
import com.taskflow.app.data.local.model.StatusFilter
import com.taskflow.app.data.local.model.TaskFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFilterTest {

    @Test
    fun defaultFilter_isNotActive() {
        assertFalse(TaskFilter().isActive)
    }

    @Test
    fun statusFilter_makesFilterActive() {
        assertTrue(TaskFilter(status = StatusFilter.PENDING).isActive)
    }

    @Test
    fun priorityFilter_makesFilterActive() {
        assertTrue(TaskFilter(priorities = setOf(Priority.HIGH)).isActive)
    }

    @Test
    fun categoryFilter_makesFilterActive() {
        assertTrue(TaskFilter(categoryId = 3L).isActive)
    }

    @Test
    fun dueFilter_makesFilterActive() {
        assertTrue(TaskFilter(due = DueFilter.TODAY).isActive)
    }
}