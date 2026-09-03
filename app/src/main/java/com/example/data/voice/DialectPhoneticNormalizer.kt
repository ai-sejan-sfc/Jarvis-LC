package com.example.data.voice

import java.util.Locale

/**
 * On-device dialect and accent phonetic adaptation engine.
 *
 * Normalizes regional acoustic variants, slang synonyms, and noise-degraded phonetic
 * tokens locally on the device with zero cloud connectivity.
 */
object DialectPhoneticNormalizer {

    // Common regional and accent-driven phoneme mapping
    private val phoneticReplacementRules = listOf(
        // British RP & Scottish colloquialisms / vowel shifts
        Regex("(?i)\\b(loight|loights)\\b") to "light",
        Regex("(?i)\\b(shedyool|shed-yool)\\b") to "schedule",
        Regex("(?i)\\b(torch)\\b") to "light",
        Regex("(?i)\\b(cooker|kettle)\\b") to "espresso machine",
        Regex("(?i)\\b(lounge room|drawing room)\\b") to "living room",
        Regex("(?i)\\b(flat)\\b") to "home",
        Regex("(?i)\\b(whilst)\\b") to "while",
        Regex("(?i)\\b(braw|grand)\\b") to "good",
        Regex("(?i)\\b(wee)\\b") to "small",

        // Australian colloquialisms & contractions
        Regex("(?i)\\b(air con|aircon)\\b") to "thermostat",
        Regex("(?i)\\b(arvo)\\b") to "afternoon",
        Regex("(?i)\\b(g'day|gday)\\b") to "hello",
        Regex("(?i)\\b(reckon)\\b") to "think",
        Regex("(?i)\\b(blinds?)\\b") to "blinds",

        // Indian English idioms & syntax shifts
        Regex("(?i)\\b(prepone)\\b") to "reschedule earlier",
        Regex("(?i)\\b(put on the|switch on the)\\b") to "turn on",
        Regex("(?i)\\b(put off the|switch off the)\\b") to "turn off",
        Regex("(?i)\\b(current is on|current is off)\\b") to "power",
        Regex("(?i)\\b(ac)\\b") to "thermostat",

        // General acoustic noise-induced phonetic corruptions (e.g. low SNR in noisy environments)
        Regex("(?i)\\b(lark|lok|locke)\\s+(the\\s+)?(door|front)\\b") to "lock front door",
        Regex("(?i)\\b(turm\\s+on|tern\\s+on|torn\\s+on)\\b") to "turn on",
        Regex("(?i)\\b(turm\\s+off|tern\\s+off|torn\\s+off)\\b") to "turn off",
        Regex("(?i)\\b(termostat|termostatt|thermo)\\b") to "thermostat",
        Regex("(?i)\\b(chandalier|chandelere|chandelir)\\b") to "chandelier",
        Regex("(?i)\\b(breif|breef)\\b") to "brief",
        Regex("(?i)\\b(routin|rootine)\\b") to "routine",
        Regex("(?i)\\b(jarvis|jarviss|jaavis)\\b") to "jarvis",
        Regex("(?i)\\b(degreese?)\\b") to "degrees",
        Regex("(?i)\\b(seventy\\s+two)\\b") to "72",
        Regex("(?i)\\b(sixty\\s+eight)\\b") to "68"
    )

    fun normalize(
        rawTranscript: String,
        dialect: DialectProfile,
        noiseFilterMode: NoiseFilterMode
    ): NormalizedVoiceResult {
        var processed = rawTranscript.trim()
        val original = rawTranscript

        // Apply dialect-specific phonetic normalization
        for ((pattern, replacement) in phoneticReplacementRules) {
            processed = pattern.replace(processed, replacement)
        }

        // Noise compensation: Trim trailing acoustic artifacts (like "uh", "um", "ah", static hiss)
        processed = processed.replace(Regex("(?i)\\b(uh|um|er|ah|hmm)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Calculate a local confidence metric based on clarity and keyword detection
        val keywords = listOf("turn", "set", "lock", "unlock", "brief", "routine", "thermostat", "light", "blinds", "jarvis")
        val matchCount = keywords.count { processed.lowercase(Locale.ROOT).contains(it) }
        val baseConfidence = if (noiseFilterMode == NoiseFilterMode.HIGH_NOISE_OUTDOOR) 0.88f else 0.94f
        val confidence = (baseConfidence + (matchCount * 0.02f)).coerceIn(0.75f, 0.99f)

        return NormalizedVoiceResult(
            originalTranscript = original,
            normalizedTranscript = processed,
            detectedDialect = dialect,
            confidence = confidence
        )
    }

    /**
     * Fuzzy matching helper for device names under noisy acoustics.
     */
    fun fuzzyMatch(query: String, target: String, threshold: Int = 2): Boolean {
        val q = query.lowercase(Locale.ROOT)
        val t = target.lowercase(Locale.ROOT)
        if (q.contains(t) || t.contains(q)) return true
        return levenshteinDistance(q, t) <= threshold
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}

data class NormalizedVoiceResult(
    val originalTranscript: String,
    val normalizedTranscript: String,
    val detectedDialect: DialectProfile,
    val confidence: Float
)
