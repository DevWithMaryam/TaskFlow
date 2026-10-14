package com.taskflow.app.data.repository

import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.local.model.TaskFilter
import com.taskflow.app.data.local.model.TaskWithCategory
import com.taskflow.app.util.DateUtils
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun addTask(task: Task): Long = taskDao.insert(task)

    suspend fun updateTask(task: Task) = taskDao.update(task)

    suspend fun deleteTask(task: Task) = taskDao.delete(task)

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)

    suspend fun setCompleted(id: Long, completed: Boolean) =
        taskDao.setCompleted(id, completed)

    fun observeToday(): Flow<List<TaskWithCategory>> =
        taskDao.observeDueBetween(DateUtils.startOfToday(), DateUtils.startOfTomorrow())

    fun observePlanned(): Flow<List<TaskWithCategory>> =
        taskDao.observeDueFrom(DateUtils.startOfTomorrow())

    fun observeByCategory(categoryId: Long): Flow<List<TaskWithCategory>> =
        taskDao.observeByCategory(categoryId)

    fun search(query: String): Flow<List<TaskWithCategory>> =
        taskDao.observeSearch(query)

    fun filtered(filter: TaskFilter): Flow<List<TaskWithCategory>> =
        taskDao.observeFiltered(
            status = filter.status.code,
            filterByPriority = if (filter.priorities.isEmpty()) 0 else 1,
            // Khali list SQL ma na jaye, isliye ek bekaar lafz (filter band hoga to use nahi hota)
            priorities = if (filter.priorities.isEmpty()) listOf("") else filter.priorities.map { it.name },
            categoryId = filter.categoryId ?: -1L,
            dueMode = filter.due.code,
            todayStart = DateUtils.startOfToday(),
            tomorrowStart = DateUtils.startOfTomorrow()
        )

    fun observeTodayCount(): Flow<Int> =
        taskDao.observePendingCountDueBetween(
            DateUtils.startOfToday(), DateUtils.startOfTomorrow()
        )

    fun observePlannedCount(): Flow<Int> =
        taskDao.observePendingCountDueFrom(DateUtils.startOfTomorrow())
}