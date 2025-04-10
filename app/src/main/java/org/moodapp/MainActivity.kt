package org.moodapp


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var moodStatsManager: MoodStatsManager
    private lateinit var desireButton: Button
    private lateinit var progressBar: ProgressBar
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
           println("Разрешение на уведомления отклонено")
        }
    }
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.handleCrash(this, Throwable())
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_in)
        moodStatsManager = MoodStatsManager(this)
        moodStatsManager.startMoodStatsTimer()
        progressBar = findViewById(R.id.progressBar)
        val moodButton: Button = findViewById(R.id.mood_button)
        desireButton = findViewById(R.id.desire_button)
        val sillyButton: Button = findViewById(R.id.silly_button)

        val coffeeButton: Button = findViewById(R.id.coffee_button)
        val chatButton: Button = findViewById(R.id.chat_button)

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        createNotificationChannel()
       soundPlayer = SoundPlayer(this)
       progressBar = findViewById(R.id.progressBar)
       TelegramApiHelper.sendTelegramMessage(this, getString(R.string.app_start), AppConstants.TOKEN_KSYUSHA)
       hideProgressBar()       
        moodButton.setOnClickListener { handleMoodButtonClick() }
        coffeeButton.setOnClickListener { handleCoffeeButtonClick() }
        desireButton.setOnClickListener { handleDesireButtonClick() }
        chatButton.setOnClickListener { handleChatButtonClick() }
    }
     override fun onDestroy() {
        super.onDestroy()
        moodStatsManager.release()
        soundPlayer.release()
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getStringExtra("response")?.let { response ->
            TelegramApiHelper.sendTelegramMessage(this, "Ксюша ответила: $response", AppConstants.TOKEN_PES)
        }
        intent?.getStringExtra("like")?.let {
            TelegramApiHelper.sendTelegramMessage(this, "Ксюша поставила лайк твоему сообщению!", AppConstants.TOKEN_PES)
        }
        intent?.getStringExtra("dislike")?.let { TelegramApiHelper.sendTelegramMessage(this, "Ксюша поставила дизлайк твоему сообщению!", AppConstants.TOKEN_PES)}
    }

     private fun hideProgressBar() {
       progressBar.animate()
           .alpha(0f)
           .setDuration(500)
           .withEndAction { progressBar.visibility = View.GONE }
           .start()
        sillyButton.setOnClickListener { handleSillyButtonClick() }
     }
    private fun handleMoodButtonClick() {
        val options = AppConstants.moods.keys.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle(getString(R.string.select_mood)).setItems(options) { _, which ->
                val selectedMood = options[which]
                val moodResponse = AppConstants.moods[selectedMood] ?: ""
                android.app.AlertDialog.Builder(this).setTitle(getString(R.string.answer)).setMessage(moodResponse).setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }.show()
                TelegramApiHelper.sendTelegramMessage(
                    this,
                    "Ксюша выбрала настроение: $selectedMood\nОтвет: $moodResponse",
                    AppConstants.TOKEN_KSYUSHA
                )
                moodStatsManager.updateMoodStats(selectedMood)
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun handleDesireButtonClick() {
        val options = AppConstants.desires.keys.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle(getString(R.string.select_desire)).setItems(options) { _, which ->
                val selectedDesire = options[which]
                val desireResponse = AppConstants.desires[selectedDesire] ?: ""
                android.app.AlertDialog.Builder(this).setTitle(getString(R.string.answer)).setMessage(desireResponse).setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }.show()
                TelegramApiHelper.sendTelegramMessage(
                    this,
                    "Ксюша выбрала желание: $selectedDesire\nОтвет: $desireResponse",
                    AppConstants.TOKEN_KSYUSHA
                )
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun handleSillyButtonClick() {
        val options = AppConstants.silliness.keys.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle(getString(R.string.select_silliness)).setItems(options) { _, which ->
                val selectedSilly = options[which]
                val soundRes = AppConstants.silliness[selectedSilly] ?: return@setItems
                soundPlayer.playSound(soundRes)
                moodStatsManager.updateMoodStats(selectedSilly)

                TelegramApiHelper.sendTelegramMessage(
                    this,
                    "Ксюша выбрала придурочность: $selectedSilly",
                    AppConstants.TOKEN_KSYUSHA
                )
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }

    private fun handleCoffeeButtonClick() {
        val options = AppConstants.coffeeOptions.keys.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("Выбери кофе")
            .setItems(options) { _, which ->
                val selectedCoffee = options[which].also { coffeeResponse -> android.app.AlertDialog.Builder(this).setTitle("Ответ").setMessage(AppConstants.coffeeOptions[coffeeResponse] ?: "").
                    .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
                    .show()
                TelegramApiHelper.sendTelegramMessage(
                    this,
                    "Ксюша выбрала кофе: $selectedCoffee\nОтвет: $coffeeResponse",
                    AppConstants.TOKEN_KSYUSHA
                )
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }.show()
    }
    private fun handleChatButtonClick() {
        val chatButton = findViewById<Button>(R.id.chat_button)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, Pair.create(chatButton, getString(R.string.transition_chat_button)))
        startActivity(Intent(this, ChatActivity::class.java), options.toBundle())
    }
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = getString(R.string.mood_app_notifications)
        val descriptionText = getString(R.string.notifications_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(AppConstants.MOOD_CHANNEL, name, importance).apply { description = descriptionText }
        notificationManager.createNotificationChannel(channel)
        }
    }
