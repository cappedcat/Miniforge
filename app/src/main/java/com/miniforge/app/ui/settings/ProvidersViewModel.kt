package com.miniforge.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.model.ApiFormat
import com.miniforge.app.data.repository.AiProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProviderFormState(
    val name: String = "",
    val baseUrl: String = "",
    val apiFormat: ApiFormat = ApiFormat.OPENAI,
    val model: String = "",
    val apiKey: String = "",
    val isDefault: Boolean = false
)

@HiltViewModel
class ProvidersViewModel @Inject constructor(
    private val repo: AiProviderRepository
) : ViewModel() {

    val providers = repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(form: ProviderFormState) {
        viewModelScope.launch {
            val provider = AiProvider(
                id = UUID.randomUUID().toString(),
                name = form.name,
                baseUrl = form.baseUrl.trimEnd('/'),
                apiFormat = form.apiFormat,
                model = form.model,
                isDefault = form.isDefault
            )
            repo.save(provider, form.apiKey)
        }
    }

    fun delete(provider: AiProvider) {
        viewModelScope.launch { repo.delete(provider) }
    }

    fun setDefault(provider: AiProvider) {
        viewModelScope.launch { repo.setDefault(provider.id) }
    }
}
