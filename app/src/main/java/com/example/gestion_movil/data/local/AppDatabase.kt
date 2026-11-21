package com.example.gestion_movil.data.local

import android.content.Context
import android.provider.ContactsContract
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        OportunidadEntity::class,
        OrganizacionEntity::class,
        ContactoEntity::class,
        SeguimientoEntity::class,
        HistorialEntity::class
               ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun oportunidadDao(): OportunidadDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao():ContactoDao
    abstract fun seguimientoDao():SeguimientoDao
    abstract fun historiaDao(): HistoriaDao

    companion object {
        //longitud y longitud Ubicacion

        private val MIGRACION_1_2 = object : Migration(1,2){
            override fun migrate(db: SupportSQLiteDatabase){
                db.execSQL("ALTER TABLE oportunidades ADD COLUMN latitud REAL")
                db.execSQL("ALTER TABLE oportunidades ADD COLUMN longitud REAL")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //
        //

        @Suppress("DEPRECATION")
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oportunidad.db"
                ).addMigrations(MIGRACION_1_2)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}