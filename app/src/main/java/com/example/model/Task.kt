package com.example.model

import com.google.firebase.Timestamp
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val subject: String = "",
    val dueDate: Long = 0L,
    val priority: Priority = Priority.LOW,
    val status: TaskStatus = TaskStatus.PENDING,
    val ownerId: String = "",
    val tags: List<String> = emptyList()
)

enum class Priority { LOW, MEDIUM, HIGH }
enum class TaskStatus { PENDING, IN_PROGRESS, SUBMITTED, ARCHIVED }
