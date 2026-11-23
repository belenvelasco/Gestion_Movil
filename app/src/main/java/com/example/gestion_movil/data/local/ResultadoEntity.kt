package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resultados",
    foreignKeys = [
        ForeignKey(
            entity = OportunidadEntity::class,
            parentColumns = ["id"],
            childColumns = ["oportunidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["oportunidadId"], unique = true)]
)

data class ResultadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oportunidadId: Int,
    val resultado: String,
    val observaciones: String = "",
    val impacto: String = "",
    val cerrada: Boolean = false,
    val fecha: Long = System.currentTimeMillis()
)