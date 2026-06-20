package com.miniforge.app.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HtmlExtractor @Inject constructor() {

    fun extract(text: String): String? {
        // 1. ```html ... ``` with any whitespace around the tag
        val htmlBlock = Regex("```html\\s*\\n([\\s\\S]*?)\\n```", RegexOption.IGNORE_CASE).find(text)
        if (htmlBlock != null) return htmlBlock.groupValues[1].trim()

        // 2. Any fenced block that contains <html (e.g. ``` without language hint)
        val anyBlock = Regex("```[a-zA-Z]*\\s*\\n([\\s\\S]*?<html[\\s\\S]*?)\\n```", RegexOption.IGNORE_CASE).find(text)
        if (anyBlock != null) return anyBlock.groupValues[1].trim()

        // 3. Raw HTML in the response (no code fence at all)
        val doctypeIdx = text.indexOf("<!DOCTYPE html>", ignoreCase = true)
            .takeIf { it >= 0 }
            ?: text.indexOf("<html", ignoreCase = true).takeIf { it >= 0 }
        if (doctypeIdx != null) {
            val closeIdx = text.lastIndexOf("</html>", ignoreCase = true)
            if (closeIdx > doctypeIdx) {
                return text.substring(doctypeIdx, closeIdx + 7).trim()
            }
        }

        return null
    }
}
