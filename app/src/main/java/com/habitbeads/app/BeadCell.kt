package com.habitbeads.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DayHeader(day: DayInfo) {
    val background = if (day.isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .width(CellSize)
            .size(width = CellSize, height = 38.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .semantics { contentDescription = if (day.isToday) "Today, ${day.dayLabel} ${day.dateLabel}" else "${day.dayLabel} ${day.dateLabel}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(day.dayLabel, style = MaterialTheme.typography.labelSmall)
        Text(day.dateLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val background = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        count > 0 -> color.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    Box(
        modifier = Modifier
            .size(CellSize)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .semantics {
                contentDescription = "$habitName on ${day.dayLabel} ${day.dateLabel}: $count beads. Tap to add one. Long press to subtract one."
                role = Role.Button
            }
            .combinedClickable(onClick = onIncrement, onLongClick = onDecrement),
        contentAlignment = Alignment.Center
    ) { BeadCluster(count, color) }
}

@Composable
private fun BeadCluster(count: Int, color: Color) {
    if (count == 0) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)))
        return
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        beadRows(count).forEach { rowCount ->
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(rowCount) { Box(modifier = Modifier.padding(1.dp).size(6.dp).clip(CircleShape).background(color)) }
            }
        }
    }
}

private fun beadRows(count: Int): List<Int> = when (count.coerceIn(0, 9)) {
    1 -> listOf(1)
    2 -> listOf(2)
    3 -> listOf(1, 2)
    4 -> listOf(2, 2)
    5 -> listOf(2, 1, 2)
    6 -> listOf(3, 3)
    7 -> listOf(2, 3, 2)
    8 -> listOf(3, 2, 3)
    9 -> listOf(3, 3, 3)
    else -> emptyList()
}
