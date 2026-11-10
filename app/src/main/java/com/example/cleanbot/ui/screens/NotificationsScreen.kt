package com.example.cleanbot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.cleanbot.model.AlertSeverity
import com.example.cleanbot.ui.components.EmptyStateView
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    state: CleanBotAppState,
    repository: CleanBotRepository,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<AlertSeverity?>(null) }
    val filteredAlerts = if (selectedFilter == null) state.alerts else state.alerts.filter { it.severity == selectedFilter }

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
                    Text("Notification & Alert Center", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${state.alerts.count { !it.isRead }} unread alerts", color = TextSecondary, fontSize = 12.sp)
                }

                if (state.alerts.isNotEmpty()) {
                    TextButton(onClick = { repository.clearAllAlerts() }) {
                        Text("Clear All", color = StatusRose, fontSize = 12.sp)
                    }
                }
            }
        }

        // Severity Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${state.alerts.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AlertSeverity.CRITICAL,
                    onClick = { selectedFilter = if (selectedFilter == AlertSeverity.CRITICAL) null else AlertSeverity.CRITICAL },
                    label = { Text("Critical", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AlertSeverity.WARNING,
                    onClick = { selectedFilter = if (selectedFilter == AlertSeverity.WARNING) null else AlertSeverity.WARNING },
                    label = { Text("Warnings", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AlertSeverity.SUCCESS,
                    onClick = { selectedFilter = if (selectedFilter == AlertSeverity.SUCCESS) null else AlertSeverity.SUCCESS },
                    label = { Text("Success", fontSize = 11.sp) }
                )
            }
        }

        if (filteredAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    EmptyStateView(
                        title = "No Notifications",
                        subtitle = "System alerts, battery warnings, and container events will appear here.",
                        icon = Icons.Default.NotificationsNone
                    )
                }
            }
        } else {
            items(filteredAlerts) { alert ->
                val (color, icon) = when (alert.severity) {
                    AlertSeverity.CRITICAL -> Pair(StatusRose, Icons.Default.Error)
                    AlertSeverity.WARNING -> Pair(StatusAmber, Icons.Default.Warning)
                    AlertSeverity.SUCCESS -> Pair(StatusGreen, Icons.Default.CheckCircle)
                    AlertSeverity.INFO -> Pair(ElectricBlue, Icons.Default.Info)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { repository.markAlertAsRead(alert.id) }
                        .testTag("alert_item_${alert.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.isRead) Navy800 else Navy700
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(alert.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp))
                                Text(dateStr, color = TextMuted, fontSize = 11.sp)
                            }

                            Spacer(Modifier.height(4.dp))
                            Text(alert.message, color = TextSecondary, fontSize = 12.sp)

                            if (alert.actionLabel != null) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (alert.actionLabel == "Empty Container") {
                                            repository.emptyContainerAndConfirm()
                                        }
                                        repository.markAlertAsRead(alert.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Navy950),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(alert.actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
