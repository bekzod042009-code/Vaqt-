package com.soatbudilnik.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soatbudilnik.app.data.AnnounceInterval
import com.soatbudilnik.app.data.AppSettings
import com.soatbudilnik.app.data.SettingsRepository
import com.soatbudilnik.app.util.AlarmScheduler
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: androidx.navigation.NavController,
    settingsRepo: SettingsRepository,
    settings: AppSettings
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sozlamalar") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Text("Soat formati", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(if (settings.is24HourFormat) "24 soatlik" else "12 soatlik")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = settings.is24HourFormat,
                    onCheckedChange = { checked ->
                        scope.launch { settingsRepo.update { it.copy(is24HourFormat = checked) } }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Avtomatik vaqtni ovoz bilan aytish", style = MaterialTheme.typography.titleMedium)
            AnnounceInterval.values().forEach { interval ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.announceInterval == interval,
                        onClick = {
                            scope.launch {
                                settingsRepo.update { it.copy(announceInterval = interval) }
                                AlarmScheduler.scheduleNextAnnouncer(context, interval)
                            }
                        }
                    )
                    Text(interval.label)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Vibratsiya", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Yoqilgan")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { settingsRepo.update { it.copy(vibrationEnabled = checked) } }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Uyqu rejimi", style = MaterialTheme.typography.titleMedium)
            Text("Boshlanish: %02d:%02d — Tugash: %02d:%02d".format(
                settings.sleepStartHour, settings.sleepStartMinute,
                settings.sleepEndHour, settings.sleepEndMinute
            ))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Faol")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = settings.sleepModeEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { settingsRepo.update { it.copy(sleepModeEnabled = checked) } }
                    }
                )
            }
            Text(
                "Eslatma: uyqu rejimi budilniklarni o'chirib qo'ymaydi, faqat " +
                        "avtomatik vaqt aytish funksiyasiga ta'sir qiladi.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))
            Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = settings.darkMode,
                onCheckedChange = { checked ->
                    scope.launch { settingsRepo.update { it.copy(darkMode = checked) } }
                }
            )

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Orqaga")
            }
        }
    }
}
