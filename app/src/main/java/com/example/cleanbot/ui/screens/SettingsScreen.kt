package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.UserRole
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    var containerThreshold by remember { mutableStateOf(state.containerReturnThreshold) }
    var batteryThreshold by remember { mutableStateOf(state.batteryReturnThreshold) }
    var selectedSuctionMode by remember { mutableStateOf(state.suctionPowerMode) }
    var autoResume by remember { mutableStateOf(state.autoResumeAfterService) }

    var mqttEndpoint by remember { mutableStateOf("ssl://mqtt.cleanbot.corp:8883") }
    var mqttClientId by remember { mutableStateOf("CleanBot-Gateway-01") }

    val isAdmin = state.currentUser.role == UserRole.ADMINISTRATOR

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("System Settings & Telemetry Profiles", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Operational Thresholds, MQTT Broker & Role Management", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Active Role Switcher Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Session & Permissions", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(state.currentUser.role.label, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Current User: ${state.currentUser.name} (${state.currentUser.email})", color = TextSecondary, fontSize = 12.sp)

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { repository.switchUserRole(UserRole.ADMINISTRATOR) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdmin) CyanAccent else Navy700,
                                contentColor = if (isAdmin) Navy950 else TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("btn_switch_admin")
                        ) {
                            Text("Switch to Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { repository.switchUserRole(UserRole.OPERATOR) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAdmin) CyanAccent else Navy700,
                                contentColor = if (!isAdmin) Navy950 else TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("btn_switch_operator")
                        ) {
                            Text("Switch to Operator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Automated Workflow Thresholds (Container & Battery)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Automated Return & Servicing Thresholds", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    // Container threshold slider
                    Text("Full Container Return Threshold: ${containerThreshold.toInt()}%", color = TextSecondary, fontSize = 12.sp)
                    Slider(
                        value = containerThreshold,
                        onValueChange = {
                            containerThreshold = it
                            repository.updateSettings(it, batteryThreshold, selectedSuctionMode, autoResume)
                        },
                        valueRange = 70f..100f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Battery threshold slider
                    Text("Low Battery Return Threshold: ${batteryThreshold.toInt()}%", color = TextSecondary, fontSize = 12.sp)
                    Slider(
                        value = batteryThreshold,
                        onValueChange = {
                            batteryThreshold = it
                            repository.updateSettings(containerThreshold, it, selectedSuctionMode, autoResume)
                        },
                        valueRange = 10f..35f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = StatusAmber, activeTrackColor = StatusAmber)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Resume Unfinished Tasks After Service", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = autoResume,
                            onCheckedChange = {
                                autoResume = it
                                repository.updateSettings(containerThreshold, batteryThreshold, selectedSuctionMode, it)
                            }
                        )
                    }
                }
            }
        }

        // Suction Mode Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Suction Power Mode", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Eco (Silent)", "Standard", "Max Boost (Deep Clean)").forEach { mode ->
                            val isSelected = selectedSuctionMode == mode || (mode.startsWith(selectedSuctionMode))
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val cleaned = mode.split(" ").first()
                                    selectedSuctionMode = cleaned
                                    repository.updateSettings(containerThreshold, batteryThreshold, cleaned, autoResume)
                                },
                                label = { Text(mode, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // MQTT Physical Hardware Integration Config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = CyanAccent)
                        Text("Physical Robot MQTT Gateway", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mqttEndpoint,
                        onValueChange = { mqttEndpoint = it },
                        label = { Text("Broker Endpoint URI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mqttClientId,
                        onValueChange = { mqttClientId = it },
                        label = { Text("Client Gateway Identifier") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Status: Standby. Ready for physical hardware bridge when industrial sensors are installed.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
