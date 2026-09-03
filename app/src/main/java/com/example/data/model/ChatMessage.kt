package com.example.data.model

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val latencyMs: Long? = null,
    val intentDetected: String? = null,
    val isSpoken: Boolean = false,
    val executionLog: String? = null,
    val isGeminiLive: Boolean = false,
    val detectedDialect: String? = null,
    val confidenceScore: Float? = null,
    val noiseFloorDb: Float? = null
)
