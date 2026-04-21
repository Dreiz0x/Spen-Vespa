package dev.vskelk.cdf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.vskelk.cdf.ui.navigation.AppNavGraph
import dev.vskelk.cdf.ui.theme.VespaTheme

/**
 * MainActivity - Host de la interfaz de usuario
 *
 * Configura el tema oscuro permanente de Vespa y lanza el grafo
 * de navegación principal.
 *
 * No contiene lógica de negocio - solo coordina la UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VespaTheme {
                // Modo oscuro es el único modo permitido per spec
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
