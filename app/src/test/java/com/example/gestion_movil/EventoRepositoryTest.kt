package com.example.gestion_movil


import com.example.gestion_movil.data.remote.AddressDto
import com.example.gestion_movil.data.remote.ApiService
import com.example.gestion_movil.data.remote.CompanyDto
import com.example.gestion_movil.data.remote.PostDto
import com.example.gestion_movil.data.remote.UserDto
import com.example.gestion_movil.data.repository.EventoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventoRepositoryTest {

    private val api = mockk<ApiService>()
    private val repo = EventoRepository(api)
    private val dia = 86_400_000L

    @Test
    fun transformaPostsYUsuariosEnEventos() = runTest {
        coEvery { api.getPosts(any()) } returns listOf(
            PostDto(userId = 1, id = 1, title = "feria de beneficios", body = "Línea uno\nlínea dos")
        )
        coEvery { api.getUsers() } returns listOf(
            UserDto(id = 1, name = "Ana", address = AddressDto("Puente Alto"), company = CompanyDto("Empresa Demo"))
        )

        val eventos = repo.obtenerEventos(hoy = 0L)

        assertEquals(1, eventos.size)
        val e = eventos.first()
        assertEquals("Feria de beneficios", e.nombre)       // primera letra en mayúscula
        assertEquals("Línea uno línea dos", e.descripcion)   // sin saltos de línea
        assertEquals("Puente Alto", e.comuna)
        assertEquals("Empresa Demo", e.organizacion)
        assertEquals(10 * dia, e.fecha)                       // (0 + 7 + 1*3) días
    }

    @Test
    fun sinDatosDelUsuario_usaValoresPorDefecto() = runTest {
        coEvery { api.getPosts(any()) } returns listOf(PostDto(userId = 99, id = 5, title = null, body = null))
        coEvery { api.getUsers() } returns emptyList()

        val e = repo.obtenerEventos(hoy = 0L).first()

        assertEquals("Evento 5", e.nombre)
        assertEquals("", e.descripcion)
        assertEquals("Sin comuna", e.comuna)
        assertEquals("Sin organización", e.organizacion)
    }

    @Test
    fun lasFechasDeEjemploSonFuturasYCrecenConElId() = runTest {
        coEvery { api.getPosts(any()) } returns listOf(
            PostDto(1, 1, "a", "b"), PostDto(1, 2, "c", "d")
        )
        coEvery { api.getUsers() } returns emptyList()

        val eventos = repo.obtenerEventos(hoy = 0L)

        assertTrue(eventos[0].fecha > 0)
        assertTrue(eventos[1].fecha > eventos[0].fecha)
    }

    @Test(expected = java.io.IOException::class)
    fun siLaApiFalla_laExcepcionLlegaAlViewModel() = runTest {
        coEvery { api.getPosts(any()) } throws java.io.IOException("sin red")
        repo.obtenerEventos()
    }
}

