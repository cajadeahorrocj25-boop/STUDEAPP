package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.SharedTask
import com.example.model.Task
import com.example.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val sharedTasks by viewModel.sharedTasks.collectAsState()
    
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Colaboración") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Compartir mis tareas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(task.title)
                            Button(onClick = { 
                                selectedTask = task
                                showShareDialog = true
                            }) {
                                Text("Compartir")
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text("Tareas compartidas conmigo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(sharedTasks) { shared ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(shared.title, style = MaterialTheme.typography.titleMedium)
                            Text("Propietario: ${shared.ownerEmail}", style = MaterialTheme.typography.bodyMedium)
                            Text("Permiso: ${shared.permission.name}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        
        if (showShareDialog && selectedTask != null) {
            ShareDialog(
                task = selectedTask!!,
                onDismiss = { showShareDialog = false },
                onShare = { email, writePerm ->
                    viewModel.shareTask(selectedTask!!.id, email, writePerm)
                    showShareDialog = false
                }
            )
        }
    }
}

@Composable
fun ShareDialog(task: Task, onDismiss: () -> Unit, onShare: (String, Boolean) -> Unit) {
    var email by remember { mutableStateOf("") }
    var writePermission by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartir '${task.title}'") },
        text = {
            Column {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo del colaborador") })
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = writePermission, onCheckedChange = { writePermission = it })
                    Text("Permitir edición")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onShare(email, writePermission) }) { Text("Compartir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
