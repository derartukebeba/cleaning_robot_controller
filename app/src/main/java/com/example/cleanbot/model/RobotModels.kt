package com.example.cleanbot.model

enum class RobotStatus(val displayName: String) {
    AVAILABLE("Available"),
    CLEANING("Cleaning"),
    RETURNING_TO_STATION("Returning to Station"),
    CHARGING("Charging"),
    WAITING_FOR_OPERATOR("Waiting for Operator"),
    PAUSED("Paused"),
    ERROR("Error"),
    OFFLINE("Offline")
}

enum class CleaningType(val displayName: String, val description: String) {
    FLOOR_ONLY("Floor Only", "Cleans walkways, corridors and open floor areas"),
    TABLE_ONLY("Table Only", "Cleans surfaces of designated meeting tables"),
    DESK_ONLY("Desk Only", "Sanitizes employee desks and workstation surfaces"),
    COMBINED("Combined", "Cleans floors and accessible tabletop surfaces"),
    COMPLETE_ROOM("Complete Room", "Comprehensive deep clean of all room surfaces")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4)
}

enum class TaskStatus(val displayName: String) {
    QUEUED("Queued"),
    IN_PROGRESS("In Progress"),
    PAUSED_FOR_SERVICE("Paused for Service"),
    PAUSED_USER("Paused by Operator"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class RobotRecord(
    val id: String = "CB-X100",
    val name: String = "CleanBot Prime",
    val model: String = "Autonomous Office Series X",
    val type: String = "Dual-Brush Hybrid Suction & Wet Mop",
    val batteryPercent: Float = 94f,
    val containerCapacityMl: Int = 2500,
    val containerFillPercent: Float = 28f,
    val status: RobotStatus = RobotStatus.AVAILABLE,
    val currentTaskId: String? = null,
    val assignedStationId: String = "STN-ALPHA-01",
    val lastCommunicationTime: Long = System.currentTimeMillis(),
    val posX: Float = 15f, // 0..100 scale on map
    val posY: Float = 20f,
    val headingAngle: Float = 90f,
    val currentSpeedMps: Float = 0.35f,
    val lidarRpm: Int = 600,
    val suctionLevel: String = "Standard",
    val isBrushActive: Boolean = false,
    val isMopActive: Boolean = false
)

data class CleaningTask(
    val id: String,
    val locationRoomId: String,
    val roomName: String,
    val cleaningType: CleaningType,
    val priority: TaskPriority,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long? = null,
    val completedTime: Long? = null,
    val expectedDurationMinutes: Int = 15,
    val status: TaskStatus = TaskStatus.QUEUED,
    val progressPercent: Float = 0f,
    val cleanedCellsCount: Int = 0,
    val targetCellsCount: Int = 100,
    val pauseReason: String? = null
)
