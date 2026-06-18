package com.miniforge.app.ui.myapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.model.MiniApp
import com.miniforge.app.data.repository.MiniAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyAppsUiState(
    val apps: List<MiniApp> = emptyList(),
    val isLoading: Boolean = true,
    val deleteConfirmApp: MiniApp? = null
)

@HiltViewModel
class MyAppsViewModel @Inject constructor(
    private val repository: MiniAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAppsUiState())
    val uiState: StateFlow<MyAppsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { apps ->
                _uiState.value = _uiState.value.copy(
                    apps = apps,
                    isLoading = false
                )
            }
        }
    }

    fun requestDelete(app: MiniApp) {
        _uiState.value = _uiState.value.copy(deleteConfirmApp = app)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(deleteConfirmApp = null)
    }

    fun confirmDelete() {
        val app = _uiState.value.deleteConfirmApp ?: return
        _uiState.value = _uiState.value.copy(deleteConfirmApp = null)
        viewModelScope.launch {
            repository.delete(app)
        }
    }
}
