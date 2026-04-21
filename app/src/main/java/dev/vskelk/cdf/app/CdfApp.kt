package dev.vskelk.cdf.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Vespa Application - Punto de entrada de la aplicación
 *
 * Inicializa Hilt para inyección de dependencias y WorkManager para tareas
 * en segundo plano (sincronización, etc.).
 *
 * Siguiendo el principio de Vespa: cada componente tiene responsabilidad
 * clara y única dentro del sistema.
 */
@HiltAndroidApp
class CdfApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            // Habilitar migración destructiva durante desarrollo
            // Según spec: FallbackToDestructiveMigrationFrom(1) durante desarrollo
            // Migración explícita antes de release
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
