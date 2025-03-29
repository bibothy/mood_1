package org.moodapp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MessageWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val message = inputData.getString("message")
        if (message != null) {
            Log.d("MessageWorker", "Сохраняю сообщение: $message")
            MoodApp.database.messageDao().insert(Message(sender = "Пёс", text = message))
            return Result.success()
        }
        Log.w("MessageWorker", "Сообщение было null")
        return Result.failure()
    }
}