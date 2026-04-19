package dev.vskelk.cdf.ui.simulator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.vskelk.cdf.R
import dev.vskelk.cdf.ui.theme.*

/**
 * SimulatorScreen - Pantalla del Simulador
 *
 * Per spec:
 * - Estados: Loading (skeleton), Empty, Active, Finished
 * - Muestra reactivo + opciones
 * - Evalúa y muestra fundamento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    onNavigateBack: () -> Unit,
    viewModel: SimulatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.module_simulator)) },
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
        ) {
            when (val state = uiState) {
                is SimulatorUiState.Loading -> LoadingContent()
                is SimulatorUiState.Empty -> EmptyContent()
                is SimulatorUiState.Active -> ActiveContent(
                    state = state,
                    onSelectOption = viewModel::selectOption,
                    onNext = viewModel::nextReactivo,
                    onFinish = viewModel::finish
                )
                is SimulatorUiState.Finished -> FinishedContent(
                    state = state,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VespaPrimary)
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Quiz,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = VespaOnSurfaceLow
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.state_empty_investigator),
                style = MaterialTheme.typography.bodyLarge,
                color = VespaOnSurfaceMid,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActiveContent(
    state: SimulatorUiState.Active,
    onSelectOption: (Long) -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val reactivo = state.currentReactivo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progreso
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth(),
            color = VespaPrimary,
            trackColor = VespaOutline,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.simulator_question, state.currentIndex + 1, state.totalCount),
            style = MaterialTheme.typography.labelMedium,
            color = VespaOnSurfaceMid
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Enunciado
        Card(
            colors = CardDefaults.cardColors(containerColor = VespaSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = reactivo.enunciado,
                style = MaterialTheme.typography.bodyLarge,
                color = VespaOnSurface,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(reactivo.opciones) { index, option ->
                val isSelected = state.selectedOptionId == option.id
                val isCorrect = option.isCorrect
                val showResult = state.showResult

                val backgroundColor = when {
                    showResult && isCorrect -> VespaSuccessContainer
                    showResult && isSelected && !isCorrect -> VespaErrorContainer
                    isSelected -> VespaSurfaceVariant
                    else -> VespaSurface
                }

                val borderColor = when {
                    showResult && isCorrect -> VespaSuccess
                    showResult && isSelected && !isCorrect -> VespaError
                    isSelected -> VespaPrimary
                    else -> VespaOutline
                }

                Card(
                    onClick = { if (!showResult) onSelectOption(option.id) },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { if (!showResult) onSelectOption(option.id) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (showResult && isCorrect) VespaSuccess
                                else if (showResult && isSelected && !isCorrect) VespaError
                                else VespaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VespaOnSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (showResult && isCorrect) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = VespaSuccess
                            )
                        } else if (showResult && isSelected && !isCorrect) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = VespaError
                            )
                        }
                    }
                }
            }
        }

        // Fundamento (mostrado después de responder)
        AnimatedVisibility(
            visible = state.showResult,
            enter = expandVertically() + fadeIn()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = VespaSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.simulator_fundamento),
                        style = MaterialTheme.typography.labelMedium,
                        color = VespaOnSurfaceMid
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (reactivo.citaTextual != null) {
                        Text(
                            text = "\"${reactivo.citaTextual}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VespaOnSurface
                        )
                    }
                    reactivo.opciones.find { it.id == state.selectedOptionId }?.explicacion?.let { expl ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = expl,
                            style = MaterialTheme.typography.bodySmall,
                            color = VespaOnSurfaceMid
                        )
                    }
                }
            }
        }

        // Botón siguiente
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = if (state.isLast) onFinish else onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VespaPrimary,
                contentColor = VespaOnPrimary
            ),
            enabled = state.showResult
        ) {
            Text(
                text = if (state.isLast) stringResource(R.string.simulator_finish)
                else stringResource(R.string.simulator_next)
            )
        }
    }
}

@Composable
private fun FinishedContent(
    state: SimulatorUiState.Finished,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                if (state.precision >= 0.8f) Icons.Default.EmojiEvents
                else if (state.precision >= 0.6f) Icons.Default.ThumbUp
                else Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = when {
                    state.precision >= 0.8f -> VespaSuccess
                    state.precision >= 0.6f -> VespaWarning
                    else -> VespaError
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.simulator_results_title),
                style = MaterialTheme.typography.headlineMedium,
                color = VespaOnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.simulator_results_correct, state.correctos, state.total),
                style = MaterialTheme.typography.titleLarge,
                color = VespaOnSurfaceMid
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VespaPrimary,
                    contentColor = VespaOnPrimary
                )
            ) {
                Text("Volver al inicio")
            }
        }
    }
}
