package com.example.gestion_movil.ui.menu

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
    onVerCatalogo: () -> Unit,
    onVerServidor: () -> Unit,
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
                Text("Panel del suprvisor")
            }
            Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("Ver oportunidades")
            }
        } else {
            Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("Ver oportunidades")
            }
            Button(onClick = onNuevaOportunidades, modifier = Modifier.fillMaxWidth()) {
                Text("Nueva oportunidad")
            }
            Button(onClick = onEscanerQr, modifier = Modifier.fillMaxWidth()) {
                Text("Escanar codigo QR")
            }
            Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
                Text("Catologo de eventos")
            }
            Button(onClick = onVerServidor, modifier = Modifier.fillMaxWidth()) {
                Text("Oportunidades del servidor")
            }
        }
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}