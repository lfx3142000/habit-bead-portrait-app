package com.habitbeads.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitNameCell(
    habit: Habit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var didDrag by remember { mutableStateOf(false) }
    val rowBackground = if (isDragging) {
        habit.color.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    Row(
        modifier = Modifier
            .height(CellSize)
            .fillMaxWidth()
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .semantics {
                contentDescription = buildString {
                    append("Habit ${habit.name}")
                    if (habit.subtitle.isNotBlank()) append(", ${habit.subtitle}")
                    append(". Tap to edit.")
                }
                role = Role.Button
            }
            .combinedClickable(onClick = {
                if (didDrag) didDrag = false else onEdit()
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DragGrip(
            habitName = habit.name,
            modifier = Modifier.pointerInput(habit.id, canMoveUp, canMoveDown) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragDistance = 0f
                        isDragging = true
                        didDrag = true
                    },
                    onDragEnd = {
                        dragDistance = 0f
                        isDragging = false
                    },
                    onDragCancel = {
                        dragDistance = 0f
                        isDragging = false
                    },
                    onDrag = { _, dragAmount ->
                        dragDistance += dragAmount.y
                        if (dragDistance > 32f && canMoveDown) {
                            onMoveDown()
                            dragDistance = 0f
                        } else if (dragDistance < -32f && canMoveUp) {
                            onMoveUp()
                            dragDistance = 0f
                        }
                    }
                )
            }
        )
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(habit.color))
        Spacer(modifier = Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                habit.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (habit.subtitle.isNotBlank()) {
                Text(
                    habit.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DragGrip(habitName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(24.dp)
            .height(CellSize)
            .semantics { contentDescription = "Reorder $habitName" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        repeat(3) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.75f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
