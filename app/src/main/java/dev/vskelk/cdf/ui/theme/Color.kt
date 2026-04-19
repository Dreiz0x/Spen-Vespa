package dev.vskelk.cdf.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Vespa Color Palette
 *
 * Paleta monocromática oscura para el modo oscuro permanente.
 * Cada color tiene propósito semántico definido.
 *
 * Per spec: "Modo oscuro permanente. Sin light mode."
 */

// Base - Fondos
val VespaBackground = Color(0xFF0A0A0A)      // Fondo principal
val VespaSurface = Color(0xFF111111)          // Superficies elevadas
val VespaSurfaceVariant = Color(0xFF1A1A1A)   // Variantes de superficie
val VespaOutline = Color(0xFF2A2A2A)          // Bordes y divisores

// Textos
val VespaOnSurface = Color(0xFFE8E8E8)       // Texto principal
val VespaOnSurfaceMid = Color(0xFF888888)    // Texto secundario
val VespaOnSurfaceLow = Color(0xFF444444)     // Texto deshabilitado

// Primario
val VespaPrimary = Color(0xFFFFFFFF)          // Elementos primarios
val VespaOnPrimary = Color(0xFF000000)        // Texto sobre primario

// Semántico - Estados
val VespaError = Color(0xFFCF6679)            // Errores
val VespaErrorContainer = Color(0xFF93000A)   // Contenedor de error
val VespaSuccess = Color(0xFF4CAF7D)          // Éxito/correcto
val VespaSuccessContainer = Color(0xFF1B5E20) // Contenedor de éxito
val VespaWarning = Color(0xFFB8860B)          // Advertencia
val VespaWarningContainer = Color(0xFF5D4204) // Contenedor de advertencia

// Logo
val VespaLogoTint = Color(0xFFCCCCCC)         // Tint para el logo

// Componentes específicos
val VespaCardBackground = Color(0xFF111111)
val VespaChipBackground = Color(0xFF1A1A1A)
val VespaDivider = Color(0xFF2A2A2A)

// Módulo: Área de examen
object ExamAreas {
    val Tecnico = Color(0xFF4A90D9)          // Área técnica
    val Sistema = Color(0xFF7B68EE)           // Área sistema
    val General = Color(0xFF708090)           // Área general
}

// Módulo: Nivel cognitivo
object CognitiveLevels {
    val Conocimiento = Color(0xFF6B8E23)      // Recordar datos
    val Comprension = Color(0xFF4682B4)       // Explicar conceptos
    val Aplicacion = Color(0xFFDAA520)        // Aplicar a casos
    val Analisis = Color(0xFFCD5C5C)          // Distinguir casos
}
