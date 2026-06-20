package com.miniforge.app.ui.myapps

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.model.MiniApp
import com.miniforge.app.data.repository.MiniAppRepository
import com.miniforge.app.server.LocalServer
import com.miniforge.app.server.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.NetworkInterface
import javax.inject.Inject

data class MyAppsUiState(
    val apps: List<MiniApp> = emptyList(),
    val isLoading: Boolean = true,
    val deleteConfirmApp: MiniApp? = null,
    val isServerRunning: Boolean = false,
    val serverPort: Int = 0,
    val deviceIp: String = "localhost",
    val shareQrApp: MiniApp? = null,
    val shareQrBitmap: Bitmap? = null
)

@HiltViewModel
class MyAppsViewModel @Inject constructor(
    private val repository: MiniAppRepository,
    private val miniAppDao: MiniAppDao,
    private val fileStorage: HtmlFileStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAppsUiState())
    val uiState: StateFlow<MyAppsUiState> = _uiState.asStateFlow()

    private var localServer: LocalServer? = null

    init {
        viewModelScope.launch {
            repository.getAll().collect { apps ->
                _uiState.value = _uiState.value.copy(apps = apps, isLoading = false)
            }
        }
        _uiState.value = _uiState.value.copy(deviceIp = getDeviceIp())
    }

    fun toggleServer() {
        if (_uiState.value.isServerRunning) stopServer() else startServer()
    }

    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val server = LocalServer(miniAppDao, fileStorage)
                server.start()
                localServer = server
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isServerRunning = true,
                        serverPort = 8080
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("MyAppsVM", "Failed to start server", e)
            }
        }
    }

    private fun stopServer() {
        localServer?.stop()
        localServer = null
        _uiState.value = _uiState.value.copy(isServerRunning = false, serverPort = 0)
    }

    fun shareApp(app: MiniApp) {
        val ip = _uiState.value.deviceIp
        val port = _uiState.value.serverPort
        val url = "http://$ip:$port/${app.id}"
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = QrCodeGenerator.generate(url)
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(shareQrApp = app, shareQrBitmap = bitmap)
            }
        }
    }

    fun dismissShareQr() {
        _uiState.value = _uiState.value.copy(shareQrApp = null, shareQrBitmap = null)
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
        viewModelScope.launch { repository.delete(app) }
    }

    override fun onCleared() {
        super.onCleared()
        localServer?.stop()
    }

    private fun getDeviceIp(): String = try {
        NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress.contains('.') }
            ?.hostAddress ?: "localhost"
    } catch (e: Exception) {
        "localhost"
    }
}
