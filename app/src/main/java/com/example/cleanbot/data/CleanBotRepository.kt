package com.example.cleanbot.data

import com.example.cleanbot.hal.PhysicalRobotDriver
import com.example.cleanbot.hal.RobotDriver
import com.example.cleanbot.hal.SimulatedRobotDriver
import com.example.cleanbot.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

data class CleanBotAppState(
    val robot: RobotRecord? = RobotRecord(),
    val isRobotRegistered: Boolean = true,
    val station: ServiceStation = ServiceStation(),
    val rooms: List<Room> = emptyList(),
    val furniture: List<Furniture> = emptyList(),
    val restrictedZones: List<RestrictedZone> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val tasks: List<CleaningTask> = emptyList(),
    val dirtEvents: List<DirtDetectionEvent> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val dockingHistory: List<DockingEvent> = emptyList(),
    val currentUser: UserProfile = UserProfile(),
    val cleanedTrailPoints: List<Pair<Float, Float>> = emptyList(),
    val plannedWaypoints: List<Pair<Float, Float>> = emptyList(),
    // Simulation state
    val isSimulationRunning: Boolean = true,
    val simulationSpeedMultiplier: Int = 1, // 1x, 2x, 5x
    val activeDriverType: String = "SIMULATED", // "SIMULATED" or "PHYSICAL"
    // Settings
    val containerReturnThreshold: Float = 95f,
    val batteryReturnThreshold: Float = 20f,
    val batteryResumeThreshold: Float = 85f,
    val suctionPowerMode: String = "Standard",
    val autoResumeAfterService: Boolean = true
)

class CleanBotRepository private constructor() {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val simulatedDriver = SimulatedRobotDriver()
    private val physicalDriver = PhysicalRobotDriver()
    private var currentDriver: RobotDriver = simulatedDriver

