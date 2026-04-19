package dev.vskelk.cdf.ui.diagnosis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vskelk.cdf.core.domain.model.*
import dev.vskelk.cdf.core.domain.repository.OntologyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    private val ontologyRepository: OntologyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Loading)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    init {
        loadDiagnostico()
    }

    private fun loadDiagnostico() {
        viewModelScope.launch {
            try {
                val diagnostico = ontologyRepository.getDiagnostico()
                _uiState.value = DiagnosisUiState.Content(
                    diagnostico = diagnostico,
                    subtemasDebiles = diagnostico.subtemasDebiles,
                    precisionGeneral = diagnostico.precisionGeneral,
                    totalSubtemas = diagnostico.totalSubtemas,
                    recomendaciones = diagnostico.recomendaciones
                )
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Content(
                    diagnostico = DiagnosticoResult(0, emptyList(), emptyMap(), 0f, emptyList()),
                    subtemasDebiles = emptyList(),
                    precisionGeneral = 0f,
                    totalSubtemas = 0,
                    recomendaciones = emptyList()
                )
            }
        }
    }

    fun investigate(subtemaId: Long) {
        // TODO: Navegar al investigador con el subtema preconfigurado
    }
}

sealed interface DiagnosisUiState {
    data object Loading : DiagnosisUiState
    data class Content(
        val diagnostico: DiagnosticoResult,
        val subtemasDebiles: List<SubtemaConDominio>,
        val precisionGeneral: Float,
        val totalSubtemas: Int,
        val recomendaciones: List<Recomendacion>
    ) : DiagnosisUiState
}
