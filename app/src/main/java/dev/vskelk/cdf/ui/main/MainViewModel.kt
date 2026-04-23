package dev.vskelk.cdf.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vskelk.cdf.core.domain.model.BootstrapState
import dev.vskelk.cdf.core.domain.repository.AdaptiveRepository
import dev.vskelk.cdf.core.domain.repository.BootstrapRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val adaptiveRepository: AdaptiveRepository,
    private val bootstrapRepository: BootstrapRepository // ⚡ Inyectado para activar la siembra
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // ⚡ Iniciamos la observación del estado de arranque
        checkBootstrap()
        loadStats()
    }

    private fun checkBootstrap() {
        viewModelScope.launch {
            bootstrapRepository.bootstrapState.collect { state ->
                when (state) {
                    is BootstrapState.Seeding -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is BootstrapState.Ready -> {
                        _uiState.update { it.copy(isLoading = false) }
                        loadStats() // Recargamos stats ahora que hay datos
                    }
                    is BootstrapState.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        // Aquí podrías emitir un evento de error a la UI
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = adaptiveRepository.getOverallStats()
                _uiState.update {
                    it.copy(
                        progresoGeneral = (stats.precisionGeneral * 100).toInt(),
                        brechasDetectadas = stats.brechasActivas,
                        sesionesCompletadas = stats.totalSesiones,
                        subtemasDominados = stats.subtemasDominados,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            adaptiveRepository.observeRecentSessions(5).collect { sessions ->
                _uiState.update { 
                    it.copy(recientes = sessions.map { s -> 
                        SessionSummary(s.id, s.correctos, s.totalReactivos, s.modulo) 
                    }) 
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadStats()
    }
}
