package org.moodapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val TOKEN_KSYUSHA = "7782370418:AAGuPd40pssvAlp7snMlBJ2VaacCXYFrRlM"
    companion object {
        const val TOKEN_PES = "7582958998:AAHpQAyi6H_EMszftAKxYRQ6AS-_CnsHo4s"
        private const val CHAT_ID = "361509391"
        private const val TELEGRAM_API_URL = "https://api.telegram.org"
        private val client = OkHttpClient()

        fun sendTelegramMessage(context: Context, text: String, token: String) {
            if (!isNetworkAvailable(context)) {
                Log.w("MainActivity", "Нет интернета, сообщение не отправлено: $text")
                return
            }
            thread {
                val url = "$TELEGRAM_API_URL/bot$token/sendMessage"
                val body = FormBody.Builder()
                    .add("chat_id", CHAT_ID)
                    .add("text", text)
                    .build()
                val request = Request.Builder().url(url).post(body).build()
                try {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Log.i("MainActivity", "Сообщение успешно отправлено через токен $token: $text")
                        } else {
                            Log.e("MainActivity", "Не удалось отправить сообщение: ${response.code} - ${response.body?.string()}")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("MainActivity", "Ошибка отправки в Telegram: ${e.message}")
                }
            }
        }

        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private val moodStats = mutableMapOf<String, Int>()
    private var isRunning = true

    private val moods = mapOf(
        "Плохо" to "Давай я тебя обниму, а если будет недостаточно, то найду какой-нибудь сладкий прикол для тебя))",
        "Тревожно" to "Ты пила таблетки сегодня, котешка? Можешь на меня положиться, я рядом и всегда готов тебе помочь",
        "Хорошо" to "Я очень рад за тебя!",
        "Устала" to "Грустно, что ты так устала, этот ремонт скоро закончится и я свожу тебя на массаж в Тюмени",
        "Веселое" to "Время подъебать пса!! Хде ты маленький??",
        "Радость" to "Юхууу!! Ты рада и я тоже рад вместе с тобой, это заразно!!!!"
    )

    private val desires = mapOf(
        "Воды" to "Да, сейчас принесу..",
        "Чай" to "Еще чай? ОЙ я забыл..",
        "Сладкого" to "Еще сладкого? Ты забыла про 1000 калорий?.. Ну ладно, сейчас принесу:3",
        "Обнимашки" to "Я работаю над этим, котешка",
        "Секс" to "Он уже стоит и готов",
        "КОФЕ" to "ЭХ КОТЕШКА, ТЕПЕРЬ Я НЕ СМОГУ ЗА НИМ СХОДИТЬ, НО МОГУ ЗАКАЗАТЬ!!! Для выбора кофе — кнопка снизу!"
    )

    private val coffeeOptions = mapOf(
        "Латте" to "Латте для котешки готово!",
        "Американо" to "Американо — бодрость для тебя!",
        "Раф" to "Раф — нежность в чашке!",
        "Капучино" to "Капучино — пена счастья для котешки!",
        "Выбери кофе котешке сам!!!" to "Котешка получит сюрприз!"
    )

    private val silliness = mapOf(
        "Мяу 1" to R.raw.mau_1,
        "Мяу 2" to R.raw.mau_2,
        "Кофе" to R.raw.cofe,
        "Хорошо" to R.raw.good,
        "Грусть" to R.raw.grust,
        "Мрр" to R.raw.mr,
        "Обожаю" to R.raw.oboj,
        "Русский" to R.raw.rus,
        "Грустно" to R.raw.sad,
        "Секс" to R.raw.sex,
        "Need Mommy" to R.raw.need_mommy,
        "Meow Very Sad" to R.raw.meow_very_sad,
        "Meow Sad" to R.raw.meow_sad
    )

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Разрешение на уведомления получено")
        } else {
            Log.w("MainActivity", "Разрешение на уведомления отклонено")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            handleCrash(throwable)
        }
        setContentView(R.layout.activity_main)

        val moodButton: Button = findViewById(R.id.mood_button)
        val desireButton: Button = findViewById(R.id.desire_button)
        val sillyButton: Button = findViewById(R.id.silly_button)
        val coffeeButton: Button = findViewById(R.id.coffee_button)
        val chatButton: Button = findViewById(R.id.chat_button)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        createNotificationChannel()
        startMoodStatsTimer()
        sendTelegramMessage(this, "Приложение запущено!", TOKEN_KSYUSHA)
        // sendFcmTokenToPesBot() — убираем отсюда, оставляем только в MyFirebaseMessagingService

        moodButton.setOnClickListener {
            val options = moods.keys.toTypedArray()
            android.app.AlertDialog.Builder(this)
                .setTitle("Выбери настроение")
                .setItems(options) { _, which ->
                    val selectedMood = options[which]
                    val moodResponse = moods[selectedMood] ?: ""
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Ответ")
                        .setMessage(moodResponse)
                        .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
                        .show()
                    sendTelegramMessage(this, "Ксюша выбрала настроение: $selectedMood\nОтвет: $moodResponse", TOKEN_KSYUSHA)
                    updateMoodStats(selectedMood)
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        desireButton.setOnClickListener {
            val options = desires.keys.toTypedArray()
            android.app.AlertDialog.Builder(this)
                .setTitle("Выбери желание")
                .setItems(options) { _, which ->
                    val selectedDesire = options[which]
                    val desireResponse = desires[selectedDesire] ?: ""
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Ответ")
                        .setMessage(desireResponse)
                        .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
                        .show()
                    sendTelegramMessage(this, "Ксюша выбрала желание: $selectedDesire\nОтвет: $desireResponse", TOKEN_KSYUSHA)
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        sillyButton.setOnClickListener {
            val options = silliness.keys.toTypedArray()
            android.app.AlertDialog.Builder(this)
                .setTitle("Выбери придурочность")
                .setItems(options) { _, which ->
                    val selectedSilly = options[which]
                    val soundRes = silliness[selectedSilly] ?: return@setItems
                    playSound(soundRes)
                    sendTelegramMessage(this, "Ксюша выбрала придурочность: $selectedSilly", TOKEN_KSYUSHA)
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        coffeeButton.setOnClickListener {
            val options = coffeeOptions.keys.toTypedArray()
            android.app.AlertDialog.Builder(this)
                .setTitle("Выбери кофе")
                .setItems(options) { _, which ->
                    val selectedCoffee = options[which]
                    val coffeeResponse = coffeeOptions[selectedCoffee] ?: ""
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Ответ")
                        .setMessage(coffeeResponse)
                        .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
                        .show()
                    sendTelegramMessage(this, "Ксюша выбрала кофе: $selectedCoffee\nОтвет: $coffeeResponse", TOKEN_KSYUSHA)
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        chatButton.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MoodApp Notifications"
            val descriptionText = "Уведомления от MoodApp"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MOOD_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMoodStatsTimer() {
        thread {
            while (isRunning) {
                Thread.sleep(7 * 24 * 60 * 60 * 1000L) // 1 неделя
                sendMoodStats()
            }
        }
    }

    private fun sendMoodStats() {
        if (moodStats.isNotEmpty()) {
            val statsMessage = buildString {
                append("Статистика настроений за неделю:\n")
                moodStats.forEach { (mood, count) ->
                    append("$mood: $count раз\n")
                }
            }
            sendTelegramMessage(this, statsMessage, TOKEN_KSYUSHA)
            moodStats.clear()
        }
    }

    private fun handleCrash(throwable: Throwable) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logMessage = buildString {
            append("Crash time: $timeStamp\n")
            append("Exception: ${throwable.message}\n")
            append("Stack trace:\n")
            throwable.stackTrace.take(10).forEach { element ->
                append("$element\n")
            }
        }
        sendTelegramMessage(this, "Приложение упало!\n$logMessage", TOKEN_KSYUSHA)
        Thread.sleep(2000)
        finish()
    }

    private fun playSound(soundRes: Int) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = MediaPlayer.create(this, soundRes)
            if (mediaPlayer == null) {
                Log.e("MainActivity", "Не удалось создать MediaPlayer для ресурса: $soundRes")
                return
            }
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка воспроизведения звука: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        isRunning = false
    }

    fun updateMoodStats(mood: String) {
        moodStats[mood] = moodStats.getOrDefault(mood, 0) + 1
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra("response")?.let { response ->
            sendTelegramMessage(this, "Ксюша ответила: $response", TOKEN_PES)
        }
        intent?.getStringExtra("like")?.let {
            sendTelegramMessage(this, "Ксюша поставила лайк твоему сообщению!", TOKEN_PES)
        }
        intent?.getStringExtra("dislike")?.let {
            sendTelegramMessage(this, "Ксюша поставила дизлайк твоему сообщению!", TOKEN_PES)
        }
    }
}