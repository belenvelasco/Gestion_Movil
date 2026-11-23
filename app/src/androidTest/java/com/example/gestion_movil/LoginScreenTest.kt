package com.example.gestion_movil

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestion_movil.ui.login.LoginScreen
import com.example.gestion_movil.ui.login.LoginViewModel
import com.example.gestion_movil.ui.login.Rol
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: LoginViewModel
    private var loginsExitosos = 0

    @Before
    fun iniciar() {
        viewModel = LoginViewModel()
        loginsExitosos = 0
        composeRule.setContent {
            MaterialTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginExitoso = { loginsExitosos++ }
                )
            }
        }
    }

    private fun ingresar(usuario: String, clave: String) {
        if (usuario.isNotEmpty()) composeRule.onNodeWithText("Usuario").performTextInput(usuario)
        if (clave.isNotEmpty()) composeRule.onNodeWithText("Clave").performTextInput(clave)
        composeRule.onNodeWithText("Iniciar sesión").performClick()
    }

    @Test
    fun muestraCamposYBoton() {
        composeRule.onNodeWithText("Gestión de oportunidades").assertIsDisplayed()
        composeRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Clave").assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun camposVaciosMuestranErrorYNoEntran() {
        ingresar("", "")

        composeRule.onAllNodesWithText("Campo obligatorio").assertCountEquals(2)
        assertEquals(0, loginsExitosos)
    }

    @Test
    fun credencialesIncorrectasMuestranMensaje() {
        ingresar("ejecutivo", "9999")

        composeRule.onNodeWithText("Usuario o clave incorrectos").assertIsDisplayed()
        assertEquals(0, loginsExitosos)
    }

    @Test
    fun loginDeEjecutivoEntraConSuRol() {
        ingresar(LoginViewModel.USUARIO_PRUEBA, LoginViewModel.CLAVE_PRUEBA)

        composeRule.waitForIdle()
        assertEquals(1, loginsExitosos)
        assertEquals(Rol.EJECUTIVO, viewModel.uiState.value.rol)
    }

    @Test
    fun loginDeSupervisorEntraConSuRol() {
        ingresar(LoginViewModel.USUARIO_SUPERVISOR, LoginViewModel.CLAVE_SUPERVISOR)

        composeRule.waitForIdle()
        assertEquals(1, loginsExitosos)
        assertEquals(Rol.SUPERVISOR, viewModel.uiState.value.rol)
    }
}