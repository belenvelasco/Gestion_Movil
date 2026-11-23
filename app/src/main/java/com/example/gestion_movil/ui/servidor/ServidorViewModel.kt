package com.example.gestion_movil.ui.servidor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.remote.ApiBackend
import com.example.gestion_movil.data.remote.BackendInstance
import com.example.gestion_movil.data.remote.OportunidadRemotaDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServidorUiState(
    val cargando: Boolean = false,
    val oportunidades: List<OportunidadRemotaDto> = emptyList(),
    val error: String? = null
)

class ServidorViewModel(
    private val api: ApiBackend = BackendInstance.api
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServidorUiState())
    val uiState: StateFlow<ServidorUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val lista = api.getOportunidades()
                _uiState.update { it.copy(cargando = false, oportunidades = lista) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false,
                        error = "No se pudo conectar con el servidor. Revisa que esté encendido.")
                }
            }
        }
    }
}