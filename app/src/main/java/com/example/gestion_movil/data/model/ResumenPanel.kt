package com.example.gestion_movil.data.model

import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.SeguimientoEntity
import kotlin.collections.component1
import kotlin.collections.component2

data class AccionPendiente(val oportunidad: OportunidadEntity, val proximaAccion: String)

// Números que muestra el panel del supervisor
data class ResumenPanel(
    val total: Int = 0,
    // Oportunidades que siguen en juego (no confirmadas ni descartadas)
    val abiertas: Int = 0,
    val confirmadas: Int = 0,
    val descartadas: Int = 0,
    val altaPrioridadAbiertas: Int = 0,
    val porEstado: List<Pair<String, Int>> = emptyList(),
    val porPrioridad: List<Pair<String, Int>> = emptyList(),
    val porResponsable: List<Pair<String, Int>> = emptyList(),
    val altaSinResponsable: Int = 0,
    val proximosEventos: List<OportunidadEntity> = emptyList(),
    val accionesPendientes: List<AccionPendiente> = emptyList()
) {
    companion object {
        const val ESTADO_CONFIRMADA = "Participación confirmada"
        const val ESTADO_DESCARTADA = "Oportunidad descartada"
        private const val DIA = 86_400_000L

        val ESTADOS = listOf(
            "Evento identificado", "Contacto pendiente", "Contacto realizado",
            "Propuesta enviada", "En espera de respuesta", ESTADO_CONFIRMADA, ESTADO_DESCARTADA
        )
        val PRIORIDADES = listOf("Alta", "Media", "Baja")

        // Función pura (sin Android): fácil de probar con unit tests
        fun calcular(
            oportunidades: List<OportunidadEntity>,
            seguimientos: List<SeguimientoEntity>,
            hoy: Long = System.currentTimeMillis()
        ): ResumenPanel {
            val cerradas = setOf(ESTADO_CONFIRMADA, ESTADO_DESCARTADA)
            val abiertas = oportunidades.filter { it.estado !in cerradas }

            // Medianoche UTC de hoy: un evento de hoy todavía cuenta como próximo
            val inicioHoy = (hoy / DIA) * DIA
            val proximos = oportunidades
                .filter { it.estado != ESTADO_DESCARTADA }
                .filter { (it.fechaEvento ?: -1L) >= inicioHoy }
                .sortedBy { it.fechaEvento }
                .take(5)

            // Solo cuenta la última gestión de cada oportunidad
            val porId = oportunidades.associateBy { it.id }
            val pendientes = seguimientos
                .groupBy { it.oportunidadId}
                .mapNotNull { (id, lista) ->
                    val ultima = lista.maxWithOrNull(compareBy({ it.fecha }, { it.id }))
                    val oportunidad = porId[id]
                    if (ultima != null && oportunidad != null &&
                        oportunidad.estado !in cerradas && ultima.proximaAccion.isNotBlank()
                    ) {
                        AccionPendiente(oportunidad, ultima.proximaAccion)
                    } else {
                        null
                    }
                }
                .sortedBy { it.oportunidad.nombre }
                .take(5)

            return ResumenPanel(
                total = oportunidades.size,
                abiertas = abiertas.size,
                confirmadas = oportunidades.count { it.estado == ESTADO_CONFIRMADA },
                descartadas = oportunidades.count { it.estado == ESTADO_DESCARTADA },
                altaPrioridadAbiertas = abiertas.count { it.prioridad == "Alta" },
                porEstado = ESTADOS.map { e -> e to oportunidades.count { it.estado == e } },
                porPrioridad = PRIORIDADES.map { p -> p to oportunidades.count { it.prioridad == p } },
                porResponsable = (Responsable.LISTA + Responsable.SIN_ASIGNAR).map { r ->
                    r to abiertas.count{ Responsable.mostrar(it.responsable) == r}
                },
                altaSinResponsable = abiertas.count {
                    it.prioridad == "Alta" && Responsable.estaVacio(it.responsable)
                },
                proximosEventos = proximos,
                accionesPendientes = pendientes
            )
        }
    }
}
