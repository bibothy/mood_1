package org.moodapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.adapter.MessageAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageDao: MessageDao
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChatActivity", "onCreate called")
        setContentView(R.layout.activity_chat)

        val database = AppDatabase.getDatabase(this)
        messageDao = database.messageDao()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter()
        recyclerView.adapter = adapter

        messageDao.getAllMessages().asLiveData().observe(this) { messages: List<MessageEntity> ->
            Log.d("ChatActivity", "Observing messages: ${messages.size}")
            adapter.updateMessages(messages)
            Log.d("ChatActivity", "Messages updated in adapter")
        }
    }
}