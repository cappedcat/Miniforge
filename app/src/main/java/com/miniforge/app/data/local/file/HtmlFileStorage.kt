package com.miniforge.app.data.local.file

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HtmlFileStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dir: File get() = File(context.filesDir, "miniapps").also { it.mkdirs() }

    fun save(id: String, html: String): String {
        val file = File(dir, "$id.html")
        file.writeText(html, Charsets.UTF_8)
        return file.absolutePath
    }

    fun read(path: String): String? = runCatching { File(path).readText(Charsets.UTF_8) }.getOrNull()

    fun delete(path: String) { File(path).delete() }
}
