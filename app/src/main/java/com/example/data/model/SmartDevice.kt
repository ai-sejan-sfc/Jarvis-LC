package com.example.data.model

enum class DeviceType {
    LIGHT,
    THERMOSTAT,
    LOCK,
    BLINDS,
    PLUG,
    SENSOR
}

enum class SmartProtocol(val label: String) {
    MATTER("Matter 1.3"),
    ZIGBEE("Zigbee 3.0"),
    THREAD("Thread Mesh"),
    WIFI_LAN("WiFi / LAN")
}

data class SmartDevice(
    val id: String,
    val name: String,
    val room: String,
    val type: DeviceType,
    val isPowered: Boolean = false,
    val level: Int = 0, // 0-100 for brightness, blinds position, etc.
    val targetTemperatureF: Int = 72,
    val currentTemperatureF: Int = 71,
    val isLocked: Boolean = true,
    val protocol: SmartProtocol = SmartProtocol.MATTER,
    val powerWatts: Double = 0.0,
    val isOnline: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
