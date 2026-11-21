package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao{
    @Query("SELECT * FROM contactos WHERE oportunidadId = :oportunidadId ORDER BY fechaRegistro DESC")
    fun observarPorOportunidad(oportunidadId: Int): Flow<List<ContactoEntity>>

    @Insert
    suspend fun insertar(contacto: ContactoEntity): Long

    @Delete
    suspend fun eliminar(contacto: ContactoEntity)
}