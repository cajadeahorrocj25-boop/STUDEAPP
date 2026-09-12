package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen() {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isProcessing = true
            processImage(bitmap) { text ->
                extractedText = text
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Escáner de Documentos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { cameraLauncher.launch(null) }) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (capturedBitmap == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Presiona el ícono de la cámara para escanear el texto de una asignación o carné estudiantil.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Imagen escaneada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isProcessing) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Procesando imagen con ML Kit...")
                } else {
                    OutlinedTextField(
                        value = extractedText,
                        onValueChange = { extractedText = it },
                        label = { Text("Texto Extraído (Editable)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            showSuccessSnackbar = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copiar / Guardar Texto")
                    }
                    
                    if (showSuccessSnackbar) {
                        Text(
                            text = "¡Texto guardado exitosamente!",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

fun processImage(bitmap: Bitmap, onResult: (String) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            if (visionText.text.isEmpty()) {
                onResult("No se detectó texto en la imagen.")
            } else {
                onResult(visionText.text)
            }
        }
        .addOnFailureListener { e ->
            onResult("Error al reconocer el texto: ${e.localizedMessage}")
        }
}
