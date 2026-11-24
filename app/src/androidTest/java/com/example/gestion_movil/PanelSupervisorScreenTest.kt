package com.example.gestion_movil

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.ui.panel.PanelSupervisorContenido
import org.junit.Rule
import org.junit.Test

class PanelSupervisorScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private fun op(id: Int, prioridad: String, responsable: String = "") = OportunidadEntity(
        id = id, nombre = "Op $id", tipo = "Feria", descripcion = "", fuente = "Web",
        prioridad = prioridad, potencial = "Media", estado = "Evento identificado",
        observaciones = "", responsable = responsable
    )

    @Test
    fun muestraPorResponsableYAltaSinResponsable() {
        val resumen = ResumenPanel.calcular(
            listOf(op(1, "Alta", "Maria rojas"), op(2, "Alta")), emptyList()
        )
        regla.setContent { PanelSupervisorContenido(resumen = resumen, onVerDetalle = {}) }

        regla.onNodeWithText("Por responsable (abiertas)").performScrollTo().assertIsDisplayed()
        regla.onNodeWithText("Alta prioridad sin responsable: 1").performScrollTo().assertIsDisplayed()
    }
}