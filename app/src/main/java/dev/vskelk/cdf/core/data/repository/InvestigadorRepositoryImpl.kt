package dev.vskelk.cdf.core.data.repository

import dev.vskelk.cdf.core.database.dao.*
import dev.vskelk.cdf.core.database.entity.*
import dev.vskelk.cdf.core.domain.model.*
import dev.vskelk.cdf.core.domain.repository.InvestigadorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * InvestigadorRepositoryImpl - Implementación del Auto-Investigador
 *
 * Per spec: La IA nunca escribe conocimiento canónico directamente.
 * Solo extrae, estructura y propone. El usuario valida.
 */
@Singleton
class InvestigadorRepositoryImpl @Inject constructor(
    private val cuarentenaDao: CuarentenaDao,
    private val normativeDao: NormativeDao,
    private val ontologyDao: OntologyDao,
    private val preferencesDataSource: dev.vskelk.cdf.core.datastore.PreferencesDataSource
) : InvestigadorRepository {

    override suspend fun investigar(tema: String, areaExamen: String?): InvestigacionEstado {
        try {
            // PASO 1: ACOTAR - Verificar qué hay ya en Room
            emitState(InvestigacionEstado.Acotando("Buscando información existente..."))
            val existente = normativeDao.searchByKeyword(tema)
            if (existente.size >= 5) {
                // Hay suficiente información, no necesitamos llamar a la API
                return InvestigacionEstado.Completado(
                    InvestigacionResult(
                        fragmentos = existente.map { it.toInvestigado() },
                        conflictos = emptyList(),
                        fuentesVerificadas = existente.size,
                        necesitaRevision = false
                    )
                )
            }

            // PASO 2: FORMULAR - Preparar prompt para IA
            emitState(InvestigacionEstado.Formulando("Preparando consulta..."))
            val apiKey = preferencesDataSource.getApiKey("ANTHROPIC")
                ?: return InvestigacionEstado.Error("No hay API key configurada")

            // En producción, aquí se construiría el prompt y se llamaría a la IA
            // Por ahora, retornamos un estado de simulación
            emitState(InvestigacionEstado.Consultando("Consultando fuentes..."))
            // Simular llamada a IA
            kotlinx.coroutines.delay(1000)

            // PASO 3-5: Validar, parsear y guardar en cuarentena
            emitState(InvestigacionEstado.Validando("Analizando resultados..."))

            return InvestigacionEstado.Completado(
                InvestigacionResult(
                    fragmentos = emptyList(),
                    conflictos = emptyList(),
                    fuentesVerificadas = 0,
                    necesitaRevision = true
                )
            )
        } catch (e: Exception) {
            return InvestigacionEstado.Error(e.message ?: "Error en investigación")
        }
    }

    private suspend fun emitState(state: InvestigacionEstado): InvestigacionEstado {
        kotlinx.coroutines.delay(500) // Simular trabajo
        return state
    }

    override fun observePendientes(): Flow<List<CuarentenaFragmentoEntity>> {
        return cuarentenaDao.observePendientes()
    }

    override fun observeConflictos(): Flow<List<CuarentenaFragmentoEntity>> {
        return cuarentenaDao.observeConflictos()
    }

    override suspend fun approveFragmento(fragmentoId: Long) {
        cuarentenaDao.approveFragmento(fragmentoId)
    }

    override suspend fun rejectFragmento(fragmentoId: Long) {
        cuarentenaDao.rejectFragmento(fragmentoId)
    }

    override suspend fun approveAndPromoteToNormative(fragmentoId: Long) {
        val fragmento = cuarentenaDao.getFragmentoById(fragmentoId) ?: return

        // 1. Verificar si existe duplicado exacto
        val existente = normativeDao.findExactDuplicate(fragmento.contenido)
        if (existente != null) {
            // Reforzar confianza del existente
            normativeDao.updateFragment(
                existente.copy(
                    confidenceCount = existente.confidenceCount + 1,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            // 2. Crear nuevo fragmento normativo
            val nuevoFragmento = NormativeFragmentEntity(
                content = fragmento.contenido,
                source = fragmento.fuente ?: "DESCONOCIDA",
                articleRef = null,
                sourceType = fragmento.fuenteTipo ?: "DESCONOCIDO",
                certainty = fragmento.certeza,
                areaExamen = fragmento.areaExamen ?: "TECNICO",
                vigenciaDesde = System.currentTimeMillis()
            )
            normativeDao.insertFragment(nuevoFragmento)
        }

        // 3. Marcar como aprobado en cuarentena
        cuarentenaDao.approveFragmento(fragmentoId)
    }

    override fun observePendienteCount(): Flow<Int> {
        return cuarentenaDao.observePendienteCount()
    }

    override fun observeConflictoCount(): Flow<Int> {
        return cuarentenaDao.observeConflictoCount()
    }

    private fun NormativeFragmentEntity.toInvestigado() = FragmentoInvestigado(
        contenido = content,
        fuente = source,
        articleRef = articleRef,
        certeza = certainty,
        areaExamen = areaExamen,
        nodoSugerido = null
    )
}
