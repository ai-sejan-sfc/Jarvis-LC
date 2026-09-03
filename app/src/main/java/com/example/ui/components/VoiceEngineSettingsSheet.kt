package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.voice.DialectProfile
import com.example.data.voice.NoiseFilterMode
import com.example.data.voice.VoicePersona
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceEngineSettingsSheet(
    isGeminiLiveMode: Boolean,
    selectedPersona: VoicePersona,
    selectedDialect: DialectProfile,
    selectedNoiseFilter: NoiseFilterMode,
    liveRmsDb: Float,
    ambientNoiseFloorDb: Float,
    snrDb: Float,
    apiKey: String = "",
    onApiKeyChange: (String) -> Unit = {},
    onGeminiLiveModeToggle: (Boolean) -> Unit,
    onSelectPersona: (VoicePersona) -> Unit,
    onSelectDialect: (DialectProfile) -> Unit,
    onSelectNoiseFilter: (NoiseFilterMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JarvisBackground,
        dragHandle = null,
        modifier = Modifier.testTag("voice_engine_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JarvisCyan.copy(alpha = 0.15f))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Engine",
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Voice & Acoustic Engine",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        )
                        Text(
                            text = "Gemini Live API & On-Device Recognition",
                            style = MaterialTheme.typography.bodySmall.copy(color = JarvisCyan)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = JarvisTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Conversational Mode Toggle
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = JarvisCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isGeminiLiveMode) JarvisCyan.copy(alpha = 0.6f) else JarvisEmerald.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isGeminiLiveMode) Icons.Default.AutoAwesome else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (isGeminiLiveMode) JarvisCyan else JarvisEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isGeminiLiveMode) "Gemini Live Voice Engine" else "Pure On-Device Privacy Mode",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = JarvisTextPrimary
                                            )
                                        )
                                        Text(
                                            text = if (isGeminiLiveMode) "Real-time generative speech synthesis & reasoning" else "100% offline local processing, zero internet packets",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isGeminiLiveMode) JarvisCyan else JarvisEmerald
                                            )
                                        )
                                    }
                                }
                                Switch(
                                    checked = isGeminiLiveMode,
                                    onCheckedChange = onGeminiLiveModeToggle,
                                    modifier = Modifier.testTag("gemini_live_toggle"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JarvisCyan,
                                        checkedTrackColor = JarvisCyan.copy(alpha = 0.35f),
                                        uncheckedThumbColor = JarvisEmerald,
                                        uncheckedTrackColor = JarvisEmerald.copy(alpha = 0.25f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Section: Gemini API Key Configuration
                item {
                    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = JarvisCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (apiKey.isNotBlank()) JarvisCyan.copy(alpha = 0.5f) else JarvisAmber.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (apiKey.isNotBlank()) JarvisCyan else JarvisAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Gemini API Key",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = JarvisTextPrimary
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (apiKey.isNotBlank()) JarvisEmerald.copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (apiKey.isNotBlank()) "ACTIVE" else "KEY MISSING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (apiKey.isNotBlank()) JarvisEmerald else JarvisAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Required for Live Gemini 1.5 Flash conversational queries.",
                                style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextMuted)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = keyInput,
                                onValueChange = {
                                    keyInput = it
                                    onApiKeyChange(it)
                                },
                                placeholder = {
                                    Text("Enter Gemini API Key (AIzaSy...)", fontSize = 12.sp, color = JarvisTextMuted)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_api_key_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisBorder,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                // Section 2: Real-time Acoustic & Noise Telemetry
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = null,
                                        tint = JarvisAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Acoustic Telemetry & Privacy Monitor",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = JarvisTextPrimary
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JarvisEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AIR-GAPPED AUDIO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = JarvisEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricColumn(
                                    label = "Live Speech RMS",
                                    value = "${"%.1f".format(liveRmsDb)} dB"
                                )
                                MetricColumn(
                                    label = "Noise Floor",
                                    value = "${"%.1f".format(ambientNoiseFloorDb)} dB"
                                )
                                MetricColumn(
                                    label = "Acoustic SNR",
                                    value = "${"%.1f".format(snrDb)} dB"
                                )
                            }
                        }
                    }
                }

                // Section 3: Accent & Dialect Adaptation
                item {
                    Column {
                        Text(
                            text = "On-Device Dialect & Accent Engine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        )
                        Text(
                            text = "Local phonetic normalizer adapts to regional accents and idioms",
                            style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DialectProfile.values().forEach { dialect ->
                                val isSelected = dialect == selectedDialect
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) JarvisCyan.copy(alpha = 0.12f) else JarvisCardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectDialect(dialect) }
                                        .testTag("dialect_${dialect.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = dialect.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) JarvisCyan else JarvisTextPrimary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(JarvisBorder)
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = dialect.regionCode,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = JarvisTextMuted,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                            }
                                            Text(
                                                text = dialect.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = JarvisTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = JarvisCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Noise Filtering & Environmental Acoustic Gate
                item {
                    Column {
                        Text(
                            text = "Acoustic Noise Filter Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        )
                        Text(
                            text = "Spectral gating and noise cancellation for noisy environments",
                            style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NoiseFilterMode.values().forEach { mode ->
                                val isSelected = mode == selectedNoiseFilter
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) JarvisAmber.copy(alpha = 0.12f) else JarvisCardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) JarvisAmber else JarvisBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectNoiseFilter(mode) }
                                        .testTag("noise_filter_${mode.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) JarvisAmber else JarvisTextPrimary
                                                )
                                            )
                                            Text(
                                                text = mode.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = JarvisTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) JarvisAmber.copy(alpha = 0.2f) else JarvisBorder)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "-${mode.attenuationDb} dB",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) JarvisAmber else JarvisTextMuted,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: Gemini Live Voice Personas
                item {
                    Column {
                        Text(
                            text = "Gemini Live Voice Persona",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        )
                        Text(
                            text = "Select speech cadence, timber, and neural persona",
                            style = MaterialTheme.typography.bodySmall.copy(color = JarvisTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            VoicePersona.values().forEach { persona ->
                                val isSelected = persona == selectedPersona
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF7C4DFF).copy(alpha = 0.15f) else JarvisCardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFB388FF) else JarvisBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPersona(persona) }
                                        .testTag("persona_${persona.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = persona.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color(0xFFB388FF) else JarvisTextPrimary
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF7C4DFF).copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = persona.geminiVoiceName,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFD1C4E9),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                            }
                                            Text(
                                                text = persona.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = JarvisTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFFB388FF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = JarvisTextMuted, fontSize = 10.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = JarvisTextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
