package com.example.gestion_movil


import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.OportunidadDao
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.OrganizacionDao
import com.example.gestion_movil.data.repository.OportunidadRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class OportunidadRepository: StringSpec({

    val base = OportunidadEntity(
        id = 1, nombre = "Feria Verde", tipo = "Feria", descripcion = "", fuente = "Web",
        prioridad = "Media", potencial = "Media", estado = "Evento identificado",
        observaciones = "", responsable = ""
    )

    fun crear(dao: OportunidadDao, historia: HistoriaDao): OportunidadRepository {
        val orgDao = mockk<OrganizacionDao>()
        every { dao.observarTodas() } returns flowOf(emptyList())
        every { orgDao.observarTodas() } returns flowOf(emptyList())
        return OportunidadRepository(dao, orgDao, historia)
    }

    "cambiar la prioridad deja registro en el historial" {
        runTest {
            val dao = mockk<OportunidadDao>()
            val historia = mockk<HistoriaDao>()
            val registros = mutableListOf<HistoriaEntity>()
            coEvery { dao.obtenerPorId(1) } returns base
            coEvery { dao.guardar(any()) } returns -1L
            coEvery { historia.insertar(capture(registros)) } returns 1L

            crear(dao, historia).guardar(base.copy(prioridad = "Alta"))

            registros.map { it.descripcion } shouldContain "Prioridad: Media -> Alta"
        }
    }

    "si no cambia nada no se escribe en el historial" {
        runTest {
            val dao = mockk<OportunidadDao>()
            val historia = mockk<HistoriaDao>(relaxed = true)
            coEvery { dao.obtenerPorId(1) } returns base
            coEvery { dao.guardar(any()) } returns -1L

            crear(dao, historia).guardar(base)

            coVerify(exactly = 0) { historia.insertar(any()) }
        }
    }

    "una oportunidad nueva se registra con el id que devuelve la base" {
        runTest {
            val dao = mockk<OportunidadDao>()
            val historia = mockk<HistoriaDao>()
            val registro = slot<HistoriaEntity>()
            coEvery { dao.guardar(any()) } returns 7L
            coEvery { historia.insertar(capture(registro)) } returns 1L

            val id = crear(dao, historia).guardar(base.copy(id = 0))

            id shouldBe 7L
            registro.captured.oportunidadId shouldBe 7
        }
    }

    "reasignar el responsable actualiza la oportunidad y deja historial" {
        runTest {
            val dao = mockk<OportunidadDao>()
            val historia = mockk<HistoriaDao>()
            val registro = slot<HistoriaEntity>()
            coEvery { dao.obtenerPorId(1) } returns base
            coEvery { dao.actualizarResponsable(any(), any(), any()) } returns Unit
            coEvery { historia.insertar(capture(registro)) } returns 1L

            crear(dao, historia).reasignarResponsable(1, "Camilo ramos")

            coVerify { dao.actualizarResponsable(1, "Camilo ramos", any()) }
            registro.captured.descripcion shouldBe "Responsable: Sin asignar -> Camilo ramos"
        }
    }

    "reasignar al mismo responsable no hace nada" {
        runTest {
            val dao = mockk<OportunidadDao>()
            val historia = mockk<HistoriaDao>(relaxed = true)
            coEvery { dao.obtenerPorId(1) } returns base.copy(responsable = "Maria rojas")

            crear(dao, historia).reasignarResponsable(1, "Maria rojas")

            coVerify(exactly = 0) { dao.actualizarResponsable(any(), any(), any()) }
            coVerify(exactly = 0) { historia.insertar(any()) }
        }
    }
})
