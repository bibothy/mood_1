package org.moodapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages")
    fun getAllMessages(): List<MessageEntity>

    @Insert()
    fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    fun deleteAllMessages()
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    fun deleteMessageById(messageId: Int)
}