package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.repository.GestionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.DetalleOportunidadViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetalleOportunidadViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val oportunidadRepo = mockk<OportunidadRepository>(relaxed = true)
    private val gestionRepo = mockk<GestionRepository>(relaxed = true)

    private fun crearVm(id: Int = 3): DetalleOportunidadViewModel {
        val vm = DetalleOportunidadViewModel(mockk<Application>(relaxed = true), oportunidadRepo, gestionRepo)
        vm.cargar(id)
        return vm
    }

    // ---------- Contactos ----------

    @Test
    fun guardarContacto_conFormularioVacio_marcaNombreYCargo() {
        val vm = crearVm()
        vm.abrirFormContacto()

        vm.guardarContacto()

        val s = vm.uiState.value
        assertEquals("Campo obligatorio", s.contactoNombreError)
        assertEquals("Campo obligatorio", s.contactoCargoError)
        assertNull(s.contactoCorreoError)          // el correo es opcional
        coVerify(exactly = 0) { gestionRepo.agregarContacto(any()) }
    }

    @Test
    fun guardarContacto_correoConFormatoInvalido() {
        val vm = crearVm()
        vm.abrirFormContacto()
        vm.onContactoNombre("Ana Demo")
        vm.onContactoCargo("Jefa de eventos")

        for (correo in listOf("ana", "ana@", "ana@demo", "ana demo@x.cl")) {
            vm.onContactoCorreo(correo)
            vm.guardarContacto()
            assertEquals("Correo no válido", vm.uiState.value.contactoCorreoError)
        }
        coVerify(exactly = 0) { gestionRepo.agregarContacto(any()) }
    }

    @Test
    fun guardarContacto_valido_guardaYCierraElFormulario() {
        val vm = crearVm(id = 3)
        vm.abrirFormContacto()
        assertTrue(vm.uiState.value.mostrarFormContacto)
        vm.onContactoNombre("  Ana Demo ")
        vm.onContactoCargo("Jefa de eventos")
        vm.onContactoCorreo("ana@demo.cl")
        vm.onContactoTelefono("+56 9 0000 0000")

        vm.guardarContacto()

        coVerify(exactly = 1) {
            gestionRepo.agregarContacto(
                match { it.oportunidadId == 3 && it.nombre == "Ana Demo" && it.correo == "ana@demo.cl" }
            )
        }
        assertFalse(vm.uiState.value.mostrarFormContacto)
    }

    @Test
    fun guardarContacto_sinOportunidadCargada_noHaceNada() {
        val vm = crearVm(id = 0)
        vm.onContactoNombre("Ana")
        vm.onContactoCargo("Jefa")

        vm.guardarContacto()

        coVerify(exactly = 0) { gestionRepo.agregarContacto(any()) }
    }

    @Test
    fun eliminarContacto_delegaEnElRepositorio() {
        val vm = crearVm()
        val contacto = com.example.gestion_movil.data.local.ContactoEntity(
            oportunidadId = 3, nombre = "Ana", cargo = "Jefa"
        )

        vm.eliminarContacto(contacto)

        coVerify(exactly = 1) { gestionRepo.eliminarContacto(contacto) }
    }

    @Test
    fun abrirYCerrarFormularioDeContacto() {
        val vm = crearVm()
        vm.abrirFormContacto()
        assertTrue(vm.uiState.value.mostrarFormContacto)
        vm.cerrarFormContacto()
        assertFalse(vm.uiState.value.mostrarFormContacto)
    }

    // ---------- Gestiones ----------

    @Test
    fun guardarGestion_sinTipoNiNota_muestraErrores() {
        val vm = crearVm()
        vm.abrirFormGestion()

        vm.guardarGestion()

        val s = vm.uiState.value
        assertEquals("Campo obligatorio", s.gestionTipoError)
        assertEquals("Campo obligatorio", s.gestionNotaError)
        coVerify(exactly = 0) { gestionRepo.agregarSeguimiento(any()) }
    }

    @Test
    fun guardarGestion_sinProximaAccion_seGuardaVacia() {
        val vm = crearVm()
        vm.abrirFormGestion()
        vm.onGestionTipo("Reunión")
        vm.onGestionNota("  Se acordó enviar propuesta  ")
        vm.onGestionProximaAccion(vm.proximasAcciones.first())      // "Sin próxima acción"

        vm.guardarGestion()

        coVerify(exactly = 1) {
            gestionRepo.agregarSeguimiento(
                match { it.proximaAccion == "" && it.nota == "Se acordó enviar propuesta" && it.tipo == "Reunión" }
            )
        }
        assertFalse(vm.uiState.value.mostrarFormGestion)
    }

    @Test
    fun guardarGestion_conProximaAccion_laConserva() {
        val vm = crearVm()
        vm.abrirFormGestion()
        vm.onGestionTipo("Contacto realizado")
        vm.onGestionNota("Llamada")
        vm.onGestionProximaAccion("Seguimiento")

        vm.guardarGestion()

        coVerify(exactly = 1) { gestionRepo.agregarSeguimiento(match { it.proximaAccion == "Seguimiento" }) }
    }

    // ---------- Supervisor: prioridad y responsable ----------

    @Test
    fun ajustarPrioridad_guardaLaOportunidadConLaNuevaPrioridad() = runTest {
        every { oportunidadRepo.observarPorId(any()) } returns flowOf(oportunidadDemo(3, prioridad = "Baja"))
        val vm = crearVm(id = 3)
        recolectar(vm.oportunidad)

        vm.ajustarPrioridad("Alta")

        coVerify(exactly = 1) { oportunidadRepo.guardar(match { it.prioridad == "Alta" }) }
    }

    @Test
    fun ajustarPrioridad_siEsLaMisma_noHaceNada() = runTest {
        every { oportunidadRepo.observarPorId(any()) } returns flowOf(oportunidadDemo(3, prioridad = "Alta"))
        val vm = crearVm(id = 3)
        recolectar(vm.oportunidad)

        vm.ajustarPrioridad("Alta")

        coVerify(exactly = 0) { oportunidadRepo.guardar(any()) }
    }

    @Test
    fun reasignarResponsable_delegaEnElRepositorio() {
        val vm = crearVm(id = 3)

        vm.reasignarResponsable("Camilo ramos")

        coVerify(exactly = 1) { oportunidadRepo.reasignarResponsable(3, "Camilo ramos") }
    }

    @Test
    fun reasignarResponsable_sinOportunidadCargada_noHaceNada() {
        val vm = crearVm(id = 0)

        vm.reasignarResponsable("Camilo ramos")

        coVerify(exactly = 0) { oportunidadRepo.reasignarResponsable(any(), any()) }
    }
}
