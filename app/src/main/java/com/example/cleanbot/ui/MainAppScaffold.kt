package com.example.cleanbot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.data.CleanBotAppState
import com.example.cleanbot.data.CleanBotRepository
import com.example.cleanbot.model.UserRole
import com.example.cleanbot.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    MAP("Navigation Map", Icons.Default.Map),
    REQUESTS("Cleaning Operations", Icons.Default.CleaningServices),
    ROBOT("Robot Management", Icons.Default.SmartToy),
    OFFICE("Office & Zones", Icons.Default.MeetingRoom),
    DIRT("Dirt Detection", Icons.Default.Opacity),
    CONTAINER("Container & Battery", Icons.Default.DeleteOutline),
    STATION("Service Station", Icons.Default.EvStation),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    HISTORY("Cleaning History", Icons.Default.History),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    state: CleanBotAppState,
    repository: CleanBotRepository
) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle back button: return to dashboard if on another screen
    BackHandler(enabled = currentDestination != AppDestination.DASHBOARD) {
        currentDestination = AppDestination.DASHBOARD
    }

    val unreadAlerts = state.alerts.count { !it.isRead }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Navy900,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy800)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Navy950)
                            }
                            Column {
                                Text("CleanBot AI", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Autonomous Facility System", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Role Selector in drawer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Navy900, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .clickable {
                                    val nextRole = if (state.currentUser.role == UserRole.ADMINISTRATOR) UserRole.OPERATOR else UserRole.ADMINISTRATOR
                                    repository.switchUserRole(nextRole)
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Role (Tap to switch):", color = TextMuted, fontSize = 10.sp)
                                Text(state.currentUser.role.label, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Switch", tint = CyanAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                HorizontalDivider(color = Navy700)

                // Navigation items list
                Spacer(Modifier.height(8.dp))
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) CyanAccent else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                destination.label,
                                color = if (isSelected) CyanAccent else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        badge = {
                            if (destination == AppDestination.NOTIFICATIONS && unreadAlerts > 0) {
                                Badge(containerColor = StatusRose) {
                                    Text("$unreadAlerts", color = TextPrimary, fontSize = 10.sp)
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            currentDestination = destination
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Navy800,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("drawer_item_${destination.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "CleanBot AI",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen)
                            )
                            Text(
                                text = "• " + currentDestination.label,
                                color = CyanAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_drawer_menu")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = TextPrimary)
                        }
                    },
                    actions = {
                        // Quick Role Toggle Pill
                        Surface(
                            onClick = {
                                val next = if (state.currentUser.role == UserRole.ADMINISTRATOR) UserRole.OPERATOR else UserRole.ADMINISTRATOR
                                repository.switchUserRole(next)
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Navy800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (state.currentUser.role == UserRole.ADMINISTRATOR) "Admin" else "Operator",
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Notification Icon with Badge
                        IconButton(
                            onClick = { currentDestination = AppDestination.NOTIFICATIONS },
                            modifier = Modifier.testTag("btn_notifications_bell")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadAlerts > 0) {
                                        Badge(containerColor = StatusRose) {
                                            Text("$unreadAlerts", color = TextPrimary)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = TextPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy900,
                        titleContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Navy900,
                    contentColor = TextPrimary,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val bottomItems = listOf(
                        AppDestination.DASHBOARD,
                        AppDestination.MAP,
                        AppDestination.REQUESTS,
                        AppDestination.STATION,
                        AppDestination.NOTIFICATIONS
                    )

                    bottomItems.forEach { item ->
                        val isSelected = currentDestination == item
                        NavigationBarItem(
                            icon = {
                                if (item == AppDestination.NOTIFICATIONS && unreadAlerts > 0) {
                                    BadgedBox(badge = { Badge(containerColor = StatusRose) { Text("$unreadAlerts") } }) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label)
                                }
                            },
                            label = { Text(item.label.split(" ").first(), fontSize = 10.sp) },
                            selected = isSelected,
                            onClick = { currentDestination = item },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy950,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("bottom_nav_${item.name.lowercase()}")
                        )
                    }
                }
            },
            containerColor = Navy900
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Navy900)
            ) {
                when (currentDestination) {
                    AppDestination.DASHBOARD -> DashboardScreen(
                        state = state,
                        repository = repository,
                        onNavigateToMap = { currentDestination = AppDestination.MAP },
                        onNavigateToQueue = { currentDestination = AppDestination.REQUESTS },
                        onNavigateToAlerts = { currentDestination = AppDestination.NOTIFICATIONS },
                        onNavigateToStation = { currentDestination = AppDestination.STATION }
                    )
                    AppDestination.MAP -> LiveMapScreen(state = state, repository = repository)
                    AppDestination.REQUESTS -> CleaningRequestsScreen(state = state, repository = repository)
                    AppDestination.ROBOT -> RobotManagementScreen(state = state, repository = repository)
                    AppDestination.OFFICE -> OfficeManagementScreen(state = state, repository = repository)
                    AppDestination.DIRT -> DirtDetectionScreen(state = state, repository = repository)
                    AppDestination.CONTAINER -> ContainerBatteryScreen(state = state, repository = repository)
                    AppDestination.STATION -> ServiceStationScreen(state = state, repository = repository)
                    AppDestination.NOTIFICATIONS -> NotificationsScreen(state = state, repository = repository)
                    AppDestination.HISTORY -> CleaningHistoryScreen(state = state)
                    AppDestination.ANALYTICS -> AnalyticsScreen(state = state)
                    AppDestination.SETTINGS -> SettingsScreen(state = state, repository = repository)
                }
            }
        }
    }
}
