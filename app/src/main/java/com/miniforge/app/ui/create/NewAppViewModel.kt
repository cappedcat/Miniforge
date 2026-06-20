package com.miniforge.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.repository.AiProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewAppUiState(
    val providers: List<AiProvider> = emptyList(),
    val selectedProviderId: String? = null,
    val selectedModel: String? = null,
    val availableModels: Map<String, List<String>> = emptyMap(),
    val loadingModels: Set<String> = emptySet()
) {
    val effectiveProvider: AiProvider?
        get() = providers.firstOrNull { it.id == selectedProviderId }
            ?: providers.firstOrNull { it.isDefault }
            ?: providers.firstOrNull()

    val providerModels: List<String>?
        get() = effectiveProvider?.id?.let { availableModels[it] }

    val selectedModelOrDefault: String?
        get() = selectedModel ?: providerModels?.firstOrNull() ?: effectiveProvider?.model
}

@HiltViewModel
class NewAppViewModel @Inject constructor(
    private val repo: AiProviderRepository,
    private val modelFetcher: ModelFetcher
) : ViewModel() {

    private val selectedId = MutableStateFlow<String?>(null)
    private val selectedModel = MutableStateFlow<String?>(null)
    private val availableModels = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val loadingModels = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<NewAppUiState> = combine(
        repo.getAll(),
        selectedId,
        selectedModel,
        availableModels,
        loadingModels
    ) { providers, id, model, models, loading ->
        NewAppUiState(
            providers = providers,
            selectedProviderId = id,
            selectedModel = model,
            availableModels = models,
            loadingModels = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NewAppUiState())

    fun selectProvider(id: String) {
        selectedId.value = id
        selectedModel.value = null
        fetchModelsForProvider(id)
    }

    fun selectModel(model: String) {
        selectedModel.value = model
    }

    private fun fetchModelsForProvider(providerId: String) {
        if (availableModels.value[providerId] != null) {
            return // Already fetched
        }

        loadingModels.value = loadingModels.value + providerId
        viewModelScope.launch {
            try {
                val providerList = repo.getAll().first()
                val provider = providerList.firstOrNull { it.id == providerId }
                if (provider != null) {
                    val models = modelFetcher.fetchAndCacheModels(provider)
                    if (models.isNotEmpty()) {
                        availableModels.value = availableModels.value + (providerId to models)
                    }
                }
            } catch (e: Exception) {
                // Silently fail - use default model
            } finally {
                loadingModels.value = loadingModels.value - providerId
            }
        }
    }
}
