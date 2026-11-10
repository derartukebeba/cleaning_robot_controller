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
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.*
import com.example.cleanbot.ui.components.TaskPriorityBadge
import com.example.ui.theme.*

@Composable
fun OfficeManagementScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Rooms (${state.rooms.size})", "Desks & Tables (${state.furniture.size})", "Restricted Zones (${state.restrictedZones.size})")

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddFurnitureDialog by remember { mutableStateOf(false) }
    var showAddRestrictedDialog by remember { mutableStateOf(false) }

    val isAdmin = state.currentUser.role == UserRole.ADMINISTRATOR

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
                    Text("Office Layout & Zones", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("CleanBot HQ • Building Suite 400", color = TextSecondary, fontSize = 12.sp)
                }

                if (isAdmin) {
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> showAddRoomDialog = true
                                1 -> showAddFurnitureDialog = true
                                2 -> showAddRestrictedDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_add_office_item")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when (selectedTab) {
                                0 -> "+ Room"
                                1 -> "+ Desk/Table"
                                else -> "+ Zone"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Navy800,
                contentColor = CyanAccent,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) CyanAccent else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        if (!isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                        Text("You are viewing as Operator (Read-Only). Switch role to Administrator in the drawer menu to edit layout.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Rooms List
                items(state.rooms) { room ->
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
                                Text(room.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Type: ${room.type} • Floor: 1", color = TextSecondary, fontSize = 12.sp)
                                Text(
                                    "Status: ${if (room.isCleaned) "Cleaned" else "Pending Routine Clean"}",
                                    color = if (room.isCleaned) StatusGreen else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TaskPriorityBadge(room.priority)
                                if (isAdmin) {
                                    IconButton(
                                        onClick = { repository.deleteRoom(room.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Furniture List
                items(state.furniture) { fur ->
                    val room = state.rooms.find { it.id == fur.roomId }
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
                                Text(fur.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Type: ${fur.type.label} • Room: ${room?.name ?: "Unknown"}", color = TextSecondary, fontSize = 12.sp)
                            }
                            if (isAdmin) {
                                IconButton(
                                    onClick = { repository.deleteFurniture(fur.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRose, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Restricted Zones List
                items(state.restrictedZones) { zone ->
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = StatusRose, modifier = Modifier.size(16.dp))
                                    Text(zone.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Reason: ${zone.reason}", color = StatusRose.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("Bounds: (${zone.x.toInt()}%, ${zone.y.toInt()}%)", color = TextMuted, fontSize = 11.sp)
                            }
                            if (isAdmin) {
                                IconButton(
                                    onClick = { repository.deleteRestrictedZone(zone.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusRose, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Room Dialog
    if (showAddRoomDialog) {
        var roomName by remember { mutableStateOf("") }
        var roomType by remember { mutableStateOf("Workspace") }
        var roomPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Add Office Room", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Room Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roomType,
                        onValueChange = { roomType = it },
                        label = { Text("Room Category (e.g. Conference, Workspace)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomName.isNotBlank()) {
                            repository.addRoom(
                                Room(
                                    id = "rm_${System.currentTimeMillis()}",
                                    name = roomName,
                                    floorId = "floor-1",
                                    type = roomType,
                                    x = 40f,
                                    y = 60f,
                                    width = 25f,
                                    height = 25f,
                                    priority = roomPriority
                                )
                            )
                            showAddRoomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950)
                ) {
                    Text("Add Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Navy800
        )
    }

    // Add Furniture Dialog
    if (showAddFurnitureDialog) {
        var furName by remember { mutableStateOf("") }
        var selectedRoomId by remember { mutableStateOf(state.rooms.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { showAddFurnitureDialog = false },
            title = { Text("Add Desk or Meeting Table", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = furName,
                        onValueChange = { furName = it },
                        label = { Text("Item Name (e.g. Desk Cluster B2)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (furName.isNotBlank() && selectedRoomId.isNotBlank()) {
                            repository.addFurniture(
                                Furniture(
                                    id = "fur_${System.currentTimeMillis()}",
                                    roomId = selectedRoomId,
                                    name = furName,
                                    type = FurnitureType.DESK,
                                    x = 45f,
                                    y = 65f,
                                    width = 8f,
                                    height = 5f
                                )
                            )
                            showAddFurnitureDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Navy950)
                ) {
                    Text("Add Furniture")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFurnitureDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Navy800
        )
    }

    // Add Restricted Zone Dialog
    if (showAddRestrictedDialog) {
        var zoneName by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("Server Racks / High Voltage") }

        AlertDialog(
            onDismissRequest = { showAddRestrictedDialog = false },
            title = { Text("Add Restricted Zone (Virtual Wall)", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Zone Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Restriction") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (zoneName.isNotBlank()) {
                            repository.addRestrictedZone(
                                RestrictedZone(
                                    id = "res_${System.currentTimeMillis()}",
                                    name = zoneName,
                                    floorId = "floor-1",
                                    x = 50f,
                                    y = 30f,
                                    width = 15f,
                                    height = 10f,
                                    reason = reason
                                )
                            )
                            showAddRestrictedDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRose, contentColor = TextPrimary)
                ) {
                    Text("Create Virtual Wall")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRestrictedDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = Navy800
        )
    }
}