    private val _appState = MutableStateFlow(createInitialState())
    val appState: StateFlow<CleanBotAppState> = _appState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
    }

    private fun createInitialState(): CleanBotAppState {
        val defaultRooms = listOf(
            Room("rm_01", "Reception Area", "floor-1", "Reception", 5f, 5f, 28f, 25f, TaskPriority.HIGH),
            Room("rm_02", "Open Workspace Alpha", "floor-1", "Workspace", 38f, 5f, 57f, 35f, TaskPriority.MEDIUM),
            Room("rm_03", "Executive Conference", "floor-1", "Conference", 5f, 35f, 28f, 28f, TaskPriority.URGENT),
            Room("rm_04", "Lounge & Breakroom", "floor-1", "Lounge", 5f, 68f, 28f, 26f, TaskPriority.LOW),
            Room("rm_05", "Quiet Focus Pods", "floor-1", "Workspace", 38f, 45f, 25f, 49f, TaskPriority.MEDIUM),
            Room("rm_06", "Executive Boardroom", "floor-1", "Conference", 68f, 45f, 27f, 49f, TaskPriority.HIGH)
        )

        val defaultFurniture = listOf(
            Furniture("fur_01", "rm_02", "Desk Cluster A1", FurnitureType.DESK, 42f, 10f, 10f, 6f),
            Furniture("fur_02", "rm_02", "Desk Cluster A2", FurnitureType.DESK, 56f, 10f, 10f, 6f),
            Furniture("fur_03", "rm_02", "Desk Cluster A3", FurnitureType.DESK, 70f, 10f, 10f, 6f),
            Furniture("fur_04", "rm_03", "Conference Oval Table", FurnitureType.MEETING_TABLE, 10f, 44f, 18f, 10f),
            Furniture("fur_05", "rm_06", "Boardroom Table 12P", FurnitureType.MEETING_TABLE, 72f, 60f, 18f, 14f),
            Furniture("fur_06", "rm_04", "Pantry Coffee Table", FurnitureType.COUCH, 12f, 78f, 14f, 8f)
        )

        val defaultRestricted = listOf(
            RestrictedZone("res_01", "Server Racks & UPS", "floor-1", 38f, 38f, 18f, 6f, "High-Voltage & Fiber Racks"),
            RestrictedZone("res_02", "HVAC Telecom Closet", "floor-1", 5f, 62f, 12f, 5f, "Restricted Equipment")
        )

        val defaultObstacles = listOf(
            Obstacle("obs_01", 62f, 26f, 2.5f, "Spilled Delivery Box")
        )

        val defaultTasks = listOf(
            CleaningTask(
                id = "TASK-101",
                locationRoomId = "rm_03",
                roomName = "Executive Conference",
                cleaningType = CleaningType.COMBINED,
                priority = TaskPriority.URGENT,
                createdAt = System.currentTimeMillis() - 1800000,
                expectedDurationMinutes = 15,
                status = TaskStatus.IN_PROGRESS,
                progressPercent = 42f,
                cleanedCellsCount = 42,
                targetCellsCount = 100
            ),
            CleaningTask(
                id = "TASK-102",
                locationRoomId = "rm_02",
                roomName = "Open Workspace Alpha",
                cleaningType = CleaningType.FLOOR_ONLY,
                priority = TaskPriority.HIGH,
                createdAt = System.currentTimeMillis() - 1200000,
                expectedDurationMinutes = 25,
                status = TaskStatus.QUEUED,
                progressPercent = 0f
            ),
            CleaningTask(
                id = "TASK-103",
                locationRoomId = "rm_06",
                roomName = "Executive Boardroom",
                cleaningType = CleaningType.COMPLETE_ROOM,
                priority = TaskPriority.MEDIUM,
                createdAt = System.currentTimeMillis() - 600000,
                expectedDurationMinutes = 20,
                status = TaskStatus.QUEUED,
                progressPercent = 0f
            )
        )

        val defaultAlerts = listOf(
            AlertItem(
                id = "alt_01",
                timestamp = System.currentTimeMillis() - 120000,
                type = NotificationType.TASK_STARTED,
                severity = AlertSeverity.INFO,
                title = "Cleaning Task Started",
                message = "Task #TASK-101 initiated in Executive Conference room.",
                isRead = true
            ),
            AlertItem(
                id = "alt_02",
                timestamp = System.currentTimeMillis() - 60000,
                type = NotificationType.OBSTACLE_DETECTED,
                severity = AlertSeverity.WARNING,
                title = "Obstacle Detected & Bypassed",
                message = "LiDAR detected Spilled Delivery Box at (62, 26). Rerouted path safely.",
                isRead = false,
                actionLabel = "View on Map"
            )
        )

        val defaultStation = ServiceStation(
            id = "STN-ALPHA-01",
            name = "Docking Bay STN-01",
            locationName = "Main Central Corridor (Bay 1)",
            posX = 15f,
            posY = 20f,
            isConnected = true,
            isCharging = false,
            containerEmptyingStatus = "Ready"
        )

        val defaultRobot = RobotRecord(
            id = "CB-X100",
            name = "CleanBot AI Prime",
            model = "Autonomous Enterprise X-100",
            type = "Dual-Brush Hybrid Suction & Wet Mop",
            batteryPercent = 88f,
            containerCapacityMl = 2500,
            containerFillPercent = 34f,
            status = RobotStatus.CLEANING,
            currentTaskId = "TASK-101",
            assignedStationId = defaultStation.id,
            posX = 18f,
            posY = 42f,
            headingAngle = 135f,
            currentSpeedMps = 0.42f,
            lidarRpm = 600,
            suctionLevel = "Standard",
            isBrushActive = true,
            isMopActive = true
        )

        val initialTrail = listOf(
            Pair(15f, 20f),
            Pair(15f, 32f),
            Pair(12f, 38f),
            Pair(16f, 40f),
            Pair(18f, 42f)
        )

        val initialWaypoints = listOf(
            Pair(18f, 42f),
            Pair(22f, 44f),
            Pair(20f, 52f),
            Pair(12f, 54f),
            Pair(10f, 42f)
        )

        return CleanBotAppState(
            robot = defaultRobot,
            isRobotRegistered = true,
            station = defaultStation,
            rooms = defaultRooms,
            furniture = defaultFurniture,
            restrictedZones = defaultRestricted,
            obstacles = defaultObstacles,
            tasks = defaultTasks,
            dirtEvents = emptyList(),
            alerts = defaultAlerts,
            dockingHistory = listOf(
                DockingEvent("dock_01", System.currentTimeMillis() - 7200000, "UNDOCK_CLEANING", "Undocked to begin scheduled cleaning sequence")
            ),
            currentUser = UserProfile(),
            cleanedTrailPoints = initialTrail,
            plannedWaypoints = initialWaypoints
        )
    }

    // ==========================================
    // SIMULATION LOOP & TICK ENGINE
    // ==========================================
    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = coroutineScope.launch {
            while (isActive) {
                val delayMs = when (_appState.value.simulationSpeedMultiplier) {
                    2 -> 500L
                    5 -> 200L
                    else -> 1000L
                }
                delay(delayMs)
                if (_appState.value.isSimulationRunning) {
                    runSimulationTick()
                }
            }
        }
    }

    private fun runSimulationTick() {
        val state = _appState.value
        val robot = state.robot ?: return

        when (robot.status) {
            RobotStatus.CLEANING -> handleCleaningTick(state, robot)
            RobotStatus.RETURNING_TO_STATION -> handleReturningToStationTick(state, robot)
            RobotStatus.CHARGING -> handleChargingTick(state, robot)
            RobotStatus.WAITING_FOR_OPERATOR -> {
                // Robot waits idly at station until operator confirms container emptying
            }
            RobotStatus.AVAILABLE -> handleAvailableTick(state, robot)
            RobotStatus.PAUSED, RobotStatus.ERROR, RobotStatus.OFFLINE -> {
                // No movement
            }
        }
    }

    private fun handleCleaningTick(state: CleanBotAppState, robot: RobotRecord) {
        val activeTask = state.tasks.find { it.id == robot.currentTaskId && it.status == TaskStatus.IN_PROGRESS }

        if (activeTask == null) {
            // No active task, look for next in queue or become available
            startNextQueuedTask()
            return
        }

        // 1. Check Full Container condition
        if (robot.containerFillPercent >= state.containerReturnThreshold) {
            triggerFullContainerWorkflow("Container reached ${robot.containerFillPercent.toInt()}% full")
            return
        }

        // 2. Check Low Battery condition
        if (robot.batteryPercent <= state.batteryReturnThreshold) {
            triggerLowBatteryWorkflow("Battery depleted to ${robot.batteryPercent.toInt()}%")
            return
        }

        // 3. Increment task progress and fill container, consume battery
        val newProgress = (activeTask.progressPercent + 2f).coerceAtMost(100f)
        val newCleanedCells = (activeTask.cleanedCellsCount + 2).coerceAtMost(activeTask.targetCellsCount)
        val newContainerFill = (robot.containerFillPercent + 1.2f).coerceAtMost(100f)
        val newBattery = (robot.batteryPercent - 0.4f).coerceAtLeast(0f)

        // 4. Move robot towards target waypoints or sweep room
        val targetRoom = state.rooms.find { it.id == activeTask.locationRoomId }
        val (nextX, nextY, heading) = calculateNextCleaningPosition(robot, targetRoom, state)

        val updatedTrail = (state.cleanedTrailPoints + Pair(nextX, nextY)).takeLast(80)

        // Random simulated dirt detection
        val maybeDirt = if (Random.nextFloat() < 0.08f) {
            generateSimulatedDirtEvent(targetRoom?.id ?: "rm_01", targetRoom?.name ?: "Room", nextX, nextY)
        } else null

        val updatedDirtList = if (maybeDirt != null) state.dirtEvents + maybeDirt else state.dirtEvents

        if (newProgress >= 100f) {
            // Task Completed!
            val completedTask = activeTask.copy(
                status = TaskStatus.COMPLETED,
                progressPercent = 100f,
                completedTime = System.currentTimeMillis()
            )
            val updatedTasks = state.tasks.map { if (it.id == completedTask.id) completedTask else it }

            val updatedRooms = state.rooms.map {
                if (it.id == activeTask.locationRoomId) it.copy(isCleaned = true, lastCleanedTime = System.currentTimeMillis()) else it
            }

            val completeAlert = AlertItem(
                id = "alt_${System.currentTimeMillis()}",
                type = NotificationType.TASK_COMPLETED,
                severity = AlertSeverity.SUCCESS,
                title = "Cleaning Task Completed",
                message = "Finished cleaning ${activeTask.roomName} (${activeTask.cleaningType.displayName})."
            )

            _appState.value = state.copy(
                tasks = updatedTasks,
                rooms = updatedRooms,
                dirtEvents = updatedDirtList,
                alerts = listOf(completeAlert) + state.alerts,
                cleanedTrailPoints = updatedTrail,
                robot = robot.copy(
                    posX = nextX,
                    posY = nextY,
                    headingAngle = heading,
                    batteryPercent = newBattery,
                    containerFillPercent = newContainerFill,
                    currentTaskId = null,
                    status = RobotStatus.AVAILABLE,
                    isBrushActive = false,
                    isMopActive = false
                )
            )

            // Auto-check next task
            startNextQueuedTask()
        } else {
            // Continue cleaning
            val updatedTasks = state.tasks.map {
                if (it.id == activeTask.id) it.copy(progressPercent = newProgress, cleanedCellsCount = newCleanedCells) else it
            }

            _appState.value = state.copy(
                tasks = updatedTasks,
                dirtEvents = updatedDirtList,
                cleanedTrailPoints = updatedTrail,
                robot = robot.copy(
                    posX = nextX,
                    posY = nextY,
                    headingAngle = heading,
                    batteryPercent = newBattery,
                    containerFillPercent = newContainerFill,
                    isBrushActive = true,
                    isMopActive = true,
                    lastCommunicationTime = System.currentTimeMillis()
                )
            )
        }
    }

    private fun handleReturningToStationTick(state: CleanBotAppState, robot: RobotRecord) {
        val stationX = state.station.posX
        val stationY = state.station.posY

        val dx = stationX - robot.posX
        val dy = stationY - robot.posY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance < 2.5f) {
            // Arrived at station!
            val isFullContainer = robot.containerFillPercent >= state.containerReturnThreshold
            val isLowBattery = robot.batteryPercent <= state.batteryReturnThreshold

            val newStatus = if (isFullContainer) {
                RobotStatus.WAITING_FOR_OPERATOR
            } else {
                RobotStatus.CHARGING
            }

            val dockEvent = DockingEvent(
                id = "dock_${System.currentTimeMillis()}",
                eventType = if (isFullContainer) "DOCK_CONTAINER_FULL" else "DOCK_CHARGING",
                details = "Docked at ${state.station.name}. Container: ${robot.containerFillPercent.toInt()}%, Battery: ${robot.batteryPercent.toInt()}%."
            )

            val alert = if (isFullContainer) {
                AlertItem(
                    id = "alt_${System.currentTimeMillis()}",
                    type = NotificationType.CONTAINER_FULL,
                    severity = AlertSeverity.CRITICAL,
                    title = "Waste Container Full - Operator Required",
                    message = "Robot arrived at Service Station. Please empty the container and confirm in app to resume.",
                    actionLabel = "Empty Container"
                )
            } else {
                AlertItem(
                    id = "alt_${System.currentTimeMillis()}",
                    type = NotificationType.RETURN_TO_STATION,
                    severity = AlertSeverity.INFO,
                    title = "Robot Docked at Service Station",
                    message = "Charging initiated automatically at 120W fast dock."
                )
            }

            _appState.value = state.copy(
                robot = robot.copy(
                    posX = stationX,
                    posY = stationY,
                    status = newStatus,
                    isBrushActive = false,
                    isMopActive = false
                ),
                station = state.station.copy(isCharging = (newStatus == RobotStatus.CHARGING)),
                dockingHistory = listOf(dockEvent) + state.dockingHistory,
                alerts = listOf(alert) + state.alerts
            )
        } else {
            // Move towards station
            val angle = (atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat()
            val step = 1.8f
            val nx = robot.posX + cos(angle * Math.PI / 180).toFloat() * step
            val ny = robot.posY + sin(angle * Math.PI / 180).toFloat() * step

            _appState.value = state.copy(
                robot = robot.copy(
                    posX = nx,
                    posY = ny,
                    headingAngle = angle,
                    batteryPercent = (robot.batteryPercent - 0.1f).coerceAtLeast(0f)
                )
            )
        }
    }

    private fun handleChargingTick(state: CleanBotAppState, robot: RobotRecord) {
        val newBattery = (robot.batteryPercent + 2.5f).coerceAtMost(100f)

        if (newBattery >= state.batteryResumeThreshold) {
            // Charged sufficiently
            val unfinishedTask = state.tasks.find { it.status == TaskStatus.PAUSED_FOR_SERVICE }

            if (unfinishedTask != null && state.autoResumeAfterService && robot.containerFillPercent < state.containerReturnThreshold) {
                // Resume task!
                val resumedTasks = state.tasks.map {
                    if (it.id == unfinishedTask.id) it.copy(status = TaskStatus.IN_PROGRESS, pauseReason = null) else it
                }
                val resumeAlert = AlertItem(
                    id = "alt_${System.currentTimeMillis()}",
                    type = NotificationType.CHARGING_COMPLETED,
                    severity = AlertSeverity.SUCCESS,
                    title = "Battery Recharged - Resuming Task",
                    message = "Battery reached ${newBattery.toInt()}%. Auto-resuming ${unfinishedTask.roomName}."
                )

                _appState.value = state.copy(
                    tasks = resumedTasks,
                    robot = robot.copy(
                        batteryPercent = newBattery,
                        status = RobotStatus.CLEANING,
                        currentTaskId = unfinishedTask.id
                    ),
                    station = state.station.copy(isCharging = false),
                    alerts = listOf(resumeAlert) + state.alerts
                )
            } else {
                // Fully charged, available
                _appState.value = state.copy(
                    robot = robot.copy(
                        batteryPercent = newBattery,
                        status = RobotStatus.AVAILABLE
                    ),
                    station = state.station.copy(isCharging = false)
                )
            }
        } else {
            _appState.value = state.copy(
                robot = robot.copy(batteryPercent = newBattery),
                station = state.station.copy(isCharging = true)
            )
        }
    }

    private fun handleAvailableTick(state: CleanBotAppState, robot: RobotRecord) {
        // If there's an unfinished task or queued task, pick it up
        val nextTask = state.tasks.find { it.status == TaskStatus.PAUSED_FOR_SERVICE }
            ?: state.tasks.filter { it.status == TaskStatus.QUEUED }
                .maxByOrNull { it.priority.level }

        if (nextTask != null && robot.batteryPercent > state.batteryReturnThreshold && robot.containerFillPercent < state.containerReturnThreshold) {
            val updatedTasks = state.tasks.map {
                if (it.id == nextTask.id) it.copy(status = TaskStatus.IN_PROGRESS, startTime = System.currentTimeMillis()) else it
            }
            val startAlert = AlertItem(
                id = "alt_${System.currentTimeMillis()}",
                type = NotificationType.TASK_STARTED,
                severity = AlertSeverity.INFO,
                title = "Dispatched Cleaning Task",
                message = "Starting ${nextTask.cleaningType.displayName} in ${nextTask.roomName}."
            )

            _appState.value = state.copy(
                tasks = updatedTasks,
                robot = robot.copy(
                    status = RobotStatus.CLEANING,
                    currentTaskId = nextTask.id,
                    isBrushActive = true,
                    isMopActive = true
                ),
                alerts = listOf(startAlert) + state.alerts
            )
        }
    }

    private fun calculateNextCleaningPosition(
        robot: RobotRecord,
        room: Room?,
        state: CleanBotAppState
    ): Triple<Float, Float, Float> {
        val minX = room?.x ?: 10f
        val maxX = (room?.x ?: 10f) + (room?.width ?: 20f)
        val minY = room?.y ?: 10f
        val maxY = (room?.y ?: 10f) + (room?.height ?: 20f)

        var angle = robot.headingAngle
        val speed = 1.2f
        var nx = robot.posX + cos(angle * Math.PI / 180).toFloat() * speed
        var ny = robot.posY + sin(angle * Math.PI / 180).toFloat() * speed

        // Boundary check or bounce within room
        if (nx < minX + 2f || nx > maxX - 2f || ny < minY + 2f || ny > maxY - 2f) {
            angle = (angle + 120f + Random.nextInt(40)) % 360f
            nx = robot.posX.coerceIn(minX + 2f, maxX - 2f)
            ny = robot.posY.coerceIn(minY + 2f, maxY - 2f)
        }

        // Avoid restricted zones
        for (rz in state.restrictedZones) {
            if (nx in rz.x..(rz.x + rz.width) && ny in rz.y..(rz.y + rz.height)) {
                angle = (angle + 140f) % 360f
                nx = robot.posX
                ny = robot.posY
                break
            }
        }

        // Avoid obstacles
        for (obs in state.obstacles) {
            val dist = hypot(nx - obs.x, ny - obs.y)
            if (dist < obs.radius + 2f) {
                angle = (angle + 90f) % 360f
                nx = robot.posX
                ny = robot.posY
                break
            }
        }

        return Triple(nx, ny, angle)
    }

    private fun generateSimulatedDirtEvent(roomId: String, roomName: String, x: Float, y: Float): DirtDetectionEvent {
        val categories = DirtCategory.values()
        val cat = categories.random()
        val classification = when (cat) {
            DirtCategory.LIQUID_SPILLS -> DirtClassification.WET
            DirtCategory.DUST, DirtCategory.SAND, DirtCategory.PAPER -> DirtClassification.DRY
            DirtCategory.FOOD_PARTICLES -> DirtClassification.MIXED
            DirtCategory.OTHER_DEBRIS -> DirtClassification.UNKNOWN
        }
        val method = when (classification) {
            DirtClassification.WET -> "Absorbent Microfiber Wet Mop + Sanitizing Fluid"
            DirtClassification.DRY -> "High-Suction Dual Brush Sweeper (600 RPM)"
            DirtClassification.MIXED -> "Multi-Pass Sweeping followed by Damp Mop"
            DirtClassification.UNKNOWN -> "Standard High-Torque Cleaning Routine"
        }

        return DirtDetectionEvent(
            id = "dirt_${System.currentTimeMillis()}",
            roomId = roomId,
            roomName = roomName,
            posX = x,
            posY = y,
            category = cat,
            classification = classification,
            confidencePercent = Random.nextInt(85, 99),
            recommendedMethod = method
        )
    }

    // ==========================================
    // WORKFLOWS: CONTAINER & BATTERY
    // ==========================================
    fun triggerFullContainerWorkflow(reason: String = "Container full") {
        val state = _appState.value
        val robot = state.robot ?: return

        // 1. Stop cleaning safely, save unfinished task
        val updatedTasks = state.tasks.map { task ->
            if (task.id == robot.currentTaskId && task.status == TaskStatus.IN_PROGRESS) {
                task.copy(
                    status = TaskStatus.PAUSED_FOR_SERVICE,
                    pauseReason = "Waste container full (${robot.containerFillPercent.toInt()}%)"
                )
            } else task
        }

        val alert = AlertItem(
            id = "alt_${System.currentTimeMillis()}",
            type = NotificationType.RETURN_TO_STATION,
            severity = AlertSeverity.WARNING,
            title = "Returning to Station - Container Full",
            message = "Safely saved task progress. Navigating to Service Station for container servicing."
        )

        _appState.value = state.copy(
            tasks = updatedTasks,
            alerts = listOf(alert) + state.alerts,
            robot = robot.copy(
                status = RobotStatus.RETURNING_TO_STATION,
                isBrushActive = false,
                isMopActive = false
            )
        )
    }

    fun triggerLowBatteryWorkflow(reason: String = "Battery low") {
        val state = _appState.value
        val robot = state.robot ?: return

        val updatedTasks = state.tasks.map { task ->
            if (task.id == robot.currentTaskId && task.status == TaskStatus.IN_PROGRESS) {
                task.copy(
                    status = TaskStatus.PAUSED_FOR_SERVICE,
                    pauseReason = "Low battery (${robot.batteryPercent.toInt()}%)"
                )
            } else task
        }

        val alert = AlertItem(
            id = "alt_${System.currentTimeMillis()}",
            type = NotificationType.LOW_BATTERY,
            severity = AlertSeverity.WARNING,
            title = "Returning to Station - Low Battery",
            message = "Battery below threshold (${robot.batteryPercent.toInt()}%). Returning to station to charge."
        )

        _appState.value = state.copy(
            tasks = updatedTasks,
            alerts = listOf(alert) + state.alerts,
            robot = robot.copy(
                status = RobotStatus.RETURNING_TO_STATION,
                isBrushActive = false,
                isMopActive = false
            )
        )
    }

    fun emptyContainerAndConfirm() {
        val state = _appState.value
        val robot = state.robot ?: return

        val dockEvent = DockingEvent(
            id = "dock_${System.currentTimeMillis()}",
            eventType = "OPERATOR_EMPTIED",
            details = "Operator emptied waste container. Optical fill sensor verified 0% volume."
        )

        val alert = AlertItem(
            id = "alt_${System.currentTimeMillis()}",
            type = NotificationType.OPERATOR_INTERVENTION,
            severity = AlertSeverity.SUCCESS,
            title = "Waste Container Serviced",
            message = "Waste container emptied and verified. Ready to resume operations."
        )

        // Find if there was an unfinished task to resume
        val unfinishedTask = state.tasks.find { it.status == TaskStatus.PAUSED_FOR_SERVICE }

        val (nextStatus, activeTaskId) = if (unfinishedTask != null && robot.batteryPercent > state.batteryReturnThreshold) {
            Pair(RobotStatus.CLEANING, unfinishedTask.id)
        } else {
            Pair(RobotStatus.AVAILABLE, null)
        }

        val updatedTasks = state.tasks.map {
            if (it.id == unfinishedTask?.id && nextStatus == RobotStatus.CLEANING) {
                it.copy(status = TaskStatus.IN_PROGRESS, pauseReason = null)
            } else it
        }

        _appState.value = state.copy(
            robot = robot.copy(
                containerFillPercent = 0f,
                status = nextStatus,
                currentTaskId = activeTaskId,
                isBrushActive = (nextStatus == RobotStatus.CLEANING),
                isMopActive = (nextStatus == RobotStatus.CLEANING)
            ),
            tasks = updatedTasks,
            dockingHistory = listOf(dockEvent) + state.dockingHistory,
            alerts = listOf(alert) + state.alerts
        )
    }

    // ==========================================
    // TASK MANAGEMENT & CRUD
    // ==========================================
    fun createCleaningTask(
        roomId: String,
        cleaningType: CleaningType,
        priority: TaskPriority,
        durationMinutes: Int
    ): CleaningTask {
        val state = _appState.value
        val room = state.rooms.find { it.id == roomId }
        val taskId = "TASK-${Random.nextInt(100, 999)}"

        val newTask = CleaningTask(
            id = taskId,
            locationRoomId = roomId,
            roomName = room?.name ?: "Designated Office Room",
            cleaningType = cleaningType,
            priority = priority,
            expectedDurationMinutes = durationMinutes,
            status = TaskStatus.QUEUED
        )

        val alert = AlertItem(
            id = "alt_${System.currentTimeMillis()}",
            type = NotificationType.NEW_REQUEST,
            severity = AlertSeverity.INFO,
            title = "New Cleaning Request Created",
            message = "$taskId for ${room?.name} queued with ${priority.displayName} priority."
        )

        _appState.value = state.copy(
            tasks = state.tasks + newTask,
            alerts = listOf(alert) + state.alerts
        )

        // If robot is available, trigger it
        if (state.robot?.status == RobotStatus.AVAILABLE) {
            startNextQueuedTask()
        }

        return newTask
    }

    fun startTask(taskId: String) {
        val state = _appState.value
        val robot = state.robot ?: return

        if (robot.status != RobotStatus.AVAILABLE && robot.status != RobotStatus.PAUSED) return

        val updatedTasks = state.tasks.map {
            if (it.id == taskId) it.copy(status = TaskStatus.IN_PROGRESS, startTime = System.currentTimeMillis()) else it
        }

        _appState.value = state.copy(
            tasks = updatedTasks,
            robot = robot.copy(
                status = RobotStatus.CLEANING,
                currentTaskId = taskId,
                isBrushActive = true,
                isMopActive = true
            )
        )
    }

    fun pauseCurrentTask() {
        val state = _appState.value
        val robot = state.robot ?: return

        val updatedTasks = state.tasks.map {
            if (it.id == robot.currentTaskId) it.copy(status = TaskStatus.PAUSED_USER, pauseReason = "Operator paused") else it
        }

        _appState.value = state.copy(
            tasks = updatedTasks,
            robot = robot.copy(
                status = RobotStatus.PAUSED,
                isBrushActive = false,
                isMopActive = false
            )
        )
    }

    fun resumeCurrentTask() {
        val state = _appState.value
        val robot = state.robot ?: return

        val pausedTask = state.tasks.find { it.id == robot.currentTaskId } ?: return

        val updatedTasks = state.tasks.map {
            if (it.id == pausedTask.id) it.copy(status = TaskStatus.IN_PROGRESS, pauseReason = null) else it
        }

        _appState.value = state.copy(
            tasks = updatedTasks,
            robot = robot.copy(
                status = RobotStatus.CLEANING,
                isBrushActive = true,
                isMopActive = true
            )
        )
    }

    fun cancelTask(taskId: String) {
        val state = _appState.value
        val robot = state.robot

        val updatedTasks = state.tasks.map {
            if (it.id == taskId) it.copy(status = TaskStatus.CANCELLED) else it
        }

        val updatedRobot = if (robot?.currentTaskId == taskId) {
            robot.copy(
                status = RobotStatus.AVAILABLE,
                currentTaskId = null,
                isBrushActive = false,
                isMopActive = false
            )
        } else robot

        _appState.value = state.copy(
            tasks = updatedTasks,
            robot = updatedRobot
        )
    }

    private fun startNextQueuedTask() {
        val state = _appState.value
        val next = state.tasks.filter { it.status == TaskStatus.QUEUED }
            .maxByOrNull { it.priority.level } ?: return

        val robot = state.robot ?: return
        if (robot.status != RobotStatus.AVAILABLE) return

        val updatedTasks = state.tasks.map {
            if (it.id == next.id) it.copy(status = TaskStatus.IN_PROGRESS, startTime = System.currentTimeMillis()) else it
        }

        _appState.value = state.copy(
            tasks = updatedTasks,
            robot = robot.copy(
                status = RobotStatus.CLEANING,
                currentTaskId = next.id,
                isBrushActive = true,
                isMopActive = true
            )
        )
    }

    // ==========================================
    // OFFICE MANAGEMENT CRUD
    // ==========================================
    fun addRoom(room: Room) {
        _appState.value = _appState.value.copy(
            rooms = _appState.value.rooms + room
        )
    }

    fun updateRoom(room: Room) {
        _appState.value = _appState.value.copy(
            rooms = _appState.value.rooms.map { if (it.id == room.id) room else it }
        )
    }

    fun deleteRoom(roomId: String) {
        _appState.value = _appState.value.copy(
            rooms = _appState.value.rooms.filterNot { it.id == roomId },
            furniture = _appState.value.furniture.filterNot { it.roomId == roomId }
        )
    }

    fun addFurniture(furniture: Furniture) {
        _appState.value = _appState.value.copy(
            furniture = _appState.value.furniture + furniture
        )
    }

    fun deleteFurniture(furnitureId: String) {
        _appState.value = _appState.value.copy(
            furniture = _appState.value.furniture.filterNot { it.id == furnitureId }
        )
    }

    fun addRestrictedZone(zone: RestrictedZone) {
        _appState.value = _appState.value.copy(
            restrictedZones = _appState.value.restrictedZones + zone
        )
    }

    fun deleteRestrictedZone(zoneId: String) {
        _appState.value = _appState.value.copy(
            restrictedZones = _appState.value.restrictedZones.filterNot { it.id == zoneId }
        )
    }

    fun addObstacle(obstacle: Obstacle) {
        _appState.value = _appState.value.copy(
            obstacles = _appState.value.obstacles + obstacle
        )
    }

    fun removeObstacle(obstacleId: String) {
        _appState.value = _appState.value.copy(
            obstacles = _appState.value.obstacles.filterNot { it.id == obstacleId }
        )
    }

    // ==========================================
    // ROBOT REGISTRATION & SINGLE ROBOT RULE
    // ==========================================
    fun registerRobot(name: String, model: String, type: String) {
        // Enforce single robot rule: cannot add a second robot
        if (_appState.value.isRobotRegistered && _appState.value.robot != null) {
            return
        }
        val newRobot = RobotRecord(
            id = "CB-${Random.nextInt(100, 999)}",
            name = name,
            model = model,
            type = type,
            batteryPercent = 100f,
            containerFillPercent = 0f,
            status = RobotStatus.AVAILABLE
        )
        _appState.value = _appState.value.copy(
            robot = newRobot,
            isRobotRegistered = true
        )
    }

    fun updateRobotConfig(name: String, model: String, suctionLevel: String) {
        val robot = _appState.value.robot ?: return
        _appState.value = _appState.value.copy(
            robot = robot.copy(name = name, model = model, suctionLevel = suctionLevel)
        )
    }

    // ==========================================
    // SIMULATION CONTROLS & DIAGNOSTIC TRIGGERS
    // ==========================================
    fun toggleSimulationPause() {
        _appState.value = _appState.value.copy(
            isSimulationRunning = !_appState.value.isSimulationRunning
        )
    }

    fun setSimulationSpeed(speedMultiplier: Int) {
        _appState.value = _appState.value.copy(
            simulationSpeedMultiplier = speedMultiplier
        )
    }

    fun resetSimulation() {
        _appState.value = createInitialState()
    }

    fun forceFullContainer() {
        val robot = _appState.value.robot ?: return
        _appState.value = _appState.value.copy(
            robot = robot.copy(containerFillPercent = 100f)
        )
        triggerFullContainerWorkflow("Simulated forced full container (100%)")
    }

    fun forceLowBattery() {
        val robot = _appState.value.robot ?: return
        _appState.value = _appState.value.copy(
            robot = robot.copy(batteryPercent = 12f)
        )
        triggerLowBatteryWorkflow("Simulated forced low battery (12%)")
    }

    fun spawnRandomSpill() {
        val room = _appState.value.rooms.randomOrNull() ?: return
        val x = room.x + Random.nextFloat() * (room.width - 4f) + 2f
        val y = room.y + Random.nextFloat() * (room.height - 4f) + 2f
        val event = generateSimulatedDirtEvent(room.id, room.name, x, y)
        _appState.value = _appState.value.copy(
            dirtEvents = _appState.value.dirtEvents + event,
            alerts = listOf(
                AlertItem(
                    id = "alt_${System.currentTimeMillis()}",
                    type = NotificationType.OBSTACLE_DETECTED,
                    severity = AlertSeverity.WARNING,
                    title = "Simulated Spill Detected",
                    message = "${event.category.displayName} (${event.classification.displayName}) identified in ${room.name}."
                )
            ) + _appState.value.alerts
        )
    }

    fun spawnObstacleInPath() {
        val robot = _appState.value.robot ?: return
        val obs = Obstacle(
            id = "obs_${System.currentTimeMillis()}",
            x = robot.posX + 4f,
            y = robot.posY + 4f,
            radius = 3f,
            label = "Office Chair Obstacle"
        )
        _appState.value = _appState.value.copy(
            obstacles = _appState.value.obstacles + obs,
            alerts = listOf(
                AlertItem(
                    id = "alt_${System.currentTimeMillis()}",
                    type = NotificationType.OBSTACLE_DETECTED,
                    severity = AlertSeverity.WARNING,
                    title = "Obstacle Placed in Path",
                    message = "Simulated dynamic obstacle placed near robot coordinates."
                )
            ) + _appState.value.alerts
        )
    }

    // ==========================================
    // ALERTS & ROLE MANAGEMENT
    // ==========================================
    fun markAlertAsRead(alertId: String) {
        _appState.value = _appState.value.copy(
            alerts = _appState.value.alerts.map { if (it.id == alertId) it.copy(isRead = true) else it }
        )
    }

    fun clearAllAlerts() {
        _appState.value = _appState.value.copy(alerts = emptyList())
    }

    fun switchUserRole(role: UserRole) {
        val profile = when (role) {
            UserRole.ADMINISTRATOR -> UserProfile("usr_admin", "Alex Vance", "a.vance@cleanbot.enterprise.io", UserRole.ADMINISTRATOR)
            UserRole.OPERATOR -> UserProfile("usr_op", "Morgan Chen", "m.chen@cleanbot.enterprise.io", UserRole.OPERATOR)
        }
        _appState.value = _appState.value.copy(currentUser = profile)
    }

    fun updateSettings(
        containerThreshold: Float,
        batteryThreshold: Float,
        suctionMode: String,
        autoResume: Boolean
    ) {
        _appState.value = _appState.value.copy(
            containerReturnThreshold = containerThreshold,
            batteryReturnThreshold = batteryThreshold,
            suctionPowerMode = suctionMode,
            autoResumeAfterService = autoResume
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: CleanBotRepository? = null

        fun getInstance(): CleanBotRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CleanBotRepository().also { INSTANCE = it }
            }
        }
    }
}
