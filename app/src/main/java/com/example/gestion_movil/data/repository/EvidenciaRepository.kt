package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.EvidenciaDao
import com.example.gestion_movil.data.local.EvidenciaEntity
import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.HistoriaEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

class EvidenciaRepository(
    private val evidenciaDao: EvidenciaDao,
    private val historiaDao: HistoriaDao
) {
    fun evidencias(oportunidadId: Int): Flow<List<EvidenciaEntity>> =
        evidenciaDao.observarPorOportunidad(oportunidadId)

    suspend fun agregar(evidencia: EvidenciaEntity): Long {
        val id = evidenciaDao.insertar(evidencia)
        val que = if (evidencia.tipo == EvidenciaEntity.VIDEO) "Video" else "Foto"
        historiaDao.insertar(
            HistoriaEntity(
                oportunidadId = evidencia.oportunidadId,
                descripcion = "$que agregado como evidencia"
            )
        )
        return id
    }

    suspend fun eliminar(evidencia: EvidenciaEntity) {
        evidenciaDao.eliminar(evidencia)
        File(evidencia.ruta).delete()
    }
}