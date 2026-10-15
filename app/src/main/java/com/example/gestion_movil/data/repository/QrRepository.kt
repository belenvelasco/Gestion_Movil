package com.example.gestion_movil.data.repository
//
import com.example.gestion_movil.data.model.QrResult

class QrRepository {
    fun processQrContent(content: String): QrResult{
        return QrResult(content)
    }
}