package com.example.gestion_movil

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestion_movil.ui.componentes.CampoDesplegable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampoDesplegableTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun seleccionarUnaOpcionLlamaAlCallback() {
        var seleccionada: String? = null

        composeRule.setContent {
            MaterialTheme {
                CampoDesplegable(
                    label = "Prioridad",
                    valor = "Media",
                    opciones = listOf("Alta", "Media", "Baja"),
                    error = null,
                    onSeleccion = { seleccionada = it }
                )
            }
        }

        composeRule.onNodeWithText("Media").assertIsDisplayed()

        composeRule.onNodeWithText("Prioridad").performClick()
        composeRule.onNodeWithText("Alta").performClick()

        composeRule.waitForIdle()
        assertEquals("Alta", seleccionada)
    }
}