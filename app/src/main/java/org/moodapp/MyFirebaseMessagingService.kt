package org.moodapp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.cancel

private const val TAG = "FirebaseMessage"


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var messageDao: MessageDao


    override fun onCreate() {
        super.onCreate()
        messageDao = AppDatabase.getDatabase(applicationContext).messageDao()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message: ${remoteMessage.data}")
        if (remoteMessage.data.isNotEmpty() && remoteMessage.data["message"] != null) {
            val messageText = remoteMessage.data["message"]
            messageText?.let {
                val messageEntity = MessageEntity(message = messageText)
                GlobalScope.launch(Dispatchers.IO) {
                    messageDao.insertMessage(messageEntity)
                    Log.d(TAG, "Message saved to database: $messageText")
                }
                sendNotification(it)
                Log.d(TAG,"Message sent: $messageText")
            }
        }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val channelId = AppConstants.MOOD_CHANNEL
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationId = 1

        val openResponseIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("open_response", "response")
        }
        val openResponsePendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, openResponseIntent, PendingIntent.FLAG_IMMUTABLE)

        val likeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("like", "like")
        }
        val likePendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, likeIntent, PendingIntent.FLAG_IMMUTABLE)

        val dislikeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("dislike", "dislike")
        }
        val dislikePendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, dislikeIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Новое сообщение!")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(NotificationCompat.Action(0, "Ответить", openResponsePendingIntent))
            .addAction(NotificationCompat.Action(0, "Лайк", likePendingIntent))
            .addAction(NotificationCompat.Action(0, "Дизлайк", dislikePendingIntent))

        NotificationManagerCompat.from(this).notify(notificationId, notificationBuilder.build())
    }
    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.cancel()
        Log.d(TAG, "FirebaseMessagingService destroyed, scope cancelled.")
    }

}
