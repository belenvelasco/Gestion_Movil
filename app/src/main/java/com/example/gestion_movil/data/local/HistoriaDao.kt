package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao

interface HistoriaDao {
    @Query("SELECT * FROM histria_cambios WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC, id DESC")
    fun observarPorOportunidad(oportunidadId: Int): Flow<List<HistoriaEntity>>

    @Insert
    suspend fun insertar(registro: HistoriaEntity): Long
}