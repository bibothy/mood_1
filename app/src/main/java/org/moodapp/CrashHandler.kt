package org.moodapp

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler {
    companion object {
        fun handleCrash(context: Context, throwable: Throwable) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val logMessage = buildString {
                append("Crash time: $timeStamp\n")
                append("Exception: ${throwable.message}\n")
                append("Stack trace:\n")
                throwable.stackTrace.take(10).forEach { element ->
                    append("$element\n")
                }
            }
            TelegramApiHelper.sendTelegramMessage(context, "Приложение упало!\n$logMessage", AppConstants.TOKEN_KSYUSHA)
        }
    }
}