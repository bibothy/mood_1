package org.moodapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Новый токен: $token")
        val intent = Intent("send_token_to_telegram").apply {
            putExtra("token", token)
        }
        sendBroadcast(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Получено сообщение: ${remoteMessage.data}")

        // Обрабатываем data-сообщение
        val message = remoteMessage.data["message"]
        if (message != null) {
            saveMessage(message)
            sendMessageToChat(message)
        }

        // Обрабатываем notification-сообщение
        remoteMessage.notification?.let {
            val title = it.title ?: "Сообщение от Пса"
            val body = it.body ?: ""
            showNotification(title, body)
        }
    }

    private fun saveMessage(message: String) {
        val prefs = getSharedPreferences("ChatPrefs", MODE_PRIVATE)
        val editor = prefs.edit()

        // Проверяем тип данных под ключом "messages"
        val messages: MutableSet<String> = try {
            prefs.getStringSet("messages", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        } catch (e: ClassCastException) {
            Log.e("FCM", "Ошибка: под ключом 'messages' хранится String вместо Set. Преобразуем данные.")
            val stringValue = prefs.getString("messages", null)
            if (stringValue != null) {
                mutableSetOf(stringValue) // Преобразуем String в Set
            } else {
                editor.remove("messages").apply() // Удаляем некорректные данные
                mutableSetOf()
            }
        }

        messages.add(message)
        editor.putStringSet("messages", messages).apply()
        Log.d("FCM", "Сообщение сохранено: $message")
    }

    private fun sendMessageToChat(message: String) {
        val intent = Intent("new_message").apply {
            putExtra("message", message)
        }
        sendBroadcast(intent)
        Log.d("FCM", "Сообщение отправлено в чат: $message")
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "moodapp_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val channel = NotificationChannel(channelId, "MoodApp Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("message", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d("FCM", "Уведомление показано: $message")
    }
}