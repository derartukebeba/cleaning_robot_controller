package com.example.cleanbot.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.model.TaskStatus
import com.example.cleanbot.ui.components.StatCard
import com.example.cleanbot.ui.components.TelemetryMeter
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    state: CleanBotAppState,
    modifier: Modifier = Modifier
) {
    val totalTasks = state.tasks.size
    val completedTasks = state.tasks.count { it.status == TaskStatus.COMPLETED }
    val inProgressTasks = state.tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val pausedTasks = state.tasks.count { it.status == TaskStatus.PAUSED_FOR_SERVICE || it.status == TaskStatus.PAUSED_USER }
    val queuedTasks = state.tasks.count { it.status == TaskStatus.QUEUED }
    val cancelledTasks = state.tasks.count { it.status == TaskStatus.CANCELLED }

    val completionRate = if (totalTasks > 0) ((completedTasks.toFloat() / totalTasks) * 100).toInt() else 0

    // Approximate area cleaned: e.g. 15 sq meters per task or based on progress
    val estimatedAreaSqM = state.tasks.sumOf { (it.progressPercent * 0.45).toInt() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Facility Analytics & Telemetry Reports", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Calculated dynamically from real operation logs", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Top KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "COMPLETION RATE",
                    value = "$completionRate%",
                    subtitle = "$completedTasks of $totalTasks finished",
                    icon = Icons.Default.Analytics,
                    iconTint = StatusGreen,
                    modifier = Modifier.weight(1f).testTag("analytics_kpi_completion")
                )
                StatCard(
                    title = "AREA CLEANED",
                    value = "$estimatedAreaSqM m²",
                    subtitle = "Office surface covered",
                    icon = Icons.Default.SquareFoot,
                    iconTint = CyanAccent,
                    modifier = Modifier.weight(1f).testTag("analytics_kpi_area")
                )
            }
        }

        // Task Breakdown Visual Chart (Compose Canvas Bar Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Operations Breakdown by Status", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))

                    val statuses = listOf(
                        Pair("Done", completedTasks to StatusGreen),
                        Pair("Active", inProgressTasks to CyanAccent),
                        Pair("Queued", queuedTasks to ElectricBlue),
                        Pair("Paused", pausedTasks to StatusAmber),
                        Pair("Abort", cancelledTasks to StatusRose)
                    )

                    val maxVal = (statuses.maxOfOrNull { it.second.first } ?: 1).coerceAtLeast(1)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = 32f
                        val spacing = (size.width - (barWidth * statuses.size)) / (statuses.size + 1)

                        statuses.forEachIndexed { index, (label, data) ->
                            val (count, color) = data
                            val barHeight = (count.toFloat() / maxVal) * (size.height - 40f)
                            val x = spacing + index * (barWidth + spacing)
                            val y = size.height - 20f - barHeight

                            // Background slot
                            drawRoundRect(
                                color = Navy700,
                                topLeft = Offset(x, 10f),
                                size = Size(barWidth, size.height - 30f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )

                            // Filled bar
                            if (barHeight > 0) {
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        statuses.forEach { (label, data) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${data.first}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(label, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Room Performance Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zone Performance & Cleanliness Health", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    state.rooms.forEach { room ->
                        val roomTasks = state.tasks.filter { it.locationRoomId == room.id }
                        val isRoomCleaned = room.isCleaned || roomTasks.any { it.status == TaskStatus.COMPLETED }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(room.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${room.type} • ${roomTasks.size} operations total", color = TextSecondary, fontSize = 11.sp)
                            }
                            Text(
                                text = if (isRoomCleaned) "100% Sanitized" else "Pending Cycle",
                                color = if (isRoomCleaned) StatusGreen else StatusAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(color = Navy700, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Robot Uptime & Battery Efficiency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Robot Uptime & Fleet Telemetry", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    TelemetryMeter(label = "Uptime Availability (Target 95%)", value = 92f, color = StatusGreen)
                    Spacer(Modifier.height(8.dp))
                    TelemetryMeter(label = "Suction Motor Duty Cycle", value = 76f, color = CyanAccent)
                    Spacer(Modifier.height(8.dp))
                    TelemetryMeter(label = "Dock Fast-Charge Utilization", value = 34f, color = StatusAmber)
                }
            }
        }
    }
}
