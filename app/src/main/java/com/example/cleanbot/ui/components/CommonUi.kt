package com.example.cleanbot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.model.RobotStatus
import com.example.cleanbot.model.TaskPriority
import com.example.ui.theme.*

@Composable
fun RobotStatusBadge(status: RobotStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, dotColor) = when (status) {
        RobotStatus.CLEANING -> Triple(StatusGreenBg, CyanAccent, CyanBright)
        RobotStatus.AVAILABLE -> Triple(StatusGreenBg, StatusGreen, StatusGreen)
        RobotStatus.CHARGING -> Triple(StatusAmberBg, StatusAmber, StatusAmber)
        RobotStatus.RETURNING_TO_STATION -> Triple(Navy700, ElectricBlue, ElectricBlue)
        RobotStatus.WAITING_FOR_OPERATOR -> Triple(StatusRoseBg, StatusRose, StatusRose)
        RobotStatus.PAUSED -> Triple(StatusAmberBg, StatusAmber, StatusAmber)
        RobotStatus.ERROR -> Triple(StatusRoseBg, StatusRose, StatusRose)
        RobotStatus.OFFLINE -> Triple(Navy700, TextMuted, TextMuted)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (status == RobotStatus.CLEANING || status == RobotStatus.WAITING_FOR_OPERATOR)
                        dotColor.copy(alpha = alpha)
                    else dotColor
                )
        )
        Text(
            text = status.displayName.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TaskPriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        TaskPriority.URGENT -> StatusRose
        TaskPriority.HIGH -> StatusAmber
        TaskPriority.MEDIUM -> ElectricBlue
        TaskPriority.LOW -> TextSecondary
    }

    Text(
        text = priority.displayName.uppercase(),
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun TelemetryMeter(
    label: String,
    value: Float, // 0..100
    color: Color,
    unit: String = "%",
    warningThreshold: Float? = null,
    modifier: Modifier = Modifier
) {
    val displayColor = if (warningThreshold != null && value >= warningThreshold) StatusRose else color

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
            Text(
                text = "${value.toInt()}$unit",
                color = displayColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = displayColor,
            trackColor = Navy700,
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = CyanAccent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(text = title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}
