package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.RobotStatus
import com.example.cleanbot.ui.components.RobotStatusBadge
import com.example.cleanbot.ui.components.TelemetryMeter
import com.example.ui.theme.*

@Composable
fun ContainerBatteryScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    val robot = state.robot

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Container & Battery Telemetry", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Automated Return, Servicing & Recharging Workflows", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // 1. Waste Container Workflow Section
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = CyanAccent)
                            Text("Waste Container Monitoring", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if ((robot?.containerFillPercent ?: 0f) >= state.containerReturnThreshold) "FULL - SERVICE NEEDED" else "NORMAL",
                            color = if ((robot?.containerFillPercent ?: 0f) >= state.containerReturnThreshold) StatusRose else StatusGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    TelemetryMeter(
                        label = "Current Fill Level (${((robot?.containerFillPercent ?: 0f) / 100f * (robot?.containerCapacityMl ?: 2500)).toInt()} / ${robot?.containerCapacityMl ?: 2500} ml)",
                        value = robot?.containerFillPercent ?: 0f,
                        color = if ((robot?.containerFillPercent ?: 0f) > 85f) StatusRose else CyanAccent,
                        warningThreshold = state.containerReturnThreshold
                    )

                    Spacer(Modifier.height(14.dp))

                    // Step by step workflow display
                    Text("Automated 11-Step Full Container Workflow:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy900, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            WorkflowStep(num = 1, text = "Safely stop active cleaning motors", isDone = (robot?.status == RobotStatus.WAITING_FOR_OPERATOR || robot?.status == RobotStatus.RETURNING_TO_STATION))
                            WorkflowStep(num = 2, text = "Save completed tasks and current room progress %", isDone = true)
                            WorkflowStep(num = 3, text = "Autonomous return navigation to Dock Bay 1", isDone = (robot?.status == RobotStatus.WAITING_FOR_OPERATOR))
                            WorkflowStep(num = 4, text = "Status -> 'Waiting for Operator' & alert dispatched", isDone = (robot?.status == RobotStatus.WAITING_FOR_OPERATOR))
                            WorkflowStep(num = 5, text = "Operator physical dump & confirmation", isDone = false)
                            WorkflowStep(num = 6, text = "Sensor emptiness verification & auto-resume unfinished work", isDone = false)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { repository.emptyContainerAndConfirm() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Navy950),
                            modifier = Modifier.weight(1f).testTag("btn_empty_container_action")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Empty Container & Confirm", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { repository.forceFullContainer() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRose),
                            modifier = Modifier.testTag("btn_force_container_100")
                        ) {
                            Text("Simulate 100% Full", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. Battery & Charging Section
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = StatusGreen)
                            Text("Battery & Power Management", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        if (robot != null) {
                            RobotStatusBadge(robot.status)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    TelemetryMeter(
                        label = "Battery Charge Level",
                        value = robot?.batteryPercent ?: 0f,
                        color = if ((robot?.batteryPercent ?: 0f) < 20f) StatusRose else StatusGreen
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Low Battery Threshold: ${state.batteryReturnThreshold.toInt()}%", color = TextSecondary, fontSize = 12.sp)
                        Text("Resume Threshold: ${state.batteryResumeThreshold.toInt()}%", color = TextSecondary, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { repository.forceLowBattery() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAmber),
                            modifier = Modifier.weight(1f).testTag("btn_force_low_battery")
                        ) {
                            Icon(Icons.Default.BatteryAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Simulate Low Battery (12%)", fontSize = 11.sp)
                        }

                        if (robot?.status == RobotStatus.CHARGING) {
                            FilledTonalButton(
                                onClick = { repository.resumeCurrentTask() },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = StatusGreenBg, contentColor = StatusGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Undock & Resume", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowStep(num: Int, text: String, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) StatusGreen else TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Text("$num. $text", color = if (isDone) TextPrimary else TextSecondary, fontSize = 11.sp)
    }
}
