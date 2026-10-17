package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.OportunidadDao
import com.example.gestion_movil.data.local.OportunidadEntity
import kotlinx.coroutines.flow.Flow

class OportunidadRepository(private val dao: OportunidadDao) {

    val oportunidades: Flow<List<OportunidadEntity>> dao.observarTodas()


    suspend fun obtenerPorId(id: Int): OportunidadEntity? = dao.optenerPorId(id)

    suspend fun guardar(oportunidad: OportunidadEntity): Long =
        dao.guardar(oportunidad.copy(FechaActualizacion = System.currentTimeMillis()))

    suspend fun eliminar(oportunidad: OportunidadEntity) =  dao.eliminar(oportunidad)
}