package com.taskflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.local.model.TaskWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    // ---- Lists (Flow: data badlte hi UI khud update hogi) ----

    /** Today: due date [start, end) ke andar. Pehle pending, phir completed. */
    @Query(
        """SELECT t.*, c.name AS categoryName
           FROM tasks t LEFT JOIN categories c ON t.categoryId = c.id
           WHERE t.dueDate >= :start AND t.dueDate < :end
           ORDER BY t.isCompleted ASC, t.id DESC"""
    )
    fun observeDueBetween(start: Long, end: Long): Flow<List<TaskWithCategory>>

    /** Planned: due date :from ya us ke baad. */
    @Query(
        """SELECT t.*, c.name AS categoryName
           FROM tasks t LEFT JOIN categories c ON t.categoryId = c.id
           WHERE t.dueDate >= :from
           ORDER BY t.dueDate ASC, t.isCompleted ASC, t.id DESC"""
    )
    fun observeDueFrom(from: Long): Flow<List<TaskWithCategory>>

    @Query(
        """SELECT t.*, c.name AS categoryName
           FROM tasks t LEFT JOIN categories c ON t.categoryId = c.id
           WHERE t.categoryId = :categoryId
           ORDER BY t.isCompleted ASC, t.id DESC"""
    )
    fun observeByCategory(categoryId: Long): Flow<List<TaskWithCategory>>

    /**
     * Search: title ya description ma woh lafz maujood ho (chhote/bare English letters ka farq nahi).
     * instr() use kiya hai taake % ya _ jaise nishan likhne par bhi ghalat natije na aayen.
     */
    @Query(
        """SELECT t.*, c.name AS categoryName
           FROM tasks t LEFT JOIN categories c ON t.categoryId = c.id
           WHERE instr(LOWER(t.title), LOWER(:query)) > 0
              OR instr(LOWER(IFNULL(t.description, '')), LOWER(:query)) > 0
           ORDER BY t.isCompleted ASC, t.id DESC"""
    )
    fun observeSearch(query: String): Flow<List<TaskWithCategory>>

    // ---- Counts (sirf pending tasks) ----

    @Query(
        """SELECT COUNT(*) FROM tasks
           WHERE isCompleted = 0 AND dueDate >= :start AND dueDate < :end"""
    )
    fun observePendingCountDueBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND dueDate >= :from")
    fun observePendingCountDueFrom(from: Long): Flow<Int>
}