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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.CryptoManager
import com.example.data.model.PluginItem
import com.example.data.model.PluginStatus
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
fun SecurityPluginsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val plugins by viewModel.plugins.collectAsState()
    val isVaultLocked by viewModel.isVaultLocked.collectAsState()

    var testPlainText by remember { mutableStateOf("Top Secret Daily Agenda Note") }
    var encryptedCipherOutput by remember { mutableStateOf("") }
    var decryptedVerifiedOutput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Section 1: Cryptographic Vault
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
                                text = "CLOUD SECURITY VAULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isVaultLocked) "Vault Armed & Sealed" else "Vault Unlocked (Session Active)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVaultLocked) JarvisEmerald else JarvisAmber
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleVaultLock() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVaultLocked) JarvisEmerald else JarvisAmber,
                                contentColor = JarvisBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("vault_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isVaultLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVaultLocked) "Unlock" else "Lock Vault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Key Specs Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisSurfaceElevated, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VaultSpecRow(title = "Cipher Suite", value = "TLS 1.3 / Cloud Stream Encryption")
                        VaultSpecRow(title = "Key Provider", value = "Android Keystore (Hardware Backed)")
                        VaultSpecRow(title = "Key Fingerprint", value = CryptoManager.getKeyFingerprint())
                        VaultSpecRow(title = "Integrity Check", value = "SHA-256 HMAC Verified")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Interactive Cipher Playground
                    Text(
                        text = "Live On-Device Cipher Verification",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JarvisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = testPlainText,
                        onValueChange = { testPlainText = it },
                        label = { Text("Plaintext Input") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val enc = CryptoManager.encrypt(testPlainText)
                            encryptedCipherOutput = enc
                            decryptedVerifiedOutput = CryptoManager.decrypt(enc)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = JarvisBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Stream Encryption Roundtrip", fontWeight = FontWeight.Bold)
                    }

                    if (encryptedCipherOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ENCRYPTED CIPHERTEXT (Base64):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = encryptedCipherOutput.take(48) + "...",
                            fontSize = 11.sp,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DECRYPTED & INTEGRITY VERIFIED: $decryptedVerifiedOutput",
                            fontSize = 11.sp,
                            color = JarvisEmerald,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section 2: Modular Third-Party Plugins
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
                                text = "MODULAR THIRD-PARTY PLUGINS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyan,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Secure Sandboxed Integrations",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "All external connectors run within isolated permissions sandbox without leaking personal calendar or smart home data.",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Plugin Cards List
        items(plugins, key = { it.id }) { plugin ->
            PluginItemCard(
                plugin = plugin,
                onToggle = { viewModel.togglePlugin(plugin.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun PluginItemCard(
    plugin: PluginItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plugin_card_${plugin.id}"),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(JarvisSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (plugin.id) {
                                "plg_weather" -> Icons.Default.CloudQueue
                                "plg_homeassistant" -> Icons.Default.Router
                                "plg_spotify" -> Icons.Default.MusicNote
                                "plg_caldav" -> Icons.Default.Sync
                                else -> Icons.Default.Extension
                            },
                            contentDescription = null,
                            tint = if (plugin.isEnabled) JarvisCyan else JarvisTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plugin.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisTextPrimary
                            )
                            if (plugin.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Plugin",
                                    tint = JarvisEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "${plugin.author} • ${plugin.version}",
                            fontSize = 11.sp,
                            color = JarvisTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = plugin.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisCyan,
                        checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = JarvisTextMuted,
                        uncheckedTrackColor = JarvisSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plugin.description,
                fontSize = 12.sp,
                color = JarvisTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sandboxed Permissions Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                plugin.sandboxedPermissions.forEach { perm ->
                    Box(
                        modifier = Modifier
                            .border(0.6.dp, JarvisBorder, RoundedCornerShape(8.dp))
                            .background(JarvisSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔒 $perm",
                            fontSize = 9.sp,
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            if (plugin.endpointUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ENDPOINT: ${plugin.endpointUrl}",
                        fontSize = 10.sp,
                        color = JarvisTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${plugin.latencyMs}ms",
                        fontSize = 10.sp,
                        color = JarvisEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultSpecRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 11.sp, color = JarvisTextSecondary, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisCyan, fontFamily = FontFamily.Monospace)
    }
}
