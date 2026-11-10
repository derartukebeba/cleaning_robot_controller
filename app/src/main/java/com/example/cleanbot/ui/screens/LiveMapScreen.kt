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
import com.example.cleanbot.ui.components.OfficeMapCanvas
import com.example.cleanbot.ui.components.TaskPriorityBadge
import com.example.ui.theme.*

@Composable
fun LiveMapScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var showDispatchDialog by remember { mutableStateOf(false) }

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
                    Text("Interactive Office Navigation Map", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Floor 1 • Real-time SLAM & LiDAR Navigation", color = TextSecondary, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = { repository.spawnObstacleInPath() },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = StatusAmberBg, contentColor = StatusAmber),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_map_drop_obstacle")
                ) {
                    Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Drop Obstacle", fontSize = 11.sp)
                }
            }
        }

        // Map Canvas
        item {
            OfficeMapCanvas(
                rooms = state.rooms,
                furniture = state.furniture,
                restrictedZones = state.restrictedZones,
                obstacles = state.obstacles,
                serviceStation = state.station,
                robot = state.robot,
                cleanedTrail = state.cleanedTrailPoints,
                plannedWaypoints = state.plannedWaypoints,
                onRoomSelected = { room ->
                    selectedRoom = room
                    showDispatchDialog = true
                },
                modifier = Modifier.height(340.dp)
            )
        }

        // Room selection inspection card
        if (selectedRoom != null) {
            val room = selectedRoom!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("room_inspect_card"),
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
                                Text(room.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Type: ${room.type} • Priority: ${room.priority.displayName}", color = TextSecondary, fontSize = 12.sp)
                            }
                            TaskPriorityBadge(room.priority)
                        }

                        Spacer(Modifier.height(10.dp))

                        val desksInRoom = state.furniture.filter { it.roomId == room.id }
                        Text(
                            text = "Workstations & Furniture: ${if (desksInRoom.isEmpty()) "None registered" else desksInRoom.joinToString { it.name }}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    repository.createCleaningTask(
                                        roomId = room.id,
                                        cleaningType = CleaningType.COMBINED,
                                        priority = room.priority,
                                        durationMinutes = 15
                                    )
                                    selectedRoom = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                                modifier = Modifier.weight(1f).testTag("btn_dispatch_room_clean")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Clean Room Now", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { selectedRoom = null }
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }

        // Obstacles & Restricted Zones list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Active Obstacles & Virtual Walls", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    if (state.obstacles.isEmpty() && state.restrictedZones.isEmpty()) {
                        Text("No active obstacles or virtual walls.", color = TextMuted, fontSize = 12.sp)
                    } else {
                        state.restrictedZones.forEach { rz ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = StatusRose, modifier = Modifier.size(16.dp))
                                    Text(rz.name, color = TextPrimary, fontSize = 12.sp)
                                }
                                Text(rz.reason, color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        state.obstacles.forEach { obs ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(16.dp))
                                    Text(obs.label, color = TextPrimary, fontSize = 12.sp)
                                }
                                IconButton(
                                    onClick = { repository.removeObstacle(obs.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Obstacle", tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
