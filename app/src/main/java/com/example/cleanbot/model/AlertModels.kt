package com.example.cleanbot.model

enum class AlertSeverity {
    INFO,
    SUCCESS,
    WARNING,
    CRITICAL
}

enum class NotificationType {
    TASK_COMPLETED,
    TASK_STARTED,
    NEW_REQUEST,
    LOW_BATTERY,
    CONTAINER_FULL,
    RETURN_TO_STATION,
    CHARGING_COMPLETED,
    OBSTACLE_DETECTED,
    OPERATOR_INTERVENTION,
    MAINTENANCE
}

data class AlertItem(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: NotificationType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val actionLabel: String? = null
)
