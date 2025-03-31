package org.moodapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import org.moodapp.database.MessageDao // Импорт DAO
import org.moodapp.database.MessageEntity
import kotlin.random.Random // Для уникальных ID уведомлений

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService" // Сократил для логов
    private val serviceJob = Job()
    // ИСПРАВЛЕНО: Используем Dispatchers.IO для операций с БД
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Ленивое получение DAO
    private val messageDao: MessageDao by lazy {
        (applicationContext as MoodApplication).database.messageDao()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // Отправляем токен в MainActivity через Broadcast, чтобы она отправила его боту Пес
        val intent = Intent("send_token_to_telegram") // Действие для Broadcast
        intent.putExtra("fcm_token", token)
        // ИСПРАВЛЕНО: Используем sendBroadcast без явного пакета для простоты
        sendBroadcast(intent)
        Log.d(TAG,"Broadcast 'send_token_to_telegram' sent with new token.")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message Received From: ${remoteMessage.from}")

        // Обработка Data Payload (предпочитаемый способ)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val sender = remoteMessage.data["sender"] ?: "Пес" // Отправитель по умолчанию
            val text = remoteMessage.data["text"] ?: "Пустое сообщение"
            val timestamp = System.currentTimeMillis() // Текущее время

            val messageEntity = MessageEntity(sender = sender, text = text, timestamp = timestamp)

            // Запускаем корутину для записи в БД
            serviceScope.launch {
                try {
                    messageDao.insert(messageEntity)
                    Log.d(TAG, "Message from $sender saved to DB.")

                    // Показываем уведомление из основного потока
                    withContext(Dispatchers.Main) {
                        sendNotification(sender, text) // Показываем уведомление
                    }
                    // Broadcast больше не нужен, так как ChatActivity использует LiveData
                    // val intent = Intent("new_message") ... sendBroadcast(intent) // УБРАНО

                } catch (e: Exception) {
                    Log.e(TAG, "Error saving message to DB", e)
                }
            }
        } else if (remoteMessage.notification != null) {
            // Обработка Notification Payload (если Data Payload пуст)
            Log.d(TAG, "Message Notification Body: ${remoteMessage.notification?.body}")
            val title = remoteMessage.notification?.title ?: "Уведомление"
            val body = remoteMessage.notification?.body ?: "Новое уведомление"
            // Просто показываем уведомление как есть
            sendNotification(title, body)
            // В этом случае сообщение НЕ сохраняется в БД и чате,
            // если только бэкенд не отправляет и data, и notification payload одновременно.
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        // Интент для открытия ChatActivity при клике на уведомление
        val intent = Intent(this, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Флаги для правильной навигации
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */,
            intent,
            // ИСПРАВЛЕНО: Используем FLAG_IMMUTABLE, обязательно для Android 12+
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Используем ID канала из ресурсов strings.xml
        val channelId = getString(R.string.default_notification_channel_id)

        // Строим уведомление
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // ИСПРАВЛЕНО: Убедитесь что иконка ic_notification существует в res/drawable
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // Уведомление исчезнет после клика
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Высокий приоритет
            .setContentIntent(pendingIntent) // Действие по клику

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android O (8.0) и выше (если еще не создан)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Сообщения чата", // Имя канала, видимое пользователю
                NotificationManager.IMPORTANCE_HIGH // Важность для всплывающих уведомлений
            ).apply {
                description = "Уведомления о новых сообщениях от Пса"
                // Можно добавить настройки вибрации, света и т.д.
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Показываем уведомление с уникальным ID
        // ИСПРАВЛЕНО: Используем Random.nextInt() для генерации ID
        val notificationId = Random.nextInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification sent with ID: $notificationId")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отменяем все корутины при уничтожении сервиса
        serviceJob.cancel()
        Log.d(TAG,"FirebaseMessagingService destroyed, scope cancelled.")
    }
}