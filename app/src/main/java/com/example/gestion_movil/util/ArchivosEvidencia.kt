package com.example.gestion_movil.util

import android.content.Context
import com.example.gestion_movil.data.local.EvidenciaEntity
import java.io.File

//crea una ruta para el archivo tenga que guardar la fotos y videos
object ArchivosEvidencia {

    fun crear(context: Context, oportunidadInt: Int, tipo: String): File {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val esVideo = tipo == EvidenciaEntity.VIDEO
        val nombre = "${if (esVideo) "video" else " foto"}_${oportunidadInt}_" +
                "${System.currentTimeMillis()}.${if (esVideo) "mp4" else "jpg"}"
        return File(carpeta,nombre)
    }
}