package org.moodapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.moodapp.databinding.ActivityChatBinding
class ChatActivity : AppCompatActivity() {

    private lateinit var messageDao: MessageDao
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_in)
        Log.d("ChatActivity", "onCreate called")

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = binding.progressBar
        appDatabase = AppDatabase.getDatabase(this)
        messageDao = appDatabase.messageDao()

        adapter = MessageAdapter(mutableListOf()) { messageId ->
            deleteMessage(messageId)
        }
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messageRecyclerView.adapter = adapter

        loadMessages()
        binding.deleteAllMessagesButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                messageDao.deleteAllMessages()
                loadMessages()
            }
        }
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                binding.messageEditText.text.clear()
                addMessage(MessageEntity(message = message))
                lifecycleScope.launch {
                    delay(500)
                    TelegramApiHelper.sendTelegramMessage(this@ChatActivity, "Ответ от Ксюши: $message", AppConstants.TOKEN_PES)
                }
            }
        }
        handleIntent()
    }
    private fun deleteMessage(messageId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.deleteMessageById(messageId)
            val messages = messageDao.getAllMessages()
            runOnUiThread {
                adapter.updateData(messages)
                binding.messageRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val messages = withContext(Dispatchers.IO) {
                messageDao.getAllMessages()
            }
            runOnUiThread {
                adapter.updateData(messages)
                binding.messageRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
            hideProgressBar() 
        }
    }

    fun addMessage(message: MessageEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertMessage(message)
            val messages = messageDao.getAllMessages()
            runOnUiThread {
                adapter.updateData(messages)
                binding.messageRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun hideProgressBar() {
        progressBar.animate().alpha(0f).setDuration(500).withEndAction { progressBar.visibility = View.GONE }
    }
    private fun handleIntent() {
        intent?.getStringExtra("open_response")?.let {
            lifecycleScope.launch{
                delay(500)
                TelegramApiHelper.sendTelegramMessage(this@ChatActivity, "Ксюша хочет ответить!", AppConstants.TOKEN_PES)}
        }
    }
}
