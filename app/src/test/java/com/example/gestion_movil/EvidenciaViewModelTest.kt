package com.example.gestion_movil

import android.app.Application
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.data.repository.EvidenciaRepository
import com.example.gestion_movil.ui.evidencias.EvidenciaViewModel
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class EvidenciaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<EvidenciaRepository>(relaxed = true)
    private val vm = EvidenciaViewModel(mockk<Application>(relaxed = true), repo)

    @Test
    fun registrar_guardaLaEvidenciaConLosDatosRecibidos() {
        vm.registrar(oportunidadId = 5, tipo = EvidenciaEntity.FOTO, ruta = "/ruta/foto.jpg", nombre = "Stand")

        coVerify(exactly = 1) {
            repo.agregar(
                match {
                    it.oportunidadId == 5 && it.tipo == EvidenciaEntity.FOTO &&
                        it.ruta == "/ruta/foto.jpg" && it.nombre == "Stand"
                }
            )
        }
    }

    @Test
    fun eliminar_delegaEnElRepositorio() {
        val e = EvidenciaEntity(oportunidadId = 5, tipo = EvidenciaEntity.VIDEO, ruta = "/ruta/video.mp4")

        vm.eliminar(e)

        coVerify(exactly = 1) { repo.eliminar(e) }
    }
}
