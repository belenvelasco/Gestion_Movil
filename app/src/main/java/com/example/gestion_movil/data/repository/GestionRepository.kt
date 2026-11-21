package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.ContactoDao
import com.example.gestion_movil.data.local.ContactoEntity
import com.example.gestion_movil.data.local.SeguimientoDao
import com.example.gestion_movil.data.local.SeguimientoEntity
import kotlinx.coroutines.flow.Flow


//
class GestionRepository(
    private val contactoDao: ContactoDao,
    private val seguimientoDao: SeguimientoDao
) {

    fun contactos(oportunidadId: Int): Flow<List<ContactoEntity>> =
        contactoDao.observarPorOportunidad(oportunidadId)

    fun seguimientos(oportunidadId: Int): Flow<List<SeguimientoEntity>> =
        seguimientoDao.observarPorOportunidad(oportunidadId)

    suspend fun agregarContacto(contacto: ContactoEntity): Long = contactoDao.insertar(contacto)

    suspend fun eliminarContacto(contacto: ContactoEntity) = contactoDao.eliminar(contacto)

    suspend fun agregarSeguimiento(seguimiento: SeguimientoEntity): Long =
        seguimientoDao.insertar(seguimiento)
}