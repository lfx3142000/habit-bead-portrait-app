package com.habitbeads.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val CellSize = 48.dp
val HabitColumnWidth = 212.dp

data class Habit(
    val id: Int,
    val name: String,
    val subtitle: String = "",
    val color: Color,
    val target: Int = 1
)

data class DayInfo(
    val dateKey: String,
    val dayLabel: String,
    val dateLabel: String,
    val isToday: Boolean
)

val habitColors = listOf(
    Color(0xFFE76F51),
    Color(0xFFF4A261),
    Color(0xFF2A9D8F),
    Color(0xFF43AA8B),
    Color(0xFF277DA1),
    Color(0xFF6D597A),
    Color(0xFFB56576)
)

fun defaultHabits() = listOf(
    Habit(1, "Morning mobility and posture reset", "Neck, shoulders, hips", Color(0xFF277DA1), target = 1),
    Habit(2, "Stretch", "Quick daily movement", Color(0xFF2A9D8F), target = 1),
    Habit(3, "Read", "At least a few pages", Color(0xFFB56576), target = 1)
)
