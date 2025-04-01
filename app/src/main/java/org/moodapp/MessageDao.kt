package org.moodapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE timestamp < :timestamp")
    suspend fun deleteMessagesOlderThan(timestamp: Long): Int

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}