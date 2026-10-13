package com.taskflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskflow.app.TaskFlowApp
import com.taskflow.app.data.local.entity.Task
import com.taskflow.app.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    fun addTask(task: Task) {
        viewModelScope.launch { repository.addTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun setCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(taskId, completed) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TaskFlowApp
                TasksViewModel(app.taskRepository)
            }
        }
    }
}