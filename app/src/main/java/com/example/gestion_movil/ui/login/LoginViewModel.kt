package com.example.gestion_movil.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val UsuarioError: String? = null,
    val ClaveError : String? = null,
    val loginExitoso: Boolean = false
)

class LoginViewModel : ViewModel(){

    companion object{
        const val USUARIO_PRUEBA = "ejecutivo"
        const val  CLAVE_PRUEBA = "1234"
    }

    private val _uistate = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> =  _uistate.asStateFlow()

    fun onUsuario(v: String) = _uistate.update { it.copy(usuario = v, UsuarioError = null) }

    fun onClave(v: String) = _uistate.update { it.copy(clave = v, ClaveError = null) }

    fun iniciarSesion(){
        val s = _uistate.value
        val usuarioError = if (s.usuario.isBlank()) "Campo obligatorio" else null
        val  claveError = when {
            s.clave.isBlank() -> "Campo obligatorio"
            usuarioError == null &&
                    (s.usuario.trim().lowercase() != USUARIO_PRUEBA || s.clave !=CLAVE_PRUEBA) ->
                        "Usuario o clave incorrecta"
            else -> null
        }
        _uistate.update {
            it.copy(
                UsuarioError = usuarioError,
                ClaveError = claveError,
                loginExitoso = usuarioError == null && claveError == null
            )
        }
    }

    fun consumirLogin() = _uistate.update { it.copy(loginExitoso = false, clave = "") }
}