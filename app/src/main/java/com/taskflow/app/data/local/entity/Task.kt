package com.taskflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Category delete ho to task bachega, categoryId null ho jayega
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val categoryId: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    /** Us din ke local 12:00am ke millis. null = koi due date nahi. */
    val dueDate: Long? = null,
    val isCompleted: Boolean = false
)