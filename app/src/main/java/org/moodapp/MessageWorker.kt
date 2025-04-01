package org.moodapp

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageWorker(appContext: android.content.Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            val messageDao = database.messageDao()

            val message = MessageEntity(
                sender = "Worker",
                text = "Test message from Worker",
                timestamp = System.currentTimeMillis()
            )
            messageDao.insert(message)

            val threshold = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            messageDao.deleteMessagesOlderThan(threshold)

            Result.success()
        }
    }
}