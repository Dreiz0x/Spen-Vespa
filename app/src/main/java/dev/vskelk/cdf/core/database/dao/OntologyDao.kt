package dev.vskelk.cdf.core.database.dao

import androidx.room.*
import dev.vskelk.cdf.core.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * OntologyDao - Acceso a la capa ontológica
 *
 * La ontología es el eje que conecta todos los módulos de Vespa.
 */
@Dao
interface OntologyDao {

    // ===== NODOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: OntologyNodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<OntologyNodeEntity>): List<Long>

    @Update
    suspend fun updateNode(node: OntologyNodeEntity)

    @Delete
    suspend fun deleteNode(node: OntologyNodeEntity)

    @Query("SELECT * FROM ontology_nodes WHERE id = :nodeId")
    suspend fun getNodeById(nodeId: Long): OntologyNodeEntity?

    @Query("SELECT * FROM ontology_nodes WHERE id = :nodeId")
    fun observeNodeById(nodeId: Long): Flow<OntologyNodeEntity?>

    @Query("SELECT * FROM ontology_nodes WHERE nodeType = :nodeType AND isActive = 1 ORDER BY displayOrder")
    fun getNodesByType(nodeType: String): Flow<List<OntologyNodeEntity>>

    @Query("SELECT * FROM ontology_nodes WHERE parentId = :parentId AND isActive = 1 ORDER BY displayOrder")
    fun getChildNodes(parentId: Long): Flow<List<OntologyNodeEntity>>

    @Query("SELECT * FROM ontology_nodes WHERE parentId IS NULL AND isActive = 1 ORDER BY displayOrder")
    fun getRootNodes(): Flow<List<OntologyNodeEntity>>

    @Query("SELECT * FROM ontology_nodes WHERE cargoId = :cargoId AND isActive = 1 ORDER BY displayOrder")
    fun getNodesByCargo(cargoId: Long): Flow<List<OntologyNodeEntity>>

    @Query("SELECT * FROM ontology_nodes WHERE isActive = 1")
    fun observeAllNodes(): Flow<List<OntologyNodeEntity>>

    @Query("SELECT COUNT(*) FROM ontology_nodes WHERE isActive = 1")
    suspend fun getActiveNodeCount(): Int

    // ===== RELACIONES =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: OntologyRelationEntity)

    @Query("SELECT * FROM ontology_relations WHERE sourceNodeId = :nodeId")
    fun getRelationsFromNode(nodeId: Long): Flow<List<OntologyRelationEntity>>

    @Query("SELECT * FROM ontology_relations WHERE targetNodeId = :nodeId")
    fun getRelationsToNode(nodeId: Long): Flow<List<OntologyRelationEntity>>

    @Query("""
        SELECT n.* FROM ontology_nodes n
        INNER JOIN ontology_relations r ON n.id = r.targetNodeId
        WHERE r.sourceNodeId = :nodeId AND r.relationType = :relationType
    """)
    fun getRelatedNodes(nodeId: Long, relationType: String): Flow<List<OntologyNodeEntity>>

    // ===== CARGOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCargo(cargo: CargoEntity): Long

    @Query("SELECT * FROM cargos WHERE id = :cargoId")
    suspend fun getCargoById(cargoId: Long): CargoEntity?

    @Query("SELECT * FROM cargos WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCargo(): CargoEntity?

    @Query("SELECT * FROM cargos")
    fun observeAllCargos(): Flow<List<CargoEntity>>

    // ===== ÓRGANOS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrgano(organo: OrganoEntity): Long

    @Query("SELECT * FROM organos WHERE id = :organoId")
    suspend fun getOrganoById(organoId: Long): OrganoEntity?

    @Query("SELECT * FROM organos WHERE tipo = :tipo")
    fun getOrganosByTipo(tipo: String): Flow<List<OrganoEntity>>

    // ===== SUBTEMAS (alias para nodos específicos) =====

    @Query("SELECT * FROM ontology_nodes WHERE nodeType = :nodeType AND isActive = 1")
    fun getSubtemas(nodeType: String = OntologyNodeTypes.SUBTEMA): Flow<List<OntologyNodeEntity>>

    @Query("""
        SELECT n.* FROM ontology_nodes n
        WHERE n.nodeType = 'SUBTEMA'
        AND n.id IN (
            SELECT DISTINCT m.subtemaId FROM user_topic_mastery m
            WHERE m.subtemaId IS NOT NULL
        )
        AND n.isActive = 1
    """)
    fun getSubtemasConMastery(): Flow<List<OntologyNodeEntity>>

    // ===== BÚSQUEDA =====

    @Query("""
        SELECT * FROM ontology_nodes
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND isActive = 1
        LIMIT :limit
    """)
    suspend fun searchNodes(query: String, limit: Int = 20): List<OntologyNodeEntity>
}
