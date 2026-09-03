package com.example.data.model

enum class RoutineTriggerType(val label: String) {
    TIME("Scheduled Time"),
    LOCATION("Geofence / Location"),
    VOICE("Voice Command"),
    SENSOR("Environmental Sensor"),
    MANUAL("Manual Execution")
}

data class RoutineAction(
    val description: String,
    val targetDeviceId: String? = null,
    val actionType: String // e.g., "SET_POWER", "SET_TEMP", "LOCK", "BRIEFING"
)

data class Routine(
    val id: String,
    val name: String,
    val triggerCondition: String, // e.g. "07:00 AM Daily", "Arrive Home", "Say 'Goodnight Jarvis'"
    val triggerType: RoutineTriggerType,
    val isEnabled: Boolean = true,
    val actions: List<RoutineAction>,
    val lastExecuted: String? = null,
    val iconKey: String = "routine"
)
