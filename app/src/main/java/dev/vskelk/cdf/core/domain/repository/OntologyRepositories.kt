package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.OntologyNode
import dev.vskelk.cdf.core.domain.model.DiagnosticoResult
import dev.vskelk.cdf.core.domain.model.SubtemaConDominio
import kotlinx.coroutines.flow.Flow

/**
 * OntologyRepository - Acceso a la capa ontológica
 */
interface OntologyRepository {

    /**
     * Obtiene todos los nodos ontológicos
     */
    fun observeAllNodes(): Flow<List<OntologyNode>>

    /**
     * Obtiene nodos por tipo
     */
    fun observeNodesByType(nodeType: String): Flow<List<OntologyNode>>

    /**
     * Obtiene hijos de un nodo
     */
    fun observeChildNodes(parentId: Long): Flow<List<OntologyNode>>

    /**
     * Obtiene un nodo por ID
     */
    suspend fun getNodeById(nodeId: Long): OntologyNode?

    /**
     * Busca nodos por nombre
     */
    suspend fun searchNodes(query: String): List<OntologyNode>

    /**
     * Obtiene subtemas con su estado de dominio
     */
    fun observeSubtemasConDominio(): Flow<List<SubtemaConDominio>>

    /**
     * Obtiene los subtemas más débiles
     */
    suspend fun getWeakSubtemas(limit: Int = 10): List<SubtemaConDominio>

    /**
     * Genera diagnóstico completo
     */
    suspend fun getDiagnostico(): DiagnosticoResult

    /**
     * Obtiene nodos relacionados a un nodo
     */
    fun observeRelatedNodes(nodeId: Long, relationType: String): Flow<List<OntologyNode>>
}
