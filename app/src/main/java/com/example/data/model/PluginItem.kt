package com.example.data.model

enum class PluginStatus(val label: String) {
    ACTIVE("Operational"),
    STANDBY("Standby"),
    DISABLED("Disabled"),
    ERROR("Configuration Required")
}

data class PluginItem(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val isEnabled: Boolean,
    val status: PluginStatus,
    val sandboxedPermissions: List<String>,
    val endpointUrl: String = "",
    val apiKeyMasked: String = "••••••••••••••••",
    val latencyMs: Int = 18,
    val isVerified: Boolean = true
)
