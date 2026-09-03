package com.example.data.model

enum class TaskPriority(val label: String) {
    HIGH("High Priority"),
    MEDIUM("Normal"),
    LOW("Low")
}

enum class TaskCategory(val label: String) {
    WORK("Work"),
    HOME("Smart Home"),
    PERSONAL("Personal"),
    HEALTH("Health")
}

data class CalendarTask(
    val id: String,
    val title: String,
    val description: String = "",
    val timeSlot: String, // e.g. "09:00 AM - 10:00 AM"
    val date: String,     // e.g. "Today" or "Tomorrow"
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val isEncrypted: Boolean = true,
    val cipherTag: String = "CLOUD-SECURE"
)
