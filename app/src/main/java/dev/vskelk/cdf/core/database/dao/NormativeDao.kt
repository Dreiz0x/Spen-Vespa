package dev.vskelk.cdf.core.database.dao

import androidx.room.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * NormativeDao - Acceso a fragmentos normativos canónicos
 *
 * Per spec: Solo fuentes oficiales:
 * LEGIPE, Reglamento INE, Acuerdos CG, TEPJF, Reglamento Interior INE
 */
@Dao
interface NormativeDao {

    // ===== FRAGMENTOS NORMATIVOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFragment(fragment: NormativeFragmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFragments(fragments: List<NormativeFragmentEntity>): List<Long>

    @Update
    suspend fun updateFragment(fragment: NormativeFragmentEntity)

    @Query("SELECT * FROM normative_fragments WHERE id = :fragmentId")
    suspend fun getFragmentById(fragmentId: Long): NormativeFragmentEntity?

    @Query("SELECT * FROM normative_fragments WHERE id = :fragmentId")
    fun observeFragmentById(fragmentId: Long): Flow<NormativeFragmentEntity?>

    @Query("""
        SELECT * FROM normative_fragments
        WHERE status = 'VIGENTE'
        AND (vigenciaHasta IS NULL OR vigenciaHasta > :currentTime)
        ORDER BY source, articleRef
    """)
    fun getVigenteFragments(currentTime: Long = System.currentTimeMillis()): Flow<List<NormativeFragmentEntity>>

    @Query("""
        SELECT * FROM normative_fragments
        WHERE areaExamen = :areaExamen
        AND status = 'VIGENTE'
        ORDER BY source, articleRef
    """)
    fun getFragmentsByArea(areaExamen: String): Flow<List<NormativeFragmentEntity>>

    @Query("""
        SELECT * FROM normative_fragments
        WHERE source = :source
        AND status = 'VIGENTE'
        ORDER BY articleRef
    """)
    fun getFragmentsBySource(source: String): Flow<List<NormativeFragmentEntity>>

    @Query("SELECT COUNT(*) FROM normative_fragments WHERE status = 'VIGENTE'")
    suspend fun getVigenteFragmentCount(): Int

    @Query("SELECT COUNT(*) FROM normative_fragments WHERE status = 'VIGENTE'")
    fun observeVigenteFragmentCount(): Flow<Int>

    // ===== BÚSQUEDA =====

    @Query("""
        SELECT * FROM normative_fragments
        WHERE content LIKE '%' || :keyword || '%'
        OR articleRef LIKE '%' || :keyword || '%'
        OR source LIKE '%' || :keyword || '%'
        LIMIT :limit
    """)
    suspend fun searchByKeyword(keyword: String, limit: Int = 20): List<NormativeFragmentEntity>

    @Query("""
        SELECT * FROM normative_fragments
        WHERE source = :source
        AND articleRef = :articleRef
        AND status = 'VIGENTE'
        LIMIT 1
    """)
    suspend fun findBySourceAndArticle(source: String, articleRef: String): NormativeFragmentEntity?

    // ===== VERSIONADO NORMATIVO =====

    @Query("""
        SELECT * FROM normative_fragments
        WHERE status = 'MODIFICADO'
        OR status = 'DEROGADO'
        ORDER BY updatedAt DESC
    """)
    fun getNonVigenteFragments(): Flow<List<NormativeFragmentEntity>>

    @Transaction
    suspend fun invalidateFragment(
        fragmentId: Long,
        invalidateReason: String,
        replacedById: Long? = null
    ) {
        val fragment = getFragmentById(fragmentId) ?: return
        updateFragment(
            fragment.copy(
                status = if (replacedById != null) "MODIFICADO" else "DEROGADO",
                vigenciaHasta = System.currentTimeMillis(),
                invalidationReason = invalidateReason,
                reemplazadoPorId = replacedById,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ===== CROSS-REFERENCES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReactivoFragmentCrossRef(crossRef: ReactivoFragmentCrossRef)

    @Query("""
        SELECT nf.* FROM normative_fragments nf
        INNER JOIN reactivo_fragment_cross_ref rf ON nf.id = rf.fragmentId
        WHERE rf.reactivoId = :reactivoId
        ORDER BY rf.isPrimary DESC
    """)
    fun getFragmentsForReactivo(reactivoId: Long): Flow<List<NormativeFragmentEntity>>

    // ===== FUENTES DOCUMENTALES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: DocumentSourceEntity): Long

    @Query("SELECT * FROM document_sources WHERE abreviatura = :abreviatura")
    suspend fun getSourceByAbreviatura(abreviatura: String): DocumentSourceEntity?

    @Query("SELECT * FROM document_sources ORDER BY nombreCompleto")
    fun observeAllSources(): Flow<List<DocumentSourceEntity>>

    // ===== VALIDACIÓN DE DUPLICADOS =====

    @Query("""
        SELECT * FROM normative_fragments
        WHERE content = :content
        AND status = 'VIGENTE'
        LIMIT 1
    """)
    suspend fun findExactDuplicate(content: String): NormativeFragmentEntity?

    @Query("""
        SELECT * FROM normative_fragments
        WHERE source = :source
        AND articleRef = :articleRef
        AND content LIKE '%' || :contentFragment || '%'
        AND status = 'VIGENTE'
    """)
    suspend fun findSimilarFragment(
        source: String,
        articleRef: String,
        contentFragment: String
    ): List<NormativeFragmentEntity>
}
