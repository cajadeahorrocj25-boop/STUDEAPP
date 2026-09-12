package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.viewmodel.TaskViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Process data
    val tasksBySubject = tasks.groupBy { it.subject }
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.status == TaskStatus.SUBMITTED || it.status == TaskStatus.ARCHIVED }
    val completionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) * 100f else 0f

    // Monochrome / Neutral Colors for subjects
    val subjectColors = listOf(
        Color(0xFF212121), Color(0xFF424242), Color(0xFF616161),
        Color(0xFF757575), Color(0xFF9E9E9E), Color(0xFFBDBDBD)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen Académico", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { generateAndSharePdf(context, tasks) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir Reporte PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Progress Card
            OverviewCard(completedTasks, totalTasks, completionPercentage)

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Agrega tareas para ver tus estadísticas 📊")
                }
            } else {
                // Progress by Subject Chart
                SubjectProgressChartCard(tasksBySubject, subjectColors)

                // Time Spent Mock Chart (Bar Chart)
                TimeSpentChartCard(tasksBySubject, subjectColors)
            }
        }
    }
}

@Composable
fun OverviewCard(completed: Int, total: Int, percentage: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Progreso Global",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tareas completadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Circular Progress
            CircularProgressBar(percentage = percentage)
        }
    }
}

@Composable
fun CircularProgressBar(percentage: Float) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(durationMillis = 1500), label = "progressAnim"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF000000), // Black for progress instead of Purple
                startAngle = -90f,
                sweepAngle = (currentPercentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${currentPercentage.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SubjectProgressChartCard(tasksBySubject: Map<String, List<Task>>, colors: List<Color>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tareas por Materia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Custom Horizontal Bar Chart
            tasksBySubject.entries.forEachIndexed { index, entry ->
                val subject = entry.key.ifEmpty { "Sin materia" }
                val subjectTasks = entry.value
                val total = subjectTasks.size
                val completed = subjectTasks.count { it.status == TaskStatus.SUBMITTED || it.status == TaskStatus.ARCHIVED }
                val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                val color = colors[index % colors.size]

                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("$completed/$total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var animationPlayed by remember { mutableStateOf(false) }
                    val currentProgress by animateFloatAsState(
                        targetValue = if (animationPlayed) progress else 0f,
                        animationSpec = tween(durationMillis = 1000, delayMillis = index * 100),
                        label = "barAnim"
                    )

                    LaunchedEffect(key1 = true) {
                        animationPlayed = true
                    }

                    Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                        drawRoundRect(
                            color = color.copy(alpha = 0.2f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = color,
                            size = Size(size.width * currentProgress, size.height),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSpentChartCard(tasksBySubject: Map<String, List<Task>>, colors: List<Color>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tiempo Promedio Dedicado (Estimado)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Basado en la cantidad y prioridad de tareas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Custom Vertical Bar Chart
            var animationPlayed by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = true) { animationPlayed = true }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Calculate max hours to scale bars
                val maxHours = tasksBySubject.values.maxOfOrNull { tasks ->
                    tasks.sumOf { if (it.priority == com.example.model.Priority.HIGH) 3 else if (it.priority == com.example.model.Priority.MEDIUM) 2 else 1 }.toFloat()
                } ?: 1f

                tasksBySubject.entries.forEachIndexed { index, entry ->
                    val color = colors[index % colors.size]
                    val hours = entry.value.sumOf { 
                        if (it.priority == com.example.model.Priority.HIGH) 3 
                        else if (it.priority == com.example.model.Priority.MEDIUM) 2 
                        else 1 
                    }.toFloat()
                    
                    val heightRatio = if (maxHours > 0) hours / maxHours else 0f
                    val currentHeightRatio by animateFloatAsState(
                        targetValue = if (animationPlayed) heightRatio else 0f,
                        animationSpec = tween(durationMillis = 1000, delayMillis = index * 100),
                        label = "vertBarAnim"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "${hours.toInt()}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .fillMaxHeight(currentHeightRatio.coerceAtLeast(0.01f))
                                .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = entry.key.take(3).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun generateAndSharePdf(context: Context, tasks: List<Task>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Reporte de Tareas Académicas", 50f, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    var yPosition = 100f

    val completed = tasks.filter { it.status == TaskStatus.SUBMITTED || it.status == TaskStatus.ARCHIVED }
    val pending = tasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }

    canvas.drawText("Resumen:", 50f, yPosition, paint)
    yPosition += 25f
    canvas.drawText("- Tareas Completadas: ${completed.size}", 50f, yPosition, paint)
    yPosition += 20f
    canvas.drawText("- Tareas Pendientes: ${pending.size}", 50f, yPosition, paint)
    yPosition += 40f

    paint.isFakeBoldText = true
    canvas.drawText("Tareas Pendientes:", 50f, yPosition, paint)
    yPosition += 25f
    paint.isFakeBoldText = false
    
    pending.forEach { task ->
        canvas.drawText("- ${task.title} (${task.subject}) - Prioridad: ${task.priority.name}", 50f, yPosition, paint)
        yPosition += 20f
        if (yPosition > 800f) {
            // Stop drawing if it overflows for this simple implementation
        }
    }

    pdfDocument.finishPage(page)

    val reportsDir = File(context.cacheDir, "reports")
    if (!reportsDir.exists()) reportsDir.mkdirs()
    val file = File(reportsDir, "Reporte_Academico.pdf")
    
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte Académico")
            putExtra(Intent.EXTRA_TEXT, "Adjunto mi reporte de tareas académicas generado por Billetera Estudiantil.")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
