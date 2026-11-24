package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.OportunidadDao
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.OrganizacionDao
import com.example.gestion_movil.data.local.OrganizacionEntity
import com.example.gestion_movil.data.model.Responsable
import kotlinx.coroutines.flow.Flow

//
class OportunidadRepository(
    private val dao: OportunidadDao,
    private val organizacionDao: OrganizacionDao,
    private val historiaDao: HistoriaDao
) {

    val oportunidades: Flow<List<OportunidadEntity>> = dao.observarTodas()

    val organizaciones: Flow<List<OrganizacionEntity>> = organizacionDao.observarTodas()

    fun historial (oportunidadId: Int): Flow<List<HistoriaEntity >> =
        historiaDao .observarPorOportunidad(oportunidadId)

    fun observarPorId(id: Int): Flow<OportunidadEntity?> = dao.observarPorId(id)

    suspend fun obtenerPorId(id: Int): OportunidadEntity? = dao.obtenerPorId(id)

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
        val anterior = if (oportunidad.id != 0) dao.obtenerPorId(oportunidad.id) else null
        val aGuardar = oportunidad.copy(
            responsable = anterior?.responsable ?: oportunidad.responsable,
            fechaActualizacion = ahora
        )
        val resultado = dao.guardar(aGuardar)
        val id = if (resultado == -1L) oportunidad.id.toLong() else resultado
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
                cambios += "Prioridad: ${anterior.prioridad} -> ${nuevo.prioridad}"
            }
            if (anterior.potencial != nuevo.potencial) {
                cambios += "Potencial: ${anterior.potencial} -> ${nuevo.potencial}"
            }
        }
        cambios.forEach { descripcion ->
            historiaDao.insertar(
                HistoriaEntity(oportunidadId = id, descripcion = descripcion, fecha = fecha)
            )
        }
    }

    suspend fun reasignarResponsable(id: Int, nuevo: String) {
        val anterior = dao.obtenerPorId(id) ?: return
        if (anterior.responsable == nuevo) return
        val ahora = System.currentTimeMillis()
        dao.actualizarResponsable(id, nuevo, ahora)
        historiaDao.insertar(
            HistoriaEntity(
                oportunidadId = id,
                descripcion = "Responsable: ${Responsable.mostrar(anterior.responsable)} -> ${Responsable.mostrar(nuevo)}",
                fecha = ahora
            )
        )
    }
}