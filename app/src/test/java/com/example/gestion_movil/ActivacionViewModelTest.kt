package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.ResultadoEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.data.repository.ActivacionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.activacion.ActivacionViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActivacionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val app = mockk<Application>(relaxed = true)
    private val oportunidadRepo = mockk<OportunidadRepository>(relaxed = true)
    private val repo = mockk<ActivacionRepository>(relaxed = true)
    private val obligatorio = "Campo obligatorio"

    /** Crea el ViewModel con la oportunidad 5 cargada en el estado indicado. */
    private fun TestScope.vmCargado(
        estado: String = "Evento identificado",
        resultadoGuardado: ResultadoEntity? = null
    ): ActivacionViewModel {
        every { oportunidadRepo.observarPorId(any()) } returns flowOf(oportunidadDemo(5, estado = estado))
        coEvery { repo.obtenerActivacion(5) } returns null
        coEvery { repo.obtenerResultado(5) } returns resultadoGuardado
        val vm = ActivacionViewModel(app, oportunidadRepo, repo)
        recolectar(vm.oportunidad)     // la pantalla observa la oportunidad
        vm.cargar(5)
        return vm
    }

    private fun llenarCondiciones(vm: ActivacionViewModel) {
        vm.onEspacio("  Stand central  ")
        vm.onFecha(1_800_000_000_000L)
        vm.onCosto("50000")
        vm.onRequisitos("Mesa y toldo")
        vm.onResponsable("Maria rojas")
    }

    // ---------- Cargar ----------

    @Test
    fun cargar_rellenaElFormularioConLoYaGuardado() = runTest {
        every { oportunidadRepo.observarPorId(any()) } returns flowOf(oportunidadDemo(5))
        coEvery { repo.obtenerActivacion(5) } returns ActivacionEntity(
            oportunidadId = 5, espacio = "Patio", costo = 1000, responsable = "Camilo ramos"
        )
        coEvery { repo.obtenerResultado(5) } returns ResultadoEntity(
            oportunidadId = 5, resultado = "Activación realizada", impacto = "Alto", cerrada = true
        )
        val vm = ActivacionViewModel(app, oportunidadRepo, repo)

        vm.cargar(5)

        val s = vm.uiState.value
        assertEquals("Patio", s.espacio)
        assertEquals("1000", s.costo)
        assertEquals("Camilo ramos", s.responsable)
        assertEquals("Activación realizada", s.resultado)
        assertTrue(s.cerrada)
    }

    // ---------- Confirmar participación ----------

    @Test
    fun confirmarParticipacion_cambiaElEstadoDeLaOportunidad() = runTest {
        val vm = vmCargado(estado = "Propuesta enviada")

        vm.confirmarParticipacion()

        coVerify(exactly = 1) {
            oportunidadRepo.guardar(match { it.estado == ResumenPanel.ESTADO_CONFIRMADA })
        }
        assertEquals("Participación confirmada", vm.uiState.value.mensaje)
    }

    @Test
    fun confirmarParticipacion_siYaEstabaConfirmada_noHaceNada() = runTest {
        val vm = vmCargado(estado = ResumenPanel.ESTADO_CONFIRMADA)

        vm.confirmarParticipacion()

        coVerify(exactly = 0) { oportunidadRepo.guardar(any()) }
    }

    // ---------- Condiciones ----------

    @Test
    fun guardarCondiciones_sinCamposObligatorios_muestraErrores() = runTest {
        val vm = vmCargado()

        vm.guardarCondiciones()

        val s = vm.uiState.value
        assertEquals(obligatorio, s.espacioError)
        assertEquals(obligatorio, s.fechaError)
        assertEquals(obligatorio, s.responsableError)
        coVerify(exactly = 0) { repo.guardarActivacion(any()) }
    }

    @Test
    fun guardarCondiciones_costoInvalido() = runTest {
        val vm = vmCargado()
        llenarCondiciones(vm)

        for (costo in listOf("abc", "-10", "1.5")) {
            vm.onCosto(costo)
            vm.guardarCondiciones()
            assertEquals("Ingresa un número entero válido", vm.uiState.value.costoError)
        }
        coVerify(exactly = 0) { repo.guardarActivacion(any()) }
    }

    @Test
    fun guardarCondiciones_costoVacioEsValido() = runTest {
        val vm = vmCargado()
        llenarCondiciones(vm)
        vm.onCosto("")

        vm.guardarCondiciones()

        assertNull(vm.uiState.value.costoError)
        coVerify(exactly = 1) { repo.guardarActivacion(match { it.costo == null }) }
    }

    @Test
    fun guardarCondiciones_validas_guardaLaActivacion() = runTest {
        val vm = vmCargado()
        llenarCondiciones(vm)

        vm.guardarCondiciones()

        coVerify(exactly = 1) {
            repo.guardarActivacion(
                match {
                    it.oportunidadId == 5 && it.espacio == "Stand central" &&
                        it.costo == 50000 && it.responsable == "Maria rojas"
                }
            )
        }
        assertEquals("Condiciones y responsable guardados", vm.uiState.value.mensaje)
    }

    @Test
    fun guardarCondiciones_sinOportunidadCargada_noHaceNada() {
        val vm = ActivacionViewModel(app, oportunidadRepo, repo)   // idActual = 0
        vm.guardarCondiciones()
        coVerify(exactly = 0) { repo.guardarActivacion(any()) }
    }

    // ---------- Resultado ----------

    @Test
    fun guardarResultado_sinParticipacionConfirmada_avisaYNoGuarda() = runTest {
        val vm = vmCargado(estado = "Evento identificado")
        vm.onResultado("Activación realizada")
        vm.onImpacto("Alto")

        vm.guardarResultado(cerrar = false)

        assertEquals("Confirma la participación antes de registrar resultados", vm.uiState.value.mensaje)
        coVerify(exactly = 0) { repo.guardarResultado(any()) }
    }

    @Test
    fun guardarResultado_sinResultadoNiImpacto_muestraErrores() = runTest {
        val vm = vmCargado(estado = ResumenPanel.ESTADO_CONFIRMADA)

        vm.guardarResultado(cerrar = false)

        val s = vm.uiState.value
        assertEquals(obligatorio, s.resultadoError)
        assertEquals(obligatorio, s.impactoError)
        coVerify(exactly = 0) { repo.guardarResultado(any()) }
    }

    @Test
    fun guardarResultado_valido_sinCerrar() = runTest {
        val vm = vmCargado(estado = ResumenPanel.ESTADO_CONFIRMADA)
        vm.onResultado("Activación realizada")
        vm.onImpacto("Medio")
        vm.onObservaciones("  Todo bien  ")

        vm.guardarResultado(cerrar = false)

        coVerify(exactly = 1) {
            repo.guardarResultado(match { !it.cerrada && it.observaciones == "Todo bien" })
        }
        val s = vm.uiState.value
        assertFalse(s.cerrada)
        assertEquals("Resultado guardado", s.mensaje)
    }

    @Test
    fun guardarResultado_cerrando_cierraLaOportunidad() = runTest {
        val vm = vmCargado(estado = ResumenPanel.ESTADO_CONFIRMADA)
        vm.onResultado("Activación realizada")
        vm.onImpacto("Alto")

        vm.guardarResultado(cerrar = true)

        coVerify(exactly = 1) { repo.guardarResultado(match { it.cerrada }) }
        val s = vm.uiState.value
        assertTrue(s.cerrada)
        assertEquals("Oportunidad cerrada", s.mensaje)
    }

    @Test
    fun guardarResultado_siYaEstabaCerrada_noGuardaDeNuevo() = runTest {
        val cerrada = ResultadoEntity(oportunidadId = 5, resultado = "x", impacto = "Alto", cerrada = true)
        val vm = vmCargado(estado = ResumenPanel.ESTADO_CONFIRMADA, resultadoGuardado = cerrada)

        vm.guardarResultado(cerrar = true)

        coVerify(exactly = 0) { repo.guardarResultado(any()) }
    }

    // ---------- Mensajes y errores ----------

    @Test
    fun escribirEnUnCampo_limpiaSuError_yLimpiarMensaje() = runTest {
        val vm = vmCargado()
        vm.guardarCondiciones()
        assertNotNull(vm.uiState.value.espacioError)

        vm.onEspacio("Patio")
        assertNull(vm.uiState.value.espacioError)

        vm.confirmarParticipacion()
        vm.limpiarMensaje()
        assertNull(vm.uiState.value.mensaje)
    }
}
