package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Query("SELECT * FROM evidencias WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC, id DESC")
    fun observarPorOportunidad(oportunidadId: Int): Flow<List<EvidenciaEntity>>

    @Insert
    suspend fun insertar(evidencia: EvidenciaEntity): Long

    @Delete
    suspend fun eliminar(evidencia: EvidenciaEntity)
}
