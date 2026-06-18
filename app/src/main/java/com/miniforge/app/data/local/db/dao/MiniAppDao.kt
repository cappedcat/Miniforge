package com.miniforge.app.data.local.db.dao

import androidx.room.*
import com.miniforge.app.data.local.db.entity.MiniAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiniAppDao {
    @Query("SELECT * FROM mini_apps ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<MiniAppEntity>>

    @Query("SELECT * FROM mini_apps WHERE id = :id")
    suspend fun getById(id: String): MiniAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: MiniAppEntity)

    @Delete
    suspend fun delete(app: MiniAppEntity)

    @Query("UPDATE mini_apps SET updatedAt = :updatedAt, htmlFilePath = :htmlFilePath WHERE id = :id")
    suspend fun updateHtml(id: String, htmlFilePath: String, updatedAt: Long)
}
