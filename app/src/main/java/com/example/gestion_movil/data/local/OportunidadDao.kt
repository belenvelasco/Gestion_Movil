package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Query("SELECT * FROM oportunidades ORDER BY fechaActualizacion DESC")
    fun observarTodas(): Flow<List<OportunidadEntity>>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    fun observarPorId(id: Int): Flow<OportunidadEntity?>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun obtenerPorId(id: Int): OportunidadEntity?

    @Upsert
    suspend fun guardar(oportunidad: OportunidadEntity): Long

    @Delete
    suspend fun eliminar(oportunidad: OportunidadEntity)
}

