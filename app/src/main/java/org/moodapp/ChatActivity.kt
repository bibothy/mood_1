package org.moodapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

        // Если открыто через уведомление, добавляем сообщение
        intent.getStringExtra("message")?.let { message ->
            if (!messages.any { it.text == message }) {
                messages.add(Message("Пёс", message))
                adapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(messageReceiver, IntentFilter("new_message"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(messageReceiver)
    }

    private fun loadMessages() {
        val prefs = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)
        val messagesSet = prefs.getStringSet("messages", emptySet()) ?: emptySet()
        messages.clear()
        messages.addAll(messagesSet.map { Message("Пёс", it) })
        Log.d("ChatActivity", "Загрузил сообщений: ${messages.size}")
    }
}