package com.example.gestion_movil.data.repository

import com.example.gestion_movil.data.local.ActivacionDao
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.HistoriaDao
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.ResultadoDao
import com.example.gestion_movil.data.local.ResultadoEntity

class ActivacionRepository(
    private val activacionDao: ActivacionDao,
    private val resultadoDao: ResultadoDao,
    private val historiaDao: HistoriaDao
) {
    suspend fun obtenerActivacion(oportunidadId: Int): ActivacionEntity? =
        activacionDao.obtener(oportunidadId)

    suspend fun obtenerResultado(oportunidadId: Int): ResultadoEntity? =
        resultadoDao.obtener(oportunidadId)

    suspend fun guardarActivacion(activacion: ActivacionEntity) {
        activacionDao.guardar(activacion)
        historiaDao.insertar(
            HistoriaEntity(
                oportunidadId = activacion.oportunidadId,
                descripcion = "Condiciones de la activación actualizadas " +
                        "(responsable: ${activacion.responsable})"
            )
        )
    }

    suspend fun guardarResultado(resultado: ResultadoEntity) {
        resultadoDao.guardar(resultado)
        historiaDao.insertar(
            HistoriaEntity(
                oportunidadId = resultado.oportunidadId,
                descripcion = "Resultado registrado: ${resultado.resultado} · " +
                        "impacto ${resultado.impacto}"
            )
        )
        if (resultado.cerrada) {
            historiaDao.insertar(
                HistoriaEntity(
                    oportunidadId = resultado.oportunidadId,
                    descripcion = "Oportunidad cerrada"
                )
            )
        }
    }
}