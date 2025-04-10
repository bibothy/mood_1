package org.moodapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.moodapp.databinding.ActivityStatsBinding

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var moodDao: MoodDao
    private lateinit var desireDao: DesireDao
    private lateinit var sillyDao: SillyDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = AppDatabase.getDatabase(this)
        moodDao = appDatabase.moodDao()
        desireDao = appDatabase.desireDao()
        sillyDao = appDatabase.sillyDao()

        loadStats()
    }

    private fun loadStats() {
        lifecycleScope.launch {
            val mostFrequentMood = withContext(Dispatchers.IO) {
                moodDao.getMostFrequentMood()
            }
            val mostFrequentDesire = withContext(Dispatchers.IO) {
                desireDao.getMostFrequentDesire()
            }
            val mostFrequentSilly = withContext(Dispatchers.IO) {
                sillyDao.getMostFrequentSilly()
            }

            runOnUiThread {
                binding.mostFrequentMoodTextView.text = mostFrequentMood ?: "No mood data"
                binding.mostFrequentDesireTextView.text = mostFrequentDesire ?: "No desire data"
                binding.mostFrequentSillyTextView.text = mostFrequentSilly ?: "No silly data"
            }
        }
    }
}
```
```kotlin
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Most Frequent Mood:"
        android:textSize="18sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/mostFrequentMoodTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Loading..."
        android:textSize="16sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Most Frequent Desire:"
        android:textSize="18sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/mostFrequentDesireTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Loading..."
        android:textSize="16sp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Most Frequent Silly:"
        android:textSize="18sp"
        android:textStyle="bold" />

    <TextView
        android:id="@+id/mostFrequentSillyTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Loading..."
        android:textSize="16sp" />

</LinearLayout>
```
```kotlin
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>

    </data>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <include layout="@layout/activity_stats" />
    </LinearLayout>
</layout>