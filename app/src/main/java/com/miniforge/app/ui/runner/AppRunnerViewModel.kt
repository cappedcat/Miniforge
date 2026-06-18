package com.miniforge.app.ui.runner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.repository.MiniAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RunnerState(
    val appName: String = "",
    val html: String? = null,
    val error: String? = null
)

@HiltViewModel
class AppRunnerViewModel @Inject constructor(
    private val repo: MiniAppRepository,
    private val fileStorage: HtmlFileStorage,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appId: String = savedStateHandle["appId"] ?: ""

    private val _state = MutableStateFlow(RunnerState())
    val state: StateFlow<RunnerState> = _state.asStateFlow()

    init {
        loadApp()
    }

    fun reload() {
        loadApp()
    }

    private fun loadApp() {
        _state.value = RunnerState()
        viewModelScope.launch {
            try {
                val app = repo.getById(appId)
                if (app == null) {
                    _state.value = RunnerState(error = "App not found (id=$appId)")
                    return@launch
                }
                val html = fileStorage.read(app.htmlFilePath)
                if (html == null) {
                    _state.value = RunnerState(appName = app.name, error = "Could not read app HTML file")
                    return@launch
                }
                _state.value = RunnerState(appName = app.name, html = html)
            } catch (e: Exception) {
                _state.value = RunnerState(error = e.message ?: "Unknown error")
            }
        }
    }
}
