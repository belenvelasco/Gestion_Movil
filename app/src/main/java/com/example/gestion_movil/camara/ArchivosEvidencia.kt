package com.example.gestion_movil.camara

import android.content.Context
import com.example.gestion_movil.data.local.EvidenciaEntity
import java.io.File

object ArchivosEvidencia {
    fun crear(context: Context, oportunidadId: Int, tipo: String): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        val carpeta = File(base, "evidencias/$oportunidadId").apply { mkdirs() }
        val extension = if (tipo == EvidenciaEntity.VIDEO) "mp4" else "jpg"
        return File(carpeta, "${tipo.lowercase()}_${System.currentTimeMillis()}.$extension")
    }
}