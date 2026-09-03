package com.example.data.voice

enum class NoiseFilterMode(
    val title: String,
    val subtitle: String,
    val attenuationDb: Int,
    val description: String
) {
    ADAPTIVE_AUTO(
        title = "Adaptive Environmental Gate",
        subtitle = "Auto-calibrating SNR tracking",
        attenuationDb = 18,
        description = "Continually measures ambient room noise floor and gates acoustic chatter."
    ),
    HIGH_NOISE_OUTDOOR(
        title = "Acoustic Beamforming & Heavy Filter",
        subtitle = "Street, kitchen & machinery noise",
        attenuationDb = 28,
        description = "Aggressive spectral subtraction and vowel focus for high-noise surroundings."
    ),
    STUDIO_CLEAN(
        title = "Studio Pure (High Sensitivity)",
        subtitle = "Quiet office or bedroom",
        attenuationDb = 6,
        description = "Maximum acoustic sensitivity, detecting whisper-quiet commands."
    )
}
