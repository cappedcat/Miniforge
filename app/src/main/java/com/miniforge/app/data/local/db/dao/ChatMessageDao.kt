package com.miniforge.app.data.local.db.dao

import androidx.room.*
import com.miniforge.app.data.local.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_history WHERE appId = :appId ORDER BY createdAt ASC")
    fun getForApp(appId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_history WHERE appId = :appId")
    suspend fun deleteForApp(appId: String)
}
