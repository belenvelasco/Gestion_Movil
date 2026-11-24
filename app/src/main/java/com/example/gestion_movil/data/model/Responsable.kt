package com.example.gestion_movil.data.model

object Responsable {
    const val SIN_ASIGNAR = "Sin asignar"
    val LISTA = listOf("Maria rojas", "Camilo ramos", "Valentina peña")

    // Opciones para reasignar: incluye "Sin asignar" para poder quitar al responsable
    val OPCIONES = listOf(SIN_ASIGNAR) + LISTA

    fun aGuardar(opcion: String): String =
        if (opcion == SIN_ASIGNAR) "" else opcion.trim()

    // Considera vacío también el valor "''" que quedó guardado por el bug anterior
    fun estaVacio(responsable: String) = responsable.trim().trim('\'').isBlank()

    fun mostrar(responsable: String): String =
        if (estaVacio(responsable)) SIN_ASIGNAR else responsable.trim()
}