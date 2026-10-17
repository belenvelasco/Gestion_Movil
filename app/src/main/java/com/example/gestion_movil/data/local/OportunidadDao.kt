package com.example.gestion_movil.data.local

import kotlinx.coroutines.flow.Flow

@Dao
interface OportunidadDao {
    @Query(" SELECT * FROM oportunidades ORDER BY fachaActualizacion DESC")
    fun observarTodas(): Flow<List<OportunidadEntity>>

    @Query("SELECT * FROM oportunidades WHERE id = :id")
    suspend fun optenerPorId(id: Int): OportunidadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(oportunidad: OportunidadEntity): Long

    @Delete
    suspend fun eliminar(oportunidad: OportunidadEntity)

}