package org.moodapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // ИСПРАВЛЕНО: Добавлен suspend. OnConflictStrategy.REPLACE полезен, если могут прийти дубликаты.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    // ИСПРАВЛЕНО: Добавлена сортировка по времени. Возвращает Flow для ChatActivity.
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    // ДОБАВЛЕНО: Метод для получения списка (не Flow), нужен для Worker. Помечен как suspend.
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<MessageEntity>

    // ДОБАВЛЕНО: Метод для удаления старых сообщений, нужен для Worker. Помечен как suspend.
    // Параметр :timestamp используется в запросе.
    @Query("DELETE FROM messages WHERE timestamp < :timestamp")
    suspend fun deleteMessagesOlderThan(timestamp: Long): Int // Возвращает количество удаленных строк

    // (Опционально) Метод для удаления всех сообщений
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}