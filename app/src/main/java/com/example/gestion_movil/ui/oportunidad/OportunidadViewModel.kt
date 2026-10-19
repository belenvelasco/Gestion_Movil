package com.example.gestion_movil.ui.oportunidad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.repository.OportunidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OportunidadViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = OportunidadRepository(
        AppDatabase.getInstance(app).oportunidadDao()
    )

    val tipos = listOf(
        "Evento corporativo", "Feria empresarial", "Actividad municipal",
        "Jornada de beneficios", "Feria institucional", "Activación comercial", "Otra"
    )
    val prioridades = listOf("Alta", "Media", "Baja")
    val potenciales = listOf("Alto", "Medio", "Bajo")
    val estados = listOf(
        "Evento identificado", "Contacto pendiente", "Contacto realizado",
        "Propuesta enviada", "En espera de respuesta", "Participación confirmada",
        "Oportunidad descartada"
    )

    // se actualiza sola al registrar o modificar
    val lista: StateFlow<List<OportunidadEntity>> = repository.oportunidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //  Búsqueda y filtro de la lista
    val opcionesFiltro = listOf("Todas") + prioridades

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _filtroPrioridad = MutableStateFlow("Todas")
    val filtroPrioridad: StateFlow<String> = _filtroPrioridad.asStateFlow()

    val listaFiltrada: StateFlow<List<OportunidadEntity>> =
        combine(lista, _busqueda, _filtroPrioridad) { todas, texto, prioridad ->
            val q = texto.trim()
            todas.filter { o ->
                val coincidePrioridad = prioridad == "Todas" || o.prioridad == prioridad
                val coincideTexto = q.isEmpty() ||
                        o.nombre.contains(q, ignoreCase = true) ||
                        o.tipo.contains(q, ignoreCase = true) ||
                        o.descripcion.contains(q, ignoreCase = true) ||
                        o.fuente.contains(q, ignoreCase = true)
                coincidePrioridad && coincideTexto
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBusqueda(v: String) {
        _busqueda.value = v
    }

    fun onFiltroPrioridad(v: String) {
        _filtroPrioridad.value = v
    }

    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    //  Eventos que llegan desde el formulario
    fun onNombre(v: String) = _uiState.update { it.copy(nombre = v, nombreError = null) }
    fun onTipo(v: String) = _uiState.update { it.copy(tipo = v, tipoError = null) }
    fun onDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, descripcionError = null) }
    fun onFuente(v: String) = _uiState.update { it.copy(fuente = v, fuenteError = null) }
    fun onPrioridad(v: String) = _uiState.update { it.copy(prioridad = v, prioridadError = null) }
    fun onPotencial(v: String) = _uiState.update { it.copy(potencial = v, potencialError = null) }
    fun onEstado(v: String) = _uiState.update { it.copy(estado = v, estadoError = null) }
    fun onObservaciones(v: String) = _uiState.update { it.copy(observaciones = v) }

    // Ubicación
    fun iniciarUbicacion() =
        _uiState.update { it.copy(ubicando = true, ubicacionMensaje = null) }

    fun onUbicacionObtenida(latitud: Double, longitud: Double) = _uiState.update {
        it.copy(latitud = latitud, longitud = longitud, ubicando = false, ubicacionMensaje = null)
    }

    fun onUbicacionError(mensaje: String) =
        _uiState.update { it.copy(ubicando = false, ubicacionMensaje = mensaje) }

    fun quitarUbicacion() =
        _uiState.update { it.copy(latitud = null, longitud = null, ubicacionMensaje = null) }

    // Prepara el formulario: vacío (id) o cargado (editar)
    fun cargar(id: Int) {
        viewModelScope.launch {
            if (id == 0) {
                _uiState.value = OportunidadUiState()
            } else {
                repository.obtenerPorId(id)?.let { e ->
                    _uiState.value = OportunidadUiState(
                        id = e.id,
                        nombre = e.nombre,
                        tipo = e.tipo,
                        descripcion = e.descripcion,
                        fuente = e.fuente,
                        prioridad = e.prioridad,
                        potencial = e.potencial,
                        estado = e.estado,
                        observaciones = e.observaciones,
                        fechaDeteccion = e.fechaDeteccion,
                        latitud = e.latitud,
                        longitud = e.longitud
                    )
                }
            }
        }
    }

    // Validación: los errores se guardan en el UiState
    private fun validar(): Boolean {
        val s = _uiState.value
        val obligatorio = "Campo obligatorio"
        val nuevo = s.copy(
            nombreError = if (s.nombre.isBlank()) obligatorio else null,
            tipoError = if (s.tipo.isBlank()) obligatorio else null,
            descripcionError = if (s.descripcion.isBlank()) obligatorio else null,
            fuenteError = if (s.fuente.isBlank()) obligatorio else null,
            prioridadError = if (s.prioridad.isBlank()) obligatorio else null,
            potencialError = when {
                s.potencial.isBlank() -> obligatorio
                s.potencial !in potenciales -> "Opción no válida"
                else -> null
            },
            estadoError = if (s.estado.isBlank()) obligatorio else null
        )
        _uiState.value = nuevo
        return listOf(
            nuevo.nombreError, nuevo.tipoError, nuevo.descripcionError,
            nuevo.fuenteError, nuevo.prioridadError, nuevo.potencialError,
            nuevo.estadoError
        ).all { it == null }
    }

    fun guardar() {
        if (!validar()) return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            repository.guardar(
                OportunidadEntity(
                    id = s.id,
                    nombre = s.nombre.trim(),
                    tipo = s.tipo,
                    descripcion = s.descripcion.trim(),
                    fuente = s.fuente.trim(),
                    prioridad = s.prioridad,
                    potencial = s.potencial,
                    estado = s.estado,
                    observaciones = s.observaciones.trim(),
                    fechaDeteccion = if (s.fechaDeteccion == 0L) System.currentTimeMillis() else s.fechaDeteccion,
                    latitud = s.latitud,
                    longitud = s.longitud
                )
            )
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    fun eliminar(oportunidad: OportunidadEntity) {
        viewModelScope.launch { repository.eliminar(oportunidad) }
    }

    fun consumirGuardado() = _uiState.update { it.copy(guardadoExitosamente = false) }

    // Usado por el lector QR: el código puede ser el id o el nombre exacto
    fun buscarPorCodigo(codigo: String): OportunidadEntity? {
        val texto = codigo.trim()
        return lista.value.firstOrNull {
            it.id.toString() == texto || it.nombre.equals(texto, ignoreCase = true)
        }
    }
}