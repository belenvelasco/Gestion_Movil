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
    val fechaDeteccion: Long = 0L,
    val nombreError: String? = null,
    val tipoError: String? = null,
    val descripcionError: String? = null,
    val fuenteError: String? = null,
    val prioridadError: String? = null,
    val potencialError: String? = null,
    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)

object OportunidadOpciones {
    val tipos = listOf(
        "Evento corporativo", "Feria empresarial", "Actividad municipal",
        "Jornada de beneficios", "Feria institucional",
        "Actividad de trabajadores", "Activación comercial", "Otra instancia relevante"
    )
    val prioridades = listOf("Alta", "Media", "Baja")
    val potenciales = listOf("Alto", "Medio", "Bajo")
    val estados = listOf(
        "Evento identificado", "Contacto pendiente", "Contacto realizado",
        "Propuesta enviada", "En espera de respuesta",
        "Participación confirmada", "Oportunidad descartada"
    )
}