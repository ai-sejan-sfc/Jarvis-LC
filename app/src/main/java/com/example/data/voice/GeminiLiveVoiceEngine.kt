package com.example.data.voice

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CalendarTask
import com.example.data.model.Routine
import com.example.data.model.SmartDevice
import com.example.data.model.SystemHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class GeminiLiveResponse(
    val conversationalText: String,
    val spokenText: String,
    val latencyMs: Long,
    val isLiveApi: Boolean,
    val actionCommands: List<VoiceActionCommand> = emptyList(),
    val isError: Boolean = false,
    val selectedModelName: String? = null
)

data class VoiceActionCommand(
    val type: String,
    val targetId: String?,
    val value: String?
)

class GeminiLiveVoiceEngine {

    private val tag = "GeminiLiveEngine"

    // OkHttpClient with 30s timeout configuration
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

        // Error Handling: API key missing error displayed as a system alert in the chat interface
        if (!hasValidKey) {
            return@withContext GeminiLiveResponse(
                conversationalText = "System Alert: API Key is missing. Please configure your Gemini API Key in the settings to enable AI features.",
                spokenText = "System Alert: API Key is missing. Please configure your Gemini API Key in settings.",
                latencyMs = System.currentTimeMillis() - startTime,
                isLiveApi = false,
                isError = true,
                selectedModelName = null
            )
        }

