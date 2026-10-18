package com.example.gestion_movil.ui.oportunidad

data class OportunidadUiState(
    val id: Int = 0,
    val nombre: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fuente: String = "",
    val prioridad: String = "",
    val potencial: String = "",
    val estado: String = "Evento identificado",
    val observaciones: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val ubicando: Boolean = false,
    val ubicacionMensaje: String? = null,
    val fechaDeteccion: Long = 0L,
    val nombreError: String? = null,
    val tipoError: String? = null,
    val descripcionError: String? = null,
    val fuenteError: String? = null,
    val prioridadError: String? = null,
    val potencialError: String? = null,
    val estadoError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)
