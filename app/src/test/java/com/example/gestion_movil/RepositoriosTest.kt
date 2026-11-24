package com.example.gestion_movil

import com.example.gestion_movil.data.local.ActivacionDao
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.ContactoDao
import com.example.gestion_movil.data.local.ContactoEntity
import com.example.gestion_movil.data.local.EvidenciaDao
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.OportunidadDao
import com.example.gestion_movil.data.local.OrganizacionDao
import com.example.gestion_movil.data.local.OrganizacionEntity
import com.example.gestion_movil.data.local.ResultadoDao
import com.example.gestion_movil.data.local.ResultadoEntity
import com.example.gestion_movil.data.local.SeguimientoDao
import com.example.gestion_movil.data.local.SeguimientoEntity
import com.example.gestion_movil.data.repository.ActivacionRepository
import com.example.gestion_movil.data.repository.EvidenciaRepository
import com.example.gestion_movil.data.repository.GestionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Repositorios con DAOs simulados: se prueba la lógica (historial, responsable, organización). */
class OportunidadRepositoryTest {

    private val dao = mockk<OportunidadDao>(relaxed = true)
    private val organizacionDao = mockk<OrganizacionDao>(relaxed = true)
    private val historiaDao = mockk<HistoriaDao>(relaxed = true)
    private val repo = OportunidadRepository(dao, organizacionDao, historiaDao)

    @Test
    fun guardarNueva_devuelveElIdYRegistraElHistorial() = runTest {
        coEvery { dao.guardar(any()) } returns 10L

        val id = repo.guardar(oportunidadDemo(id = 0, estado = "Evento identificado"))

        assertEquals(10L, id)
        coVerify(exactly = 1) {
            historiaDao.insertar(
                match { it.oportunidadId == 10 && it.descripcion.contains("Oportunidad registrada") }
            )
        }
    }

    @Test
    fun editar_registraLosCambiosDeEstadoYPrioridad() = runTest {
        coEvery { dao.obtenerPorId(2) } returns oportunidadDemo(2, estado = "Evento identificado", prioridad = "Baja")
        coEvery { dao.guardar(any()) } returns 2L

        repo.guardar(oportunidadDemo(2, estado = "Contacto pendiente", prioridad = "Alta"))

        coVerify(exactly = 1) {
            historiaDao.insertar(match { it.descripcion == "Estado: Evento identificado -> Contacto pendiente" })
        }
        coVerify(exactly = 1) {
            historiaDao.insertar(match { it.descripcion == "Prioridad: Baja -> Alta" })
        }
    }

    @Test
    fun editarSinCambios_noRegistraHistorial() = runTest {
        val igual = oportunidadDemo(2)
        coEvery { dao.obtenerPorId(2) } returns igual
        coEvery { dao.guardar(any()) } returns 2L

        repo.guardar(igual)

        coVerify(exactly = 0) { historiaDao.insertar(any()) }
    }

    @Test
    fun guardar_conservaElResponsableQueYaTenia() = runTest {
        coEvery { dao.obtenerPorId(2) } returns oportunidadDemo(2, responsable = "Maria rojas")
        coEvery { dao.guardar(any()) } returns 2L
        val guardada = slot<com.example.gestion_movil.data.local.OportunidadEntity>()

        repo.guardar(oportunidadDemo(2, responsable = ""))

        coVerify { dao.guardar(capture(guardada)) }
        assertEquals("Maria rojas", guardada.captured.responsable)
    }

    @Test
    fun guardar_siRoomDevuelveMenosUno_usaElIdOriginal() = runTest {
        coEvery { dao.obtenerPorId(4) } returns oportunidadDemo(4)
        coEvery { dao.guardar(any()) } returns -1L

        assertEquals(4L, repo.guardar(oportunidadDemo(4)))
    }

    @Test
    fun reasignarResponsable_actualizaYRegistraElCambio() = runTest {
        coEvery { dao.obtenerPorId(3) } returns oportunidadDemo(3, responsable = "Maria rojas")

        repo.reasignarResponsable(3, "Camilo ramos")

        coVerify(exactly = 1) { dao.actualizarResponsable(3, "Camilo ramos", any()) }
        coVerify(exactly = 1) {
            historiaDao.insertar(match { it.descripcion == "Responsable: Maria rojas -> Camilo ramos" })
        }
    }

    @Test
    fun reasignarResponsable_sinResponsableAnterior_muestraSinAsignar() = runTest {
        coEvery { dao.obtenerPorId(3) } returns oportunidadDemo(3, responsable = "")

        repo.reasignarResponsable(3, "Valentina peña")

        coVerify { historiaDao.insertar(match { it.descripcion == "Responsable: Sin asignar -> Valentina peña" }) }
    }

    @Test
    fun reasignarResponsable_siEsElMismo_noHaceNada() = runTest {
        coEvery { dao.obtenerPorId(3) } returns oportunidadDemo(3, responsable = "Maria rojas")

        repo.reasignarResponsable(3, "Maria rojas")

        coVerify(exactly = 0) { dao.actualizarResponsable(any(), any(), any()) }
        coVerify(exactly = 0) { historiaDao.insertar(any()) }
    }

    @Test
    fun reasignarResponsable_siLaOportunidadNoExiste_noHaceNada() = runTest {
        coEvery { dao.obtenerPorId(99) } returns null

        repo.reasignarResponsable(99, "Camilo ramos")

        coVerify(exactly = 0) { dao.actualizarResponsable(any(), any(), any()) }
    }

