package com.taskflow.app.data.repository

import com.taskflow.app.data.local.dao.CategoryDao
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.model.CategoryWithCount
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun observeCategories(): Flow<List<Category>> = categoryDao.observeAll()

    fun observeCategoriesWithCount(): Flow<List<CategoryWithCount>> =
        categoryDao.observeAllWithCount()

    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun getTaskCount(categoryId: Long): Int = categoryDao.getTaskCount(categoryId)

    suspend fun nameExists(name: String, excludeId: Long = 0): Boolean =
        categoryDao.countByName(name, excludeId) > 0
}