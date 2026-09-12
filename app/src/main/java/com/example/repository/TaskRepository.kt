package com.example.repository

import com.example.model.SharedTask
import com.example.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    fun getSharedTasks(): Flow<List<SharedTask>>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun shareTask(taskId: String, email: String, writePermission: Boolean)
}