        try {
            // Dynamic API Model Selection:
            // First make a GET request to https://generativelanguage.googleapis.com/v1beta/models?key=[API_KEY]
            val selectedModel = fetchBestAvailableModel(apiKey)
            Log.d(tag, "Dynamically selected text-generation model: $selectedModel")

            // Construct subsequent POST request URL dynamically using this selected model name
            val responseJson = executeGeminiPostRequest(
                selectedModel = selectedModel,
                apiKey = apiKey,
                userQuery = userQuery,
                persona = persona,
                devices = devices,
                tasks = tasks,
                routines = routines,
                health = health
            )

            val latency = System.currentTimeMillis() - startTime
            return@withContext parseLiveResponse(
                jsonString = responseJson,
                latencyMs = latency,
                isLive = true,
                modelName = selectedModel
            )
        } catch (e: Exception) {
            Log.e(tag, "Gemini request failed: ${e.message}", e)
            val latency = System.currentTimeMillis() - startTime

            // Error Handling: Graceful network failure and authentication alerts
            val alertMessage = when {
                e is UnknownHostException || e is SocketTimeoutException || e is IOException -> {
                    "System Alert: Network failure. Unable to reach Gemini API servers. Please check your internet connection and retry."
                }
                e.message?.contains("400") == true || e.message?.contains("403") == true || e.message?.contains("API_KEY_INVALID") == true -> {
                    "System Alert: Invalid API Key or authentication failed. Please verify your Gemini API Key in settings."
                }
                e.message?.contains("404") == true -> {
                    "System Alert: Selected model endpoint not found on Gemini API."
                }
                else -> {
                    "System Alert: ${e.message?.take(150) ?: "Network failure or communication error while contacting AI service."}"
                }
            }

            return@withContext GeminiLiveResponse(
                conversationalText = alertMessage,
                spokenText = "System Alert: Communication failed. Please check network connection or API key.",
                latencyMs = latency,
                isLiveApi = false,
                isError = true,
                selectedModelName = null
            )
        }
    }

    /**
     * Dynamic API Model Selection:
     * Makes a GET request to https://generativelanguage.googleapis.com/v1beta/models?key=[API_KEY]
     * Parses the response to automatically find and select the best available text-generation model
     * associated with that specific API key, prioritizing the latest pro or flash models.
     */
    private fun fetchBestAvailableModel(apiKey: String): String {
        val listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        val request = Request.Builder()
            .url(listUrl)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("HTTP ${response.code} fetching model list: $errorBody")
            }

            val bodyString = response.body?.string() ?: "{}"
            val root = JSONObject(bodyString)
            val modelsArray = root.optJSONArray("models") ?: JSONArray()

            val eligibleModels = mutableListOf<String>()
            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.optJSONObject(i) ?: continue
                val name = modelObj.optString("name")
                val methodsArray = modelObj.optJSONArray("supportedGenerationMethods")

                var supportsGenerateContent = false
                if (methodsArray != null) {
                    for (j in 0 until methodsArray.length()) {
                        if (methodsArray.optString(j) == "generateContent") {
                            supportsGenerateContent = true
                            break
                        }
                    }
                }

                if (supportsGenerateContent && name.isNotBlank()) {
                    eligibleModels.add(name)
                }
            }

            if (eligibleModels.isEmpty()) {
                throw IOException("No models supporting 'generateContent' found for this API key.")
            }

            return selectBestModel(eligibleModels)
        }
    }

    /**
     * Selects the highest priority text-generation model, prioritizing latest pro or flash models.
     */
    private fun selectBestModel(models: List<String>): String {
        fun priorityScore(rawName: String): Int {
            val name = rawName.lowercase()
            return when {
                name.contains("2.5-pro") -> 200
                name.contains("2.0-pro") -> 190
                name.contains("2.5-flash") -> 180
                name.contains("2.0-flash") -> 170
                name.contains("1.5-pro") -> 160
                name.contains("1.5-flash") -> 150
                name.contains("pro-latest") -> 140
                name.contains("flash-latest") -> 130
                name.contains("pro") && !name.contains("vision") -> 110
                name.contains("flash") -> 100
                name.contains("gemini") -> 80
                else -> 20
            }
        }

        return models.maxByOrNull { priorityScore(it) } ?: models.first()
    }

    /**
     * Constructs the subsequent POST request URL dynamically using the selected model name,
     * and includes the strict friendly Bengali persona system instruction in the payload.
     */
    private fun executeGeminiPostRequest(
        selectedModel: String,
        apiKey: String,
        userQuery: String,
        persona: VoicePersona,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        health: SystemHealth
    ): String {
        // Construct subsequent POST request URL dynamically using this selected model name
        val modelPath = if (selectedModel.startsWith("models/")) selectedModel else "models/$selectedModel"
        val postUrl = "https://generativelanguage.googleapis.com/v1beta/$modelPath:generateContent?key=$apiKey"

        // Strict Friendly Bengali Persona instruction mandated by the prompt
        val bengaliSystemInstruction = "You are a highly advanced, yet incredibly warm, friendly, and supportive AI assistant. You must always communicate and respond to the user exclusively in friendly, conversational Bengali language, maintaining a polite and helpful tone."

        val systemPrompt = buildString {
            append(bengaliSystemInstruction)
            append("\n\nSmart Home Integration & Execution Directives:\n")
            append("You also control the user's smart home devices, automated routines, and calendar. Whenever the user asks to control devices or trigger routines (such as switching lights on/off, adjusting temperature, locking doors, or running morning/night routines), respond in warm, polite, and helpful conversational Bengali and append the corresponding action tags at the end of your response:\n")
            append("[ACTION:POWER:dev_id:true/false], [ACTION:TEMP:dev_id:temp_value], [ACTION:LOCK:dev_id:true/false], [ACTION:BLINDS:dev_id:percent], [ACTION:ROUTINE:routine_id]\n")
            append("Connected Smart Home Devices:\n")
            devices.forEach { dev ->
                append("- ${dev.name} [ID: ${dev.id}, Type: ${dev.type}, Powered: ${dev.isPowered}, Level: ${dev.level}%, Temp: ${dev.targetTemperatureF}F, Locked: ${dev.isLocked}]\n")
            }
            append("Pending Tasks: ${tasks.count { !it.isCompleted }}\n")
            append("System Status: Ping ${health.pingMs}ms, CPU ${health.cpuPercent}%\n")
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

            // systemInstruction with the friendly Bengali persona
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
            .url(postUrl)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Empty error"
                throw IOException("HTTP ${response.code}: $errorBody")
            }
            return response.body?.string() ?: "{}"
        }
    }

    private fun parseLiveResponse(
        jsonString: String,
        latencyMs: Long,
        isLive: Boolean,
        modelName: String
    ): GeminiLiveResponse {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text")?.takeIf { it.isNotBlank() }
            ?: "সব সিস্টেম সচল রয়েছে। আমি আপনাকে কীভাবে সহায়তা করতে পারি?"

        // Extract action tags [ACTION:TYPE:ID:VAL]
        val actions = mutableListOf<VoiceActionCommand>()
        val actionRegex = Regex("\\[ACTION:([A-Z_]+)(?::([a-zA-Z0-9_]+))?(?::([a-zA-Z0-9_]+))?\\]")

        actionRegex.findAll(rawText).forEach { match ->
            val type = match.groupValues.getOrNull(1) ?: ""
            val target = match.groupValues.getOrNull(2)
            val value = match.groupValues.getOrNull(3)
            actions.add(VoiceActionCommand(type, target, value))
        }

        // Clean spoken/display text by stripping raw action tokens
        val cleanText = rawText.replace(actionRegex, "").trim()
        val cleanModel = modelName.removePrefix("models/")

        return GeminiLiveResponse(
            conversationalText = cleanText.ifBlank { rawText },
            spokenText = cleanText.ifBlank { "সবকিছু ঠিকঠাক কাজ করছে।" },
            latencyMs = latencyMs,
            isLiveApi = isLive,
            actionCommands = actions,
            selectedModelName = cleanModel
        )
    }
}
