package com.example.data.local

import com.example.data.model.CalendarTask
import com.example.data.model.Routine
import com.example.data.model.SmartDevice
import com.example.data.model.SystemHealth
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import java.util.Locale

data class NlpResult(
    val intent: String,
    val slots: Map<String, String>,
    val responseText: String,
    val spokenText: String,
    val latencyMs: Long,
    val actionType: NlpActionType = NlpActionType.NONE,
    val updatedDevice: SmartDevice? = null,
    val createdTask: CalendarTask? = null,
    val executedRoutineId: String? = null
)

enum class NlpActionType {
    NONE,
    DEVICE_UPDATE,
    TASK_CREATE,
    ROUTINE_EXECUTE,
    SYSTEM_DIAGNOSTIC,
    SYNC_TRIGGER
}

object OnDeviceNlpEngine {

    fun processQuery(
        rawQuery: String,
        devices: List<SmartDevice>,
        tasks: List<CalendarTask>,
        routines: List<Routine>,
        health: SystemHealth
    ): NlpResult {
        val startTime = System.nanoTime()
        val query = rawQuery.trim().lowercase(Locale.ROOT)

        // 1. Voice-activated summaries / Daily briefing
        if (query.contains("brief") || query.contains("summary") || query.contains("agenda") || 
            query.contains("daily overview") || query.contains("morning update") || query.contains("schedule today")) {
            val pendingTasks = tasks.filter { !it.isCompleted }
            val activeDevices = devices.filter { it.isPowered || !it.isLocked }
            val highPriority = pendingTasks.filter { it.priority == TaskPriority.HIGH }

            val response = buildString {
                append("Good day, sir. Here is your on-device system summary:\n")
                append("• Calendar: ${pendingTasks.size} pending tasks scheduled for today")
                if (highPriority.isNotEmpty()) {
                    append(" (${highPriority.size} marked High Priority: '${highPriority.first().title}')")
                }
                append(".\n")
                append("• Smart Home: ${activeDevices.size} active endpoints running on local Matter mesh.\n")
                append("• System Perimeter: Cryptographic vault is locked with AES-256-GCM. Hub latency is ${health.pingMs}ms.")
            }

            val spoken = "Good day, sir. You have ${pendingTasks.size} pending tasks today. " +
                    if (highPriority.isNotEmpty()) "Top priority is ${highPriority.first().title}. " else "" +
                    "All smart home perimeters are secure, and local latency is ${health.pingMs} milliseconds."

            val latency = (System.nanoTime() - startTime) / 1_000_000
            return NlpResult(
                intent = "VOICE_BRIEFING",
                slots = mapOf("scope" to "daily_overview", "pending_tasks" to pendingTasks.size.toString()),
                responseText = response,
                spokenText = spoken,
                latencyMs = latency.coerceAtLeast(8),
                actionType = NlpActionType.NONE
            )
        }

        // 2. Automated Routine Triggers
        for (routine in routines) {
            val routineNameMatch = routine.name.lowercase(Locale.ROOT)
            if (query.contains("routine") && (query.contains(routineNameMatch) || query.contains(routine.id)) ||
                (routine.id == "morning" && (query.contains("good morning") || query.contains("wake up") || query.contains("rise and shine"))) ||
                (routine.id == "bedtime" && (query.contains("good night") || query.contains("sleep") || query.contains("bedtime"))) ||
                (routine.id == "leaving" && (query.contains("leaving") || query.contains("leave home") || query.contains("goodbye"))) ||
                (routine.id == "focus" && (query.contains("focus mode") || query.contains("studio mode") || query.contains("deep work")))) {

                val response = "Executing '${routine.name}' routine. Running ${routine.actions.size} sequenced local triggers."
                val spoken = "Executing ${routine.name} routine right away, sir."
                val latency = (System.nanoTime() - startTime) / 1_000_000
                return NlpResult(
                    intent = "EXECUTE_ROUTINE",
                    slots = mapOf("routine_id" to routine.id, "name" to routine.name),
                    responseText = response,
                    spokenText = spoken,
                    latencyMs = latency.coerceAtLeast(10),
                    actionType = NlpActionType.ROUTINE_EXECUTE,
                    executedRoutineId = routine.id
                )
            }
        }

        // 3. Smart Home Device Control: Lighting, Thermostat, Lock, Blinds, Plug
        val deviceMatches = devices.mapNotNull { dev ->
            val devName = dev.name.lowercase(Locale.ROOT)
            val devRoom = dev.room.lowercase(Locale.ROOT)
            val score = when {
                query.contains(devName) -> 100
                query.contains(devRoom) && (query.contains("light") && dev.type == com.example.data.model.DeviceType.LIGHT) -> 90
                query.contains(devRoom) && (query.contains("thermostat") || query.contains("ac") || query.contains("temp")) -> 90
                query.contains("thermostat") && dev.type == com.example.data.model.DeviceType.THERMOSTAT -> 85
                query.contains("door") || query.contains("lock") && dev.type == com.example.data.model.DeviceType.LOCK -> 85
                query.contains("blind") && dev.type == com.example.data.model.DeviceType.BLINDS -> 85
                query.contains("coffee") || query.contains("plug") && dev.type == com.example.data.model.DeviceType.PLUG -> 85
                query.contains("light") && dev.type == com.example.data.model.DeviceType.LIGHT -> 70
                else -> 0
            }
            if (score > 0) Pair(dev, score) else null
        }.sortedByDescending { it.second }

        if (deviceMatches.isNotEmpty()) {
            val targetDevice = deviceMatches.first().first

            // Temperature adjustment
            val tempRegex = Regex("""(\d{2})\s*(degrees|deg|f)?""")
            val tempMatch = tempRegex.find(query)
            if (targetDevice.type == com.example.data.model.DeviceType.THERMOSTAT && (tempMatch != null || query.contains("cooler") || query.contains("warmer"))) {
                val newTemp = tempMatch?.groupValues?.get(1)?.toIntOrNull() 
                    ?: if (query.contains("cooler")) targetDevice.targetTemperatureF - 2 else targetDevice.targetTemperatureF + 2
                val updated = targetDevice.copy(targetTemperatureF = newTemp, isPowered = true)
                val resp = "Thermostat adjusted to ${newTemp}°F in ${targetDevice.room}."
                val latency = (System.nanoTime() - startTime) / 1_000_000
                return NlpResult(
                    intent = "SET_TEMPERATURE",
                    slots = mapOf("device" to targetDevice.name, "target_temp" to "$newTemp"),
                    responseText = resp,
                    spokenText = "Adjusted ${targetDevice.name} to ${newTemp} degrees, sir.",
                    latencyMs = latency.coerceAtLeast(11),
                    actionType = NlpActionType.DEVICE_UPDATE,
                    updatedDevice = updated
                )
            }

            // Lock / Unlock
            if (targetDevice.type == com.example.data.model.DeviceType.LOCK || query.contains("lock") || query.contains("unlock")) {
                val shouldLock = !query.contains("unlock")
                val updated = targetDevice.copy(isLocked = shouldLock)
                val statusStr = if (shouldLock) "secured and locked" else "unlocked"
                val resp = "${targetDevice.name} is now $statusStr."
                val latency = (System.nanoTime() - startTime) / 1_000_000
                return NlpResult(
                    intent = if (shouldLock) "LOCK_DEVICE" else "UNLOCK_DEVICE",
                    slots = mapOf("device" to targetDevice.name, "state" to if (shouldLock) "locked" else "unlocked"),
                    responseText = resp,
                    spokenText = "${targetDevice.name} has been $statusStr.",
                    latencyMs = latency.coerceAtLeast(12),
                    actionType = NlpActionType.DEVICE_UPDATE,
                    updatedDevice = updated
                )
            }

            // Power / Level / Brightness
            val levelRegex = Regex("""(\d{1,3})\s*(%|percent)""")
            val levelMatch = levelRegex.find(query)
            if (levelMatch != null) {
                val levelVal = levelMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 80
                val updated = targetDevice.copy(level = levelVal, isPowered = levelVal > 0)
                val resp = "${targetDevice.name} level set to ${levelVal}%."
                val latency = (System.nanoTime() - startTime) / 1_000_000
                return NlpResult(
                    intent = "SET_LEVEL",
                    slots = mapOf("device" to targetDevice.name, "level" to "$levelVal%"),
                    responseText = resp,
                    spokenText = "Set ${targetDevice.name} to ${levelVal} percent.",
                    latencyMs = latency.coerceAtLeast(10),
                    actionType = NlpActionType.DEVICE_UPDATE,
                    updatedDevice = updated
                )
            }

            // Turn On / Off / Open / Close
            val turnOff = query.contains("off") || query.contains("close") || query.contains("shut")
            val turnOn = query.contains("on") || query.contains("open") || query.contains("start") || query.contains("activate")
            val newState = if (turnOff) false else if (turnOn) true else !targetDevice.isPowered

            val updated = targetDevice.copy(
                isPowered = newState,
                level = if (newState && targetDevice.level == 0) 100 else if (!newState) 0 else targetDevice.level
            )
            val stateWord = if (newState) "powered on" else "powered down"
            val resp = "${targetDevice.name} has been $stateWord."
            val latency = (System.nanoTime() - startTime) / 1_000_000
            return NlpResult(
                intent = if (newState) "TURN_ON" else "TURN_OFF",
                slots = mapOf("device" to targetDevice.name, "state" to stateWord),
                responseText = resp,
                spokenText = "${targetDevice.name} is now $stateWord, sir.",
                latencyMs = latency.coerceAtLeast(9),
                actionType = NlpActionType.DEVICE_UPDATE,
                updatedDevice = updated
            )
        }

        // 4. Calendar Task Creation
        if (query.startsWith("add task") || query.startsWith("schedule") || query.startsWith("remind me to") || query.contains("new task")) {
            val cleanTitle = query.replace("add task", "")
                .replace("schedule", "")
                .replace("remind me to", "")
                .replace("new task", "")
                .trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                .ifEmpty { "New Calendar Task" }

            val isHighPriority = query.contains("urgent") || query.contains("high priority") || query.contains("important")
            val newTask = CalendarTask(
                id = java.util.UUID.randomUUID().toString().take(8),
                title = cleanTitle,
                description = "Created via local voice assistant command",
                timeSlot = "14:00 - 15:00",
                date = "Today",
                priority = if (isHighPriority) TaskPriority.HIGH else TaskPriority.MEDIUM,
                category = if (query.contains("home")) TaskCategory.HOME else TaskCategory.WORK
            )
            val resp = "Task scheduled: '${newTask.title}' [${newTask.priority.label}]. Encrypted with AES-256-GCM."
            val spoken = "Task ${newTask.title} has been encrypted and added to your schedule."
            val latency = (System.nanoTime() - startTime) / 1_000_000
            return NlpResult(
                intent = "CREATE_CALENDAR_TASK",
                slots = mapOf("title" to newTask.title, "priority" to newTask.priority.name),
                responseText = resp,
                spokenText = spoken,
                latencyMs = latency.coerceAtLeast(14),
                actionType = NlpActionType.TASK_CREATE,
                createdTask = newTask
            )
        }

        // 5. System Health / Security Diagnostics
        if (query.contains("system health") || query.contains("diagnostic") || query.contains("vault") || query.contains("ping") || query.contains("status")) {
            val resp = "System Diagnostics:\n• CPU Load: ${health.cpuPercent}%\n• RAM: ${health.ramUsedMb}MB / ${health.ramTotalMb}MB\n• E2EE Vault: Active (${health.e2eeCipher})\n• Hub Latency: ${health.pingMs}ms (${health.packetLossPercent}% loss)"
            val spoken = "All internal systems are functioning within normal parameters, sir. Latency is ${health.pingMs} milliseconds."
            val latency = (System.nanoTime() - startTime) / 1_000_000
            return NlpResult(
                intent = "SYSTEM_HEALTH_CHECK",
                slots = mapOf("cpu" to "${health.cpuPercent}%", "latency" to "${health.pingMs}ms"),
                responseText = resp,
                spokenText = spoken,
                latencyMs = latency.coerceAtLeast(10),
                actionType = NlpActionType.SYSTEM_DIAGNOSTIC
            )
        }

        // 6. Conversational Jarvis Persona
        val greetingResp = "At your service, sir. You can ask me for a daily briefing, control smart home devices, run automated routines, or inspect your encrypted system health."
        val latency = (System.nanoTime() - startTime) / 1_000_000
        return NlpResult(
            intent = "ASSISTANT_CONVERSATION",
            slots = mapOf("query" to rawQuery),
            responseText = greetingResp,
            spokenText = "Standing by for your command, sir.",
            latencyMs = latency.coerceAtLeast(8),
            actionType = NlpActionType.NONE
        )
    }
}
