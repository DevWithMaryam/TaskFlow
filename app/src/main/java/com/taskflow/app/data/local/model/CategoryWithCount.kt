package com.taskflow.app.data.local.model

import androidx.room.Embedded
import com.taskflow.app.data.local.entity.Category

/** Category ke saath uske pending tasks ki ginti. */
data class CategoryWithCount(
    @Embedded val category: Category,
    val taskCount: Int
)