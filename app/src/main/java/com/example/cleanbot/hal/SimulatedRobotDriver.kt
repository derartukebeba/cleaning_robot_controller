package com.example.cleanbot.hal

import kotlin.random.Random

class SimulatedRobotDriver : RobotDriver {
    override val driverName: String = "CleanBot-SimDriver v2.4"
    override val isHardwareConnected: Boolean = true
    override val driverStatus: String = "Operational (Simulation Mode Active)"
    override val connectionType: String = "Virtual Loopback Bus"

    private var currentBattery: Float = 94f
    private var currentContainer: Float = 28f
    private var isBrushRunning: Boolean = false
    private var suctionLevel: String = "Standard"
    private var isMopDown: Boolean = false

    override fun connect(): Boolean = true
    override fun disconnect() {}

    override fun sendMovementCommand(linearMps: Float, angularRad: Float) {
        // Simulated kinematic execution
    }

    override fun setBrushMotor(enabled: Boolean, rpm: Int) {
        isBrushRunning = enabled
    }

    override fun setSuctionMotor(level: String) {
        suctionLevel = level
    }

    override fun setMopActuator(engaged: Boolean) {
        isMopDown = engaged
    }

    override fun readBatteryLevel(): Float = currentBattery

    override fun readContainerLevel(): Float = currentContainer

    fun updateTelemetry(battery: Float, container: Float) {
        currentBattery = battery
        currentContainer = container
    }

    override fun readLidarScan(): List<Float> {
        // Returns simulated 360-degree range rays (samples every 45 deg)
        return List(8) { Random.nextFloat() * 4.0f + 0.8f }
    }

    override fun readDirtSensor(): String? {
        return if (Random.nextFloat() < 0.15f) "Simulated Dust Detection" else null
    }

    override fun triggerEmptyContainerVerification(): Boolean {
        currentContainer = 0f
        return true
    }

    override fun emergencyStop() {
        isBrushRunning = false
        isMopDown = false
    }
}
