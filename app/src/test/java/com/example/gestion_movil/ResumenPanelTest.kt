package com.example.gestion_movil

import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.model.ResumenPanel
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenPanelTest {

    private fun op(id: Int, estado: String, prioridad: String = "Media", responsable: String = "") =
        OportunidadEntity(
            id = id, nombre = "Op $id", tipo = "Feria", descripcion = "", fuente = "",
            prioridad = prioridad, potencial = "Alto", estado = estado,
            observaciones = "", responsable = responsable
        )

    @Test
    fun cuentaAbiertasConfirmadasYDescartadas() {
        val r = ResumenPanel.calcular(
            listOf(
                op(1, "Evento identificado"),
                op(2, ResumenPanel.ESTADO_CONFIRMADA),
                op(3, ResumenPanel.ESTADO_DESCARTADA)
            ),
            emptyList()
        )
        assertEquals(3, r.total)
        assertEquals(1, r.abiertas)
        assertEquals(1, r.confirmadas)
        assertEquals(1, r.descartadas)
    }

    @Test
    fun altaSinResponsableIgnoraComillasSueltas() {
        val r = ResumenPanel.calcular(
            listOf(
                op(1, "Evento identificado", "Alta", "''"),
                op(2, "Evento identificado", "Alta", "Maria rojas")
            ),
            emptyList()
        )
        assertEquals(1, r.altaSinResponsable)
    }
}