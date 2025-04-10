package org.moodapp

import android.content.Context
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.concurrent.thread

class TelegramApiHelper {
    companion object {
        private val client = OkHttpClient()

        fun sendTelegramMessage(context: Context, text: String, token: String) {
            if (!NetworkHelper.isNetworkAvailable(context)) {
                Log.w("TelegramApiHelper", "Нет интернета, сообщение не отправлено: $text")
                return
            }
            thread {
                val url = "${AppConstants.TELEGRAM_API_URL}/bot$token/sendMessage"
                val body = FormBody.Builder()
                    .add("chat_id", AppConstants.CHAT_ID)
                    .add("text", text)
                    .build()
                val request = Request.Builder().url(url).post(body).build()
                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Log.i("TelegramApiHelper", "Сообщение успешно отправлено через токен $token: $text")
                        } else {
                            Log.e("TelegramApiHelper", "Не удалось отправить сообщение: ${response.code} - ${response.body?.string()}")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("TelegramApiHelper", "Ошибка отправки в Telegram: ${e.message}")
                }
            }
        }
    }
}