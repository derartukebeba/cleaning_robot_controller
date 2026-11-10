package com.example.cleanbot.hal

class PhysicalRobotDriver(
    val brokerEndpoint: String = "ssl://mqtt.cleanbot.corp:8883",
    val clientId: String = "CleanBot-Gateway-01",
    val baseTopic: String = "cleanbot/robot/CB-X100"
) : RobotDriver {
    override val driverName: String = "Physical-MQTT-Driver (Stub / Standby)"
    override val isHardwareConnected: Boolean = false
    override val driverStatus: String = "Awaiting Physical Hardware & Safety Verification"
    override val connectionType: String = "MQTT / WebSocket Gateway (Protocol v3.1.1)"

    override fun connect(): Boolean = false
    override fun disconnect() {}
    override fun sendMovementCommand(linearMps: Float, angularRad: Float) {}
    override fun setBrushMotor(enabled: Boolean, rpm: Int) {}
    override fun setSuctionMotor(level: String) {}
    override fun setMopActuator(engaged: Boolean) {}
    override fun readBatteryLevel(): Float = 0f
    override fun readContainerLevel(): Float = 0f
    override fun readLidarScan(): List<Float> = emptyList()
    override fun readDirtSensor(): String? = null
    override fun triggerEmptyContainerVerification(): Boolean = false
    override fun emergencyStop() {}
}
