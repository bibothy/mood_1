package org.moodapp

import android.content.Context
import kotlin.concurrent.thread

class MoodStatsManager(private val context: Context) {
    private val moodStats: MutableMap<String, Int> = mutableMapOf()
    private var isRunning = true

    fun updateMoodStats(mood: String) {
        moodStats[mood] = moodStats.getOrDefault(mood, 0) + 1
    }

    fun sendMoodStats() {
        if (moodStats.isNotEmpty()) {
            val statsMessage = buildString {
                append("Статистика настроений за неделю:\n")
                moodStats.forEach { (mood, count) ->
                    append("$mood: $count раз\n")
                }
            }
            TelegramApiHelper.sendTelegramMessage(context, statsMessage, AppConstants.TOKEN_KSYUSHA)
            moodStats.clear()
        }
    }

    fun startMoodStatsTimer() {
        thread {
            while (isRunning) {
                Thread.sleep(7 * 24 * 60 * 60 * 1000L) // 1 неделя
                sendMoodStats()
            }
        }
    }

    fun stop(){
        isRunning = false
    }
}