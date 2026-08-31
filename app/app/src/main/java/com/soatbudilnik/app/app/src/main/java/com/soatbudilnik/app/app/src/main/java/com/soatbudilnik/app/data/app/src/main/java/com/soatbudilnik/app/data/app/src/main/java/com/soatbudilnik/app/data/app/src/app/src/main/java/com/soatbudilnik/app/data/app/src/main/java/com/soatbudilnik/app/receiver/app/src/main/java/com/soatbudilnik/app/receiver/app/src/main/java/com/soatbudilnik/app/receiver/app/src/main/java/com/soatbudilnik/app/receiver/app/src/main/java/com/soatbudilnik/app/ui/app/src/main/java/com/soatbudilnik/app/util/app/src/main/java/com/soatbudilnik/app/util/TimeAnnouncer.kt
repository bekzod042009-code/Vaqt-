package com.soatbudilnik.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Faqat vaqtni ovoz bilan aytadi. Masalan: "07:30".
 * Hech qanday qo'shimcha so'z ("Hozir soat...") ishlatilmaydi - spetsifikatsiyaga ko'ra.
 */
class TimeAnnouncer(context: Context, private val onDone: (() -> Unit)? = null) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val pendingQueue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("uz") // agar uz mavjud bo'lmasa tizim standart tilga o'tadi
                ready = true
                pendingQueue.forEach { speakInternal(it) }
                pendingQueue.clear()
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone?.invoke() }
            override fun onError(utteranceId: String?) {}
        })
    }

    /** hour:minute ni "07:30" ko'rinishida talaffuz qiladi */
    fun announceTime(hour: Int, minute: Int) {
        val text = String.format(Locale.US, "%02d:%02d", hour, minute)
        if (ready) speakInternal(text) else pendingQueue.add(text)
    }

    private fun speakInternal(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "time_announce")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
