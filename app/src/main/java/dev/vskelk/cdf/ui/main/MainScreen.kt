package dev.vskelk.cdf.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vskelk.cdf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSimulator: () -> Unit,
    onNavigateToDiagnosis: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToInvestigator: () -> Unit,
    onNavigateToQuarantine: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vespa", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VespaBackground,
                    titleContentColor = VespaOnSurface
                ),
                actions = {
                    // ✅ AHORA SÍ: Settings conectado correctamente
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = VespaOnSurfaceMid
                        )
                    }
                    IconButton(onClick = { /* Toggle offline mode */ }) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Modo offline",
                            tint = VespaOnSurfaceMid
                        )
                    }
                }
            )
        },
        containerColor = VespaBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de estado del corpus
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VespaSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Corpus v${uiState.corpusVersion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VespaOnSurface
                            )
                            if (uiState.pendientesInvestigador > 0) {
                                Text(
                                    text = "${uiState.pendientesInvestigador} pendientes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VespaWarning
                                )
                            }
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VespaSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Botones Simulador y Diagnóstico
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToSimulator,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simulador")
                    }
                    Button(
                        onClick = onNavigateToDiagnosis,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Diagnóstico")
                    }
                }
            }

            // Campo de búsqueda con ícono de enviar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Consulta al motor experto...", color = VespaOnSurfaceLow) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VespaOutline,
                        unfocusedBorderColor = VespaOutline,
                        focusedTextColor = VespaOnSurface,
                        unfocusedTextColor = VespaOnSurface
                    ),
                    trailingIcon = {
                        IconButton(onClick = { /* Buscar */ }) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = VespaOnSurfaceMid
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Botón Investigador
            item {
                OutlinedButton(
                    onClick = onNavigateToInvestigator,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VespaOnSurface)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Investigador")
                }
            }

            // Sección de resultados recientes
            if (uiState.recientes.isNotEmpty()) {
                item {
                    Text(
                        "Resultados Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        color = VespaOnSurfaceMid
                    )
                }
                items(uiState.recientes) { session ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VespaSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text("Sesión · ${session.modulo}", color = VespaOnSurface)
                        }
                    }
                }
            }
        }
    }
}
