package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.RobotStatus
import com.example.cleanbot.ui.components.EmptyStateView
import com.example.cleanbot.ui.components.RobotStatusBadge
import com.example.cleanbot.ui.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RobotManagementScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    val robot = state.robot
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(robot?.name ?: "") }
    var editModel by remember { mutableStateOf(robot?.model ?: "") }
    var editSuction by remember { mutableStateOf(robot?.suctionLevel ?: "Standard") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Robot Hardware Management", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Single-Robot Architecture • Enforced Singleton", color = TextSecondary, fontSize = 12.sp)
                }

                if (robot != null && state.currentUser.role.name == "ADMINISTRATOR") {
                    FilledTonalButton(
                        onClick = {
                            editName = robot.name
                            editModel = robot.model
                            editSuction = robot.suctionLevel
                            showEditDialog = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Navy700, contentColor = CyanAccent),
                        modifier = Modifier.testTag("btn_edit_robot")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Configure", fontSize = 11.sp)
                    }
                }
            }
        }

        if (robot == null) {
            // Setup screen shown ONLY if no robot exists yet
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("robot_registration_card"),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No Robot Registered", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Register the single autonomous cleaning robot for the facility.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(Modifier.height(16.dp))

                        var newName by remember { mutableStateOf("CleanBot Prime") }
                        var newModel by remember { mutableStateOf("Enterprise Series X-100") }
                        var newType by remember { mutableStateOf("Dual-Brush Hybrid Suction & Wet Mop") }

                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Robot Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newModel,
                            onValueChange = { newModel = it },
                            label = { Text("Hardware Model") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newType,
                            onValueChange = { newType = it },
                            label = { Text("Chassis Cleaning Type") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { repository.registerRobot(newName, newModel, newType) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                            modifier = Modifier.fillMaxWidth().testTag("btn_register_robot_submit")
                        ) {
                            Text("Register Single Robot", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Display Robot Info Card
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
                            Column {
                                Text(robot.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("ID: ${robot.id} • Model: ${robot.model}", color = TextSecondary, fontSize = 12.sp)
                            }
                            RobotStatusBadge(robot.status)
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Navy700)
                        Spacer(Modifier.height(12.dp))

                        // Technical specs grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Cleaning Mechanism", color = TextMuted, fontSize = 11.sp)
                                Text(robot.type, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Assigned Docking Station", color = TextMuted, fontSize = 11.sp)
                                Text(robot.assignedStationId, color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Last Communication", color = TextMuted, fontSize = 11.sp)
                                val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(robot.lastCommunicationTime))
                                Text(dateStr, color = TextPrimary, fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Container Max Volume", color = TextMuted, fontSize = 11.sp)
                                Text("${robot.containerCapacityMl} ml", color = TextPrimary, fontSize = 12.sp)
                            }
                            Column {
                                Text("Active Task", color = TextMuted, fontSize = 11.sp)
                                Text(robot.currentTaskId ?: "Idle / None", color = CyanAccent, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Hardware Abstraction Layer (HAL) Driver Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = CyanAccent)
                            Text("Hardware Abstraction Layer (HAL)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Navy900, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Active Driver Interface", color = TextSecondary, fontSize = 12.sp)
                                    Text("SimulatedRobotDriver", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Transport Protocol", color = TextSecondary, fontSize = 12.sp)
                                    Text("Virtual Loopback Bus", color = TextPrimary, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Status", color = TextSecondary, fontSize = 12.sp)
                                    Text("Operational", color = StatusGreen, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Physical Hardware Ready Standby Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Navy700, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Text("PhysicalRobotDriver (MQTT Standby)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Physical integration stub configured for MQTT broker (ssl://mqtt.cleanbot.corp:8883). Safety interlocks require verified physical hardware before deployment.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Configuration Dialog
    if (showEditDialog && robot != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Configure Robot Settings", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Robot Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editModel,
                        onValueChange = { editModel = it },
                        label = { Text("Model Version") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSuction,
                        onValueChange = { editSuction = it },
                        label = { Text("Suction Mode (Eco / Standard / Max)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateRobotConfig(editName, editModel, editSuction)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Navy800
        )
    }
}
