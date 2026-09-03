package com.example.data.model

data class SyncLogEntry(
    val timestamp: String,
    val action: String,
    val entity: String,
    val vectorClock: String,
    val status: String
)

data class SyncState(
    val isOnline: Boolean = true,
    val pendingMutations: Int = 0,
    val lastSyncTimestamp: String = "19:34:00",
    val syncTarget: String = "Gemini Live Cloud Sync",
    val conflictCount: Int = 0,
    val recentLogs: List<SyncLogEntry> = emptyList()
)
