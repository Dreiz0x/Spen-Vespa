package dev.vskelk.cdf.ui.navigation

/**
 * Routes - Definición de rutas de navegación
 */
object Routes {
    const val SPLASH = "splash"
    const val MAIN = "main"
    const val SIMULATOR = "simulator"
    const val DIAGNOSIS = "diagnosis"
    const val INTERVIEW = "interview"
    const val INVESTIGATOR = "investigator"
    const val QUARANTINE = "quarantine"
    const val SETTINGS = "settings"
    const val CONVERSATION = "conversation/{conversationId}"

    fun conversation(conversationId: Long) = "conversation/$conversationId"
}
