package org.moodapp

import androidx.work.Worker
import androidx.work.WorkerParameters

class MessageWorker(appContext: android.content.Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val messageDao = database.messageDao()

        val message = MessageEntity(content = "Test message from Worker")
        messageDao.insert(message)

        return Result.success()
    }
}