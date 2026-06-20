package com.miniforge.app.ui.runner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.repository.MiniAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class RunnerState(
    val appName: String = "",
    val html: String? = null,
    val error: String? = null,
    val exportMessage: String? = null
)

@HiltViewModel
class AppRunnerViewModel @Inject constructor(
    private val repo: MiniAppRepository,
    private val fileStorage: HtmlFileStorage,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
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

    fun exportHtml() {
        val html = _state.value.html ?: return
        val appName = _state.value.appName.ifBlank { "miniapp" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")

        viewModelScope.launch {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, "$appName.html")
                file.writeText(html)

                // Share via intent
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share $appName").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                _state.value = _state.value.copy(exportMessage = "Saved to Downloads/$appName.html")
            } catch (e: Exception) {
                _state.value = _state.value.copy(exportMessage = "Export failed: ${e.message}")
            }
        }
    }

    fun clearExportMessage() {
        _state.value = _state.value.copy(exportMessage = null)
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
