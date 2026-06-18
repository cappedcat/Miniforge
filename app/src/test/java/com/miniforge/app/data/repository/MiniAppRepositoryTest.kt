package com.miniforge.app.data.repository

import com.miniforge.app.data.local.db.dao.ChatMessageDao
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.entity.MiniAppEntity
import com.miniforge.app.data.local.file.HtmlFileStorage
import com.miniforge.app.data.model.MiniApp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MiniAppRepositoryTest {
    private lateinit var miniAppDao: MiniAppDao
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var htmlFileStorage: HtmlFileStorage
    private lateinit var repo: MiniAppRepository

    @Before
    fun setup() {
        miniAppDao = mockk(relaxed = true)
        chatMessageDao = mockk(relaxed = true)
        htmlFileStorage = mockk()
        repo = MiniAppRepository(miniAppDao, chatMessageDao, htmlFileStorage)
    }

    @Test
    fun `getAll maps entities to domain models`() = runTest {
        every { miniAppDao.getAll() } returns flowOf(listOf(
            MiniAppEntity("id1", "App", "desc", "/path/id1.html", 1L, 1L, null)
        ))
        val apps = repo.getAll().first()
        assertEquals(1, apps.size)
        assertEquals("App", apps[0].name)
    }

    @Test
    fun `save writes html and inserts entity`() = runTest {
        val app = MiniApp("id2", "New", "desc", "", 1L, 1L)
        every { htmlFileStorage.save("id2", "<html/>") } returns "/path/id2.html"
        coEvery { miniAppDao.insert(any()) } returns Unit

        repo.save(app, "<html/>")

        coVerify { miniAppDao.insert(match { it.id == "id2" && it.htmlFilePath == "/path/id2.html" }) }
    }

    @Test
    fun `delete removes html file and entity`() = runTest {
        val app = MiniApp("id3", "Del", "", "/path/id3.html", 1L, 1L)
        every { htmlFileStorage.delete("/path/id3.html") } returns Unit
        coEvery { miniAppDao.delete(any()) } returns Unit

        repo.delete(app)

        coVerify { htmlFileStorage.delete("/path/id3.html") }
        coVerify { miniAppDao.delete(any()) }
    }
}
