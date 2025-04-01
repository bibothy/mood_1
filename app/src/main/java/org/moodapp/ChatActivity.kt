package org.moodapp

import android.os.Bundle
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
        setContentView(R.layout.activity_chat)

        // Инициализация Room (предполагаем, что у вас есть AppDatabase)
        val database = AppDatabase.getDatabase(this)
        messageDao = database.messageDao()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter() // Пустой конструктор
        recyclerView.adapter = adapter

        // Наблюдение за изменениями в базе данных
        messageDao.getAllMessages().asLiveData().observe(this) { messages ->
            adapter.updateMessages(messages)
        }
    }
}