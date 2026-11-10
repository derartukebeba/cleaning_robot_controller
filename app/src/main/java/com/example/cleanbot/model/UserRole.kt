package com.example.cleanbot.model

enum class UserRole(val label: String, val permissions: List<String>) {
    ADMINISTRATOR(
        label = "Administrator",
        permissions = listOf(
            "Manage Users & Permissions",
            "Configure Robot Hardware",
            "Modify Office Layout & Zones",
            "Configure Service Station",
            "Create Automated Schedules",
            "Full System Analytics & Export",
            "System Settings Configuration"
        )
    ),
    OPERATOR(
        label = "Operator",
        permissions = listOf(
            "Monitor Live Telemetry",
            "Create & Dispatch Cleaning Requests",
            "Start, Pause, Resume & Cancel Tasks",
            "Empty Waste Container & Verify",
            "Acknowledge & Respond to Alerts",
            "View Cleaning Progress & History"
        )
    )
}

data class UserProfile(
    val id: String = "usr_01",
    val name: String = "Alex Vance",
    val email: String = "a.vance@cleanbot.enterprise.io",
    val role: UserRole = UserRole.ADMINISTRATOR
)
