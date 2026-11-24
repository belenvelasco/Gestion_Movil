package com.example.gestion_movil

import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.model.Responsable
import com.example.gestion_movil.data.model.ResumenPanel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResumenPanelTest : StringSpec({

    fun op(id: Int, estado: String = "Evento identificado", prioridad: String = "Media", responsable: String = "") =
        OportunidadEntity(
            id = id, nombre = "Op $id", tipo = "Feria", descripcion = "", fuente = "",
            prioridad = prioridad, potencial = "Alto", estado = estado,
            observaciones = "", responsable = responsable
        )

    "cuenta abiertas, confirmadas y descartadas" {
        val r = ResumenPanel.calcular(
            listOf(
                op(1),
                op(2, ResumenPanel.ESTADO_CONFIRMADA),
                op(3, ResumenPanel.ESTADO_DESCARTADA)
            ),
            emptyList()
        )
        r.total shouldBe 3
        r.abiertas shouldBe 1
        r.confirmadas shouldBe 1
        r.descartadas shouldBe 1
    }

    "alta sin responsable ignora comillas sueltas" {
        val r = ResumenPanel.calcular(
            listOf(op(1, prioridad = "Alta", responsable = "''"), op(2, prioridad = "Alta", responsable = "Maria rojas")),
            emptyList()
        )
        r.altaSinResponsable shouldBe 1
    }

    "cuenta por responsable solo las abiertas" {
        val r = ResumenPanel.calcular(
            listOf(
                op(1, responsable = "Maria rojas"),
                op(2, responsable = "Maria rojas"),
                op(3, responsable = "Camilo ramos"),
                op(4),
                op(5, ResumenPanel.ESTADO_CONFIRMADA, responsable = "Maria rojas")
            ),
            emptyList()
        ).porResponsable.toMap()

        r["Maria rojas"] shouldBe 2
        r["Camilo ramos"] shouldBe 1
        r["Valentina peña"] shouldBe 0
        r["Sin asignar"] shouldBe 1
    }

    "al reasignar el responsable el panel se actualiza" {
        val antes = ResumenPanel.calcular(listOf(op(1, prioridad = "Alta")), emptyList())
        val despues = ResumenPanel.calcular(
            listOf(op(1, prioridad = "Alta", responsable = "Valentina peña")), emptyList()
        )
        antes.altaSinResponsable shouldBe 1
        despues.altaSinResponsable shouldBe 0
    }

    "Responsable: mostrar, opciones y conversion a guardar" {
        Responsable.mostrar("") shouldBe "Sin asignar"
        Responsable.mostrar("''") shouldBe "Sin asignar"
        Responsable.mostrar("Maria rojas") shouldBe "Maria rojas"
        Responsable.OPCIONES.first() shouldBe "Sin asignar"
        Responsable.aGuardar("Sin asignar") shouldBe ""
        Responsable.aGuardar("Camilo ramos") shouldBe "Camilo ramos"
    }
})
