package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.model.Priority
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.viewmodel.TaskViewModel
import org.json.JSONArray
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val text = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                
                var importedCount = 0
                if (it.toString().lowercase().endsWith("json") || text.trim().startsWith("[")) {
                    val jsonArray = JSONArray(text)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        viewModel.addTask(Task(
                            id = UUID.randomUUID().toString(),
                            title = obj.optString("title", "Tarea Importada"),
                            subject = obj.optString("subject", "General"),
                            dueDate = System.currentTimeMillis() + 86400000L,
                            priority = try { Priority.valueOf(obj.optString("priority", "MEDIUM").uppercase()) } catch (e: Exception) { Priority.MEDIUM },
                            status = TaskStatus.PENDING
                        ), context)
                        importedCount++
                    }
                } else {
                    // Tratar como CSV: titulo,materia,prioridad
                    val lines = text.split("\n")
                    lines.forEach { line ->
                        val parts = line.split(",")
                        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                            viewModel.addTask(Task(
                                id = UUID.randomUUID().toString(),
                                title = parts[0].trim(),
                                subject = parts.getOrNull(1)?.trim() ?: "General",
                                dueDate = System.currentTimeMillis() + 86400000L,
                                priority = try { Priority.valueOf(parts.getOrNull(2)?.trim()?.uppercase() ?: "MEDIUM") } catch(e: Exception) { Priority.MEDIUM },
                                status = TaskStatus.PENDING
                            ), context)
                            importedCount++
                        }
                    }
                }
                Toast.makeText(context, "Se importaron $importedCount tareas", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al importar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Importar Tareas CSV/JSON")
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "Agregar Tarea")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                EnhancedTaskCard(task)
            }
        }

        if (showDialog) {
            AddTaskDialog(
                onDismiss = { showDialog = false },
                onAdd = { title, subject, tags ->
                    viewModel.addTask(Task(title = title, subject = subject, tags = tags, dueDate = System.currentTimeMillis() + 86400000L), context)
                    showDialog = false
                }
            )
        }
    }
}

fun suggestTags(text: String): List<String> {
    val lowerText = text.lowercase()
    val tags = mutableSetOf<String>()
    
    if (listOf("examen", "parcial", "quiz", "prueba", "test").any { lowerText.contains(it) }) {
        tags.add("Examen")
    }
    if (listOf("tarea", "homework", "deber", "ejercicio", "assignment").any { lowerText.contains(it) }) {
        tags.add("Tarea")
    }
    if (listOf("proyecto", "trabajo", "exposición", "presentación", "project").any { lowerText.contains(it) }) {
        tags.add("Proyecto")
    }
    if (listOf("leer", "lectura", "capítulo", "read").any { lowerText.contains(it) }) {
        tags.add("Lectura")
    }
    if (listOf("ensayo", "reporte", "resumen", "essay").any { lowerText.contains(it) }) {
        tags.add("Ensayo")
    }
    return tags.toList()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, List<String>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    
    val suggestedTags = remember(title) { suggestTags(title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { 
                        title = it
                        // Auto-add suggested tags that aren't already selected
                        val newSuggestions = suggestTags(it)
                        selectedTags = selectedTags + newSuggestions
                    }, 
                    label = { Text("Título (Ej. Leer Capítulo 3)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject, 
                    onValueChange = { subject = it }, 
                    label = { Text("Materia") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (selectedTags.isNotEmpty() || suggestedTags.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Etiquetas sugeridas (Auto NLP):", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val allTags = (selectedTags + suggestedTags).distinct()
                        allTags.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    if (isSelected) {
                                        selectedTags = selectedTags - tag
                                    } else {
                                        selectedTags = selectedTags + tag
                                    }
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(title, subject, selectedTags.toList()) }) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
