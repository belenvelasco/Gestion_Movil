package com.example.gestion_movil.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


enum class  Rol {EJECUTIVO, SUPERVISOR}
data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val usuarioError: String? = null,
    val claveError : String? = null,
    val loginExitoso: Boolean = false,
    //
    val rol: Rol = Rol.EJECUTIVO
)

class LoginViewModel : ViewModel(){

    companion object{
        const val USUARIO_PRUEBA = "ejecutivo"
        const val  CLAVE_PRUEBA = "1234"

        const val USUARIO_SUPERVISOR = "supervisor"

        const val CLAVE_SUPERVISOR = "1234"
    }

    private val _uistate = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> =  _uistate.asStateFlow()

    fun onUsuario(v: String) = _uistate.update { it.copy(usuario = v, usuarioError = null) }

    fun onClave(v: String) = _uistate.update { it.copy(clave = v, claveError = null) }

    fun iniciarSesion() {
        val s = _uistate.value
        val usuario = s.usuario.trim()

        val usuarioError = if (usuario.isBlank()) "Campo obligatorio" else null
        val claveError = if (s.clave.isBlank()) "Campo obligatorio" else null

        var rol: Rol? = null
        var credencialesError: String? = null

        if (usuarioError == null && claveError == null) {
            rol = when {
                usuario == USUARIO_PRUEBA && s.clave == CLAVE_PRUEBA -> Rol.EJECUTIVO
                usuario == USUARIO_SUPERVISOR && s.clave == CLAVE_SUPERVISOR -> Rol.SUPERVISOR
                else -> null
            }
            if (rol == null) credencialesError = "Usuario o clave incorrectos"
        }

        _uistate.update {
            it.copy(
                usuarioError = usuarioError,
                claveError = claveError ?: credencialesError,
                rol = rol ?: it.rol,
                loginExitoso = rol != null
            )
        }
    }

    fun consumirLogin() = _uistate.update { it.copy(loginExitoso = false, clave = "") }
}