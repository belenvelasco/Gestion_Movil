package com.example.gestion_movil.ui.oportunidad

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.ContactoEntity
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.SeguimientoEntity
import com.example.gestion_movil.data.repository.GestionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de la pantalla de detalle: datos de la oportunidad, contactos, gestiones e historial
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleOportunidadViewModel(
    app: Application,
    private val oportunidadRepository: OportunidadRepository,
    private val gestionRepository: GestionRepository
) : AndroidViewModel(app) {

    // Constructor que usa la app real (lo llama viewModel() desde AppNavigation)
    constructor(app: Application) : this(
        app,
        OportunidadViewModel.crearRepositorio(app),
        crearGestionRepositorio(app)
    )

    val tiposGestion = listOf(
        "Contacto realizado", "Propuesta enviada", "Reunión", "Seguimiento", "Respuesta recibida"
    )
    val proximasAcciones = listOf(
        "Sin próxima acción", "Seguimiento", "Reunión", "Esperar respuesta", "Enviar propuesta"
    )

    // Oportunidad que se está mostrando; al cambiarla se recargan todas las listas
    private val idActual = MutableStateFlow(0)

    fun cargar(id: Int) {
        idActual.value = id
    }

    val oportunidad: StateFlow<OportunidadEntity?> = idActual
        .flatMapLatest { oportunidadRepository.observarPorId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val organizacion: StateFlow<String> = oportunidad
        .map { o ->
            o?.organizacionId?.let { oportunidadRepository.obtenerOrganizacion(it)?.nombre } ?: ""
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val contactos: StateFlow<List<ContactoEntity>> = idActual
        .flatMapLatest { gestionRepository.contactos(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seguimientos: StateFlow<List<SeguimientoEntity>> = idActual
        .flatMapLatest { gestionRepository.seguimientos(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historial: StateFlow<List<HistoriaEntity>> = idActual
        .flatMapLatest { oportunidadRepository.historial(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    // Contactos

    fun abrirFormContacto() = _uiState.update {
        it.copy(
            mostrarFormContacto = true,
            contactoNombre = "", contactoCargo = "", contactoCorreo = "", contactoTelefono = "",
            contactoNombreError = null, contactoCargoError = null, contactoCorreoError = null
        )
    }

    fun cerrarFormContacto() = _uiState.update { it.copy(mostrarFormContacto = false) }

    fun onContactoNombre(v: String) =
        _uiState.update { it.copy(contactoNombre = v, contactoNombreError = null) }

    fun onContactoCargo(v: String) =
        _uiState.update { it.copy(contactoCargo = v, contactoCargoError = null) }

    fun onContactoCorreo(v: String) =
        _uiState.update { it.copy(contactoCorreo = v, contactoCorreoError = null) }

    fun onContactoTelefono(v: String) = _uiState.update { it.copy(contactoTelefono = v) }

    fun guardarContacto() {
        val s = _uiState.value
        val oportunidadId = idActual.value
        if (oportunidadId == 0) return

        val correo = s.contactoCorreo.trim()
        val nuevo = s.copy(
            contactoNombreError = if (s.contactoNombre.isBlank()) OBLIGATORIO else null,
            contactoCargoError = if (s.contactoCargo.isBlank()) OBLIGATORIO else null,
            // El correo es opcional, pero si se escribe debe tener formato de correo
            contactoCorreoError = if (correo.isNotEmpty() && !CORREO.matches(correo)) {
                "Correo no válido"
            } else {
                null
            }
        )
        _uiState.value = nuevo
        if (nuevo.contactoNombreError != null ||
            nuevo.contactoCargoError != null ||
            nuevo.contactoCorreoError != null
        ) {
            return
        }

        viewModelScope.launch {
            gestionRepository.agregarContacto(
                ContactoEntity(
                    oportunidadId = oportunidadId,
                    nombre = s.contactoNombre.trim(),
                    cargo = s.contactoCargo.trim(),
                    correo = correo,
                    telefono = s.contactoTelefono.trim()
                )
            )
            _uiState.update { it.copy(mostrarFormContacto = false) }
        }
    }

    fun eliminarContacto(contacto: ContactoEntity) {
        viewModelScope.launch { gestionRepository.eliminarContacto(contacto) }
    }

    // Gestiones y propuestas

    fun abrirFormGestion() = _uiState.update {
        it.copy(
            mostrarFormGestion = true,
            gestionTipo = "", gestionNota = "", gestionProximaAccion = "",
            gestionTipoError = null, gestionNotaError = null
        )
    }

    fun cerrarFormGestion() = _uiState.update { it.copy(mostrarFormGestion = false) }

    fun onGestionTipo(v: String) =
        _uiState.update { it.copy(gestionTipo = v, gestionTipoError = null) }

    fun onGestionNota(v: String) =
        _uiState.update { it.copy(gestionNota = v, gestionNotaError = null) }

    fun onGestionProximaAccion(v: String) = _uiState.update { it.copy(gestionProximaAccion = v) }

    fun guardarGestion() {
        val s = _uiState.value
        val oportunidadId = idActual.value
        if (oportunidadId == 0) return

        val nuevo = s.copy(
            gestionTipoError = if (s.gestionTipo.isBlank()) OBLIGATORIO else null,
            gestionNotaError = if (s.gestionNota.isBlank()) OBLIGATORIO else null
        )
        _uiState.value = nuevo
        if (nuevo.gestionTipoError != null || nuevo.gestionNotaError != null) return

        viewModelScope.launch {
            gestionRepository.agregarSeguimiento(
                SeguimientoEntity(
                    oportunidadId = oportunidadId,
                    tipo = s.gestionTipo,
                    nota = s.gestionNota.trim(),
                    // "Sin próxima acción" se guarda como vacío
                    proximaAccion = if (s.gestionProximaAccion == proximasAcciones.first()) {
                        ""
                    } else {
                        s.gestionProximaAccion
                    }
                )
            )
            _uiState.update { it.copy(mostrarFormGestion = false) }
        }
    }

    fun reasignarResponsable(nuevo: String) {
        val id = idActual.value
        if (id == 0) return
        viewModelScope.launch { oportunidadRepository.reasignarResponsable(id, nuevo) }
    }

    fun ajustarPrioridad(nueva: String) {
        val actual = oportunidad.value ?: return
        if (actual.prioridad == nueva) return
        viewModelScope.launch { oportunidadRepository.guardar(actual.copy(prioridad = nueva)) }
    }

    companion object {
        private const val OBLIGATORIO = "Campo obligatorio"
        private val CORREO = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun crearGestionRepositorio(app: Application): GestionRepository {
            val db = AppDatabase.getInstance(app)
            return GestionRepository(db.contactoDao(), db.seguimientoDao())
        }
    }
}
