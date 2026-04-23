package dev.vskelk.cdf.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vskelk.cdf.core.datastore.PreferencesDataSource
import dev.vskelk.cdf.core.datastore.proto.ProviderProto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        viewModelScope.launch {
            val savedKey = preferencesDataSource.getApiKey("GEMINI")
            if (!savedKey.isNullOrBlank()) {
                _apiKey.value = "••••••••••••" + savedKey.takeLast(4)
            }
        }
    }

    fun saveGeminiKey(key: String) {
        if (key.isBlank()) return
        
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            try {
                preferencesDataSource.setApiKey("GEMINI", key)
                preferencesDataSource.setActiveProvider(ProviderProto.PROVIDER_GEMINI)
                _apiKey.value = "••••••••••••" + key.takeLast(4)
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Error al guardar la llave")
            }
        }
    }

    fun resetStatus() {
        _saveStatus.value = SaveStatus.Idle
    }
}

sealed class SaveStatus {
    object Idle : SaveStatus()
    object Saving : SaveStatus()
    object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}
