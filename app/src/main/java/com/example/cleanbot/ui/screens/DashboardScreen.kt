package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.cleanbot.model.*
import com.example.cleanbot.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    onNavigateToMap: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToStation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val robot = state.robot
    val activeTask = state.tasks.find { it.id == robot?.currentTaskId && it.status == TaskStatus.IN_PROGRESS }
    val queuedTasks = state.tasks.filter { it.status == TaskStatus.QUEUED }
    val completedCount = state.tasks.count { it.status == TaskStatus.COMPLETED }
    val unreadAlertsCount = state.alerts.count { !it.isRead }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Simulation Bar at top of dashboard
        item {
            SimulationControlsBar(
                isRunning = state.isSimulationRunning,
                speedMultiplier = state.simulationSpeedMultiplier,
                repository = repository
            )
        }

        // 2. Critical Operator Intervention Banner (Full Container Workflow)
        if (robot?.status == RobotStatus.WAITING_FOR_OPERATOR) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("operator_intervention_banner"),
                    colors = CardDefaults.cardColors(containerColor = StatusRoseBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRose)
                            Text(
                                text = "OPERATOR INTERVENTION REQUIRED",
                                color = StatusRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Waste container reached 100% capacity. The robot has safely docked at ${state.station.name} and is waiting for servicing.",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { repository.emptyContainerAndConfirm() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            modifier = Modifier.fillMaxWidth().testTag("empty_container_confirm_btn")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Navy950)
                            Spacer(Modifier.width(8.dp))
                            Text("Empty Waste Container & Resume Task", color = Navy950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Top KPI Summary Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "ROBOT STATUS",
                        value = robot?.status?.displayName ?: "No Robot",
                        subtitle = "Dock: ${state.station.id}",
                        icon = Icons.Default.SmartToy,
                        iconTint = when (robot?.status) {
                            RobotStatus.CLEANING -> CyanAccent
                            RobotStatus.AVAILABLE -> StatusGreen
                            RobotStatus.CHARGING -> StatusAmber
                            else -> StatusRose
                        },
                        modifier = Modifier.weight(1f).testTag("card_robot_status")
                    )
                    StatCard(
                        title = "BATTERY",
                        value = "${robot?.batteryPercent?.toInt() ?: 0}%",
                        subtitle = if (robot?.status == RobotStatus.CHARGING) "Charging (120W)" else "Operational",
                        icon = Icons.Default.BatteryChargingFull,
                        iconTint = if ((robot?.batteryPercent ?: 0f) < 20f) StatusRose else StatusGreen,
                        modifier = Modifier.weight(1f).testTag("card_battery")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "CONTAINER",
                        value = "${robot?.containerFillPercent?.toInt() ?: 0}%",
                        subtitle = "${robot?.containerCapacityMl ?: 2500} ml bin",
                        icon = Icons.Default.DeleteOutline,
                        iconTint = if ((robot?.containerFillPercent ?: 0f) > 85f) StatusRose else ElectricBlue,
                        modifier = Modifier.weight(1f).testTag("card_container")
                    )
                    StatCard(
                        title = "QUEUE / DONE",
                        value = "${queuedTasks.size} / $completedCount",
                        subtitle = "$unreadAlertsCount active alerts",
                        icon = Icons.Default.TaskAlt,
                        iconTint = CyanBright,
                        modifier = Modifier.weight(1f).testTag("card_queue")
                    )
                }
            }
        }

        // 4. Live Map Preview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                            Text("Interactive Office Navigation", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onNavigateToMap) {
                            Text("Full Map", color = CyanAccent, fontSize = 12.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OfficeMapCanvas(
                        rooms = state.rooms,
                        furniture = state.furniture,
                        restrictedZones = state.restrictedZones,
                        obstacles = state.obstacles,
                        serviceStation = state.station,
                        robot = robot,
                        cleanedTrail = state.cleanedTrailPoints,
                        plannedWaypoints = state.plannedWaypoints,
                        onRoomSelected = { /* Navigate or show dialog */ },
                        modifier = Modifier.height(280.dp)
                    )
                }
            }
        }

        // 5. Active Task Status & Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Cleaning Operation", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (robot != null) {
                            RobotStatusBadge(robot.status)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (activeTask != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(activeTask.roomName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("${activeTask.cleaningType.displayName} • ${activeTask.id}", color = TextSecondary, fontSize = 12.sp)
                            }
                            TaskPriorityBadge(activeTask.priority)
                        }

                        Spacer(Modifier.height(10.dp))

                        TelemetryMeter(
                            label = "Task Completion Progress",
                            value = activeTask.progressPercent,
                            color = CyanAccent
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { repository.pauseCurrentTask() },
                                modifier = Modifier.weight(1f).testTag("btn_pause_task")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pause", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { repository.cancelTask(activeTask.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRoseBg, contentColor = StatusRose),
                                modifier = Modifier.weight(1f).testTag("btn_cancel_task")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Abort", fontSize = 12.sp)
                            }
                        }
                    } else {
                        EmptyStateView(
                            title = "No Active Cleaning Operation",
                            subtitle = if (queuedTasks.isNotEmpty()) "${queuedTasks.size} tasks waiting in queue" else "Robot idle. Create a new cleaning request to begin.",
                            icon = Icons.Default.CheckCircleOutline
                        )
                        if (queuedTasks.isNotEmpty() && robot?.status == RobotStatus.AVAILABLE) {
                            Button(
                                onClick = { repository.startTask(queuedTasks.first().id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                                modifier = Modifier.fillMaxWidth().testTag("btn_start_queued")
                            ) {
                                Text("Dispatch Next Queued Task", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 6. Live Telemetry Readings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Sensor Telemetry & Hardware State", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Position (X, Y)", color = TextMuted, fontSize = 11.sp)
                            Text("(${robot?.posX?.toInt() ?: 0}%, ${robot?.posY?.toInt() ?: 0}%)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Heading", color = TextMuted, fontSize = 11.sp)
                            Text("${robot?.headingAngle?.toInt() ?: 0}°", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Speed", color = TextMuted, fontSize = 11.sp)
                            Text("${robot?.currentSpeedMps ?: 0f} m/s", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("LiDAR RPM", color = TextMuted, fontSize = 11.sp)
                            Text("${robot?.lidarRpm ?: 0}", color = ElectricBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Navy700)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (robot?.isBrushActive == true) StatusGreen else TextMuted)
                            )
                            Text("Dual Brush: ${if (robot?.isBrushActive == true) "ENGAGED" else "OFF"}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (robot?.isMopActive == true) CyanAccent else TextMuted)
                            )
                            Text("Wet Mop: ${if (robot?.isMopActive == true) "ENGAGED" else "OFF"}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Text("Suction: ${robot?.suctionLevel ?: "Eco"}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        // 7. Recent Alerts Quick Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent System Alerts", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateToAlerts) {
                            Text("View All", color = CyanAccent, fontSize = 12.sp)
                        }
                    }

                    if (state.alerts.isEmpty()) {
                        Text("No active alerts at this time.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        state.alerts.take(3).forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { repository.markAlertAsRead(alert.id) },
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (alert.severity) {
                                                AlertSeverity.CRITICAL -> StatusRose
                                                AlertSeverity.WARNING -> StatusAmber
                                                AlertSeverity.SUCCESS -> StatusGreen
                                                AlertSeverity.INFO -> ElectricBlue
                                            }
                                        )
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alert.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(alert.message, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
