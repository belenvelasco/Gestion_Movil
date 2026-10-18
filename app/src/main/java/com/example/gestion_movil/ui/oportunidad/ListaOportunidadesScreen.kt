package com.example.gestion_movil.ui.oportunidad

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestion_movil.data.local.OportunidadEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListaOportunidadesScreen(
    viewModel: OportunidadViewModel,
    onNueva : () -> Unit,
    onEditar : (Int) -> Unit
){
    val lista by viewModel.lista.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Oportunidades",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (lista.isEmpty()){
                Text(
                    "Aún no hay oportunidades registradas. " +
                    "Pulsar \"Agregar oportunidad\" para crear la primera",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else{
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(lista, key = {it.id}){ oportunidad ->
                        OportunidadItem(oportunidad, onClick = { onEditar(oportunidad.id ) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNueva,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Agregar oportunidad")
        }
    }
}

@Composable
private fun OportunidadItem(oportunidad : OportunidadEntity, onClick:() ->  Unit) {
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
        .format(Date(oportunidad.fechaDeteccion))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(oportunidad.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Tipo: ${oportunidad.tipo}", style = MaterialTheme.typography.bodyMedium)
            Text("Prioridad: ${oportunidad.prioridad}", style = MaterialTheme.typography.bodyMedium)
            Text("Estado: ${oportunidad.estado}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Detectado: $fecha · Código: ${oportunidad.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}