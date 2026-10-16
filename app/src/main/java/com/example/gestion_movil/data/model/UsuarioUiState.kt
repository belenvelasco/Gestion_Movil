package com.example.gestion_movil.data.model
//reprecenta el formurario del usuario

data class UsuarioUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val direccion: String = "",
    val errores: UsuarioErrores = UsuarioErrores(),
    val aceptaTerminos: Boolean = false
)