package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * InvestigadorRepository - Auto-Investigador y sistema de cuarentena
 */
interface InvestigadorRepository {

    /**
     * Investiga un tema usando IA
     */
    suspend fun investigar(tema: String, areaExamen: String?): InvestigacionEstado

    /**
     * Obtiene fragmentos pendientes de revisión
     */
    fun observePendientes(): Flow<List<CuarentenaFragmentoEntity>>

    /**
     * Obtiene fragmentos en conflicto
     */
    fun observeConflictos(): Flow<List<CuarentenaFragmentoEntity>>

    /**
     * Aprueba un fragmento
     */
    suspend fun approveFragmento(fragmentoId: Long)

    /**
     * Rechaza un fragmento
     */
    suspend fun rejectFragmento(fragmentoId: Long)

    /**
     * Aprueba un fragmento y lo convierte en normativo
     */
    suspend fun approveAndPromoteToNormative(fragmentoId: Long)

    /**
     * Obtiene el conteo de pendientes
     */
    fun observePendienteCount(): Flow<Int>

    /**
     * Obtiene el conteo de conflictos
     */
    fun observeConflictoCount(): Flow<Int>
}
