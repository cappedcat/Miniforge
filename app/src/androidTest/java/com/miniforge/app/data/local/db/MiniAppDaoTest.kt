package com.miniforge.app.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.entity.MiniAppEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniAppDaoTest {
    private lateinit var db: MiniForgeDatabase
    private lateinit var dao: MiniAppDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniForgeDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.miniAppDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndGetById() = runTest {
        val app = MiniAppEntity(
            id = "id1", name = "Test App", description = "desc",
            htmlFilePath = "/files/miniapps/id1.html",
            createdAt = 1000L, updatedAt = 1000L, marketplaceId = null
        )
        dao.insert(app)
        val loaded = dao.getById("id1")
        assertEquals("Test App", loaded?.name)
    }

    @Test
    fun deleteRemovesApp() = runTest {
        val app = MiniAppEntity(
            id = "id2", name = "Delete Me", description = "",
            htmlFilePath = "/files/miniapps/id2.html",
            createdAt = 1000L, updatedAt = 1000L, marketplaceId = null
        )
        dao.insert(app)
        dao.delete(app)
        assertNull(dao.getById("id2"))
    }

    @Test
    fun getAllFlowEmitsInsertedApps() = runTest {
        dao.insert(MiniAppEntity("a", "A", "", "/a.html", 1L, 1L, null))
        dao.insert(MiniAppEntity("b", "B", "", "/b.html", 2L, 2L, null))
        val apps = dao.getAll().first()
        assertEquals(2, apps.size)
    }
}
