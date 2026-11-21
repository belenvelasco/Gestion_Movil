package com.example.gestion_movil.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizaciones")

data class OrganizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String

)