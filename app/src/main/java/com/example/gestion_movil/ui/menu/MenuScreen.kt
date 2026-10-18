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
    onVerOportunidades: () -> Unit,
    onNuevaOportunidades: () -> Unit,
    onEscanerQr: () -> Unit,
    onCerrarSesion: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text("Menu principal",style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
            Text("Ver oportunidades")
        }

        Button(onClick = onNuevaOportunidades, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva oportunidad")
        }

        Button(onClick = onEscanerQr, modifier = Modifier.fillMaxWidth()) {
            Text("Escanerar codigo Qr")
        }

        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesion")
        }
    }
}