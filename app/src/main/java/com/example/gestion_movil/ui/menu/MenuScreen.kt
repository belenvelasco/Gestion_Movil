package com.example.gestion_movil.ui.menu

import androidx.camera.core.Camera
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun MenuScreen(
    esSupervisor: Boolean,
    onVerPanel: () -> Unit,
    onVerOportunidades: () -> Unit,
    onNuevaOportunidades: () -> Unit,
    onEscanerQr: () -> Unit,
    onCamera: () -> Unit,
    onVerCatalogo: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text("Menú principal", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (esSupervisor) "Perfil: Supervisor" else "perfil: Ejecutivo",
            style = MaterialTheme.typography.bodyMedium
        )
        //
        if (esSupervisor) {
            Button(onClick = onVerPanel, modifier = Modifier.fillMaxWidth()) {
                Text("panel del suprvisor")
            }
            Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("ver oportunidades")
            }
        } else {
            Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("ver oportunidades")
            }
            Button(onClick = onNuevaOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("nueva oportunidad")
            }
            Button(onClick = onEscanerQr, modifier = Modifier.fillMaxWidth()) {
                Text("Escanar codigo QR")
            }
            Button(onClick =  onCamera, modifier = Modifier.fillMaxWidth()) {
                Text("camara (foto y video)")
            }
            Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
                Text("Catologo de eventos")
            }
        }
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("cerrar sesion")
        }
    }
}