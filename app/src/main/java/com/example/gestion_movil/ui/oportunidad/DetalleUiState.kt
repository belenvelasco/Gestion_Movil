package com.example.gestion_movil.ui.oportunidad

// Estado de los dos formularios emergentes de la pantalla de detalle:
// "Agregar contacto" y "Registrar gestión"
data class DetalleUiState(
    // Formulario de contacto
    val mostrarFormContacto: Boolean = false,
    val contactoNombre: String = "",
    val contactoCargo: String = "",
    val contactoCorreo: String = "",
    val contactoTelefono: String = "",
    val contactoNombreError: String? = null,
    val contactoCargoError: String? = null,
    val contactoCorreoError: String? = null,

    // Formulario de gestión (contacto realizado, propuesta enviada, reunión...)
    val mostrarFormGestion: Boolean = false,
    val gestionTipo: String = "",
    val gestionNota: String = "",
    val gestionProximaAccion: String = "",
    val gestionTipoError: String? = null,
    val gestionNotaError: String? = null
)
