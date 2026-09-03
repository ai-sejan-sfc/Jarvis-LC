package com.example.data.voice

enum class VoicePersona(
    val id: String,
    val displayName: String,
    val geminiVoiceName: String,
    val description: String,
    val pitch: Float,
    val speechRate: Float
) {
    JARVIS_CLASSIC(
        id = "jarvis_kore",
        displayName = "Jarvis Classic",
        geminiVoiceName = "Kore",
        description = "Refined British intellectual tone with courteous demeanor.",
        pitch = 0.93f,
        speechRate = 1.02f
    ),
    FRIDAY_MELODIC(
        id = "friday_aoede",
        displayName = "Friday",
        geminiVoiceName = "Aoede",
        description = "Crisp, soothing, and precision-engineered conversational flow.",
        pitch = 1.05f,
        speechRate = 1.04f
    ),
    PULSE_DYNAMIC(
        id = "pulse_puck",
        displayName = "Pulse",
        geminiVoiceName = "Puck",
        description = "Energetic, rapid-fire, witty and high-tempo companion.",
        pitch = 1.12f,
        speechRate = 1.10f
    ),
    TITAN_COMMAND(
        id = "titan_fenrir",
        displayName = "Titan Command",
        geminiVoiceName = "Fenrir",
        description = "Deep, commanding tactical voice with low resonant pitch.",
        pitch = 0.82f,
        speechRate = 0.98f
    ),
    VISION_CORE(
        id = "vision_charon",
        displayName = "Vision Core",
        geminiVoiceName = "Charon",
        description = "Authoritative, calm, philosophical artificial intelligence.",
        pitch = 0.88f,
        speechRate = 0.96f
    )
}
