package dev.vskelk.cdf.core.database.dao

import androidx.room.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * ReactivoDao - Acceso a reactivos y sus opciones
 *
 * Gestiona el ciclo de vida de los reactivos del examen.
 */
@Dao
interface ReactivoDao {

    // ===== REACTIVOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactivo(reactivo: ReactivoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactivos(reactivos: List<ReactivoEntity>): List<Long>

    @Update
    suspend fun updateReactivo(reactivo: ReactivoEntity)

    @Delete
    suspend fun deleteReactivo(reactivo: ReactivoEntity)

    @Query("SELECT * FROM reactivos WHERE id = :reactivoId")
    suspend fun getReactivoById(reactivoId: Long): ReactivoEntity?

    @Query("SELECT * FROM reactivos WHERE id = :reactivoId")
    fun observeReactivoById(reactivoId: Long): Flow<ReactivoEntity?>

    @Query("""
        SELECT * FROM reactivos
        WHERE status = 'ACTIVE'
        AND (vigenciaHasta IS NULL OR vigenciaHasta > :currentTime)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getRandomActiveReactivos(
        limit: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<ReactivoEntity>

    @Query("""
        SELECT * FROM reactivos
        WHERE modulo = :modulo
        AND examArea = :examArea
        AND status = 'ACTIVE'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getReactivosByModuloAndArea(
        modulo: String,
        examArea: String,
        limit: Int
    ): List<ReactivoEntity>

    @Query("""
        SELECT * FROM reactivos
        WHERE subtemaId = :subtemaId
        AND status = 'ACTIVE'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getReactivosBySubtema(subtemaId: Long, limit: Int): List<ReactivoEntity>

    @Query("""
        SELECT * FROM reactivos
        WHERE subtemaId IN (:subtemaIds)
        AND status = 'ACTIVE'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getReactivosBySubtemas(
        subtemaIds: List<Long>,
        limit: Int
    ): List<ReactivoEntity>

    @Query("""
        SELECT * FROM reactivos
        WHERE nivelCognitivo = :nivel
        AND status = 'ACTIVE'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getReactivosByNivelCognitivo(nivel: String, limit: Int): List<ReactivoEntity>

    @Query("SELECT COUNT(*) FROM reactivos WHERE status = 'ACTIVE'")
    suspend fun getActiveReactivoCount(): Int

    @Query("SELECT COUNT(*) FROM reactivos WHERE status = 'ACTIVE'")
    fun observeActiveReactivoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reactivos WHERE status = 'INVALIDATED'")
    fun observeInvalidatedReactivoCount(): Flow<Int>

    // ===== OPCIONES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOption(option: ReactivoOptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<ReactivoOptionEntity>)

    @Query("SELECT * FROM reactivo_opciones WHERE reactivoId = :reactivoId ORDER BY displayOrder")
    suspend fun getOptionsForReactivo(reactivoId: Long): List<ReactivoOptionEntity>

    @Query("SELECT * FROM reactivo_opciones WHERE reactivoId = :reactivoId ORDER BY displayOrder")
    fun observeOptionsForReactivo(reactivoId: Long): Flow<List<ReactivoOptionEntity>>

    @Query("SELECT * FROM reactivo_opciones WHERE id = :optionId")
    suspend fun getOptionById(optionId: Long): ReactivoOptionEntity?

    // ===== REACTIVOS + OPCIONES (combinado) =====

    @Transaction
    @Query("SELECT * FROM reactivos WHERE id = :reactivoId AND status = 'ACTIVE'")
    suspend fun getReactivoWithOptions(reactivoId: Long): ReactivoWithOptions?

    @Transaction
    @Query("SELECT * FROM reactivos WHERE subtemaId = :subtemaId AND status = 'ACTIVE'")
    suspend fun getReactivosWithOptionsBySubtema(subtemaId: Long): List<ReactivoWithOptions>

    // ===== INTENTOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntento(intento: ReactivoIntentoEntity): Long

    @Query("""
        SELECT * FROM reactivo_intentos
        WHERE sessionId = :sessionId
        ORDER BY createdAt
    """)
    suspend fun getIntentosForSession(sessionId: Long): List<ReactivoIntentoEntity>

    @Query("""
        SELECT COUNT(*) FROM reactivo_intentos
        WHERE reactivoId = :reactivoId
    """)
    suspend fun getIntentoCountForReactivo(reactivoId: Long): Int

    @Query("""
        SELECT AVG(tiempoRespuestaMs) FROM reactivo_intentos
        WHERE sessionId = :sessionId
    """)
    suspend fun getAverageTimeForSession(sessionId: Long): Float?

    // ===== BÚSQUEDA =====

    @Query("""
        SELECT * FROM reactivos
        WHERE enunciado LIKE '%' || :query || '%'
        AND status = 'ACTIVE'
        LIMIT :limit
    """)
    suspend fun searchReactivos(query: String, limit: Int = 20): List<ReactivoEntity>

    // ===== INVALIDACIÓN =====

    @Query("""
        SELECT r.* FROM reactivos r
        INNER JOIN reactivo_fragment_cross_ref rf ON r.id = rf.reactivoId
        WHERE rf.fragmentId = :fragmentId
        AND r.status = 'ACTIVE'
    """)
    suspend fun getReactivosByFragment(fragmentId: Long): List<ReactivoEntity>

    @Transaction
    suspend fun invalidateByFragment(
        fragmentId: Long,
        invalidationReason: String
    ) {
        val reactivos = getReactivosByFragment(fragmentId)
        reactivos.forEach { reactivo ->
            updateReactivo(
                reactivo.copy(
                    status = ReactivoStatus.INVALIDATED,
                    invalidationReason = invalidationReason,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

/**
 * Data class para reactivo con sus opciones
 */
data class ReactivoWithOptions(
    @Embedded
    val reactivo: ReactivoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "reactivoId"
    )
    val opciones: List<ReactivoOptionEntity>
)
