package com.example.gestion_movil


import android.app.Application
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.repository.GestionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.DetalleOportunidadViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleSupervisorViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    val base = OportunidadEntity(
        id = 1, nombre = "Feria Verde", tipo = "Feria", descripcion = "", fuente = "Web",
        prioridad = "Media", potencial = "Media", estado = "Evento identificado",
        observaciones = "", responsable = "Maria rojas"
    )

    fun crear(): Pair<DetalleOportunidadViewModel, OportunidadRepository> {
        val repo = mockk<OportunidadRepository>(relaxed = true)
        every { repo.observarPorId(1) } returns flowOf(base)
        val vm = DetalleOportunidadViewModel(
            mockk<Application>(relaxed = true), repo, mockk<GestionRepository>(relaxed = true)
        )
        return vm to repo
    }

    "ajustar la prioridad guarda la oportunidad con la nueva prioridad" {
        runTest {
            val (vm, repo) = crear()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.oportunidad.collect {} }
            val guardada = slot<OportunidadEntity>()
            coEvery { repo.guardar(capture(guardada)) } returns 1L
            vm.cargar(1)

            vm.ajustarPrioridad("Alta")

            guardada.captured.prioridad shouldBe "Alta"
            job.cancel()
        }
    }

    "ajustar a la misma prioridad no guarda nada" {
        runTest {
            val (vm, repo) = crear()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.oportunidad.collect {} }
            vm.cargar(1)

            vm.ajustarPrioridad("Media")

            coVerify(exactly = 0) { repo.guardar(any()) }
            job.cancel()
        }
    }

    "reasignar el responsable llama al repositorio" {
        runTest {
            val (vm, repo) = crear()
            vm.cargar(1)

            vm.reasignarResponsable("Camilo ramos")

            coVerify { repo.reasignarResponsable(1, "Camilo ramos") }
        }
    }

    "elegir Sin asignar quita al responsable" {
        runTest {
            val (vm, repo) = crear()
            vm.cargar(1)

            vm.reasignarResponsable("Sin asignar")

            coVerify { repo.reasignarResponsable(1, "") }
        }
    }
})
