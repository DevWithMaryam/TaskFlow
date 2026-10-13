package com.taskflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.model.CategoryWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Category>>

    /** Har category ke saath uske pending tasks ki ginti. */
    @Query(
        """SELECT c.*, COUNT(t.id) AS taskCount
           FROM categories c
           LEFT JOIN tasks t ON t.categoryId = c.id AND t.isCompleted = 0
           GROUP BY c.id
           ORDER BY c.name COLLATE NOCASE ASC"""
    )
    fun observeAllWithCount(): Flow<List<CategoryWithCount>>

    /** Delete dialog ke liye: category ma kitne tasks hain (pending + completed). */
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    suspend fun getTaskCount(categoryId: Long): Int

    /** Same naam ki category pehle se hai? (chhote/bare letters ka farq nahi) */
    @Query(
        """SELECT COUNT(*) FROM categories
           WHERE name = :name COLLATE NOCASE AND id != :excludeId"""
    )
    suspend fun countByName(name: String, excludeId: Long): Int
}