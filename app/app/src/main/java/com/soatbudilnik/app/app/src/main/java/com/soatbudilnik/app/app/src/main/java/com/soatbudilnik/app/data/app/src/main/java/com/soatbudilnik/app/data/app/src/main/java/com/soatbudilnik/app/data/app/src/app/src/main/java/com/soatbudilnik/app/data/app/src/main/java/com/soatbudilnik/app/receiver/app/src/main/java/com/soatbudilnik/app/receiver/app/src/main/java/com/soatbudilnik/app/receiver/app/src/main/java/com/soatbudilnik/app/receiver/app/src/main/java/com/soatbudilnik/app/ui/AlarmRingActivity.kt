package com.soatbudilnik.app.ui

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soatbudilnik.app.data.AlarmSoundType
import com.soatbudilnik.app.data.AppDatabase
import com.soatbudilnik.app.util.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Budilnik signal berayotganda ochiladigan to'liq ekranli oyna.
 * Signal turiga qarab: ovozli vaqt aytish / vibratsiya / musiqa ijro etadi.
 */
class AlarmRingActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var timeAnnouncer: TimeAnnouncer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmId = intent.getIntExtra("alarm_id", -1)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        setContent {
            MaterialTheme {
                var label by remember { mutableStateOf("") }
                var timeText by remember { mutableStateOf("") }

                LaunchedEffect(alarmId) {
                    val alarm = AppDatabase.getInstance(applicationContext).alarmDao().getById(alarmId)
                    if (alarm != null) {
                        label = alarm.label
                        timeText = String.format("%02d:%02d", alarm.hour, alarm.minute)
                        playSignal(alarm.soundType, alarm.musicUri, alarm.hour, alarm.minute)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(timeText, fontSize = 64.sp)
                    if (label.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(label, fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(48.dp))
                    Button(onClick = { stopSignalAndFinish() }) {
                        Text("To'xtatish")
                    }
                }
            }
        }
    }

    private fun playSignal(type: AlarmSoundType, musicUri: String?, hour: Int, minute: Int) {
        when (type) {
            AlarmSoundType.VOICE -> {
                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                timeAnnouncer = TimeAnnouncer(applicationContext)
                timeAnnouncer?.announceTime(hour, minute)
            }
            AlarmSoundType.VIBRATION -> {
                vibrator?.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            AlarmSoundType.MUSIC -> {
                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                if (musicUri != null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(applicationContext, Uri.parse(musicUri))
                        isLooping = true
                        prepare()
                        start()
                    }
                }
            }
        }
    }

    private fun stopSignalAndFinish() {
        vibrator?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        timeAnnouncer?.shutdown()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        timeAnnouncer?.shutdown()
    }
}
