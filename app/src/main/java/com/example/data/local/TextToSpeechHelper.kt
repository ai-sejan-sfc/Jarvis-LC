package com.example.data.local

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.voice.VoicePersona
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class TextToSpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val helperScope = CoroutineScope(Dispatchers.Main)
    private var speechWaveJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0.15f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(0.93f)
                tts?.setSpeechRate(1.02f)
                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        startSpeechWaveAnimation()
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopSpeechWaveAnimation()
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopSpeechWaveAnimation()
                    }
                })
            }
        }
    }

    fun applyPersona(persona: VoicePersona) {
        tts?.setPitch(persona.pitch)
        tts?.setSpeechRate(persona.speechRate)
    }

    fun speak(text: String, persona: VoicePersona? = null) {
        persona?.let { applyPersona(it) }
        if (isInitialized && tts != null) {
            _isSpeaking.value = true
            val utteranceId = "JARVIS_UTTERANCE_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            _isSpeaking.value = false
        }
    }

    private fun startSpeechWaveAnimation() {
        speechWaveJob?.cancel()
        speechWaveJob = helperScope.launch {
            var step = 0
            while (isActive && _isSpeaking.value) {
                val wave = 0.35f + (0.45f * kotlin.math.sin(step * 0.4f).toFloat().coerceAtLeast(0f))
                _speechAmplitude.value = wave.coerceIn(0.15f, 0.95f)
                step++
                delay(80)
            }
            _speechAmplitude.value = 0.15f
        }
    }

    private fun stopSpeechWaveAnimation() {
        speechWaveJob?.cancel()
        speechWaveJob = null
        _speechAmplitude.value = 0.15f
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopSpeechWaveAnimation()
    }

    fun shutdown() {
        stopSpeechWaveAnimation()
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isSpeaking.value = false
    }
}
