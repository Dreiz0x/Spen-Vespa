package dev.vskelk.cdf.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CuarentenaEntities - Sistema de validación de conocimiento
 *
 * Per spec: La IA nunca escribe conocimiento canónico directamente.
 * Solo extrae, estructura y propone. El usuario valida.
 *
 * El flujo es:
 * 1. Auto-Investigador extrae fragmentos
 * 2. Se guardan en cuarentena con estado PENDIENTE, CONFLICTO, o APROBADO
 * 3. Usuario revisa y decide: aprobar → se convierte en NormativeFragment
 *                                           rechazar → se elimina
 */

/**
 * CuarentenaFragmentoEntity - Fragmentos en espera de validación
 */
@Entity(
    tableName = "cuarentena_fragmentos",
    indices = [
        Index(value = ["estado"]),
        Index(value = ["fuenteTipo"]),
        Index(value = ["areaExamen"]),
        Index(value = ["creadoEn"])
    ]
)
data class CuarentenaFragmentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Contenido extraído por la IA */
    val contenido: String,

    /** Fuente cited por la IA */
    val fuente: String? = null,

    /** Tipo de fuente */
    val fuenteTipo: String? = null, // LEGIPE, REGLAMENTO, etc.

    /** Nivel de certeza de la extracción */
    val certeza: String, // ALTA, MEDIA, BAJA

    /** Área de examen asignada */
    val areaExamen: String? = null, // TECNICO, SISTEMA, GENERAL

    /** FK al fragmento conflictuado (si es CONFLICTO) */
    val conflictoConId: Long? = null,

    /** Descripción del conflicto (si es CONFLICTO) */
    val conflictoDescripcion: String? = null,

    /** Estado del fragmento en cuarentena */
    val estado: String, // PENDIENTE, APROBADO, RECHAZADO, CONFLICTO

    /** Prompt original que generó este fragmento */
    val promptOrigen: String? = null,

    /** Respuesta cruda de la IA (para auditoría) */
    val respuestaRaw: String? = null,

    /** Nodos ontológicos sugeridos (JSON array) */
    val suggestedNodesJson: String? = null,

    /** Timestamp de creación */
    val creadoEn: Long = System.currentTimeMillis(),

    /** Timestamp de revisión */
    val revisadoEn: Long? = null,

    /** Usuario que revisó (para auditoría) */
    val revisadoPor: String? = null
)

/**
 * Constantes de estado de cuarentena
 */
object CuarentenaEstado {
    /** Pendiente de revisión por el usuario */
    const val PENDIENTE = "PENDIENTE"

    /** Aprobado por el usuario - listo para pasar a NormativeFragment */
    const val APROBADO = "APROBADO"

    /** Rechazado por el usuario - se eliminará */
    const val RECHAZADO = "RECHAZADO"

    /** Conflicto detectado - requiere resolución manual */
    const val CONFLICTO = "CONFLICTO"
}

/**
 * Reglas de validación automática
 *
 * Per spec:
 * - Sin fuente: Descarte silencioso
 * - Certeza BAJA: Cuarentena con marca roja, nunca auto-aprobado
 * - Contradice Room: CONFLICTO, revisión obligatoria
 * - Fuente no oficial: Certeza BAJA forzada
 * - Duplicado exacto: Refuerza confianza del existente, no inserta
 */
object CuarentenaRules {
    /**
     * Verifica si un fragmento debe ser descartado automáticamente
     */
    fun debeDescartarse(fuente: String?, certeza: String): Boolean {
        // Sin fuente = descarte silencioso
        if (fuente.isNullOrBlank()) return true
        // Certeza BAJA se marca pero NO se descarta automáticamente
        return false
    }

    /**
     * Determina el estado inicial según las reglas
     */
    fun determinarEstadoInicial(
        fuente: String?,
        certeza: String,
        tieneConflicto: Boolean
    ): String {
        return when {
            fuente.isNullOrBlank() -> CuarentenaEstado.PENDIENTE
            tieneConflicto -> CuarentenaEstado.CONFLICTO
            certeza == ExtractionCertainty.BAJA -> CuarentenaEstado.PENDIENTE
            else -> CuarentenaEstado.PENDIENTE
        }
    }

    /**
     * Verifica si debe forzarse certeza baja
     */
    fun debeForzarCertezaBaja(fuenteOficial: Boolean): Boolean {
        return !fuenteOficial
    }
}
