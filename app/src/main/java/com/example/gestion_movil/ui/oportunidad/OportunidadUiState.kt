package com.example.gestion_movil.ui.oportunidad

import com.google.android.datatransport.runtime.Destination


data object OportunidadUiState (
    val id: Int = 0,
    val nombre : String ="",
    val tipo: String = ""
    val descripcion : String = "",
    val fuente : String = "",
    val prioridad : String = "",
    val potencial : String = "",
    val estado : String = "",
    val observaciones : String = "",
    val nombreError : String ? = null,
    val tipoError: String? = null,
    val descripcionError: String ?= null,
    val fuenteError: String?= null,
    val prioridadError: String?= null,
    val potencialError: String? = null,
    val estadoError: String ?= null,
    val guardado: String? = null,
    val guardadoExitosamente: Boolean = false
)