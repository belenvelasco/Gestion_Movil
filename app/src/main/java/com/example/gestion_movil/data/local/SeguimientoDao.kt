package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SeguimientoDao {

    @Query("SELECT * FROM seguimientos WHERE oportunidadId = :oportunidadId ORDER BY fecha DESC, id DESC")
    fun observarPorOportunidad(oportunidadId: Int): Flow<List<SeguimientoEntity>>

    @Query("SELECT * FROM seguimientos ORDER BY fecha DESC, id DESC")
    fun observarTodos(): Flow<List<SeguimientoEntity>>

    @Insert
    suspend fun insertar(seguimiento: SeguimientoEntity): Long
}
