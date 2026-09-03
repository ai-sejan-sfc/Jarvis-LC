package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.SmartProtocol
import com.example.data.model.TemperatureUnit
import com.example.data.voice.CaptureState
import com.example.data.voice.DialectProfile
import com.example.data.voice.VoicePersona
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCobalt
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Preferences state
    val offlineOnlyMode by viewModel.offlineOnlyPrivacyMode.collectAsState()
    val isGeminiLiveMode by viewModel.isGeminiLiveMode.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val defaultLightBrightness by viewModel.defaultLightBrightness.collectAsState()
    val preferredRoom by viewModel.preferredRoom.collectAsState()
    val autoLockDelaySeconds by viewModel.autoLockDelaySeconds.collectAsState()
    val preferredProtocol by viewModel.preferredProtocol.collectAsState()
    val confirmSecurityActions by viewModel.confirmSecurityActions.collectAsState()

    // Microphone & Wake Word state
    val wakeWordEnabled by viewModel.wakeWordEnabled.collectAsState()
    val wakeWordPhrase by viewModel.wakeWordPhrase.collectAsState()
    val wakeWordSensitivity by viewModel.wakeWordSensitivity.collectAsState()
    val hasMicPermission by viewModel.hasMicPermission.collectAsState()
    val wakeWordCaptureState by viewModel.wakeWordCaptureState.collectAsState()
    val wakeWordRmsDb by viewModel.wakeWordRmsDb.collectAsState()
    val wakeWordTriggerCount by viewModel.wakeWordTriggerCount.collectAsState()
    val lastCapturedVoiceCommand by viewModel.lastCapturedVoiceCommand.collectAsState()

    // Persona & AI state
    val customApiKey by viewModel.customApiKey.collectAsState()
    val voicePersona by viewModel.voicePersona.collectAsState()
    val dialect by viewModel.dialect.collectAsState()

    var showClearLogsDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var apiKeySavedFeedback by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateMicPermission(isGranted)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Screen Title Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM PREFERENCES",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "NEURAL CORE • PRIVACY MODES • HOME AUTOMATION",
                        fontSize = 10.sp,
                        color = JarvisTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                }

                // Privacy Shield Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (offlineOnlyMode) JarvisEmerald.copy(alpha = 0.2f) else JarvisCyan.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (offlineOnlyMode) JarvisEmerald else JarvisCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (offlineOnlyMode) "AIR-GAPPED" else "HYBRID AI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (offlineOnlyMode) JarvisEmerald else JarvisCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ==========================================
        // 1. OFFLINE-ONLY PRIVACY MODES
        // ==========================================
        item {
            SectionHeader(
                title = "PRIVACY & NEURAL CORE",
                icon = Icons.Default.Security,
                badge = if (offlineOnlyMode) "AIR-GAPPED 100%" else "HYBRID MODE"
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_settings_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (offlineOnlyMode) JarvisEmerald.copy(alpha = 0.7f) else JarvisBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Master Offline Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline-Only Privacy Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Disable all external cloud requests. Enforce 100% on-device neural processing with zero outbound telemetry.",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = offlineOnlyMode,
                            onCheckedChange = { viewModel.setOfflineOnlyPrivacyMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisEmerald,
                                checkedTrackColor = JarvisEmerald.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisSurfaceVariant
                            ),
                            modifier = Modifier.testTag("offline_privacy_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = JarvisBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (offlineOnlyMode) JarvisEmerald.copy(alpha = 0.08f) else JarvisCyan.copy(alpha = 0.06f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                0.8.dp,
                                if (offlineOnlyMode) JarvisEmerald.copy(alpha = 0.3f) else JarvisCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (offlineOnlyMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (offlineOnlyMode) JarvisEmerald else JarvisCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (offlineOnlyMode) "AIR-GAPPED NEURAL EXECUTION" else "GEMINI LIVE HYBRID STREAMING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (offlineOnlyMode) JarvisEmerald else JarvisCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (offlineOnlyMode)
                                    "Offline cache active. Gemini Live Cloud Engine temporarily paused."
                                else
                                    "Gemini Live Cloud Engine with dynamic model selection and Bengali persona.",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ephemeral Data Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Chat History & Transcripts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Encrypted chat & speech logs",
                                fontSize = 11.sp,
                                color = JarvisTextMuted
                            )
                        }
                        OutlinedButton(
                            onClick = { showClearLogsDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("clear_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Memory", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. MICROPHONE CAPTURE & WAKE WORD SERVICE
        // ==========================================
        item {
            SectionHeader(
                title = "MICROPHONE & WAKE WORD SERVICE",
                icon = Icons.Default.Mic,
                badge = if (hasMicPermission) "ACTIVE" else "REQUIRES PERMISSION"
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wake_word_settings_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Permission Status Indicator & Request Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (hasMicPermission) JarvisEmerald.copy(alpha = 0.08f) else JarvisAmber.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (hasMicPermission) JarvisEmerald.copy(alpha = 0.4f) else JarvisAmber.copy(alpha = 0.5f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (hasMicPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasMicPermission) JarvisEmerald else JarvisAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (hasMicPermission) "MICROPHONE PERMISSION GRANTED" else "AUDIO PERMISSION REQUIRED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasMicPermission) JarvisEmerald else JarvisAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (hasMicPermission)
                                        "Foreground audio capture service ready."
                                    else
                                        "Required for wake word listening and voice commands.",
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        if (!hasMicPermission) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisAmber,
                                    contentColor = JarvisBackground
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_mic_permission_button")
                            ) {
                                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Background Wake Word Service Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acoustic Wake Word Detection",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Continuous background microphone capture service listening for activation phrases.",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !hasMicPermission) {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                                viewModel.setWakeWordEnabled(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisSurfaceVariant
                            ),
                            modifier = Modifier.testTag("wake_word_service_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Service Status & RMS Acoustic Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (wakeWordCaptureState) {
                                            CaptureState.LISTENING_FOR_WAKE_WORD -> JarvisEmerald
                                            CaptureState.WAKE_WORD_DETECTED,
                                            CaptureState.RECORDING_COMMAND -> Color(0xFFFF334B)
                                            CaptureState.PROCESSING_COMMAND -> JarvisCyan
                                            else -> JarvisTextMuted
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SERVICE STATUS: ${wakeWordCaptureState.displayLabel.uppercase()}",
                                fontSize = 10.sp,
                                color = JarvisCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "RMS: %.1f dB • TRIGGERS: %d".format(wakeWordRmsDb, wakeWordTriggerCount),
                            fontSize = 10.sp,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Decibel Level Bar
                    val normalizedDbProgress = ((wakeWordRmsDb + 60f) / 60f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { normalizedDbProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (normalizedDbProgress > 0.65f) Color(0xFFFF334B) else JarvisCyan,
                        trackColor = JarvisSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wake Word Trigger Phrase Selection
                    Text(
                        text = "Activation Phrase",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = JarvisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val phrases = listOf("Hey Jarvis", "Jarvis", "Computer", "Friday", "Stark")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        phrases.forEach { phrase ->
                            val isSelected = wakeWordPhrase.equals(phrase, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setWakeWordPhrase(phrase) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = phrase,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sensitivity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Microphone Sensitivity",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = JarvisTextPrimary
                        )
                        Text(
                            text = "${(wakeWordSensitivity * 100).roundToInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = wakeWordSensitivity,
                        onValueChange = { viewModel.setWakeWordSensitivity(it) },
                        valueRange = 0.5f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Manual Wake Word Test Simulator Button
                    OutlinedButton(
                        onClick = { viewModel.triggerWakeWordSimulation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_wake_word_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST WAKE WORD TRIGGER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    lastCapturedVoiceCommand?.let { cmd ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LAST RECORDED: \"$cmd\"",
                            fontSize = 10.sp,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. HOME DEVICE PREFERENCES
        // ==========================================
        item {
            SectionHeader(
                title = "HOME DEVICE PREFERENCES",
                icon = Icons.Default.Thermostat,
                badge = "${temperatureUnit.symbol} • ${preferredProtocol.label}"
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_device_preferences_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Temperature Unit Preference
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Temperature Unit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Preferred unit across thermostats & forecasts",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .background(JarvisSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TemperatureUnit.values().forEach { unit ->
                                val isSelected = temperatureUnit == unit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) JarvisCyan else Color.Transparent)
                                        .clickable { viewModel.setTemperatureUnit(unit) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${unit.symbol} (${unit.label})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) JarvisBackground else JarvisTextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = JarvisBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Default Light Brightness
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Light Brightness",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Applied when turning lights on without level query",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Text(
                            text = "$defaultLightBrightness%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = defaultLightBrightness.toFloat(),
                        onValueChange = { viewModel.setDefaultLightBrightness(it.toInt()) },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Preferred Room
                    Text(
                        text = "Preferred Primary Room / Zone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "Fallback room context when room is unspecified",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val rooms = listOf("Living Room", "Hallway", "Master Bedroom", "Lab / Office", "Kitchen")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rooms.forEach { room ->
                            val isSelected = preferredRoom == room
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setPreferredRoom(room) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = room,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = JarvisBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto-Lock Timeout
                    Text(
                        text = "Deadbolt Auto-Lock Delay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "Automatic deadbolt relock duration after opening",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val lockDelays = listOf(
                        30 to "30s",
                        60 to "1 min",
                        300 to "5 min",
                        900 to "15 min",
                        0 to "Manual"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        lockDelays.forEach { (seconds, label) ->
                            val isSelected = autoLockDelaySeconds == seconds
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setAutoLockDelaySeconds(seconds) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preferred Smart Protocol
                    Text(
                        text = "Default Connectivity Protocol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "Preferred mesh standard for device pairing and diagnostics",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmartProtocol.values().forEach { protocol ->
                            val isSelected = preferredProtocol == protocol
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisEmerald.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisEmerald else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setPreferredProtocol(protocol) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = protocol.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisEmerald else JarvisTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm High Security Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require Confirmation for Security",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = "Prompt voice verification before disarming locks or vault plugins",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = confirmSecurityActions,
                            onCheckedChange = { viewModel.setConfirmSecurityActions(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. GEMINI LIVE & VOICE ENGINE CONFIGURATION
        // ==========================================
        item {
            SectionHeader(
                title = "AI INTELLIGENCE & GEMINI LIVE",
                icon = Icons.Default.RecordVoiceOver,
                badge = voicePersona.displayName
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_settings_card"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemini API Key",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Text(
                        text = "Used for Gemini 1.5 Flash conversational reasoning and action routing",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            apiKeySavedFeedback = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_settings_input"),
                        singleLine = true,
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle API Key Visibility",
                                    tint = JarvisTextSecondary
                                )
                            }
                        },
                        placeholder = { Text("Enter Gemini API Key...", fontSize = 12.sp, color = JarvisTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = JarvisCyan
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.setCustomApiKey(apiKeyInput)
                            apiKeySavedFeedback = true
                        })
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = apiKeySavedFeedback) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = JarvisEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "API Key Saved",
                                    fontSize = 11.sp,
                                    color = JarvisEmerald,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomApiKey(apiKeyInput)
                                apiKeySavedFeedback = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisCyan,
                                contentColor = JarvisBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Text("Save Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = JarvisBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice Persona Selector
                    Text(
                        text = "Synthesizer Voice Persona",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoicePersona.values().forEach { persona ->
                            val isSelected = voicePersona == persona
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setVoicePersona(persona) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = persona.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dialect Profile
                    Text(
                        text = "Acoustic Dialect Normalization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DialectProfile.values().forEach { prof ->
                            val isSelected = dialect == prof
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else JarvisBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setDialect(prof) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = prof.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisCyan else JarvisTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Confirmation Dialog for Clearing Logs
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            title = {
                Text(
                    text = "Purge Chat History?",
                    color = JarvisTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will clear all in-memory chat messages and recorded voice transcripts.",
                    color = JarvisTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearLogsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber, contentColor = JarvisBackground)
                ) {
                    Text("Purge Logs", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Cancel", color = JarvisTextSecondary)
                }
            },
            containerColor = JarvisSurfaceElevated
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = JarvisTextPrimary,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        badge?.let {
            Text(
                text = it,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = JarvisCyan,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
