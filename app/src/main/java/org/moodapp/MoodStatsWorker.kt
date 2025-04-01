package org.moodapp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit // Для времени

class MoodStatsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = "MoodStatsWorker"
    // Получаем DAO через applicationContext
    // ИСПРАВЛЕНО: Явное получение DAO
    private val messageDao: MessageDao = (applicationContext as MoodApplication).database.messageDao()
    // ИСПРАВЛЕНО: Создаем клиент здесь или получаем из синглтона
    private val okHttpClient = OkHttpClient()

    // Токен и ID чата для отправки статистики (ВАЖНО: Небезопасно хранить здесь)
    private val TOKEN_KSYUSHA = "7782370418:AAGuPd40pssvAlp7snMlBJ2VaacCXYFrRlM"
    private val CHAT_ID_KSYUSHA = "361509391"

    companion object {
        // Имя для уникальной задачи WorkManager
        const val UNIQUE_WORK_NAME = "moodStatsSenderWork"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "MoodStatsWorker started.")
        try {
            // Время "неделю назад"
            val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

            // Получаем все сообщения (вызываем suspend функцию DAO)
            val allMessages = messageDao.getAllMessagesList()
            Log.d(TAG, "Fetched ${allMessages.size} total messages from DB.")

            // Фильтруем сообщения за последнюю неделю
            val recentMessages = allMessages.filter { it.timestamp >= oneWeekAgo }
            Log.d(TAG, "Found ${recentMessages.size} messages from the last week.")


            if (recentMessages.isEmpty()) {
                Log.d(TAG, "No recent messages to analyze. Work finished.")
                // Удаляем все сообщения старше недели, даже если новых не было
                val deletedRows = messageDao.deleteMessagesOlderThan(oneWeekAgo)
                Log.d(TAG, "Cleaned up old messages: $deletedRows deleted.")
                return@withContext Result.success()
            }

            // --- Логика подсчета статистики ---
            // Эта логика ОЧЕНЬ зависит от того, как вы сохраняете выбор настроения.
            // Если вы отправляете в Telegram "Выбрано (Настроение): Хорошо",
            // то нужно парсить поле 'text' в MessageEntity.
            // Пример:
            val moodCounts = mutableMapOf<String, Int>()
            recentMessages.forEach { msg ->
                // Ищем строки вида "Выбрано (Настроение): Значение"
                if (msg.text.startsWith("Выбрано (Настроение):")) {
                    val mood = msg.text.substringAfter("Выбрано (Настроение):").substringBefore("\n").trim()
                    if (mood.isNotEmpty()) {
                        moodCounts[mood] = moodCounts.getOrDefault(mood, 0) + 1
                    }
                }
                // Добавьте похожие проверки для "Желания", "Кофi time!", если нужно их считать
            }
            Log.d(TAG, "Calculated mood stats: $moodCounts")


            if (moodCounts.isEmpty()) {
                Log.d(TAG, "No relevant mood entries found in recent messages.")
            } else {
                // Формируем сообщение для Telegram
                val statsMessage = buildString {
                    append("Статистика настроений Ксюши за неделю:\n")
                    moodCounts.forEach { (mood, count) ->
                        append("- ${mood}: $count раз(а)\n")
                    }
                }

                // Отправляем статистику (вызываем suspend функцию)
                val sentSuccessfully = sendTelegramMessage(statsMessage)

                if (!sentSuccessfully) {
                    Log.e(TAG, "Failed to send mood stats to Telegram. Retrying later.")
                    return@withContext Result.retry() // Повторить попытку позже
                }
                Log.d(TAG, "Mood stats sent successfully.")
            }

            // Удаляем сообщения старше недели ПОСЛЕ обработки и отправки
            val deletedRows = messageDao.deleteMessagesOlderThan(oneWeekAgo)
            Log.d(TAG, "Deleted $deletedRows messages older than one week.")

            return@withContext Result.success() // Задача успешно выполнена

        } catch (e: Exception) {
            Log.e(TAG, "Error during MoodStatsWorker execution", e)
            return@withContext Result.failure() // Ошибка выполнения задачи
        }
    }

    // Функция отправки сообщения в Telegram (suspend)
    private suspend fun sendTelegramMessage(text: String): Boolean {
        // Мы уже в Dispatchers.IO из doWork, но withContext не помешает
        return withContext(Dispatchers.IO) {
            val urlString = "https://api.telegram.org/bot$TOKEN_KSYUSHA/sendMessage"
            // Экранируем текст для JSON
            val escapedText = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")

            val json = """
            {
                "chat_id": "$CHAT_ID_KSYUSHA",
                "text": "$escapedText"
            }
            """.trimIndent()
            Log.d(TAG, "Sending Telegram message: $json")


            val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(urlString)
                .post(requestBody)
                .build()

            try {
                // Используем execute() так как мы в корутине IO
                val response = okHttpClient.newCall(request).execute()
                val successful = response.isSuccessful
                val responseBody = response.body?.string() // Читаем тело ответа ОДИН РАЗ
                response.close() // Закрываем ответ

                if (!successful) {
                    Log.e(TAG, "Telegram send failed: ${response.code} ${response.message}")
                    Log.e(TAG, "Response body: $responseBody")
                } else {
                    Log.d(TAG, "Telegram message sent, response: $responseBody")
                }
                successful
            } catch (e: IOException) {
                Log.e(TAG, "IOException during Telegram send", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Telegram send", e)
                false
            }
        }
    }
}