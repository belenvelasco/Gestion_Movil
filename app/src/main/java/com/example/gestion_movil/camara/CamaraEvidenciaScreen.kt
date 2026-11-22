package com.example.gestion_movil.camara

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
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
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.ui.evidencias.EvidenciaViewModel
import com.example.gestion_movil.util.ArchivosEvidencia
import androidx.camera.video.AudioConfig
@SuppressLint("MissingPermission")
@Composable
fun CamaraEvidenciaScreen(
    oportunidadId: Int,
    viewModel: EvidenciaViewModel,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    // Se pide la cámara (obligatoria) y el micrófono (solo para el sonido del video)
    val lanzadorPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        tienePermisoCamara = resultado[Manifest.permission.CAMERA] == true
    }

    if (!tienePermisoCamara) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
            Button(
                onClick = {
                    lanzadorPermisos.launch(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    )
                }
            ) {
                Text("Conceder permisos")
            }
        }
        return
    }

    val controlador = remember { LifecycleCameraController(context) }
    var modoVideo by remember { mutableStateOf(false) }
    var grabacion by remember { mutableStateOf<Recording?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var guardadas by remember { mutableStateOf(0) }
    val ejecutor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(lifecycleOwner) {
        controlador.bindToLifecycle(lifecycleOwner)
        onDispose {
            grabacion?.stop()
            controlador.unbind()
        }
    }

    // Foto y video no se activan a la vez: se cambia de modo con los botones
    LaunchedEffect(modoVideo) {
        controlador.setEnabledUseCases(
            if (modoVideo) CameraController.VIDEO_CAPTURE else CameraController.IMAGE_CAPTURE
        )
    }

    fun tomarFoto() {
        val archivo = ArchivosEvidencia.crear(context, oportunidadId, EvidenciaEntity.FOTO)
        val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()
        controlador.takePicture(
            opciones,
            ejecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(salida: ImageCapture.OutputFileResults) {
                    viewModel.registrar(oportunidadId, EvidenciaEntity.FOTO, archivo.absolutePath)
                    guardadas++
                    mensaje = "Foto guardada"
                }

                override fun onError(error: ImageCaptureException) {
                    archivo.delete()
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
        val archivo = ArchivosEvidencia.crear(context, oportunidadId, EvidenciaEntity.VIDEO)
        val conAudio = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val opciones = FileOutputOptions.Builder(archivo).build()
        grabacion = controlador.startRecording(
            opciones,
            AudioConfig.create(conAudio),
            ejecutor
        ) { evento ->
            if (evento is VideoRecordEvent.Finalize) {
                grabacion = null
                if (evento.hasError()) {
                    archivo.delete()
                    mensaje = "No se pudo guardar el video."
                } else {
                    viewModel.registrar(oportunidadId, EvidenciaEntity.VIDEO, archivo.absolutePath)
                    guardadas++
                    mensaje = "Video guardado"
                }
            }
        }
        mensaje = "Grabando..."
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply { this.controller = controlador }
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (mensaje.isEmpty()) "Evidencias guardadas ahora: $guardadas"
                    else "$mensaje · guardadas ahora: $guardadas",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                ) { Text("Listo, volver a la oportunidad") }
            }
        }
    }
}
