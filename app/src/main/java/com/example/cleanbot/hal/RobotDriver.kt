package com.example.cleanbot.hal

interface RobotDriver {
    val driverName: String
    val isHardwareConnected: Boolean
    val driverStatus: String
    val connectionType: String

    fun connect(): Boolean
    fun disconnect()
    fun sendMovementCommand(linearMps: Float, angularRad: Float)
    fun setBrushMotor(enabled: Boolean, rpm: Int)
    fun setSuctionMotor(level: String)
    fun setMopActuator(engaged: Boolean)
    fun readBatteryLevel(): Float
    fun readContainerLevel(): Float
    fun readLidarScan(): List<Float>
    fun readDirtSensor(): String?
    fun triggerEmptyContainerVerification(): Boolean
    fun emergencyStop()
}
