package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seguimientos",
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

data class SeguimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val opotunidadId: Int,
    val tipo: String,
    val nota: String,
    val proximaAccion: String = "",
    val fecha: Long= System.currentTimeMillis()
)
