package dev.vskelk.cdf.core.domain.repository

import dev.vskelk.cdf.core.domain.model.*
import dev.vskelk.cdf.core.database.entity.CuarentenaFragmentoEntity
import kotlinx.coroutines.flow.Flow

/**
 * InvestigadorRepository - Auto-Investigador y sistema de cuarentena
 */
interface InvestigadorRepository {
    suspend fun investigar(tema: String, areaExamen: String?): InvestigacionEstado
    fun observePendientes(): Flow<List<CuarentenaFragmentoEntity>>
    fun observeConflictos(): Flow<List<CuarentenaFragmentoEntity>>
    suspend fun approveFragmento(fragmentoId: Long)
    suspend fun rejectFragmento(fragmentoId: Long)
    suspend fun approveAndPromoteToNormative(fragmentoId: Long)
    fun observePendienteCount(): Flow<Int>
    fun observeConflictoCount(): Flow<Int>
}
