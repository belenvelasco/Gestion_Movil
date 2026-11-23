package com.example.gestion_movil.data.model

object Responsable {
    const val  SIN_ASIGNAR = "Sin asignar"

    val LISTA = listOf("Maria rojas", "Camilo ramos","Valentina peña")

    // cuando no hay responsable la pantalla muetra sin asignar
    fun mostrar(responsable: String): String= responsable.ifBlank { SIN_ASIGNAR }
}