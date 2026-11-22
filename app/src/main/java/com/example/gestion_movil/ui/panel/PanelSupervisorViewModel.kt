package com.example.gestion_movil.ui.panel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.model.ResumenPanel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PanelSupervisorViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)

    val resumen: StateFlow<ResumenPanel> = combine(
        db.oportunidadDao().observarTodas(),
        db.seguimientoDao().observarTodos()
    ) { oportunidades, seguimientos ->
        ResumenPanel.calcular(oportunidades, seguimientos)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenPanel())
}
