package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.OportunidadDao
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.OrganizacionDao
import com.example.gestion_movil.data.local.OrganizacionEntity
import kotlinx.coroutines.flow.Flow

//
class OportunidadRepository(
    private val dao: OportunidadDao,
    private val organizacionDao: OrganizacionDao,
    private val historiaDao: HistoriaDao
) {

    val oportunidades: Flow<List<OportunidadEntity>> = dao.observarTodas()

    val organizaciones: Flow<List<OrganizacionEntity>> = organizacionDao.observarTodas()

    val observarPorId(id: Int): Flow<OportunidadesEntity?> = dao.observarPorID(id)

    fun historial (oportunidadId: Int): Flow<List<HistoriaEntity >> =
        historiaDao .observarPorOportunidad(oportunidadId)

    suspend fun observarPorId(id: Int): OportunidadEntity ?= dao.observarPorId(id)

    suspend fun obtenerOrganizacion(id: Int): OrganizacionEntity? = organizacionDao.obtenerPorId(id)

    //
    suspend fun obtenerOCrearOrganizacion(nombre: String): Int{
        val limpio = nombre.trim()
        val existente = organizacionDao.buscarPorNombre(limpio)
        return existente?.id ?:organizacionDao.insertar(OrganizacionEntity(nombre = limpio)).toInt()

    }


    //
    suspend fun guardar(oportunidad: OportunidadEntity): Long {
        val ahora = System.currentTimeMillis()
        val anterior = if (oportunidad.id !=0) dao.observarPorId(oportunidad.id) else null

        val resultado = dao.guardar(oportunidad.copy(fechaActualizacion = ahora))
        //
        val id = if(resultado == -1L) oportunidad.id.toLong() else resultado

        registrarCambios(id.toInt(), anterior, oportunidad, ahora)
        return id
    }

    suspend fun eliminar(oportunidad: OportunidadEntity) = dao.eliminar(oportunidad)

    private suspend fun registrarCambios(
        id: Int,
        anterior: OportunidadEntity?,
        nuevo: OportunidadEntity,
        fecha: Long
    ){
        val cambios = mutableListOf<String>()
        if (anterior == null){
            cambios +="Oportunidad registrada con estado \"${nuevo.estado}\""
        } else {
            if (anterior.estado !=nuevo.estado){
                cambios += "Estado: ${anterior.estado} -> ${nuevo.estado}"
            }
            if(anterior.prioridad != nuevo.prioridad){
                cambios += "prioridad: ${anterior.prioridad} -> ${nuevo.prioridad}"
            }
        }
        cambios.forEach { descripcion ->
            historiaDao.insertar(
                HistoriaEntity(oportunidad = id, descripcion = descripcion, fecha = fecha)
            )
        }
    }
}