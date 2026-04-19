package dev.vskelk.cdf.core.domain.model

import dev.vskelk.cdf.core.database.entity.*

/**
 * OntologyModels - Modelos de dominio para la capa ontológica
 */

/**
 * Nodo ontológico simplificado para UI
 */
data class OntologyNode(
    val id: Long,
    val nodeType: String,
    val name: String,
    val description: String?,
    val parentId: Long?,
    val weight: Float,
    val isActive: Boolean
)

/**
 * Sub题材 con estado de dominio
 */
data class SubtemaConDominio(
    val subtema: OntologyNode,
    val estadoDominio: String,
    val precision: Float,
    val totalIntentos: Int,
    val velocidadPromedio: Float
)

/**
 * Resultado del diagnóstico
 */
data class DiagnosticoResult(
    val totalSubtemas: Int,
    val subtemasDebiles: List<SubtemaConDominio>,
    val erroresFrecuentes: Map<String, Int>,
    val precisionGeneral: Float,
    val recomendaciones: List<Recomendacion>
)

/**
 * Recomendación accionable
 */
data class Recomendacion(
    val tipo: RecomendacionTipo,
    val subtemaId: Long,
    val subtemaNombre: String,
    val fundamentoRef: String?,
    val descripcion: String
)

enum class RecomendacionTipo {
    REPASAR_FUNDAMENTO,
    PRACTICAR_MAS,
    AVANZAR_NIVEL,
    INVESTIGAR
}

/**
 * Conversión de Entity a Model
 */
fun OntologyNodeEntity.toModel() = OntologyNode(
    id = id,
    nodeType = nodeType,
    name = name,
    description = description,
    parentId = parentId,
    weight = weight,
    isActive = isActive
)

fun UserTopicMasteryEntity.toSubtemaConDominio(node: OntologyNode) = SubtemaConDominio(
    subtema = node,
    estadoDominio = estadoDominio,
    precision = precision,
    totalIntentos = totalIntentos,
    velocidadPromedio = velocidadPromedio
)
