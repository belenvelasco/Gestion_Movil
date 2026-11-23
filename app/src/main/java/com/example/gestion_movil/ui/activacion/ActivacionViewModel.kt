package com.example.gestion_movil.ui.activacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.ResultadoEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.data.repository.ActivacionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivacionUiState(
    // Condiciones y responsable
    val espacio: String = "",
    val fecha: Long? = null,
    val costo: String = "",
    val requisitos: String = "",
    val responsable: String = "",
    // Resultado
    val resultado: String = "",
    val observaciones: String = "",
    val impacto: String = "",
    val cerrada: Boolean = false,
    // Errores
    val espacioError: String? = null,
    val fechaError: String? = null,
    val costoError: String? = null,
    val responsableError: String? = null,
    val resultadoError: String? = null,
    val impactoError: String? = null,
    // Aviso corto para el usuario (se muestra una vez)
    val mensaje: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ActivacionViewModel(
    app: Application,
    private val oportunidadRepository: OportunidadRepository,
    private val repository: ActivacionRepository
) : AndroidViewModel(app) {

    constructor(app: Application) : this(
        app,
        OportunidadViewModel.crearRepositorio(app),
        crearRepositorio(app)
    )

    val resultados = listOf("Activación realizada", "Activación postergada", "Activación cancelada")
    val impactos = listOf("Alto", "Medio", "Bajo")

    private val idActual = MutableStateFlow(0)

    val oportunidad: StateFlow<OportunidadEntity?> = idActual
        .flatMapLatest { oportunidadRepository.observarPorId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ActivacionUiState())
    val uiState: StateFlow<ActivacionUiState> = _uiState.asStateFlow()

    // Carga lo ya guardado (si existe) en el formulario
    fun cargar(id: Int) {
        idActual.value = id
        viewModelScope.launch {
            val activacion = repository.obtenerActivacion(id)
            val resultado = repository.obtenerResultado(id)
            _uiState.value = ActivacionUiState(
                espacio = activacion?.espacio ?: "",
                fecha = activacion?.fechaActivacion,
                costo = activacion?.costo?.toString() ?: "",
                requisitos = activacion?.requisitos ?: "",
                responsable = activacion?.responsable ?: "",
                resultado = resultado?.resultado ?: "",
                observaciones = resultado?.observaciones ?: "",
                impacto = resultado?.impacto ?: "",
                cerrada = resultado?.cerrada ?: false
            )
        }
    }

    fun onEspacio(v: String) = _uiState.update { it.copy(espacio = v, espacioError = null) }
    fun onFecha(v: Long) = _uiState.update { it.copy(fecha = v, fechaError = null) }
    fun onCosto(v: String) = _uiState.update { it.copy(costo = v, costoError = null) }
    fun onRequisitos(v: String) = _uiState.update { it.copy(requisitos = v) }
    fun onResponsable(v: String) =
        _uiState.update { it.copy(responsable = v, responsableError = null) }

    fun onResultado(v: String) = _uiState.update { it.copy(resultado = v, resultadoError = null) }
    fun onObservaciones(v: String) = _uiState.update { it.copy(observaciones = v) }
    fun onImpacto(v: String) = _uiState.update { it.copy(impacto = v, impactoError = null) }
    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    // Coordinar participación: pasa la oportunidad a "Participación confirmada"
    // (el cambio de estado queda en el historial automáticamente)
    fun confirmarParticipacion() {
        val actual = oportunidad.value ?: return
        if (actual.estado == ResumenPanel.ESTADO_CONFIRMADA) return
        viewModelScope.launch {
            oportunidadRepository.guardar(actual.copy(estado = ResumenPanel.ESTADO_CONFIRMADA))
            _uiState.update { it.copy(mensaje = "Participación confirmada") }
        }
    }

    // Condiciones (espacio, fecha, costo, requisitos) y responsable
    fun guardarCondiciones() {
        val id = idActual.value
        if (id == 0) return
        val s = _uiState.value
        val costoTexto = s.costo.trim()
        val costo = costoTexto.toIntOrNull()
        val nuevo = s.copy(
            espacioError = if (s.espacio.isBlank()) OBLIGATORIO else null,
            fechaError = if (s.fecha == null) OBLIGATORIO else null,
            // El costo es opcional, pero si se escribe debe ser un número entero
            costoError = if (costoTexto.isNotEmpty() && (costo == null || costo < 0)) {
                "Ingresa un número entero válido"
            } else {
                null
            },
            responsableError = if (s.responsable.isBlank()) OBLIGATORIO else null
        )
        _uiState.value = nuevo
        if (nuevo.espacioError != null || nuevo.fechaError != null ||
            nuevo.costoError != null || nuevo.responsableError != null
        ) {
            return
        }
        viewModelScope.launch {
            repository.guardarActivacion(
                ActivacionEntity(
                    oportunidaId = id,
                    espacio = s.espacio.trim(),
                    fechaActivacion = s.fecha,
                    costo = costo,
                    requisitos = s.requisitos.trim(),
                    responsable = s.responsable.trim()
                )
            )
            _uiState.update { it.copy(mensaje = "Condiciones y responsable guardados") }
        }
    }

    // Resultado de la activación. Con cerrar = true además se cierra la oportunidad.
    fun guardarResultado(cerrar: Boolean) {
        val id = idActual.value
        if (id == 0) return
        val s = _uiState.value
        if (s.cerrada) return
        val confirmada = oportunidad.value?.estado == ResumenPanel.ESTADO_CONFIRMADA
        val nuevo = s.copy(
            resultadoError = if (s.resultado.isBlank()) OBLIGATORIO else null,
            impactoError = if (s.impacto.isBlank()) OBLIGATORIO else null,
            mensaje = if (!confirmada) "Confirma la participación antes de registrar resultados" else null
        )
        _uiState.value = nuevo
        if (!confirmada || nuevo.resultadoError != null || nuevo.impactoError != null) return
        viewModelScope.launch {
            repository.guardarResultado(
                ResultadoEntity(
                    oportunidadId = id,
                    resultado = s.resultado,
                    observaciones = s.observaciones.trim(),
                    impacto = s.impacto,
                    cerrada = cerrar
                )
            )
            _uiState.update {
                it.copy(
                    cerrada = cerrar,
                    mensaje = if (cerrar) "Oportunidad cerrada" else "Resultado guardado"
                )
            }
        }
    }

    companion object {
        private const val OBLIGATORIO = "Campo obligatorio"

        fun crearRepositorio(app: Application): ActivacionRepository {
            val db = AppDatabase.getInstance(app)
            return ActivacionRepository(db.activacionDao(), db.resultadoDao(), db.historiaDao())
        }
    }
}