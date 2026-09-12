package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.SharedTask
import com.example.model.Task
import com.example.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.getTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedTasks: StateFlow<List<SharedTask>> = repository.getSharedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(task: Task, context: android.content.Context) {
        viewModelScope.launch {
            repository.addTask(task)
            // Configurar recordatorios para la tarea
            val notificationHelper = com.example.notifications.NotificationHelper(context)
            notificationHelper.scheduleTaskReminder(task)
            notificationHelper.showNotification("Nueva Tarea", "Se ha añadido la tarea: ${task.title}")
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun shareTask(taskId: String, email: String, writePermission: Boolean) {
        viewModelScope.launch {
            repository.shareTask(taskId, email, writePermission)
        }
    }
}
