package org.moodapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
            if (message != null) {
                Log.d("ChatActivity", "Получил сообщение через Broadcast: $message")
                messages.add(Message("Пёс", message))
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            } else {
                Log.w("ChatActivity", "Сообщение в Broadcast было null")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        loadMessages()
        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        intent.getStringExtra("message")?.let { message ->
            if (!messages.any { it.text == message }) {
                Log.d("ChatActivity", "Добавляю сообщение из intent: $message")
                messages.add(Message("Пёс", message))
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("new_message")
        ContextCompat.registerReceiver(this, messageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        Log.d("ChatActivity", "BroadcastReceiver зарегистрирован")
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(messageReceiver)
        Log.d("ChatActivity", "BroadcastReceiver отменён")
    }

    private fun loadMessages() {
        val prefs = getSharedPreferences("ChatPrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        // Проверяем, что хранится под ключом "messages"
        val messagesSet: Set<String>? = try {
            prefs.getStringSet("messages", emptySet())
        } catch (e: ClassCastException) {
            Log.e("ChatActivity", "Ошибка: под ключом 'messages' хранится String вместо Set. Очищаем данные.")
            // Проверяем, что там String, и конвертируем его в Set
            val stringValue = prefs.getString("messages", null)
            if (stringValue != null) {
                setOf(stringValue) // Преобразуем String в Set с одним элементом
            } else {
                editor.remove("messages").apply() // Удаляем некорректные данные
                emptySet()
            }
        }

        messages.clear()
        if (messagesSet == null || messagesSet.isEmpty()) {
            Log.w("ChatActivity", "Нет сохранённых сообщений в SharedPreferences")
        } else {
            messages.addAll(messagesSet.map { Message("Пёс", it) })
            Log.d("ChatActivity", "Загружено сообщений: ${messages.size}")
        }
    }
}