package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activaciones",
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

data class ActivacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oportunidadId: Int,
    val espacio: String = "",
    val fechaActivacion: Long? = null,
    val costo: Int? = null,
    val requisitos: String = "",
    val responsable: String = ""
)