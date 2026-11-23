package com.example.gestion_movil.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.model.EventoExterno
import com.example.gestion_movil.data.repository.EventoRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

//
data class EventosUiState(
    val cargando: Boolean= false,
    val eventos: List<EventoExterno> = emptyList (),
    val error: String? = null,
    //
    val importados: Set<Int> = emptySet()
)

class EventosExternosViewModel (
    app: Application,

    private val eventoRepository: EventoRepository,
    private val oportunidadRepository: OportunidadRepository
) : AndroidViewModel(app){

    //

    constructor(app: Application) : this(
        app,
        EventoRepository(),
        OportunidadViewModel.crearRepositorio(app)
    )

    private  val _uiState = MutableStateFlow(EventosUiState())
    val uiState: StateFlow<EventosUiState> = _uiState.asStateFlow()
    init {
        cargar()
    }

    //

    fun cargar(){
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
              val eventos = eventoRepository.obtenerEventos()
                //
                val fuentesExistentes = oportunidadRepository.oportunidades.first()
                    .map { it.fuente }
                    .toSet()
                val importados = eventos
                    .filter {fuenteDe(it) in fuentesExistentes}
                    .map {it.id}
                    .toSet()
                _uiState.update {
                    it.copy(cargando = false, eventos = eventos, importados = importados)
                }
            } catch (e: CancellationException){
                throw e
            } catch (e: Exception){
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo cargar el catálogo." +
                        " Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            }
        }
    }

    //



    fun importar(evento: EventoExterno){
        viewModelScope.launch {
            val organizationId = oportunidadRepository.obtenerOCrearOrganizacion(evento.organizacion)
            oportunidadRepository.guardar(
                OportunidadEntity(
                    nombre = evento.nombre,
                    tipo = "Evento corporativo",
                    descripcion = evento.descripcion.ifBlank{ evento.nombre},
                    fuente = fuenteDe(evento),
                    prioridad = "Media",
                    potencial = "Media",
                    estado = "Evento identificado",
                    observaciones = "",
                    fechaEvento = evento.fecha,
                    comuna = evento.comuna,
                    organizacionId = organizationId
                )
            )
            _uiState.update { it.copy(importados = it.importados + evento.id) }
        }
    }

    companion object{
        //
        fun fuenteDe(evento: EventoExterno) =
            "API REST (catalogo de evento externos, ID ${evento.id})"
    }
}
