package com.example.gestion_movil.ui.oportunidad

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.data.local.OportunidadEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun ListaOportunidadesScreen(
    viewModel: OportunidadViewModel,
    onNueva: () -> Unit,
    onEditar: (Int) -> Unit
) {
    // listas vienen de Room como Flow: se actualizan solas
    val todas by viewModel.lista.collectAsState()
    val filtradas by viewModel.listaFiltrada.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val filtroPrioridad by viewModel.filtroPrioridad.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    // Oportunidad que el usuario quiere eliminar (null = ninguna)
    var porEliminar by remember { mutableStateOf<OportunidadEntity?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Oportunidades",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::onBusqueda,
                label = { Text("Buscar por nombre, tipo, fuente...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                viewModel.opcionesFiltro.forEach { opcion ->
                    FilterChip(
                        selected = filtroPrioridad == opcion,
                        onClick = { viewModel.onFiltroPrioridad(opcion) },
                        label = { Text(opcion) }
                    )
                }
            }

            if (todas.isEmpty()) {
                Text(
                    "Aún no hay oportunidades registradas. " +
                            "Pulsa \"Agregar oportunidad\" para crear la primera.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (filtradas.isEmpty()) {
                Text(
                    "No hay oportunidades que coincidan con la búsqueda o el filtro.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filtradas, key = { it.id }) { oportunidad ->
                        OportunidadItem(
                            oportunidad = oportunidad,
                            modifier = Modifier.animateItem(),
                            onClick = { onEditar(oportunidad.id) },
                            onEliminar = { porEliminar = oportunidad },
                            onVerUbicacion = { abrirMapa(context, oportunidad) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 88.dp)
        )

        ExtendedFloatingActionButton(
            onClick = onNueva,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Agregar oportunidad")
        }
    }

    // Confirmación antes de borrar
    porEliminar?.let { oportunidad ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar oportunidad") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar \"${oportunidad.nombre}\"? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(oportunidad)
                        porEliminar = null
                        scope.launch { snackbarHostState.showSnackbar("Oportunidad eliminada") }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Abre la ubicación guardada en la app de mapas
private fun abrirMapa(context: android.content.Context, oportunidad: OportunidadEntity) {
    val lat = oportunidad.latitud ?: return
    val lng = oportunidad.longitud ?: return
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(oportunidad.nombre)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        Toast.makeText(context, "No hay una aplicación de mapas instalada", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun OportunidadItem(
    oportunidad: OportunidadEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    onVerUbicacion: () -> Unit
) {
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
        .format(Date(oportunidad.fechaDeteccion))
    val tieneUbicacion = oportunidad.latitud != null && oportunidad.longitud != null

    val color by animateColorAsState(
        if (oportunidad.prioridad == "Alta") MaterialTheme.colorScheme.primaryContainer
        else if (oportunidad.prioridad == "Media") MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "colorTarjeta"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Text(oportunidad.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Tipo: ${oportunidad.tipo}", style = MaterialTheme.typography.bodyMedium)
            Text("Prioridad: ${oportunidad.prioridad}", style = MaterialTheme.typography.bodyMedium)
            Text("Estado: ${oportunidad.estado}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Detectada: $fecha  ·  Código: ${oportunidad.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (tieneUbicacion) {
                    TextButton(onClick = onVerUbicacion) { Text("Ver ubicación") }
                }
                TextButton(onClick = onEliminar) { Text("Eliminar") }
            }
        }
    }
}
