package com.soatbudilnik.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soatbudilnik.app.data.AppDatabase
import com.soatbudilnik.app.data.SettingsRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val db = remember { AppDatabase.getInstance(applicationContext) }
            val settingsRepo = remember { SettingsRepository(applicationContext) }
            val settings by settingsRepo.settingsFlow.collectAsState(
                initial = com.soatbudilnik.app.data.AppSettings()
            )

            MaterialTheme(colorScheme = if (settings.darkMode) darkColorScheme() else lightColorScheme()) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            db = db,
                            is24h = settings.is24HourFormat
                        )
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController, settingsRepo = settingsRepo, settings = settings)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    db: AppDatabase,
    is24h: Boolean
) {
    val alarms by db.alarmDao().getAll().collectAsState(initial = emptyList())
    val reminders by db.reminderDao().getAll().collectAsState(initial = emptyList())

    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }
    val timeFormat = SimpleDateFormat(if (is24h) "HH:mm:ss" else "hh:mm:ss a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d-MMMM", Locale("uz"))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: budilnik qo'shish ekrani */ }) {
                Icon(Icons.Default.Add, contentDescription = "Qo'shish")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Asosiy soat
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = timeFormat.format(now),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(now).replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(24.dp))
            }

            Text("Budilniklar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyColumnAlarms(alarms)

            Spacer(Modifier.height(24.dp))
            Text("Bugungi eslatmalar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyColumnReminders(reminders)

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { navController.navigate("settings") }) {
                Text("Sozlamalar")
            }
        }
    }
}

@Composable
fun LazyColumnAlarms(alarms: List<com.soatbudilnik.app.data.Alarm>) {
    Column {
        if (alarms.isEmpty()) {
            Text("Hozircha budilnik yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        alarms.forEach { alarm ->
            val icon = when (alarm.soundType) {
                com.soatbudilnik.app.data.AlarmSoundType.VOICE -> "🔊"
                com.soatbudilnik.app.data.AlarmSoundType.VIBRATION -> "📳"
                com.soatbudilnik.app.data.AlarmSoundType.MUSIC -> "🎵"
            }
            val status = if (alarm.isEnabled) "🟢" else "⚪"
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(String.format("%02d:%02d", alarm.hour, alarm.minute))
                Text("$icon  ${alarm.label}")
                Text(status)
            }
        }
    }
}

@Composable
fun LazyColumnReminders(reminders: List<com.soatbudilnik.app.data.Reminder>) {
    Column {
        if (reminders.isEmpty()) {
            Text("Bugun uchun eslatma yo'q", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        reminders.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(String.format("%02d:%02d", r.hour, r.minute))
                Text(r.text)
            }
        }
    }
}
