package com.soatbudilnik.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Vaqtga rejalashtirilgan eslatma (masalan "10:30 - Dars qilish").
 * Ovozli yordamchi faqat vaqtni aytadi, matnni o'qimaydi -
 * matn faqat bildirishnomada ko'rsatiladi.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val text: String,
    val repeatDays: Set<Int> = emptySet(), // bo'sh bo'lsa "bir marta"
    val isEnabled: Boolean = true
)
