package com.example.gestion_movil.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gestion_movil.data.model.QrResult
import com.example.gestion_movil.data.repository.QrRepository

class QrViewModel : ViewModel() {
    private val repository = QrRepository()

    private val _qrResult = MutableLiveData<QrResult?>()
    val qrResult: LiveData<QrResult?> = _qrResult

    fun onQrDetected(content: String) {
        _qrResult.value = repository.processQrContent(content)
    }

    fun clearResult() {
        _qrResult.value = null
    }
}