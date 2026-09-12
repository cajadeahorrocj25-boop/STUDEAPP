package com.example.model

data class SharedTask(
    val id: String = "",
    val taskId: String = "",
    val title: String = "",
    val ownerEmail: String = "",
    val sharedWithEmail: String = "",
    val permission: SharePermission = SharePermission.READ
)

enum class SharePermission { READ, WRITE }
