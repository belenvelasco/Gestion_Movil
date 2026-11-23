package com.example.gestion_movil.data.local

import android.content.Context
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
        HistoriaEntity::class,
        EvidenciaEntity::class,
        ActivacionEntity::class,
        ResultadoEntity::class
               ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {



    abstract fun oportunidadDao(): OportunidadDao
    abstract fun organizacionDao (): OrganizacionDao
    abstract fun contactoDao():ContactoDao
    abstract fun seguimientoDao():SeguimientoDao
    abstract fun historiaDao(): HistoriaDao
    abstract fun evidenciaDao(): EvidenciaDao

    abstract fun activacionDao(): ActivacionDao

    abstract fun resultadoDao(): ResultadoDao

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
        private val MIGRACION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE evidencias ADD COLUMN nombre TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `activaciones` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`oportunidadId` INTEGER NOT NULL, " +
                            "`espacio` TEXT NOT NULL, " +
                            "`fechaActivacion` INTEGER, " +
                            "`costo` INTEGER, " +
                            "`requisitos` TEXT NOT NULL, " +
                            "`responsable` TEXT NOT NULL, " +
                            "FOREIGN KEY(`oportunidadId`) REFERENCES `oportunidades`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_activaciones_oportunidadId` " +
                            "ON `activaciones` (`oportunidadId`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `resultados` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`oportunidadId` INTEGER NOT NULL, " +
                            "`resultado` TEXT NOT NULL, " +
                            "`observaciones` TEXT NOT NULL, " +
                            "`impacto` TEXT NOT NULL, " +
                            "`cerrada` INTEGER NOT NULL, " +
                            "`fecha` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`oportunidadId`) REFERENCES `oportunidades`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_resultados_oportunidadId` " +
                            "ON `resultados` (`oportunidadId`)"
                )
            }
        }
    }
}