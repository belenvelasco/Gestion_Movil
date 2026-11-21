package com.example.gestion_movil.ui.eventos


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.gestion_movil.data.model.EventoExterno
import com.example.gestion_movil.util.Fechas


@Composable
fun EventosExternosScreen(viewModel: EventosExternosViewModel) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Catalogo de evento externos", Stayle = MaterialTheme.typography.headlineSmall)
        Text(
            "Datos de prueba"
            stayle = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        when {
            estado.cargado -> {
                CircularProgressIndicator()
                Text("Cargando eventos...", style = MaterialTheme.typography.bodyMedium)
            }

            estado.error != null ->{
                Text(
                    estado.error ?:"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.cargar() }) { Text("Reintentar") }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.eventos, key = { it.id }) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            importado = evento.id in estado.importados,
                            onImportar = { viewModel.importar(evento) }
                        )
                    }
                }
            }
        }
    }


}

@Composable
private fun TarjetaEvento(
    evento: EventoExterno,
    importado: Boolean,
    onImportar:()-> Unit
){
    Card(modifier = Modifier.fillMaxWidth()){
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(evento.nombre, style = MaterialTheme.typography.titleMedium)
            Text("Organizacion: ${evento.organizacion}", style = MaterialTheme.typography.bodyMedium)
            Text("comuna: ${evento.comuna}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Fecha(ejemplo): ${Fechas.formatearEvento(evento.fecha)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "ID del evento: ${evento.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onImportar,
                enabled = !importado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (importado) "Importada" else "Importar como oportunidad")
            }
        }
    }
}