package com.example

import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CleanBotWorkflowsUnitTest {

    private lateinit var repository: CleanBotRepository

    @Before
    fun setUp() {
        repository = CleanBotRepository.getInstance()
        repository.resetSimulation()
    }

    @Test
    fun testInitialSingleRobotState() {
        val state = repository.appState.value
        assertNotNull("Single robot should be present", state.robot)
        assertTrue("Robot should be registered", state.isRobotRegistered)
        assertEquals("CB-X100", state.robot?.id)
        assertEquals("Docking Bay STN-01", state.station.name)
    }

    @Test
    fun testSingleRobotRegistrationRule() {
        val stateBefore = repository.appState.value
        val robotIdBefore = stateBefore.robot?.id

        // Attempting to register another robot should be prevented by the singleton rule
        repository.registerRobot("Second Robot", "Model-Fake", "Type-Fake")
        val stateAfter = repository.appState.value

        assertEquals("Second robot registration must be blocked", robotIdBefore, stateAfter.robot?.id)
    }

    @Test
    fun testCreateCleaningTaskAndQueuePriority() {
        val highTask = repository.createCleaningTask(
            roomId = "rm_01",
            cleaningType = CleaningType.COMBINED,
            priority = TaskPriority.HIGH,
            durationMinutes = 20
        )
        assertNotNull(highTask)
        val state = repository.appState.value
        assertTrue("Task should be present in repository", state.tasks.any { it.id == highTask.id })
    }

    @Test
    fun testFullContainerWorkflowTrigger() {
        repository.forceFullContainer()
        val state = repository.appState.value

        assertTrue(
            "Robot should return to station when container is full",
            state.robot?.status == RobotStatus.RETURNING_TO_STATION || state.robot?.status == RobotStatus.WAITING_FOR_OPERATOR
        )
        assertTrue("Critical container full alert should be dispatched", state.alerts.any { it.type == NotificationType.RETURN_TO_STATION || it.type == NotificationType.CONTAINER_FULL })
    }

    @Test
    fun testEmptyContainerWorkflowResumption() {
        // Trigger full container
        repository.forceFullContainer()
        // Confirm operator emptied
        repository.emptyContainerAndConfirm()
        val state = repository.appState.value

        assertEquals("Container fill level should be reset to 0%", 0f, state.robot?.containerFillPercent ?: -1f, 0.01f)
        assertTrue("Docking event should log operator emptying", state.dockingHistory.any { it.eventType == "OPERATOR_EMPTIED" })
    }

    @Test
    fun testRoleSwitching() {
        repository.switchUserRole(UserRole.OPERATOR)
        assertEquals(UserRole.OPERATOR, repository.appState.value.currentUser.role)

        repository.switchUserRole(UserRole.ADMINISTRATOR)
        assertEquals(UserRole.ADMINISTRATOR, repository.appState.value.currentUser.role)
    }
}
