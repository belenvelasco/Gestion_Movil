package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.OrganizacionEntity
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.OportunidadUiState
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class OportunidadViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<OportunidadRepository>(relaxed = true)

    private fun crearVm(lista: List<OportunidadEntity> = emptyList()): OportunidadViewModel {
        // El ViewModel lee repository.oportunidades al crearse, por eso se define antes.
        every { repo.oportunidades } returns flowOf(lista)
        return OportunidadViewModel(mockk<Application>(relaxed = true), repo)
    }

    private fun llenarFormularioValido(vm: OportunidadViewModel) {
        vm.onNombre("  Feria Demo  ")
        vm.onTipo("Feria empresarial")
        vm.onOrganizacion("Org Demo")
        vm.onFechaEvento(1_800_000_000_000L)
        vm.onComuna("Puente Alto")
        vm.onDescripcion("Descripción")
        vm.onFuente("Redes sociales")
        vm.onPrioridad("Alta")
        vm.onPotencial("Media")
        vm.onEstado("Evento identificado")
    }

    // ---------- Validaciones ----------

    @Test
    fun guardarConFormularioVacio_marcaLosCamposObligatoriosYNoGuarda() {
        val vm = crearVm()

        vm.guardar()

        val s = vm.uiState.value
        val obligatorio = "Campo obligatorio"
        assertEquals(obligatorio, s.nombreError)
        assertEquals(obligatorio, s.tipoError)
        assertEquals(obligatorio, s.organizacionError)
        assertEquals(obligatorio, s.fechaEventoError)
        assertEquals(obligatorio, s.comunaError)
        assertEquals(obligatorio, s.descripcionError)
        assertEquals(obligatorio, s.fuenteError)
        assertEquals(obligatorio, s.prioridadError)
        assertEquals(obligatorio, s.potencialError)
        assertFalse(s.guardadoExitosamente)
        coVerify(exactly = 0) { repo.guardar(any()) }
    }

    @Test
    fun potencialFueraDeLasOpciones_esInvalido() {
        val vm = crearVm()
        llenarFormularioValido(vm)
        vm.onPotencial("Gigante")

        vm.guardar()

        assertEquals("Opción no válida", vm.uiState.value.potencialError)
        coVerify(exactly = 0) { repo.guardar(any()) }
    }

    @Test
    fun publicoEsperado_debeSerUnEnteroMayorACero() {
        val vm = crearVm()
        llenarFormularioValido(vm)

        for (valor in listOf("abc", "0", "-3", "2.5")) {
            vm.onPublicoEsperado(valor)
            vm.guardar()
            assertEquals(
                "Con '$valor' debía mostrar error",
                "Ingresa un número entero mayor a 0",
                vm.uiState.value.publicoEsperadoError
            )
        }
        coVerify(exactly = 0) { repo.guardar(any()) }
    }

    @Test
    fun publicoEsperado_esOpcional() {
        val vm = crearVm()
        llenarFormularioValido(vm)
        coEvery { repo.obtenerOCrearOrganizacion(any()) } returns 1

        vm.onPublicoEsperado("")
        vm.guardar()

        assertNull(vm.uiState.value.publicoEsperadoError)
        coVerify(exactly = 1) { repo.guardar(any()) }
    }

    @Test
    fun alEscribirEnUnCampo_seBorraSoloSuError() {
        val vm = crearVm()
        vm.guardar()

        vm.onNombre("Algo")

        val s = vm.uiState.value
        assertNull(s.nombreError)
        assertNotNull(s.tipoError)
    }

    // ---------- Guardar ----------

    @Test
    fun guardarValido_creaLaOrganizacionYGuardaLaOportunidad() {
        val vm = crearVm()
        llenarFormularioValido(vm)
        vm.onPublicoEsperado("150")
        coEvery { repo.obtenerOCrearOrganizacion("Org Demo") } returns 7
        coEvery { repo.guardar(any()) } returns 1L

        vm.guardar()

        val capturada = slot<OportunidadEntity>()
        coVerify(exactly = 1) { repo.guardar(capture(capturada)) }
        assertEquals("Feria Demo", capturada.captured.nombre)   // sin espacios sobrantes
        assertEquals(7, capturada.captured.organizacionId)
        assertEquals(150, capturada.captured.publicoEsperado)
        assertEquals("Puente Alto", capturada.captured.comuna)

        val s = vm.uiState.value
        assertTrue(s.guardadoExitosamente)
        assertFalse(s.guardando)
    }

    @Test
    fun consumirGuardado_bajaLaBandera() {
        val vm = crearVm()
        llenarFormularioValido(vm)
        coEvery { repo.obtenerOCrearOrganizacion(any()) } returns 1
        vm.guardar()

        vm.consumirGuardado()

        assertFalse(vm.uiState.value.guardadoExitosamente)
    }

    // ---------- Cargar (crear o editar) ----------

    @Test
    fun cargarConIdCero_dejaElFormularioVacio() {
        val vm = crearVm()
        vm.onNombre("Algo escrito")

        vm.cargar(0)

        assertEquals(OportunidadUiState(), vm.uiState.value)
    }

    @Test
    fun cargarConIdExistente_rellenaElFormulario() {
        val vm = crearVm()
        val existente = oportunidadDemo(id = 3, nombre = "Expo Demo", organizacionId = 2, comuna = "Maipú")
            .copy(publicoEsperado = 80)
        coEvery { repo.obtenerPorId(3) } returns existente
        coEvery { repo.obtenerOrganizacion(2) } returns OrganizacionEntity(id = 2, nombre = "Org X")

        vm.cargar(3)

        val s = vm.uiState.value
        assertEquals(3, s.id)
        assertEquals("Expo Demo", s.nombre)
        assertEquals("Org X", s.organizacion)
        assertEquals("Maipú", s.comuna)
        assertEquals("80", s.publicoEsperado)
    }

    // ---------- Ubicación ----------

    @Test
    fun ubicacion_siguelosEstadosObtenerErrorYQuitar() {
        val vm = crearVm()

        vm.iniciarUbicacion()
        assertTrue(vm.uiState.value.ubicando)

        vm.onUbicacionObtenida(-33.61, -70.57)
        var s = vm.uiState.value
        assertFalse(s.ubicando)
        assertEquals(-33.61, s.latitud!!, 0.0001)
        assertEquals(-70.57, s.longitud!!, 0.0001)

        vm.onUbicacionError("Sin permiso")
        assertEquals("Sin permiso", vm.uiState.value.ubicacionMensaje)

        vm.quitarUbicacion()
        s = vm.uiState.value
        assertNull(s.latitud)
        assertNull(s.longitud)
        assertNull(s.ubicacionMensaje)
    }

    // ---------- Lista, búsqueda y filtros ----------

    private val feria = oportunidadDemo(1, "Feria Municipal", "Feria institucional", prioridad = "Alta", comuna = "Puente Alto")
    private val jornada = oportunidadDemo(2, "Jornada Bienestar", "Jornada de beneficios", prioridad = "Media")
    private val expo = oportunidadDemo(3, "Expo Demo", "Feria empresarial", prioridad = "Alta")

    @Test
    fun listaFiltrada_sinFiltrosMuestraTodo() = runTest {
        val vm = crearVm(listOf(feria, jornada, expo))
        recolectar(vm.listaFiltrada)

        assertEquals(3, vm.listaFiltrada.value.size)
    }

    @Test
    fun filtroPorPrioridad() = runTest {
        val vm = crearVm(listOf(feria, jornada, expo))
        recolectar(vm.listaFiltrada)

        vm.onFiltroPrioridad("Alta")

        assertEquals(listOf(feria, expo), vm.listaFiltrada.value)
    }

    @Test
    fun busquedaPorTextoSinDistinguirMayusculas() = runTest {
        val vm = crearVm(listOf(feria, jornada, expo))
        recolectar(vm.listaFiltrada)

        vm.onBusqueda("JORNADA")
        assertEquals(listOf(jornada), vm.listaFiltrada.value)

        vm.onBusqueda("puente")           // coincide con la comuna
        assertEquals(listOf(feria), vm.listaFiltrada.value)
    }

    @Test
    fun busquedaYPrioridadSeCombinan() = runTest {
        val vm = crearVm(listOf(feria, jornada, expo))
        recolectar(vm.listaFiltrada)

        vm.onFiltroPrioridad("Alta")
        vm.onBusqueda("expo")

        assertEquals(listOf(expo), vm.listaFiltrada.value)
    }

    @Test
    fun busquedaSinCoincidencias_devuelveListaVacia() = runTest {
        val vm = crearVm(listOf(feria, jornada, expo))
        recolectar(vm.listaFiltrada)

        vm.onBusqueda("zzz")

        assertTrue(vm.listaFiltrada.value.isEmpty())
    }

    /**
     * HALLAZGO: organizacionesPorId se arma con repository.oportunidades (nombres de oportunidades)
     * en vez de repository.organizaciones. Por eso buscar por nombre de organización no funciona.
     * Cuando lo corrijan (ver la guía), quiten @Ignore y agreguen el stub de repo.organizaciones.
     */
    @Test
    fun busquedaPorNombreDeOrganizacion() = runTest {
        val conOrg = oportunidadDemo(4, "Evento X", organizacionId = 9)
        every { repo.organizaciones } returns flowOf(listOf(OrganizacionEntity(9, "Municipalidad Demo")))
        val vm = crearVm(listOf(conOrg, jornada))
        recolectar(vm.listaFiltrada)

        vm.onBusqueda("municipalidad")

        assertEquals(listOf(conOrg), vm.listaFiltrada.value)
    }

    // ---------- Código QR y eliminar ----------

    @Test
    fun buscarPorCodigo_porIdYPorNombre() = runTest {
        val vm = crearVm(listOf(feria, jornada))
        recolectar(vm.lista)

        assertEquals(jornada, vm.buscarPorCodigo("2"))
        assertEquals(feria, vm.buscarPorCodigo("  feria municipal "))
        assertNull(vm.buscarPorCodigo("99"))
    }

    @Test
    fun eliminar_delegaEnElRepositorio() {
        val vm = crearVm()

        vm.eliminar(feria)

        coVerify(exactly = 1) { repo.eliminar(feria) }
    }
}
