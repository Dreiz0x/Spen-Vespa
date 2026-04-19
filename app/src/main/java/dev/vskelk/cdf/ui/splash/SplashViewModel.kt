package dev.vskelk.cdf.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vskelk.cdf.core.domain.model.BootstrapState
import dev.vskelk.cdf.core.domain.repository.BootstrapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SplashViewModel - ViewModel para la pantalla de bienvenida
 *
 * Per spec: BootstrapState.Ready → botón COMENZAR aparece
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val bootstrapRepository: BootstrapRepository
) : ViewModel() {

    private val _bootstrapState = MutableStateFlow<BootstrapState>(BootstrapState.Checking)
    val bootstrapState: StateFlow<BootstrapState> = _bootstrapState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            bootstrapRepository.bootstrapState.collect { state ->
                _bootstrapState.value = state
            }
        }
    }

    fun retry() {
        _bootstrapState.value = BootstrapState.Checking
        viewModelScope.launch {
            bootstrapRepository.initialize()
        }
    }
}
