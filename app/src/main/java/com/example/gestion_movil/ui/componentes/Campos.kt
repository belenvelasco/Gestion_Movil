package com.example.gestion_movil.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.gestion_movil.util.Fechas


@Composable
fun CampoDesplegable(
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

// Campo de solo lectura que abre un calendario para elegir la fecha.
// La fecha se entrega como milisegundos de la medianoche UTC del día elegido.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    label: String,
    fechaMillis: Long?,
    error: String?,
    onFecha: (Long) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fechaMillis?.let { Fechas.formatearEvento(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Text("📅") },
            isError = error != null,
            supportingText = { Text(error ?: "") },
            modifier = Modifier.fillMaxWidth()
        )

        // Capa transparente que recibe el clic para abrir el calendario
        Box(
            Modifier
                .matchParentSize()
                .clickable { abierto = true }
        )
    }

    if (abierto) {
        val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = fechaMillis)
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoCalendario.selectedDateMillis?.let { onFecha(it) }
                        abierto = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}