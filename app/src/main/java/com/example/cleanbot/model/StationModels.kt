package com.example.cleanbot.model

data class ServiceStation(
    val id: String = "STN-ALPHA-01",
    val name: String = "Main Station Alpha",
    val locationName: String = "Corridor Central - Bay 1",
    val floorId: String = "floor-1",
    val posX: Float = 15f,
    val posY: Float = 20f,
    val isConnected: Boolean = true,
    val isCharging: Boolean = false,
    val containerEmptyingStatus: String = "Ready", // Ready, Emptying In Progress, Attention Required
    val lastServiceTime: Long = System.currentTimeMillis() - 3600000,
    val powerSupplyWatts: Int = 120,
    val wasteCapacityRemainingMl: Int = 15000
)

data class DockingEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // DOCKED_FOR_CHARGE, DOCKED_CONTAINER_FULL, OPERATOR_EMPTIED, UNDOCKED_FOR_TASK
    val details: String
)
