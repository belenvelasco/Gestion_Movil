package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.ResultadoEntity
import com.example.gestion_movil.data.model.ResumenPanel
import com.example.gestion_movil.data.repository.ActivacionRepository
import com.example.gestion_movil.data.repository.OportunidadRepository
import com.example.gestion_movil.ui.activacion.ActivacionViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
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
class ActivacionViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    fun oportunidad(estado: String = "Contacto realizado", responsable: String = "") =
        OportunidadEntity(
            id = 1, nombre = "Feria Verde", tipo = "Feria", descripcion = "", fuente = "Web",
            prioridad = "Alta", potencial = "Media", estado = estado,
            observaciones = "", responsable = responsable
        )

    class Entorno(
        val vm: ActivacionViewModel,
        val oportunidades: OportunidadRepository,
        val activaciones: ActivacionRepository
    )

    fun crear(op: OportunidadEntity): Entorno {
        val oportunidades = mockk<OportunidadRepository>(relaxed = true)
        val activaciones = mockk<ActivacionRepository>(relaxed = true)
        every { oportunidades.observarPorId(1) } returns flowOf(op)
        coEvery { oportunidades.obtenerPorId(1) } returns op
        coEvery { activaciones.obtenerActivacion(1) } returns null
        coEvery { activaciones.obtenerResultado(1) } returns null
        val vm = ActivacionViewModel(mockk<Application>(relaxed = true), oportunidades, activaciones)
        return Entorno(vm, oportunidades, activaciones)
    }

    "no guarda las condiciones si faltan campos obligatorios" {
        runTest {
            val e = crear(oportunidad())
            e.vm.cargar(1)

            e.vm.guardarCondiciones()

            val s = e.vm.uiState.value
            s.espacioError.shouldNotBeNull()
            s.fechaError.shouldNotBeNull()
            s.responsableError.shouldNotBeNull()
            coVerify(exactly = 0) { e.activaciones.guardarActivacion(any()) }
        }
    }

    "el costo debe ser un numero entero" {
        runTest {
            val e = crear(oportunidad())
            e.vm.cargar(1)
            e.vm.onEspacio("Stand central")
            e.vm.onFecha(1_700_000_000_000)
            e.vm.onResponsable("Maria rojas")
            e.vm.onCosto("abc")

            e.vm.guardarCondiciones()

            e.vm.uiState.value.costoError.shouldNotBeNull()
            coVerify(exactly = 0) { e.activaciones.guardarActivacion(any()) }
        }
    }

    "guarda las condiciones y deja el mismo responsable en la oportunidad" {
        runTest {
            val e = crear(oportunidad())
            val guardada = slot<ActivacionEntity>()
            coEvery { e.activaciones.guardarActivacion(capture(guardada)) } returns Unit
            e.vm.cargar(1)
            e.vm.onEspacio("  Stand central ")
            e.vm.onFecha(1_700_000_000_000)
            e.vm.onCosto("150000")
            e.vm.onResponsable("Camilo ramos")

            e.vm.guardarCondiciones()

            guardada.captured.espacio shouldBe "Stand central"
            guardada.captured.costo shouldBe 150000
            guardada.captured.responsable shouldBe "Camilo ramos"
            coVerify { e.oportunidades.reasignarResponsable(1, "Camilo ramos") }
            e.vm.uiState.value.mensaje shouldBe "Condiciones y responsable guardados"
        }
    }

    "al cargar manda el responsable de la oportunidad" {
        runTest {
            val e = crear(oportunidad(responsable = "Valentina peña"))
            e.vm.cargar(1)
            e.vm.uiState.value.responsable shouldBe "Valentina peña"
        }
    }

    "no permite registrar resultados sin participacion confirmada" {
        runTest {
            val e = crear(oportunidad(estado = "Contacto realizado"))
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { e.vm.oportunidad.collect {} }
            e.vm.cargar(1)
            e.vm.onResultado("Activación realizada")
            e.vm.onImpacto("Alto")

            e.vm.guardarResultado(cerrar = false)

            e.vm.uiState.value.mensaje shouldBe "Confirma la participación antes de registrar resultados"
            coVerify(exactly = 0) { e.activaciones.guardarResultado(any()) }
            job.cancel()
        }
    }

    "cerrar la oportunidad deja la activacion cerrada" {
        runTest {
            val e = crear(oportunidad(estado = ResumenPanel.ESTADO_CONFIRMADA))
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { e.vm.oportunidad.collect {} }
            val guardado = slot<ResultadoEntity>()
            coEvery { e.activaciones.guardarResultado(capture(guardado)) } returns Unit
            e.vm.cargar(1)
            e.vm.onResultado("Activación realizada")
            e.vm.onImpacto("Alto")

            e.vm.guardarResultado(cerrar = true)

            guardado.captured.cerrada shouldBe true
            e.vm.uiState.value.cerrada shouldBe true
            job.cancel()
        }
    }
})