package com.example.data.voice

enum class DialectProfile(
    val displayName: String,
    val regionCode: String,
    val description: String,
    val sampleGreeting: String
) {
    US_GENERAL(
        displayName = "General American",
        regionCode = "en-US",
        description = "Standard North American English with broad phoneme coverage.",
        sampleGreeting = "All systems nominal, sir."
    ),
    UK_RP(
        displayName = "British (Received Pronunciation)",
        regionCode = "en-GB",
        description = "Sophisticated British inflection with non-rhotic vowel adaptation.",
        sampleGreeting = "Very good, sir. Standing by."
    ),
    AUSTRALIAN(
        displayName = "Australian & Oceanic",
        regionCode = "en-AU",
        description = "Oceanic dialect with diphthong tolerance and colloquial mapping.",
        sampleGreeting = "No worries, mate. Systems online."
    ),
    INDIAN_ENGLISH(
        displayName = "Indian English",
        regionCode = "en-IN",
        description = "South Asian English with retroflex consonant and rhythm normalization.",
        sampleGreeting = "At your service. Ready to assist."
    ),
    BENGALI(
        displayName = "Bengali (বাংলা)",
        regionCode = "bn-BD",
        description = "Native Bengali conversational acoustics with high phonetic fidelity.",
        sampleGreeting = "নমস্কার, আমি জারভিস। আমি আপনাকে কীভাবে সাহায্য করতে পারি?"
    ),
    SCOTTISH(
        displayName = "Scottish / Celtic",
        regionCode = "en-GB",
        description = "Scottish inflection with rhotic trill filtering and regional vocabulary.",
        sampleGreeting = "Aye, ready whenever you are, sir."
    ),
    GLOBAL_ACCENT(
        displayName = "Global Non-Native (Adaptive)",
        regionCode = "en-US",
        description = "Acoustic fuzzy match with high vowel flexibility for international speakers.",
        sampleGreeting = "Understood. Local engine calibrated."
    )
}
