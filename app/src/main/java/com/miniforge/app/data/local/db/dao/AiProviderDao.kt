package com.miniforge.app.data.local.db.dao

import androidx.room.*
import com.miniforge.app.data.local.db.entity.AiProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiProviderDao {
    @Query("SELECT * FROM ai_providers ORDER BY name ASC")
    fun getAll(): Flow<List<AiProviderEntity>>

    @Query("SELECT * FROM ai_providers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): AiProviderEntity?

    @Query("SELECT * FROM ai_providers WHERE id = :id")
    suspend fun getById(id: String): AiProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: AiProviderEntity)

    @Delete
    suspend fun delete(provider: AiProviderEntity)

    @Query("UPDATE ai_providers SET isDefault = 0")
    suspend fun clearDefault()

    @Query("UPDATE ai_providers SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: String)
}
