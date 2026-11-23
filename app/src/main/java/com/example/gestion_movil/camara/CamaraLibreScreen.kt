package com.example.gestion_movil.camara

import androidx.camera.view.video.AudioConfig
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CamaraLibreScreen(onVolver: () -> Unit) {
    val context = LocalContext.current

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado -> tienePermiso = resultado[Manifest.permission.CAMERA] == true }

    if (tienePermiso) {
        CamaraLibreContenido(onVolver)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Permiso de cámara requerido", style = MaterialTheme.typography.titleLarge)
            Text(
                "Para tomar fotos y grabar videos necesitamos acceso a la cámara " +
                        "(y al micrófono para el sonido del video).",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = {
                lanzadorPermisos.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            }) { Text("Conceder permisos") }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun CamaraLibreContenido(onVolver: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val controlador = remember { LifecycleCameraController(context) }
    var modoVideo by remember { mutableStateOf(false) }
    var grabacion by remember { mutableStateOf<Recording?>(null) }
    var mensaje by remember { mutableStateOf("") }
    val ejecutor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(lifecycleOwner) {
        controlador.bindToLifecycle(lifecycleOwner)
        onDispose {
            grabacion?.stop()
            controlador.unbind()
        }
    }

    LaunchedEffect(modoVideo) {
        controlador.setEnabledUseCases(
            if (modoVideo) CameraController.VIDEO_CAPTURE else CameraController.IMAGE_CAPTURE
        )
    }

    fun tomarFoto() {
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "foto_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/GestionMovil")
            }
        }
        val opciones = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            valores
        ).build()
        controlador.takePicture(
            opciones,
            ejecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(salida: ImageCapture.OutputFileResults) {
                    mensaje = "Foto guardada en la galería"
                }

                override fun onError(error: ImageCaptureException) {
                    mensaje = "No se pudo tomar la foto. Inténtalo de nuevo."
                }
            }
        )
    }

    fun alternarGrabacion() {
        val actual = grabacion
        if (actual != null) {
            actual.stop()
            return
        }
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/GestionMovil")
            }
        }
        val opciones = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(valores).build()
        val conAudio = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        grabacion = controlador.startRecording(
            opciones,
            AudioConfig.create(conAudio),
            ejecutor
        ) { evento ->
            if (evento is VideoRecordEvent.Finalize) {
                grabacion = null
                mensaje = if (evento.hasError()) "No se pudo guardar el video."
                else "Video guardado en la galería"
            }
        }
        mensaje = "Grabando..."
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> PreviewView(ctx).apply { this.controller = controlador } }
        )

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mensaje.isNotEmpty()) {
                    Text(mensaje, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (modoVideo) {
                        OutlinedButton(
                            onClick = { modoVideo = false },
                            enabled = grabacion == null
                        ) { Text("Foto") }
                        FilledTonalButton(onClick = {}, enabled = false) { Text("Video") }
                    } else {
                        FilledTonalButton(onClick = {}, enabled = false) { Text("Foto") }
                        OutlinedButton(onClick = { modoVideo = true }) { Text("Video") }
                    }
                    OutlinedButton(
                        onClick = {
                            controlador.cameraSelector =
                                if (controlador.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                        },
                        enabled = grabacion == null
                    ) { Text("Girar cámara") }
                }
                Button(
                    onClick = { if (modoVideo) alternarGrabacion() else tomarFoto() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            !modoVideo -> "Tomar foto"
                            grabacion != null -> "Detener grabación"
                            else -> "Grabar video"
                        }
                    )
                }
                OutlinedButton(
                    onClick = onVolver,
                    enabled = grabacion == null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver al menú") }
            }
        }
    }
}