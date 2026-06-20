package com.miniforge.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miniforge.app.data.local.db.entity.ModelCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelCacheDao {
    @Query("SELECT * FROM model_cache WHERE providerId = :providerId ORDER BY modelName ASC")
    fun getModels(providerId: String): Flow<List<ModelCacheEntity>>

    @Query("SELECT * FROM model_cache WHERE providerId = :providerId")
    suspend fun getModelsSuspend(providerId: String): List<ModelCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<ModelCacheEntity>)

    @Query("DELETE FROM model_cache WHERE providerId = :providerId AND lastUpdated < :beforeTime")
    suspend fun deleteOldModels(providerId: String, beforeTime: Long)

    @Query("DELETE FROM model_cache WHERE providerId = :providerId")
    suspend fun deleteAllForProvider(providerId: String)

    @Query("SELECT COUNT(*) FROM model_cache WHERE providerId = :providerId")
    suspend fun getModelCount(providerId: String): Int
}
