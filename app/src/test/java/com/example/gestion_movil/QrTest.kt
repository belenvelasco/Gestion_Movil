package com.example.gestion_movil

import com.example.gestion_movil.data.model.QrResult
import com.example.gestion_movil.data.repository.QrRepository
import com.example.gestion_movil.ui.qr.QrViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrTest {

    @Test
    fun repositorio_recortaLosEspaciosDelContenido() {
        val resultado = QrRepository().processQrContent("  12  ")
        assertEquals(QrResult("12"), resultado)
    }

    @Test
    fun viewModel_guardaElResultadoAlDetectarUnQr() {
        val vm = QrViewModel()
        assertNull(vm.qrResult.value)
        vm.onQrDetected(" Feria Demo ")
        assertEquals(QrResult("Feria Demo"), vm.qrResult.value)
    }

    @Test
    fun viewModel_clearResultVuelveAlEstadoInicial() {
        val vm = QrViewModel()
        vm.onQrDetected("5")
        vm.clearResult()
        assertNull(vm.qrResult.value)
    }
}
