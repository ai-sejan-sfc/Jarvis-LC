package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DashboardScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val health by viewModel.health.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Top Connectivity Matrix
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CONNECTIVITY & SYSTEM HEALTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Cross-Platform Diagnostics",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        }

                        Button(
                            onClick = { viewModel.pingDiagnostic() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisSurfaceElevated,
                                contentColor = JarvisCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("ping_diagnostic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ping Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid of network stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NetworkStatItem(
                            label = "HUB LATENCY",
                            value = "${health.pingMs} ms",
                            color = JarvisEmerald
                        )
                        NetworkStatItem(
                            label = "PACKET LOSS",
                            value = "${health.packetLossPercent}%",
                            color = JarvisEmerald
                        )
                        NetworkStatItem(
                            label = "MATTER NODES",
                            value = "${health.matterNodesOnline}/${health.matterNodesTotal}",
                            color = JarvisCyan
                        )
                        NetworkStatItem(
                            label = "UPTIME",
                            value = health.uptimeFormatted,
                            color = JarvisTextPrimary
                        )
                    }
                }
            }
        }

        // Hardware System Health Gauges
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ON-DEVICE HARDWARE TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyan,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // CPU Metric
                    MetricRow(
                        label = "Edge Neural CPU Load",
                        valueStr = "${health.cpuPercent}%",
                        fraction = health.cpuPercent / 100f,
                        color = if (health.cpuPercent > 75) JarvisAmber else JarvisCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // RAM Metric
                    val ramFraction = health.ramUsedMb.toFloat() / health.ramTotalMb.toFloat()
                    MetricRow(
                        label = "Unified RAM Allocation",
                        valueStr = "${health.ramUsedMb} MB / ${health.ramTotalMb} MB",
                        fraction = ramFraction,
                        color = JarvisCobalt
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery & Thermal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = JarvisEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Battery: ${health.batteryPercent}% (Nominal)",
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Thermal: ${health.batteryTempC}°C",
                                fontSize = 12.sp,
                                color = JarvisTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Encrypted Storage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = JarvisAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Encrypted Vault Storage",
                                fontSize = 12.sp,
                                color = JarvisTextPrimary
                            )
                        }
                        Text(
                            text = "${health.storageEncryptedGb} GB / ${health.storageTotalGb.toInt()} GB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Real-time Sync & CRDT Vector Clock Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REAL-TIME SYNC & MESH REPLICATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisEmerald,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (syncState.isOnline) "Mesh Sync Synchronized" else "Offline Queue Active",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerManualSync() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisEmerald,
                                contentColor = JarvisBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replicate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pending mutations: ${syncState.pendingMutations} • Conflicts: ${syncState.conflictCount} • Target: ${syncState.syncTarget}",
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vector Clock Log Terminal Stream
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        syncState.recentLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "[${log.timestamp}] ${log.action} (${log.entity})",
                                    fontSize = 10.sp,
                                    color = JarvisCyanLight,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.vectorClock,
                                    fontSize = 10.sp,
                                    color = JarvisEmerald,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun NetworkStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = JarvisTextMuted,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MetricRow(
    label: String,
    valueStr: String,
    fraction: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = JarvisTextSecondary)
            Text(
                text = valueStr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = JarvisSurfaceVariant,
        )
    }
}
