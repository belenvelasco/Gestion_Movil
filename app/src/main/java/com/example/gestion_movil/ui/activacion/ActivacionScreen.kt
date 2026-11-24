package com.example.gestion_movil.ui.activacion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.ui.componentes.CampoDesplegable
import com.example.gestion_movil.ui.componentes.CampoFecha
import com.example.gestion_movil.ui.evidencias.EvidenciaViewModel
import com.example.gestion_movil.ui.evidencias.SeccionEvidencias

@Composable
fun ActivacionScreen(
    id: Int,
    viewModel: ActivacionViewModel,
    evidenciaViewModel: EvidenciaViewModel,
    onAbrirCamara: (Int) -> Unit
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsState()
    val oportunidad by viewModel.oportunidad.collectAsState()
    val evidencias by evidenciaViewModel.evidencias.collectAsState()

    LaunchedEffect(id) {
        viewModel.cargar(id)
        evidenciaViewModel.cargar(id)
    }

    // Avisos cortos (guardado, errores generales)
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    // Selector de documentos del teléfono
    val selectorDocumento = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Si el proveedor no permite conservar el permiso, igual se registra
            }
            evidenciaViewModel.registrar(
                id, EvidenciaEntity.DOCUMENTO, uri.toString(), nombreDeArchivo(context, uri)
            )
        }
    }

    val bloqueada = estado.cerrada
    val estadoActual = oportunidad?.estado ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Gestión de la activación", style = MaterialTheme.typography.headlineSmall)
        Text(oportunidad?.nombre ?: "Cargando...", style = MaterialTheme.typography.titleMedium)

        // ---------------- Participación ----------------
        Text("Participación", style = MaterialTheme.typography.titleMedium)
        Text("Estado actual: $estadoActual", style = MaterialTheme.typography.bodyMedium)
        if (estadoActual == ResumenPanel.ESTADO_DESCARTADA) {
            Text(
                "La oportunidad está descartada, no se puede coordinar la participación.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (estadoActual != ResumenPanel.ESTADO_CONFIRMADA) {
            Button(
                onClick = { viewModel.confirmarParticipacion() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Confirmar participación") }
        }

        // ---------------- Condiciones y responsable ----------------
        Text("Condiciones y responsable", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = estado.espacio,
            onValueChange = viewModel::onEspacio,
            label = { Text("Espacio (lugar o stand)") },
            enabled = !bloqueada,
            isError = estado.espacioError != null,
            supportingText = { Text(estado.espacioError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )
        CampoFecha(
            label = "Fecha de la activación",
            fechaMillis = estado.fecha,
            error = estado.fechaError,
            onFecha = { if (!bloqueada) viewModel.onFecha(it) }
        )
        OutlinedTextField(
            value = estado.costo,
            onValueChange = viewModel::onCosto,
            label = { Text("Costo (opcional)") },
            enabled = !bloqueada,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = estado.costoError != null,
            supportingText = { Text(estado.costoError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.requisitos,
            onValueChange = viewModel::onRequisitos,
            label = { Text("Requisitos (opcional)") },
            enabled = !bloqueada,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        CampoDesplegable(
            label = "Responsable",
            valor = estado.responsable,
            opciones = viewModel.responsables,
            error = estado.responsableError,
            onSeleccion = { if (!bloqueada) viewModel.onResponsable(it) }
        )
        if (!bloqueada) {
            Button(
                onClick = { viewModel.guardarCondiciones() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar condiciones") }
        }

        // ---------------- Evidencias y documentos ----------------
        OutlinedButton(
            onClick = { selectorDocumento.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Adjuntar documento") }
        SeccionEvidencias(
            evidencias = evidencias,
            onAbrirCamara = { onAbrirCamara(id) },
            onEliminar = { evidenciaViewModel.eliminar(it) }
        )

        // ---------------- Resultados ----------------
        Text("Resultados", style = MaterialTheme.typography.titleMedium)
        CampoDesplegable(
            label = "Resultado de la activación",
            valor = estado.resultado,
            opciones = viewModel.resultados,
            error = estado.resultadoError,
            onSeleccion = { if (!bloqueada) viewModel.onResultado(it) }
        )
        OutlinedTextField(
            value = estado.observaciones,
            onValueChange = viewModel::onObservaciones,
            label = { Text("Observaciones (opcional)") },
            enabled = !bloqueada,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        CampoDesplegable(
            label = "Impacto o evaluación",
            valor = estado.impacto,
            opciones = viewModel.impactos,
            error = estado.impactoError,
            onSeleccion = { if (!bloqueada) viewModel.onImpacto(it) }
        )
        if (bloqueada) {
            Text(
                "Esta oportunidad está cerrada.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedButton(
                onClick = { viewModel.guardarResultado(cerrar = false) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar resultado") }
            Button(
                onClick = { viewModel.guardarResultado(cerrar = true) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar y cerrar oportunidad") }
        }
    }
}

// Nombre visible del archivo elegido (si el proveedor no lo entrega, uno genérico)
private fun nombreDeArchivo(context: Context, uri: Uri): String =
    context.contentResolver
        .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        ?: "documento"