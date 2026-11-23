package com.example.gestion_movil.ui.evidencias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.data.repository.EvidenciaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenciaViewModel(
    app: Application,
    private val repository: EvidenciaRepository
) : AndroidViewModel(app) {

    // Constructor que usa la base real (lo llama viewModel() desde AppNavigation)
    constructor(app: Application) : this(app, crearRepositorio(app))

    private val idActual = MutableStateFlow(0)

    fun cargar(id: Int) {
        idActual.value = id
    }

    val evidencias: StateFlow<List<EvidenciaEntity>> = idActual
        .flatMapLatest { repository.evidencias(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // La cámara ya guardó el archivo; aquí se registra en Room
    fun registrar(oportunidadId: Int, tipo: String, ruta: String, nombre: String = "") {
        viewModelScope.launch {
            repository.agregar(
                EvidenciaEntity(
                    oportunidadId = oportunidadId,
                    tipo = tipo,
                    ruta = ruta,
                    nombre = nombre)
            )
        }
    }

    fun eliminar(evidencia: EvidenciaEntity) {
        viewModelScope.launch { repository.eliminar(evidencia) }
    }

    companion object {
        fun crearRepositorio(app: Application): EvidenciaRepository {
            val db = AppDatabase.getInstance(app)
            return EvidenciaRepository(db.evidenciaDao(), db.historiaDao())
        }
    }
}