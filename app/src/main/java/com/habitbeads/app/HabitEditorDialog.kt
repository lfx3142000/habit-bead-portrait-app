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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitEditorDialog(
    title: String,
    initialHabit: Habit?,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Color) -> Unit,
    onRequestDelete: (() -> Unit)?
) {
    var habitName by remember { mutableStateOf(initialHabit?.name ?: "") }
    var habitSubtitle by remember { mutableStateOf(initialHabit?.subtitle ?: "") }
    var colorIndex by remember { mutableIntStateOf(habitColors.indexOf(initialHabit?.color).takeIf { it >= 0 } ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it.take(60) },
                    singleLine = true,
                    label = { Text("Habit title") }
                )
                OutlinedTextField(
                    value = habitSubtitle,
                    onValueChange = { habitSubtitle = it.take(70) },
                    singleLine = true,
                    label = { Text("Subtitle, optional") }
                )
                Text("Color", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    habitColors.forEachIndexed { index, color ->
                        val borderColor = if (index == colorIndex) MaterialTheme.colorScheme.onSurface else color
                        Box(
                            modifier = Modifier
                                .size(if (index == colorIndex) 30.dp else 26.dp)
                                .clip(CircleShape)
                                .background(borderColor)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .semantics { contentDescription = "Select habit color ${index + 1}" }
                                .combinedClickable(onClick = { colorIndex = index })
                        )
                    }
                }
                if (onRequestDelete != null) {
                    TextButton(onClick = onRequestDelete) { Text("Delete habit") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(habitName, habitSubtitle, habitColors[colorIndex]) }) { Text(confirmText) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
