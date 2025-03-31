package org.moodapp

import android.content.* // Убираем неиспользуемый import BroadcastReceiver
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // Импортируем Observer
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.moodapp.adapter.MessageAdapter
import org.moodapp.database.MessageDao
// import org.moodapp.database.MessageEntity // Можно убрать, если не используется напрямую здесь

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageDao: MessageDao // DAO для доступа к сообщениям
    private val TAG = "ChatActivity"

    // УБИРАЕМ СТАРЫЙ BroadcastReceiver, так как LiveData будет обновлять список

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // УБЕДИТЕСЬ, что layout называется activity_chat.xml
        setContentView(R.layout.activity_chat)

        // Получаем DAO из Application класса
        messageDao = (application as MoodApplication).database.messageDao()

        // УБЕДИТЕСЬ, что ID RecyclerView в activity_chat.xml именно chat_recycler_view
        recyclerView = findViewById(R.id.chat_recycler_view)
        // Начинаем с пустого списка, LiveData его заполнит
        messageAdapter = MessageAdapter(mutableListOf())

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            // Опционально: чтобы новые сообщения появлялись снизу
            stackFromEnd = true
        }
        recyclerView.adapter = messageAdapter

        // Наблюдаем за изменениями в базе данных через LiveData
        // ИСПРАВЛЕНО: Используем this как LifecycleOwner
        messageDao.getAllMessages().asLiveData().observe(this, Observer { messages ->
            // Когда данные в БД меняются, этот код выполняется
            Log.d(TAG, "LiveData observed ${messages.size} messages from DB.")
            messageAdapter.updateMessages(messages) // Обновляем список в адаптере

            // Прокрутка к последнему сообщению только если адаптер не пустой
            if (messages.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size - 1) // Плавная прокрутка
            }
        })

        Log.d(TAG, "ChatActivity created and LiveData observer set up.")
    }

    override fun onResume() {
        super.onResume()
        // УБИРАЕМ РЕГИСТРАЦИЮ BroadcastReceiver
        Log.d(TAG,"ChatActivity resumed.")
        // Можно прокрутить вниз при возобновлении, если список не пуст
        if (messageAdapter.itemCount > 0) {
            recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    override fun onPause() {
        super.onPause()
        // УБИРАЕМ ОТМЕНУ РЕГИСТРАЦИИ BroadcastReceiver
        Log.d(TAG,"ChatActivity paused.")
    }
}