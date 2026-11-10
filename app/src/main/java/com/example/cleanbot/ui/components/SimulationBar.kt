package com.example.cleanbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.cleanbot.data.CleanBotRepository
import com.example.ui.theme.*

@Composable
fun SimulationControlsBar(
    isRunning: Boolean,
    speedMultiplier: Int,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulation_controls_bar"),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyanAccent.copy(alpha = 0.2f))
                            .border(1.dp, CyanAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SIMULATION ENGINE",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isRunning) "Active (Ticking)" else "Simulation Paused",
                        color = if (isRunning) StatusGreen else StatusAmber,
                        fontSize = 11.sp
                    )
                }

                // Speed buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 5).forEach { speed ->
                        val isSelected = speedMultiplier == speed
                        Surface(
                            onClick = { repository.setSimulationSpeed(speed) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) CyanAccent else Navy700,
                            modifier = Modifier.height(28.dp).testTag("sim_speed_${speed}x")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "${speed}x",
                                    color = if (isSelected) Navy950 else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Primary Controls: Play/Pause, Reset, and Diagnostic Triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { repository.toggleSimulationPause() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isRunning) StatusAmberBg else StatusGreenBg,
                        contentColor = if (isRunning) StatusAmber else StatusGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("sim_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isRunning) "Pause" else "Run", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { repository.resetSimulation() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier.height(34.dp).testTag("sim_reset_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp)
                }

                // Force Container Full Workflow Button
                Button(
                    onClick = { repository.forceFullContainer() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRoseBg, contentColor = StatusRose),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).weight(1f).testTag("sim_force_full_btn")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Force 100% Full", fontSize = 10.sp, maxLines = 1)
                }

                // Force Low Battery Workflow Button
                Button(
                    onClick = { repository.forceLowBattery() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAmberBg, contentColor = StatusAmber),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).weight(1f).testTag("sim_force_battery_btn")
                ) {
                    Icon(imageVector = Icons.Default.BatteryAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Force Low Bat", fontSize = 10.sp, maxLines = 1)
                }
            }

            Spacer(Modifier.height(6.dp))

            // Diagnostic Secondary Row: Spawn Obstacle & Spawn Dirt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { repository.spawnObstacleInPath() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f).height(30.dp).testTag("sim_spawn_obstacle_btn")
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("+ Obstacle", fontSize = 10.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = { repository.spawnRandomSpill() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.weight(1f).height(30.dp).testTag("sim_spawn_spill_btn")
                ) {
                    Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("+ Dirt Spill", fontSize = 10.sp, color = TextPrimary)
                }
            }
        }
    }
}
