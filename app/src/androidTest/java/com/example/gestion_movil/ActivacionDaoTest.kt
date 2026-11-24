package com.example.gestion_movil

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestion_movil.data.local.ActivacionEntity
import com.example.gestion_movil.data.local.AppDatabase
import com.example.gestion_movil.data.local.HistoriaEntity
import com.example.gestion_movil.data.local.OportunidadEntity
import com.example.gestion_movil.data.local.ResultadoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivacionDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBd() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBd() = db.close()

    private suspend fun crearOportunidad(): Int = db.oportunidadDao().guardar(
        OportunidadEntity(
            nombre = "Feria Verde", tipo = "Feria", descripcion = "", fuente = "Web",
            prioridad = "Alta", potencial = "Alta", estado = "Evento identificado",
            observaciones = ""
        )
    ).toInt()

    @Test
    fun guardarDosVecesLaActivacionReemplazaEnVezDeDuplicar() = runBlocking {
        val id = crearOportunidad()
        val dao = db.activacionDao()
        dao.guardar(ActivacionEntity(oportunidadId = id, espacio = "Stand A"))
        dao.guardar(ActivacionEntity(oportunidadId = id, espacio = "Stand B"))
        assertEquals("Stand B", dao.obtener(id)!!.espacio)
    }

    @Test
    fun alReasignarElResponsableSeGuardaEnLaOportunidad() = runBlocking {
        val id = crearOportunidad()
        db.oportunidadDao().actualizarResponsable(id, "Camilo ramos", 1L)
        assertEquals("Camilo ramos", db.oportunidadDao().obtenerPorId(id)!!.responsable)
    }

    @Test
    fun alBorrarLaOportunidadSeBorraTodoLoAsociado() = runBlocking {
        val id = crearOportunidad()
        db.activacionDao().guardar(ActivacionEntity(oportunidadId = id, espacio = "Stand"))
        db.resultadoDao().guardar(ResultadoEntity(oportunidadId = id, resultado = "Activación realizada", impacto = "Alto"))
        db.historiaDao().insertar(HistoriaEntity(oportunidadId = id, descripcion = "Cambio"))

        db.oportunidadDao().eliminar(db.oportunidadDao().obtenerPorId(id)!!)

        assertNull(db.activacionDao().obtener(id))
        assertNull(db.resultadoDao().obtener(id))
        assertEquals(0, db.historiaDao().observarPorOportunidad(id).first().size)
    }
}