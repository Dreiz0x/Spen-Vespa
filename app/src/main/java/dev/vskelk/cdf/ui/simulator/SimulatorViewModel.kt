package dev.vskelk.cdf.ui.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vskelk.cdf.core.database.entity.ReactivoModulo
import dev.vskelk.cdf.core.domain.model.ReactivoUI
import dev.vskelk.cdf.core.domain.repository.AdaptiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulatorViewModel @Inject constructor(
    private val adaptiveRepository: AdaptiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimulatorUiState>(SimulatorUiState.Loading)
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    private var sessionId: Long = 0
    private var reactivos: List<ReactivoUI> = emptyList()
    private var currentIndex = 0
    private var correctos = 0

    init {
        loadReactivos()
    }

    private fun loadReactivos() {
        viewModelScope.launch {
            try {
                // Iniciar sesión
                sessionId = adaptiveRepository.startSession(ReactivoModulo.SIMULADOR, null)

                // Cargar reactivos priorizados
                reactivos = adaptiveRepository.getPrioritizedReactivos(
                    limit = 10,
                    modulo = ReactivoModulo.SIMULADOR
                )

                if (reactivos.isEmpty()) {
                    _uiState.value = SimulatorUiState.Empty
                } else {
                    _uiState.value = SimulatorUiState.Active(
                        currentReactivo = reactivos[0],
                        currentIndex = 0,
                        totalCount = reactivos.size,
                        selectedOptionId = null,
                        showResult = false,
                        progress = 0f,
                        isLast = reactivos.size == 1
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SimulatorUiState.Empty
            }
        }
    }

    fun selectOption(optionId: Long) {
        val currentState = _uiState.value
        if (currentState !is SimulatorUiState.Active) return

        val selectedOption = currentState.currentReactivo.opciones.find { it.id == optionId } ?: return

        // Registrar respuesta
        viewModelScope.launch {
            adaptiveRepository.recordAnswer(
                sessionId = sessionId,
                reactivoId = currentState.currentReactivo.id,
                selectedOptionId = optionId,
                isCorrect = selectedOption.isCorrect,
                tiempoRespuestaMs = 0, // TODO: medir tiempo
                errorType = if (!selectedOption.isCorrect) {
                    selectedOption.distractorTipo
                } else null
            )
        }

        if (selectedOption.isCorrect) correctos++

        _uiState.update {
            currentState.copy(
                selectedOptionId = optionId,
                showResult = true,
                correctos = correctos
            )
        }
    }

    fun nextReactivo() {
        if (currentIndex < reactivos.size - 1) {
            currentIndex++
            _uiState.value = SimulatorUiState.Active(
                currentReactivo = reactivos[currentIndex],
                currentIndex = currentIndex,
                totalCount = reactivos.size,
                selectedOptionId = null,
                showResult = false,
                progress = currentIndex.toFloat() / reactivos.size,
                isLast = currentIndex == reactivos.size - 1
            )
        }
    }

    fun finish() {
        viewModelScope.launch {
            val resultado = adaptiveRepository.completeSession(sessionId)
            _uiState.value = SimulatorUiState.Finished(
                sessionId = sessionId,
                correctos = resultado.correctos,
                total = resultado.totalReactivos,
                precision = resultado.precision
            )
        }
    }
}

sealed interface SimulatorUiState {
    data object Loading : SimulatorUiState
    data object Empty : SimulatorUiState
    data class Active(
        val currentReactivo: ReactivoUI,
        val currentIndex: Int,
        val totalCount: Int,
        val selectedOptionId: Long?,
        val showResult: Boolean,
        val progress: Float,
        val isLast: Boolean,
        val correctos: Int = 0
    ) : SimulatorUiState
    data class Finished(
        val sessionId: Long,
        val correctos: Int,
        val total: Int,
        val precision: Float
    ) : SimulatorUiState
}
