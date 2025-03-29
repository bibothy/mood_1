package org.moodapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        adapter = MessageAdapter(messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        lifecycleScope.launch {
            MoodApp.database.messageDao().getAllMessages().collectLatest { messageList ->
                val oldSize = messages.size
                messages.clear()
                messages.addAll(messageList)
                if (oldSize == 0) {
                    adapter.notifyDataSetChanged() // Полное обновление при первой загрузке
                } else {
                    val newSize = messages.size
                    if (newSize > oldSize) {
                        adapter.notifyItemRangeInserted(oldSize, newSize - oldSize)
                    }
                }
                recyclerView.scrollToPosition(messages.size - 1)
                Log.d("ChatActivity", "Загружено сообщений: ${messages.size}")
            }
        }

        intent.getStringExtra("message")?.let { message ->
            if (!messages.any { it.text == message }) {
                Log.d("ChatActivity", "Добавляю сообщение из intent: $message")
                lifecycleScope.launch {
                    MoodApp.database.messageDao().insert(Message(sender = "Пёс", text = message))
                }
            }
        }
    }
}