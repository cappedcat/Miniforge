package com.miniforge.app.ai

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HtmlExtractor @Inject constructor() {
    private val openTag = "```html\n"
    private val closeTag = "\n```"

    fun extract(text: String): String? {
        val start = text.indexOf(openTag)
        if (start == -1) return null
        val contentStart = start + openTag.length
        val end = text.indexOf(closeTag, contentStart)
        if (end == -1) return null
        return text.substring(contentStart, end)
    }
}
