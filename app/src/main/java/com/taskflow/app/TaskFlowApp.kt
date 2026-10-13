package com.taskflow.app

import android.app.Application
import com.taskflow.app.data.local.TaskFlowDatabase
import com.taskflow.app.data.repository.CategoryRepository
import com.taskflow.app.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskFlowApp : Application() {

    val database by lazy { TaskFlowDatabase.getInstance(this) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }

    override fun onCreate() {
        super.onCreate()
        // Database ko background ma pehle hi bana lo, taake pehli screen par der na lage
        CoroutineScope(Dispatchers.IO).launch { database.openHelper.writableDatabase }
    }
}