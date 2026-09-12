package com.example.repository

import com.example.model.SharePermission
import com.example.model.SharedTask
import com.example.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreTaskRepository : TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val tasksCollection = firestore.collection("tasks")
    private val sharedCollection = firestore.collection("shared_tasks")

    override fun getTasks(): Flow<List<Task>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        
        val subscription = tasksCollection
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { it.toObject(Task::class.java) } ?: emptyList()
                trySend(tasks)
            }
            
        awaitClose { subscription.remove() }
    }

    override fun getSharedTasks(): Flow<List<SharedTask>> = callbackFlow {
        val user = auth.currentUser ?: return@callbackFlow
        val emailOrUid = user.email ?: user.uid
        
        val subscription = sharedCollection
            .whereEqualTo("sharedWithEmail", emailOrUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val sharedTasks = snapshot?.documents?.mapNotNull { it.toObject(SharedTask::class.java) } ?: emptyList()
                trySend(sharedTasks)
            }
            
        awaitClose { subscription.remove() }
    }

    override suspend fun addTask(task: Task) {
        val uid = auth.currentUser?.uid ?: return
        val taskToSave = task.copy(ownerId = uid)
        tasksCollection.document(taskToSave.id).set(taskToSave).await()
    }

    override suspend fun updateTask(task: Task) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun shareTask(taskId: String, email: String, writePermission: Boolean) {
        val user = auth.currentUser ?: return
        val ownerEmail = user.email ?: user.uid
        
        // Fetch task to get title
        val taskDoc = tasksCollection.document(taskId).get().await()
        val taskTitle = taskDoc.getString("title") ?: "Unknown Task"
        
        val permission = if (writePermission) SharePermission.WRITE else SharePermission.READ
        
        val sharedTask = SharedTask(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            title = taskTitle,
            ownerEmail = ownerEmail,
            sharedWithEmail = email,
            permission = permission
        )
        
        sharedCollection.document(sharedTask.id).set(sharedTask).await()
    }
}
