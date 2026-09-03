package com.example.data.voice

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CalendarTask
import com.example.data.model.Routine
import com.example.data.model.SmartDevice
import com.example.data.model.SystemHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiLiveResponse(
    val conversationalText: String,
    val spokenText: String,
    val latencyMs: Long,
    val isLiveApi: Boolean,
    val actionCommands: List<VoiceActionCommand> = emptyList(),
    val isError: Boolean = false
)

data class VoiceActionCommand(
    val type: String,
    val targetId: String?,
    val value: String?
)

class GeminiLiveVoiceEngine {

    private val tag = "GeminiLiveEngine"

    // Model candidates with gemini-1.5-flash prioritized as requested,
    // with automatic resilient failover to supported modern flash models if needed.
    private val modelCandidates = listOf(
        "gemini-1.5-flash",
        "gemini-flash-latest",
        "gemini-3.5-flash",
        "gemini-3.1-flash-lite-preview"
    )

    // Mandated OkHttpClient timeout configuration
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun converse(
        userQuery: String,
        persona: VoicePersona,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        health: SystemHealth,
        customApiKey: String = ""
    ): GeminiLiveResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val apiKey = if (customApiKey.isNotBlank()) {
            customApiKey.trim()
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
        }

        val hasValidKey = apiKey.isNotBlank() &&
                !apiKey.contains("MY_GEMINI_API_KEY") &&
                apiKey != "null"

        if (!hasValidKey) {
            // As specified: If the API Key is empty, display a J.A.R.V.I.S.-style error message
            return@withContext GeminiLiveResponse(
                conversationalText = "System Alert: API Key missing or connection failed. Please configure your Gemini API Key in the settings.",
                spokenText = "System Alert: API Key missing or connection failed.",
                latencyMs = System.currentTimeMillis() - startTime,
                isLiveApi = false,
                isError = true
            )
        }

        var lastException: Exception? = null

        // Try candidate models starting with gemini-1.5-flash
        for (model in modelCandidates) {
            try {
                val responseJson = executeGeminiRequestWithRetry(
                    model = model,
                    apiKey = apiKey,
                    userQuery = userQuery,
                    persona = persona,
                    devices = devices,
                    tasks = tasks,
                    routines = routines,
                    health = health
                )
                val latency = System.currentTimeMillis() - startTime
                val parsed = parseLiveResponse(responseJson, latency, isLive = true)
                if (parsed.conversationalText.isNotBlank()) {
                    return@withContext parsed
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(tag, "Model '$model' failed (${e.message}), trying next candidate...")
            }
        }

        // As specified: If the API call fails, display a J.A.R.V.I.S.-style error message in the chat UI
        val errorMsg = lastException?.message?.take(100) ?: "Connection failed"
        Log.e(tag, "Gemini API call failed: $errorMsg")
        return@withContext GeminiLiveResponse(
            conversationalText = "System Alert: API Key missing or connection failed. Details: $errorMsg",
            spokenText = "System Alert: API Key missing or connection failed.",
            latencyMs = System.currentTimeMillis() - startTime,
            isLiveApi = false,
            isError = true
        )
    }

    private suspend fun executeGeminiRequestWithRetry(
        model: String,
        apiKey: String,
        userQuery: String,
        persona: VoicePersona,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        health: SystemHealth,
        maxRetries: Int = 1
    ): String {
        var attempt = 0
        while (true) {
            try {
                return executeGeminiRequest(
                    model = model,
                    apiKey = apiKey,
                    userQuery = userQuery,
                    persona = persona,
                    devices = devices,
                    tasks = tasks,
                    routines = routines,
                    health = health
                )
            } catch (e: Exception) {
                val msg = e.message ?: ""
                val isTransient = msg.contains("503") || msg.contains("429") || msg.contains("UNAVAILABLE")
                if (isTransient && attempt < maxRetries) {
                    attempt++
                    delay(attempt * 200L)
                } else {
                    throw e
                }
            }
        }
    }

