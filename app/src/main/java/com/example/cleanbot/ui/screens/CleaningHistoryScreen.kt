package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
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
import com.example.cleanbot.model.CleaningType
import com.example.cleanbot.model.TaskStatus
import com.example.cleanbot.ui.components.EmptyStateView
import com.example.cleanbot.ui.components.TaskPriorityBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CleaningHistoryScreen(
    state: CleanBotAppState,
    modifier: Modifier = Modifier
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf<CleaningType?>(null) }

    val allTasks = state.tasks
    val filteredTasks = if (selectedTypeFilter == null) allTasks else allTasks.filter { it.cleaningType == selectedTypeFilter }

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
                    Text("Cleaning History & Logs", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${allTasks.size} historical & active operations logged", color = TextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_export_history")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export Report", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Filter chips by cleaning type
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Types", fontSize = 11.sp) }
                )
                listOf(CleaningType.COMBINED, CleaningType.FLOOR_ONLY, CleaningType.COMPLETE_ROOM).forEach { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                        label = { Text(type.displayName, fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        title = "No Tasks in History",
                        subtitle = "Completed and past tasks will appear here with full telemetry and duration breakdown.",
                        icon = Icons.Default.History
                    )
                }
            }
        } else {
            items(filteredTasks.reversed()) { task ->
                val statusColor = when (task.status) {
                    TaskStatus.COMPLETED -> StatusGreen
                    TaskStatus.IN_PROGRESS -> CyanAccent
                    TaskStatus.PAUSED_FOR_SERVICE, TaskStatus.PAUSED_USER -> StatusAmber
                    TaskStatus.CANCELLED -> StatusRose
                    TaskStatus.QUEUED -> TextMuted
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(task.roomName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${task.cleaningType.displayName} • Task ID: ${task.id}", color = TextSecondary, fontSize = 12.sp)
                            }
                            TaskPriorityBadge(task.priority)
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status: ${task.status.displayName}", color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(task.createdAt))
                            Text(timeStr, color = TextMuted, fontSize = 11.sp)
                        }

                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coverage: ${task.progressPercent.toInt()}% completed", color = TextSecondary, fontSize = 11.sp)
                            Text("Est. Duration: ${task.expectedDurationMinutes} min", color = TextSecondary, fontSize = 11.sp)
                        }

                        if (task.pauseReason != null) {
                            Spacer(Modifier.height(6.dp))
                            Text("Note: ${task.pauseReason}", color = StatusAmber, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Export Dialog (CSV / Report Viewer)
    if (showExportDialog) {
        val csvContent = buildString {
            append("Task_ID,Room,Type,Priority,Status,Progress_Percent,Created_At\n")
            allTasks.forEach { t ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(t.createdAt))
                append("${t.id},\"${t.roomName}\",${t.cleaningType.name},${t.priority.name},${t.status.name},${t.progressPercent.toInt()}%,$dateStr\n")
            }
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Cleaning Activity Report", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Formatted CSV Telemetry Export Preview:", color = TextSecondary, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Navy900, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = csvContent,
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Text("Ready for facility management compliance and audit log integration.", color = TextMuted, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950)
                ) {
                    Text("Export Completed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Dismiss", color = TextSecondary) }
            },
            containerColor = Navy800
        )
    }
}
