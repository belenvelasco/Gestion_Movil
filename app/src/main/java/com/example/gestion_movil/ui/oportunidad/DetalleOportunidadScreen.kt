package com.example.gestion_movil.ui.oportunidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.data.local.ContactoEntity
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.ui.componentes.CampoDesplegable
import com.example.gestion_movil.ui.evidencias.EvidenciaViewModel
import com.example.gestion_movil.ui.evidencias.SeccionEvidencias
import com.example.gestion_movil.util.Fechas

// Muestra una oportunidad completa con sus contactos, gestiones e historial de cambios
@Composable
fun DetalleOportunidadScreen(
    id: Int,
    viewModel: DetalleOportunidadViewModel,
    evidenciaViewModel: EvidenciaViewModel,
    onEditar: (Int) -> Unit,
    onAbrirCamara:(Int) -> Unit
) {
    val oportunidad by viewModel.oportunidad.collectAsState()
    val organizacion by viewModel.organizacion.collectAsState()
    val contactos by viewModel.contactos.collectAsState()
    val seguimientos by viewModel.seguimientos.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val estado by viewModel.uiState.collectAsState()

val evidencias by evidenciaViewModel.evidencias.collectAsState()
    LaunchedEffect(id) {
        viewModel.cargar(id)
        evidenciaViewModel.cargar(id)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val actual = oportunidad
        if (actual == null) {
            Text("Cargando oportunidad...", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text(actual.nombre, style = MaterialTheme.typography.headlineSmall)

            DatosOportunidad(actual, organizacion)

            Button(onClick = { onEditar(id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Editar oportunidad")
            }

            // ---------------- Contactos ----------------
            Text("Contactos", style = MaterialTheme.typography.titleMedium)
            if (contactos.isEmpty()) {
                Text("Aún no hay contactos registrados.", style = MaterialTheme.typography.bodyMedium)
            }
            contactos.forEach { contacto ->
                TarjetaContacto(contacto, onEliminar = { viewModel.eliminarContacto(contacto) })
            }
            OutlinedButton(
                onClick = { viewModel.abrirFormContacto() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar contacto")
            }

            // ---------------- Gestiones y propuestas ----------------
            Text("Gestiones y propuestas", style = MaterialTheme.typography.titleMedium)
            if (seguimientos.isEmpty()) {
                Text("Aún no hay gestiones registradas.", style = MaterialTheme.typography.bodyMedium)
            }
            seguimientos.forEach { seguimiento ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "${seguimiento.tipo} · ${Fechas.formatearConHora(seguimiento.fecha)}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(seguimiento.nota, style = MaterialTheme.typography.bodyMedium)
                        if (seguimiento.proximaAccion.isNotBlank()) {
                            Text(
                                "Próxima acción: ${seguimiento.proximaAccion}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = { viewModel.abrirFormGestion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar gestión")
            }

            SeccionEvidencias(
                evidencias = evidencias,
                onAbrirCamara = {onAbrirCamara(id)},
                onEliminar = {evidenciaViewModel.eliminar(it)}
            )

            // ---------------- Historial de cambios ----------------
            Text("Historial de cambios", style = MaterialTheme.typography.titleMedium)
            if (historial.isEmpty()) {
                Text("Sin cambios registrados.", style = MaterialTheme.typography.bodyMedium)
            }
            historial.forEach { registro ->
                Text(
                    "${Fechas.formatearConHora(registro.fecha)} — ${registro.descripcion}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // ---------------- Formulario emergente: contacto ----------------
    if (estado.mostrarFormContacto) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarFormContacto() },
            title = { Text("Nuevo contacto") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = estado.contactoNombre,
                        onValueChange = viewModel::onContactoNombre,
                        label = { Text("Nombre del contacto") },
                        singleLine = true,
                        isError = estado.contactoNombreError != null,
                        supportingText = { Text(estado.contactoNombreError ?: "") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.contactoCargo,
                        onValueChange = viewModel::onContactoCargo,
                        label = { Text("Cargo o área") },
                        singleLine = true,
                        isError = estado.contactoCargoError != null,
                        supportingText = { Text(estado.contactoCargoError ?: "") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.contactoCorreo,
                        onValueChange = viewModel::onContactoCorreo,
                        label = { Text("Correo (opcional)") },
                        singleLine = true,
                        isError = estado.contactoCorreoError != null,
                        supportingText = { Text(estado.contactoCorreoError ?: "") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.contactoTelefono,
                        onValueChange = viewModel::onContactoTelefono,
                        label = { Text("Teléfono (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.guardarContacto() }) { Text("Guardar contacto") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarFormContacto() }) { Text("Cancelar") }
            }
        )
    }

    // ---------------- Formulario emergente: gestión ----------------
    if (estado.mostrarFormGestion) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarFormGestion() },
            title = { Text("Nueva gestión") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    CampoDesplegable(
                        label = "Tipo de gestión",
                        valor = estado.gestionTipo,
                        opciones = viewModel.tiposGestion,
                        error = estado.gestionTipoError,
                        onSeleccion = viewModel::onGestionTipo
                    )
                    OutlinedTextField(
                        value = estado.gestionNota,
                        onValueChange = viewModel::onGestionNota,
                        label = { Text("Nota de la gestión") },
                        minLines = 2,
                        isError = estado.gestionNotaError != null,
                        supportingText = { Text(estado.gestionNotaError ?: "") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    CampoDesplegable(
                        label = "Próxima acción",
                        valor = estado.gestionProximaAccion,
                        opciones = viewModel.proximasAcciones,
                        error = null,
                        onSeleccion = viewModel::onGestionProximaAccion
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.guardarGestion() }) { Text("Guardar gestión") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarFormGestion() }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DatosOportunidad(oportunidad: OportunidadEntity, organizacion: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Estado: ${oportunidad.estado}", style = MaterialTheme.typography.titleSmall)
            Text("Prioridad: ${oportunidad.prioridad}", style = MaterialTheme.typography.bodyMedium)
            Text("Potencial comercial: ${oportunidad.potencial}", style = MaterialTheme.typography.bodyMedium)
            Text("Tipo: ${oportunidad.tipo}", style = MaterialTheme.typography.bodyMedium)
            if (organizacion.isNotBlank()) {
                Text("Organización: $organizacion", style = MaterialTheme.typography.bodyMedium)
            }
            if (oportunidad.fechaEvento != null) {
                Text(
                    "Fecha del evento: ${Fechas.formatearEvento(oportunidad.fechaEvento)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (oportunidad.comuna.isNotBlank()) {
                Text("Comuna: ${oportunidad.comuna}", style = MaterialTheme.typography.bodyMedium)
            }
            if (oportunidad.publicoEsperado != null) {
                Text(
                    "Público esperado: ${oportunidad.publicoEsperado} personas",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text("Fuente: ${oportunidad.fuente}", style = MaterialTheme.typography.bodyMedium)
            Text("Descripción: ${oportunidad.descripcion}", style = MaterialTheme.typography.bodyMedium)
            if (oportunidad.observaciones.isNotBlank()) {
                Text(
                    "Observaciones: ${oportunidad.observaciones}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                "Detectada: ${Fechas.formatear(oportunidad.fechaDeteccion)}  ·  Código: ${oportunidad.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaContacto(contacto: ContactoEntity, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)) {
            Text(contacto.nombre, style = MaterialTheme.typography.titleSmall)
            Text(contacto.cargo, style = MaterialTheme.typography.bodyMedium)
            if (contacto.correo.isNotBlank()) {
                Text(contacto.correo, style = MaterialTheme.typography.bodyMedium)
            }
            if (contacto.telefono.isNotBlank()) {
                Text(contacto.telefono, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onEliminar) { Text("Eliminar contacto") }
        }
    }
}