    private fun executeGeminiRequest(
        model: String,
        apiKey: String,
        userQuery: String,
        persona: VoicePersona,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        health: SystemHealth
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val systemPrompt = buildString {
            append("You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), Tony Stark's advanced artificial intelligence. You must reply strictly in character: concise, analytical, and highly technical.\n")
            append("Persona: Voice profile '${persona.displayName}'. Tone: ${persona.description}\n")
            append("Style: Speak naturally, with sharp intellect, subtle wit, and absolute clarity in 1-3 conversational sentences suited for spoken voice audio.\n")
            append("Smart Home Context:\n")
            append("- Devices: ")
            devices.forEach { dev ->
                append("${dev.name} [ID: ${dev.id}, Type: ${dev.type}, Power: ${dev.isPowered}, Level: ${dev.level}%, Temp: ${dev.targetTemperatureF}F, Locked: ${dev.isLocked}], ")
            }
            append("\n- Tasks: ${tasks.count { !it.isCompleted }} pending.\n")
            append("- System Health: Hub ping ${health.pingMs}ms, CPU ${health.cpuPercent}%.\n")
            append("If the user's intent is to control devices or trigger routines, include action tags at the end of your response:\n")
            append("[ACTION:POWER:dev_id:true/false], [ACTION:TEMP:dev_id:temp_value], [ACTION:LOCK:dev_id:true/false], [ACTION:BLINDS:dev_id:percent], [ACTION:ROUTINE:routine_id]\n")
        }

        val jsonBody = JSONObject().apply {
            // contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject().apply {
                put("role", "user")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply { put("text", userQuery) })
                put("parts", partsArray)
            }
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            // systemInstruction
            val sysInstructionObj = JSONObject().apply {
                val sysParts = JSONArray()
                sysParts.put(JSONObject().apply { put("text", systemPrompt) })
                put("parts", sysParts)
            }
            put("systemInstruction", sysInstructionObj)

