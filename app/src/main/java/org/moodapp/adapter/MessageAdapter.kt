package org.moodapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.R // Импорт R для доступа к layout
import org.moodapp.database.MessageEntity // Импорт MessageEntity
import java.text.SimpleDateFormat
import java.util.*

// ИСПРАВЛЕНО: Используем ListAdapter для лучшей производительности с DiffUtil
class MessageAdapter : ListAdapter<MessageEntity, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // Форматтер для времени сообщения (можно настроить)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // ViewHolder для отображения одного сообщения
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // УБЕДИТЕСЬ, что эти ID есть в вашем item_message.xml
        val senderTextView: TextView = view.findViewById(R.id.sender_text_view)
        val messageTextView: TextView = view.findViewById(R.id.message_text_view)
        val timestampTextView: TextView = view.findViewById(R.id.timestamp_text_view) // Добавлено время
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // УБЕДИТЕСЬ, что layout файл называется item_message.xml в res/layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // Получаем элемент с помощью getItem (метод ListAdapter)
        val message = getItem(position)
        holder.senderTextView.text = message.sender
        holder.messageTextView.text = message.text
        // Отображаем отформатированное время
        holder.timestampTextView.text = timeFormatter.format(Date(message.timestamp))
    }

    // Метод для обновления списка (не нужен при использовании ListAdapter.submitList)
    // fun updateMessages(...) // УБРАНО

}

// DiffUtil Callback для эффективного обновления списка
class MessageDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
    override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
        // ID уникален для каждого сообщения
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
        // Сравниваем содержимое, если ID совпали
        return oldItem == newItem
    }
}