package dev.vskelk.cdf.ui.diagnosis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vskelk.cdf.R
import dev.vskelk.cdf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvestigator: () -> Unit,
    viewModel: DiagnosisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.module_diagnosis)) },
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
        when (val state = uiState) {
            is DiagnosisUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VespaPrimary)
                }
            }
            is DiagnosisUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resumen general
                    item {
                        DiagnosticoSummaryCard(state = state)
                    }

                    // Subtemas débiles
                    if (state.subtemasDebiles.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.diagnosis_weak_topics),
                                style = MaterialTheme.typography.titleMedium,
                                color = VespaOnSurfaceMid
                            )
                        }

                        items(state.subtemasDebiles) { subtema ->
                            SubtemaCard(
                                subtema = subtema,
                                onInvestigate = { viewModel.investigate(subtema.subtema.id) }
                            )
                        }
                    }

                    // Recomendaciones
                    if (state.recomendaciones.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.diagnosis_recommendation),
                                style = MaterialTheme.typography.titleMedium,
                                color = VespaOnSurfaceMid
                            )
                        }

                        items(state.recomendaciones) { rec ->
                            RecomendacionCard(
                                recomendacion = rec,
                                onAction = {
                                    when (rec.tipo) {
                                        dev.vskelk.cdf.core.domain.model.RecomendacionTipo.INVESTIGAR -> onNavigateToInvestigator()
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticoSummaryCard(state: DiagnosisUiState.Content) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VespaSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Diagnóstico de Dominio",
                style = MaterialTheme.typography.titleLarge,
                color = VespaOnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${state.totalSubtemas}",
                    label = "Subtemas",
                    icon = Icons.Default.Category
                )
                StatItem(
                    value = "${state.precisionGeneral.toInt()}%",
                    label = "Precisión",
                    icon = Icons.Default.TrendingUp
                )
                StatItem(
                    value = "${state.subtemasDebiles.size}",
                    label = "Brechas",
                    icon = Icons.Default.Warning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { state.precisionGeneral },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    state.precisionGeneral >= 0.8f -> VespaSuccess
                    state.precisionGeneral >= 0.6f -> VespaWarning
                    else -> VespaError
                },
                trackColor = VespaOutline,
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = VespaOnSurfaceMid, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = VespaOnSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = VespaOnSurfaceMid)
    }
}

@Composable
private fun SubtemaCard(
    subtema: dev.vskelk.cdf.core.domain.model.SubtemaConDominio,
    onInvestigate: () -> Unit
) {
    val estadoColor = when (subtema.estadoDominio) {
        "DOMINADO" -> VespaSuccess
        "EN_CONSOLIDACION" -> VespaWarning
        "INESTABLE" -> VespaWarning
        else -> VespaError
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VespaSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(end = 8.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = estadoColor)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtema.subtema.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VespaOnSurface
                )
                Text(
                    text = "${subtema.precision.toInt()}% - ${subtema.totalIntentos} intentos",
                    style = MaterialTheme.typography.bodySmall,
                    color = VespaOnSurfaceMid
                )
            }

            IconButton(onClick = onInvestigate) {
                Icon(Icons.Default.Search, contentDescription = "Investigar", tint = VespaPrimary)
            }
        }
    }
}

@Composable
private fun RecomendacionCard(
    recomendacion: dev.vskelk.cdf.core.domain.model.Recomendacion,
    onAction: () -> Unit
) {
    Card(
        onClick = onAction,
        colors = CardDefaults.cardColors(containerColor = VespaSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (recomendacion.tipo) {
                    dev.vskelk.cdf.core.domain.model.RecomendacionTipo.INVESTIGAR -> Icons.Default.Search
                    dev.vskelk.cdf.core.domain.model.RecomendacionTipo.REPASAR_FUNDAMENTO -> Icons.Default.MenuBook
                    dev.vskelk.cdf.core.domain.model.RecomendacionTipo.PRACTICAR_MAS -> Icons.Default.FitnessCenter
                    dev.vskelk.cdf.core.domain.model.RecomendacionTipo.AVANZAR_NIVEL -> Icons.Default.TrendingUp
                },
                contentDescription = null,
                tint = VespaPrimary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recomendacion.subtemaNombre,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VespaOnSurface
                )
                Text(
                    text = recomendacion.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = VespaOnSurfaceMid
                )
            }
        }
    }
}
