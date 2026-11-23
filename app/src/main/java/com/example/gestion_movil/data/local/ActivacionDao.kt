package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActivacionDao{
    @Query("SELECT * FROM activaciones WHERE oportunidadId = :oportunidadId LIMIT 1 ")
    suspend fun obtener(oportunidadId: Int): ActivacionEntity?

    //
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(activacion: ActivacionEntity): Long
}