package com.example.gestion_movil.data.model

data class EventoExterno(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val fecha: Long,
    val comuna: String,
    val organizacion: String
)

