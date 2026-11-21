package com.example.gestion_movil.data.local

import android.provider.ContactsContract
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizacionDao{

    @Query("SELECT * FROM Organizaciones ORDER BY nombre")
    fun observarTodas(): Flow<List<OrganizacionEntity>>

    @Query("SELECT * FROM organizaciones WHERE id= :id")
    suspend fun obtenerPorId(id: Int): OrganizacionEntity?

    @Query("SELECT * FROM organizaciones WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): OrganizacionEntity?

    @Insert
    suspend fun insertar(organizacion: OrganizacionEntity): Long
}