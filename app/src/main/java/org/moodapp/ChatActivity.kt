package org.moodapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.adapter.MessageAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageDao: MessageDao // Теперь импортируется
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val database = AppDatabase.getDatabase(this)
        messageDao = database.messageDao()

        recyclerView = findViewById(R.id.recyclerView) // Исправлен ID с учётом activity_chat.xml
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter()
        recyclerView.adapter = adapter

        messageDao.getAllMessages().asLiveData().observe(this) { messages: List<MessageEntity> ->
            adapter.updateMessages(messages)
        }
    }
}