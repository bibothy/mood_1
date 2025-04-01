package org.moodapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages") // Явно указываем имя таблицы
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) // Автогенерация ID
    val id: Long = 0,
    val sender: String,
    val text: String,
    val timestamp: Long // Время получения/создания сообщения
)