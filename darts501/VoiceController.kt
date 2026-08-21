package com.example.darts501

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Непрерывное распознавание речи: после каждого результата/ошибки
 * микрофон автоматически включается заново, пока не вызван stop().
 */
class VoiceController(private val context: Context) {

    var isListening by mutableStateOf(false)
        private set

    var heard by mutableStateOf("")
        private set

    var status by mutableStateOf("Микрофон выключен")
        private set

    /** Callback со всеми вариантами распознавания (лучший — первый). */
    var onResult: (List<String>) -> Unit = {}

    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var want = false
    private var restartScheduled = false

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        if (want) return
        if (!isAvailable()) {
            status = "Распознавание речи недоступно на устройстве"
            return
        }
        want = true
        isListening = true
        status = "Слушаю…"
        createAndListen()
    }

    fun stop() {
        want = false
        isListening = false
        status = "Микрофон выключен"
        heard = ""
        handler.removeCallbacksAndMessages(null)
        restartScheduled = false
        destroyRecognizer()
    }

    fun toggle() {
        if (want) stop() else start()
    }

    private fun destroyRecognizer() {
        val r = recognizer
        recognizer = null
        if (r != null) {
            try {
                r.cancel()
                r.destroy()
            } catch (_: Exception) {
            }
        }
    }

    private fun createAndListen() {
        if (!want) return
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(listener)
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 800L)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        try {
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            destroyRecognizer()
            scheduleRestart(600)
        }
    }

    private fun scheduleRestart(delay: Long) {
        if (!want || restartScheduled) return
        restartScheduled = true
        handler.postDelayed({
            restartScheduled = false
            createAndListen()
        }, delay)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            if (want) status = "Слушаю…"
        }

        override fun onBeginningOfSpeech() {
            if (want) status = "Говорите…"
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            if (want) status = "Обработка…"
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_CLIENT -> {
                    destroyRecognizer()
                    scheduleRestart(500)
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    status = "Нет разрешения на микрофон"
                    stop()
                }
                else -> scheduleRestart(300)
            }
        }

        override fun onResults(results: Bundle?) {
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!list.isNullOrEmpty()) {
                heard = list[0]
                onResult(list)
            }
            scheduleRestart(150)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!list.isNullOrEmpty() && list[0].isNotBlank()) {
                heard = list[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
