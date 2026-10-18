package com.example.gestion_movil.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w3c.dom.Text

@Composable
fun MenuScreen(
    onVerOportunidades: () -> Unit,
    onnuevaOportunidades: () -> Unit,
    onEscanerQr: () -> Unit,
    onCerrarSesion: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text("Menu principal",stayle = MaterialTheme.typography.headlineMedium)

        Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
            Text("ver portunidades")
        }

        Button(onClick = onnuevaOportunidades, modifier = Modifier.fillMaxWidth()) {
            Text("nueva oportunidad")
        }

        Button(onClick = onEscanerQr, modifier = Modifier.fillMaxWidth()) {
            Text("Escanerar codigo Qr")
        }

        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesion")
        }
    }
}