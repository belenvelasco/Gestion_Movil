package com.example.gestion_movil.ui.evidencias

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.util.Fechas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.forEach

@Composable
fun SeccionEvidencias(
    evidencias: List<EvidenciaEntity>,
    onAbrirCamara: () -> Unit,
    onEliminar: (EvidenciaEntity) -> Unit
) {
    var videoAbierto by remember { mutableStateOf<EvidenciaEntity?>(null) }

    Text("Evidencias (fotos y videos)", style = MaterialTheme.typography.titleMedium)
    if (evidencias.isEmpty()) {
        Text("Aún no hay fotos ni videos.", style = MaterialTheme.typography.bodyMedium)
    }
    evidencias.forEach { evidencia ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val esVideo = evidencia.tipo == EvidenciaEntity.VIDEO
                Text(
                    "${if (esVideo) "Video" else "Foto"} · ${Fechas.formatearConHora(evidencia.fecha)}",
                    style = MaterialTheme.typography.titleSmall
                )
                if (!esVideo) {
                    MiniaturaFoto(evidencia.ruta)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (esVideo) {
                        OutlinedButton(onClick = { videoAbierto = evidencia }) {
                            Text("Reproducir")
                        }
                    }
                    TextButton(onClick = { onEliminar(evidencia) }) { Text("Eliminar evidencia") }
                }
            }
        }
    }
    OutlinedButton(onClick = onAbrirCamara, modifier = Modifier.fillMaxWidth()) {
        Text("Tomar foto o video")
    }

    videoAbierto?.let { video ->
        AlertDialog(
            onDismissRequest = { videoAbierto = null },
            title = { Text("Video") },
            text = {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    factory = { contexto ->
                        VideoView(contexto).apply {
                            setVideoPath(video.ruta)
                            setMediaController(MediaController(contexto).also { it.setAnchorView(this) })
                            start()
                        }
                    }
                )
            },
            confirmButton = { TextButton(onClick = { videoAbierto = null }) { Text("Cerrar") } }
        )
    }
}

// Carga la foto reducida (sin bloquear la pantalla) para no gastar memoria de más
@Composable
private fun MiniaturaFoto(ruta: String) {
    val imagen by produceState<Bitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) {
            val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
            BitmapFactory.decodeFile(ruta, opciones)
        }
    }
    val bitmap = imagen
    if (bitmap == null) {
        Text("Foto no disponible", style = MaterialTheme.typography.bodySmall)
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto de la evidencia",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}