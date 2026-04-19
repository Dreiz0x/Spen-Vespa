package dev.vskelk.cdf.ui.interview

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
fun InterviewScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.module_interview)) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = VespaPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Preparación por Competencias",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VespaOnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = VespaSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Esta función estará disponible pronto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VespaOnSurfaceMid
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "El módulo de entrevista preparará preguntas situacionales basadas en el catálogo SPEN 2024, contextualizadas con tu historial de errores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VespaOnSurfaceLow
                        )
                    }
                }
            }
        }
    }
}
