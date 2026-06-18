package com.miniforge.app.data.repository

import com.miniforge.app.data.local.db.dao.AiProviderDao
import com.miniforge.app.data.local.db.entity.AiProviderEntity
import com.miniforge.app.data.local.prefs.SecurePrefs
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.model.ApiFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiProviderRepository @Inject constructor(
    private val dao: AiProviderDao,
    private val securePrefs: SecurePrefs
) {
    fun getAll(): Flow<List<AiProvider>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getDefault(): AiProvider? = dao.getDefault()?.toDomain()

    suspend fun save(provider: AiProvider, apiKey: String) {
        if (provider.isDefault) {
            dao.clearDefault()
        }
        dao.insert(provider.toEntity())
        securePrefs.putApiKey(provider.id, apiKey)
    }

    suspend fun delete(provider: AiProvider) {
        dao.delete(provider.toEntity())
        securePrefs.removeApiKey(provider.id)
    }

    suspend fun setDefault(id: String) {
        dao.clearDefault()
        dao.setDefault(id)
    }

    fun getApiKey(providerId: String): String? = securePrefs.getApiKey(providerId)

    private fun AiProviderEntity.toDomain() = AiProvider(
        id, name, baseUrl,
        if (apiFormat == "anthropic") ApiFormat.ANTHROPIC else ApiFormat.OPENAI,
        model, isDefault
    )

    private fun AiProvider.toEntity() = AiProviderEntity(
        id, name, baseUrl,
        if (apiFormat == ApiFormat.ANTHROPIC) "anthropic" else "openai",
        model, isDefault
    )
}
