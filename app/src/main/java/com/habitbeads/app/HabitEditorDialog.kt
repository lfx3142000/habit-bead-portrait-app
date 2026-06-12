package com.habitbeads.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitEditorDialog(
    title: String,
    initialHabit: Habit?,
    colorOptions: List<Color> = habitColors,
    premiumUnlocked: Boolean,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Color) -> Unit,
    onPremiumRequested: () -> Unit,
    onRequestDelete: (() -> Unit)?
) {
    var habitName by remember { mutableStateOf(initialHabit?.name ?: "") }
    var habitSubtitle by remember { mutableStateOf(initialHabit?.subtitle ?: "") }
    var selectedColor by remember { mutableStateOf(initialHabit?.color ?: colorOptions.first()) }
    var red by remember { mutableStateOf(android.graphics.Color.red(selectedColor.toArgb()).toFloat()) }
    var green by remember { mutableStateOf(android.graphics.Color.green(selectedColor.toArgb()).toFloat()) }
    var blue by remember { mutableStateOf(android.graphics.Color.blue(selectedColor.toArgb()).toFloat()) }

    fun setCustomColor() {
        selectedColor = Color(red / 255f, green / 255f, blue / 255f)
    }

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
                    colorOptions.forEachIndexed { index, color ->
                        val selected = color.toArgb() == selectedColor.toArgb()
                        val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else color
                        Box(
                            modifier = Modifier
                                .size(if (selected) 30.dp else 26.dp)
                                .clip(CircleShape)
                                .background(borderColor)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .semantics { contentDescription = "Select habit color ${index + 1}" }
                                .combinedClickable(onClick = {
                                    selectedColor = color
                                    red = android.graphics.Color.red(color.toArgb()).toFloat()
                                    green = android.graphics.Color.green(color.toArgb()).toFloat()
                                    blue = android.graphics.Color.blue(color.toArgb()).toFloat()
                                })
                        )
                    }
                }
                if (premiumUnlocked) {
                    Text("Custom color", style = MaterialTheme.typography.bodyMedium)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(selectedColor)
                    )
                    ColorSlider(label = "Red", value = red, onValueChange = { red = it; setCustomColor() })
                    ColorSlider(label = "Green", value = green, onValueChange = { green = it; setCustomColor() })
                    ColorSlider(label = "Blue", value = blue, onValueChange = { blue = it; setCustomColor() })
                } else {
                    OutlinedButton(onClick = onPremiumRequested) { Text("Custom color requires premium") }
                }
                if (onRequestDelete != null) {
                    TextButton(onClick = onRequestDelete) { Text("Delete habit") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(habitName, habitSubtitle, selectedColor) }) { Text(confirmText) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$label ${value.roundToInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
