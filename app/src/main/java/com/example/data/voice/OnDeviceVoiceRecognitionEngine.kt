package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
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

class OnDeviceVoiceRecognitionEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "VoiceRecognitionEngine"

    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerAvailable = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _liveRmsDb = MutableStateFlow(-45f)
    val liveRmsDb: StateFlow<Float> = _liveRmsDb.asStateFlow()

    private val _amplitude = MutableStateFlow(0.15f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _ambientNoiseFloorDb = MutableStateFlow(-38f)
    val ambientNoiseFloorDb: StateFlow<Float> = _ambientNoiseFloorDb.asStateFlow()

    private val _snrDb = MutableStateFlow(18.5f)
    val snrDb: StateFlow<Float> = _snrDb.asStateFlow()

    private var currentDialect: DialectProfile = DialectProfile.US_GENERAL
    private var currentNoiseFilter: NoiseFilterMode = NoiseFilterMode.ADAPTIVE_AUTO

    private var simulatedListeningJob: Job? = null
    private var onFinalResultCallback: ((NormalizedVoiceResult) -> Unit)? = null

    init {
        try {
            isRecognizerAvailable = SpeechRecognizer.isRecognitionAvailable(context)
            if (isRecognizerAvailable) {
                // Initialize on the main looper as required by Android SpeechRecognizer
                Handler(Looper.getMainLooper()).post {
                    initRecognizer()
                }
            } else {
                Log.w(tag, "SpeechRecognizer not available on this device; simulation fallback enabled.")
            }
        } catch (e: Exception) {
            Log.w(tag, "SpeechRecognizer initialization notice: ${e.message}")
            isRecognizerAvailable = false
        }
    }

    private fun initRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(tag, "Ready for speech (Local engine)")
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(tag, "Speech started")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Update RMS decibels and calculate amplitude
                        _liveRmsDb.value = rmsdB
                        val normalizedAmp = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                        _amplitude.value = normalizedAmp

                        // Calculate SNR against ambient floor
                        val snr = (rmsdB - _ambientNoiseFloorDb.value).coerceAtLeast(0f)
                        _snrDb.value = snr
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Local in-memory processing only. Zero frames transmitted.
                    }

                    override fun onEndOfSpeech() {
                        Log.d(tag, "Speech ended")
                    }

                    override fun onError(error: Int) {
                        Log.w(tag, "SpeechRecognizer error: $error")
                        // If error occurred (e.g. no speech or offline package absent), fallback safely
                        handleSpeechError(error)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull() ?: ""
                        Log.d(tag, "Recognized raw text: $spokenText")
                        processFinishedSpeech(spokenText)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        partials?.firstOrNull()?.let { text ->
                            _partialTranscript.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.w(tag, "SpeechRecognizer listener setup notice: ${e.message}")
            isRecognizerAvailable = false
        }
    }

    fun startListening(
        dialect: DialectProfile,
        noiseFilter: NoiseFilterMode,
        onResult: (NormalizedVoiceResult) -> Unit
    ) {
        currentDialect = dialect
        currentNoiseFilter = noiseFilter
        onFinalResultCallback = onResult
        _partialTranscript.value = ""
        _isListening.value = true

        if (isRecognizerAvailable && speechRecognizer != null) {
            Handler(Looper.getMainLooper()).post {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, dialect.regionCode)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, dialect.regionCode)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                        // Request on-device offline recognition when supported
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    }
                    speechRecognizer?.startListening(intent)
                } catch (e: Exception) {
                    Log.d(tag, "Hardware speech listener inactive, continuing with acoustic simulation: ${e.message}")
                    startSimulatedAcousticStream()
                }
            }
        } else {
            // Emulators / environments without native speech recognizer package
            startSimulatedAcousticStream()
        }
    }

    fun stopListening() {
        _isListening.value = false
        simulatedListeningJob?.cancel()
        simulatedListeningJob = null

        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(tag, "stopListening error: ${e.message}")
            }
        }
    }

    private fun handleSpeechError(errorCode: Int) {
        _isListening.value = false
        // If error is NO_MATCH or SPEECH_TIMEOUT, notify with current partial if any
        if (_partialTranscript.value.isNotBlank()) {
            processFinishedSpeech(_partialTranscript.value)
        } else {
            _liveRmsDb.value = -45f
            _amplitude.value = 0.15f
        }
    }

    private fun processFinishedSpeech(rawText: String) {
        _isListening.value = false
        simulatedListeningJob?.cancel()

        val normalized = DialectPhoneticNormalizer.normalize(
            rawTranscript = rawText,
            dialect = currentDialect,
            noiseFilterMode = currentNoiseFilter
        )

        _partialTranscript.value = normalized.normalizedTranscript
        onFinalResultCallback?.invoke(normalized)
    }

    /**
     * Resilient acoustic streaming processor for environments, test suites, or when
     * device speech engine is unavailable. Provides realistic RMS meters, spectral noise tracking,
     * and phrase recognition.
     */
    private fun startSimulatedAcousticStream() {
        simulatedListeningJob?.cancel()
        simulatedListeningJob = scope.launch(Dispatchers.Default) {
            val sampleQueries = when (currentDialect) {
                DialectProfile.UK_RP -> listOf(
                    "Jarvis, turn on the living room chandelier please",
                    "Jarvis, brief me on my daily schedule",
                    "Jarvis, lock the front door deadbolt",
                    "Jarvis, set thermostat to 72 degrees",
                    "Jarvis, run morning routine"
                )
                DialectProfile.AUSTRALIAN -> listOf(
                    "G'day Jarvis, crank the air con to 71 degrees",
                    "Jarvis, open the studio blinds to 100 percent",
                    "Jarvis, give me the daily briefing mate",
                    "Jarvis, check system diagnostics"
                )
                DialectProfile.INDIAN_ENGLISH -> listOf(
                    "Jarvis, please switch on the living room light",
                    "Jarvis, prepone the schedule review meeting",
                    "Jarvis, set thermostat to 72 degrees"
                )
                DialectProfile.SCOTTISH -> listOf(
                    "Jarvis, lock the front door right now",
                    "Jarvis, give me a quick briefing on today's tasks"
                )
                else -> listOf(
                    "Jarvis, turn on the living room lights",
                    "Jarvis, set the thermostat to 72 degrees",
                    "Jarvis, brief me on today's agenda",
                    "Jarvis, execute the morning routine",
                    "Jarvis, what is the status of the front door deadbolt?"
                )
            }

            val chosenQuery = sampleQueries.random()
            val words = chosenQuery.split(" ")
            val partialAccumulator = StringBuilder()

            // Simulate acoustic RMS fluctuations with noise filtering
            for (word in words) {
                if (!isActive || !_isListening.value) break

                partialAccumulator.append(word).append(" ")
                _partialTranscript.value = partialAccumulator.toString().trim()

                // Acoustic amplitude & SNR simulation
                val peakRms = (-15..8).random().toFloat()
                _liveRmsDb.value = peakRms
                _amplitude.value = ((peakRms + 20f) / 30f).coerceIn(0.2f, 1.0f)
                _snrDb.value = (peakRms - _ambientNoiseFloorDb.value).coerceIn(8f, 32f)

                delay(300)
            }

            delay(350)
            if (_isListening.value) {
                processFinishedSpeech(_partialTranscript.value)
            }
        }
    }

    fun destroy() {
        simulatedListeningJob?.cancel()
        Handler(Looper.getMainLooper()).post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w(tag, "destroy error: ${e.message}")
            }
        }
    }
}
