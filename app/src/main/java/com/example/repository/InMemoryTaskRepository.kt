package com.example.repository

import com.example.model.SharePermission
import com.example.model.SharedTask
import com.example.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class InMemoryTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private val sharedTasks = mutableListOf<SharedTask>()
    
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())
    private val sharedTasksFlow = MutableStateFlow<List<SharedTask>>(emptyList())

    override fun getTasks(): Flow<List<Task>> = tasksFlow.asStateFlow()
    override fun getSharedTasks(): Flow<List<SharedTask>> = sharedTasksFlow.asStateFlow()

    override suspend fun addTask(task: Task) {
        tasks.add(task)
        tasksFlow.value = tasks.toList()
    }

    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            tasksFlow.value = tasks.toList()
        }
    }

    override suspend fun shareTask(taskId: String, email: String, writePermission: Boolean) {
        val task = tasks.find { it.id == taskId } ?: return
        val sharedTask = SharedTask(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            title = task.title,
            ownerEmail = "mi_cuenta@estudiante.edu",
            sharedWithEmail = email,
            permission = if (writePermission) SharePermission.WRITE else SharePermission.READ
        )
        sharedTasks.add(sharedTask)
        sharedTasksFlow.value = sharedTasks.toList()
    }
}
