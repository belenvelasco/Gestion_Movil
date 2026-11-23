package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResultadoDao{
    @Query("SELECT * FROM resultado WHERE oportunidadId = :oportunidadId LINIT 1")
    suspend fun obtener(oportunidadId: Int): ResultadoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(resultado: ResultadoEntity): Long
}