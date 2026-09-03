package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Blinds
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Curtains
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceType
import com.example.data.model.SmartDevice
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
fun SmartHomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    var selectedRoom by remember { mutableStateOf("All") }

    val rooms = listOf("All") + devices.map { it.room }.distinct()
    val filteredDevices = if (selectedRoom == "All") devices else devices.filter { it.room == selectedRoom }

    val activeCount = devices.count { it.isPowered || !it.isLocked }
    val totalPowerWatts = devices.sumOf { it.powerWatts }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Top Telemetry Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SMART HOME MESH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyan,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$activeCount of ${devices.size} Endpoints Active",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(JarvisSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = JarvisAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f W".format(totalPowerWatts),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Group Quick Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = "All Lights Off",
                    icon = Icons.Default.Lightbulb,
                    onClick = {
                        viewModel.submitQuery("turn off all lights")
                    }
                )
                QuickActionButton(
                    title = "Arm All Locks",
                    icon = Icons.Default.Lock,
                    onClick = {
                        viewModel.submitQuery("lock all doors")
                    }
                )
                QuickActionButton(
                    title = "Eco Mode 68°F",
                    icon = Icons.Default.AcUnit,
                    onClick = {
                        viewModel.submitQuery("set thermostat to 68 degrees")
                    }
                )
            }
        }

        // Room Selection Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rooms.forEach { room ->
                    val isSelected = room == selectedRoom
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) JarvisCyan else JarvisSurfaceVariant)
                            .clickable { selectedRoom = room }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = room,
                            color = if (isSelected) JarvisBackground else JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Device Cards List
        items(filteredDevices, key = { it.id }) { device ->
            DeviceControlCard(
                device = device,
                formatTemperature = { viewModel.formatTemperature(it) },
                onTogglePower = { viewModel.toggleDevicePower(device.id) },
                onLevelChange = { level -> viewModel.setDeviceLevel(device.id, level) },
                onTempChange = { delta -> viewModel.setThermostatTemp(device.id, device.targetTemperatureF + delta) },
                onToggleLock = { viewModel.toggleDeviceLock(device.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun DeviceControlCard(
    device: SmartDevice,
    formatTemperature: (Int) -> String = { "$it°F" },
    onTogglePower: () -> Unit,
    onLevelChange: (Int) -> Unit,
    onTempChange: (Int) -> Unit,
    onToggleLock: () -> Unit
) {
    val isPrimaryActive = device.isPowered || (device.type == DeviceType.LOCK && !device.isLocked)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimaryActive) JarvisSurfaceElevated else JarvisSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimaryActive) JarvisCyan.copy(alpha = 0.4f) else JarvisBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Name, Room, Protocol & Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isPrimaryActive) JarvisCyan.copy(alpha = 0.2f) else JarvisSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (device.type) {
                                DeviceType.LIGHT -> Icons.Default.Lightbulb
                                DeviceType.THERMOSTAT -> Icons.Default.Thermostat
                                DeviceType.LOCK -> if (device.isLocked) Icons.Default.Lock else Icons.Default.LockOpen
                                DeviceType.BLINDS -> Icons.Default.Curtains
                                DeviceType.PLUG -> Icons.Default.PowerSettingsNew
                                DeviceType.SENSOR -> Icons.Default.DeviceHub
                            },
                            contentDescription = null,
                            tint = if (isPrimaryActive) JarvisCyan else JarvisTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = device.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${device.room} • ${device.protocol.label}",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Action Switch / Lock Toggle
                if (device.type == DeviceType.LOCK) {
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (device.isLocked) JarvisEmerald.copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = if (device.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (device.isLocked) "Locked" else "Unlocked",
                            tint = if (device.isLocked) JarvisEmerald else JarvisAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Switch(
                        checked = device.isPowered,
                        onCheckedChange = { onTogglePower() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = JarvisTextMuted,
                            uncheckedTrackColor = JarvisSurfaceVariant
                        )
                    )
                }
            }

            // Interactive controls based on device type
            when (device.type) {
                DeviceType.LIGHT, DeviceType.BLINDS -> {
                    if (device.isPowered) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (device.type == DeviceType.LIGHT) "Brightness" else "Position",
                                fontSize = 12.sp,
                                color = JarvisTextSecondary
                            )
                            Text(
                                text = "${device.level}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = device.level.toFloat(),
                            onValueChange = { onLevelChange(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisSurfaceVariant
                            )
                        )
                    }
                }

                DeviceType.THERMOSTAT -> {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT: ${formatTemperature(device.currentTemperatureF)}",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "TARGET: ${formatTemperature(device.targetTemperatureF)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onTempChange(-1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Cooler",
                                    tint = JarvisTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onTempChange(1) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Warmer",
                                    tint = JarvisTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                DeviceType.LOCK -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (device.isLocked) "STATUS: SECURED & ARMED" else "STATUS: UNLOCKED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (device.isLocked) JarvisEmerald else JarvisAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "AES-GCM Protocol",
                            fontSize = 10.sp,
                            color = JarvisTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                DeviceType.PLUG -> {
                    if (device.isPowered) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "REAL-TIME LOAD: 850 Watts (Heating element active)",
                            fontSize = 11.sp,
                            color = JarvisAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
            .background(JarvisSurface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            fontWeight = FontWeight.Medium,
            color = JarvisTextPrimary
        )
    }
}
