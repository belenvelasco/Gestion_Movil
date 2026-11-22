package com.example.gestion_movil.ui.oportunidad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.data.repository.OportunidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OportunidadViewModel(
    app: Application,
    private val repository: OportunidadRepository
) : AndroidViewModel(app) {

    //
    constructor(app: Application) : this(app, crearRepositorio(app))

    //
    val tipos = listOf(
        "Evento corporativo", "Feria empresarial", "Actividad municipal",
        "Jornada de beneficios", "Feria institucional", "Activacion comercial", "Otros"
    )
    val prioridades = listOf("Alta", "Media", "Baja")
    val potenciales = listOf("Alta", "Media", "Baja")
    val estados = ResumenPanel.ESTADOS
    //
    val lista: StateFlow<List<OportunidadEntity>> = repository.oportunidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    //
    val organizacionesPorId: StateFlow<Map<Int, String>> = repository.oportunidades
        .map { organizaciones -> organizaciones.associate { it.id to it.nombre } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    //
    val opcionesFiltro = listOf("Todas") + prioridades

    private val _busqueda= MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _filtroPrioridad = MutableStateFlow("Todas")
    val filtroPrioridad: StateFlow<String> = _filtroPrioridad.asStateFlow()

    val listaFiltrada: StateFlow<List<OportunidadEntity>> =
        combine(lista, organizacionesPorId, _busqueda, _filtroPrioridad) { todas, organizaciones, texto, prioridad ->
            val q = texto.trim()
            todas.filter { o ->
                val coincidePrioridad = prioridad == "Todas" || o.prioridad == prioridad
                val coincideTexto = q.isEmpty() ||
                        o.nombre.contains(q, ignoreCase = true) ||
                        o.tipo.contains(q, ignoreCase = true) ||
                        o.descripcion.contains(q, ignoreCase = true) ||
                        o.fuente.contains(q, ignoreCase = true) ||
                        o.comuna.contains(q, ignoreCase = true) ||
                        (organizaciones[o.organizacionId] ?: "").contains(q, ignoreCase = true)
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

    // ---- Eventos que llegan desde el formulario ----
    fun onNombre(v: String) = _uiState.update { it.copy(nombre = v, nombreError = null) }
    fun onTipo(v: String) = _uiState.update { it.copy(tipo = v, tipoError = null) }
    fun onOrganizacion(v: String) = _uiState.update { it.copy(organizacion = v, organizacionError = null) }
    fun onFechaEvento(v: Long?) = _uiState.update { it.copy(fechaEvento = v, fechaEventoError = null) }
    fun onComuna(v: String) = _uiState.update { it.copy(comuna = v, comunaError = null) }
    fun onPublicoEsperado(v: String) =
        _uiState.update { it.copy(publicoEsperado = v, publicoEsperadoError = null) }
    fun onDescripcion(v: String) = _uiState.update { it.copy(descripcion = v, descripcionError = null) }
    fun onFuente(v: String) = _uiState.update { it.copy(fuente = v, fuenteError = null) }
    fun onPrioridad(v: String) = _uiState.update { it.copy(prioridad = v, prioridadError = null) }
    fun onPotencial(v: String) = _uiState.update { it.copy(potencial = v, potencialError = null) }
    fun onEstado(v: String) = _uiState.update { it.copy(estado = v, estadoError = null) }
    fun onObservaciones(v: String) = _uiState.update { it.copy(observaciones = v) }

    // ---- Ubicación (recurso nativo) ----
    fun iniciarUbicacion() =
        _uiState.update { it.copy(ubicando = true, ubicacionMensaje = null) }

    fun onUbicacionObtenida(latitud: Double, longitud: Double) = _uiState.update {
        it.copy(latitud = latitud, longitud = longitud, ubicando = false, ubicacionMensaje = null)
    }

    fun onUbicacionError(mensaje: String) =
        _uiState.update { it.copy(ubicando = false, ubicacionMensaje = mensaje) }

    fun quitarUbicacion() =
        _uiState.update { it.copy(latitud = null, longitud = null, ubicacionMensaje = null) }

    // Prepara el formulario: vacío (id = 0, nueva) o cargado (editar)
    fun cargar(id: Int) {
        viewModelScope.launch {
            if (id == 0) {
                _uiState.value = OportunidadUiState()
            } else {
                repository.obtenerPorId(id)?.let { e ->
                    val nombreOrganizacion = e.organizacionId
                        ?.let { repository.obtenerOrganizacion(it)?.nombre }
                        ?: ""
                    _uiState.value = OportunidadUiState(
                        id = e.id,
                        nombre = e.nombre,
                        tipo = e.tipo,
                        organizacion = nombreOrganizacion,
                        fechaEvento = e.fechaEvento,
                        comuna = e.comuna,
                        publicoEsperado = e.publicoEsperado?.toString() ?: "",
                        descripcion = e.descripcion,
                        fuente = e.fuente,
                        prioridad = e.prioridad,
                        potencial = e.potencial,
                        estado = e.estado,
                        observaciones = e.observaciones,
                        latitud = e.latitud,
                        longitud = e.longitud,
                        fechaDeteccion = e.fechaDeteccion
                    )
                }
            }
        }
    }

    // Validación: los errores se guardan en el UiState y se ven en cada campo
    private fun validar(): Boolean {
        val s = _uiState.value
        val obligatorio = "Campo obligatorio"
        val publico = s.publicoEsperado.trim()
        val nuevo = s.copy(
            nombreError = if (s.nombre.isBlank()) obligatorio else null,
            tipoError = if (s.tipo.isBlank()) obligatorio else null,
            organizacionError = if (s.organizacion.isBlank()) obligatorio else null,
            fechaEventoError = if (s.fechaEvento == null) obligatorio else null,
            comunaError = if (s.comuna.isBlank()) obligatorio else null,
            // El público esperado es opcional, pero si se escribe debe ser un número entero mayor a 0
            publicoEsperadoError = if (publico.isEmpty()) {
                null
            } else {
                val numero = publico.toIntOrNull()
                if (numero == null || numero <= 0) "Ingresa un número entero mayor a 0" else null
            },
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
            nuevo.nombreError, nuevo.tipoError, nuevo.organizacionError,
            nuevo.fechaEventoError, nuevo.comunaError, nuevo.publicoEsperadoError,
            nuevo.descripcionError, nuevo.fuenteError, nuevo.prioridadError,
            nuevo.potencialError, nuevo.estadoError
        ).all { it == null }
    }

    fun guardar() {
        if (!validar()) return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            // Si la organización no existe todavía, se crea
            val organizacionId = repository.obtenerOCrearOrganizacion(s.organizacion)
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
                    fechaEvento = s.fechaEvento,
                    comuna = s.comuna.trim(),
                    publicoEsperado = s.publicoEsperado.trim().toIntOrNull(),
                    organizacionId = organizacionId,
                    latitud = s.latitud,
                    longitud = s.longitud,
                    fechaDeteccion = s.fechaDeteccion ?: System.currentTimeMillis()
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

    companion object {
        // Arma el repositorio real con las tablas de Room
        fun crearRepositorio(app: Application): OportunidadRepository {
            val db = AppDatabase.getInstance(app)
            return OportunidadRepository(db.oportunidadDao(), db.organizacionDao(), db.historiaDao())
        }
    }
}