    @Test
    fun obtenerOCrearOrganizacion_siExiste_devuelveSuId() = runTest {
        coEvery { organizacionDao.buscarPorNombre("Org Demo") } returns OrganizacionEntity(5, "Org Demo")

        assertEquals(5, repo.obtenerOCrearOrganizacion("  Org Demo  "))
        coVerify(exactly = 0) { organizacionDao.insertar(any()) }
    }

    @Test
    fun obtenerOCrearOrganizacion_siNoExiste_laCrea() = runTest {
        coEvery { organizacionDao.buscarPorNombre("Nueva") } returns null
        coEvery { organizacionDao.insertar(any()) } returns 9L

        assertEquals(9, repo.obtenerOCrearOrganizacion("Nueva"))
        coVerify(exactly = 1) { organizacionDao.insertar(match { it.nombre == "Nueva" }) }
    }

    @Test
    fun eliminarYObtener_delegaEnElDao() = runTest {
        val o = oportunidadDemo(1)
        coEvery { dao.obtenerPorId(1) } returns o

        repo.eliminar(o)

        coVerify(exactly = 1) { dao.eliminar(o) }
        assertEquals(o, repo.obtenerPorId(1))
    }
}

class ActivacionRepositoryTest {

    private val activacionDao = mockk<ActivacionDao>(relaxed = true)
    private val resultadoDao = mockk<ResultadoDao>(relaxed = true)
    private val historiaDao = mockk<HistoriaDao>(relaxed = true)
    private val repo = ActivacionRepository(activacionDao, resultadoDao, historiaDao)

    @Test
    fun guardarActivacion_guardaYDejaRegistroEnElHistorial() = runTest {
        val a = ActivacionEntity(oportunidadId = 1, responsable = "Maria rojas")

        repo.guardarActivacion(a)

        coVerify(exactly = 1) { activacionDao.guardar(a) }
        coVerify(exactly = 1) {
            historiaDao.insertar(match { it.descripcion.contains("Maria rojas") && it.oportunidadId == 1 })
        }
    }

    @Test
    fun guardarResultado_sinCerrar_registraSoloElResultado() = runTest {
        val r = ResultadoEntity(oportunidadId = 1, resultado = "Activación realizada", impacto = "Alto", cerrada = false)

        repo.guardarResultado(r)

        coVerify(exactly = 1) { resultadoDao.guardar(r) }
        coVerify(exactly = 1) { historiaDao.insertar(any()) }
    }

    @Test
    fun guardarResultado_cerrando_agregaElRegistroDeCierre() = runTest {
        val r = ResultadoEntity(oportunidadId = 1, resultado = "Activación realizada", impacto = "Alto", cerrada = true)

        repo.guardarResultado(r)

        coVerify(exactly = 2) { historiaDao.insertar(any()) }
        coVerify(exactly = 1) { historiaDao.insertar(match { it.descripcion == "Oportunidad cerrada" }) }
    }

    @Test
    fun obtener_delegaEnLosDaos() = runTest {
        val a = ActivacionEntity(oportunidadId = 1)
        val r = ResultadoEntity(oportunidadId = 1, resultado = "x")
        coEvery { activacionDao.obtener(1) } returns a
        coEvery { resultadoDao.obtener(1) } returns r

        assertEquals(a, repo.obtenerActivacion(1))
        assertEquals(r, repo.obtenerResultado(1))
    }
}

class GestionRepositoryTest {

    private val contactoDao = mockk<ContactoDao>(relaxed = true)
    private val seguimientoDao = mockk<SeguimientoDao>(relaxed = true)
    private val repo = GestionRepository(contactoDao, seguimientoDao)

    @Test
    fun agregarYEliminarContacto_delegaEnElDao() = runTest {
        val c = ContactoEntity(oportunidadId = 1, nombre = "Ana", cargo = "Jefa")
        coEvery { contactoDao.insertar(c) } returns 4L

        assertEquals(4L, repo.agregarContacto(c))
        repo.eliminarContacto(c)

        coVerify(exactly = 1) { contactoDao.eliminar(c) }
    }

    @Test
    fun agregarSeguimiento_devuelveElIdGenerado() = runTest {
        val s = SeguimientoEntity(oportunidadId = 1, tipo = "Reunión", nota = "ok")
        coEvery { seguimientoDao.insertar(s) } returns 8L

        assertEquals(8L, repo.agregarSeguimiento(s))
    }
}

class EvidenciaRepositoryTest {

    private val evidenciaDao = mockk<EvidenciaDao>(relaxed = true)
    private val historiaDao = mockk<HistoriaDao>(relaxed = true)
    private val repo = EvidenciaRepository(evidenciaDao, historiaDao)

    private fun evidencia(tipo: String) =
        EvidenciaEntity(oportunidadId = 1, tipo = tipo, ruta = "/tmp/archivo-inexistente")

    @Test
    fun agregar_registraElTipoDeEvidenciaEnElHistorial() = runTest {
        coEvery { evidenciaDao.insertar(any()) } returns 3L

        assertEquals(3L, repo.agregar(evidencia(EvidenciaEntity.FOTO)))
        repo.agregar(evidencia(EvidenciaEntity.VIDEO))
        repo.agregar(evidencia(EvidenciaEntity.DOCUMENTO))

        coVerify { historiaDao.insertar(match { it.descripcion == "Foto agregado como evidencia" }) }
        coVerify { historiaDao.insertar(match { it.descripcion == "video agregado como evidencia" }) }
        coVerify { historiaDao.insertar(match { it.descripcion == "Documento agregado como evidencia" }) }
    }

    @Test
    fun eliminar_borraElRegistroDeLaBase() = runTest {
        val e = evidencia(EvidenciaEntity.FOTO)

        repo.eliminar(e)

        coVerify(exactly = 1) { evidenciaDao.eliminar(e) }
    }
}
