package com.example.cleanbot.model

data class Floor(
    val id: String,
    val number: Int,
    val name: String
)

data class Room(
    val id: String,
    val name: String,
    val floorId: String,
    val type: String, // Conference, Workspace, Pantry, Lounge, Reception
    val x: Float, // 0..100 map percentage
    val y: Float,
    val width: Float,
    val height: Float,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCleaned: Boolean = false,
    val lastCleanedTime: Long? = null
)

enum class FurnitureType(val label: String) {
    DESK("Work Desk"),
    MEETING_TABLE("Meeting Table"),
    CABINET("File Cabinet"),
    COUCH("Lounge Seating")
}

data class Furniture(
    val id: String,
    val roomId: String,
    val name: String,
    val type: FurnitureType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isCleaned: Boolean = false
)

data class RestrictedZone(
    val id: String,
    val name: String,
    val floorId: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val reason: String
)

data class Obstacle(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 3f,
    val label: String
)

enum class DirtCategory(val displayName: String) {
    DUST("Fine Dust"),
    SAND("Granular Sand"),
    PAPER("Paper Scraps"),
    FOOD_PARTICLES("Food Crumbs"),
    LIQUID_SPILLS("Liquid Spill"),
    OTHER_DEBRIS("Other Debris")
}

enum class DirtClassification(val displayName: String) {
    DRY("DRY"),
    WET("WET"),
    MIXED("MIXED"),
    UNKNOWN("UNKNOWN")
}

data class DirtDetectionEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val roomId: String,
    val roomName: String,
    val posX: Float,
    val posY: Float,
    val category: DirtCategory,
    val classification: DirtClassification,
    val confidencePercent: Int,
    val recommendedMethod: String,
    val isResolved: Boolean = false
)
