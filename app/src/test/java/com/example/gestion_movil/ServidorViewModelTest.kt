package com.example.gestion_movil

import com.example.gestion_movil.data.remote.ApiBackend
import com.example.gestion_movil.data.remote.OportunidadRemotaDto
import com.example.gestion_movil.ui.servidor.ServidorViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServidorViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val api = mockk<ApiBackend>()

    private fun dto(id: Long) = OportunidadRemotaDto(
        id = id, nombre = "Oportunidad $id", tipo = "Feria", descripcion = "Demo",
        fuente = "Fuente", prioridad = "Alta", potencial = "Alto", estado = "Evento identificado"
    )

    @Test
    fun alIniciar_cargaLasOportunidadesDelServidor() {
        coEvery { api.getOportunidades() } returns listOf(dto(1), dto(2))

        val vm = ServidorViewModel(api)

        val s = vm.uiState.value
        assertFalse(s.cargando)
        assertEquals(2, s.oportunidades.size)
        assertNull(s.error)
    }

    @Test
    fun siElServidorFalla_muestraMensajeDeError() {
        coEvery { api.getOportunidades() } throws IOException("sin conexión")

        val vm = ServidorViewModel(api)

        val s = vm.uiState.value
        assertFalse(s.cargando)
        assertTrue(s.oportunidades.isEmpty())
        assertEquals("No se pudo conectar con el servidor. Revisa que esté encendido.", s.error)
    }

    @Test
    fun reintentar_recuperaLosDatosYQuitaElError() {
        coEvery { api.getOportunidades() } throws IOException("caído") andThen listOf(dto(1))

        val vm = ServidorViewModel(api)
        assertNotNull(vm.uiState.value.error)

        vm.cargar()

        val s = vm.uiState.value
        assertNull(s.error)
        assertEquals(1, s.oportunidades.size)
        coVerify(exactly = 2) { api.getOportunidades() }
    }
}
