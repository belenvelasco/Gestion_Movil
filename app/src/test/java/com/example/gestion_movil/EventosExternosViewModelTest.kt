package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.model.EventoExterno
import com.example.gestion_movil.data.repository.EventoRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.eventos.EventosExternosViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventosExternosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val eventoRepo = mockk<EventoRepository>()
    private val oportunidadRepo = mockk<OportunidadRepository>(relaxed = true)

    private fun evento(id: Int) = EventoExterno(
        id = id, nombre = "Evento $id", descripcion = "Descripción $id",
        fecha = 1_800_000_000_000L, comuna = "Santiago", organizacion = "Org $id"
    )

    private fun crearVm(): EventosExternosViewModel =
        EventosExternosViewModel(mockk<Application>(relaxed = true), eventoRepo, oportunidadRepo)

    @Test
    fun alIniciar_cargaElCatalogoDeEventos() {
        coEvery { eventoRepo.obtenerEventos(any()) } returns listOf(evento(1), evento(2))
        every { oportunidadRepo.oportunidades } returns flowOf(emptyList())

        val vm = crearVm()

        val s = vm.uiState.value
        assertFalse(s.cargando)
        assertEquals(2, s.eventos.size)
        assertNull(s.error)
        assertTrue(s.importados.isEmpty())
    }

    @Test
    fun marcaComoImportadosLosEventosQueYaSonOportunidades() {
        val e1 = evento(1)
        val yaImportada = oportunidadDemo(1).copy(fuente = EventosExternosViewModel.fuenteDe(e1))
        coEvery { eventoRepo.obtenerEventos(any()) } returns listOf(e1, evento(2))
        every { oportunidadRepo.oportunidades } returns flowOf(listOf(yaImportada))

        val vm = crearVm()

        assertEquals(setOf(1), vm.uiState.value.importados)
    }

    @Test
    fun siLaApiFalla_muestraUnMensajeDeError() {
        coEvery { eventoRepo.obtenerEventos(any()) } throws IOException("sin internet")
        every { oportunidadRepo.oportunidades } returns flowOf(emptyList())

        val vm = crearVm()

        val s = vm.uiState.value
        assertFalse(s.cargando)
        assertNotNull(s.error)
        assertTrue(s.eventos.isEmpty())
    }

    @Test
    fun importar_creaLaOportunidadYMarcaElEventoComoImportado() {
        val e = evento(4)
        coEvery { eventoRepo.obtenerEventos(any()) } returns listOf(e)
        every { oportunidadRepo.oportunidades } returns flowOf(emptyList())
        coEvery { oportunidadRepo.obtenerOCrearOrganizacion("Org 4") } returns 12
        val vm = crearVm()

        vm.importar(e)

        coVerify(exactly = 1) {
            oportunidadRepo.guardar(
                match {
                    it.nombre == "Evento 4" &&
                        it.estado == "Evento identificado" &&
                        it.prioridad == "Media" &&
                        it.organizacionId == 12 &&
                        it.fuente == EventosExternosViewModel.fuenteDe(e)
                }
            )
        }
        assertTrue(4 in vm.uiState.value.importados)
    }

    @Test
    fun importar_siLaDescripcionVieneVacia_usaElNombre() {
        val e = evento(7).copy(descripcion = "")
        coEvery { eventoRepo.obtenerEventos(any()) } returns listOf(e)
        every { oportunidadRepo.oportunidades } returns flowOf(emptyList())
        coEvery { oportunidadRepo.obtenerOCrearOrganizacion(any()) } returns 1
        val vm = crearVm()

        vm.importar(e)

        coVerify { oportunidadRepo.guardar(match { it.descripcion == "Evento 7" }) }
    }

    @Test
    fun fuenteDe_incluyeElIdDelEvento() {
        assertTrue(EventosExternosViewModel.fuenteDe(evento(9)).contains("ID 9"))
    }
}
