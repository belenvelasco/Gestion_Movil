package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oportunidades")
data class OportunidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val tipo: String,
    val descripcion: String,
    val fuente: String,
    val prioridad: String,
    val potencial: String,
    val estado: String,
    val observaciones: String,
    val fechaDeteccion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)