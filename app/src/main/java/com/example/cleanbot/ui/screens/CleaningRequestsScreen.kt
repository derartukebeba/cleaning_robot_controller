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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.*
import com.example.cleanbot.ui.components.EmptyStateView
import com.example.cleanbot.ui.components.TaskPriorityBadge
import com.example.cleanbot.ui.components.TelemetryMeter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleaningRequestsScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val activeTask = state.tasks.find { it.status == TaskStatus.IN_PROGRESS }
    val pausedTasks = state.tasks.filter { it.status == TaskStatus.PAUSED_FOR_SERVICE || it.status == TaskStatus.PAUSED_USER }
    val queuedTasks = state.tasks.filter { it.status == TaskStatus.QUEUED }
        .sortedByDescending { it.priority.level }

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
                    Text("Autonomous Cleaning Operations", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Single-Task Execution • Priority Ordered Queue", color = TextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                    modifier = Modifier.testTag("btn_new_cleaning_request")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Request", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Active Task Card
        item {
            Text("Active Task Execution (Limit: 1)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))

            if (activeTask != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("active_task_card"),
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
                                Text(activeTask.roomName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("${activeTask.cleaningType.displayName} • ID: ${activeTask.id}", color = TextSecondary, fontSize = 12.sp)
                            }
                            TaskPriorityBadge(activeTask.priority)
                        }

                        Spacer(Modifier.height(10.dp))

                        TelemetryMeter(
                            label = "Cleaning Progress",
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
                                modifier = Modifier.weight(1f).testTag("btn_pause_active")
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }

                            Button(
                                onClick = { repository.cancelTask(activeTask.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRoseBg, contentColor = StatusRose),
                                modifier = Modifier.weight(1f).testTag("btn_cancel_active")
                            ) {
                                Text("Cancel Task")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        title = "No Robot Task in Progress",
                        subtitle = "Robot is ready for dispatch.",
                        icon = Icons.Default.CheckCircleOutline
                    )
                }
            }
        }

        // Unfinished / Paused for Service Tasks
        if (pausedTasks.isNotEmpty()) {
            item {
                Text("Paused / Awaiting Service Tasks", color = StatusAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
            }
            items(pausedTasks) { pTask ->
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
                                Text(pTask.roomName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Reason: ${pTask.pauseReason ?: "Service interrupt"}", color = StatusAmber, fontSize = 12.sp)
                            }
                            TaskPriorityBadge(pTask.priority)
                        }

                        Spacer(Modifier.height(8.dp))
                        TelemetryMeter(label = "Preserved Progress", value = pTask.progressPercent, color = StatusAmber)
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { repository.resumeCurrentTask() },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Navy950),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Resume Task", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { repository.cancelTask(pTask.id) }
                            ) {
                                Text("Discard")
                            }
                        }
                    }
                }
            }
        }

        // Priority Queue
        item {
            Text("Priority-Ordered Task Queue (${queuedTasks.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
        }

        if (queuedTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        title = "Task Queue is Empty",
                        subtitle = "Add new cleaning requests to queue autonomous robot operations.",
                        icon = Icons.Default.Inbox
                    )
                }
            }
        } else {
            items(queuedTasks) { qTask ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(qTask.roomName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("${qTask.cleaningType.displayName} • Est. ${qTask.expectedDurationMinutes} min", color = TextSecondary, fontSize = 12.sp)
                            Text("ID: ${qTask.id}", color = TextMuted, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TaskPriorityBadge(qTask.priority)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (state.robot?.status == RobotStatus.AVAILABLE) {
                                    FilledTonalButton(
                                        onClick = { repository.startTask(qTask.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Start", fontSize = 11.sp)
                                    }
                                }
                                IconButton(
                                    onClick = { repository.cancelTask(qTask.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = StatusRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Cleaning Request Dialog
    if (showCreateDialog) {
        var selectedRoomId by remember { mutableStateOf(state.rooms.firstOrNull()?.id ?: "") }
        var selectedType by remember { mutableStateOf(CleaningType.COMBINED) }
        var selectedPriority by remember { mutableStateOf(TaskPriority.HIGH) }
        var durationMinutes by remember { mutableStateOf(20) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Cleaning Request", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Target Room / Location:", color = TextSecondary, fontSize = 12.sp)
                    state.rooms.forEach { room ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRoomId == room.id,
                                onClick = { selectedRoomId = room.id }
                            )
                            Text(room.name, color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text("Cleaning Type Scope:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(CleaningType.FLOOR_ONLY, CleaningType.COMBINED, CleaningType.COMPLETE_ROOM).forEach { cType ->
                            FilterChip(
                                selected = selectedType == cType,
                                onClick = { selectedType = cType },
                                label = { Text(cType.displayName, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text("Priority:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH, TaskPriority.URGENT).forEach { p ->
                            FilterChip(
                                selected = selectedPriority == p,
                                onClick = { selectedPriority = p },
                                label = { Text(p.displayName, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRoomId.isNotBlank()) {
                            repository.createCleaningTask(
                                roomId = selectedRoomId,
                                cleaningType = selectedType,
                                priority = selectedPriority,
                                durationMinutes = durationMinutes
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950)
                ) {
                    Text("Add to Queue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Navy800
        )
    }
}
