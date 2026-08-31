# ⏰ Soat va Budilnik

Android uchun sodda vaqt boshqaruv ilovasi: soat, budilnik, ovozli vaqt, eslatma va uyqu rejimi.

## Texnologiyalar

- Kotlin + Jetpack Compose (UI)
- Room (budilnik va eslatmalar bazasi)
- DataStore Preferences (sozlamalar)
- AlarmManager (aniq vaqtda ishlaydigan budilnik/eslatma)
- TextToSpeech (faqat vaqtni ovoz bilan aytish)

## Loyiha tuzilishi

```
app/src/main/java/com/soatbudilnik/app/
├── data/          # Alarm, Reminder, Room DB, Settings (DataStore)
├── receiver/       # AlarmReceiver, ReminderReceiver, TimeAnnouncerReceiver, BootReceiver
├── ui/             # AlarmRingActivity (budilnik chalinganda to'liq ekran)
├── util/           # AlarmScheduler, TimeAnnouncer
├── MainActivity.kt
└── SettingsScreen.kt
```

## Android Studio'da ochish

1. Android Studio'ni oching → **Open** → shu papkani tanlang
2. Gradle sinxronizatsiyasi tugashini kuting
3. `app` konfiguratsiyasini ishga tushiring (Run ▶️)

## GitHub'ga joylash

```bash
cd SoatBudilnik
git init
git add .
git commit -m "Boshlang'ich loyiha: soat, budilnik, eslatma, uyqu rejimi"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO_NOMI.git
git push -u origin main
```

`.gitignore` fayli `build/`, `.gradle/`, `local.properties` kabi keraksiz fayllarni avtomatik chetlab o'tadi.

## Hozircha bajarilgan (spetsifikatsiyaga ko'ra)

- ✅ Asosiy soat ekrani (soat, sana, hafta kuni)
- ✅ Budilnik: vaqt, nom, takrorlash, yoqish/o'chirish
- ✅ 3 xil signal turi: ovozli vaqt / vibratsiya / musiqa
- ✅ Avtomatik vaqtni ovoz bilan aytish (interval tanlash)
- ✅ Uyqu rejimi (faqat avtomatik ovozga ta'sir qiladi, budilnikka ta'sir qilmaydi)
- ✅ Vaqtga rejalashtirilgan eslatma (ovoz faqat vaqtni aytadi, matn faqat bildirishnomada)
- ✅ Telefon qayta yoqilganda barcha alarm/eslatmalarni tiklash (BootReceiver)
- ✅ Dark/Light mode

## Keyingi qadamlar (siz qo'shishingiz kerak bo'lgan qismlar)

- "+ Budilnik" va "+ Eslatma" tugmalari uchun to'liq qo'shish/tahrirlash ekranlari (hozir joy belgilangan)
- Musiqa tanlash uchun `ACTION_OPEN_DOCUMENT` orqali fayl tanlagich
- Ilova ikonkasi (`ic_launcher`) — hozir oddiy vector ikonka, xohlasangiz Image Asset Studio orqali real logotip qo'ying
- Android 13+ uchun runtime permission so'rovlari (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`)

Bu loyiha spetsifikatsiyadagi barcha asosiy mantiqni (data model, scheduling, receiver'lar) o'z ichiga oladi va Android Studio'da to'g'ridan-to'g'ri ochib, qolgan UI ekranlarini to'ldirib bemalol davom ettirsa bo'ladi.
