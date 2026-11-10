package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.cleanbot.ui.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServiceStationScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    val station = state.station
    val robot = state.robot
    val isRobotDocked = (robot?.posX == station.posX && robot?.posY == station.posY) ||
            robot?.status == RobotStatus.CHARGING || robot?.status == RobotStatus.WAITING_FOR_OPERATOR

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Autonomous Service Station", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Assigned Station: ${station.id} • ${station.name}", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Station Telemetry Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "DOCK STATUS",
                    value = if (isRobotDocked) "Robot Docked" else "Bay Empty",
                    subtitle = if (station.isCharging) "Charging Active" else "Ready",
                    icon = Icons.Default.EvStation,
                    iconTint = if (isRobotDocked) CyanAccent else TextMuted,
                    modifier = Modifier.weight(1f).testTag("card_dock_status")
                )
                StatCard(
                    title = "POWER SUPPLY",
                    value = "${station.powerSupplyWatts} W",
                    subtitle = "Fast Charge Ready",
                    icon = Icons.Default.Bolt,
                    iconTint = StatusAmber,
                    modifier = Modifier.weight(1f).testTag("card_power_supply")
                )
            }
        }

        // Station Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Station Specifications & Diagnostics", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Location", color = TextMuted, fontSize = 12.sp)
                        Text(station.locationName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Connection Interface", color = TextMuted, fontSize = 12.sp)
                        Text(if (station.isConnected) "Online (Encrypted Loopback)" else "Offline", color = StatusGreen, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Central Disposal Bin Capacity", color = TextMuted, fontSize = 12.sp)
                        Text("${station.wasteCapacityRemainingMl} ml capacity", color = TextPrimary, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    if (robot?.status == RobotStatus.WAITING_FOR_OPERATOR) {
                        Button(
                            onClick = { repository.emptyContainerAndConfirm() },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Navy950),
                            modifier = Modifier.fillMaxWidth().testTag("btn_station_empty_confirm")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Empty Waste Container & Verify Emptiness", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Maintenance Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Station & Robot Maintenance Needs", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    MaintenanceItem(label = "Dock Charging Contacts", status = "Clean / Verified", isOk = true)
                    MaintenanceItem(label = "HEPA Exhaust Filter", status = "94% Cleanliness", isOk = true)
                    MaintenanceItem(label = "Dual Main Roller Brushes", status = "Nominal Wear (12 hrs remaining)", isOk = true)
                    MaintenanceItem(label = "LiDAR Glass Window", status = "Clear / No Smudges", isOk = true)
                }
            }
        }

        // Docking Arrival & Departure History Log
        item {
            Text("Arrival & Departure Telemetry History", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(state.dockingHistory) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.eventType, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(event.details, color = TextPrimary, fontSize = 12.sp)
                    }
                    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))
                    Text(dateStr, color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun MaintenanceItem(label: String, status: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) StatusGreen else StatusAmber,
                modifier = Modifier.size(14.dp)
            )
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
        Text(status, color = if (isOk) TextPrimary else StatusAmber, fontSize = 11.sp)
    }
}
