package com.example.gestion_movil.ui.panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.util.Fechas
import com.example.gestion_movil.data.model.ResumenPanel

@Composable
fun PanelSupervisorScreen(
    viewModel: PanelSupervisorViewModel,
    onVerDetalle: (Int) -> Unit
) {

    val resumen by viewModel.resumen.collectAsState()
    PanelSupervisorContenido(resumen = resumen, onVerDetalle = onVerDetalle)
}

// Parte visual sin ViewModel: así se puede probar con Compose UI Test
@Composable
fun PanelSupervisorContenido(
    resumen: ResumenPanel,
    onVerDetalle: (Int) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Panel del supervisor", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Resumen", style = MaterialTheme.typography.titleMedium)
                Text("Total de oportunidades: ${resumen.total}")
                Text("Abiertas: ${resumen.abiertas}")
                Text("Alta prioridad abiertas: ${resumen.altaPrioridadAbiertas}")
                Text("Participación confirmada: ${resumen.confirmadas}")
                Text("Descartadas: ${resumen.descartadas}")
            }
        }

        Text("Por estado", style = MaterialTheme.typography.titleMedium)
        resumen.porEstado.forEach { (estado, cantidad) ->
            FilaConteo(estado, cantidad)
        }

        Text("Por prioridad", style = MaterialTheme.typography.titleMedium)
        resumen.porPrioridad.forEach { (prioridad, cantidad) ->
            FilaConteo(prioridad, cantidad)
        }

        Text("Por responsable (abiertas)", style = MaterialTheme.typography.titleMedium)
        resumen.porResponsable.forEach { (responsable, cantidad) ->
            FilaConteo(responsable, cantidad)
        }
        if (resumen.altaSinResponsable > 0) {
            Text(
                "Alta prioridad sin responsable: ${resumen.altaSinResponsable}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text("Próximos eventos", style = MaterialTheme.typography.titleMedium)
        if (resumen.proximosEventos.isEmpty()) {
            Text("No hay eventos próximos.", style = MaterialTheme.typography.bodyMedium)
        }
        resumen.proximosEventos.forEach { oportunidad ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerDetalle(oportunidad.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(oportunidad.nombre, style = MaterialTheme.typography.titleSmall)
                    oportunidad.fechaEvento?.let {
                        Text("Fecha: ${Fechas.formatearEvento(it)}")
                    }
                    Text("Estado: ${oportunidad.estado}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Acciones pendientes", style = MaterialTheme.typography.titleMedium)
        if (resumen.accionesPendientes.isEmpty()) {
            Text("No hay acciones pendientes.", style = MaterialTheme.typography.bodyMedium)
        }
        resumen.accionesPendientes.forEach { pendiente ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVerDetalle(pendiente.oportunidad.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(pendiente.oportunidad.nombre, style = MaterialTheme.typography.titleSmall)
                    Text("Próxima acción: ${pendiente.proximaAccion}")
                }
            }
        }
    }
}

@Composable
private fun FilaConteo(etiqueta: String, cantidad: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(cantidad.toString(), style = MaterialTheme.typography.titleSmall)
    }
}