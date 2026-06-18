package com.miniforge.app.data.local.file

import android.content.Context
import java.io.File

/**
 * Stores and retrieves HTML content as files in app-specific cache directory.
 * Files are automatically cleared when app cache is cleared.
 */
class HtmlFileStorage(private val context: Context) {

    fun saveHtml(filename: String, content: String) {
        val file = File(context.cacheDir, filename)
        file.writeText(content, Charsets.UTF_8)
    }

    fun getHtml(filename: String): String? {
        val file = File(context.cacheDir, filename)
        return if (file.exists()) {
            File(context.cacheDir, filename).readText(Charsets.UTF_8)
        } else {
            null
        }
    }

    fun deleteHtml(filename: String): Boolean {
        val file = File(context.cacheDir, filename)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun clearAllHtml() {
        context.cacheDir.listFiles()?.forEach { it.delete() }
    }
}
