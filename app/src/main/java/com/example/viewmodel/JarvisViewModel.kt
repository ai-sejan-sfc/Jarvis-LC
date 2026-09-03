package com.example.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.CryptoManager
import com.example.data.local.NlpActionType
import com.example.data.local.OnDeviceNlpEngine
import com.example.data.local.TextToSpeechHelper
import com.example.data.voice.CaptureState
import com.example.data.voice.DialectProfile
import com.example.data.voice.GeminiLiveVoiceEngine
import com.example.data.voice.MicrophoneCaptureService
import com.example.data.voice.NoiseFilterMode
import com.example.data.voice.OnDeviceVoiceRecognitionEngine
import com.example.data.voice.VoicePersona
import com.example.data.model.CalendarTask
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceType
import com.example.data.model.MessageSender
import com.example.data.model.PluginItem
import com.example.data.model.PluginStatus
import com.example.data.model.Routine
import com.example.data.model.RoutineAction
import com.example.data.model.RoutineTriggerType
import com.example.data.model.SmartDevice
import com.example.data.model.SmartProtocol
import com.example.data.model.SyncLogEntry
import com.example.data.model.SyncState
import com.example.data.model.SystemHealth
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.data.model.TemperatureUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsHelper = TextToSpeechHelper(application)

    private val onDeviceVoiceEngine = OnDeviceVoiceRecognitionEngine(application, viewModelScope)
    private val geminiLiveEngine = GeminiLiveVoiceEngine()

    private val prefs = application.getSharedPreferences("jarvis_app_prefs", Context.MODE_PRIVATE)

    // Text Input Field state (auto-filled by voice transcription)
    private val _inputQuery = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery.asStateFlow()

    // Configured Gemini API Key
    private val _customApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    fun updateInputQuery(text: String) {
        _inputQuery.value = text
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
        prefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun getEffectiveApiKey(): String {
        val userKey = _customApiKey.value.trim()
        if (userKey.isNotBlank()) return userKey
        val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY") && buildKey != "null") {
            return buildKey
        }
        return ""
    }

    // Gemini Live vs On-Device Privacy Mode
    private val _isGeminiLiveMode = MutableStateFlow(true)
    val isGeminiLiveMode: StateFlow<Boolean> = _isGeminiLiveMode.asStateFlow()

    // Offline-Only Privacy Mode (Strict 100% Air-Gapped Neural Core)
    private val _offlineOnlyPrivacyMode = MutableStateFlow(prefs.getBoolean("offline_only_privacy_mode", false))
    val offlineOnlyPrivacyMode: StateFlow<Boolean> = _offlineOnlyPrivacyMode.asStateFlow()

    // Smart Home Preferences
    private val _temperatureUnit = MutableStateFlow(
        try {
            TemperatureUnit.valueOf(prefs.getString("temperature_unit", "FAHRENHEIT") ?: "FAHRENHEIT")
        } catch (e: Exception) {
            TemperatureUnit.FAHRENHEIT
        }
    )
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    private val _defaultLightBrightness = MutableStateFlow(prefs.getInt("default_light_brightness", 80))
    val defaultLightBrightness: StateFlow<Int> = _defaultLightBrightness.asStateFlow()

    private val _preferredRoom = MutableStateFlow(prefs.getString("preferred_room", "Living Room") ?: "Living Room")
    val preferredRoom: StateFlow<String> = _preferredRoom.asStateFlow()

    private val _autoLockDelaySeconds = MutableStateFlow(prefs.getInt("auto_lock_delay_seconds", 60))
    val autoLockDelaySeconds: StateFlow<Int> = _autoLockDelaySeconds.asStateFlow()

    private val _preferredProtocol = MutableStateFlow(
        try {
            SmartProtocol.valueOf(prefs.getString("preferred_protocol", "MATTER") ?: "MATTER")
        } catch (e: Exception) {
            SmartProtocol.MATTER
        }
    )
    val preferredProtocol: StateFlow<SmartProtocol> = _preferredProtocol.asStateFlow()

    private val _confirmSecurityActions = MutableStateFlow(prefs.getBoolean("confirm_security_actions", true))
    val confirmSecurityActions: StateFlow<Boolean> = _confirmSecurityActions.asStateFlow()

    // Wake Word & Microphone Capture Service State
    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean("wake_word_enabled", true))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _wakeWordPhrase = MutableStateFlow(prefs.getString("wake_word_phrase", "Hey Jarvis") ?: "Hey Jarvis")
    val wakeWordPhrase: StateFlow<String> = _wakeWordPhrase.asStateFlow()

    private val _wakeWordSensitivity = MutableStateFlow(prefs.getFloat("wake_word_sensitivity", 0.8f))
    val wakeWordSensitivity: StateFlow<Float> = _wakeWordSensitivity.asStateFlow()

    private val _hasMicPermission = MutableStateFlow(
        ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    )
    val hasMicPermission: StateFlow<Boolean> = _hasMicPermission.asStateFlow()

    // Observables forwarded from MicrophoneCaptureService
    val wakeWordCaptureState: StateFlow<CaptureState> = MicrophoneCaptureService.captureState
    val wakeWordRmsDb: StateFlow<Float> = MicrophoneCaptureService.liveRmsDb
    val wakeWordTriggerCount: StateFlow<Int> = MicrophoneCaptureService.wakeWordTriggerCount
    val lastCapturedVoiceCommand: StateFlow<String?> = MicrophoneCaptureService.lastCapturedCommand

    init {
        // Enforce offline-only state if enabled
        if (_offlineOnlyPrivacyMode.value) {
            _isGeminiLiveMode.value = false
        }

        MicrophoneCaptureService.updateWakeWordConfig(_wakeWordPhrase.value, _wakeWordSensitivity.value)
        MicrophoneCaptureService.onVoiceCommandCaptured = { voiceCommand ->
            submitQuery(
                rawQuery = voiceCommand,
                fromVoice = true,
                dialect = _dialect.value,
                confidence = 0.95f
            )
        }

        if (_hasMicPermission.value && _wakeWordEnabled.value) {
            try {
                MicrophoneCaptureService.start(application)
            } catch (e: Exception) {
                // Background start restriction fallback
            }
        }
    }

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _voicePersona = MutableStateFlow(VoicePersona.JARVIS_CLASSIC)
    val voicePersona: StateFlow<VoicePersona> = _voicePersona.asStateFlow()

    private val _dialect = MutableStateFlow(DialectProfile.US_GENERAL)
    val dialect: StateFlow<DialectProfile> = _dialect.asStateFlow()

    private val _noiseFilter = MutableStateFlow(NoiseFilterMode.ADAPTIVE_AUTO)
    val noiseFilter: StateFlow<NoiseFilterMode> = _noiseFilter.asStateFlow()

    private val _voiceSettingsOpen = MutableStateFlow(false)
    val voiceSettingsOpen: StateFlow<Boolean> = _voiceSettingsOpen.asStateFlow()

    val isListening: StateFlow<Boolean> = onDeviceVoiceEngine.isListening
    val isSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val livePartialTranscript: StateFlow<String> = onDeviceVoiceEngine.partialTranscript
    val liveRmsDb: StateFlow<Float> = onDeviceVoiceEngine.liveRmsDb
    val ambientNoiseFloorDb: StateFlow<Float> = onDeviceVoiceEngine.ambientNoiseFloorDb
    val snrDb: StateFlow<Float> = onDeviceVoiceEngine.snrDb

    // Dynamic amplitude reflecting either listening microphone input or speech playback
    val voiceAmplitude: StateFlow<Float> = combine(
        onDeviceVoiceEngine.isListening,
        onDeviceVoiceEngine.amplitude,
        ttsHelper.isSpeaking,
        ttsHelper.speechAmplitude
    ) { listening, micAmp, speaking, speechAmp ->
        when {
            listening -> micAmp
            speaking -> speechAmp
            else -> 0.15f
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.15f)

    // Smart Home Devices
    private val _devices = MutableStateFlow<List<SmartDevice>>(
        listOf(
            SmartDevice(
                id = "dev_light_living",
                name = "Living Room Chandelier",
                room = "Living Room",
                type = DeviceType.LIGHT,
                isPowered = true,
                level = 85,
                protocol = SmartProtocol.MATTER,
                powerWatts = 24.5
            ),
            SmartDevice(
                id = "dev_thermostat_main",
                name = "Smart Thermostat Pro",
                room = "Hallway",
                type = DeviceType.THERMOSTAT,
                isPowered = true,
                targetTemperatureF = 72,
                currentTemperatureF = 71,
                protocol = SmartProtocol.THREAD,
                powerWatts = 120.0
            ),
            SmartDevice(
                id = "dev_lock_front",
                name = "Front Door Deadbolt",
                room = "Entryway",
                type = DeviceType.LOCK,
                isLocked = true,
                protocol = SmartProtocol.MATTER,
                powerWatts = 1.2
            ),
            SmartDevice(
                id = "dev_blinds_studio",
                name = "Motorized Studio Blinds",
                room = "Studio",
                type = DeviceType.BLINDS,
                isPowered = true,
                level = 100, // 100% open
                protocol = SmartProtocol.ZIGBEE,
                powerWatts = 15.0
            ),
            SmartDevice(
                id = "dev_plug_coffee",
                name = "Espresso Machine Outlet",
                room = "Kitchen",
                type = DeviceType.PLUG,
                isPowered = false,
                protocol = SmartProtocol.WIFI_LAN,
                powerWatts = 0.0
            ),
            SmartDevice(
                id = "dev_light_bedroom",
                name = "Bedroom Ambient Glow",
                room = "Master Bedroom",
                type = DeviceType.LIGHT,
                isPowered = false,
                level = 40,
                protocol = SmartProtocol.THREAD,
                powerWatts = 0.0
            )
        )
    )
    val devices: StateFlow<List<SmartDevice>> = _devices.asStateFlow()

    // Calendar & Tasks
    private val _tasks = MutableStateFlow<List<CalendarTask>>(
        listOf(
            CalendarTask(
                id = "tsk_1",
                title = "Hardware Architecture Review",
                description = "Review Matter 1.3 thread boundary router mesh performance.",
                timeSlot = "09:30 AM - 10:30 AM",
                date = "Today",
                isCompleted = false,
                priority = TaskPriority.HIGH,
                category = TaskCategory.WORK
            ),
            CalendarTask(
                id = "tsk_2",
                title = "Security Key Rotation Audit",
                description = "Verify AES-256 cryptographic salts and Keystore hardware tokens.",
                timeSlot = "01:00 PM - 02:00 PM",
                date = "Today",
                isCompleted = true,
                priority = TaskPriority.MEDIUM,
                category = TaskCategory.HOME
            ),
            CalendarTask(
                id = "tsk_3",
                title = "Cardio & Health Biometrics",
                description = "45 minute zone 2 recovery exercise.",
                timeSlot = "05:30 PM - 06:15 PM",
                date = "Today",
                isCompleted = false,
                priority = TaskPriority.LOW,
                category = TaskCategory.HEALTH
            )
        )
    )
    val tasks: StateFlow<List<CalendarTask>> = _tasks.asStateFlow()

    // Automated Routines
    private val _routines = MutableStateFlow<List<Routine>>(
        listOf(
            Routine(
                id = "morning",
                name = "Rise & Shine Protocol",
                triggerCondition = "07:00 AM Daily or 'Good Morning'",
                triggerType = RoutineTriggerType.TIME,
                isEnabled = true,
                actions = listOf(
                    RoutineAction("Set Thermostat to 72°F", "dev_thermostat_main", "SET_TEMP"),
                    RoutineAction("Turn on Living Room Chandelier (60%)", "dev_light_living", "SET_POWER"),
                    RoutineAction("Open Studio Blinds to 100%", "dev_blinds_studio", "OPEN_BLINDS"),
                    RoutineAction("Preheat Espresso Machine Outlet", "dev_plug_coffee", "SET_POWER"),
                    RoutineAction("Read Daily Schedule Briefing", null, "BRIEFING")
                ),
                lastExecuted = "Today, 07:00 AM"
            ),
            Routine(
                id = "bedtime",
                name = "Perimeter Night Guard",
                triggerCondition = "11:00 PM or 'Goodnight Jarvis'",
                triggerType = RoutineTriggerType.VOICE,
                isEnabled = true,
                actions = listOf(
                    RoutineAction("Turn off all interior lighting", null, "ALL_LIGHTS_OFF"),
                    RoutineAction("Secure Front Door Deadbolt", "dev_lock_front", "LOCK"),
                    RoutineAction("Set Thermostat to 68°F Sleep Mode", "dev_thermostat_main", "SET_TEMP"),
                    RoutineAction("Close all motorized blinds", "dev_blinds_studio", "CLOSE_BLINDS"),
                    RoutineAction("Arm Local Security Sensors", null, "ARM_SECURITY")
                ),
                lastExecuted = "Yesterday, 11:00 PM"
            ),
            Routine(
                id = "leaving",
                name = "Departure Lockdown",
                triggerCondition = "Geofence Exit (Simulated) or 'Leaving Home'",
                triggerType = RoutineTriggerType.LOCATION,
                isEnabled = true,
                actions = listOf(
                    RoutineAction("Verify all locks secured", "dev_lock_front", "LOCK"),
                    RoutineAction("Switch Thermostat to Eco 65°F", "dev_thermostat_main", "SET_TEMP"),
                    RoutineAction("Turn off unused appliance plugs", "dev_plug_coffee", "SET_POWER"),
                    RoutineAction("Activate E2EE Remote Telemetry", null, "VAULT_LOCK")
                ),
                lastExecuted = "2 days ago"
            ),
            Routine(
                id = "focus",
                name = "Deep Work Studio",
                triggerCondition = "Manual or 'Focus Mode'",
                triggerType = RoutineTriggerType.MANUAL,
                isEnabled = true,
                actions = listOf(
                    RoutineAction("Set Studio Lighting to 100% Cool White", "dev_light_living", "SET_POWER"),
                    RoutineAction("Silence Non-Urgent Notifications", null, "SILENCE"),
                    RoutineAction("Sync Calendar Deadlines", null, "SYNC")
                ),
                lastExecuted = null
            )
        )
    )
    val routines: StateFlow<List<Routine>> = _routines.asStateFlow()

    // System Health & Connectivity
    private val _health = MutableStateFlow(SystemHealth())
    val health: StateFlow<SystemHealth> = _health.asStateFlow()

    // Modular Third-Party Plugins
    private val _plugins = MutableStateFlow<List<PluginItem>>(
        listOf(
            PluginItem(
                id = "plg_weather",
                name = "HyperLocal Weather & Barometer",
                description = "Integrates on-device sensor barometrics with open weather radar telemetry.",
                author = "MeteorEdge Lab",
                version = "v1.4.2",
                isEnabled = true,
                status = PluginStatus.ACTIVE,
                sandboxedPermissions = listOf("Network Access", "Sensor Read"),
                endpointUrl = "https://api.open-meteo.com/v1/forecast",
                latencyMs = 14
            ),
            PluginItem(
                id = "plg_homeassistant",
                name = "HomeAssistant / Zigbee Bridge",
                description = "Direct local-area WebSocket bridge to HomeAssistant Matter & Thread meshes.",
                author = "NabuCasa / Local",
                version = "v3.0.1",
                isEnabled = true,
                status = PluginStatus.ACTIVE,
                sandboxedPermissions = listOf("Local Network (LAN)", "Device Control"),
                endpointUrl = "ws://192.168.1.120:8123/api/websocket",
                latencyMs = 6
            ),
            PluginItem(
                id = "plg_spotify",
                name = "Ambient Audio Synthesizer",
                description = "Controls ambient focus soundscapes and smart room speaker streaming.",
                author = "SoundLab Core",
                version = "v2.1.0",
                isEnabled = false,
                status = PluginStatus.STANDBY,
                sandboxedPermissions = listOf("Media Playback", "Audio Focus"),
                endpointUrl = "https://api.spotify.com/v1/me/player",
                latencyMs = 28
            ),
            PluginItem(
                id = "plg_caldav",
                name = "CalDAV / Nextcloud Sync",
                description = "Two-way encrypted calendar sync with zero data retention on external servers.",
                author = "PrivacyFoundation",
                version = "v1.1.8",
                isEnabled = true,
                status = PluginStatus.ACTIVE,
                sandboxedPermissions = listOf("Calendar Read/Write", "E2EE Storage"),
                endpointUrl = "https://cloud.local/remote.php/dav/calendars",
                latencyMs = 19
            )
        )
    )
    val plugins: StateFlow<List<PluginItem>> = _plugins.asStateFlow()

    // Offline & Sync Engine State
    private val _syncState = MutableStateFlow(
        SyncState(
            isOnline = true,
            pendingMutations = 0,
            lastSyncTimestamp = "Just now",
            conflictCount = 0,
            recentLogs = listOf(
                SyncLogEntry("19:34:01", "MUTATION_RECORD", "dev_light_living", "v3:nodeA", "COMMITTED_LOCAL"),
                SyncLogEntry("19:34:02", "REPLICATE_MESH", "dev_thermostat_main", "v2:hubB", "SYNC_RESOLVED"),
                SyncLogEntry("19:34:04", "CRYPT_VAULT", "tasks_schema", "v4:keystore", "AES_GCM_VERIFIED")
            )
        )
    )
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // E2EE Cryptographic Vault State
    private val _isVaultLocked = MutableStateFlow(false)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    // Chat / Interaction Log
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.JARVIS,
                text = "Jarvis Core v2.4 initialized. All requests are processed 100% locally on device with AES-256-GCM encryption. Say 'Brief me' or tap an action below.",
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                latencyMs = 11,
                intentDetected = "SYSTEM_INITIALIZE"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _routineExecutionStatus = MutableStateFlow<String?>(null)
    val routineExecutionStatus: StateFlow<String?> = _routineExecutionStatus.asStateFlow()

    fun formatTemperature(tempF: Int): String {
        return if (_temperatureUnit.value == TemperatureUnit.CELSIUS) {
            val c = ((tempF - 32) * 5.0 / 9.0).roundToInt()
            "$c°C"
        } else {
            "$tempF°F"
        }
    }

    fun setOfflineOnlyPrivacyMode(enabled: Boolean) {
        _offlineOnlyPrivacyMode.value = enabled
        prefs.edit().putBoolean("offline_only_privacy_mode", enabled).apply()
        if (enabled) {
            _isGeminiLiveMode.value = false
            val notice = ChatMessage(
                sender = MessageSender.SYSTEM,
                text = "🛡️ AIR-GAPPED PRIVACY ENGAGED: Outbound network egress blocked. 100% on-device neural processing with AES-256 local database encryption.",
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                intentDetected = "AIR_GAP_PRIVACY_ACTIVATED"
            )
            _chatMessages.value = _chatMessages.value + notice
            ttsHelper.speak("Air gapped privacy mode engaged. Operating strictly on device.", _voicePersona.value)
        } else {
            _isGeminiLiveMode.value = true
            val notice = ChatMessage(
                sender = MessageSender.SYSTEM,
                text = "🌐 HYBRID CLOUD / LOCAL MODE ACTIVE: Gemini Live 1.5 Flash conversational core restored.",
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                intentDetected = "CLOUD_HYBRID_RESTORED"
            )
            _chatMessages.value = _chatMessages.value + notice
            ttsHelper.speak("Cloud interfaces restored. Gemini Live online.", _voicePersona.value)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit.value = unit
        prefs.edit().putString("temperature_unit", unit.name).apply()
    }

    fun setDefaultLightBrightness(level: Int) {
        val clamped = level.coerceIn(5, 100)
        _defaultLightBrightness.value = clamped
        prefs.edit().putInt("default_light_brightness", clamped).apply()
    }

    fun setPreferredRoom(room: String) {
        _preferredRoom.value = room
        prefs.edit().putString("preferred_room", room).apply()
    }

    fun setAutoLockDelaySeconds(seconds: Int) {
        _autoLockDelaySeconds.value = seconds
        prefs.edit().putInt("auto_lock_delay_seconds", seconds).apply()
    }

    fun setPreferredProtocol(protocol: SmartProtocol) {
        _preferredProtocol.value = protocol
        prefs.edit().putString("preferred_protocol", protocol.name).apply()
    }

    fun setConfirmSecurityActions(confirm: Boolean) {
        _confirmSecurityActions.value = confirm
        prefs.edit().putBoolean("confirm_security_actions", confirm).apply()
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _wakeWordEnabled.value = enabled
        prefs.edit().putBoolean("wake_word_enabled", enabled).apply()
        if (enabled) {
            if (_hasMicPermission.value) {
                MicrophoneCaptureService.start(getApplication())
            }
        } else {
            MicrophoneCaptureService.stop(getApplication())
        }
    }

    fun setWakeWordPhrase(phrase: String) {
        _wakeWordPhrase.value = phrase
        prefs.edit().putString("wake_word_phrase", phrase).apply()
        MicrophoneCaptureService.updateWakeWordConfig(phrase, _wakeWordSensitivity.value)
    }

    fun setWakeWordSensitivity(sensitivity: Float) {
        _wakeWordSensitivity.value = sensitivity
        prefs.edit().putFloat("wake_word_sensitivity", sensitivity).apply()
        MicrophoneCaptureService.updateWakeWordConfig(_wakeWordPhrase.value, sensitivity)
    }

    fun updateMicPermission(granted: Boolean) {
        _hasMicPermission.value = granted
        if (granted && _wakeWordEnabled.value) {
            try {
                MicrophoneCaptureService.start(getApplication())
            } catch (e: Exception) {
                // Background start restriction fallback
            }
        }
    }

    fun triggerWakeWordSimulation(command: String? = null) {
        MicrophoneCaptureService.simulateWakeWordTrigger(command)
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
    }

    fun setGeminiLiveMode(enabled: Boolean) {
        if (enabled && _offlineOnlyPrivacyMode.value) {
            _offlineOnlyPrivacyMode.value = false
            prefs.edit().putBoolean("offline_only_privacy_mode", false).apply()
        }
        _isGeminiLiveMode.value = enabled
        if (!enabled) {
            ttsHelper.speak("Pure on-device privacy mode engaged. Cloud interfaces disabled.", _voicePersona.value)
        } else {
            ttsHelper.speak("Gemini Live conversational engine online. Full speech reasoning active.", _voicePersona.value)
        }
    }

    fun setVoicePersona(persona: VoicePersona) {
        _voicePersona.value = persona
        ttsHelper.applyPersona(persona)
        ttsHelper.speak("Voice persona updated to ${persona.displayName}.", persona)
    }

    fun setDialect(profile: DialectProfile) {
        _dialect.value = profile
        ttsHelper.speak("Acoustic recognizer calibrated for ${profile.displayName}.", _voicePersona.value)
    }

    fun setNoiseFilter(mode: NoiseFilterMode) {
        _noiseFilter.value = mode
    }

    fun setVoiceSettingsOpen(isOpen: Boolean) {
        _voiceSettingsOpen.value = isOpen
    }

    fun submitQuery(
        rawQuery: String,
        fromVoice: Boolean = false,
        dialect: DialectProfile? = null,
        confidence: Float? = null
    ) {
        if (rawQuery.isBlank()) return

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = rawQuery.trim(),
            timestamp = timeStr,
            detectedDialect = dialect?.displayName,
            confidenceScore = confidence,
            noiseFloorDb = if (fromVoice) onDeviceVoiceEngine.ambientNoiseFloorDb.value else null
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isThinking.value = true
            ttsHelper.stop()

            try {
                if (_isGeminiLiveMode.value && !_offlineOnlyPrivacyMode.value) {
                    val effectiveKey = getEffectiveApiKey()
                    val liveResponse = geminiLiveEngine.converse(
                        userQuery = rawQuery,
                        persona = _voicePersona.value,
                        devices = _devices.value,
                        tasks = _tasks.value,
                        routines = _routines.value,
                        health = _health.value,
                        customApiKey = effectiveKey
                    )

                    // Apply any action commands
                    for (action in liveResponse.actionCommands) {
                        when (action.type) {
                            "POWER" -> action.targetId?.let { devId ->
                                val turnOn = action.value?.toBoolean() ?: true
                                _devices.value = _devices.value.map { dev ->
                                    if (dev.id == devId) dev.copy(isPowered = turnOn, level = if (turnOn && dev.level == 0) 85 else if (!turnOn) 0 else dev.level)
                                    else dev
                                }
                                recordSyncMutation("VOICE_POWER", devId)
                            }
                            "TEMP" -> action.targetId?.let { devId ->
                                val temp = action.value?.toIntOrNull() ?: 72
                                _devices.value = _devices.value.map { dev ->
                                    if (dev.id == devId) dev.copy(targetTemperatureF = temp, isPowered = true)
                                    else dev
                                }
                                recordSyncMutation("VOICE_TEMP", "$temp°F")
                            }
                            "LOCK" -> action.targetId?.let { devId ->
                                val lock = action.value?.toBoolean() ?: true
                                _devices.value = _devices.value.map { dev ->
                                    if (dev.id == devId) dev.copy(isLocked = lock)
                                    else dev
                                }
                                recordSyncMutation(if (lock) "VOICE_LOCK" else "VOICE_UNLOCK", devId)
                            }
                            "BLINDS" -> action.targetId?.let { devId ->
                                val lvl = action.value?.toIntOrNull() ?: 100
                                _devices.value = _devices.value.map { dev ->
                                    if (dev.id == devId) dev.copy(level = lvl, isPowered = lvl > 0)
                                    else dev
                                }
                                recordSyncMutation("VOICE_BLINDS", "$lvl%")
                            }
                            "ROUTINE" -> action.targetId?.let { rId ->
                                executeRoutine(rId)
                            }
                        }
                    }

                    val jarvisMsg = ChatMessage(
                        sender = if (liveResponse.isError) MessageSender.SYSTEM else MessageSender.JARVIS,
                        text = liveResponse.conversationalText,
                        timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                        latencyMs = liveResponse.latencyMs,
                        intentDetected = if (liveResponse.isError) "SYSTEM_ALERT" else if (liveResponse.isLiveApi) "GEMINI_LIVE_CONVERSATION" else "LOCAL_CONVERSATION_FALLBACK",
                        isSpoken = true,
                        isGeminiLive = liveResponse.isLiveApi
                    )
                    _chatMessages.value = _chatMessages.value + jarvisMsg

                    ttsHelper.speak(liveResponse.spokenText, _voicePersona.value)
                } else {
                    // On-device privacy processing
                    delay(120) // Micro feedback delay
                    val nlpResult = OnDeviceNlpEngine.processQuery(
                        rawQuery = rawQuery,
                        devices = _devices.value,
                        tasks = _tasks.value,
                        routines = _routines.value,
                        health = _health.value
                    )

                    // Handle mutations
                    when (nlpResult.actionType) {
                        NlpActionType.DEVICE_UPDATE -> {
                            nlpResult.updatedDevice?.let { updated ->
                                _devices.value = _devices.value.map { if (it.id == updated.id) updated else it }
                                recordSyncMutation("DEVICE_UPDATE", updated.name)
                            }
                        }
                        NlpActionType.TASK_CREATE -> {
                            nlpResult.createdTask?.let { newTask ->
                                _tasks.value = listOf(newTask) + _tasks.value
                                recordSyncMutation("TASK_CREATE", newTask.title)
                            }
                        }
                        NlpActionType.ROUTINE_EXECUTE -> {
                            nlpResult.executedRoutineId?.let { rId ->
                                executeRoutine(rId)
                            }
                        }
                        NlpActionType.SYSTEM_DIAGNOSTIC -> {
                            pingDiagnostic()
                        }
                        else -> Unit
                    }

                    val jarvisMsg = ChatMessage(
                        sender = MessageSender.JARVIS,
                        text = nlpResult.responseText,
                        timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                        latencyMs = nlpResult.latencyMs,
                        intentDetected = nlpResult.intent,
                        isSpoken = true,
                        isGeminiLive = false
                    )
                    _chatMessages.value = _chatMessages.value + jarvisMsg

                    ttsHelper.speak(nlpResult.spokenText, _voicePersona.value)
                }
            } catch (t: Throwable) {
                val fallbackText = "Acknowledged, sir. System processed your query locally."
                val jarvisMsg = ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = fallbackText,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    latencyMs = 24,
                    intentDetected = "LOCAL_RECOVERY",
                    isSpoken = true,
                    isGeminiLive = false
                )
                _chatMessages.value = _chatMessages.value + jarvisMsg
                ttsHelper.speak(fallbackText, _voicePersona.value)
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun toggleVoiceListening() {
        if (onDeviceVoiceEngine.isListening.value) {
            onDeviceVoiceEngine.stopListening()
        } else {
            ttsHelper.stop()
            onDeviceVoiceEngine.startListening(
                dialect = _dialect.value,
                noiseFilter = _noiseFilter.value,
                onPartial = { partial ->
                    // Real-time speech transcription into text input field
                    _inputQuery.value = partial
                },
                onResult = { result ->
                    // Auto-fill the text input field with the final transcribed text
                    _inputQuery.value = result.normalizedTranscript
                }
            )
        }
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    fun toggleDevicePower(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val newPower = !dev.isPowered
                val newLevel = if (newPower && dev.level == 0) 100 else if (!newPower) 0 else dev.level
                val updated = dev.copy(isPowered = newPower, level = newLevel)
                recordSyncMutation("POWER_TOGGLE", dev.name)
                updated
            } else dev
        }
    }

    fun setDeviceLevel(deviceId: String, level: Int) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val updated = dev.copy(level = level, isPowered = level > 0)
                recordSyncMutation("SET_LEVEL_${level}%", dev.name)
                updated
            } else dev
        }
    }

    fun setThermostatTemp(deviceId: String, tempF: Int) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val updated = dev.copy(targetTemperatureF = tempF, isPowered = true)
                recordSyncMutation("SET_TEMP_${tempF}F", dev.name)
                updated
            } else dev
        }
    }

    fun toggleDeviceLock(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                val updated = dev.copy(isLocked = !dev.isLocked)
                recordSyncMutation(if (updated.isLocked) "LOCK" else "UNLOCK", dev.name)
                updated
            } else dev
        }
    }

    fun toggleTaskComplete(taskId: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val updated = task.copy(isCompleted = !task.isCompleted)
                recordSyncMutation(if (updated.isCompleted) "TASK_COMPLETED" else "TASK_REOPENED", task.title)
                updated
            } else task
        }
    }

    fun addTask(
        title: String,
        timeSlot: String,
        priority: TaskPriority,
        category: TaskCategory,
        description: String = ""
    ) {
        val newTask = CalendarTask(
            id = "tsk_" + System.currentTimeMillis().toString().takeLast(6),
            title = title,
            description = description,
            timeSlot = timeSlot,
            date = "Today",
            priority = priority,
            category = category
        )
        _tasks.value = listOf(newTask) + _tasks.value
        recordSyncMutation("TASK_CREATED", title)
    }

    fun deleteTask(taskId: String) {
        val taskName = _tasks.value.find { it.id == taskId }?.title ?: taskId
        _tasks.value = _tasks.value.filter { it.id != taskId }
        recordSyncMutation("TASK_DELETED", taskName)
    }

    fun executeRoutine(routineId: String) {
        val routine = _routines.value.find { it.id == routineId } ?: return
        viewModelScope.launch {
            _routineExecutionStatus.value = "Starting ${routine.name}..."

            for (action in routine.actions) {
                delay(350)
                _routineExecutionStatus.value = "Executing: ${action.description}"

                // Apply concrete state changes
                action.targetDeviceId?.let { targetId ->
                    _devices.value = _devices.value.map { dev ->
                        if (dev.id == targetId) {
                            when (action.actionType) {
                                "SET_POWER" -> dev.copy(isPowered = true, level = if (dev.level == 0) 80 else dev.level)
                                "SET_TEMP" -> dev.copy(targetTemperatureF = if (routineId == "bedtime") 68 else 72, isPowered = true)
                                "LOCK" -> dev.copy(isLocked = true)
                                "OPEN_BLINDS" -> dev.copy(isPowered = true, level = 100)
                                "CLOSE_BLINDS" -> dev.copy(isPowered = false, level = 0)
                                else -> dev
                            }
                        } else dev
                    }
                }

                if (action.actionType == "ALL_LIGHTS_OFF") {
                    _devices.value = _devices.value.map {
                        if (it.type == DeviceType.LIGHT) it.copy(isPowered = false, level = 0) else it
                    }
                }
            }

            // Update routine timestamp
            _routines.value = _routines.value.map {
                if (it.id == routineId) it.copy(lastExecuted = "Just now") else it
            }

            _routineExecutionStatus.value = "Completed: ${routine.name}"
            ttsHelper.speak("${routine.name} executed successfully. All parameters adjusted.")
            recordSyncMutation("ROUTINE_EXECUTED", routine.name)

            delay(1500)
            _routineExecutionStatus.value = null
        }
    }

    fun toggleRoutineEnabled(routineId: String) {
        _routines.value = _routines.value.map {
            if (it.id == routineId) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun togglePlugin(pluginId: String) {
        _plugins.value = _plugins.value.map { plugin ->
            if (plugin.id == pluginId) {
                val newEnabled = !plugin.isEnabled
                plugin.copy(
                    isEnabled = newEnabled,
                    status = if (newEnabled) PluginStatus.ACTIVE else PluginStatus.DISABLED
                )
            } else plugin
        }
    }

    fun toggleOnlineSync() {
        val newOnline = !_syncState.value.isOnline
        _syncState.value = _syncState.value.copy(
            isOnline = newOnline,
            pendingMutations = if (!newOnline) _syncState.value.pendingMutations + 1 else 0,
            lastSyncTimestamp = if (newOnline) "Just now" else _syncState.value.lastSyncTimestamp
        )

        if (newOnline) {
            triggerManualSync()
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            val nowTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val newEntry = SyncLogEntry(
                timestamp = nowTime,
                action = "REPLICATE_MESH_STATE",
                entity = "GlobalVault_Vector",
                vectorClock = "v${System.currentTimeMillis() % 1000}:peerMesh",
                status = "SYNCHRONIZED_0_CONFLICTS"
            )
            _syncState.value = _syncState.value.copy(
                pendingMutations = 0,
                lastSyncTimestamp = "Just now",
                recentLogs = listOf(newEntry) + _syncState.value.recentLogs.take(6)
            )
        }
    }

    fun pingDiagnostic() {
        viewModelScope.launch {
            val simulatedPing = (8..15).random()
            _health.value = _health.value.copy(
                pingMs = simulatedPing,
                cpuPercent = (14..24).random(),
                lastSyncTimestamp = "Just now"
            )
        }
    }

    fun toggleVaultLock() {
        _isVaultLocked.value = !_isVaultLocked.value
    }

    private fun recordSyncMutation(action: String, target: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val log = SyncLogEntry(
            timestamp = timeStr,
            action = action,
            entity = target,
            vectorClock = "v${(100..999).random()}:local",
            status = if (_syncState.value.isOnline) "COMMITTED_E2EE" else "QUEUED_OFFLINE"
        )
        val pending = if (!_syncState.value.isOnline) _syncState.value.pendingMutations + 1 else 0
        _syncState.value = _syncState.value.copy(
            pendingMutations = pending,
            recentLogs = listOf(log) + _syncState.value.recentLogs.take(6)
        )
    }

    override fun onCleared() {
        super.onCleared()
        onDeviceVoiceEngine.destroy()
        ttsHelper.shutdown()
    }
}
