package com.miniforge.app.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miniforge.app.data.local.db.dao.AiProviderDao
import com.miniforge.app.data.local.db.entity.AiProviderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiProviderDaoTest {
    private lateinit var db: MiniForgeDatabase
    private lateinit var dao: AiProviderDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniForgeDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.aiProviderDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndGetDefault() = runTest {
        dao.insert(AiProviderEntity("p1", "Claude", "https://api.anthropic.com", "anthropic", "claude-3-5-sonnet-20241022", isDefault = true))
        dao.insert(AiProviderEntity("p2", "OpenAI", "https://api.openai.com", "openai", "gpt-4o", isDefault = false))
        val default = dao.getDefault()
        assertEquals("p1", default?.id)
    }

    @Test
    fun getAllReturnsAllProviders() = runTest {
        dao.insert(AiProviderEntity("p1", "Claude", "https://api.anthropic.com", "anthropic", "claude-3-5-sonnet-20241022", isDefault = true))
        val providers = dao.getAll().first()
        assertEquals(1, providers.size)
    }
}
