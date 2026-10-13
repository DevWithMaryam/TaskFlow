package com.taskflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.taskflow.app.data.local.dao.CategoryDao
import com.taskflow.app.data.local.dao.TaskDao
import com.taskflow.app.data.local.entity.Category
import com.taskflow.app.data.local.entity.Task

@Database(
    entities = [Task::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class TaskFlowDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: TaskFlowDatabase? = null

        // Poori app ma database ka sirf ek hi object hona chahiye
        fun getInstance(context: Context): TaskFlowDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskFlowDatabase::class.java,
                    "taskflow.db"
                ).build().also { INSTANCE = it }
            }
    }
}