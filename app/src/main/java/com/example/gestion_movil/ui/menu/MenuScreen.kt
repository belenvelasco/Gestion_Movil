package com.example.gestion_movil.ui.menu

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.gestion_movil.R

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
        Text("Menú principal",style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onVerOportunidades, modifier = Modifier.fillMaxWidth()) {
            ContenidoBoton(R.drawable.ic_lists, "Ver oportunidades")
        }

        Button(onClick = onNuevaOportunidades, modifier = Modifier.fillMaxWidth()) {
            ContenidoBoton(R.drawable.ic_add, "Nueva oportunidad")
        }

        Button(onClick = onEscanerQr, modifier = Modifier.fillMaxWidth()) {
            ContenidoBoton(R.drawable.ic_scan_qr, "Escanear código QR")
        }

        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            ContenidoBoton(R.drawable.ic_logout, "Cerrar sesión")
        }
    }
}

@Composable
private fun ContenidoBoton(@DrawableRes icono: Int, texto: String) {
    Icon(
        painter = painterResource(icono),
        contentDescription = null,
        modifier = Modifier.size(ButtonDefaults.IconSize)
    )
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text(texto)
}