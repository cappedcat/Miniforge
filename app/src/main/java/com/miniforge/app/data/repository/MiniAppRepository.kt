package com.miniforge.app.data.repository

import com.miniforge.app.data.local.db.dao.ChatMessageDao
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.entity.MiniAppEntity
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.model.MiniApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiniAppRepository @Inject constructor(
    private val miniAppDao: MiniAppDao,
    private val chatMessageDao: ChatMessageDao,
    private val htmlFileStorage: HtmlFileStorage
) {
    fun getAll(): Flow<List<MiniApp>> =
        miniAppDao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): MiniApp? = miniAppDao.getById(id)?.toDomain()

    suspend fun save(app: MiniApp, html: String): MiniApp {
        val path = htmlFileStorage.save(app.id, html)
        val entity = app.toEntity(path)
        miniAppDao.insert(entity)
        return app.copy(htmlFilePath = path)
    }

    suspend fun updateHtml(id: String, htmlPath: String) {
        miniAppDao.updateHtml(id, htmlPath, System.currentTimeMillis())
    }

    suspend fun delete(app: MiniApp) {
        htmlFileStorage.delete(app.htmlFilePath)
        chatMessageDao.deleteForApp(app.id)
        miniAppDao.delete(app.toEntity(app.htmlFilePath))
    }

    private fun MiniAppEntity.toDomain() = MiniApp(id, name, description, htmlFilePath, createdAt, updatedAt, marketplaceId)
    private fun MiniApp.toEntity(path: String) = MiniAppEntity(id, name, description, path, createdAt, updatedAt, marketplaceId)
}
