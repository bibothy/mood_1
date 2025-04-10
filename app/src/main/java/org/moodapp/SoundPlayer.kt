package org.moodapp

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.lang.Exception

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(soundRes: Int) {
         try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, soundRes) ?: throw Exception("Не удалось создать MediaPlayer для ресурса: $soundRes")
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
        } catch (e: Exception){
            Log.e("SoundPlayer", "Ошибка воспроизведения звука: ${e.message}")
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}