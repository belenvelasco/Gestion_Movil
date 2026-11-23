package com.example.gestion_movil.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencias",
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
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oportunidadId: Int,
    // FOTO o VIDEO
    val tipo: String,
    val ruta: String,
    @ColumnInfo(defaultValue = "''")
    val nombre: String ="",
    val fecha: Long = System.currentTimeMillis()
) {
    companion object {
        const val FOTO = "FOTO"
        const val VIDEO = "VIDEO"

        const val DOCUMENTO = "DOCUMENTO"
    }
}