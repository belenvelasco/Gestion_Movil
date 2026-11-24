package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.oportunidad.OportunidadViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadViewModel: StringSpec({

    // viewModelScope usa Dispatchers.Main, que no existe en un test JVM
    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    fun op(id: Int, nombre: String, prioridad: String, responsable: String = "") =
        OportunidadEntity(
            id = id, nombre = nombre, tipo = "Feria", descripcion = "", fuente = "Web",
            prioridad = prioridad, potencial = "Media", estado = "Evento identificado",
            observaciones = "", responsable = responsable
        )

    fun crearViewModel(): OportunidadViewModel {
        val repo = mockk<OportunidadRepository>()
        every { repo.oportunidades } returns flowOf(
            listOf(
                op(1, "Feria Verde", "Alta", "Maria rojas"),
                op(2, "Expo Pyme", "Baja"),
                op(3, "Jornada Municipal", "Alta")
            )
        )
        every { repo.organizaciones } returns flowOf(emptyList())
        return OportunidadViewModel(mockk<Application>(relaxed = true), repo)
    }

    "filtra por prioridad" {
        runTest {
            val vm = crearViewModel()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaFiltrada.collect {} }
            vm.onFiltroPrioridad("Alta")
            vm.listaFiltrada.value.map { it.id } shouldBe listOf(1, 3)
            job.cancel()
        }
    }

    "busca por nombre" {
        runTest {
            val vm = crearViewModel()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaFiltrada.collect {} }
            vm.onBusqueda("expo")
            vm.listaFiltrada.value.map { it.id } shouldBe listOf(2)
            job.cancel()
        }
    }

    "busca por responsable" {
        runTest {
            val vm = crearViewModel()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.listaFiltrada.collect {} }
            vm.onBusqueda("maria")
            vm.listaFiltrada.value.map { it.id } shouldBe listOf(1)
            job.cancel()
        }
    }
})
