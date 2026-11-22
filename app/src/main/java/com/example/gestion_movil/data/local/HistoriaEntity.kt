package com.example.gestion_movil.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.descriptors.SerialDescriptor

@Entity(
    tableName = "historia_cambios",
    foreignKeys = [
        ForeignKey(
            entity = OportunidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("oportunidadId")]
)

data class HistoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oportunidad: Int,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis()
)

