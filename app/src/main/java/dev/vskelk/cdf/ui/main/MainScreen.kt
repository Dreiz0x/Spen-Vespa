package dev.vskelk.cdf.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
    onNavigateToInterview: () -> Unit,   // ⚡ RESTAURADO
    onNavigateToInvestigator: () -> Unit,
    onNavigateToQuarantine: () -> Unit,  // ⚡ RESTAURADO
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Vespa", style = MaterialTheme.typography.titleLarge) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Corpus v${uiState.corpusVersion}", color = VespaOnSurface)
                        if (uiState.pendientesInvestigador > 0) {
                            Text("${uiState.pendientesInvestigador} pendientes", color = VespaWarning)
                        }
                    }
                }
            }
            
            items(uiState.recientes) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp)) {
                        Text(session.modulo, Modifier.weight(1f))
                        Text("${session.correctos}/${session.total}")
                    }
                }
            }
        }
    }
}
