package com.example.data.model

data class SystemHealth(
    val cpuPercent: Int = 18,
    val ramUsedMb: Int = 2450,
    val ramTotalMb: Int = 5800,
    val batteryPercent: Int = 92,
    val batteryTempC: Double = 31.4,
    val storageEncryptedGb: Double = 12.8,
    val storageTotalGb: Double = 128.0,
    val uptimeFormatted: String = "4d 18h 22m",
    val pingMs: Int = 12,
    val packetLossPercent: Double = 0.0,
    val matterNodesOnline: Int = 8,
    val matterNodesTotal: Int = 8,
    val e2eeCipher: String = "AES-256-GCM / 256-bit PFS",
    val keystoreAlias: String = "JarvisMasterKey_v3",
    val localNlpEngineVersion: String = "Jarvis-Neural-Edge v2.4 (100% On-Device)",
    val lastSyncTimestamp: String = "Just now",
    val isPeerSyncActive: Boolean = true
)
