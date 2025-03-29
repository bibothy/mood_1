package org.moodapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TOKEN_KSYUSHA = "7782370418:AAGuPd40pssvAlp7snMlBJ2VaacCXYFrRlM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Новый токен: $token")
        MainActivity.sendTelegramMessage(this, "Новый FCM токен: $token", TOKEN_KSYUSHA)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Получено сообщение: ${remoteMessage.data}")

        val message = remoteMessage.data["message"]
        if (message != null) {
            val workRequest = OneTimeWorkRequestBuilder<MessageWorker>()
                .setInputData(workDataOf("message" to message))
                .build()
            WorkManager.getInstance(this).enqueue(workRequest)
            showNotification("Сообщение от Пса", message)
        }

        remoteMessage.notification?.let {
            val title = it.title ?: "Сообщение от Пса"
            val body = it.body ?: ""
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "moodapp_channel"
        val notificationId = System.currentTimeMillis().toInt()

        val channel = NotificationChannel(
            channelId,
            "MoodApp Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления от MoodApp"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("message", message)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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