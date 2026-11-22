package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val oportunidadId: Int,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis()
)

