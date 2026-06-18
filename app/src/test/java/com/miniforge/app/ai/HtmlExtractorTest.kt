package com.miniforge.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HtmlExtractorTest {
    private val extractor = HtmlExtractor()

    @Test
    fun `extracts html from well-formed code block`() {
        val input = "Here is your app:\n```html\n<!DOCTYPE html><html></html>\n```\nDone."
        val result = extractor.extract(input)
        assertEquals("<!DOCTYPE html><html></html>", result)
    }

    @Test
    fun `returns null when no html block present`() {
        assertNull(extractor.extract("No code block here"))
    }

    @Test
    fun `returns null when block is unclosed`() {
        assertNull(extractor.extract("```html\n<!DOCTYPE html>"))
    }

    @Test
    fun `handles multiline html correctly`() {
        val html = "<!DOCTYPE html>\n<html>\n<body>hello</body>\n</html>"
        val input = "```html\n$html\n```"
        assertEquals(html, extractor.extract(input))
    }

    @Test
    fun `trims trailing newline before closing backticks`() {
        val input = "```html\n<html/>\n```"
        assertEquals("<html/>", extractor.extract(input))
    }
}
