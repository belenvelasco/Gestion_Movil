package com.example.gestion_movil.ui.oportunidad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OportunidadFormScreen(
    id : Int,
    viewModel : OportunidadViewModel,
    onVolver: () -> Unit
){
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(id) {viewModel.cargar(id) }

    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente){
            viewModel.consumirGuardado()
            onVolver()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (id == 0) "nueva oportunidad" else "editar oportunidad" ,
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::onNombreChange,
            label = {Text("nombre de la oportunidad")},
            isError = estado.nombreError != null,
            supportingText = {Text(estado.nombreError ?: "")},
            modifier = Modifier.fillMaxWidth()
        )

        CampoDesplegable(
            label = "Tipo de oportunidad",
            valor = estado.tipo,
            opciones = OportunidadOpciones.tipos,
            error = estado.tipoError,
            onSeleccion = viewModel::onTipoChange
        )

        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcionChange,
            label = { Text("Descripción") },
            minLines = 3,
            isError = estado.descripcionError != null,
            supportingText = { Text(estado.descripcionError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.fuente,
            onValueChange = viewModel::onFuenteChange,
            label = { Text("Fuente de información") },
            isError = estado.fuenteError != null,
            supportingText = { Text(estado.fuenteError ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        CampoDesplegable(
            label = "Prioridad",
            valor = estado.prioridad,
            opciones = OportunidadOpciones.prioridades,
            error = estado.prioridadError,
            onSeleccion = viewModel::onPrioridadChange
        )

        CampoDesplegable(
            label = "Potencial comercial",
            valor = estado.potencial,
            opciones = OportunidadOpciones.potenciales,
            error = estado.potencialError,
            onSeleccion = viewModel::onPotencialChange
        )

        CampoDesplegable(
            label = "Estado de gestión",
            valor = estado.estado,
            opciones = OportunidadOpciones.estados,
            error = null,
            onSeleccion = viewModel::onEstadoChange
        )

        OutlinedTextField(
            value = estado.observaciones,
            onValueChange = viewModel::onObservacionesChange,
            label = { Text("Observaciones (opcional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.guardar() },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.guardando) "Guardando..." else "Guardar")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

// Campo de solo lectura que abre un menú con las opciones permitidas
@Composable
private fun CampoDesplegable(
    label: String,
    valor: String,
    opciones: List<String>,
    error: String?,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Text("▼") },
            isError = error != null,
            supportingText = { Text(error ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        // Capa transparente que recibe el clic para abrir el menú
        Box(
            Modifier
                .matchParentSize()
                .clickable { expandido = true }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}