package com.example.gestion_movil

import com.example.gestion_movil.ui.login.LoginViewModel
import com.example.gestion_movil.ui.login.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var vm: LoginViewModel

    @Before
    fun preparar() {
        vm = LoginViewModel()
    }

    private fun intentar(usuario: String, clave: String) {
        vm.onUsuario(usuario)
        vm.onClave(clave)
        vm.iniciarSesion()
    }

    @Test
    fun camposVacios_muestranErroresYNoEntra() {
        vm.iniciarSesion()
        val s = vm.uiState.value
        assertEquals("Campo obligatorio", s.usuarioError)
        assertEquals("Campo obligatorio", s.claveError)
        assertFalse(s.loginExitoso)
    }

    @Test
    fun soloClaveVacia_marcaSoloLaClave() {
        intentar("ejecutivo", "")
        val s = vm.uiState.value
        assertNull(s.usuarioError)
        assertEquals("Campo obligatorio", s.claveError)
        assertFalse(s.loginExitoso)
    }

    @Test
    fun credencialesIncorrectas_muestranMensajeYNoEntran() {
        intentar("ejecutivo", "0000")
        val s = vm.uiState.value
        assertEquals("Usuario o clave incorrectos", s.claveError)
        assertFalse(s.loginExitoso)
    }

    @Test
    fun usuarioDesconocido_noEntra() {
        intentar("otro", "1234")
        assertFalse(vm.uiState.value.loginExitoso)
    }

    @Test
    fun ejecutivo_entraConRolEjecutivo() {
        intentar("ejecutivo", "1234")
        val s = vm.uiState.value
        assertTrue(s.loginExitoso)
        assertEquals(Rol.EJECUTIVO, s.rol)
        assertNull(s.usuarioError)
        assertNull(s.claveError)
    }

    @Test
    fun supervisor_entraConRolSupervisor() {
        intentar("supervisor", "1234")
        val s = vm.uiState.value
        assertTrue(s.loginExitoso)
        assertEquals(Rol.SUPERVISOR, s.rol)
    }

    @Test
    fun usuarioConEspacios_seRecorta() {
        intentar("  supervisor  ", "1234")
        assertTrue(vm.uiState.value.loginExitoso)
    }

    @Test
    fun escribirEnUnCampo_limpiaSuError() {
        vm.iniciarSesion()
        vm.onUsuario("e")
        vm.onClave("1")
        val s = vm.uiState.value
        assertNull(s.usuarioError)
        assertNull(s.claveError)
    }

    @Test
    fun consumirLogin_bajaLaBanderaYBorraLaClave() {
        intentar("ejecutivo", "1234")
        vm.consumirLogin()
        val s = vm.uiState.value
        assertFalse(s.loginExitoso)
        assertEquals("", s.clave)
    }
}
