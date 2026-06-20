package com.miniforge.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.ai.AiService
import com.miniforge.app.ai.GenerationState
import com.miniforge.app.data.local.db.dao.ModelCacheDao
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.repository.AiProviderRepository
import com.miniforge.app.data.repository.MiniAppRepository
import com.miniforge.app.ui.create.ModelFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditAppUiState(
    val appId: String = "",
    val appName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val generationState: GeneratorUiState = GeneratorUiState.Idle,
    val providers: List<AiProvider> = emptyList(),
    val selectedProviderId: String? = null,
    val availableModels: Map<String, List<String>> = emptyMap(),
    val selectedModelId: String? = null,
    val loadingModels: Set<String> = emptySet()
)

@HiltViewModel
class EditAppViewModel @Inject constructor(
    private val repo: MiniAppRepository,
    private val fileStorage: HtmlFileStorage,
    private val aiService: AiService,
    private val providerRepository: AiProviderRepository,
    private val modelFetcher: ModelFetcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAppUiState())
    val uiState: StateFlow<EditAppUiState> = _uiState.asStateFlow()

    private var currentHtml: String = ""

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            try {
                providerRepository.getAll().first().let { providers ->
                    _uiState.value = _uiState.value.copy(providers = providers)
                    if (providers.isNotEmpty()) {
                        selectProvider(providers.firstOrNull { it.isDefault }?.id ?: providers[0].id)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EditAppVM", "Failed to load providers", e)
            }
        }
    }

    fun selectProvider(providerId: String) {
        _uiState.value = _uiState.value.copy(
            selectedProviderId = providerId,
            selectedModelId = null
        )
        fetchModelsForProvider(providerId)
    }

    private fun fetchModelsForProvider(providerId: String) {
        if (_uiState.value.availableModels[providerId] != null) return
        _uiState.value = _uiState.value.copy(
            loadingModels = _uiState.value.loadingModels + providerId
        )
        viewModelScope.launch {
            try {
                val provider = _uiState.value.providers.firstOrNull { it.id == providerId }
                if (provider != null) {
                    val models = modelFetcher.fetchAndCacheModels(provider)
                    if (models.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            availableModels = _uiState.value.availableModels + (providerId to models),
                            selectedModelId = models[0]
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("EditAppVM", "Failed to fetch models", e)
            } finally {
                _uiState.value = _uiState.value.copy(
                    loadingModels = _uiState.value.loadingModels - providerId
                )
            }
        }
    }

    fun selectModel(modelId: String) {
        _uiState.value = _uiState.value.copy(selectedModelId = modelId)
    }

    fun loadApp(appId: String) {
        viewModelScope.launch {
            try {
                val app = repo.getById(appId) ?: run {
                    _uiState.value = _uiState.value.copy(error = "App not found")
                    return@launch
                }

                var html = fileStorage.read(app.htmlFilePath)

                if (html == null || html.isBlank() || !html.contains("<")) {
                    _uiState.value = _uiState.value.copy(error = "Failed to load app HTML")
                    android.util.Log.e("EditAppVM", "Failed to read HTML from: ${app.htmlFilePath}")
                    return@launch
                }

                currentHtml = html
                _uiState.value = EditAppUiState(
                    appId = appId,
                    appName = app.name,
                    isLoading = false,
                    generationState = GeneratorUiState.Ready(html),
                    providers = _uiState.value.providers
                )
            } catch (e: Exception) {
                android.util.Log.e("EditAppVM", "Error loading app", e)
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }

    fun refine(prompt: String) {
        if (prompt.isBlank()) return

        _uiState.value = _uiState.value.copy(
            generationState = GeneratorUiState.Streaming("")
        )

        viewModelScope.launch {
            try {
                // Build a prompt that provides the full current HTML and asks the AI to apply the user's changes.
                // We include the HTML without markdown fences to avoid double‑wrapping.
                val refinementPrompt = "Current app HTML:\n${currentHtml}\n\nUser request: $prompt\n\nPlease return the complete updated HTML wrapped in a single fenced code block (```html ... ```)."

                aiService.generate(
                    refinementPrompt,
                    providerId = _uiState.value.selectedProviderId,
                    modelId = _uiState.value.selectedModelId
                ).collect { state ->
                    when (state) {
                        is GenerationState.Streaming -> {
                            _uiState.value = _uiState.value.copy(
                                generationState = GeneratorUiState.Streaming(state.accumulated)
                            )
                        }
                        is GenerationState.Complete -> {
                            val generatedHtml = state.html
                            if (generatedHtml.isBlank() || !generatedHtml.contains("<")) {
                                _uiState.value = _uiState.value.copy(
                                    generationState = GeneratorUiState.Error("Generated invalid HTML")
                                )
                                return@collect
                            }

                            currentHtml = generatedHtml
                            val app = repo.getById(_uiState.value.appId)
                            if (app != null) {
                                try {
                                    val newPath = fileStorage.save(app.id, generatedHtml)
                                    // Verify the file was actually saved
                                    val verified = fileStorage.read(newPath)
                                    if (verified != null && verified.length > 50 && verified.contains("<")) {
                                        repo.updateHtml(app.id, newPath)
                                        _uiState.value = _uiState.value.copy(
                                            generationState = GeneratorUiState.Ready(generatedHtml)
                                        )
                                    } else {
                                        android.util.Log.e("EditAppVM", "File verification failed: $newPath")
                                        _uiState.value = _uiState.value.copy(
                                            generationState = GeneratorUiState.Error("Failed to save HTML file")
                                        )
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("EditAppVM", "Error saving file", e)
                                    _uiState.value = _uiState.value.copy(
                                        generationState = GeneratorUiState.Error("Error saving: ${e.message}")
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    generationState = GeneratorUiState.Error("App not found")
                                )
                            }
                        }
                        is GenerationState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                generationState = GeneratorUiState.Error(state.message)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    generationState = GeneratorUiState.Error(e.message ?: "Unknown error")
                )
            }
        }
    }
}
