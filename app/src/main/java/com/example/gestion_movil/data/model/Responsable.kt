package com.example.gestion_movil.data.model

object Responsable {
    const val SIN_ASIGNAR = "Sin asignar"
    val LISTA = listOf("Maria rojas", "Camilo ramos", "Valentina peña")

    // Considera vacío también el valor "''" que quedó guardado por el bug anterior
    fun estaVacio(responsable: String) = responsable.trim().trim('\'').isBlank()

    fun mostrar(responsable: String): String =
        if (estaVacio(responsable)) SIN_ASIGNAR else responsable.trim()
}