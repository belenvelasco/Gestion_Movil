package com.example.gestion_movil.ui.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.data.model.QrResult
import com.example.gestion_movil.lecturaQR.QrScanner
import com.example.gestion_movil.ui.viewmodel.QrViewModel
// linia 86
@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val qrResult by viewModel.qrResult.observeAsState()
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasCameraPermission) {
            Text(
                "Permiso de cámara requerido",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Para escanear códigos QR, necesitamos acceso a la cámara",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission
            ) {
                Text("Conceder permiso de cámara")
            }
        } else if (qrResult == null && isScanning) {
            Text(
                "Escanea un código QR",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // scanner con CameraX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                QrScanner(
                    onQrCodeScanned = { QrContent ->
                        // Procesar el QR
                        viewModel.onQrDetected(QrContent)
                        isScanning = false
                        Toast.makeText(context, "QR detectado!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay para al escaneo
                Surface(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center),
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.primary
                    )
                ) {}
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Enfoca el código QR en el marco central",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La cámara debería activarse automáticamente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (qrResult != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    " QR Detectado:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ){
                    Text(
                        qrResult!!.content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.clearResult()
                        isScanning = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Escanear otro código QR")
                }
            }
        }
    }
}