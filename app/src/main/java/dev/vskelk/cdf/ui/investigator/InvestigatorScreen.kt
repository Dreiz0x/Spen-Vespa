package dev.vskelk.cdf.ui.investigator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.vskelk.cdf.R
import dev.vskelk.cdf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestigatorScreen(
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.investigator_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VespaBackground,
                    titleContentColor = VespaOnSurface
                )
            )
        },
        containerColor = VespaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Campo de búsqueda
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.investigator_query_hint),
                        color = VespaOnSurfaceLow
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VespaPrimary,
                    unfocusedBorderColor = VespaOutline,
                    cursorColor = VespaPrimary,
                    focusedTextColor = VespaOnSurface,
                    unfocusedTextColor = VespaOnSurface
                ),
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Investigar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = VespaPrimary)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Información del módulo
            Card(
                colors = CardDefaults.cardColors(containerColor = VespaSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = VespaPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-Investigador",
                            style = MaterialTheme.typography.titleMedium,
                            color = VespaOnSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ingresa un tema electoral y la IA investigará fuentes oficiales para ti. Los resultados pasarán por un proceso de validación antes de ser agregados a tu corpus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VespaOnSurfaceMid
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fuentes válidas
                    Text(
                        text = "Fuentes válidas:",
                        style = MaterialTheme.typography.labelMedium,
                        color = VespaOnSurfaceMid
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("LEGIPE", "Reglamento INE", "Acuerdos CG", "TEPJF").forEach { fuente ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = VespaSuccess
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fuente,
                                style = MaterialTheme.typography.bodySmall,
                                color = VespaOnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
