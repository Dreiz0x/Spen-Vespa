package dev.vskelk.cdf.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.vskelk.cdf.core.database.dao.*
import dev.vskelk.cdf.core.database.entity.*

/**
 * AppDatabase - Base de datos principal de Vespa
 *
 * Room es la única fuente de verdad en tiempo de ejecución.
 * Per spec: "Room es la única fuente de verdad en tiempo de ejecución."
 *
 * Schema exportado para migraciones.
 */
@Database(
    entities = [
        // Ontología
        OntologyNodeEntity::class,
        OntologyRelationEntity::class,
        CargoEntity::class,
        OrganoEntity::class,

        // Normativa
        NormativeFragmentEntity::class,
        DocumentSourceEntity::class,
        ReactivoFragmentCrossRef::class,

        // Reactivos
        ReactivoEntity::class,
        ReactivoOptionEntity::class,
        ReactivoIntentoEntity::class,

        // Motor Adaptativo
        UserTopicMasteryEntity::class,
        UserGapLogEntity::class,
        StudySessionEntity::class,

        // Cuarentena
        CuarentenaFragmentoEntity::class,

        // Conversaciones
        ConversationEntity::class,
        MessageEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = true // Exportar schema para migraciones
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs de Ontología
    abstract fun ontologyDao(): OntologyDao

    // DAOs de Normativa
    abstract fun normativeDao(): NormativeDao

    // DAOs de Reactivos
    abstract fun reactivoDao(): ReactivoDao

    // DAOs de Motor Adaptativo
    abstract fun userMasteryDao(): UserMasteryDao
    abstract fun studySessionDao(): StudySessionDao

    // DAOs de Cuarentena
    abstract fun cuarentenaDao(): CuarentenaDao

    // DAOs de Conversaciones
    abstract fun conversationDao(): ConversationDao

    companion object {
        const val DATABASE_NAME = "vespa_database"

        // Schema location for KSP
        const val SCHEMA_LOCATION = "schemas"
    }
}
