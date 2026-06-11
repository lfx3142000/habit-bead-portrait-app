package com.habitbeads.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DayHeader(day: DayInfo, cellSize: Dp = CellSize) {
    Column(
        modifier = Modifier
            .width(cellSize)
            .size(width = cellSize, height = 44.dp)
            .semantics {
                contentDescription = if (day.isToday) {
                    "Today, ${day.dayLabel} ${day.dateLabel}"
                } else {
                    "${day.dayLabel} ${day.dateLabel}"
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            day.dayLabel.take(1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            day.dateLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeadCell(
    habitName: String,
    day: DayInfo,
    count: Int,
    color: Color,
    isToday: Boolean,
    showNumber: Boolean,
    cellSize: Dp = CellSize,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val beadColor = if (count > 0) beadHueForCount(color, count) else Color(0xFFF0F3F8)
    val beadModifier = Modifier
        .size(cellSize)
        .padding(6.dp)
        .clip(CircleShape)
        .background(beadColor)
        .then(
            if (count == 0 && isToday) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            } else {
                Modifier
            }
        )
        .semantics {
            contentDescription = "$habitName on ${day.dayLabel} ${day.dateLabel}: $count beads. Tap to add one. Long press to subtract one."
            role = Role.Button
        }
        .combinedClickable(onClick = onIncrement, onLongClick = onDecrement)

    Box(modifier = beadModifier, contentAlignment = Alignment.Center) {
        if (showNumber && count > 0) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (count >= 5) Color.White.copy(alpha = 0.94f) else Color(0xFF2F343B)
            )
        }
    }
}

private fun beadHueForCount(base: Color, count: Int): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(base.toArgb(), hsv)
    val level = count.coerceIn(1, 9)
    val progress = (level - 1) / 8f
    hsv[0] = (hsv[0] + (progress * 54f) + 360f) % 360f
    hsv[1] = (0.18f + progress * 0.66f).coerceIn(0.18f, 0.84f)
    hsv[2] = (0.99f - progress * 0.21f).coerceIn(0.78f, 0.99f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