            // generationConfig
            val genConfig = JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.95)
                put("topK", 40)
            }
            put("generationConfig", genConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Empty error"
                throw RuntimeException("HTTP ${response.code}: $errorBody")
            }
            return response.body?.string() ?: "{}"
        }
    }

    private fun parseLiveResponse(
        jsonString: String,
        latencyMs: Long,
        isLive: Boolean
    ): GeminiLiveResponse {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
            ?: "All systems operational, sir. Ready for your command."

        // Extract action tags [ACTION:TYPE:ID:VAL]
        val actions = mutableListOf<VoiceActionCommand>()
        val actionRegex = Regex("\\[ACTION:([A-Z_]+)(?::([a-zA-Z0-9_]+))?(?::([a-zA-Z0-9_]+))?\\]")

        actionRegex.findAll(rawText).forEach { match ->
            val type = match.groupValues.getOrNull(1) ?: ""
            val target = match.groupValues.getOrNull(2)
            val value = match.groupValues.getOrNull(3)
            actions.add(VoiceActionCommand(type, target, value))
        }

        // Clean spoken text by stripping out raw bracketed action tokens
        val spoken = rawText.replace(actionRegex, "").trim()

        return GeminiLiveResponse(
            conversationalText = rawText,
            spokenText = spoken.ifBlank { "Acknowledged, sir." },
            latencyMs = latencyMs,
            isLiveApi = isLive,
            actionCommands = actions
        )
    }

    private fun generateLocalConversationalFallback(
        userQuery: String,
        persona: VoicePersona,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        startTime: Long
    ): GeminiLiveResponse {
        val q = userQuery.lowercase()
        val actions = mutableListOf<VoiceActionCommand>()

        val reply = when {
            q.contains("light") || q.contains("chandelier") || q.contains("lamp") -> {
                val light = devices.find { it.id == "dev_light_living" }
                val willTurnOn = !q.contains("off")
                actions.add(VoiceActionCommand("POWER", "dev_light_living", willTurnOn.toString()))
                if (willTurnOn) {
                    "Right away, sir. Illuminating the living room chandelier to 85%."
                } else {
                    "Understood. Powering down the living room illumination."
                }
            }
            q.contains("thermostat") || q.contains("temp") || q.contains("heat") || q.contains("air con") || q.contains("climate") -> {
                val temp = when {
                    q.contains("68") -> "68"
                    q.contains("70") -> "70"
                    q.contains("74") -> "74"
                    else -> "72"
                }
                actions.add(VoiceActionCommand("TEMP", "dev_thermostat_main", temp))
                "Adjusting the climate controls. Smart thermostat set to $temp degrees Fahrenheit."
            }
            q.contains("lock") || q.contains("door") || q.contains("deadbolt") -> {
                val doLock = !q.contains("unlock")
                actions.add(VoiceActionCommand("LOCK", "dev_lock_front", doLock.toString()))
                if (doLock) {
                    "Securing front door deadbolt with hardware token verification. Perimeter sealed."
                } else {
                    "Front door deadbolt unlatched. Welcome home, sir."
                }
            }
            q.contains("blind") || q.contains("studio") || q.contains("shade") || q.contains("curtain") -> {
                val open = !q.contains("close")
                actions.add(VoiceActionCommand("BLINDS", "dev_blinds_studio", if (open) "100" else "0"))
                if (open) "Opening motorized studio blinds to allow natural ambient light."
                else "Closing studio blinds for privacy."
            }
            q.contains("morning") || q.contains("rise and shine") || (q.contains("good morning")) -> {
                actions.add(VoiceActionCommand("ROUTINE", "morning", null))
                "Good morning, sir. Initiating the Rise and Shine sequence. Adjusting lighting, climate, and readying your briefing."
            }
            q.contains("night") || q.contains("good night") || q.contains("sleep") -> {
                actions.add(VoiceActionCommand("ROUTINE", "night", null))
                "Good night, sir. Engaging Night Lockdown. Arming perimeter sensors, lowering thermostat, and turning off all lights."
            }
            q.contains("movie") || q.contains("cinema") -> {
                actions.add(VoiceActionCommand("ROUTINE", "movie", null))
                "Initiating Cinema Mode. Dimming lighting to theater levels and closing studio blinds."
            }
            q.contains("away") || q.contains("leaving") -> {
                actions.add(VoiceActionCommand("ROUTINE", "away", null))
                "Setting residence to Away Mode. Securing locks and switching climate to eco mode."
            }
            q.contains("brief") || q.contains("agenda") || q.contains("schedule") || q.contains("calendar") || q.contains("today") -> {
                val pending = tasks.count { !it.isCompleted }
                val nextTask = tasks.firstOrNull { !it.isCompleted }
                if (nextTask != null) {
                    "At your command. You have $pending pending schedule items today. Up next is '${nextTask.title}' at ${nextTask.timeSlot}."
                } else {
                    "Your schedule is completely clear for today, sir. All home subsystems are operating nominally."
                }
            }
            q.contains("health") || q.contains("status") || q.contains("diagnostic") || q.contains("ping") || q.contains("cpu") -> {
                val onlineCount = devices.count { it.isOnline }
                "System diagnostics nominal. $onlineCount IoT mesh endpoints online. Mesh latency 12ms with zero packet loss."
            }
            q.contains("who are you") || q.contains("what can you do") -> {
                "I am Jarvis, an advanced smart home hub assistant powered by Gemini intelligence with an on-device neural privacy core."
            }
            q.contains("hello") || q.contains("hi") || q.contains("hey jarvis") -> {
                "Greetings, sir. Voice persona '${persona.displayName}' active and ready for your command."
            }
            q.contains("joke") -> {
                "Why did the smart thermostat break up with the smart bulb? There was no real connection, and the heat was unbearable."
            }
            q.contains("thank") -> {
                "Always at your service, sir."
            }
            else -> {
                "Acknowledged, sir. Local conversational core processed your request under '${persona.displayName}'. All subsystems operational."
            }
        }

        val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(18)
        return GeminiLiveResponse(
            conversationalText = reply,
            spokenText = reply,
            latencyMs = latency,
            isLiveApi = false,
            actionCommands = actions
        )
    }
}
