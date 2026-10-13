package com.taskflow.app.data.local.model

import androidx.room.Embedded
import com.taskflow.app.data.local.entity.Task

/** Task ke saath uski category ka naam (list ma dikhane ke liye). */
data class TaskWithCategory(
    @Embedded val task: Task,
    val categoryName: String?
)