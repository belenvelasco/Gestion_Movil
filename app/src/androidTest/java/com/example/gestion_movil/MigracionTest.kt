package com.example.gestion_movil

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestion_movil.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Prueba el SQL de la migracion 5 -> 6 sobre una base minima de la version 5
// (solo las tablas que la migracion toca; no valida el esquema completo de Room).
@RunWith(AndroidJUnit4::class)
class MigracionTest {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun crearBaseVersion5() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(contexto)
            .name(null) // en memoria
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE oportunidades (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL)"
                    )
                    db.execSQL(
                        "CREATE TABLE evidencias (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "oportunidadId INTEGER NOT NULL, tipo TEXT NOT NULL, ruta TEXT NOT NULL)"
                    )
                    db.execSQL("INSERT INTO oportunidades (id, nombre) VALUES (1, 'Feria Verde')")
                    db.execSQL("INSERT INTO evidencias (oportunidadId, tipo, ruta) VALUES (1, 'FOTO', 'a.jpg')")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, antigua: Int, nueva: Int) {}
            })
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(config)
        db = helper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = ON")
    }

    @After
    fun cerrar() = helper.close()

    private fun existeTabla(nombre: String): Boolean =
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$nombre'").use { it.count == 1 }

    @Test
    fun conservaLosDatosYAgregaLaColumnaNombre() {
        AppDatabase.MIGRACION_5_6.migrate(db)

        db.query("SELECT ruta, nombre FROM evidencias").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("a.jpg", c.getString(0))
            assertEquals("", c.getString(1))
        }
        db.query("SELECT nombre FROM oportunidades WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("Feria Verde", c.getString(0))
        }
    }

    @Test
    fun creaLasTablasDeActivacionesYResultados() {
        AppDatabase.MIGRACION_5_6.migrate(db)
        assertTrue(existeTabla("activaciones"))
        assertTrue(existeTabla("resultados"))
    }

    @Test
    fun alBorrarLaOportunidadSeBorranSuActivacionYResultado() {
        AppDatabase.MIGRACION_5_6.migrate(db)
        db.execSQL(
            "INSERT INTO activaciones (oportunidadId, espacio, requisitos, responsable) " +
                    "VALUES (1, 'Stand', '', 'Maria rojas')"
        )
        db.execSQL(
            "INSERT INTO resultados (oportunidadId, resultado, observaciones, impacto, cerrada, fecha) " +
                    "VALUES (1, 'Activación realizada', '', 'Alto', 0, 0)"
        )

        db.execSQL("DELETE FROM oportunidades WHERE id = 1")

        db.query("SELECT COUNT(*) FROM activaciones").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
        db.query("SELECT COUNT(*) FROM resultados").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
    }
}
