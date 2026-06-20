package com.miniforge.app.server

import android.util.Log
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.file.HtmlFileStorage
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalServer(
    private val miniAppDao: MiniAppDao,
    private val fileStorage: HtmlFileStorage,
    port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.trimEnd('/')

        return when {
            uri == "" || uri == "/" -> serveAppList()
            uri.endsWith("/manifest.json") -> {
                val appId = uri.removeSuffix("/manifest.json").trimStart('/')
                serveManifest(appId)
            }
            else -> {
                val appId = uri.trimStart('/')
                serveApp(appId)
            }
        }
    }

    private fun serveAppList(): Response {
        return try {
            val apps = runBlocking { miniAppDao.getAll().first() }
            val list = apps.map { mapOf("id" to it.id, "name" to it.name, "description" to it.description) }
            val json = Json.encodeToString(list)
            newFixedLengthResponse(Response.Status.OK, "application/json", json)
                .also { it.addHeader("Access-Control-Allow-Origin", "*") }
        } catch (e: Exception) {
            Log.e("LocalServer", "Error listing apps", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }

    private fun serveApp(appId: String): Response {
        return try {
            val app = runBlocking { miniAppDao.getById(appId) }
                ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "App not found")
            val html = fileStorage.read(app.htmlFilePath)
                ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "HTML file not found")
            newFixedLengthResponse(Response.Status.OK, "text/html", html)
                .also { it.addHeader("Access-Control-Allow-Origin", "*") }
        } catch (e: Exception) {
            Log.e("LocalServer", "Error serving app $appId", e)
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }

    private fun serveManifest(appId: String): Response {
        return try {
            val app = runBlocking { miniAppDao.getById(appId) }
                ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "App not found")
            val json = Json.encodeToString(mapOf("id" to appId, "name" to app.name, "description" to app.description))
            newFixedLengthResponse(Response.Status.OK, "application/json", json)
                .also { it.addHeader("Access-Control-Allow-Origin", "*") }
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }
}
