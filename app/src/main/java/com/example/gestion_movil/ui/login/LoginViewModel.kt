package com.example.gestion_movil.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class  Rol {EJECUTIVO, SUPERVISOR}
data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val UsuarioError: String? = null,
    val ClaveError : String? = null,
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

    fun onUsuario(v: String) = _uistate.update { it.copy(usuario = v, UsuarioError = null) }

    fun onClave(v: String) = _uistate.update { it.copy(clave = v, ClaveError = null) }

    fun iniciarSesion(){
        val s = _uistate.value
        val usuarioError = if (s.usuario.isBlank()) "Campo obligatorio" else null
        val usuario = s.usuario.trim()
        val  rol = when {
            usuario == USUARIO_PRUEBA && s.clave == CLAVE_PRUEBA -> Rol.EJECUTIVO
            usuario == USUARIO_SUPERVISOR && s.clave == CLAVE_SUPERVISOR -> Rol.SUPERVISOR
            else -> null
        }

        val claveError = when{
            s.clave.isBlank() -> "campo obligatorio"
            usuarioError == null && rol == null -> "usuario o cleve incorrecta"
            else -> null
        }
        _uistate.update {
            it.copy(
                UsuarioError = usuarioError,
                ClaveError = claveError,
                loginExitoso = usuarioError == null && claveError == null,
                rol = rol ?: it.rol
            )
        }
    }

    fun consumirLogin() = _uistate.update { it.copy(loginExitoso = false, clave = "") }
}