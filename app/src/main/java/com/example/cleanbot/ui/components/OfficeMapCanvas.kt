package com.example.cleanbot.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleanbot.model.*
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun OfficeMapCanvas(
    rooms: List<Room>,
    furniture: List<Furniture>,
    restrictedZones: List<RestrictedZone>,
    obstacles: List<Obstacle>,
    serviceStation: ServiceStation,
    robot: RobotRecord?,
    cleanedTrail: List<Pair<Float, Float>>,
    plannedWaypoints: List<Pair<Float, Float>>,
    onRoomSelected: (Room) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Lidar rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "lidar_sweep")
    val lidarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lidar_angle"
    )

    // Pulse animation for robot ring
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(Navy950, RoundedCornerShape(16.dp))
            .padding(8.dp)
            .testTag("office_map_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rooms) {
                    detectTapGestures { offset ->
                        val pctX = (offset.x / size.width) * 100f
                        val pctY = (offset.y / size.height) * 100f

                        val tappedRoom = rooms.find { r ->
                            pctX in r.x..(r.x + r.width) && pctY in r.y..(r.y + r.height)
                        }
                        if (tappedRoom != null) {
                            onRoomSelected(tappedRoom)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            fun toPixelX(pct: Float) = (pct / 100f) * canvasW
            fun toPixelY(pct: Float) = (pct / 100f) * canvasH

            // 1. Draw subtle background architectural grid
            drawGrid(canvasW, canvasH)

            // 2. Draw Rooms
            for (room in rooms) {
                val rx = toPixelX(room.x)
                val ry = toPixelY(room.y)
                val rw = toPixelX(room.width)
                val rh = toPixelY(room.height)

                val roomBgColor = if (room.isCleaned) Color(0xFF132A38) else Color(0xFF172033)
                val roomBorderColor = if (room.isCleaned) StatusGreen.copy(alpha = 0.5f) else Navy700

                // Room Fill
                drawRoundRect(
                    color = roomBgColor,
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Room Outline
                drawRoundRect(
                    color = roomBorderColor,
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh),
                    cornerRadius = CornerRadius(12f, 12f),
                    style = Stroke(width = 2f)
                )

                // Room Name Label
                val measured = textMeasurer.measure(
                    text = AnnotatedString(room.name),
                    style = TextStyle(color = TextPrimary.copy(alpha = 0.85f), fontSize = 10.sp)
                )
                drawText(measured, topLeft = Offset(rx + 8f, ry + 6f))

                // Priority Badge
                val priorityColor = when (room.priority) {
                    TaskPriority.URGENT -> StatusRose
                    TaskPriority.HIGH -> StatusAmber
                    TaskPriority.MEDIUM -> ElectricBlue
                    TaskPriority.LOW -> TextMuted
                }
                drawCircle(
                    color = priorityColor,
                    radius = 4f,
                    center = Offset(rx + rw - 12f, ry + 12f)
                )
            }

            // 3. Draw Furniture (Desks, Meeting Tables)
            for (fur in furniture) {
                val fx = toPixelX(fur.x)
                val fy = toPixelY(fur.y)
                val fw = toPixelX(fur.width)
                val fh = toPixelY(fur.height)

                val furColor = when (fur.type) {
                    FurnitureType.MEETING_TABLE -> Color(0xFF2C3E50)
                    FurnitureType.DESK -> Color(0xFF1E2E42)
                    FurnitureType.COUCH -> Color(0xFF283747)
                    FurnitureType.CABINET -> Color(0xFF212F3D)
                }

                drawRoundRect(
                    color = furColor,
                    topLeft = Offset(fx, fy),
                    size = Size(fw, fh),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Navy600.copy(alpha = 0.6f),
                    topLeft = Offset(fx, fy),
                    size = Size(fw, fh),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 1.2f)
                )

                if (fw > 30f && fh > 20f) {
                    val furText = textMeasurer.measure(
                        text = AnnotatedString(fur.name.take(10)),
                        style = TextStyle(color = TextSecondary, fontSize = 8.sp)
                    )
                    drawText(furText, topLeft = Offset(fx + 4f, fy + 4f))
                }
            }

            // 4. Draw Restricted Zones (Red diagonal hazard hatching)
            for (rz in restrictedZones) {
                val zx = toPixelX(rz.x)
                val zy = toPixelY(rz.y)
                val zw = toPixelX(rz.width)
                val zh = toPixelY(rz.height)

                // Fill with transparent rose
                drawRect(
                    color = Color(0x33EF4444),
                    topLeft = Offset(zx, zy),
                    size = Size(zw, zh)
                )

                // Hazard stripes
                var sx = zx
                while (sx < zx + zw + zh) {
                    drawLine(
                        color = Color(0x88EF4444),
                        start = Offset(sx, zy),
                        end = Offset(sx - zh, zy + zh),
                        strokeWidth = 2f
                    )
                    sx += 16f
                }

                // Border
                drawRect(
                    color = StatusRose,
                    topLeft = Offset(zx, zy),
                    size = Size(zw, zh),
                    style = Stroke(width = 2f)
                )

                val rzText = textMeasurer.measure(
                    text = AnnotatedString("RESTRICTED: ${rz.name}"),
                    style = TextStyle(color = StatusRose, fontSize = 9.sp)
                )
                drawText(rzText, topLeft = Offset(zx + 6f, zy + 4f))
            }

            // 5. Draw Cleaned Trail (Cyan path)
            if (cleanedTrail.size > 1) {
                val path = Path()
                path.moveTo(toPixelX(cleanedTrail.first().first), toPixelY(cleanedTrail.first().second))
                for (pt in cleanedTrail.drop(1)) {
                    path.lineTo(toPixelX(pt.first), toPixelY(pt.second))
                }
                drawPath(
                    path = path,
                    color = CyanAccent.copy(alpha = 0.35f),
                    style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 6. Draw Planned Waypoints (Dashed Blue path)
            if (plannedWaypoints.isNotEmpty() && robot?.status == RobotStatus.CLEANING) {
                val path = Path()
                path.moveTo(toPixelX(robot.posX), toPixelY(robot.posY))
                for (wp in plannedWaypoints) {
                    path.lineTo(toPixelX(wp.first), toPixelY(wp.second))
                }
                drawPath(
                    path = path,
                    color = ElectricBlue.copy(alpha = 0.5f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))
                )
            }

            // 7. Draw Obstacles (Yellow caution indicators)
            for (obs in obstacles) {
                val ox = toPixelX(obs.x)
                val oy = toPixelY(obs.y)
                val rad = 14f

                drawCircle(color = StatusAmberBg, radius = rad, center = Offset(ox, oy))
                drawCircle(color = StatusAmber, radius = rad, center = Offset(ox, oy), style = Stroke(width = 2f))
                drawCircle(color = StatusAmber, radius = 4f, center = Offset(ox, oy))

                val obsText = textMeasurer.measure(
                    text = AnnotatedString("! ${obs.label}"),
                    style = TextStyle(color = StatusAmber, fontSize = 8.sp)
                )
                drawText(obsText, topLeft = Offset(ox - rad, oy + rad + 2f))
            }

            // 8. Draw Service Station STN-ALPHA-01
            val stnX = toPixelX(serviceStation.posX)
            val stnY = toPixelY(serviceStation.posY)

            drawCircle(
                color = ElectricBlue.copy(alpha = 0.2f),
                radius = 24f,
                center = Offset(stnX, stnY)
            )
            drawRoundRect(
                color = ElectricBlue.copy(alpha = 0.8f),
                topLeft = Offset(stnX - 16f, stnY - 16f),
                size = Size(32f, 32f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = if (serviceStation.isCharging) StatusAmber else CyanAccent,
                radius = 6f,
                center = Offset(stnX, stnY)
            )

            val stnLabel = textMeasurer.measure(
                text = AnnotatedString("DOCK STN-01"),
                style = TextStyle(color = CyanAccent, fontSize = 8.sp)
            )
            drawText(stnLabel, topLeft = Offset(stnX - 24f, stnY + 20f))

            // 9. Draw Robot (CleanBot Prime)
            if (robot != null) {
                val rx = toPixelX(robot.posX)
                val ry = toPixelY(robot.posY)
                val robotRadius = 18f

                // Status Ring Color
                val statusColor = when (robot.status) {
                    RobotStatus.CLEANING -> CyanAccent
                    RobotStatus.AVAILABLE -> StatusGreen
                    RobotStatus.CHARGING -> StatusAmber
                    RobotStatus.RETURNING_TO_STATION -> ElectricBlue
                    RobotStatus.WAITING_FOR_OPERATOR -> StatusRose
                    RobotStatus.PAUSED -> StatusAmber
                    RobotStatus.ERROR -> StatusRose
                    RobotStatus.OFFLINE -> TextMuted
                }

                // Lidar Sweep cone when active
                if (robot.status == RobotStatus.CLEANING || robot.status == RobotStatus.RETURNING_TO_STATION) {
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(CyanAccent.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(rx, ry),
                            radius = 60f
                        ),
                        startAngle = lidarAngle,
                        sweepAngle = 45f,
                        useCenter = true,
                        topLeft = Offset(rx - 60f, ry - 60f),
                        size = Size(120f, 120f)
                    )
                }

                // Outer animated pulse ring
                drawCircle(
                    color = statusColor.copy(alpha = pulseAlpha * 0.4f),
                    radius = robotRadius + 8f,
                    center = Offset(rx, ry)
                )

                // Robot Chassis
                drawCircle(
                    color = Navy900,
                    radius = robotRadius,
                    center = Offset(rx, ry)
                )
                drawCircle(
                    color = statusColor,
                    radius = robotRadius,
                    center = Offset(rx, ry),
                    style = Stroke(width = 3f)
                )

                // Inner core
                drawCircle(
                    color = CyanBright,
                    radius = 5f,
                    center = Offset(rx, ry)
                )

                // Heading pointer line / arrow
                val rad = robot.headingAngle * (Math.PI / 180.0)
                val pointerLen = robotRadius + 8f
                val hx = rx + cos(rad).toFloat() * pointerLen
                val hy = ry + sin(rad).toFloat() * pointerLen

                drawLine(
                    color = CyanAccent,
                    start = Offset(rx, ry),
                    end = Offset(hx, hy),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Status label above robot
                val statusText = textMeasurer.measure(
                    text = AnnotatedString("${robot.name} [${robot.status.displayName}]"),
                    style = TextStyle(color = TextPrimary, fontSize = 9.sp)
                )
                drawText(statusText, topLeft = Offset(rx - 45f, ry - robotRadius - 16f))
            }
        }

        // Overlay Legend / Quick Stats Pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Navy900.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(8.dp).background(CyanAccent, RoundedCornerShape(4.dp)))
            Text("Cleaned Area", color = TextSecondary, fontSize = 11.sp)

            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(8.dp).background(StatusRose, RoundedCornerShape(4.dp)))
            Text("Restricted Zone", color = TextSecondary, fontSize = 11.sp)

            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(8.dp).background(StatusAmber, RoundedCornerShape(4.dp)))
            Text("Obstacle", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

private fun DrawScope.drawGrid(w: Float, h: Float) {
    val step = 40f
    var x = step
    while (x < w) {
        drawLine(
            color = Navy800.copy(alpha = 0.4f),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }
    var y = step
    while (y < h) {
        drawLine(
            color = Navy800.copy(alpha = 0.4f),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}
