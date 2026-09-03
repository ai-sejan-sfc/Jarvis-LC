package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Routine
import com.example.data.model.RoutineTriggerType
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

@Composable
fun RoutinesScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val executionStatus by viewModel.routineExecutionStatus.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Execution status banner
        item {
            AnimatedVisibility(visible = executionStatus != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JarvisCyan.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = executionStatus ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUTOMATED ROUTINES & TRIGGERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Local Sequence Orchestrator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automated triggers execute sequenced actions across lighting, locks, climate, and daily briefings with zero internet latency.",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Routines List
        items(routines, key = { it.id }) { routine ->
            RoutineCard(
                routine = routine,
                onRun = { viewModel.executeRoutine(routine.id) },
                onToggleEnabled = { viewModel.toggleRoutineEnabled(routine.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun RoutineCard(
    routine: Routine,
    onRun: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(JarvisSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (routine.id) {
                                "morning" -> Icons.Default.WbSunny
                                "bedtime" -> Icons.Default.Nightlight
                                "leaving" -> Icons.Default.DirectionsWalk
                                else -> Icons.Default.Lightbulb
                            },
                            contentDescription = null,
                            tint = when (routine.id) {
                                "morning" -> JarvisAmber
                                "bedtime" -> JarvisCobalt
                                "leaving" -> JarvisEmerald
                                else -> JarvisCyan
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = routine.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (routine.triggerType) {
                                    RoutineTriggerType.TIME -> Icons.Default.AccessTime
                                    RoutineTriggerType.LOCATION -> Icons.Default.LocationOn
                                    RoutineTriggerType.VOICE -> Icons.Default.Mic
                                    RoutineTriggerType.SENSOR -> Icons.Default.Sensors
                                    RoutineTriggerType.MANUAL -> Icons.Default.PlayArrow
                                },
                                contentDescription = null,
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = routine.triggerCondition,
                                fontSize = 11.sp,
                                color = JarvisTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Switch(
                    checked = routine.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisCyan,
                        checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = JarvisTextMuted,
                        uncheckedTrackColor = JarvisSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sequence Step Pills
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                routine.actions.forEachIndexed { idx, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${idx + 1}.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = action.description,
                            fontSize = 12.sp,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Last Executed + Run Routine Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (routine.lastExecuted != null) "Last run: ${routine.lastExecuted}" else "Ready to trigger",
                    fontSize = 11.sp,
                    color = JarvisTextMuted,
                    fontFamily = FontFamily.Monospace
                )

                Button(
                    onClick = onRun,
                    enabled = routine.isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = JarvisBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("run_routine_${routine.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Run Routine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
