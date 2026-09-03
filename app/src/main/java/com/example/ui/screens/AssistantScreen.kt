package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.ArcReactorWaveform
import com.example.ui.components.VoiceEngineSettingsSheet
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCobalt
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun AssistantScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isGeminiLiveMode by viewModel.isGeminiLiveMode.collectAsState()
    val voicePersona by viewModel.voicePersona.collectAsState()
    val dialect by viewModel.dialect.collectAsState()
    val noiseFilter by viewModel.noiseFilter.collectAsState()
    val voiceSettingsOpen by viewModel.voiceSettingsOpen.collectAsState()
    val livePartialTranscript by viewModel.livePartialTranscript.collectAsState()
    val liveRmsDb by viewModel.liveRmsDb.collectAsState()
    val ambientNoiseFloorDb by viewModel.ambientNoiseFloorDb.collectAsState()
    val snrDb by viewModel.snrDb.collectAsState()
    val voiceAmplitude by viewModel.voiceAmplitude.collectAsState()

    val listState = rememberLazyListState()
    var inputQuery by remember { mutableStateOf("") }

    // Auto scroll to newest messages
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
    ) {
        // Upper Command HUD: Arc Reactor & Status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurface.copy(alpha = 0.5f))
                .padding(top = 8.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Engine Header Controls: Mode pill & Settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isGeminiLiveMode) Color(0xFF7C4DFF).copy(alpha = 0.15f) else JarvisEmerald.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isGeminiLiveMode) Color(0xFFB388FF).copy(alpha = 0.5f) else JarvisEmerald.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setVoiceSettingsOpen(true) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isGeminiLiveMode) Icons.Default.AutoAwesome else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isGeminiLiveMode) Color(0xFFB388FF) else JarvisEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGeminiLiveMode) "GEMINI LIVE • ${voicePersona.displayName}" else "100% LOCAL PRIVACY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGeminiLiveMode) Color(0xFFD1C4E9) else JarvisEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { viewModel.setVoiceSettingsOpen(true) },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisBorder, CircleShape)
                        .testTag("open_voice_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Engine Settings",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            ArcReactorWaveform(
                sizeDp = 145.dp,
                isListening = isListening,
                isSpeaking = isSpeaking,
                isThinking = isThinking,
                isGeminiLive = isGeminiLiveMode,
                voiceAmplitude = voiceAmplitude,
                onClick = {
                    viewModel.toggleVoiceListening()
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // State indicator pill
            val statusText = when {
                isThinking -> "GEMINI LIVE SYNTHESIZING..."
                isListening -> "ON-DEVICE RECOGNIZING • SPEAK NOW"
                isSpeaking -> "VOCALIZING RESPONSE..."
                else -> "${dialect.displayName.uppercase()} • TAP TO CONVERSE"
            }
            val statusColor = when {
                isThinking -> Color(0xFFB388FF)
                isListening -> JarvisAmber
                isSpeaking -> if (isGeminiLiveMode) Color(0xFF00E5FF) else JarvisCyanLight
                else -> JarvisCyan
            }

            Row(
                modifier = Modifier
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isThinking -> Icons.Default.Psychology
                        isSpeaking -> Icons.Default.RecordVoiceOver
                        isListening -> Icons.Default.GraphicEq
                        else -> Icons.Default.Hearing
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Real-Time Acoustic & Speech Recognition Floating Stream Bar
            AnimatedVisibility(
                visible = isListening || (livePartialTranscript.isNotBlank() && isThinking),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0D1B2A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(JarvisAmber)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE SPEECH STREAM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "SNR: ${"%.1f".format(snrDb)}dB • RMS: ${"%.0f".format(liveRmsDb)}dB",
                                fontSize = 9.sp,
                                color = JarvisTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (livePartialTranscript.isNotBlank()) "\"$livePartialTranscript\"" else "Awaiting acoustic input...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Voice Command Trigger Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "Brief me on today's schedule",
                    "Turn on the living room chandelier",
                    "Set thermostat to 72 degrees",
                    "Lock the front door deadbolt",
                    "Run Morning Routine",
                    "Open studio blinds to 100%",
                    "Who are you and what can you do?"
                )
                chips.forEach { chipText ->
                    Box(
                        modifier = Modifier
                            .border(0.8.dp, JarvisBorder, RoundedCornerShape(20.dp))
                            .background(JarvisSurfaceVariant, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.submitQuery(chipText)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        // Conversational Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onReplay = {
                        viewModel.submitQuery(msg.text)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bottom Voice & Query Input Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone Toggle Button with Live Pulse Ring
                IconButton(
                    onClick = {
                        viewModel.toggleVoiceListening()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isListening) JarvisAmber.copy(alpha = 0.25f) else JarvisSurfaceElevated)
                        .border(
                            1.dp,
                            if (isListening) JarvisAmber else JarvisCyan.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .testTag("mic_voice_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Voice Input",
                        tint = if (isListening) JarvisAmber else JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Query Text Field
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            text = if (isGeminiLiveMode) "Speak or ask Gemini Live anything..." else "Ask Jarvis or give smart home command...",
                            fontSize = 13.sp,
                            color = JarvisTextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("query_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.submitQuery(inputQuery)
                            inputQuery = ""
                        }
                    })
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.submitQuery(inputQuery)
                            inputQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan.copy(alpha = 0.15f))
                        .testTag("send_query_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Command",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Voice Engine Settings Bottom Sheet
    if (voiceSettingsOpen) {
        VoiceEngineSettingsSheet(
            isGeminiLiveMode = isGeminiLiveMode,
            selectedPersona = voicePersona,
            selectedDialect = dialect,
            selectedNoiseFilter = noiseFilter,
            liveRmsDb = liveRmsDb,
            ambientNoiseFloorDb = ambientNoiseFloorDb,
            snrDb = snrDb,
            onGeminiLiveModeToggle = { viewModel.setGeminiLiveMode(it) },
            onSelectPersona = { viewModel.setVoicePersona(it) },
            onSelectDialect = { viewModel.setDialect(it) },
            onSelectNoiseFilter = { viewModel.setNoiseFilter(it) },
            onDismiss = { viewModel.setVoiceSettingsOpen(false) }
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onReplay: () -> Unit = {}
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                // Jarvis Avatar
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceElevated)
                        .border(
                            1.dp,
                            if (message.isGeminiLive) Color(0xFFB388FF) else JarvisCyan.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (message.isGeminiLive) "G" else "J",
                        color = if (message.isGeminiLive) Color(0xFFB388FF) else JarvisCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isUser -> JarvisCobalt
                        isSystem -> JarvisSurfaceVariant
                        message.isGeminiLive -> Color(0xFF161426)
                        else -> JarvisSurfaceElevated
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    when {
                        isUser -> JarvisCobalt
                        message.isGeminiLive -> Color(0xFF7C4DFF).copy(alpha = 0.4f)
                        else -> JarvisBorder
                    }
                ),
                modifier = Modifier.widthIn(max = 310.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Latency & Engine telemetry
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isUser) {
                                if (message.isGeminiLive) {
                                    Text(
                                        text = "GEMINI LIVE • ${message.latencyMs ?: 240}ms",
                                        fontSize = 9.sp,
                                        color = Color(0xFFB388FF),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else if (message.latencyMs != null) {
                                    Text(
                                        text = "${message.latencyMs}ms • 100% LOCAL",
                                        fontSize = 9.sp,
                                        color = JarvisEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else if (message.detectedDialect != null) {
                                Text(
                                    text = "${message.detectedDialect} • ${((message.confidenceScore ?: 0.94f) * 100).toInt()}% Conf",
                                    fontSize = 9.sp,
                                    color = JarvisAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = JarvisTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
