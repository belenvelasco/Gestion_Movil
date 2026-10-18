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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OportunidadViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = OportunidadRepository(
        AppDatabase.getInstance(app).oportunidadDao()
    )

    val lista: StateFlow<List<OportunidadEntity>> = repository.oportunidades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    // ---- Eventos que llegan desde el formulario ----
    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v, nombreError = null) }
    fun onTipoChange(v: String) = _uiState.update { it.copy(tipo = v, tipoError = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v, descripcionError = null) }
    fun onFuenteChange(v: String) = _uiState.update { it.copy(fuente = v, fuenteError = null) }
    fun onPrioridadChange(v: String) = _uiState.update { it.copy(prioridad = v, prioridadError = null) }
    fun onPotencialChange(v: String) = _uiState.update { it.copy(potencial = v, potencialError = null) }
    fun onEstadoChange(v: String) = _uiState.update { it.copy(estado = v) }
    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observaciones = v) }


    // Prepara el formulario: vacío (id = 0, nueva) o cargado (editar)
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
                        observaciones = e.observaciones
                    )
                }
            }
        }
    }

    // Validación: los errores se guardan en el UiState y se ven en cada campo
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
                s.potencial !in OportunidadOpciones.potenciales -> "Opción no válida"
                else -> null
            },
        )
        _uiState.value = nuevo
        return listOf(
            nuevo.nombreError, nuevo.tipoError, nuevo.descripcionError,
            nuevo.fuenteError, nuevo.prioridadError, nuevo.potencialError
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
                    observaciones = s.observaciones.trim()
                )
            )
            _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
        }
    }

    fun consumirGuardado() = _uiState.update { it.copy(guardadoExitosamente = false) }

    // Usado por el lector QR: el código puede ser el id o el nombre exacto
    fun buscarPorCodigo(codigo: String): OportunidadEntity? {
        val texto = codigo.trim()
        return lista.value.firstOrNull {
            it.id.toString() == texto || it.nombre.equals(texto, ignoreCase = true)
        }
    }
    fun abrirDesdeQr(contenido: String, onEncontrada: (Int) -> Unit, onNoEncontrada: () -> Unit) {
        val id = contenido.trim().removePrefix("OPORTUNIDAD:").toIntOrNull()
        if (id == null) {
            onNoEncontrada()
            return
        }
        viewModelScope.launch {
            if (repository.obtenerPorId(id) != null) onEncontrada(id) else onNoEncontrada()
        }
    }
}